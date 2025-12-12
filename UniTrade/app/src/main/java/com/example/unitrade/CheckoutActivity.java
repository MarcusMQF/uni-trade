package com.example.unitrade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CheckoutActivity extends BaseActivity {

    // UI
    private Spinner spinnerReceivingMethod;
    private LinearLayout layoutDeliveryAddress;
    private LinearLayout itemsContainer;
    private EditText edtDeliveryAddress;
    private TextView txtDeliveryFee, txtSubtotal, txtTotal, txtDelivery;
    private Button btnPlaceOrder;

    private MovableFabHelper mover;
    private FloatingActionButton btnChatWithSeller;

    // Data
    private ArrayList<Product> checkoutList = new ArrayList<>();
    private double subtotal = 0;
    private double platformFee = 0.54;
    private double deliveryFee = 3.00;
    private String selectedMethod = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Toolbar toolbar = findViewById(R.id.appBarCheckout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Checkout");
        tintToolbarOverflow(toolbar);

        initViews();
        setupSpinner();

        // Receive list from ShoppingCartActivity
        checkoutList = getIntent().getParcelableArrayListExtra("checkoutItems");
        if (checkoutList == null) checkoutList = new ArrayList<>();

        displayItems();
        calculateSubtotal();
        updateTotal();
    }

    private void initViews() {

        MaterialCardView layoutQrPayment = findViewById(R.id.layoutQrPayment);
        ImageView imgQrCode = findViewById(R.id.imgQrCode);
        Button btnScanQr = findViewById(R.id.btnScanQR);

        spinnerReceivingMethod = findViewById(R.id.spinnerReceivingMethod);
        layoutDeliveryAddress = findViewById(R.id.layoutDeliveryAddress);
        edtDeliveryAddress = findViewById(R.id.edtDeliveryAddress);
        txtDelivery = findViewById(R.id.txtDelivery);
        txtDeliveryFee = findViewById(R.id.txtDeliveryFee);
        itemsContainer = findViewById(R.id.itemsContainer);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtTotal = findViewById(R.id.txtTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        btnChatWithSeller = findViewById(R.id.btnChatWithSeller);

        mover = new MovableFabHelper();

        // Enable FAB movement
        View appBar = findViewById(R.id.appBarCheckout);
        View bottomBar = findViewById(R.id.bottomBar);
        View rootLayout = findViewById(android.R.id.content);

        appBar.post(() -> bottomBar.post(() ->
                mover.enable(btnChatWithSeller, rootLayout, appBar, bottomBar)
        ));

        btnChatWithSeller.setOnClickListener(v -> {
            Intent i = new Intent(CheckoutActivity.this, MainActivity.class);
            i.putExtra("openChatFragment", true);
            startActivity(i);
        });

        btnPlaceOrder.setOnClickListener(v -> finalizePurchase());

        btnScanQr.setOnClickListener(v -> {
            if (layoutQrPayment.getVisibility() == View.GONE) {
                layoutQrPayment.setVisibility(View.VISIBLE);
                btnScanQr.setText("Hide QR");
            } else {
                layoutQrPayment.setVisibility(View.GONE);
                btnScanQr.setText("Scan QR For Payment");
            }
        });
    }

    private void displayItems() {
        itemsContainer.removeAllViews();

        for (Product p : checkoutList) {

            View row = getLayoutInflater().inflate(R.layout.item_checkout_row, itemsContainer, false);
            ImageView img = row.findViewById(R.id.imgItem);
            TextView txtItemName = row.findViewById(R.id.txtItemName);
            TextView txtItemPrice = row.findViewById(R.id.txtItemPrice);

            txtItemName.setText(p.getName());
            txtItemPrice.setText("RM " + p.getPrice());

            Glide.with(this).load(p.getImageUrls().get(0)).into(img);
            itemsContainer.addView(row);
        }
    }

    private void calculateSubtotal() {
        subtotal = 0;
        for (Product p : checkoutList) {
            subtotal += p.getPrice();
        }
        txtSubtotal.setText("RM " + String.format("%.2f", subtotal));
    }

    private void setupSpinner() {
        String[] methods = {
                "Select Receiving method",
                "Face-to-face handover",
                "Delivery"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                methods
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // disable hint
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                tv.setTextColor(position == 0 ? Color.parseColor("#A0A0A0") : Color.BLACK);
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;

                tv.setTextColor(position == 0 ? Color.parseColor("#A0A0A0") : Color.BLACK);
                return v;
            }
        };

        spinnerReceivingMethod.setAdapter(adapter);

        spinnerReceivingMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedMethod = parent.getItemAtPosition(position).toString();

                if (selectedMethod.equals("Delivery")) {
                    layoutDeliveryAddress.setVisibility(View.VISIBLE);
                    txtDelivery.setVisibility(View.VISIBLE);
                    txtDeliveryFee.setVisibility(View.VISIBLE);
                    txtDeliveryFee.setText("RM " + deliveryFee);
                } else {
                    layoutDeliveryAddress.setVisibility(View.GONE);
                    txtDeliveryFee.setVisibility(View.GONE);
                }

                updateTotal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateTotal() {
        double total = subtotal + platformFee;

        if (selectedMethod.equals("Delivery")) {
            total += deliveryFee;
        }

        txtTotal.setText("RM " + String.format("%.2f", total));
    }

    // -------------------------------
    //      FINALIZE PURCHASE
    // -------------------------------
    private void finalizePurchase() {

        String currentUserId = UserSession.get().getId();

        for (Product p : checkoutList) {

            // -----------------------------------------
            // UPDATE PRODUCT DIRECTLY (GLOBAL INSTANCE)
            // -----------------------------------------
            p.setBuyerId(currentUserId);

            if (p.getPrice() > 0) {
                p.setStatus("Sold");
            } else {
                p.setStatus("Donated");
            }

            // -----------------------------------------
            // SAVE TO PURCHASE HISTORY
            // -----------------------------------------
            PurchaseHistoryManager.purchasedItems.add(p);
        }

        // -----------------------------------------
        // REMOVE FROM CART
        // -----------------------------------------
        CartManager.removePurchased(checkoutList);

        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();

        // -----------------------------------------
        // GO BACK TO HOME
        // -----------------------------------------
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("goToHome", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        finish();
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}