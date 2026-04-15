package com.example.unitrade;
//data model class
import android.os.Parcel;// write/read data for Android IPC.
import android.os.Parcelable;//Imports the Parcelable interface so this object can be passed between activities.
//Address.java is used for profile address list and can be passed if the app sends it via Intent
public class Address implements Parcelable {
    private String address;
    private boolean isDefault;

    // Required by Firestore
    public Address() {
    }

    public Address(String address, boolean isDefault) {
        this.address = address;
        this.isDefault = isDefault;
    }

    protected Address(Parcel in) {
        address = in.readString();
        isDefault = in.readByte() != 0;
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeByte((byte) (isDefault ? 1 : 0));
    }
}
