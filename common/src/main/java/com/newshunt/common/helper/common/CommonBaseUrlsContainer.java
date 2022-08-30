/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common;

/**
 * Singleton class holding the required initialization of common urls
 *
 * @author arun.babu
 */
public class CommonBaseUrlsContainer {

  private static CommonBaseUrlsContainer instance;

  private final String analyticsUrl;
  private final String notificationNewsUrl;
  private final String notificationTriggerUrl;
  private final String pullNotificationUrl;
  private final String notificationChannelUrl;

  private CommonBaseUrlsContainer(CommonBaseUrls commonBaseUrls) {
    this.analyticsUrl = commonBaseUrls.analyticsUrl;
    this.notificationNewsUrl = commonBaseUrls.notificationNewsUrl;
    this.notificationTriggerUrl = commonBaseUrls.notificationTriggerUrl;
    this.pullNotificationUrl = commonBaseUrls.pullNotificationUrl;
    this.notificationChannelUrl = commonBaseUrls.notificationChannelUrl;
  }

  public static CommonBaseUrlsContainer getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Common Url's not yet Initialized");
    }
    return instance;
  }

  public static synchronized CommonBaseUrlsContainer createInstance(CommonBaseUrls commonBaseUrls) {
    instance = new CommonBaseUrlsContainer(commonBaseUrls);
    return instance;
  }

  public String getAnalyticsUrl() {
    return analyticsUrl;
  }

  public String getNotificationNewsUrl() {
    return notificationNewsUrl;
  }

  public String getNotificationTriggerUrl() {
    return notificationTriggerUrl;
  }

  public String getPullNotificationUrl() {
    return pullNotificationUrl;
  }

  public String getNotificationChannelUrl() {
    return notificationChannelUrl;
  }
}
