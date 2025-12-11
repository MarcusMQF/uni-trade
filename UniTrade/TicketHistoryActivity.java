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

// 1. Implement the OnAttachmentClickListener interface
public class TicketHistoryActivity extends AppCompatActivity implements TicketHistoryAdapter.OnAttachmentClickListener {

    private RecyclerView rvTicketHistory;
    private TextView tvNoTickets;
    private TicketHistoryAdapter adapter;
    private List<Ticket> ticketList; // Make this a field to access it in the click listener

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
        // Use the class field
        ticketList = TicketHistoryManager.getTickets(this);

        if (ticketList.isEmpty()) {
            tvNoTickets.setVisibility(View.VISIBLE);
            rvTicketHistory.setVisibility(View.GONE);
        } else {
            tvNoTickets.setVisibility(View.GONE);
            rvTicketHistory.setVisibility(View.VISIBLE);

            // 2. Pass 'this' as the third argument for the listener
            adapter = new TicketHistoryAdapter(this, ticketList, this);
            rvTicketHistory.setAdapter(adapter);
        }
    }

    // 3. Add the required onAttachmentClick method from the interface
    @Override
    public void onAttachmentClick(Uri attachmentUri) {
        if (attachmentUri != null) {
            // Create an intent to view the attachment
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(attachmentUri.getPath()));
            intent.setData(fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No application can handle this file.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No attachment available.", Toast.LENGTH_SHORT).show();
        }
    }
}
