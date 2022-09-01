package com.newsdistill.pvadenginedemo.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NDAND-VIJAY on 27-02-2017.
 */
public class DescriptionElementsP implements Parcelable {

    private List<SplitP> splits = new ArrayList<>();

    public DescriptionElementsP() {
    }

    public DescriptionElementsP(List<SplitP> splits) {
        this.splits = splits;
    }

    public List<SplitP> getSplits() {
        return splits;
    }

    public void setSplits(List<SplitP> splits) {
        this.splits = splits;
    }

    public void addSplit(SplitP split) {
        this.splits.add(split);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public DescriptionElementsP(Parcel source) {
        Bundle b = source.readBundle(DescriptionElementsP.class.getClassLoader());
        splits = b.getParcelableArrayList("splits");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle b = new Bundle();
        b.putParcelableArrayList("splits", (ArrayList<? extends Parcelable>) splits);
        dest.writeBundle(b);
    }

    public static final Parcelable.Creator<DescriptionElementsP> CREATOR = new Parcelable.Creator<DescriptionElementsP>() {

        @Override
        public DescriptionElementsP createFromParcel(Parcel source) {
            return new DescriptionElementsP(source);
        }

        @Override
        public DescriptionElementsP[] newArray(int size) {
            return new DescriptionElementsP[size];
        }
    };
}