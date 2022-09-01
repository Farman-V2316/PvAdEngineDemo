package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Reaction implements Parcelable {

    @SerializedName("type")
    @Expose
    private int type;

    @SerializedName("count")
    @Expose
    private int count;

    public Reaction() {
    }

    public Reaction(int type) {
        this.type = type;
    }

    public Reaction(int type, int count) {
        this.type = type;
        this.count = count;
    }

    protected Reaction(Parcel in) {
        type = in.readInt();
        count = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(count);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Reaction> CREATOR = new Creator<Reaction>() {
        @Override
        public Reaction createFromParcel(Parcel in) {
            return new Reaction(in);
        }

        @Override
        public Reaction[] newArray(int size) {
            return new Reaction[size];
        }
    };

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increment() {
        this.setCount(this.getCount()+1);
    }

    public void decrement() {
        this.setCount(this.getCount()-1);
    }

    @Override
    public String toString() {
        return "Reaction{" +
                "type=" + type +
                ", count=" + count +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reaction reaction = (Reaction) o;

        return type == reaction.type;

    }

    @Override
    public int hashCode() {
        return type;
    }
}