package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Location implements Serializable, Parcelable {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("longitude")
    @Expose
    private double longitude;
    @SerializedName("latitude")
    @Expose
    private double latitude;

    @SerializedName("distance")
    @Expose
    private String distance;

    @SerializedName("locationTypeId")
    @Expose
    private String locationTypeId;

    @SerializedName("locationId")
    @Expose
    private String locationId;

    @SerializedName("mandalId")
    @Expose
    private String mandalId;

    @SerializedName("districtId")
    @Expose
    private String districtId;

    @SerializedName("stateId")
    @Expose
    private String stateId;

    @SerializedName("constituencyId")
    @Expose
    private String constituencyId;

    public final static Creator<Location> CREATOR = new Creator<Location>() {

        @SuppressWarnings({
                "unchecked"
        })
        public Location createFromParcel(Parcel in) {
            Location instance = new Location();
            instance.name = ((String) in.readValue((String.class.getClassLoader())));
            instance.longitude = ((double) in.readValue((double.class.getClassLoader())));
            instance.latitude = ((double) in.readValue((double.class.getClassLoader())));
            instance.distance = ((String) in.readValue((String.class.getClassLoader())));
            instance.locationId = ((String) in.readValue((String.class.getClassLoader())));
            instance.locationTypeId = ((String) in.readValue((String.class.getClassLoader())));
            instance.mandalId = ((String) in.readValue((String.class.getClassLoader())));
            instance.districtId = ((String) in.readValue((String.class.getClassLoader())));
            instance.stateId = ((String) in.readValue((String.class.getClassLoader())));
            instance.constituencyId = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public Location[] newArray(int size) {
            return (new Location[size]);
        }

    };

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(name);
        dest.writeValue(longitude);
        dest.writeValue(latitude);
        dest.writeValue(distance);
        dest.writeValue(locationId);
        dest.writeValue(locationTypeId);
        dest.writeValue(mandalId);
        dest.writeValue(districtId);
        dest.writeValue(stateId);
        dest.writeValue(constituencyId);
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Location{" +
                "name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", distance=" + distance +
                '}';
    }

    public String getLocationTypeId() {
        return locationTypeId;
    }

    public void setLocationTypeId(String locationTypeId) {
        this.locationTypeId = locationTypeId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getMandalId() {
        return mandalId;
    }

    public void setMandalId(String mandalId) {
        this.mandalId = mandalId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getConstituencyId() {
        return constituencyId;
    }

    public void setConstituencyId(String constituencyId) {
        this.constituencyId = constituencyId;
    }
}
