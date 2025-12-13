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

    private static List<User> cachedUsers = null;
    private static List<Product> cachedProducts = null;

    // ============================================================
    // PRODUCT ID GENERATOR
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
            String PKG = "com.example.unitrade";

            // -------------------------
            // USER 1
            // -------------------------
            List<Address> addresses1 = new ArrayList<>();
            addresses1.add(new Address(
                    "No. 12, Jalan Universiti, 50603 Kuala Lumpur",
                    true
            ));

        cachedUsers.add(new User(
                "u1",
                "michael01",
                "Michael Tan",
                "michael01@um.edu.my",
                "012-3456789",
                "android.resource://" + PKG + "/" + R.drawable.profile_pic_1,
                4.7,
                4.5,
                System.currentTimeMillis(),
                "Easy-going seller, fast response and honest descriptions.",
                System.currentTimeMillis(),
                addresses1
        ));

            // -------------------------
            // USER 2
            // -------------------------
            List<Address> addresses2 = new ArrayList<>();
            addresses2.add(new Address(
                    "KK8, University of Malaya, Kuala Lumpur",
                    true
            ));

        cachedUsers.add(new User(
                "u2",
                "alicia_um",
                "Alicia Wong",
                "alicia_um@um.edu.my",
                "013-8899776",
                "android.resource://" + PKG + "/" + R.drawable.profile_pic_2,
                4.9,
                4.8,
                System.currentTimeMillis(),
                "Loves collecting cute items.",
                System.currentTimeMillis(),
                addresses2
        ));

            // -------------------------
            // USER 3
            // -------------------------
            List<Address> addresses3 = new ArrayList<>();
            addresses3.add(new Address(
                    "Faculty of Computer Science, UM",
                    true
            ));

        cachedUsers.add(new User(
                "u3",
                "jonathan_cs",
                "Jonathan Lee",
                "jonathan_cs@um.edu.my",
                "017-5566778",
                "android.resource://" + PKG + "/" + R.drawable.profile_pic_3,
                4.2,
                4.6,
                System.currentTimeMillis(),
                "Friendly UM student selling gadgets.",
                System.currentTimeMillis(),
                addresses3
        ));

            return cachedUsers;
        }


    public static User getUserById(Context context, String id) {
        for (User u : generateSampleUsers(context)) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    public static List<User> getAllUsers(Context context) {

        if (cachedUsers == null) {
            cachedUsers = generateSampleUsers(context);
        }

        return cachedUsers;
    }

    public static void updateUser(Context context, User updated) {
        if (updated == null) return;

        List<User> users = getAllUsers(context);

        for (User u : users) {
            if (u.getId().equals(updated.getId())) {

                // MUTATE THE EXISTING OBJECT
                u.setUsername(updated.getUsername());
                u.setFullName(updated.getFullName());
                u.setEmail(updated.getEmail());
                u.setPhoneNumber(updated.getPhoneNumber());
                u.setBio(updated.getBio());

                u.setAddresses(updated.getAddresses());

                // 🔥 IMPORTANT: update image & version
                u.setProfileImageUrl(updated.getProfileImageUrl());
                u.setProfileImageVersion(updated.getProfileImageVersion());

                u.setSellerRating(updated.getSellerRating());
                u.setUserRating(updated.getUserRating());
                u.setLastEdited(System.currentTimeMillis());

                return;  // STOP HERE
            }
        }
    }



    // ============================================================
    // PRODUCTS (SAMPLE DATA)
    // ============================================================
    public static List<Product> generateSampleProducts(Context context) {

        if (cachedProducts != null) return cachedProducts;

        cachedProducts = new ArrayList<>();
        String qrUrl = "android.resource://" + PKG + "/" + R.drawable.qr_pic_1;

        List<User> users = generateSampleUsers(context);
        User sellerA = users.get(0);
        User sellerB = users.get(1);
        User sellerC = users.get(2);

        String desc =
                "This item is in good condition. Perfect for students. " +
                        "Lightly used and still functioning well.";

        cachedProducts.add(new Product("p1", "Polaroid Camera", 129.99,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.polaroid_caemra_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.polaroid_caemra_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.polaroid_caemra_pic_3
                ),
                desc, "Good", 500, "Available", "Hobbies",
                "KK12 Block D", sellerA.getId(), qrUrl));

        cachedProducts.add(new Product("p2", "Mechanical Keyboard", 89.00,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.mechanical_keyboard_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.mechanical_keyboard_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.mechanical_keyboard_pic_3
                ),
                desc, "Like New", 0, "Sold", "Electronics",
                "KK3 Block B", sellerA.getId(), qrUrl));

        cachedProducts.add(new Product("p3", "iPhone XR", 599.00,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.iphonexr_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.iphonexr_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.iphonexr_pic_3
                ),
                desc, "Fair", 27, "Sold", "Electronics",
                "UM Central Library", sellerA.getId(), qrUrl));

        cachedProducts.add(new Product("p4", "UM Stationery Set", 15.00,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.stationery_set_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.stationery_set_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.stationery_set_pic_3
                ),
                desc, "Brand New", 0, "Available", "Stationery",
                "FBA – Business Faculty", sellerB.getId(), qrUrl));

        cachedProducts.add(new Product("p5", "Nike Running Shoes", 120.00,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.nike_running_shoes_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.nike_running_shoes_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.nike_running_shoes_pic_3
                ),
                desc, "Good", 48, "Available", "Fashion",
                "KK11 Block A", sellerA.getId(), qrUrl));

        cachedProducts.add(new Product("p6", "Hostel Table Lamp", 18.90,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.hostel_table_lamp_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.hostel_table_lamp_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.hostel_table_lamp_pic_3
                ),
                desc, "Like New", 0, "Available", "Room Essentials",
                "Engineering Faculty", sellerC.getId(), qrUrl));

        cachedProducts.add(new Product("p7", "Basketball", 25.00,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.basketball_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.basketball_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.basketball_pic_3
                ),
                desc, "Good", 1, "Sold", "Sports",
                "KK10 Sports Court", sellerB.getId(), qrUrl));

        cachedProducts.add(new Product("p8", "WIA2001 Textbook", 30.00,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.textbook_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.textbook_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.textbook_pic_3
                ),
                desc, "Fair", 36, "Available", "Textbooks",
                "Computer Science Faculty", sellerA.getId(), qrUrl));

        cachedProducts.add(new Product("p9", "Electric Kettle", 49.00,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.electric_kettle_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.electric_kettle_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.electric_kettle_pic_3
                ),
                desc, "Good", 38, "Available", "Room Essentials",
                "KK9 Block C", sellerC.getId(), qrUrl));

        cachedProducts.add(new Product("p10", "Sushi Bento", 8.50,
                List.of(
                        "android.resource://" + PKG + "/" + R.drawable.sushi_bento_pic_1,
                        "android.resource://" + PKG + "/" + R.drawable.sushi_bento_pic_2,
                        "android.resource://" + PKG + "/" + R.drawable.sushi_bento_pic_3
                ),
                desc, "Brand New", 0, "Available", "Food",
                "Library Café", sellerB.getId(), qrUrl));

        return cachedProducts;
    }

    // ============================================================
    // ACCESSORS & MUTATORS
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


    // ============================================================
// REVIEWS (MOCK DATA)
// ============================================================
    public static List<Review> generateMockReviewsForUser(
            Context context,
            User user
    ) {

        List<Review> reviews = new ArrayList<>();
        if (user == null) return reviews;

        List<User> users = generateSampleUsers(context);

        // Avoid self-review if possible
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



}
