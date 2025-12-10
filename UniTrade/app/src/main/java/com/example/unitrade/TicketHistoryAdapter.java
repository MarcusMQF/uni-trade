package com.example.unitrade;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TicketHistoryAdapter extends RecyclerView.Adapter<TicketHistoryAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;

    public TicketHistoryAdapter(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.subject.setText("Subject: " + ticket.getSubject());
        holder.description.setText("Description: " + ticket.getDescription());
        holder.timestamp.setText("Submitted: " + ticket.getFormattedTimestamp());
        holder.status.setText("Status: " + ticket.getStatus());

        int statusColor = Color.DKGRAY; // Default color
        switch (ticket.getStatus()) {
            case Ticket.STATUS_NOT_SEEN:
                statusColor = Color.GRAY;
                break;
            case Ticket.STATUS_IN_PROGRESS:
                statusColor = Color.BLUE;
                break;
            case Ticket.STATUS_RESOLVED:
                statusColor = Color.GREEN;
                break;
        }
        holder.status.setTextColor(statusColor);


        if (ticket.getAttachmentUri() != null) {
            holder.attachment.setText("Attachment: " + getFileName(ticket.getAttachmentUri().toString()));
            holder.attachment.setVisibility(View.VISIBLE);
        } else {
            holder.attachment.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    private String getFileName(String path) {
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            return path.substring(cut + 1);
        }
        return path;
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView subject, description, timestamp, attachment, status;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.tvTicketSubject);
            description = itemView.findViewById(R.id.tvTicketDescription);
            timestamp = itemView.findViewById(R.id.tvTicketTimestamp);
            attachment = itemView.findViewById(R.id.tvTicketAttachment);
            status = itemView.findViewById(R.id.tvTicketStatus);
        }
    }
}
