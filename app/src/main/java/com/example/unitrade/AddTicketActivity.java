package com.example.unitrade;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddTicketActivity extends BaseActivity {

    private EditText etSubject, etDescription;
    private TextView tvAttachedFileName;
    private ImageView ivAttachmentPreview;
    private MaterialCardView attachmentPreviewCard;
    private ImageButton btnClearAttachment;
    private LinearLayout attachmentDropZone;
    private Uri attachedFileUri;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    attachedFileUri = saveFileLocally(uri);
                    if (attachedFileUri != null) {
                        String fileName = getFileName(attachedFileUri);
                        tvAttachedFileName.setText(fileName);
                        attachmentPreviewCard.setVisibility(View.VISIBLE);
                        attachmentDropZone.setVisibility(View.GONE);

                        if (isImage(fileName) || isVideo(fileName)) {
                            Glide.with(this).load(attachedFileUri).into(ivAttachmentPreview);
                        } else {
                            ivAttachmentPreview.setImageResource(R.drawable.ic_menu_attachment);
                        }
                    } else {
                        Toast.makeText(this, "Failed to attach file.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ticket);

        MaterialToolbar toolbar = findViewById(R.id.appBarAddTicket);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        tintToolbarOverflow(toolbar);

        etSubject = findViewById(R.id.etProblemSubject);
        etDescription = findViewById(R.id.etProblemDescription);
        tvAttachedFileName = findViewById(R.id.tvAttachedFileName);
        ivAttachmentPreview = findViewById(R.id.ivAttachmentPreview);
        attachmentPreviewCard = findViewById(R.id.attachmentPreviewCard);
        btnClearAttachment = findViewById(R.id.btnClearAttachment);
        attachmentDropZone = findViewById(R.id.attachmentDropZone);

        attachmentDropZone.setOnClickListener(v -> openFilePicker());

        View attachmentPreviewLayout = findViewById(R.id.attachmentPreviewLayout);
        attachmentPreviewLayout.setOnClickListener(v -> openAttachment());

        btnClearAttachment.setOnClickListener(v -> clearAttachment());

        Button btnSubmit = findViewById(R.id.btnSubmitTicket);
        btnSubmit.setOnClickListener(v -> submitTicket());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"});
        filePickerLauncher.launch(intent);
    }

    private void submitTicket() {
        String subject = etSubject.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (subject.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String attachmentUriString = (attachedFileUri != null) ? attachedFileUri.toString() : null;

        Ticket ticket = new Ticket(subject, description, attachmentUriString, System.currentTimeMillis());
        TicketHistoryManager.addTicket(this, ticket);

        Toast.makeText(this, "Ticket submitted successfully", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void clearAttachment() {
        attachedFileUri = null;
        attachmentPreviewCard.setVisibility(View.GONE);
        attachmentDropZone.setVisibility(View.VISIBLE);
    }

    private void openAttachment() {
        if (attachedFileUri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(attachedFileUri.getPath()));
            intent.setData(fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private boolean isImage(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".gif");
    }

    private boolean isVideo(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".mp4") || lowerCaseName.endsWith(".3gp") || lowerCaseName.endsWith(".mkv");
    }

    private Uri saveFileLocally(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String fileName = getFileName(uri);
            if (fileName == null) fileName = "attachment_" + System.currentTimeMillis();
            File file = new File(getFilesDir(), fileName);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
            inputStream.close();
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
