/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.helper.share;

/**
 * Provide default apps using which we can share story.
 *
 * @author shreyas.desai
 */
public enum ShareApplication {
  WHATS_APP_PACKAGE("com.whatsapp"),
  FACEBOOK_APP_PACKAGE("com.facebook.katana"),
  TWITTER_APP_PACKAGE("com.twitter.android"),
  GMAIL_APP_PACKAGE("com.google.android.gm"),

  SMS_PACKAGE("com.android.mmsAppdetails");


  private String packageName;

  ShareApplication(String packageName) {
    this.packageName = packageName;
  }

  public static ShareApplication fromName(String packageName) {
    for (ShareApplication shareApplication : ShareApplication.values()) {
      if (shareApplication.packageName.equalsIgnoreCase(packageName)) {
        return shareApplication;
      }
    }

    return null;
  }

  public String getPackageName() {
    return packageName;
  }
}
