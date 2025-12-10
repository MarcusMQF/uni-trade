package com.example.unitrade;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TransactionStatisticsFragment extends Fragment {

    private TextView selectedDate;
    private PieChart pieChart;
    private TextView buyItemsText, buyAmountText, sellItemsText, sellAmountText, profitText;
    private RecyclerView rvHistory;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private Calendar currentSelectedDate = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        selectedDate = view.findViewById(R.id.selectedDate);
        pieChart = view.findViewById(R.id.pieChart);
        buyItemsText = view.findViewById(R.id.buyItemsText);
        buyAmountText = view.findViewById(R.id.buyAmountText);
        sellItemsText = view.findViewById(R.id.sellItemsText);
        sellAmountText = view.findViewById(R.id.sellAmountText);
        profitText = view.findViewById(R.id.profitText);
        rvHistory = view.findViewById(R.id.rvHistory);
        View dateSelector = view.findViewById(R.id.dateSelector);
        TextView viewAllTransactions = view.findViewById(R.id.tvViewAllTransactions);

        dateSelector.setOnClickListener(v -> showDatePickerDialog());

        if (viewAllTransactions != null) {
            viewAllTransactions.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                startActivity(intent);
            });
        }

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(getContext(), transactionList);
        rvHistory.setAdapter(transactionAdapter);

        updateDataForDate(currentSelectedDate.get(Calendar.DAY_OF_MONTH), currentSelectedDate.get(Calendar.MONTH), currentSelectedDate.get(Calendar.YEAR));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDataForDate(currentSelectedDate.get(Calendar.DAY_OF_MONTH), currentSelectedDate.get(Calendar.MONTH), currentSelectedDate.get(Calendar.YEAR));
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (datePicker, year1, monthOfYear, dayOfMonth) -> {
                    currentSelectedDate.set(year1, monthOfYear, dayOfMonth);
                    updateDataForDate(dayOfMonth, monthOfYear, year1);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateDataForDate(int day, int month, int year) {
        String dateSeed = day + "/" + (month + 1) + "/" + year;

        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year, month, day, 0, 0, 0);
        selectedCalendar.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (selectedCalendar.equals(today)) {
            selectedDate.setText("Today");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            selectedDate.setText(sdf.format(selectedCalendar.getTime()));
        }

        if (selectedCalendar.after(today)) {
            clearAllData();
        } else {
            List<Transaction> dailyTransactions = generateTransactionsForDate(dateSeed, selectedCalendar);
            setupPieChart(dailyTransactions);
            Collections.shuffle(dailyTransactions);
            loadRecentTransactions(dailyTransactions);
        }
    }

    private List<Transaction> generateTransactionsForDate(String dateSeed, Calendar selectedCalendar) {
        List<Transaction> transactions = new ArrayList<>();
        Random random = new Random(dateSeed.hashCode());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String transactionDate = sdf.format(selectedCalendar.getTime());

        String[] buyProductNames = {"Second-hand Textbook", "Used Keyboard", "Antique Desk Lamp", "Classic Novel Set", "Refurbished Laptop"};
        String[] sellProductNames = {"Vintage T-Shirt", "Handmade Bracelet", "Collectible Action Figure", "Retro Video Game", "Used Smartphone"};

        int totalTransactions = random.nextInt(15) + 5; // 5 to 19 transactions

        for (int i = 0; i < totalTransactions; i++) {
            boolean isBuy = random.nextDouble() > 0.6; // Skew towards selling
            String name;
            double amount;

            if (isBuy) {
                name = buyProductNames[random.nextInt(buyProductNames.length)];
                amount = 20 + (random.nextDouble() * 150);
            } else {
                name = sellProductNames[random.nextInt(sellProductNames.length)];
                amount = 30 + (random.nextDouble() * 300);
            }
            transactions.add(new Transaction(name, transactionDate, amount, isBuy));
        }
        return transactions;
    }

    private void clearAllData() {
        pieChart.clear();
        pieChart.setCenterText("No Data");
        pieChart.invalidate();

        buyItemsText.setText("Buy : 0 items");
        buyAmountText.setText(AppSettings.formatPrice(getContext(), 0));
        sellItemsText.setText("Sell : 0 items");
        sellAmountText.setText(AppSettings.formatPrice(getContext(), 0));
        profitText.setText("Profit: " + AppSettings.formatPrice(getContext(), 0));

        transactionList.clear();
        transactionAdapter.notifyDataSetChanged();
    }

    private void setupPieChart(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            clearAllData();
            return;
        }

        int buyItems = 0;
        int sellItems = 0;
        float buyAmount = 0;
        float sellAmount = 0;

        for (Transaction t : transactions) {
            if (t.isBuy()) {
                buyItems++;
                buyAmount += t.getAmount();
            } else {
                sellItems++;
                sellAmount += t.getAmount();
            }
        }

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(buyItems, "Buy"));
        pieEntries.add(new PieEntry(sellItems, "Sell"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#F44336")});
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(12f);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Items");
        pieChart.animateY(1000);
        pieChart.invalidate();

        buyItemsText.setText("Buy : " + buyItems + " items");
        buyAmountText.setText(AppSettings.formatPrice(getContext(), buyAmount));
        sellItemsText.setText("Sell : " + sellItems + " items");
        sellAmountText.setText(AppSettings.formatPrice(getContext(), sellAmount));

        float profit = sellAmount - buyAmount;
        profitText.setText("Profit: " + AppSettings.formatPrice(getContext(), profit));
    }

    private void loadRecentTransactions(List<Transaction> transactions) {
        transactionList.clear();
        int itemsToShow = Math.min(transactions.size(), 3);
        for (int i = 0; i < itemsToShow; i++) {
            transactionList.add(transactions.get(i));
        }
        transactionAdapter.notifyDataSetChanged();
    }
}
