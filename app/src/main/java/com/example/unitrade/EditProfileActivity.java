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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;

    private TextInputEditText etFullName, etUsername, etEmail, etPhone, etNewAddress, etBio;
    private Button btnSaveProfile, btnAddAddress;
    private ImageButton btnFetchLocation;
    private RecyclerView rvAddresses;
    private ImageView imgProfile;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnChangePhoto;

    private User currentUser;
    private Uri newProfileUri = null;
    private AddressAdapter addressAdapter;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Uri picked = result.getData().getData();
                    Uri safeUri = ImageStorageUtil.copyFromUri(this, picked);
                    if (safeUri != null) {
                        newProfileUri = safeUri;
                        Glide.with(this).load(safeUri).circleCrop().into(imgProfile);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null && result.getData().getExtras() != null) {
                    android.graphics.Bitmap bmp = (android.graphics.Bitmap) result.getData().getExtras().get("data");
                    Uri safeUri = ImageStorageUtil.saveBitmap(this, bmp);
                    if (safeUri != null) {
                        newProfileUri = safeUri;
                        Glide.with(this).load(safeUri).circleCrop().into(imgProfile);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        MaterialToolbar toolbar = findViewById(R.id.appBarEditProfile);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();

        String userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            UserRepository.getUserByUid(userId, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    currentUser = user;
                    loadUserInfo();
                    setupChangePhotoButton();
                    setupButtonActions();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EditProfileActivity.this, "Failed to load user", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "No user ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etNewAddress = findViewById(R.id.etNewAddress);
        etBio = findViewById(R.id.eTBio);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnFetchLocation = findViewById(R.id.btnFetchLocation);

        rvAddresses = findViewById(R.id.rvAddresses);
        imgProfile = findViewById(R.id.imgProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
    }

    private void loadUserInfo() {
        if (currentUser == null)
            return;

        etFullName.setText(currentUser.getFullName());
        etUsername.setText(currentUser.getUsername());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhoneNumber());
        etBio.setText(currentUser.getBio());

        if (currentUser.getProfileImageUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getProfileImageUrl())
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
            String[] options = { "Take Photo", "Choose From Gallery", "Cancel" };
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Change Profile Photo")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                        } else if (which == 1) {
                            galleryLauncher.launch(
                                    new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
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
        if (currentUser == null)
            return;

        currentUser.setFullName(etFullName.getText().toString().trim());
        currentUser.setUsername(etUsername.getText().toString().trim());
        currentUser.setEmail(etEmail.getText().toString().trim());
        currentUser.setPhoneNumber(etPhone.getText().toString().trim());
        currentUser.setBio(etBio.getText().toString().trim());
        currentUser.setLastEdited(System.currentTimeMillis());

        if (newProfileUri != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference ref = FirebaseStorage.getInstance()
                    .getReference("profile_images/" + uid + ".jpg");

            ref.putFile(newProfileUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                currentUser.setProfileImageUrl(uri.toString());
                                currentUser.setProfileImageVersion(System.currentTimeMillis());
                                saveUserToFirestore();
                            })
                            .addOnFailureListener(
                                    e -> Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(
                            e -> Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        } else {
            saveUserToFirestore();
        }
    }

    private void saveUserToFirestore() {
        if (currentUser.getId() == null) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                currentUser.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            } else {
                Toast.makeText(this, "Error: User ID is missing", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        UserRepository.updateUser(currentUser, new UserRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                UserSession.set(currentUser);
                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("updated_user", currentUser);
                setResult(RESULT_OK, resultIntent);

                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
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
                        List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),
                                1);
                        if (!list.isEmpty()) {
                            etNewAddress.setText(list.get(0).getAddressLine(0));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
