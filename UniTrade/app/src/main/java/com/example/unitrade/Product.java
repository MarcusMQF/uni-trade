package com.example.unitrade;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Product implements Parcelable {

    private String id;
    private String name;
    private double price;
    private List<String> imageUrls = new ArrayList<>();
    private String description;
    private String condition;
    private int usedDaysTotal;
    private String status;
    private String category;
    private String sellerId;
    private String location;
    private String buyerId;
    private boolean isHeader = false;
    private String qrPaymentUrl;

    private long imageVersion;


    public Product() {}

    public Product(String id, String name, double price, List<String> imageUrls,
                   String description, String condition, int usedDaysTotal,
                   String status, String category, String location,
                   String sellerId, String qrPaymentUrl) {

        this.id = id;
        this.name = name;
        this.price = price;
        setImageUrls(imageUrls); // ✅ SAFE
        this.description = description;
        this.condition = condition;
        this.usedDaysTotal = usedDaysTotal;
        this.status = status;
        this.category = category;
        this.location = location;
        this.sellerId = sellerId;
        this.qrPaymentUrl = qrPaymentUrl;
        this.imageVersion = System.currentTimeMillis();
    }

    // ---------------- GETTERS ----------------
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    public List<String> getImageUrls() {
        return imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }

    public String getDescription() { return description; }
    public String getCondition() { return condition; }
    public int getUsedDaysTotal() { return usedDaysTotal; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getSellerId() { return sellerId; }
    public String getLocation() { return location; }
    public String getQrPaymentUrl() { return qrPaymentUrl; }
    public String getBuyerId() { return buyerId; }
    public boolean isHeader() { return isHeader; }

    public long getImageVersion() {
        return imageVersion;
    }

    // ---------------- SETTERS ----------------
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null
                ? new ArrayList<>()
                : new ArrayList<>(imageUrls);
    }

    public void setDescription(String description) { this.description = description; }
    public void setCondition(String condition) { this.condition = condition; }
    public void setUsedDaysTotal(int usedDaysTotal) { this.usedDaysTotal = usedDaysTotal; }
    public void setStatus(String status) { this.status = status; }
    public void setCategory(String category) { this.category = category; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setLocation(String location) { this.location = location; }
    public void setQrPaymentUrl(String qrPaymentUrl) { this.qrPaymentUrl = qrPaymentUrl; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public void setHeader(boolean header) { isHeader = header; }

    public void setImageVersion(long imageVersion) {
        this.imageVersion = imageVersion;
    }

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
        buyerId = in.readString();
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
        dest.writeString(buyerId);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Parcelable.Creator<Product> CREATOR = new Creator<>() {
        @Override public Product createFromParcel(Parcel in) { return new Product(in); }
        @Override public Product[] newArray(int size) { return new Product[size]; }
    };

    // ---------------- EQUALITY ----------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product p = (Product) o;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    public static List<Product> filterBySeller(
            List<Product> allProducts,
            String sellerId
    ) {

        List<Product> result = new ArrayList<>();

        if (allProducts == null || sellerId == null) return result;

        for (Product p : allProducts) {
            if (sellerId.equals(p.getSellerId())) {
                result.add(p);
            }
        }

        return result;
    }
}
