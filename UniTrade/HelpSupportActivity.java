package com.example.unitrade;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class HelpSupportActivity extends BaseActivity {

    private static final int REQUEST_CALL_PERMISSION = 101;
    private RecyclerView rvFaqs;
    private FaqAdapter adapter;
    private List<Faq> faqList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        MaterialToolbar toolbar = findViewById(R.id.appBarHelpSupport);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        tintToolbarOverflow(toolbar);

        rvFaqs = findViewById(R.id.rvFaqs);
        rvFaqs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FaqAdapter(faqList);
        rvFaqs.setAdapter(adapter);

        findViewById(R.id.topic_account).setOnClickListener(v -> showAccountFaqs());
        findViewById(R.id.topic_payments).setOnClickListener(v -> showPaymentsFaqs());
        findViewById(R.id.topic_shipping).setOnClickListener(v -> showShippingFaqs());
        findViewById(R.id.btnCallUs).setOnClickListener(v -> callSupport());

        // Show account FAQs by default
        showAccountFaqs();
    }

    private void showAccountFaqs() {
        faqList.clear();
        faqList.add(new Faq("How do I change my password?", "You can change your password in the Login & Security section of the settings page."));
        faqList.add(new Faq("How do I edit my profile?", "You can edit your profile from the Settings page by selecting Edit Profile."));
        faqList.add(new Faq("How do I delete my account?", "Please contact our support team via the Call Us button to request account deletion."));
        adapter.notifyDataSetChanged();
    }

    private void showPaymentsFaqs() {
        faqList.clear();
        faqList.add(new Faq("What payment methods do you accept?", "We accept a variety of payment methods, including credit/debit cards and online bank transfers."));
        faqList.add(new Faq("How do I get a refund?", "Refund policies are determined by the seller. Please contact the seller directly to request a refund."));
        adapter.notifyDataSetChanged();
    }

    private void showShippingFaqs() {
        faqList.clear();
        faqList.add(new Faq("How does shipping work?", "Shipping is arranged between the buyer and seller. We recommend using a tracked shipping service."));
        faqList.add(new Faq("How much does shipping cost?", "Shipping costs vary depending on the item and the seller. Please check the item description for more information."));
        adapter.notifyDataSetChanged();
    }

    private void callSupport() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            makePhoneCall();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
    }

    private void makePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:+603-2345678"));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
