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
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionStatisticsFragment extends Fragment {

    enum TimeMode {
        DAY,
        MONTH,
        YEAR
    }

    private TextView selectedDate;
    private PieChart pieChart;
    private TextView buyItemsText, buyAmountText, sellItemsText, sellAmountText, profitText;
    private RecyclerView rvHistory;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private Calendar currentSelectedDate = Calendar.getInstance();

    private TimeMode currentMode = TimeMode.DAY;

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


        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggleTimeRange);

        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnDay) {
                currentMode = TimeMode.DAY;
                // keep exact day
            }
            else if (checkedId == R.id.btnMonth) {
                currentMode = TimeMode.MONTH;


                currentSelectedDate.set(Calendar.DAY_OF_MONTH, 1);
            }
            else if (checkedId == R.id.btnYear) {
                currentMode = TimeMode.YEAR;


                currentSelectedDate.set(Calendar.MONTH, Calendar.JANUARY);
                currentSelectedDate.set(Calendar.DAY_OF_MONTH, 1);
            }

            updateDataForDate(currentSelectedDate);
        });

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

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (datePicker, y, m, d) -> {

                    if (currentMode == TimeMode.DAY) {
                        currentSelectedDate.set(y, m, d);
                    } else if (currentMode == TimeMode.MONTH) {
                        currentSelectedDate.set(y, m, 1); // ignore day
                    } else {
                        currentSelectedDate.set(y, Calendar.JANUARY, 1); // year only
                    }

                    updateDataForDate(currentSelectedDate);
                },
                year, month, day
        );

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }


    private void updateDataForDate(Calendar selectedCalendar) {
        SimpleDateFormat sdf;

        if (currentMode == TimeMode.DAY) {
            sdf = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
        } else if (currentMode == TimeMode.MONTH) {
            sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        } else {
            sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        }

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
        List<Product> allProducts = SampleData.getAllProducts(requireContext());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        String me = UserSession.get().getId();
        if (me == null) return transactions;

        for (Product product : allProducts) {

            if (product.getTransactionDate() <= 0) continue;

            Calendar productCal = Calendar.getInstance();
            productCal.setTimeInMillis(product.getTransactionDate());

            boolean match;
            if (currentMode == TimeMode.DAY) {
                match = selectedCalendar.get(Calendar.YEAR) == productCal.get(Calendar.YEAR)
                        && selectedCalendar.get(Calendar.DAY_OF_YEAR) == productCal.get(Calendar.DAY_OF_YEAR);
            } else if (currentMode == TimeMode.MONTH) {
                match = selectedCalendar.get(Calendar.YEAR) == productCal.get(Calendar.YEAR)
                        && selectedCalendar.get(Calendar.MONTH) == productCal.get(Calendar.MONTH);
            } else {
                match = selectedCalendar.get(Calendar.YEAR) == productCal.get(Calendar.YEAR);
            }

            if (!match) continue;

            boolean iBought = me.equals(product.getBuyerId());
            boolean iSold   = me.equals(product.getSellerId());

            if (!iBought && !iSold) continue;

            // Ignore donations
            if ("Donated".equalsIgnoreCase(product.getStatus()) || product.getPrice() <= 0) {
                continue;
            }

            boolean isBuy = iBought;

            String imageUrl = product.getImageUrls().isEmpty()
                    ? null
                    : product.getImageUrls().get(0);

            transactions.add(new Transaction(
                    product.getName(),
                    sdf.format(productCal.getTime()),
                    product.getPrice(),
                    isBuy,
                    imageUrl,
                    product.getTransactionDate(),
                    product.getImageVersion()
            ));
        }

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
