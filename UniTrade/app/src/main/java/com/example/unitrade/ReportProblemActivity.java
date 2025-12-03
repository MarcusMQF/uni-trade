package com.example.unitrade;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class ReportProblemActivity extends BaseActivity {

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
                    attachedFileUri = result.getData().getData();
                    String fileName = getFileName(attachedFileUri);
                    tvAttachedFileName.setText(fileName);
                    attachmentPreviewCard.setVisibility(View.VISIBLE);
                    attachmentDropZone.setVisibility(View.GONE);

                    if (isImage(fileName) || isVideo(fileName)) {
                        Glide.with(this).load(attachedFileUri).into(ivAttachmentPreview);
                    } else {
                        ivAttachmentPreview.setImageResource(R.drawable.ic_menu_attachment);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        MaterialToolbar toolbar = findViewById(R.id.appBarReportProblem);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_problem_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(this, TicketHistoryActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        Ticket ticket = new Ticket(subject, description, attachedFileUri, System.currentTimeMillis());
        TicketHistoryManager.addTicket(this, ticket);

        String message = "Ticket submitted successfully!";
        if (attachedFileUri != null) {
            message += " (with attachment)";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
            intent.setData(attachedFileUri);
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
}
