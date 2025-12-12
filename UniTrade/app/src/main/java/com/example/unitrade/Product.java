package com.example.unitrade;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Product implements Parcelable {

    private String id;
    private String name;
    private double price;
    private List<String> imageUrls;
    private String description;
    private String condition;
    private int usedDaysTotal;
    private String status;
    private String category;
    private String sellerId;
    private String location;
    private boolean isHeader = false;

    private String qrPaymentUrl;
    private long transactionDate; // NEW: Store transaction date (timestamp)

    public Product() {}

    public Product(String id, String name, double price, List<String> imageUrls,
                   String description, String condition, int usedDaysTotal,
                   String status, String category, String location,
                   String sellerId, String qrPaymentUrl, long transactionDate) {   // ← Updated parameter

        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrls = imageUrls;
        this.description = description;
        this.condition = condition;
        this.usedDaysTotal = usedDaysTotal;
        this.status = status;
        this.category = category;
        this.location = location;
        this.sellerId = sellerId;
        this.qrPaymentUrl = qrPaymentUrl;
        this.transactionDate = transactionDate;
    }

    // Constructor without transactionDate for backward compatibility if needed (defaults to 0 or now)
    public Product(String id, String name, double price, List<String> imageUrls,
                   String description, String condition, int usedDaysTotal,
                   String status, String category, String location,
                   String sellerId, String qrPaymentUrl) {
        this(id, name, price, imageUrls, description, condition, usedDaysTotal, status, category, location, sellerId, qrPaymentUrl, System.currentTimeMillis());
    }

    // ---------------- GETTERS ----------------

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getDescription() { return description; }
    public String getCondition() { return condition; }
    public int getUsedDaysTotal() { return usedDaysTotal; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getSellerId() { return sellerId; }
    public String getLocation() { return location; }
    public boolean isHeader() { return isHeader; }
    public String getQrPaymentUrl() { return qrPaymentUrl; }
    public long getTransactionDate() { return transactionDate; } // NEW

    // ---------------- SETTERS ----------------

    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public void setDescription(String description) { this.description = description; }
    public void setCondition(String condition) { this.condition = condition; }
    public void setPrice(double price) { this.price = price; }
    public void setUsedDaysTotal(int usedDaysTotal) { this.usedDaysTotal = usedDaysTotal; }
    public void setStatus(String status) { this.status = status; }
    public void setCategory(String category) { this.category = category; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setHeader(boolean header) { this.isHeader = header; }
    public void setQrPaymentUrl(String qrPaymentUrl) { this.qrPaymentUrl = qrPaymentUrl; }
    public void setTransactionDate(long transactionDate) { this.transactionDate = transactionDate; } // NEW


    // ---------------- PARCELABLE ----------------

    protected Product(Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readDouble();
        imageUrls = in.createStringArrayList();
        description = in.readString();
        condition = in.readString();
        usedDaysTotal = in.readInt();
        status = in.readString();
        category = in.readString();
        location = in.readString();
        sellerId = in.readString();
        isHeader = in.readByte() != 0;
        qrPaymentUrl = in.readString();
        transactionDate = in.readLong(); // NEW
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeStringList(imageUrls);
        dest.writeString(description);
        dest.writeString(condition);
        dest.writeInt(usedDaysTotal);
        dest.writeString(status);
        dest.writeString(category);
        dest.writeString(location);
        dest.writeString(sellerId);
        dest.writeByte((byte) (isHeader ? 1 : 0));
        dest.writeString(qrPaymentUrl);
        dest.writeLong(transactionDate); // NEW
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) { return new Product(in); }

        @Override
        public Product[] newArray(int size) { return new Product[size]; }
    };

    @Override
    public int describeContents() { return 0; }


    // ---------------- FILTER UTILITY ----------------

    public static List<Product> filterBySeller(List<Product> all, String sellerId) {
        List<Product> result = new ArrayList<>();
        for (Product p : all) {
            if (sellerId.equals(p.getSellerId())) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product p = (Product) o;
        return this.id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Date getListingDate() {
        return new Date(transactionDate);
    }

    public String getFormattedDate() {
        Date date = new Date(transactionDate);
        return date.toString();
    }
}
