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
import java.util.List;

public class CheckoutActivity extends BaseActivity {

    // UI
    private Spinner spinnerReceivingMethod;
    private LinearLayout layoutDeliveryAddress;
    private LinearLayout itemsContainer;
    private EditText edtDeliveryAddress;
    private TextView txtDeliveryFee, txtSubtotal, txtTotal, txtDelivery;
    private Button btnPlaceOrder;

    private FloatingActionButton btnChatWithSeller;
    private MovableFabHelper mover;

    // DATA (ID BASED)
    private ArrayList<String> checkoutIds = new ArrayList<>();
    private ArrayList<Product> checkoutProducts = new ArrayList<>();

    private double subtotal = 0;
    private final double platformFee = 0.54;
    private final double deliveryFee = 3.00;
    private String selectedMethod = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // -------------------------------
        // Toolbar
        // -------------------------------
        Toolbar toolbar = findViewById(R.id.appBarCheckout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Checkout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tintToolbarOverflow(toolbar);

        initViews();
        setupSpinner();

        // -------------------------------
        // RECEIVE PRODUCT IDS
        checkoutIds = getIntent().getStringArrayListExtra("checkout_ids");

        if (checkoutIds == null || checkoutIds.isEmpty()) {

            // BUY NOW fallback
            String singleId = getIntent().getStringExtra("product_id");

            if (singleId != null) {
                checkoutIds = new ArrayList<>();
                checkoutIds.add(singleId);
            } else {
                checkoutIds = new ArrayList<>();
            }
        }


        for (String id : checkoutIds) {
            Product p = SampleData.getProductById(this, id);
            if (p != null) checkoutProducts.add(p);
        }

        displayItems();
        calculateSubtotal();
        updateTotal();
    }

    // =================================================
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

        View appBar = findViewById(R.id.appBarCheckout);
        View bottomBar = findViewById(R.id.bottomBar);
        View rootLayout = findViewById(android.R.id.content);

        appBar.post(() -> bottomBar.post(() ->
                mover.enable(btnChatWithSeller, rootLayout, appBar, bottomBar)
        ));

        btnChatWithSeller.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
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

    // =================================================
    private void displayItems() {
        itemsContainer.removeAllViews();

        for (Product p : checkoutProducts) {

            View row = getLayoutInflater()
                    .inflate(R.layout.item_checkout_row, itemsContainer, false);

            ImageView img = row.findViewById(R.id.imgItem);
            TextView txtItemName = row.findViewById(R.id.txtItemName);
            TextView txtItemPrice = row.findViewById(R.id.txtItemPrice);

            txtItemName.setText(p.getName());
            txtItemPrice.setText(AppSettings.formatPrice(this, p.getPrice()));

            Glide.with(this)
                    .load(p.getImageUrls().get(0))
                    .into(img);

            itemsContainer.addView(row);
        }
    }

    // =================================================
    private void calculateSubtotal() {
        subtotal = 0;
        for (Product p : checkoutProducts) {
            subtotal += p.getPrice();
        }
        txtSubtotal.setText("RM " + String.format("%.2f", subtotal));
    }

    // =================================================
    private void setupSpinner() {

        String[] methods = {
                "Select Receiving method",
                "Face-to-face handover",
                "Delivery"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_dropdown_item, methods) {

                    @Override
                    public boolean isEnabled(int position) {
                        return position != 0;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView tv = (TextView) super.getView(position, convertView, parent);
                        tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                        return tv;
                    }
                };

        spinnerReceivingMethod.setAdapter(adapter);

        spinnerReceivingMethod.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {

                        selectedMethod = parent.getItemAtPosition(position).toString();

                        if ("Delivery".equals(selectedMethod)) {
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
                }
        );
    }

    // =================================================
    private void updateTotal() {
        double total = subtotal + platformFee;
        if ("Delivery".equals(selectedMethod)) total += deliveryFee;
        txtTotal.setText("RM " + String.format("%.2f", total));
    }
    // =================================================
// FINALIZE PURCHASE (ID SAFE)
// =================================================
    private void finalizePurchase() {

        String currentUserId = UserSession.get().getId();
        List<String> purchasedIds = new ArrayList<>();

        for (String productId : checkoutIds) {

            Product p = SampleData.getProductById(this, productId);
            if (p == null) continue;

            p.setBuyerId(currentUserId);
            if (p.getPrice() > 0) {
                p.setStatus(Product.STATUS_SOLD);
            } else {
                p.setStatus(Product.STATUS_DONATED);
            }

            // ✅ THIS FIXES YOUR ERROR
            p.setTransactionDate(System.currentTimeMillis());

            purchasedIds.add(productId);
        }

        CartManager.removePurchasedByIds(this, checkoutIds);

        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("goToHome", true);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }



    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
