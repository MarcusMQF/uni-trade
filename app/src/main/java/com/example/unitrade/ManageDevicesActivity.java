package com.example.unitrade;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageDevicesActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private List<Device> devices;
    private DevicesAdapter adapter;
    private String currentLocation = "Unknown Location";

    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_devices);

        Toolbar toolbar = findViewById(R.id.appBarManageDevices);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Devices");
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        }
        tintToolbarOverflow(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView recyclerView = findViewById(R.id.rvDevices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        devices = new ArrayList<>();
        // Add some mock devices for demonstration purposes.
        // In a real application, this data would come from a backend server.
        devices.add(new Device("MacBook Pro", "Selangor, Malaysia", "Active 2 hours ago", false, "laptop"));
        devices.add(new Device("iPhone 12", "Penang, Malaysia", "Active 1 day ago", false, "phone"));

        adapter = new DevicesAdapter(devices, this::showLogoutConfirmationDialog);
        recyclerView.setAdapter(adapter);

        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                addCurrentDevice("Location Permission Denied");
            }
        }
    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastKnownLocation != null) {
                    getAddressFromLocation(lastKnownLocation);
                } else {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            getAddressFromLocation(location);
                        }
                        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                        @Override public void onProviderEnabled(@NonNull String provider) {}
                        @Override public void onProviderDisabled(@NonNull String provider) {
                            addCurrentDevice("Location Disabled");
                        }
                    }, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                addCurrentDevice("Unknown Location");
            }
        }
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Construct location string: e.g., "Kuala Lumpur, Malaysia"
                String city = address.getLocality();
                String country = address.getCountryName();
                if (city != null && country != null) {
                    currentLocation = city + ", " + country;
                } else if (country != null) {
                    currentLocation = country;
                } else {
                    currentLocation = "Unknown Location";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            currentLocation = "Unknown Location";
        }
        addCurrentDevice(currentLocation);
    }

    private void addCurrentDevice(String location) {
        String deviceName = getDeviceName();
        // Check if already added to avoid duplicates if location updates multiple times
        boolean exists = false;
        for (Device d : devices) {
            if (d.isCurrent()) {
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            // Add current device to the top of the list
            devices.add(0, new Device(deviceName, location, "Active now", true, "phone"));
            adapter.notifyDataSetChanged();
        }
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void showLogoutConfirmationDialog(Device device) {
        if (device.isCurrent()) {
            Toast.makeText(this, "You cannot log out from the current device.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Log Out Device")
                .setMessage("Are you sure you want to log out from " + device.getName() + "?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    int position = devices.indexOf(device);
                    if (position != -1) {
                        devices.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Logged out from " + device.getName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder> {

        private List<Device> devices;
        private final OnDeviceClickListener listener;

        public DevicesAdapter(List<Device> devices, OnDeviceClickListener listener) {
            this.devices = devices;
            this.listener = listener;
        }

        @NonNull
        @Override
        public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
            return new DeviceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
            Device device = devices.get(position);
            holder.bind(device);
            holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        static class DeviceViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView name, location, status;

            public DeviceViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.imgDeviceIcon);
                name = itemView.findViewById(R.id.txtDeviceName);
                location = itemView.findViewById(R.id.txtDeviceLocation);
                status = itemView.findViewById(R.id.txtDeviceStatus);
            }

            public void bind(Device device) {
                name.setText(device.getName());
                location.setText(device.getLocation());
                
                if (device.isCurrent()) {
                    status.setText("This device");
                    status.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                } else {
                    status.setText(device.getLastActive());
                    status.setTextColor(0xFF757575); // Grey color
                }

                if ("laptop".equalsIgnoreCase(device.getType())) {
                    icon.setImageResource(R.drawable.ic_website); // Assuming website/laptop icon
                } else {
                    icon.setImageResource(R.drawable.ic_phone);
                }
            }
        }
    }
}
