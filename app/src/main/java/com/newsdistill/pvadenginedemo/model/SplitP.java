package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by NDAND-VIJAY on 27-02-2017.
 */
public class SplitP implements Parcelable {
    private String elementType;
    private String data;
    private String imageUrl;

    public SplitP() {
    }

    public SplitP(String elementType, String data, String imageUrl) {
        this.elementType = elementType;
        this.data = data;
        this.imageUrl = imageUrl;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public SplitP(Parcel source1) {
        elementType = source1.readString();
        data = source1.readString();
        imageUrl = source1.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(elementType);
        dest.writeString(data);
        dest.writeString(imageUrl);
    }

    public static final Parcelable.Creator<SplitP> CREATOR = new Parcelable.Creator<SplitP>() {

        @Override
        public SplitP createFromParcel(Parcel source) {
            return new SplitP(source);
        }

        @Override
        public SplitP[] newArray(int size) {
            return new SplitP[size];
        }
    };

}