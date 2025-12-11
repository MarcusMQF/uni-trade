package com.example.unitrade;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SampleData {

    private static final String PKG = "com.example.unitrade";
    private static List<User> cachedUsers = null;
    private static List<Product> cachedProducts = null;

    public static List<User> generateSampleUsers(Context context) {
        if (cachedUsers != null) return cachedUsers;
        List<User> users = new ArrayList<>();
        users.add(new User(
                "u1", "michael01", "Michael Choo Wei Jian", "michael.choo@um.edu.my", "017-452 9988",
                "android.resource://" + PKG + "/" + R.drawable.profile_pic_1, 4.7, 4.5,
                System.currentTimeMillis() - (5 * 60 * 1000), "Easy-going seller, fast response and honest descriptions.\n\nAll items are well-kept and accurately described.",
                0L, Collections.emptyList(), 0));
        users.add(new User(
                "u2", "alicia_um", "Alicia Tan Yi Xuan", "alicia.tan@um.edu.my", "018-334 7721",
                "android.resource://" + PKG + "/" + R.drawable.profile_pic_2, 4.9, 4.8,
                System.currentTimeMillis() - (45 * 60 * 1000), "Loves collecting cute items. Keeps everything in excellent condition.",
                0L, Collections.emptyList(), 0));
        users.add(new User(
                "u3", "jonathan_cs", "Jonathan Lee Jun Hao", "jonathan.lee@um.edu.my", "016-982 4423",
                "android.resource://" + PKG + "/" + R.drawable.profile_pic_3, 4.2, 4.6,
                System.currentTimeMillis() - (3 * 60 * 60 * 1000), "Friendly UM student selling tech gadgets and stationery.",
                0L, Collections.emptyList(), 0));
        cachedUsers = users;
        return users;
    }

    public static User getUserById(Context context, String id) {
        for (User u : generateSampleUsers(context)) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    public static void updateUser(Context context, User updatedUser) {
        if (updatedUser == null) return;
        List<User> users = generateSampleUsers(context);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(updatedUser.getId())) {
                users.set(i, updatedUser);
                return;
            }
        }
    }

    public static List<Product> generateSampleProducts(Context context) {
        if (cachedProducts != null) return cachedProducts;

        List<Product> list = new ArrayList<>();
        String qrUrl = "android.resource://" + PKG + "/" + R.drawable.qr_pic_1;

        // --- Image Sets ---
        List<String> imgSetBasketBall = List.of("android.resource://" + PKG + "/" + R.drawable.basketball_pic_1, "android.resource://" + PKG + "/" + R.drawable.basketball_pic_2, "android.resource://" + PKG + "/" + R.drawable.basketball_pic_3);
        List<String> imgSetPolaroidCamera = List.of("android.resource://" + PKG + "/" + R.drawable.polaroid_caemra_pic_1, "android.resource://" + PKG + "/" + R.drawable.polaroid_caemra_pic_2, "android.resource://" + PKG + "/" + R.drawable.polaroid_caemra_pic_3);
        List<String> imgSetTextbook = List.of("android.resource://" + PKG + "/" + R.drawable.textbook_pic_1, "android.resource://" + PKG + "/" + R.drawable.textbook_pic_2, "android.resource://" + PKG + "/" + R.drawable.textbook_pic_3);
        List<String> imgSetElectricKettle = List.of("android.resource://" + PKG + "/" + R.drawable.electric_kettle_pic_1, "android.resource://" + PKG + "/" + R.drawable.electric_kettle_pic_2, "android.resource://" + PKG + "/" + R.drawable.electric_kettle_pic_3);
        List<String> imgSetSushiBento = List.of("android.resource://" + PKG + "/" + R.drawable.sushi_bento_pic_1, "android.resource://" + PKG + "/" + R.drawable.sushi_bento_pic_2, "android.resource://" + PKG + "/" + R.drawable.sushi_bento_pic_3);
        List<String> imgSetStationerySet = List.of("android.resource://" + PKG + "/" + R.drawable.stationery_set_pic_1, "android.resource://" + PKG + "/" + R.drawable.stationery_set_pic_2, "android.resource://" + PKG + "/" + R.drawable.stationery_set_pic_3);
        List<String> imgSetIphoneXR = List.of("android.resource://" + PKG + "/" + R.drawable.iphonexr_pic_1, "android.resource://" + PKG + "/" + R.drawable.iphonexr_pic_2, "android.resource://" + PKG + "/" + R.drawable.iphonexr_pic_3);
        List<String> imgSetHostelTableLamp = List.of("android.resource://" + PKG + "/" + R.drawable.hostel_table_lamp_pic_1, "android.resource://" + PKG + "/" + R.drawable.hostel_table_lamp_pic_2, "android.resource://" + PKG + "/" + R.drawable.hostel_table_lamp_pic_3);
        List<String> imgSetMechanicalKeyboard = List.of("android.resource://" + PKG + "/" + R.drawable.mechanical_keyboard_pic_1, "android.resource://" + PKG + "/" + R.drawable.mechanical_keyboard_pic_2, "android.resource://" + PKG + "/" + R.drawable.mechanical_keyboard_pic_3);
        List<String> imgSetNikeRunningShoes = List.of("android.resource://" + PKG + "/" + R.drawable.nike_running_shoes_pic_1, "android.resource://" + PKG + "/" + R.drawable.nike_running_shoes_pic_2, "android.resource://" + PKG + "/" + R.drawable.nike_running_shoes_pic_3);

        List<User> users = generateSampleUsers(context);
        User sellerA = users.get(0);
        User sellerB = users.get(1);
        User sellerC = users.get(2);

        String description = "This item is in good condition. Perfect for students. Lightly used and still functioning well.";

        // C:/Users/HAM ZENG YI/Documents/UniTrade/app/src/main/java/com/example/unitrade/SampleData.java

// --- Date Helpers ---
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();cal.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -2);
        Date twoDaysAgo = cal.getTime();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        Date oneWeekAgo = cal.getTime();
        cal.add(Calendar.MONTH, -1);
        Date oneMonthAgo = cal.getTime();

// --- Products ---
// FIX: Convert Date objects to long using .getTime()
        list.add(new Product("p1", "Polaroid Camera", 129.99, imgSetPolaroidCamera, description, "Good", 500, "Available", "Hobbies", "KK12 Block D", sellerA.getId(), qrUrl, today.getTime()));
        list.add(new Product("p2", "Mechanical Keyboard", 89.00, imgSetMechanicalKeyboard, description, "Like New", 0, "Available", "Electronics", "KK3 Block B", sellerA.getId(), qrUrl, yesterday.getTime()));
        list.add(new Product("p3", "iPhone XR", 599.00, imgSetIphoneXR, description, "Fair", 27, "Reserved", "Electronics", "UM Central Library", sellerA.getId(), qrUrl, twoDaysAgo.getTime()));
        list.add(new Product("p4", "UM Stationery Set", 15.00, imgSetStationerySet, description, "Brand New", 0, "Available", "Stationery", "FBA – Business Faculty", sellerB.getId(), qrUrl, oneWeekAgo.getTime()));
        list.add(new Product("p5", "Nike Running Shoes", 120.00, imgSetNikeRunningShoes, description, "Good", 48, "Available", "Fashion", "KK11 Block A", sellerA.getId(), qrUrl, oneMonthAgo.getTime()));
        list.add(new Product("p6", "Hostel Table Lamp", 18.90, imgSetHostelTableLamp, description, "Like New", 0, "Available", "Room Essentials", "Engineering Faculty", sellerC.getId(), qrUrl, today.getTime()));
        list.add(new Product("p7", "Basketball", 25.00, imgSetBasketBall, description, "Good", 1, "Sold", "Sports", "KK10 Sports Court", sellerB.getId(), qrUrl, yesterday.getTime()));
        list.add(new Product("p8", "WIA2001 Textbook", 30.00, imgSetTextbook, description, "Fair", 36, "Available", "Textbooks", "Computer Science Faculty", sellerA.getId(), qrUrl, twoDaysAgo.getTime()));
        list.add(new Product("p9", "Electric Kettle", 49.00, imgSetElectricKettle, description, "Good", 38, "Available", "Room Essentials", "KK9 Block C", sellerC.getId(), qrUrl, oneWeekAgo.getTime()));
        list.add(new Product("p10", "Sushi Bento", 8.50, imgSetSushiBento, description, "Brand New", 0, "Available", "Food", "Library Café", sellerB.getId(), qrUrl, oneMonthAgo.getTime()));


        cachedProducts = list;
        return list;
    }

    public static List<Review> generateReviewsForUser(Context context, User user) {
        List<User> users = generateSampleUsers(context);
        List<Review> reviews = new ArrayList<>();
        User michael = users.get(0);
        User alicia = users.get(1);
        User jonathan = users.get(2);

        reviews.add(new Review("r1", alicia, "The item arrived as described! Very friendly seller and fast response.", 5.0, "June 5, 2019", "seller"));
        reviews.add(new Review("r2", michael, "Good buyer, smooth transaction and polite!", 4.5, "Aug 12, 2023", "user"));
        reviews.add(new Review("r3", jonathan, "Great seller. Item quality is exactly like described.", 4.0, "Jan 3, 2024", "seller"));
        reviews.add(new Review("r4", alicia, "Good communication and on-time meetup.", 4.5, "May 15, 2023", "user"));

        return reviews;
    }
}
