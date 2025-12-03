package com.example.unitrade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
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

        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                // Only allow selecting a NEW currency, not deselecting the current one.
                if (selectedPosition != holder.getAdapterPosition()) {
                    listener.onCurrencyClick(holder.getAdapterPosition());
                }
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
        selectedPosition = position;
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
        RadioButton radioButton;

        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            currencyName = itemView.findViewById(R.id.tvCurrencyName);
            radioButton = itemView.findViewById(R.id.rbCurrency);
        }
    }
}
