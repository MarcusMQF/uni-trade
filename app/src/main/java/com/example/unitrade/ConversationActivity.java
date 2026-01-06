package com.example.unitrade;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.InputStream;

import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Callback;

import de.hdodenhof.circleimageview.CircleImageView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

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

    private FirebaseFirestore db;

    private com.google.firebase.storage.FirebaseStorage storage;
    private com.google.firebase.storage.StorageReference storageReference;

    private String currentUserId;
    private String receiverId;
    private String chatId;
    private User receiverUser;
    public static String activeChatId = null;

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    selectedMediaUris.add(photoURI);
                    showPreview(photoURI);
                }
            });

    private final ActivityResultLauncher<Intent> takeVideoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    selectedMediaUris.add(photoURI);
                    showPreview(photoURI);
                }
            });

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
            });

    private final ActivityResultLauncher<Intent> speechLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        messageEditText.append(spokenText + " ");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Chat chat = getIntent().getParcelableExtra("chat");
        if (chat != null) {
            receiverId = chat.getUserId();
        } else {
            // Fallback if userId is passed directly
            receiverId = getIntent().getStringExtra("receiverId");
        }

        if (currentUserId == null || receiverId == null) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Create Chat ID (Consistent for 1-to-1)
        List<String> ids = Arrays.asList(currentUserId, receiverId);
        Collections.sort(ids);
        chatId = ids.get(0) + "_" + ids.get(1);

        setupRecyclerView();
        setupInputLayout();
        setupQuickReplies();
        updateSendButtonState();

        // Load user from Firebase
        UserRepository.getUserByUid(receiverId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                receiverUser = user;
                userPhoneNumber = user.getPhoneNumber();
                setupToolbar(chat, user);
            }

            @Override
            public void onFailure(Exception e) {
                receiverUser = null;
                userPhoneNumber = null;
                setupToolbar(chat, null);
            }
        });

        listenForMessages();

        if (savedInstanceState == null) {
            checkForProductInterest();
        }
    }

    private void checkForProductInterest() {
        Intent intent = getIntent();
        if (intent.hasExtra("product_name")) { // Check for at minimum the product name
            String imgUrl = intent.getStringExtra("product_image");
            String name = intent.getStringExtra("product_name");
            String price = intent.getStringExtra("product_price");
            String productId = intent.getStringExtra("product_id");

            sendProductCardMessage(name, price, imgUrl, productId);

            // Clear extras to prevent duplicate sending
            intent.removeExtra("product_image");
            intent.removeExtra("product_name");
            intent.removeExtra("product_price");
            intent.removeExtra("product_id");
        }
    }

    private void sendProductCardMessage(String name, String price, String imgUrl, String productId) {
        Message msg = new Message();
        msg.setText("I'm interested in this product."); // Default text
        msg.setSenderId(currentUserId);
        msg.setTimestamp(System.currentTimeMillis());

        // set product card fields
        msg.setProductName(name);
        msg.setProductPrice(price);
        msg.setProductImageUrl(imgUrl);
        msg.setProductId(productId);

        db.collection("chats").document(chatId).collection("messages").add(msg);

        // Update last message preview
        Map<String, Object> chatUpdates = new HashMap<>();
        chatUpdates.put("lastMessage", "Sent product inquiry: " + name);
        chatUpdates.put("lastMessageTime", System.currentTimeMillis());
        chatUpdates.put("participants", new ArrayList<>(Arrays.asList(currentUserId, receiverId)));

        db.collection("chats").document(chatId).set(chatUpdates, SetOptions.merge());

        fetchReceiverTokenAndNotify("Sent product inquiry: " + name);
    }

    private void sendTextMessage(String text) {
        if (text == null || text.trim().isEmpty())
            return;

        Message msg = new Message(text, currentUserId, System.currentTimeMillis());

        db.collection("chats").document(chatId).collection("messages").add(msg);

        Map<String, Object> chatUpdates = new HashMap<>();
        chatUpdates.put("lastMessage", text);
        chatUpdates.put("lastMessageTime", System.currentTimeMillis());
        chatUpdates.put("participants", new ArrayList<>(Arrays.asList(currentUserId, receiverId)));

        db.collection("chats").document(chatId).set(chatUpdates, SetOptions.merge());

        fetchReceiverTokenAndNotify(text);
    }

    private void listenForMessages() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message message = dc.getDocument().toObject(Message.class);
                                messageList.add(message);
                                adapter.notifyItemInserted(messageList.size() - 1);
                                recyclerView.scrollToPosition(messageList.size() - 1);

                            }
                        }
                    }
                });
    }

    private void setupToolbar(Chat chat, User user) {
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        ImageView callButton = findViewById(R.id.call_button);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        CircleImageView profileImage = findViewById(R.id.profile_image);

        if (user != null) {
            String name = user.getUsername();
            if (name == null || name.isEmpty()) {
                name = user.getFullName();
            }
            if (name == null || name.isEmpty()) {
                name = "Chat";
            }
            toolbarTitle.setText(name);
        } else {
            toolbarTitle.setText("Chat");
        }

        callButton.setOnClickListener(v -> {
            if (userPhoneNumber != null && !userPhoneNumber.isEmpty()) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall(userPhoneNumber);
                } else {
                    ActivityCompat.requestPermissions(
                            this,
                            new String[] { Manifest.permission.CALL_PHONE },
                            REQUEST_CALL_PERMISSION);
                }
            } else {
                showToast("Phone number not available.");
            }
        });

        if (user != null && user.getProfileImageUrl() != null) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .signature(new ObjectKey(String.valueOf(user.getProfileImageVersion())))
                    .placeholder(R.drawable.profile_pic_2)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.profile_pic_2);
        }

        profileImage.setOnClickListener(v -> {
            if (receiverUser != null) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("user_to_view", receiverUser);
                startActivity(intent);
            } else {
                showToast("User profile loading...");
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, currentUserId);
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        sendButton.setOnClickListener(v -> sendMessage());
        galleryButton.setOnClickListener(v -> showMediaSourceDialog());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inputLayout), (v, windowInsets) -> {
            Insets insets = windowInsets
                    .getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom + v.getPaddingTop());
            return windowInsets;
        });
    }

    private void setupQuickReplies() {
        quickReplyLayout = findViewById(R.id.quickReplyLayout);
        addQuickReplyButton("Is this still available?");
        addQuickReplyButton("What's the condition?");
        addQuickReplyButton("Can you share more photos?");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
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

        if (!hasText && !hasMedia)
            return;

        if (hasMedia) {
            uploadMediaAndSendMessage(selectedMediaUris.get(0));
        }

        if (hasText) {
            Message msg = new Message(messageText, currentUserId, System.currentTimeMillis());

            // Add to Firestore
            db.collection("chats").document(chatId).collection("messages").add(msg);

            // Update Parent Chat Document
            Map<String, Object> chatUpdates = new HashMap<>();
            chatUpdates.put("lastMessage", messageText);
            chatUpdates.put("lastMessageTime", System.currentTimeMillis());
            chatUpdates.put("participants", new ArrayList<>(Arrays.asList(currentUserId, receiverId)));

            // Check if it's referenced by a product (optional, if we passed product ID)
            // for now, just simplified.

            db.collection("chats").document(chatId).set(chatUpdates, SetOptions.merge());
        }
        fetchReceiverTokenAndNotify(messageText != null ? messageText : "Sent an image");

        messageEditText.setText("");
        clearPreview();
    }

    private void showPreview(Uri mediaUri) {
        previewLayout.setVisibility(View.VISIBLE);
        Glide.with(this).load(mediaUri).into(previewImageView);

        boolean isVideo = isVideo(mediaUri);
        playIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        updateSendButtonState();
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
        params.setMargins(20, 8, 20, 8);

        button.setLayoutParams(params);
        button.setOnClickListener(v -> messageEditText.setText(text));

        quickReplyLayout.addView(button);
    }

    private void showMediaSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Send Media")
                .setItems(new CharSequence[] { "Take Photo", "Take Video", "Choose from Gallery" }, (dialog, which) -> {
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
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA },
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void checkCameraPermissionAndTakeVideo() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakeVideoIntent();
        } else {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA },
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void pickMediaFromGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("*/*");
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "image/*", "video/*" });
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

    private void uploadMediaAndSendMessage(Uri fileUri) {
        // 1. Determine file type
        String type = isVideo(fileUri) ? "video" : "image";
        String extension = isVideo(fileUri) ? ".mp4" : ".jpg";

        // 2. Create a unique path in Firebase Storage
        String fileName = "chat_media/" + chatId + "/" + System.currentTimeMillis() + extension;
        com.google.firebase.storage.StorageReference fileRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                .getReference().child(fileName);

        // 3. Upload the file
        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // 4. Once uploaded, create a media message
                saveMediaMessageToFirestore(uri.toString(), type);
            });
        }).addOnFailureListener(e -> showToast("Failed to upload media: " + e.getMessage()));
    }

    private void saveMediaMessageToFirestore(String mediaUrl, String type) {
        // Create a message where the "text" is the URL or a placeholder
        String placeholderText = "[" + type + "]";
        Message mediaMsg = new Message(placeholderText, currentUserId, System.currentTimeMillis());

        // If you add a setMediaUrl to your Message.java later, you can call it here:
        // mediaMsg.setMediaUrl(mediaUrl);
        mediaMsg.setMediaUrl(mediaUrl);
        mediaMsg.setMediaType(type);

        // Add to the same Firestore collection
        db.collection("chats").document(chatId).collection("messages").add(mediaMsg);

        // Update the inbox preview
        Map<String, Object> chatUpdates = new HashMap<>();
        chatUpdates.put("lastMessage", "Sent an " + type);
        chatUpdates.put("lastMessageTime", System.currentTimeMillis());
        chatUpdates.put("participants", new ArrayList<>(Arrays.asList(currentUserId, receiverId)));
        db.collection("chats").document(chatId).set(chatUpdates, com.google.firebase.firestore.SetOptions.merge());
    }

    private void fetchReceiverTokenAndNotify(String messageText) {
        db.collection("users").document(receiverId).get().addOnSuccessListener(documentSnapshot -> {
            String token = documentSnapshot.getString("fcmToken");
            if (token != null && !token.isEmpty()) {
                String preview = (messageText == null || messageText.isEmpty()) ? "Sent an image" : messageText;
                String senderName = (receiverUser != null) ? receiverUser.getUsername() : "UniTrade User";

                // This calls your OkHttp V1 method
                sendPushNotificationV1(token, senderName, preview);
            }
        });
    }

    private void sendPushNotificationV1(String token, String title, String body) {
        new Thread(() -> {
            try {
                // 1. Get OAuth 2.0 Access Token
                InputStream stream = getAssets().open("service-account.json");
                GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
                credentials.refreshIfExpired();
                String accessToken = credentials.getAccessToken().getTokenValue();

                // 2. Prepare URL and JSON
                String projectId = FirebaseApp.getInstance().getOptions().getProjectId();
                String url = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";

                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);

                JSONObject data = new JSONObject();
                data.put("chatId", chatId);

                JSONObject messageObj = new JSONObject();
                messageObj.put("token", token);
                messageObj.put("notification", notification);
                messageObj.put("data", data);

                JSONObject rootPayload = new JSONObject();
                rootPayload.put("message", messageObj);

                // Inside your JSON building logic in ConversationActivity
                JSONObject androidConfig = new JSONObject();
                JSONObject androidNotification = new JSONObject();
                androidNotification.put("priority", "high"); // Forces the phone to wake up
                androidConfig.put("notification", androidNotification);

                messageObj.put("android", androidConfig);

                // 3. Execute Request
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        rootPayload.toString());

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();

                // This "try-with-resources" block fixes your .close() error and prevents memory
                // leaks
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // Success
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 2. When the user enters the chat, set the activeChatId
        activeChatId = chatId;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 3. When the user leaves, clear it
        activeChatId = null;
    }

}
