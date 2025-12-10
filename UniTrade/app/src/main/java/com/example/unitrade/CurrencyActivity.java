package com.example.unitrade;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class CurrencyActivity extends BaseActivity implements CurrencyAdapter.OnCurrencyClickListener {

    private RecyclerView rvCurrency;
    private CurrencyAdapter adapter;
    private List<Currency> currencyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        MaterialToolbar toolbar = findViewById(R.id.appBarCurrency);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Currency");
        }

        tintToolbarOverflow(toolbar);

        rvCurrency = findViewById(R.id.rvCurrency);
        rvCurrency.setLayoutManager(new LinearLayoutManager(this));

        setupCurrencyList();

        adapter = new CurrencyAdapter(this, currencyList, this);
        String currentCurrency = AppSettings.getCurrency(this);
        adapter.setSelectedCurrency(currentCurrency);
        rvCurrency.setAdapter(adapter);

        Button btnSave = findViewById(R.id.btnSaveCurrency);
        btnSave.setOnClickListener(v -> {
            saveSelectedCurrency();
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupCurrencyList() {
        currencyList.add(new Currency("Malaysian Ringgit", "RM"));
        currencyList.add(new Currency("US Dollar", "$"));
        currencyList.add(new Currency("Euro", "€"));
        currencyList.add(new Currency("British Pound", "£"));
        currencyList.add(new Currency("Japanese Yen", "¥"));
    }

    private void saveSelectedCurrency() {
        Currency selectedCurrency = adapter.getSelectedCurrency();
        if (selectedCurrency != null) {
            AppSettings.setCurrency(this, selectedCurrency.getCode());
        }
    }

    @Override
    public void onCurrencyClick(int position) {
        adapter.setSelectedPosition(position);
    }
}
