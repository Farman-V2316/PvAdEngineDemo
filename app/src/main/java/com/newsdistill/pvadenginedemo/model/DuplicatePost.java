package com.newsdistill.pvadenginedemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DuplicatePost implements Parcelable {
    @SerializedName("bucketNum")
    @Expose
    private String bucketNum;

    @SerializedName("displayScore")
    @Expose
    private String displayScore;

    @SerializedName("postId")
    @Expose
    private String postId;

    @SerializedName("channelId")
    @Expose
    private String channelId;

    @SerializedName("channelName")
    @Expose
    private String channelName;

    @SerializedName("channelImageUrl")
    @Expose
    private String channelImageUrl;

    protected DuplicatePost(Parcel in) {
        bucketNum = in.readString();
        displayScore = in.readString();
        postId = in.readString();
        channelId = in.readString();
        channelName = in.readString();
        channelImageUrl = in.readString();
    }

    public static final Creator<DuplicatePost> CREATOR = new Creator<DuplicatePost>() {
        @Override
        public DuplicatePost createFromParcel(Parcel in) {
            return new DuplicatePost(in);
        }

        @Override
        public DuplicatePost[] newArray(int size) {
            return new DuplicatePost[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bucketNum);
        dest.writeString(displayScore);
        dest.writeString(postId);
        dest.writeString(channelId);
        dest.writeString(channelName);
        dest.writeString(channelImageUrl);
    }

    public String getBucketNum() {
        return bucketNum;
    }

    public void setBucketNum(String bucketNum) {
        this.bucketNum = bucketNum;
    }

    public String getDisplayScore() {
        return displayScore;
    }

    public void setDisplayScore(String displayScore) {
        this.displayScore = displayScore;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelImageUrl() {
        return channelImageUrl;
    }

    public void setChannelImageUrl(String channelImageUrl) {
        this.channelImageUrl = channelImageUrl;
    }

    @Override
    public String toString() {
        return "DuplicatePost{" +
                "bucketNum='" + bucketNum + '\'' +
                ", displayScore='" + displayScore + '\'' +
                ", postId='" + postId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", channelImageUrl='" + channelImageUrl + '\'' +
                '}';
    }
}
