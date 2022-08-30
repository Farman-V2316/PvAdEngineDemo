/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.font.FontType;
import com.newshunt.common.helper.font.FontWeight;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dhutil.view.service.KeepAliveService;

import java.util.List;

/**
 * Util class for Custom Tabs.
 * file copied from
 * https://github.com/GoogleChrome/custom-tabs-client/blob/master/shared/src/main/java/org/chromium/customtabsclient/shared/CustomTabsHelper.java
 *
 * @author neeraj.kumar
 */
public class CustomTabsUtil {
  static final String STABLE_CHROME_PACKAGE = "com.android.chrome";
  private static final String ACTION_CUSTOM_TABS_CONNECTION =
      "android.support.customtabs.action.CustomTabsService";
  private static final String EXTRA_CUSTOM_TABS_KEEP_ALIVE =
      "android.support.customtabs.extra.KEEP_ALIVE";

  private static String sPackageNameToUse;

  private CustomTabsUtil() {
  }

  public static void addKeepAliveExtra(Context context, Intent intent) {
    Intent keepAliveIntent = new Intent().setClassName(
        context.getPackageName(), KeepAliveService.class.getCanonicalName());
    intent.putExtra(EXTRA_CUSTOM_TABS_KEEP_ALIVE, keepAliveIntent);
  }

  /**
   * Goes through all apps that handle VIEW intents and check if chrome is availabe and have a
   * warmup service then returns the chrome package name.
   * <p>
   * This is <strong>not</strong> threadsafe.
   *
   * @param context {@link Context} to use for accessing {@link PackageManager}.
   * @return The package name recommended to use for connecting to custom tabs related components.
   */
  public static String getPackageNameToUse(Context context) {
    if (sPackageNameToUse != null) {
      return sPackageNameToUse;
    }

    PackageManager pm = context.getPackageManager();
    Intent activityIntent = new Intent(Intent.ACTION_VIEW);
    activityIntent.setData(Uri.parse("http://www.google.com"));

    // Get all apps that can handle VIEW intents.
    List<ResolveInfo> resolvedActivityList =
        pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);

    for (ResolveInfo info : resolvedActivityList) {
      Intent serviceIntent = new Intent();
      serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
      serviceIntent.setPackage(info.activityInfo.packageName);
      //check if chrome is availabe and supports custome tabs
      if (STABLE_CHROME_PACKAGE.equalsIgnoreCase(info.activityInfo.packageName)
          && pm.resolveService(serviceIntent, 0) != null) {
        sPackageNameToUse = info.activityInfo.packageName;
        return sPackageNameToUse;
      }
    }
    return sPackageNameToUse;
  }

  public static void changeFontInViewGroup(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);
      if (TextView.class.isAssignableFrom(child.getClass())) {
        FontHelper.setupTextView((TextView) viewGroup.getChildAt(i), FontType.NEWSHUNT_BOLD, FontWeight.BOLD);
      } else if (ViewGroup.class.isAssignableFrom(child.getClass())) {
        changeFontInViewGroup((ViewGroup) viewGroup.getChildAt(i));
      }
    }
  }

  public static boolean tabsSwipeEnabled(){
    return PreferenceManager.getPreference(AppStatePreference.IS_TABS_SWIPE_ENABLED,false);
  }
}
