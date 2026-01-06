package com.example.unitrade;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class MediaViewerActivity extends AppCompatActivity {

    public static final String EXTRA_MEDIA_URL = "media_url";
    public static final String EXTRA_MEDIA_TYPE = "media_type"; // "image" or "video"

    private WebView webView;
    private VideoView videoView;
    private RelativeLayout videoLayout;
    private ProgressBar loadingSpinner;
    private ImageButton btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_viewer);

        String mediaUrl = getIntent().getStringExtra(EXTRA_MEDIA_URL);
        String mediaType = getIntent().getStringExtra(EXTRA_MEDIA_TYPE);

        if (mediaUrl == null) {
            finish();
            return;
        }

        webView = findViewById(R.id.webView);
        videoView = findViewById(R.id.videoView);
        videoLayout = findViewById(R.id.videoLayout);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> finish());

        if ("video".equals(mediaType)) {
            showVideo(mediaUrl);
        } else {
            showImage(mediaUrl);
        }
    }

    private void showImage(String url) {
        webView.setVisibility(View.VISIBLE);
        videoLayout.setVisibility(View.GONE);

        // Configure WebView for Image Zoom logic
        webView.setBackgroundColor(0); // Transparent/Black
        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptEnabled(true); // Sometimes needed for some CDNs

        // Construct a simple HTML to center the image and allow zooming
        // Or simply load the URL directly. Direct URL is easiest but might not be
        // centered perfectly.
        // Let's try direct URL first.
        webView.loadUrl(url);
    }

    private void showVideo(String url) {
        webView.setVisibility(View.GONE);
        videoLayout.setVisibility(View.VISIBLE);
        loadingSpinner.setVisibility(View.VISIBLE);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(url));

        videoView.setOnPreparedListener(mp -> {
            loadingSpinner.setVisibility(View.GONE);
            videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            loadingSpinner.setVisibility(View.GONE);
            return false;
        });
    }
}
