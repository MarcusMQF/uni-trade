package com.example.unitrade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.transactionName.setText(transaction.getName());
        holder.transactionDate.setText(transaction.getDate());

        String formattedPrice = AppSettings.formatPrice(context, transaction.getAmount());

        if (transaction.isBuy()) {
            holder.transactionAmount.setText("-" + formattedPrice);
            holder.transactionAmount.setTextColor(ContextCompat.getColor(context, R.color.red));
            // Default icon if image fails or not provided
            holder.transactionIcon.setImageResource(android.R.drawable.arrow_down_float);
        } else {
            holder.transactionAmount.setText("+" + formattedPrice);
            holder.transactionAmount.setTextColor(ContextCompat.getColor(context, R.color.green));
            // Default icon if image fails or not provided
            holder.transactionIcon.setImageResource(android.R.drawable.arrow_up_float);
        }

        // Load image if available
        if (transaction.getImageUrl() != null && !transaction.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(transaction.getImageUrl())
                    .placeholder(transaction.isBuy() ? android.R.drawable.arrow_down_float : android.R.drawable.arrow_up_float)
                    .error(transaction.isBuy() ? android.R.drawable.arrow_down_float : android.R.drawable.arrow_up_float)
                    .centerCrop()
                    .into(holder.transactionIcon);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView transactionIcon;
        TextView transactionName, transactionDate, transactionAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionIcon = itemView.findViewById(R.id.iv_transaction_icon);
            transactionName = itemView.findViewById(R.id.tv_transaction_name);
            transactionDate = itemView.findViewById(R.id.tv_transaction_date);
            transactionAmount = itemView.findViewById(R.id.tv_transaction_amount);
        }
    }
}
