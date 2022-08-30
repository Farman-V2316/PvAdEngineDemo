/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.viral.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @author ketki.garg on 01/12/17.
 */

public class ShareConfig implements Parcelable {
  private String shareText;
  private String shareBannerUrl;

  public ShareConfig() {

  }

  private ShareConfig(Parcel in) {
    shareText = in.readString();
    shareBannerUrl = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(shareText);
    dest.writeString(shareBannerUrl);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ShareConfig> CREATOR = new Creator<ShareConfig>() {
    @Override
    public ShareConfig createFromParcel(Parcel in) {
      return new ShareConfig(in);
    }

    @Override
    public ShareConfig[] newArray(int size) {
      return new ShareConfig[size];
    }
  };

  public String getShareText() {
    return shareText;
  }

  public void setShareText(String shareText) {
    this.shareText = shareText;
  }

  public String getShareBannerUrl() {
    return shareBannerUrl;
  }

  public void setShareBannerUrl(String shareBannerUrl) {
    this.shareBannerUrl = shareBannerUrl;
  }
}
