package com.example.unitrade;

import android.app.AlertDialog;
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
import androidx.core.content.ContextCompat;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        updateDataForDate(currentSelectedDate);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDataForDate(currentSelectedDate);
    }

    private void showDatePickerDialog() {
        int year = currentSelectedDate.get(Calendar.YEAR);
        int month = currentSelectedDate.get(Calendar.MONTH);
        int day = currentSelectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (datePicker, year1, monthOfYear, dayOfMonth) -> {
                    currentSelectedDate.set(year1, monthOfYear, dayOfMonth);
                    updateDataForDate(currentSelectedDate);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateDataForDate(Calendar selectedCalendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
        selectedDate.setText(sdf.format(selectedCalendar.getTime()));

        List<Transaction> dailyTransactions = fetchTransactionsForDate(selectedCalendar);

        if (dailyTransactions.isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("No Transactions")
                    .setMessage("No Buy or Sell transactions for this date.")
                    .setPositiveButton("OK", null)
                    .show();
            clearAllData();
            return;
        }

        setupPieChart(dailyTransactions);
        loadRecentTransactions(dailyTransactions);
    }

    private List<Transaction> fetchTransactionsForDate(Calendar selectedCalendar) {
        List<Transaction> transactions = new ArrayList<>();
        List<Product> allProducts = SampleData.generateSampleProducts(requireContext());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        for (Product product : allProducts) {
            Calendar productCalendar = Calendar.getInstance();
            productCalendar.setTime(new Date(product.getTransactionDate()));

            boolean sameDay = selectedCalendar.get(Calendar.YEAR) == productCalendar.get(Calendar.YEAR) &&
                    selectedCalendar.get(Calendar.DAY_OF_YEAR) == productCalendar.get(Calendar.DAY_OF_YEAR);

            if (sameDay) {
                // If status is "Sold", it's a Sell transaction. Otherwise, it's a Buy transaction (as per HistoryActivity logic).
                boolean isBuy = !"Sold".equalsIgnoreCase(product.getStatus());
                
                String imageUrl = null;
                if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                    imageUrl = product.getImageUrls().get(0);
                }

                transactions.add(new Transaction(
                        product.getName(),
                        sdf.format(productCalendar.getTime()),
                        product.getPrice(),
                        isBuy,
                        imageUrl
                ));
            }
        }
        // No sort needed if following random order? Or sort by time? SampleData doesn't have time other than date granularity often.
        // But user said "arrange follow the date sequence".
        // Since we filtered for a specific date, they are all on the same date.
        return transactions;
    }

    private void clearAllData() {
        pieChart.clear();
        pieChart.setCenterText("");
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
        pieDataSet.setColors(new int[]{Color.parseColor("#E57373"), Color.parseColor("#81C784")}); // Red for Buy, Green for Sell
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
        buyAmountText.setTextColor(Color.parseColor("#E57373"));
        sellItemsText.setText("Sell : " + sellItems + " items");
        sellAmountText.setText(AppSettings.formatPrice(getContext(), sellAmount));
        sellAmountText.setTextColor(Color.parseColor("#81C784"));

        float profit = sellAmount - buyAmount;
        profitText.setText("Profit: " + AppSettings.formatPrice(getContext(), profit));
    }

    private void loadRecentTransactions(List<Transaction> transactions) {
        transactionList.clear();
        transactionList.addAll(transactions);
        transactionAdapter.notifyDataSetChanged();
    }
}
