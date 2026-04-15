/*
 * ============================================================================
 * FILE: User.java
 * PURPOSE: Data model representing a user in the UniTrade application
 * LOCATION: app/src/main/java/com/example/unitrade/User.java
 * 
 * DESCRIPTION:
 * The User class is a core data model that represents a user profile in the
 * UniTrade marketplace. Users can be both buyers and sellers, each with their
 * own ratings and statistics. The class implements Parcelable to allow passing
 * user data between Android Activities. It also integrates with Firebase
 * Firestore for cloud storage and synchronization.
 * 
 * KEY FEATURES:
 * - User authentication (ID, username, email)
 * - Profile information (full name, bio, phone)
 * - Profile picture with cache busting
 * - Seller and buyer ratings (separate tracking)
 * - Multiple delivery addresses management
 * - Last seen timestamp tracking
 * - Profile edit history
 * - Parcelable for inter-activity data passing
 * 
 * IMPORTS EXPLANATION:
 * android.os.Parcel - Used for marshalling objects into Parcelable form
 * android.os.Parcelable - Interface for passing objects between Activities
 * firebase.firestore.PropertyName - Maps Java field names to Firestore field names
 * firebase.firestore.Exclude - Marks fields to be excluded from Firestore serialization
 * ============================================================================
 */

package com.example.unitrade;

// Import for Parcel (Android serialization mechanism)
import android.os.Parcel;
// Import for Parcelable interface (allows objects to be passed between Activities)
import android.os.Parcelable;

// Firebase imports for database integration
import com.google.firebase.firestore.PropertyName;     // Maps Java fields to Firestore fields
import com.google.firebase.firestore.Exclude;         // Excludes fields from Firestore storage

// Java utility imports
import java.text.SimpleDateFormat;                     // For formatting dates/times
import java.util.ArrayList;                            // For dynamic list storage
import java.util.Calendar;                             // For date/time calculations
import java.util.Date;                                 // For date objects
import java.util.List;                                 // List interface
import java.util.Locale;                               // For locale-specific formatting

/**
 * ============================================================================
 * Class Declaration: User
 * ============================================================================
 * 
 * Implements Parcelable - This allows User objects to be efficiently passed
 * between Android Activities via Intent extras. Parcelable is more efficient
 * than Serializable for Android.
 * 
 * The User object is automatically serialized/deserialized by Firebase when
 * saving to or reading from Firestore database.
 */
public class User implements Parcelable {

    // ========================================================================
    // SECTION 1: AUTHENTICATION & IDENTIFICATION FIELDS
    // ========================================================================
    
    // Unique identifier for the user (typically Firebase authentication UID)
    // This is immutable and never changes for a user account
    private String id;
    
    // The username the user chose (displayed publicly, may be changed)
    // Used for logging in and user recognition
    private String username;
    
    // Full legal name of the user (not necessarily displayed publicly)
    private String fullName;
    
    // Email address associated with the account
    // Used for account recovery and notifications
    private String email;
    
    // Phone number for the user account
    // May be used for SMS notifications and verification
    private String phoneNumber;

    // ========================================================================
    // SECTION 2: PROFILE & IMAGE DATA
    // ========================================================================
    
    // URL to the user's profile picture stored in Firebase Cloud Storage
    // This is a full URL that can be directly loaded into ImageView
    private String profileImageUrl;
    
    // Version/timestamp of profile image (for cache busting)
    // Incremented each time image is updated, forces image reload
    private long profileImageVersion = 0;
    
    // User's bio/description (public text shown on their profile)
    private String bio;

    // ========================================================================
    // SECTION 3: RATING FIELDS (Separate tracking for buyers and sellers)
    // ========================================================================
    
    // Average rating as a SELLER (0.0 to 5.0)
    // Only counted when user sells items
    private double sellerRating;
    
    // Average rating as a BUYER/USER (0.0 to 5.0)
    // Only counted when user buys items
    private double userRating;
    
    // Number of reviews received for selling
    // Used to calculate average seller rating
    private int sellerRatingCount;
    
    // Number of reviews received for buying
    // Used to calculate average buyer rating
    private int userRatingCount;
    
    // Combined overall rating across both buying and selling
    // Calculated from both sellerRating and userRating
    private double overallRating;
    
    // Total number of ratings (seller + buyer)
    private int overallRatingCount;

    // ========================================================================
    // SECTION 4: TIMESTAMP FIELDS
    // ========================================================================
    
    // Timestamp (in milliseconds) of when user was last active
    // Used to show "Last seen: X minutes ago" on profile
    private long lastSeen;
    
    // Timestamp (in milliseconds) of when profile was last edited
    // Helps track profile update history
    private long lastEdited;

    // ========================================================================
    // SECTION 5: ADDRESS & DELIVERY DATA
    // ========================================================================
    
    // List of delivery addresses for this user
    // Multiple addresses allow users to have home, office, etc.
    // Marked with @Exclude to prevent Firestore storage (stored separately)
    private List<Address> addresses = new ArrayList<>();
    
    // Index of the default address in the addresses list
    // Used when checking out if user doesn't explicitly select an address
    private int defaultAddressIndex = 0;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================
    
    /**
     * REQUIRED EMPTY CONSTRUCTOR by Firestore
     * 
     * Firebase requires all model classes to have a no-argument constructor
     * so it can instantiate the class during deserialization from the database.
     * This constructor initializes nothing - all fields get default values.
     */
    public User() {
        // Empty constructor - required by Firestore
    }

    /**
     * FULL CONSTRUCTOR - All parameters provided
     * 
     * Used when creating a user with all information known (typically at signup)
     * 
     * @param id                   Unique user ID (from Firebase Auth)
     * @param username            Username for login/display
     * @param fullName            Full legal name
     * @param email               Email address
     * @param phoneNumber         Phone number
     * @param profileImageUrl     URL to profile picture
     * @param sellerRating        Average seller rating (0-5)
     * @param userRating          Average buyer rating (0-5)
     * @param lastSeen            Timestamp of last activity
     * @param bio                 User biography
     * @param lastEdited          Timestamp of last profile edit
     * @param address             First address (for backward compatibility)
     */
    public User(String id, String username, String fullName, String email, String phoneNumber,
            String profileImageUrl, double sellerRating, double userRating, long lastSeen,
            String bio, long lastEdited, String address) {
        
        // Store all parameters directly
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
        
        // Initialize addresses as empty list, add address parameter
        this.addresses = addresses != null ? addresses : new ArrayList<>();
        
        // Set profile image version to current time (for cache busting)
        this.profileImageVersion = System.currentTimeMillis();
        
        // Calculate overall rating from seller and user ratings
        updateOverallRating();
    }

    /**
     * SIMPLIFIED CONSTRUCTOR - For quick user creation
     * 
     * Used when only basic profile information is available
     * Extra parameter at end is ignored (for backward compatibility)
     * 
     * @param id                   Unique user ID
     * @param username            Username
     * @param profileImageUrl     Profile picture URL
     * @param sellerRating        Seller rating
     * @param userRating          Buyer rating
     * @param lastSeen            Last activity timestamp
     * @param bio                 Biography text
     * @param address             Delivery address
     * @param ignored             Ignored parameter (for compatibility)
     */
    public User(String id, String username, String profileImageUrl, double sellerRating,
            double userRating, long lastSeen, String bio, String address, int ignored) {
        
        // Call full constructor with empty values for missing parameters
        this(id, username, "", "", "", profileImageUrl, sellerRating, 
             userRating, lastSeen, bio, 0, address);
    }

    /**
     * SIMPLIFIED CONSTRUCTOR 2 - Minimal user creation
     * 
     * Used for quick user instantiation with just ID and name information
     * 
     * @param id                   Unique user ID
     * @param username            Username
     * @param profileImageUrl     Profile picture URL
     * @param sellerRating        Seller rating
     * @param userRating          Buyer rating
     * @param lastSeen            Last activity timestamp
     * @param bio                 Biography text
     */
    public User(String id, String username, String profileImageUrl, double sellerRating,
            double userRating, long lastSeen, String bio) {
        
        // Call full constructor with empty values
        this(id, username, "", "", "", profileImageUrl, sellerRating, 
             userRating, lastSeen, bio, 0, "");
    }

    // ========================================================================
    // GETTERS - Authentication & Identification
    // ========================================================================
    
    /**
     * Get user's unique identifier
     * @return User's firestore/auth ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get user's login username
     * @return Username for login
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get user's full legal name
     * @return Full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Get user's email address
     * @return Email associated with account
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get user's phone number
     * @return Phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // ========================================================================
    // GETTERS - Profile & Image
    // ========================================================================
    
    /**
     * Get URL to user's profile picture
     * @return Firebase Cloud Storage URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Get profile image version (for cache busting)
     * @return Version timestamp
     */
    public long getProfileImageVersion() {
        return profileImageVersion;
    }

    /**
     * Get user's biography
     * @return Bio text
     */
    public String getBio() {
        return bio;
    }

    // ========================================================================
    // GETTERS - Ratings & Statistics
    // ========================================================================
    
    /**
     * Get user's average seller rating (0-5 stars)
     * @return Average rating when user sold items
     */
    public double getSellerRating() {
        return sellerRating;
    }

    /**
     * Get user's average buyer rating (0-5 stars)
     * @return Average rating when user bought items
     */
    public double getUserRating() {
        return userRating;
    }

    /**
     * Get overall average rating across all transactions
     * @return Combined average rating
     */
    public double getOverallRating() {
        return overallRating;
    }

    /**
     * Get count of seller ratings received
     * @return Number of seller reviews
     */
    public int getSellerRatingCount() {
        return sellerRatingCount;
    }

    /**
     * Get count of buyer ratings received
     * @return Number of buyer reviews
     */
    public int getUserRatingCount() {
        return userRatingCount;
    }

    /**
     * Get total count of all ratings (seller + buyer)
     * @return Total number of reviews
     */
    public int getOverallRatingCount() {
        return userRatingCount + sellerRatingCount;
    }

    // ========================================================================
    // GETTERS - Address & Delivery
    // ========================================================================
    
    /**
     * Get list of all user's delivery addresses
     * 
     * @Exclude annotation prevents this from being stored in Firestore
     * (addresses are stored in a separate sub-collection)
     * 
     * @return List of Address objects
     */
    @Exclude
    public List<Address> getAddresses() {
        return addresses;
    }

    /**
     * Get index of default delivery address
     * @return Index in addresses list (default 0)
     */
    public int getDefaultAddressIndex() {
        return defaultAddressIndex;
    }

    /**
     * Get first address as a string (backward compatibility)
     * @return Address string or empty string
     */
    public String getAddress() {
        // If addresses list exists and has at least one item
        if (addresses != null && !addresses.isEmpty()) {
            // Get the first address and convert to string
            return addresses.get(0).getAddress();
        }
        // Return empty string if no addresses
        return "";
    }

    // ========================================================================
    // GETTERS - Timestamps
    // ========================================================================
    
    /**
     * Get when user was last seen/active
     * @return Timestamp in milliseconds
     */
    public long getLastSeen() {
        return lastSeen;
    }

    /**
     * Get when profile was last edited
     * @return Timestamp in milliseconds
     */
    public long getLastEdited() {
        return lastEdited;
    }

    // ========================================================================
    // SETTERS - Authentication & Identification
    // ========================================================================
    
    /**
     * Set user's unique identifier
     * @param id User's firestore/auth ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set user's username
     * @param username New username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Set user's full name
     * @param fullName New full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Set user's email
     * @param email New email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Set user's phone number
     * @param phoneNumber New phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ========================================================================
    // SETTERS - Profile & Image
    // ========================================================================
    
    /**
     * Set user's profile image URL
     * @param profileImageUrl New image URL
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Set profile image version (cache busting)
     * @param version New version timestamp
     */
    public void setProfileImageVersion(long version) {
        this.profileImageVersion = version;
    }

    /**
     * Set user's biography
     * @param bio New bio text
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    // ========================================================================
    // SETTERS - Ratings & Statistics
    // ========================================================================
    
    /**
     * Set seller rating and recalculate overall rating
     * @param sellerRating New seller rating (0-5)
     */
    public void setSellerRating(double sellerRating) {
        this.sellerRating = sellerRating;
        // Recalculate overall rating after change
        updateOverallRating();
    }

    /**
     * Set buyer rating and recalculate overall rating
     * @param userRating New buyer rating (0-5)
     */
    public void setUserRating(double userRating) {
        this.userRating = userRating;
        // Recalculate overall rating after change
        updateOverallRating();
    }

    /**
     * Set count of seller ratings
     * @param sellerRatingCount Number of seller reviews
     */
    public void setSellerRatingCount(int sellerRatingCount) {
        this.sellerRatingCount = sellerRatingCount;
    }

    /**
     * Set count of buyer ratings
     * @param userRatingCount Number of buyer reviews
     */
    public void setUserRatingCount(int userRatingCount) {
        this.userRatingCount = userRatingCount;
    }

    /**
     * Set overall rating (direct assignment without calculation)
     * @param overallRating Overall average rating
     */
    public void setOverallRating(double overallRating) {
        this.overallRating = overallRating;
    }

    /**
     * Set total rating count
     * @param overallRatingCount Total number of ratings
     */
    public void setOverallRatingCount(int overallRatingCount) {
        this.overallRatingCount = overallRatingCount;
    }

    // ========================================================================
    // SETTERS - Address & Delivery
    // ========================================================================
    
    /**
     * Set list of delivery addresses
     * 
     * @Exclude prevents storage in Firestore (stored separately)
     * 
     * @param addresses New list of addresses
     */
    @Exclude
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    /**
     * Set addresses from a single string (for backward compatibility)
     * @param address Address as string
     */
    public void setAddress(String address) {
        // Clear existing addresses
        this.addresses = new ArrayList<>();
        
        // If address string is not null and not empty
        if (address != null && !address.isEmpty()) {
            // Create Address object and mark as default
            this.addresses.add(new Address(address, true));
        }
    }

    /**
     * Set default address index
     * @param defaultAddressIndex Index in addresses list
     */
    public void setDefaultAddressIndex(int defaultAddressIndex) {
        this.defaultAddressIndex = defaultAddressIndex;
    }

    // ========================================================================
    // SETTERS - Timestamps
    // ========================================================================
    
    /**
     * Set when user was last active
     * @param lastSeen New timestamp in milliseconds
     */
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * Set when profile was last edited
     * @param lastEdited New timestamp in milliseconds
     */
    public void setLastEdited(long lastEdited) {
        this.lastEdited = lastEdited;
    }

    // ========================================================================
    // HELPER METHODS - Rating Calculation
    // ========================================================================
    
    /**
     * Calculate overall rating from seller and buyer ratings
     * 
     * This method is called whenever seller or buyer rating changes.
     * Overall rating is weighted average:
     * 
     * Overall = (sellerRating × sellerCount + userRating × userCount) / total
     * 
     * Example:
     * - 3 seller reviews averaging 4.5 stars = 13.5 stars
     * - 2 buyer reviews averaging 5.0 stars = 10.0 stars
     * - Total = (13.5 + 10.0) / 5 = 4.7 stars overall
     */
    private void updateOverallRating() {
        // Calculate total number of reviews
        int totalCount = userRatingCount + sellerRatingCount;

        // If no ratings exist, set overall to 0
        if (totalCount == 0) {
            overallRating = 0.0;
            return;
        }

        // Weight ratings by their count and sum
        double totalStars = (userRating * userRatingCount) + (sellerRating * sellerRatingCount);

        // Divide by total to get weighted average
        overallRating = totalStars / totalCount;
    }

    // ========================================================================
    // PARCELABLE IMPLEMENTATION - For passing between Activities
    // ========================================================================
    
    /**
     * Constructor for deserializing from Parcel
     * 
     * When passing a User between Activities, Android serializes all fields
     * into a Parcel (binary format). This constructor reconstructs the object
     * from the Parcel data.
     * 
     * @param in Parcel containing serialized User data
     */
    protected User(Parcel in) {
        // Read strings in same order as writeToParcel
        id = in.readString();                    // Read user ID
        username = in.readString();              // Read username
        fullName = in.readString();              // Read full name
        email = in.readString();                 // Read email
        phoneNumber = in.readString();           // Read phone
        profileImageUrl = in.readString();       // Read image URL
        
        // Read doubles for ratings
        sellerRating = in.readDouble();          // Read seller rating
        userRating = in.readDouble();            // Read buyer rating
        overallRating = in.readDouble();         // Read overall rating
        
        // Read long values for timestamps
        lastSeen = in.readLong();                // Read last seen time
        
        // Read more strings
        bio = in.readString();                   // Read biography
        
        // Read last edited timestamp
        lastEdited = in.readLong();              // Read last edit time
        
        // Initialize addresses list and read from parcel
        addresses = new ArrayList<>();           // Create empty list
        in.readList(addresses, Address.class.getClassLoader()); // Populate with Address objects
        
        // Read image version
        profileImageVersion = in.readLong();    // Read cache bust version
    }

    /**
     * Write this User object to a Parcel (for passing between Activities)
     * 
     * This serializes all User fields into binary format that can be
     * efficiently passed via Intent.putExtra().
     * 
     * IMPORTANT: Read and write operations must use same order!
     * 
     * @param dest Parcel to write data to
     * @param flags Parcelable flags (usually 0)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write strings in consistent order (matches constructor above)
        dest.writeString(id);                    // Write user ID
        dest.writeString(username);              // Write username
        dest.writeString(fullName);              // Write full name
        dest.writeString(email);                 // Write email
        dest.writeString(phoneNumber);           // Write phone number
        dest.writeString(profileImageUrl);       // Write profile image URL
        
        // Write double values for ratings
        dest.writeDouble(sellerRating);          // Write seller rating
        dest.writeDouble(userRating);            // Write buyer rating
        dest.writeDouble(overallRating);         // Write overall rating
        
        // Write long values for timestamps
        dest.writeLong(lastSeen);                // Write last activity time
        
        // Write bio string
        dest.writeString(bio);                   // Write biography
        
        // Write last edited timestamp
        dest.writeLong(lastEdited);              // Write last edit time
        
        // Write addresses list
        dest.writeList(addresses);               // Write all Address objects
        
        // Write image version
        dest.writeLong(profileImageVersion);    // Write cache bust version
    }

    /**
     * Required by Parcelable interface
     * Returns 0 for most cases (no special flags needed)
     */
    @Override
    public int describeContents() {
        return 0;  // No special parcelable objects contained
    }

    /**
     * REQUIRED CREATOR for Parcelable
     * 
     * This static object is used by Android to create User instances
     * from Parcel data during deserialization.
     */
    public static final Creator<User> CREATOR = new Creator<User>() {
        
        /**
         * Create User from Parcel buffer
         * @param in Parcel containing User data
         * @return New User instance reconstructed from Parcel
         */
        @Override
        public User createFromParcel(Parcel in) {
            // Call the protected constructor that takes a Parcel
            return new User(in);
        }

        /**
         * Create array of Users for deserialization
         * @param size Size of array to create
         * @return New User array
         */
        @Override
        public User[] newArray(int size) {
            // Initialize empty array of specified size
            return new User[size];
        }
    };

    // ========================================================================
    // HUMAN-READABLE TIME FORMATTING METHODS
    // ========================================================================
    
    /**
     * Get human-readable "last seen" time
     * 
     * Examples:
     * - "Just now" - within last minute
     * - "5 minutes ago" - less than an hour ago
     * - "3 hours ago" - earlier today
     * - "Yesterday at 3:45 PM" - last day
     * - "Mar 15, 2024" - older than yesterday
     * 
     * @return Formatted string describing when user was last active
     */
    public String getLastSeenString() {
        // If no last seen timestamp, return "Never"
        if (lastSeen <= 0)
            return "Never";
        
        // Calculate current time
        long now = System.currentTimeMillis();
        
        // Calculate difference in milliseconds
        long diff = now - lastSeen;
        
        // Convert to minutes and hours
        long minutes = diff / (60 * 1000);           // milliseconds to minutes
        long hours = diff / (60 * 60 * 1000);        // milliseconds to hours

        // Get calendar objects for comparison
        Calendar lastSeenCal = Calendar.getInstance();
        lastSeenCal.setTimeInMillis(lastSeen);      // Set to last seen time
        Calendar nowCal = Calendar.getInstance();    // Set to now

        // Check if last seen was today (same year and day of year)
        if (nowCal.get(Calendar.YEAR) == lastSeenCal.get(Calendar.YEAR)
                && nowCal.get(Calendar.DAY_OF_YEAR) == lastSeenCal.get(Calendar.DAY_OF_YEAR)) {
            
            // If within last minute
            if (minutes < 1)
                return "Just now";
            
            // If within last hour
            if (minutes < 60)
                return minutes + " minutes ago";
            
            // If earlier today
            return hours + " hours ago";
        } 
        // Check if last seen was yesterday
        else if (nowCal.get(Calendar.YEAR) == lastSeenCal.get(Calendar.YEAR)
                && nowCal.get(Calendar.DAY_OF_YEAR) - 1 == lastSeenCal.get(Calendar.DAY_OF_YEAR)) {
            
            // Format time as "Yesterday at HH:MM AM/PM"
            SimpleDateFormat timeFormat = new SimpleDateFormat(" 'at' hh:mm a", Locale.getDefault());
            return "Yesterday" + timeFormat.format(new Date(lastSeen));
        } 
        // Older than yesterday
        else {
            // Format as full date: "Mar 15, 2024"
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return dateFormat.format(new Date(lastSeen));
        }
    }

    /**
     * Get human-readable "last edited" time
     * 
     * Format: "Mar 15, 2024 at 3:45 PM"
     * 
     * @return Formatted string showing when profile was last modified
     */
    public String getLastEditedString() {
        // If no edit timestamp, return "Never"
        if (lastEdited <= 0)
            return "Never";
        
        // Format as "MMM dd, yyyy 'at' hh:mm a" (e.g., "Mar 15, 2024 at 3:45 PM")
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        return dateFormat.format(new Date(lastEdited));
    }

}  // End of User class
