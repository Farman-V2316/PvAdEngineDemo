package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OverallRating implements Parcelable {

    @SerializedName("overallRating")
    @Expose
    private double overallRating;

    @SerializedName("count")
    @Expose
    private int count;

    public OverallRating() {
    }


    protected OverallRating(Parcel in) {
        overallRating = in.readDouble();
        count = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(overallRating);
        dest.writeInt(count);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OverallRating> CREATOR = new Creator<OverallRating>() {
        @Override
        public OverallRating createFromParcel(Parcel in) {
            return new OverallRating(in);
        }

        @Override
        public OverallRating[] newArray(int size) {
            return new OverallRating[size];
        }
    };

    public double getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(double overallRating) {
        this.overallRating = overallRating;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "OverallRating{" +
                "overallRating=" + overallRating +
                ", count=" + count +
                '}';
    }
}
