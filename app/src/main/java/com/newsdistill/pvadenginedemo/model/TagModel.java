package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TagModel implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("labelName")
    @Expose
    private String labelName;

    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;

    protected TagModel(Parcel in) {
        id = in.readString();
        labelName = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<TagModel> CREATOR = new Creator<TagModel>() {
        @Override
        public TagModel createFromParcel(Parcel in) {
            return new TagModel(in);
        }

        @Override
        public TagModel[] newArray(int size) {
            return new TagModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(labelName);
        dest.writeString(imageUrl);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "TagModel{" +
                "id='" + id + '\'' +
                ", labelName='" + labelName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}