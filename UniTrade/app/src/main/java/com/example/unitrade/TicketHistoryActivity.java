package com.example.unitrade;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.List;

public class TicketHistoryActivity extends AppCompatActivity implements TicketHistoryAdapter.OnAttachmentClickListener {

    private RecyclerView rvTicketHistory;
    private TextView tvNoTickets;
    private TicketHistoryAdapter adapter;
    private List<Ticket> ticketList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_history);

        MaterialToolbar toolbar = findViewById(R.id.appBarTicketHistory);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvTicketHistory = findViewById(R.id.rvTicketHistory);
        tvNoTickets = findViewById(R.id.tvNoTickets);

        rvTicketHistory.setLayoutManager(new LinearLayoutManager(this));

        loadTicketHistory();
    }

    private void loadTicketHistory() {
        ticketList = TicketHistoryManager.getTickets(this);

        if (ticketList.isEmpty()) {
            tvNoTickets.setVisibility(View.VISIBLE);
            rvTicketHistory.setVisibility(View.GONE);
        } else {
            tvNoTickets.setVisibility(View.GONE);
            rvTicketHistory.setVisibility(View.VISIBLE);

            adapter = new TicketHistoryAdapter(this, ticketList, this);
            rvTicketHistory.setAdapter(adapter);
        }
    }

    @Override
    public void onAttachmentClick(String attachmentPath) {
        if (attachmentPath != null && !attachmentPath.isEmpty()) {
            try {
                Uri uri = Uri.parse(attachmentPath);
                File file;
                // Handle file:// URI scheme
                if ("file".equals(uri.getScheme())) {
                    file = new File(uri.getPath());
                } else {
                    // Try treating as a direct path
                    file = new File(attachmentPath);
                }

                if (file.exists()) {
                    Uri fileUri = FileProvider.getUriForFile(
                            this,
                            getApplicationContext().getPackageName() + ".provider",
                            file
                    );

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(fileUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(intent);
                } else {
                    Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Cannot open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No attachment available.", Toast.LENGTH_SHORT).show();
        }
    }
}
