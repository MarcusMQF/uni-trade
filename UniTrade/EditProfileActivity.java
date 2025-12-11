package com.example.unitrade;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;

    private TextInputEditText etFullName, etUsername, etEmail, etPhone, etNewAddress, eTBio;
    private Button btnSaveProfile, btnAddAddress;
    private ImageButton btnFetchLocation;
    private RecyclerView rvAddresses;

    private User currentUser;
    private AddressAdapter addressAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        currentUser = getIntent().getParcelableExtra("user_to_edit");
        if (currentUser == null) {
            // Create a dummy user if none is passed
            currentUser = new User("dummyId", "dummyUser", "", 0, 0, 0, "");
            currentUser.setAddresses(new ArrayList<>());
        }

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etNewAddress = findViewById(R.id.etNewAddress);
        eTBio = findViewById(R.id.eTBio);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnFetchLocation = findViewById(R.id.btnFetchLocation);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        rvAddresses = findViewById(R.id.rvAddresses);

        etFullName.setText(currentUser.getFullName());
        etUsername.setText(currentUser.getUsername());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhoneNumber());
        eTBio.setText(currentUser.getBio());

        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(this, currentUser.getAddresses());
        rvAddresses.setAdapter(addressAdapter);

        btnFetchLocation.setOnClickListener(v -> fetchLocation());

        btnAddAddress.setOnClickListener(v -> {
            String newAddress = etNewAddress.getText().toString();
            if (!newAddress.isEmpty()) {
                addressAdapter.addAddress(new com.example.unitrade.Address(newAddress, false));
                etNewAddress.setText("");
            }
        });

        btnSaveProfile.setOnClickListener(v -> {
            currentUser.setFullName(etFullName.getText().toString());
            currentUser.setUsername(etUsername.getText().toString());
            currentUser.setEmail(etEmail.getText().toString());
            currentUser.setPhoneNumber(etPhone.getText().toString());
            currentUser.setBio(eTBio.getText().toString());

            int defaultAddressIndex = -1;
            for (int i = 0; i < currentUser.getAddresses().size(); i++) {
                if (currentUser.getAddresses().get(i).isDefault()) {
                    defaultAddressIndex = i;
                    break;
                }
            }
            currentUser.setDefaultAddressIndex(defaultAddressIndex);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_user", currentUser);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(EditProfileActivity.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    etNewAddress.setText(address.getAddressLine(0));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Could not fetch location.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
