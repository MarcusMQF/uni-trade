package com.example.unitrade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoginHistoryAdapter extends RecyclerView.Adapter<LoginHistoryAdapter.ViewHolder> {

    private final List<LoginHistoryItem> loginHistory;

    public LoginHistoryAdapter(List<LoginHistoryItem> loginHistory) {
        this.loginHistory = loginHistory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_login_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoginHistoryItem item = loginHistory.get(position);
        long timestamp = item.getTimestamp();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        holder.textViewDate.setText(dateFormat.format(new Date(timestamp)));
        holder.textViewDay.setText(dayFormat.format(new Date(timestamp)));
        holder.textViewTime.setText(timeFormat.format(new Date(timestamp)));
    }

    @Override
    public int getItemCount() {
        return loginHistory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;
        TextView textViewDay;
        TextView textViewTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewDay = itemView.findViewById(R.id.textViewDay);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }
    }
}
