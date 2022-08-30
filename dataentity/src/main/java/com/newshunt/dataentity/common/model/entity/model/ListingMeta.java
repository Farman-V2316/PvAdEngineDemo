package com.newshunt.dataentity.common.model.entity.model;
/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

public class ListingMeta {
  public final String bannerImageUrl;
  public final String backgroundColor;
  public final String appIndexDescription;
  public final String deepLinkUrl;
  public final String bannerTitleTextColor;
  public final String bannerTitle;
  public final String activeTextColor;
  public final BannerSize bannerSize;
  public final String inactiveTextColor;

  public ListingMeta(String bannerImageUrl, String backgroundColor,
                     String appIndexDescription, String deepLinkUrl,
                     String bannerTitleTextColor, String bannerTitle,
                     String activeTextColor, BannerSize bannerSize,
                     String inactiveTextColor) {
    this.bannerImageUrl = bannerImageUrl;
    this.backgroundColor = backgroundColor;
    this.appIndexDescription = appIndexDescription;
    this.deepLinkUrl = deepLinkUrl;
    this.bannerTitleTextColor = bannerTitleTextColor;
    this.bannerTitle = bannerTitle;
    this.activeTextColor = activeTextColor;
    this.bannerSize = bannerSize;
    this.inactiveTextColor = inactiveTextColor;
  }

  public enum BannerSize {
    FIXED, SCROLLABLE
  }

  public static ListingMeta withBannerAndTitle(String banner, String title) {
    return new ListingMeta(banner, "#000000", "article listing", "newshunt", "#ffffff",
        "Similar stories ",
        "#ffffff", BannerSize.SCROLLABLE, "#dfdfdf");
  }
}