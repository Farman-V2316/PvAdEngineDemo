/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides utility functions for detecting deeplinking.
 *
 * @author karthik.r
 */
public class DeeplinkHelper {
  private static List<ComponentName> standardComponents;

  /**
   * @param url     Link to detect deeplink
   * @return Empty list, if {@code url} not a deeplink,
   * Singleton list containing Component name of DH, if DH is one of the deeplink
   * handling apps
   * Else, List of component names of all apps that can handle {@code url} as a
   * deeplink.
   */
  public static List<ComponentName> getDeeplinkActivities(String url) {
    DeeplinkActivities deeplinkActivities = getAllDeeplinkActivities(url);
    List<ComponentName> currentUrlComponents = deeplinkActivities.deeplinkComponents;
    if (deeplinkActivities.isInternalDeeplink || CommonUtils.isEmpty(currentUrlComponents)) {
      return currentUrlComponents;
    }
    getInstalledBrowsers();
    // Remove browser apps from list of handling apps
    for (ComponentName resolveInfo : standardComponents) {
      currentUrlComponents.remove(resolveInfo);
    }

    return currentUrlComponents;
  }

  /**
   * Utility function to check if given url can be handled by our app
   *
   * @param url - url to check
   * @return - true if internal deeplink url else false
   */
  public static boolean isInternalDeeplinkUrl(String url) {
    DeeplinkActivities deeplinkActivities = getAllDeeplinkActivities(url);
    return deeplinkActivities != null && deeplinkActivities.isInternalDeeplink;
  }

  /*
   * This function will never return null and will never return null deeplinkcomponents
   */
  public static DeeplinkActivities getAllDeeplinkActivities(String url) {
    if (TextUtils.isEmpty(url)) {
      // Invalid URL
      return new DeeplinkActivities(Collections.emptyList(), false);
    }

    Intent intent = new Intent(Intent.ACTION_VIEW);
    PackageManager packageManager = CommonUtils.getApplication().getPackageManager();

    // Resolve all apps for current URL
    intent.setData(Uri.parse(url));
    List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
    if (CommonUtils.isEmpty(resolveInfos)) {
      return new DeeplinkActivities(Collections.emptyList(), false);
    }

    List<ComponentName> currentUrlComponents = new ArrayList<>();
    for (ResolveInfo resolveInfo : resolveInfos) {
      if (resolveInfo.activityInfo == null) {
        continue;
      }
      String packageName = resolveInfo.activityInfo.packageName;
      String activityName = resolveInfo.activityInfo.name;

      // Check if DH is one of the deeplink handling app
      if (DHConstants.PACKAGE_NAME.equalsIgnoreCase(packageName)) {
        // DH supersedes all other apps.
        return new DeeplinkActivities(Collections.singletonList(new ComponentName(packageName,
            activityName)), true);
      }
      if (resolveInfo.activityInfo.exported) {
            currentUrlComponents.add(new ComponentName(packageName, activityName));
      }
    }
    return new DeeplinkActivities(currentUrlComponents, false);
  }

  /**
   * Fetch the installed browser components installed on the device
   * @return
   */
  public static List<ComponentName> getInstalledBrowsers() {
    if (null == standardComponents) {
      synchronized (DeeplinkHelper.class) {
        if (null == standardComponents) {
          Intent intent = new Intent(Intent.ACTION_VIEW);
          PackageManager packageManager = CommonUtils.getApplication().getPackageManager();
          // Resolve all apps for standard URL(possibly only Browsers)
          // This will be used as reference to detect deeplinks
          intent.setData(Uri.parse(Constants.STANDARD_EXTERNAL_URL));
          standardComponents = new ArrayList<>();
          List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
          if (!CommonUtils.isEmpty(resolveInfos)) {
            for (ResolveInfo resolveInfo : resolveInfos) {
              if (resolveInfo.activityInfo == null) {
                continue;
              }
              standardComponents.add(new ComponentName(resolveInfo.activityInfo.packageName,
                  resolveInfo.activityInfo.name));
            }
          }
        }
      }
    }
    return standardComponents;
  }


  public static class DeeplinkActivities {
    private List<ComponentName> deeplinkComponents;
    private boolean isInternalDeeplink;

    public DeeplinkActivities(List<ComponentName> deeplinkComponents, boolean isInternalDeeplink) {
      this.deeplinkComponents = deeplinkComponents;
      this.isInternalDeeplink = isInternalDeeplink;
    }

    public List<ComponentName> getComponents() {
      return deeplinkComponents;
    }

    public boolean isInternalDeeplink() {
      return isInternalDeeplink;
    }
  }
}
