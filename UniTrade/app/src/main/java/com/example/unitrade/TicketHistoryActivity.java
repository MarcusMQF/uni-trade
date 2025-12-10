package com.example.unitrade;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class TicketHistoryActivity extends AppCompatActivity {

    private RecyclerView rvTicketHistory;
    private TextView tvNoTickets;
    private TicketHistoryAdapter adapter;

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
