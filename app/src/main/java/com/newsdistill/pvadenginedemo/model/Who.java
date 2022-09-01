package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Who implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;
    @SerializedName("anonymous")
    @Expose
    private boolean anonymous;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("role")
    @Expose
    private String role;
    @SerializedName("verified")
    @Expose
    private boolean verified;
    @SerializedName("admin")
    @Expose
    private boolean admin;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("roleId")
    @Expose
    private String roleId;
    @SerializedName("redirection")
    @Expose
    private boolean redirection;
    @SerializedName("functionId")
    @Expose
    private String functionId;

    @SerializedName("functionName")
    @Expose
    private String functionName;
    @SerializedName("locationId")
    @Expose
    private String locationId;
    @SerializedName("locationTypeId")
    @Expose
    private String locationTypeId;
    @SerializedName("location")
    @Expose
    private String location;


    public Who() {

    }


    protected Who(Parcel in) {
        id = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        anonymous = in.readByte() != 0;
        status = in.readString();
        role = in.readString();
        verified = in.readByte() != 0;
        admin = in.readByte() != 0;
        type = in.readString();
        roleId = in.readString();
        redirection = in.readByte() != 0;
        functionId = in.readString();
        functionName = in.readString();
        locationId = in.readString();
        locationTypeId = in.readString();
        location = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (anonymous ? 1 : 0));
        dest.writeString(status);
        dest.writeString(role);
        dest.writeByte((byte) (verified ? 1 : 0));
        dest.writeByte((byte) (admin ? 1 : 0));
        dest.writeString(type);
        dest.writeString(roleId);
        dest.writeByte((byte) (redirection ? 1 : 0));
        dest.writeString(functionId);
        dest.writeString(functionName);
        dest.writeString(locationId);
        dest.writeString(locationTypeId);
        dest.writeString(location);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Who> CREATOR = new Creator<Who>() {
        @Override
        public Who createFromParcel(Parcel in) {
            return new Who(in);
        }

        @Override
        public Who[] newArray(int size) {
            return new Who[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public boolean isRedirection() {
        return redirection;
    }

    public void setRedirection(boolean redirection) {
        this.redirection = redirection;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationTypeId() {
        return locationTypeId;
    }

    public void setLocationTypeId(String locationTypeId) {
        this.locationTypeId = locationTypeId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Who{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", anonymous=" + anonymous +
                ", status='" + status + '\'' +
                ", role='" + role + '\'' +
                ", verified=" + verified +
                ", admin=" + admin +
                ", type='" + type + '\'' +
                ", roleId='" + roleId + '\'' +
                ", redirection=" + redirection +
                ", functionId='" + functionId + '\'' +
                ", functionName='" + functionName + '\'' +
                ", locationId='" + locationId + '\'' +
                ", locationTypeId='" + locationTypeId + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}