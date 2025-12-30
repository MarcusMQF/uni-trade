package com.example.unitrade;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class User implements Parcelable {

    private String id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private double sellerRating;
    private double userRating;
    private double overallRating;
    private long lastSeen;
    private String bio;
    private long lastEdited;
    private List<Address> addresses = new ArrayList<>();
    private long profileImageVersion = 0;

    // REQUIRED by Firestore
    public User() {
        // empty constructor
    }


    // Main constructor
    public User(String id, String username, String fullName, String email, String phoneNumber,
                String profileImageUrl, double sellerRating, double userRating, long lastSeen,
                String bio, long lastEdited, String address) { // Updated
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.sellerRating = sellerRating;
        this.userRating = userRating;
        this.lastSeen = lastSeen;
        this.bio = bio;
        this.lastEdited = lastEdited;
        this.addresses = addresses != null ? addresses : new ArrayList<>();
        this.profileImageVersion = System.currentTimeMillis();
        updateOverallRating();
    }

    // Simple constructor
    public User(String id, String username, String profileImageUrl, double sellerRating,
                double userRating, long lastSeen, String bio, String address, int ignored) { // Updated to match SampleData calls if needed, or I will fix SampleData
        this(id, username, "", "", "", profileImageUrl, sellerRating, userRating, lastSeen, bio, 0, address);
    }

     // Simple constructor for SampleData
     public User(String id, String username, String profileImageUrl, double sellerRating,
                 double userRating, long lastSeen, String bio) {
         this(id, username, "", "", "", profileImageUrl, sellerRating, userRating, lastSeen, bio, 0, "");
     }

    // Add this field variable near the top with other variables
    private int defaultAddressIndex = 0; // Default to 0 (the first address)

    // Add this getter method
    public int getDefaultAddressIndex() {
        return defaultAddressIndex;
    }

    // Add this setter method (optional, but likely needed later)
    public void setDefaultAddressIndex(int defaultAddressIndex) {
        this.defaultAddressIndex = defaultAddressIndex;
    }


    // Inside User.java

    public void setOverallRating(double overallRating) {
        this.overallRating = overallRating;
    }


    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }

    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    @PropertyName("profileImageURL")
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public double getSellerRating() { return sellerRating; }
    public double getUserRating() { return userRating; }
    public double getOverallRating() { return overallRating; }
    public long getLastSeen() { return lastSeen; }
    public String getBio() { return bio; }
    public long getLastEdited() { return lastEdited; }
    public List<Address> getAddresses() { return addresses; }

    public long getProfileImageVersion() {
        return profileImageVersion;
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    @PropertyName("profileImageURL")
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    public void setSellerRating(double sellerRating) { this.sellerRating = sellerRating; updateOverallRating(); }
    public void setUserRating(double userRating) { this.userRating = userRating; updateOverallRating(); }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
    public void setBio(String bio) { this.bio = bio; }
    public void setLastEdited(long lastEdited) { this.lastEdited = lastEdited; }
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public void setProfileImageVersion(long version) {
        this.profileImageVersion = version;
    }

    private void updateOverallRating() {
        this.overallRating = (sellerRating + userRating) / 2.0;
    }

    // Parcelable implementation
    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        fullName = in.readString();
        email = in.readString();
        phoneNumber = in.readString();
        profileImageUrl = in.readString();
        sellerRating = in.readDouble();
        userRating = in.readDouble();
        overallRating = in.readDouble();
        lastSeen = in.readLong();
        bio = in.readString();
        lastEdited = in.readLong();
        addresses = new ArrayList<>();
        in.readList(addresses, Address.class.getClassLoader());
        profileImageVersion = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(phoneNumber);
        dest.writeString(profileImageUrl);
        dest.writeDouble(sellerRating);
        dest.writeDouble(userRating);
        dest.writeDouble(overallRating);
        dest.writeLong(lastSeen);
        dest.writeString(bio);
        dest.writeLong(lastEdited);
        dest.writeList(addresses);
        dest.writeLong(profileImageVersion);
    }


    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) { return new User(in); }
        @Override
        public User[] newArray(int size) { return new User[size]; }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    public String getLastSeenString() {
        if (lastSeen <= 0) return "Never";
        long now = System.currentTimeMillis();
        long diff = now - lastSeen;
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);

        Calendar lastSeenCal = Calendar.getInstance();
        lastSeenCal.setTimeInMillis(lastSeen);
        Calendar nowCal = Calendar.getInstance();

        if (nowCal.get(Calendar.YEAR) == lastSeenCal.get(Calendar.YEAR) && nowCal.get(Calendar.DAY_OF_YEAR) == lastSeenCal.get(Calendar.DAY_OF_YEAR)) {
            if (minutes < 1) return "Just now";
            if (minutes < 60) return minutes + " minutes ago";
            return hours + " hours ago";
        } else if (nowCal.get(Calendar.YEAR) == lastSeenCal.get(Calendar.YEAR) && nowCal.get(Calendar.DAY_OF_YEAR) - 1 == lastSeenCal.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat(" 'at' hh:mm a", Locale.getDefault());
            return "Yesterday" + timeFormat.format(new Date(lastSeen));
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return dateFormat.format(new Date(lastSeen));
        }
    }

    // NEW: Human-readable last edited
    public String getLastEditedString() {
        if (lastEdited <= 0) return "Never";
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        return dateFormat.format(new Date(lastEdited));
    }
}
