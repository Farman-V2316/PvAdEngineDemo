/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

/**
 * Common base urls
 *
 * @author arun.babu
 */
public class CommonBaseUrls {

  final String analyticsUrl;
  final String notificationNewsUrl;
  final String notificationTriggerUrl;
  final String pullNotificationUrl;
  final String notificationChannelUrl;

  public CommonBaseUrls(String analyticsUrl, String notificationNewsUrl,
                        String notificationTriggerUrl, String pullNotificationUrl,
                        String notificationChannelUrl) {
    this.analyticsUrl = analyticsUrl;
    this.notificationNewsUrl = notificationNewsUrl;
    this.notificationTriggerUrl = notificationTriggerUrl;
    this.pullNotificationUrl = pullNotificationUrl;
    this.notificationChannelUrl = notificationChannelUrl;
  }
}
