package com.example.unitrade;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EditProfileActivity extends BaseActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private ImageView imgProfile, btnChangePhoto;
    private EditText etFullName, etUsername, etEmail, etPhone, etBio;
    private Button btnSaveProfile;
    private TextView tvLastEdited;

    private User user;
    private Uri photoURI;

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Glide.with(this).load(photoURI).circleCrop().into(imgProfile);
                    user.setProfileImageUrl(photoURI.toString());
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).circleCrop().into(imgProfile);
                    user.setProfileImageUrl(selectedImageUri.toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.appBarEditProfile);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }

        tintToolbarOverflow(toolbar);

        // Initialize views
        imgProfile = findViewById(R.id.imgProfile);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etBio = findViewById(R.id.eTBio);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        tvLastEdited = findViewById(R.id.tvLastEdited);

        user = getIntent().getParcelableExtra("user_to_edit");

        if (user == null) {
            Toast.makeText(this, "Error: No user received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etFullName.setText(user.getFullName());
        etUsername.setText(user.getUsername());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhoneNumber());
        etBio.setText(user.getBio());

        if (user.getLastEdited() > 0) {
            tvLastEdited.setText("Last edited: " + user.getLastEditedString());
        } else {
            tvLastEdited.setText("Last edited: Never");
        }

        Glide.with(this)
                .load(user.getProfileImageUrl())
                .circleCrop()
                .placeholder(R.drawable.profile_pic_1)
                .into(imgProfile);

        btnChangePhoto.setOnClickListener(v -> showChangePhotoDialog());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChangePhotoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Change Profile Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery", "Cancel"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    checkCameraPermissionAndTakePhoto();
                                    break;
                                case 1:
                                    pickImageFromGallery();
                                    break;
                            }
                        })
                .show();
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(this, "com.example.unitrade.provider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureLauncher.launch(takePictureIntent);
        }
    }

    private void pickImageFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickPhoto);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setBio(bio);
        user.setLastEdited(System.currentTimeMillis());



        Intent resultIntent = new Intent();
        resultIntent.putExtra("updated_user", user);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}
