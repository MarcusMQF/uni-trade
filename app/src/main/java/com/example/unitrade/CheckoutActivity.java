package com.example.unitrade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CheckoutActivity extends BaseActivity {

    private static final String TAG = "CheckoutActivity";

    // Firebase
    private FirebaseFirestore db;

    // UI
    private Spinner spinnerReceivingMethod;
    private LinearLayout layoutDeliveryAddress;
    private LinearLayout itemsContainer;
    private EditText edtDeliveryAddress;
    private TextView txtDeliveryFee, txtSubtotal, txtTotal, txtDelivery;
    private Button btnPlaceOrder;
    private FloatingActionButton btnChatWithSeller;
    private MovableFabHelper mover;

    // Data
    private ArrayList<String> checkoutIds = new ArrayList<>();
    private ArrayList<Product> checkoutProducts = new ArrayList<>();

    private double subtotal = 0;
    private final double platformFee = 0.54;
    private final double deliveryFee = 3.00;
    private String selectedMethod = "";
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        currentUserId = UserSession.get().getId();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.appBarCheckout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Checkout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tintToolbarOverflow(toolbar);

        initViews();
        setupSpinner();

        // Get checkout IDs
        checkoutIds = getIntent().getStringArrayListExtra("checkout_ids");
        if (checkoutIds == null || checkoutIds.isEmpty()) {
            String singleId = getIntent().getStringExtra("product_id");
            if (singleId != null) {
                checkoutIds = new ArrayList<>();
                checkoutIds.add(singleId);
            } else {
                checkoutIds = new ArrayList<>();
            }
        }

        loadProductsFromFirebase();
    }

    // Load products from Firebase
    private void loadProductsFromFirebase() {
        if (checkoutIds.isEmpty()) {
            Toast.makeText(this, "No products to checkout", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkoutProducts.clear();

        for (String productId : checkoutIds) {
            db.collection("products")
                    .document(productId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            if ("available".equalsIgnoreCase(product.getStatus())) {
                                checkoutProducts.add(product);
                            } else {
                                Toast.makeText(this,
                                        product.getName() + " is no longer available",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        // Update UI once all products loaded
                        if (checkoutProducts.size() >= checkoutIds.size()) {
                            displayItems();
                            calculateSubtotal();
                            updateTotal();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading product: " + productId, e);
                        Toast.makeText(this, "Failed to load some products", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Initialize views
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

    // Display checkout items
    private void displayItems() {
        itemsContainer.removeAllViews();
        for (Product p : checkoutProducts) {
            View row = getLayoutInflater().inflate(R.layout.item_checkout_row, itemsContainer, false);
            ImageView img = row.findViewById(R.id.imgItem);
            TextView txtItemName = row.findViewById(R.id.txtItemName);
            TextView txtItemPrice = row.findViewById(R.id.txtItemPrice);

            txtItemName.setText(p.getName());
            txtItemPrice.setText(AppSettings.formatPrice(this, p.getPrice()));

            Glide.with(this).load(p.getImageUrls().get(0))
                    .signature(new com.bumptech.glide.signature.ObjectKey(p.getImageVersion()))
                    .into(img);

            itemsContainer.addView(row);
        }
    }

    private void calculateSubtotal() {
        subtotal = 0;
        for (Product p : checkoutProducts) subtotal += p.getPrice();
        txtSubtotal.setText("RM " + String.format("%.2f", subtotal));
    }

    private void setupSpinner() {
        String[] methods = {"Select Receiving method", "Face-to-face handover", "Delivery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, methods) {
            @Override
            public boolean isEnabled(int position) { return position != 0; }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return tv;
            }
        };
        spinnerReceivingMethod.setAdapter(adapter);

        spinnerReceivingMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateTotal() {
        double total = subtotal + platformFee;
        if ("Delivery".equals(selectedMethod)) total += deliveryFee;
        txtTotal.setText("RM " + String.format("%.2f", total));
    }

    // Finalize purchase and update Firebase
    private void finalizePurchase() {
        if (selectedMethod.isEmpty() || "Select Receiving method".equals(selectedMethod)) {
            Toast.makeText(this, "Please select a receiving method", Toast.LENGTH_SHORT).show();
            return;
        }

        String deliveryAddress = null;
        if ("Delivery".equals(selectedMethod)) {
            deliveryAddress = edtDeliveryAddress.getText().toString().trim();
            if (deliveryAddress.isEmpty()) {
                Toast.makeText(this, "Please enter delivery address", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Processing...");

        for (String productId : checkoutIds) {
            db.collection("products").document(productId)
                    .update(
                            "buyerId", currentUserId,
                            "status", "sold",
                            "transactionDate", System.currentTimeMillis()
                    )
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Product updated: " + productId))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update: " + productId, e));
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