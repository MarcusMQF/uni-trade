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
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private final List<Transaction> transactionList;

    // =====================
    // CONSTRUCTOR
    // =====================
    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    // =====================
    // PUBLIC UPDATE METHOD (IMPROVEMENT 1)
    // =====================
    public void setData(@NonNull List<Transaction> newList) {
        transactionList.clear();
        transactionList.addAll(newList);
        notifyDataSetChanged();
    }

    // =====================
    // ADAPTER OVERRIDES
    // =====================
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.transactionName.setText(transaction.getName());
        holder.transactionDate.setText(transaction.getDate());

        String formattedPrice = AppSettings.formatPrice(context, transaction.getAmount());

        // ---------------------
        // Amount + Color
        // ---------------------
        String sign = transaction.isBuy() ? "-" : "+";
        int color = transaction.isBuy() ? R.color.red : R.color.green;

        holder.transactionAmount.setText(sign + formattedPrice);
        holder.transactionAmount.setTextColor(
                ContextCompat.getColor(context, color)
        );

        // ---------------------
        // Icon / Image
        // ---------------------
        int fallbackIcon = transaction.isBuy()
                ? android.R.drawable.arrow_down_float
                : android.R.drawable.arrow_up_float;

        if (transaction.getImageUrl() != null && !transaction.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(transaction.getImageUrl())
                    .signature(new ObjectKey(transaction.getImageVersion()))
                    .placeholder(fallbackIcon)
                    .error(fallbackIcon)
                    .centerCrop()
                    .into(holder.transactionIcon);
        } else {
            holder.transactionIcon.setImageResource(fallbackIcon);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // =====================
    // VIEW HOLDER
    // =====================
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {

        ImageView transactionIcon;
        TextView transactionName;
        TextView transactionDate;
        TextView transactionAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionIcon = itemView.findViewById(R.id.iv_transaction_icon);
            transactionName = itemView.findViewById(R.id.tv_transaction_name);
            transactionDate = itemView.findViewById(R.id.tv_transaction_date);
            transactionAmount = itemView.findViewById(R.id.tv_transaction_amount);
        }
    }
}
