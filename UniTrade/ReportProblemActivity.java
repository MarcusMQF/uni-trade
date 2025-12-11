package com.example.unitrade;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.List;

public class ReportProblemActivity extends AppCompatActivity implements TicketHistoryAdapter.OnAttachmentClickListener {

    private RecyclerView rvTicketHistory;
    private TextView tvNoTickets;
    private TicketHistoryAdapter adapter;

    private final ActivityResultLauncher<Intent> addTicketLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Refresh the ticket list
                    loadTicketHistory();
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

        rvTicketHistory = findViewById(R.id.rvTicketHistory);
        tvNoTickets = findViewById(R.id.tvNoTickets);
        FloatingActionButton fabAddTicket = findViewById(R.id.fabAddTicket);

        rvTicketHistory.setLayoutManager(new LinearLayoutManager(this));

        loadTicketHistory();

        fabAddTicket.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTicketActivity.class);
            addTicketLauncher.launch(intent);
        });
    }

    private void loadTicketHistory() {
        List<Ticket> ticketList = TicketHistoryManager.getTickets(this);

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
    public void onAttachmentClick(Uri attachmentUri) {
        if (attachmentUri != null) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, reload the ticket history to reflect changes
            loadTicketHistory();
        }
    }
}
