package com.example.unitrade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ReportProblemActivity extends AppCompatActivity {

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
            adapter = new TicketHistoryAdapter(ticketList);
            rvTicketHistory.setAdapter(adapter);
        }
    }
}
