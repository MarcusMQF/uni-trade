package com.example.unitrade;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private EditText messageEditText;
    private LinearLayout quickReplyLayout;
    private ImageButton sendButton;
    private ImageButton galleryButton;
    private Uri photoURI;
    private RelativeLayout previewLayout;
    private ImageView previewImageView, playIcon;
    private ImageButton closePreviewButton;
    private List<Uri> selectedMediaUris = new ArrayList<>();
    private String userPhoneNumber;

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    selectedMediaUris.add(photoURI);
                    showPreview(photoURI);
                }
            }
    );

    private final ActivityResultLauncher<Intent> takeVideoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    selectedMediaUris.add(photoURI);
                    showPreview(photoURI);
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickMediaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            selectedMediaUris.add(clipData.getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        selectedMediaUris.add(data.getData());
                    }

                    if (!selectedMediaUris.isEmpty()) {
                        showPreview(selectedMediaUris.get(selectedMediaUris.size() - 1));
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> speechLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        messageEditText.append(spokenText + " ");
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Chat chat = getIntent().getParcelableExtra("chat");
        User user = SampleData.getUserById(this, chat.getUserId());
        userPhoneNumber = (user != null) ? user.getPhoneNumber() : null;

        setupToolbar(chat, user);
        setupRecyclerView();
        setupInputLayout();
        setupQuickReplies();

        updateSendButtonState();
    }

    private void setupToolbar(Chat chat, User user) {

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        ImageView callButton = findViewById(R.id.call_button);
        callButton.setOnClickListener(v -> {
            if (userPhoneNumber != null && !userPhoneNumber.isEmpty()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall(userPhoneNumber);
                } else {
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            REQUEST_CALL_PERMISSION
                    );
                }
            } else {
                showToast("Phone number not available.");
            }
        });

        // ✅ Use USER for title
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(user != null ? user.getUsername() : "Chat");

        // ✅ Use USER for avatar
        CircleImageView profileImage = findViewById(R.id.profile_image);

        if (user != null) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .signature(new ObjectKey(user.getProfileImageVersion()))
                    .placeholder(R.drawable.profile_pic_2)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.profile_pic_2);
        }

        // ✅ Profile click
        profileImage.setOnClickListener(v -> {
            if (user != null) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("user_to_view", user);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);
    }

    private void setupInputLayout() {
        messageEditText = findViewById(R.id.messageEditText);
        ImageView micButton = findViewById(R.id.mic_button);
        micButton.setOnClickListener(v -> startSpeechToText());

        sendButton = findViewById(R.id.sendButton);
        galleryButton = findViewById(R.id.galleryButton);

        previewLayout = findViewById(R.id.previewLayout);
        previewImageView = findViewById(R.id.previewImageView);
        playIcon = findViewById(R.id.playIcon);
        closePreviewButton = findViewById(R.id.closePreviewButton);

        closePreviewButton.setOnClickListener(v -> clearPreview());

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        sendButton.setOnClickListener(v -> sendMessage());

        galleryButton.setOnClickListener(v -> showMediaSourceDialog());
    }

    private void setupQuickReplies() {
        quickReplyLayout = findViewById(R.id.quickReplyLayout);
        addQuickReplyButton("Is this still available?");
        addQuickReplyButton("What's the condition?");
        addQuickReplyButton("Can you share more photos?");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Permission granted. Please tap the call button again.");
            } else {
                showToast("Call permission denied.");
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Camera permission granted.");
            } else {
                showToast("Camera permission denied.");
            }
        }
    }

    private void makePhoneCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        boolean hasText = !messageText.isEmpty();
        boolean hasMedia = !selectedMediaUris.isEmpty();

        if (!hasText && !hasMedia) {
            return; // Nothing to send
        }

        // Create one message with the text and attach the first media item
        if (hasMedia) {
            messageList.add(new Message(selectedMediaUris.get(0), hasText ? messageText : "", true));
            // Create separate messages for the remaining media items
            for (int i = 1; i < selectedMediaUris.size(); i++) {
                messageList.add(new Message(selectedMediaUris.get(i), "", true));
            }
        } else {
            // If there's only text, create a single message for it
            messageList.add(new Message(null, messageText, true));
        }

        int originalSize = messageList.size();
        int itemsAdded = hasMedia ? selectedMediaUris.size() : (hasText ? 1 : 0);
        if (hasMedia && hasText) itemsAdded = selectedMediaUris.size();

        adapter.notifyItemRangeInserted(originalSize - itemsAdded, itemsAdded);
        recyclerView.scrollToPosition(messageList.size() - 1);
        messageEditText.setText("");
        clearPreview();

        // Simulate Auto-Reply only once after the batch is sent
        new Handler().postDelayed(() -> {
            String replyText = "Thanks for your message! I'll get back to you shortly.";
            messageList.add(new Message(null, replyText, false));
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.smoothScrollToPosition(messageList.size() - 1);
        }, 1500);
    }

    private void showPreview(Uri mediaUri) {
        previewLayout.setVisibility(View.VISIBLE);
        Glide.with(this).load(mediaUri).into(previewImageView);

        boolean isVideo = isVideo(mediaUri);
        playIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        updateSendButtonState();

        if (selectedMediaUris.size() > 1) {
            showToast(selectedMediaUris.size() + " items selected");
        }
    }

    private boolean isVideo(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.startsWith("video");
    }

    private void clearPreview() {
        selectedMediaUris.clear();
        previewLayout.setVisibility(View.GONE);
        updateSendButtonState();
    }

    private void updateSendButtonState() {
        boolean hasText = !messageEditText.getText().toString().trim().isEmpty();
        boolean hasMedia = !selectedMediaUris.isEmpty();
        sendButton.setEnabled(hasText || hasMedia);
    }

    private void addQuickReplyButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.parseColor("#333333"));
        button.setElevation(6f);
        button.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_quick_reply));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 0, 20, 4);

        button.setLayoutParams(params);
        button.setOnClickListener(v -> messageEditText.setText(text));

        quickReplyLayout.addView(button);
    }

    private void showMediaSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Send Media")
                .setItems(new CharSequence[]{"Take Photo", "Take Video", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndTakePhoto();
                    } else if (which == 1) {
                        checkCameraPermissionAndTakeVideo();
                    } else {
                        pickMediaFromGallery();
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

    private void checkCameraPermissionAndTakeVideo() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakeVideoIntent();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void pickMediaFromGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("*/*");
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickMediaLauncher.launch(pickIntent);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File mediaFile = null;
        try {
            mediaFile = createMediaFile(".jpg");
        } catch (IOException ex) {
            showToast("Error creating media file.");
        }
        if (mediaFile != null) {
            photoURI = FileProvider.getUriForFile(this, "com.example.unitrade.provider", mediaFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureLauncher.launch(takePictureIntent);
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File mediaFile = null;
        try {
            mediaFile = createMediaFile(".mp4");
        } catch (IOException ex) {
            showToast("Error creating media file.");
        }
        if (mediaFile != null) {
            photoURI = FileProvider.getUriForFile(this, "com.example.unitrade.provider", mediaFile);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takeVideoLauncher.launch(takeVideoIntent);
        }
    }

    private File createMediaFile(String suffix) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MEDIA_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, suffix, storageDir);
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to type");
        try {
            speechLauncher.launch(intent);
        } catch (Exception e) {
            showToast("Speech recognition is not supported on this device.");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
