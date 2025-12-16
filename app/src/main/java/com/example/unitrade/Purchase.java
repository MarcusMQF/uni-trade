package com.example.unitrade;

import com.google.firebase.Timestamp;

public class Purchase {
    private String id;
    private String productId;
    private String buyerId;
    private String sellerId;
    private double price;
    private Timestamp purchaseDate;
    private String status; // "completed", "pending", "cancelled"
    private String receivingMethod; // "Face-to-face handover" or "Delivery"
    private String deliveryAddress;

    public Purchase() {
        // Required empty constructor for Firebase
    }

    public Purchase(String productId, String buyerId, String sellerId, double price) {
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.price = price;
        this.purchaseDate = Timestamp.now();
        this.status = "completed";
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Timestamp getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Timestamp purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReceivingMethod() { return receivingMethod; }
    public void setReceivingMethod(String receivingMethod) { this.receivingMethod = receivingMethod; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
