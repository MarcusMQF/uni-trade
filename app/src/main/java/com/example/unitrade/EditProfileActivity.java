package com.example.unitrade;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;

    private TextInputEditText etFullName, etUsername, etEmail, etPhone, etNewAddress, eTBio;
    private Button btnSaveProfile, btnAddAddress;
    private ImageButton btnFetchLocation;
    private RecyclerView rvAddresses;
    private ImageView imgProfile;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnChangePhoto;

    private User currentUser;
    private Uri newProfileUri = null;

    private AddressAdapter addressAdapter;

    // ---------------------------------------------------
    // ActivityResult Launchers for Camera & Gallery
    // ---------------------------------------------------
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Uri picked = result.getData().getData();

                    Uri safeUri =
                            com.example.unitrade.ImageStorageUtil.copyFromUri(this, picked);

                    if (safeUri != null) {
                        newProfileUri = safeUri;

                        Glide.with(this)
                                .load(safeUri)
                                .circleCrop()
                                .into(imgProfile);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null && result.getData().getExtras() != null) {
                    android.graphics.Bitmap bmp = (android.graphics.Bitmap)
                            result.getData().getExtras().get("data");

                     bmp = (android.graphics.Bitmap) result.getData().getExtras().get("data");

                    Uri safeUri =
                            com.example.unitrade.ImageStorageUtil.saveBitmap(this, bmp);

                    if (safeUri != null) {
                        newProfileUri = safeUri;

                        Glide.with(this)
                                .load(safeUri)
                                .circleCrop()
                                .into(imgProfile);
                    }


                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.appBarEditProfile);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ---------------------------------------------------
        // Get ONLY user_id
        // ---------------------------------------------------
        String userId = getIntent().getStringExtra("user_id");
        currentUser = SampleData.getUserById(this, userId);

        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        loadUserInfo();
        setupChangePhotoButton();
        setupButtonActions();
    }

    private void bindViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etNewAddress = findViewById(R.id.etNewAddress);
        eTBio = findViewById(R.id.eTBio);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnFetchLocation = findViewById(R.id.btnFetchLocation);

        rvAddresses = findViewById(R.id.rvAddresses);
        imgProfile = findViewById(R.id.imgProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
    }

    private void loadUserInfo() {
        etFullName.setText(currentUser.getFullName());
        etUsername.setText(currentUser.getUsername());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhoneNumber());
        eTBio.setText(currentUser.getBio());

        String img = currentUser.getProfileImageUrl();

        if (img != null && img.startsWith("content://")) {
            imgProfile.setImageResource(R.drawable.ic_profile_small);
        } else {
            Glide.with(this)
                    .load(img)
                    .signature(new ObjectKey(currentUser.getProfileImageVersion()))
                    .circleCrop()
                    .into(imgProfile);
        }

        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(this, currentUser.getAddresses());
        rvAddresses.setAdapter(addressAdapter);
    }

    private void setupChangePhotoButton() {
        btnChangePhoto.setOnClickListener(v -> {
            String[] options = {"Take Photo", "Choose From Gallery", "Cancel"};

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Change Profile Photo")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraLauncher.launch(cam);
                        } else if (which == 1) {
                            Intent pick = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            galleryLauncher.launch(pick);
                        }
                    })
                    .show();
        });
    }

    private void setupButtonActions() {

        btnAddAddress.setOnClickListener(v -> {
            String addr = etNewAddress.getText().toString();
            if (!addr.isEmpty()) {
                addressAdapter.addAddress(new com.example.unitrade.Address(addr, false));
                etNewAddress.setText("");
            }
        });

        btnFetchLocation.setOnClickListener(v -> fetchLocation());

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {

        // Update text fields
        currentUser.setFullName(etFullName.getText().toString());
        currentUser.setUsername(etUsername.getText().toString());
        currentUser.setEmail(etEmail.getText().toString());
        currentUser.setPhoneNumber(etPhone.getText().toString());
        currentUser.setBio(eTBio.getText().toString());
        currentUser.setLastEdited(System.currentTimeMillis());

        // Update profile image if changed
        if (newProfileUri != null) {
            currentUser.setProfileImageUrl(newProfileUri.toString());
            currentUser.setProfileImageVersion(System.currentTimeMillis());
        }

        // Update in SampleData (mutates existing object)


        SampleData.updateUser(this, currentUser);


        User updated = SampleData.getUserById(this, currentUser.getId());


        UserSession.set(updated);

        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();

        finish();
    }



    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        List<Address> list = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1);

                        if (!list.isEmpty()) {
                            etNewAddress.setText(list.get(0).getAddressLine(0));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
