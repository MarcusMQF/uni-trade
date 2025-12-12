package com.example.unitrade;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SampleData {

    // ============================================================
    // CONSTANTS
    // ============================================================
    private static final String PKG = "com.example.unitrade";

    private static List<User> cachedUsers;
    private static List<Product> cachedProducts;

    // ============================================================
    // ID GENERATOR
    // ============================================================
    public static String generateProductId() {
        return "p" + System.currentTimeMillis()
                + ((int) (Math.random() * 900) + 100);
    }

    // ============================================================
    // USERS
    // ============================================================
    public static List<User> generateSampleUsers(Context context) {

        if (cachedUsers != null) return cachedUsers;

        cachedUsers = new ArrayList<>();

        cachedUsers.add(new User(
                "u1",
                "michael01",
                "Michael Choo Wei Jian",
                "michael.choo@um.edu.my",
                "017-452 9988",
                "android.resource://" + PKG + "/" + R.drawable.profile_pic_1,
                4.7,
                4.5,
                System.currentTimeMillis(),
                "Easy-going seller, fast response and honest descriptions.",
                0L
        ));

        cachedUsers.add(new User(
                "u2",
                "alicia_um",
                "Alicia Tan Yi Xuan",
                "alicia.tan@um.edu.my",
                "018-334 7721",
                "android.resource://" + PKG + "/" + R.drawable.profile_pic_2,
                4.9,
                4.8,
                System.currentTimeMillis(),
                "Loves collecting cute items.",
                0L
        ));

        cachedUsers.add(new User(
                "u3",
                "jonathan_cs",
                "Jonathan Lee Jun Hao",
                "jonathan.lee@um.edu.my",
                "016-982 4423",
                "android.resource://" + PKG + "/" + R.drawable.profile_pic_3,
                4.2,
                4.6,
                System.currentTimeMillis(),
                "Friendly UM student selling gadgets.",
                0L
        ));

        return cachedUsers;
    }

    public static User getUserById(Context context, String id) {
        for (User u : generateSampleUsers(context)) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    // ============================================================
    // PRODUCTS (ALL SAMPLE PRODUCTS)
    // ============================================================
    public static List<Product> generateSampleProducts(Context context) {

        if (cachedProducts != null) return cachedProducts;

        cachedProducts = new ArrayList<>();
        String qrUrl = "android.resource://" + PKG + "/" + R.drawable.qr_pic_1;

        // ---------------- Sellers ----------------
        List<User> users = generateSampleUsers(context);
        User sellerA = users.get(0);
        User sellerB = users.get(1);
        User sellerC = users.get(2);

        String description =
                "This item is in good condition. Perfect for students. " +
                        "Lightly used and still functioning well.";

        // ======================================================
        // p1 Polaroid Camera
        // ======================================================
        List<String> imgPolaroid = new ArrayList<>();
        imgPolaroid.add("android.resource://" + PKG + "/" + R.drawable.polaroid_caemra_pic_1);
        imgPolaroid.add("android.resource://" + PKG + "/" + R.drawable.polaroid_caemra_pic_2);
        imgPolaroid.add("android.resource://" + PKG + "/" + R.drawable.polaroid_caemra_pic_3);

        cachedProducts.add(new Product(
                "p1", "Polaroid Camera", 129.99,
                imgPolaroid, description,
                "Good", 500, "Donated",
                "Hobbies", "KK12 Block D",
                sellerA.getId(), qrUrl
        ));

        // ======================================================
        // p2 Mechanical Keyboard
        // ======================================================
        List<String> imgKeyboard = new ArrayList<>();
        imgKeyboard.add("android.resource://" + PKG + "/" + R.drawable.mechanical_keyboard_pic_1);
        imgKeyboard.add("android.resource://" + PKG + "/" + R.drawable.mechanical_keyboard_pic_2);
        imgKeyboard.add("android.resource://" + PKG + "/" + R.drawable.mechanical_keyboard_pic_3);

        cachedProducts.add(new Product(
                "p2", "Mechanical Keyboard", 89.00,
                imgKeyboard, description,
                "Like New", 0, "Available",
                "Electronics", "KK3 Block B",
                sellerA.getId(), qrUrl
        ));

        // ======================================================
        // p3 iPhone XR
        // ======================================================
        List<String> imgIphone = new ArrayList<>();
        imgIphone.add("android.resource://" + PKG + "/" + R.drawable.iphonexr_pic_1);
        imgIphone.add("android.resource://" + PKG + "/" + R.drawable.iphonexr_pic_2);
        imgIphone.add("android.resource://" + PKG + "/" + R.drawable.iphonexr_pic_3);

        cachedProducts.add(new Product(
                "p3", "iPhone XR", 599.00,
                imgIphone, description,
                "Fair", 27, "Sold",
                "Electronics", "UM Central Library",
                sellerA.getId(), qrUrl
        ));

        // ======================================================
        // p4 Stationery Set
        // ======================================================
        List<String> imgStationery = new ArrayList<>();
        imgStationery.add("android.resource://" + PKG + "/" + R.drawable.stationery_set_pic_1);
        imgStationery.add("android.resource://" + PKG + "/" + R.drawable.stationery_set_pic_2);
        imgStationery.add("android.resource://" + PKG + "/" + R.drawable.stationery_set_pic_3);

        cachedProducts.add(new Product(
                "p4", "UM Stationery Set", 15.00,
                imgStationery, description,
                "Brand New", 0, "Available",
                "Stationery", "FBA – Business Faculty",
                sellerB.getId(), qrUrl
        ));

        // ======================================================
        // p5 Nike Running Shoes
        // ======================================================
        List<String> imgShoes = new ArrayList<>();
        imgShoes.add("android.resource://" + PKG + "/" + R.drawable.nike_running_shoes_pic_1);
        imgShoes.add("android.resource://" + PKG + "/" + R.drawable.nike_running_shoes_pic_2);
        imgShoes.add("android.resource://" + PKG + "/" + R.drawable.nike_running_shoes_pic_3);

        cachedProducts.add(new Product(
                "p5", "Nike Running Shoes", 120.00,
                imgShoes, description,
                "Good", 48, "Available",
                "Fashion", "KK11 Block A",
                sellerA.getId(), qrUrl
        ));

        // ======================================================
        // p6 Hostel Table Lamp
        // ======================================================
        List<String> imgLamp = new ArrayList<>();
        imgLamp.add("android.resource://" + PKG + "/" + R.drawable.hostel_table_lamp_pic_1);
        imgLamp.add("android.resource://" + PKG + "/" + R.drawable.hostel_table_lamp_pic_2);
        imgLamp.add("android.resource://" + PKG + "/" + R.drawable.hostel_table_lamp_pic_3);

        cachedProducts.add(new Product(
                "p6", "Hostel Table Lamp", 18.90,
                imgLamp, description,
                "Like New", 0, "Sold",
                "Room Essentials", "Engineering Faculty",
                sellerC.getId(), qrUrl
        ));

        // ======================================================
        // p7 Basketball
        // ======================================================
        List<String> imgBasketball = new ArrayList<>();
        imgBasketball.add("android.resource://" + PKG + "/" + R.drawable.basketball_pic_1);
        imgBasketball.add("android.resource://" + PKG + "/" + R.drawable.basketball_pic_2);
        imgBasketball.add("android.resource://" + PKG + "/" + R.drawable.basketball_pic_3);

        cachedProducts.add(new Product(
                "p7", "Basketball", 25.00,
                imgBasketball, description,
                "Good", 1, "Sold",
                "Sports", "KK10 Sports Court",
                sellerB.getId(), qrUrl
        ));

        // ======================================================
        // p8 Textbook
        // ======================================================
        List<String> imgTextbook = new ArrayList<>();
        imgTextbook.add("android.resource://" + PKG + "/" + R.drawable.textbook_pic_1);
        imgTextbook.add("android.resource://" + PKG + "/" + R.drawable.textbook_pic_2);
        imgTextbook.add("android.resource://" + PKG + "/" + R.drawable.textbook_pic_3);

        cachedProducts.add(new Product(
                "p8", "WIA2001 Textbook", 30.00,
                imgTextbook, description,
                "Fair", 36, "Available",
                "Textbook", "Computer Science Faculty",
                sellerA.getId(), qrUrl
        ));

        // ======================================================
        // p9 Electric Kettle
        // ======================================================
        List<String> imgKettle = new ArrayList<>();
        imgKettle.add("android.resource://" + PKG + "/" + R.drawable.electric_kettle_pic_1);
        imgKettle.add("android.resource://" + PKG + "/" + R.drawable.electric_kettle_pic_2);
        imgKettle.add("android.resource://" + PKG + "/" + R.drawable.electric_kettle_pic_3);

        cachedProducts.add(new Product(
                "p9", "Electric Kettle", 49.00,
                imgKettle, description,
                "Good", 38, "Available",
                "Room Essentials", "KK9 Block C",
                sellerC.getId(), qrUrl
        ));

        // ======================================================
        // p10 Sushi Bento
        // ======================================================
        List<String> imgSushi = new ArrayList<>();
        imgSushi.add("android.resource://" + PKG + "/" + R.drawable.sushi_bento_pic_1);
        imgSushi.add("android.resource://" + PKG + "/" + R.drawable.sushi_bento_pic_2);
        imgSushi.add("android.resource://" + PKG + "/" + R.drawable.sushi_bento_pic_3);

        cachedProducts.add(new Product(
                "p10", "Sushi Bento", 8.50,
                imgSushi, description,
                "Brand New", 0, "Available",
                "Food", "Library Café",
                sellerB.getId(), qrUrl
        ));

        return cachedProducts;
    }


    // ============================================================
    // ACCESSORS
    // ============================================================
    public static List<Product> getAllProducts(Context context) {
        if (cachedProducts == null) generateSampleProducts(context);
        return cachedProducts;
    }

    public static Product getProductById(Context context, String id) {
        for (Product p : getAllProducts(context)) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    // ============================================================
    // SAFE UPDATE (CRITICAL FIX)
    // ============================================================
    public static Product updateProduct(Product updated) {
        if (cachedProducts == null || updated == null) return null;

        for (Product p : cachedProducts) {
            if (p.getId().equals(updated.getId())) {

                p.setName(updated.getName());
                p.setPrice(updated.getPrice());
                p.setImageUrls(new ArrayList<>(updated.getImageUrls()));
                p.setDescription(updated.getDescription());
                p.setCondition(updated.getCondition());
                p.setUsedDaysTotal(updated.getUsedDaysTotal());
                p.setStatus(updated.getStatus());
                p.setCategory(updated.getCategory());
                p.setLocation(updated.getLocation());
                p.setSellerId(updated.getSellerId());
                p.setQrPaymentUrl(updated.getQrPaymentUrl());
                p.setBuyerId(updated.getBuyerId());
                p.setImageVersion(updated.getImageVersion());

                return p; // 🔥 RETURN THE REAL INSTANCE
            }
        }
        return null;
    }

    // ============================================================
    // FILTER HELPERS
    // ============================================================
    public static List<Product> getAvailableItems(Context context, String userId) {
        List<Product> list = new ArrayList<>();
        for (Product p : getAllProducts(context)) {
            if (p.getStatus().equalsIgnoreCase("Available")
                    && !p.getSellerId().equals(userId)) {
                list.add(p);
            }
        }
        return list;
    }

    public static List<Product> getActiveItems(Context context, String userId) {
        List<Product> list = new ArrayList<>();
        for (Product p : getAllProducts(context)) {
            if (p.getSellerId().equals(userId)
                    && p.getStatus().equalsIgnoreCase("Available")) {
                list.add(p);
            }
        }
        return list;
    }

    public static List<Product> getCompletedItems(Context context, String userId) {
        List<Product> list = new ArrayList<>();
        for (Product p : getAllProducts(context)) {
            if (p.getSellerId().equals(userId)) {
                String s = p.getStatus().toLowerCase();
                if (s.equals("sold") || s.equals("donated")) {
                    list.add(p);
                }
            }
        }
        return list;
    }

    public static List<Review> generateMockReviewsForUser(
            Context context,
            User user) {

        List<Review> reviews = new ArrayList<>();

        List<User> users = generateSampleUsers(context);

        User u1 = users.get(0);
        User u2 = users.get(1);
        User u3 = users.get(2);

        // ---------------- SELLER REVIEWS ----------------
        reviews.add(new Review(
                "r1",
                u2,
                "Item condition exactly as described. Smooth transaction!",
                5.0,
                "5 Jun 2024",
                "seller"
        ));

        reviews.add(new Review(
                "r2",
                u3,
                "Friendly seller and fast response. Recommended.",
                4.5,
                "18 May 2024",
                "seller"
        ));

        // ---------------- USER (BUYER) REVIEWS ----------------
        reviews.add(new Review(
                "r3",
                u1,
                "Good buyer, punctual and polite during meetup.",
                4.0,
                "2 Apr 2024",
                "user"
        ));

        reviews.add(new Review(
                "r4",
                u2,
                "Transaction was smooth, no issues at all.",
                4.8,
                "12 Mar 2024",
                "user"
        ));

        // ---------------- OPTIONAL EXTRA ----------------
        reviews.add(new Review(
                "r5",
                u3,
                "Very understanding and easy to communicate with.",
                4.6,
                "28 Feb 2024",
                "user"
        ));

        return reviews;
    }

    public static void applyRatingsFromMockReviews(
            Context context,
            User user,
            List<Review> reviews) {

        if (user == null || reviews == null || reviews.isEmpty()) return;

        double total = 0;
        double sellerSum = 0;
        double userSum = 0;
        int sellerCount = 0;
        int userCount = 0;

        for (Review r : reviews) {
            total += r.getRating();

            if ("seller".equals(r.getType())) {
                sellerSum += r.getRating();
                sellerCount++;
            }

            if ("user".equals(r.getType())) {
                userSum += r.getRating();
                userCount++;
            }
        }

        user.setSellerRating(sellerCount == 0 ? 0 : sellerSum / sellerCount);
        user.setUserRating(userCount == 0 ? 0 : userSum / userCount);

        // Sync back to cached users
        updateUser(context, user);
    }

    public static void updateUser(Context context, User updatedUser) {
        if (updatedUser == null) return;

        List<User> users = generateSampleUsers(context);

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);

            if (u.getId().equals(updatedUser.getId())) {

                updatedUser.setLastEdited(System.currentTimeMillis());

                users.set(i, updatedUser);
                return;
            }
        }
    }

    public static void addProduct(Context context, Product newProduct) {

        if (newProduct == null) return;

        // Ensure product list exists
        if (cachedProducts == null) {
            cachedProducts = generateSampleProducts(context);
        }

        // Prevent duplicate IDs
        for (Product p : cachedProducts) {
            if (p.getId().equals(newProduct.getId())) {
                return; // already exists, do nothing
            }
        }

        // Add to TOP so it appears first in Home/Profile
        cachedProducts.add(0, newProduct);
    }

    public static void deleteProduct(Context context, String productId) {

        // Ensure product list exists
        if (cachedProducts == null) {
            cachedProducts = generateSampleProducts(context);
        }

        Iterator<Product> it = cachedProducts.iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p.getId().equals(productId)) {
                it.remove();   // ✅ SAFE removal
                return;
            }
        }
    }



}
