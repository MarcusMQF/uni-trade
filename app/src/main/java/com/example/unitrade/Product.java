package com.example.unitrade;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Product implements Parcelable {

    // ---------------- BASIC INFO ----------------
    private String productId;
    private String productName;
    private double productPrice;

    private long createdAt;
    private List<String> imageUrls = new ArrayList<>();
    private String productDescription;
    private String productCondition;
    private int productUsedDaysTotal;
    private String productStatus;
    private String productCategory;
    private String location;

    // ---------------- USER INFO ----------------
    private String sellerId;
    private String buyerId;

    // ---------------- UI / META ----------------
    private boolean isHeader = false;
    private String qrPaymentUrl;

    // 🔥 NEW: listing date (auto-created)
    private Date listingDate;

    // 🔥 Image cache busting
    private long imageVersion;

    private long transactionDate;

    // ---------------- TRANSACTION INFO ----------------
    private String soldTo; // buyer's user ID
    private Timestamp soldAt; // when it was sold
    private String purchaseId; // reference to purchase document

    private boolean buyTransaction;
    private boolean sellTransaction;
    private boolean donation;
    private int stability;

    public static final String STATUS_BOUGHT = "BOUGHT";
    public static final String STATUS_SOLD = "SOLD";
    public static final String STATUS_DONATED = "DONATED";

    // ---------------- CONSTRUCTORS ----------------
    public Product() {
        // empty constructor (required for Firebase)
    }

    public Product(
            String id,
            String name,
            double price,
            List<String> imageUrls,
            String description,
            String condition,
            int usedDaysTotal,
            String status,
            String category,
            String location,
            String sellerId,
            String qrPaymentUrl) {
        this.productId = id;
        this.productName = name;
        this.productPrice = price;
        setImageUrls(imageUrls);
        this.productDescription = description;
        this.productCondition = condition;
        this.productUsedDaysTotal = usedDaysTotal;
        this.productStatus = status;
        this.productCategory = category;
        this.location = location;
        this.sellerId = sellerId;
        this.qrPaymentUrl = qrPaymentUrl;

        this.imageVersion = System.currentTimeMillis();
        this.listingDate = new Date();
    }

    // ---------------- GETTERS ----------------
    public String getId() {
        return productId;
    }

    public String getName() {
        return productName;
    }

    public double getPrice() {
        return productPrice;
    }

    public List<String> getImageUrls() {
        return imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }

    public String getDescription() {
        return productDescription;
    }

    public String getCondition() {
        return productCondition;
    }

    public int getUsedDaysTotal() {
        return productUsedDaysTotal;
    }

    public String getStatus() {
        return productStatus;
    }

    public String getCategory() {
        return productCategory;
    }

    public String getLocation() {
        return location;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getQrPaymentUrl() {
        return qrPaymentUrl;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public Date getListingDate() {
        if (listingDate == null)
            listingDate = new Date();
        return listingDate;
    }

    public long getTransactionDate() {
        return transactionDate;
    }

    public long getImageVersion() {
        return imageVersion;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getSoldTo() {
        return soldTo;
    }

    public Timestamp getSoldAt() {
        return soldAt;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public boolean isBuyTransactionFlag() {
        return buyTransaction;
    }

    public boolean isSellTransactionFlag() {
        return sellTransaction;
    }

    public boolean isDonationFlag() {
        return donation;
    }

    public int getStability() {
        return stability;
    }

    // ---------------- SETTERS ----------------
    public void setId(String id) {
        this.productId = id;
    }

    public void setName(String name) {
        this.productName = name;
    }

    public void setPrice(double price) {
        this.productPrice = price;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }

    public void setDescription(String description) {
        this.productDescription = description;
    }

    public void setCondition(String condition) {
        this.productCondition = condition;
    }

    public void setUsedDaysTotal(int usedDaysTotal) {
        this.productUsedDaysTotal = usedDaysTotal;
    }

    public void setStatus(String status) {
        this.productStatus = status;
    }

    public void setCategory(String category) {
        this.productCategory = category;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public void setQrPaymentUrl(String qrPaymentUrl) {
        this.qrPaymentUrl = qrPaymentUrl;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public void setListingDate(Date listingDate) {
        this.listingDate = listingDate;
    }

    public void setTransactionDate(long transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setImageVersion(long imageVersion) {
        this.imageVersion = imageVersion;
    }

    public void setSoldTo(String soldTo) {
        this.soldTo = soldTo;
    }

    public void setSoldAt(Timestamp soldAt) {
        this.soldAt = soldAt;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    public void setBuyTransaction(boolean buyTransaction) {
        this.buyTransaction = buyTransaction;
    }

    public void setSellTransaction(boolean sellTransaction) {
        this.sellTransaction = sellTransaction;
    }

    public void setDonation(boolean donation) {
        this.donation = donation;
    }

    public void setStability(int stability) {
        this.stability = stability;
    }

    // ---------------- PARCELABLE ----------------
    protected Product(Parcel in) {
        productId = in.readString();
        productName = in.readString();
        productPrice = in.readDouble();
        imageUrls = in.createStringArrayList();
        productDescription = in.readString();
        productCondition = in.readString();
        productUsedDaysTotal = in.readInt();
        productStatus = in.readString();
        productCategory = in.readString();
        location = in.readString();
        sellerId = in.readString();
        buyerId = in.readString();
        isHeader = in.readByte() != 0;
        qrPaymentUrl = in.readString();
        imageVersion = in.readLong();

        long time = in.readLong();
        listingDate = time == -1 ? null : new Date(time);
        transactionDate = in.readLong();

        soldTo = in.readString();
        long soldAtTime = in.readLong();
        soldAt = soldAtTime == -1 ? null : new Timestamp(new Date(soldAtTime));
        purchaseId = in.readString();

        buyTransaction = in.readByte() != 0;
        sellTransaction = in.readByte() != 0;
        donation = in.readByte() != 0;
        stability = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId); // Fixed: was writing productStatus instead of productId
        dest.writeString(productName);
        dest.writeDouble(productPrice);
        dest.writeStringList(imageUrls);
        dest.writeString(productDescription);
        dest.writeString(productCondition);
        dest.writeInt(productUsedDaysTotal);
        dest.writeString(productStatus);
        dest.writeString(productCategory);
        dest.writeString(location);
        dest.writeString(sellerId);
        dest.writeString(buyerId);
        dest.writeByte((byte) (isHeader ? 1 : 0));
        dest.writeString(qrPaymentUrl);
        dest.writeLong(imageVersion);
        dest.writeLong(listingDate != null ? listingDate.getTime() : -1);
        dest.writeLong(transactionDate);

        dest.writeString(soldTo);
        dest.writeLong(soldAt != null ? soldAt.toDate().getTime() : -1);
        dest.writeString(purchaseId);

        dest.writeByte((byte) (buyTransaction ? 1 : 0));
        dest.writeByte((byte) (sellTransaction ? 1 : 0));
        dest.writeByte((byte) (donation ? 1 : 0));
        dest.writeInt(stability);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Product> CREATOR = new Creator<>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    // ---------------- EQUALITY ----------------
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Product))
            return false;
        Product p = (Product) o;
        return productId != null && productId.equals(p.productId);
    }

    @Override
    public int hashCode() {
        return productId == null ? 0 : productId.hashCode();
    }

    public boolean isBuyTransaction() {
        return STATUS_BOUGHT.equals(productStatus);
    }

    public boolean isSellTransaction() {
        return STATUS_SOLD.equals(productStatus);
    }

    public boolean isDonation() {
        return STATUS_DONATED.equals(productStatus);
    }

    // ---------------- UTIL ----------------
    public static List<Product> filterBySeller(List<Product> allProducts, String sellerId) {
        List<Product> result = new ArrayList<>();
        if (allProducts == null || sellerId == null)
            return result;
        for (Product p : allProducts)
            if (sellerId.equals(p.getSellerId()))
                result.add(p);
        return result;
    }
}
