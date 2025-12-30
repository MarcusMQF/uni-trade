package com.example.unitrade;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface UserCallback {
        void onSuccess(User user);

        void onFailure(Exception e);
    }

    public static void getUserByUid(String uid, UserCallback callback) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null)
                            user.setId(doc.getId());
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getAllProducts(ProductsCallback callback) {
        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            products.add(product);
                        }
                    }
                    callback.onSuccess(products);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface ProductsCallback {
        void onSuccess(List<Product> products);

        void onFailure(Exception e);
    }

    public interface UpdateCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    public static void updateUser(User user, UpdateCallback callback) {
        db.collection("users")
                .document(user.getId())
                .update(
                        "fullName", user.getFullName(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "phoneNumber", user.getPhoneNumber(),
                        "bio", user.getBio(),
                        "profileImageUrl", user.getProfileImageUrl(),
                        "profileImageVersion", user.getProfileImageVersion(),
                        "address", user.getAddress(),
                        "lastEdited", user.getLastEdited())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

}
