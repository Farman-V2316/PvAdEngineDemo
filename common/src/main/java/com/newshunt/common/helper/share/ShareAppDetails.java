/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.share;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;

import java.io.Serializable;

/**
 * Holds information about app using which item was shared.
 *
 * @author sumedh.tambat
 */
public class ShareAppDetails implements Serializable, Comparable {

  private static final long serialVersionUID = 2L;

  private String appName;
  private String appPackage;
  private Drawable appIcon;
  private long shareScore;

  private ShareAppDetails() {

  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getAppPackage() {
    return appPackage;
  }

  public void setAppPackage(String appPackage) {
    this.appPackage = appPackage;
  }

  public Drawable getAppIcon() {
    return appIcon;
  }

  public void setAppIcon(Drawable appIcon) {
    this.appIcon = appIcon;
  }

  public long getShareScore() {
    return shareScore;
  }

  public void setShareScore(long shareScore) {
    this.shareScore = shareScore;
  }

  public static ShareAppDetails get(String packageName) {
    try {
      PackageManager packageManager =
          CommonUtils.getApplication().getPackageManager();
      final ComponentName componentName = new ComponentName(packageName, Constants.EMPTY_STRING);

      ShareAppDetails shareAppDetail = new ShareAppDetails();
      shareAppDetail.setAppPackage(packageName);

      ApplicationInfo appInfo;
      appInfo = packageManager.getApplicationInfo(componentName.getPackageName(), 0);
      shareAppDetail.setAppName(
          Constants.EMPTY_STRING + packageManager.getApplicationLabel(appInfo));
      shareAppDetail.setAppIcon(
          packageManager.getApplicationIcon(packageName));
      shareAppDetail.setShareScore(AndroidUtils.getLastUseTime(packageName));
      return shareAppDetail;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }


  @Override
  public int compareTo(Object another) {
    if (!(another instanceof ShareAppDetails)) {
      return -1;
    }
    if (getShareScore() - ((ShareAppDetails) another).getShareScore() == 0L) {
      return 0;
    }
    if (getShareScore() - ((ShareAppDetails) another).getShareScore() < 0L) {
      return -1;
    } else {
      return 1;
    }
  }

  // overridden to show package name on Debugger
  @Override
  public String toString() {
    return "package :" + appPackage;
  }
}
