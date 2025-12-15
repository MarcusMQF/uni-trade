package com.example.unitrade;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {

    private Context context;
    private List<Currency> currencyList;
    private int selectedPosition = -1;
    private OnCurrencyClickListener listener;

    public interface OnCurrencyClickListener {
        void onCurrencyClick(int position);
    }

    public CurrencyAdapter(Context context, List<Currency> currencyList, OnCurrencyClickListener listener) {
        this.context = context;
        this.currencyList = currencyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_currency, parent, false);
        return new CurrencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        Currency currency = currencyList.get(position);
        holder.currencyName.setText(currency.getName() + " (" + currency.getCode() + ")");

        if (position == selectedPosition) {
            holder.currencyItem.setBackgroundColor(Color.parseColor("#E0E0E0"));
            holder.currencyName.setTextColor(Color.BLACK);
        } else {
            holder.currencyItem.setBackgroundColor(Color.TRANSPARENT);
            holder.currencyName.setTextColor(Color.parseColor("#333333"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onCurrencyClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return currencyList.size();
    }

    public Currency getSelectedCurrency() {
        if (selectedPosition != -1) {
            return currencyList.get(selectedPosition);
        }
        return null;
    }

    public void setSelectedPosition(int position) {
        if (position == selectedPosition) {
            selectedPosition = -1;
        } else {
            selectedPosition = position;
        }
        notifyDataSetChanged();
    }

    public void setSelectedCurrency(String currencyCode) {
        for (int i = 0; i < currencyList.size(); i++) {
            if (currencyList.get(i).getCode().equals(currencyCode)) {
                selectedPosition = i;
                notifyDataSetChanged();
                return;
            }
        }
    }

    public static class CurrencyViewHolder extends RecyclerView.ViewHolder {
        TextView currencyName;
        LinearLayout currencyItem;

        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            currencyName = itemView.findViewById(R.id.tvCurrencyName);
            currencyItem = itemView.findViewById(R.id.currencyItem);
        }
    }
}
