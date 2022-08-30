/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.helper;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

import com.newshunt.dhutil.view.service.ServiceConnection;

import java.util.List;

/**
 * Helper class to manage connection to the Custom tab service
 * File coipied from
 * https://github.com/GoogleChrome/custom-tabs-client/blob/master/demos/src/main/java/org/chromium/customtabsdemos/CustomTabActivityHelper.java
 *
 * @author neeraj.kumar
 */
public class CustomTabActivityHelper implements ServiceConnection.ServiceConnectionCallback {
  private CustomTabsSession mCustomTabsSession;
  private CustomTabsClient mClient;
  private CustomTabsServiceConnection mConnection;

  /**
   * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
   *
   * @param activity         The host activity.
   * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
   * @param uri              the Uri to be opened.
   */
  public static boolean openCustomTab(Activity activity,
                                      CustomTabsIntent customTabsIntent, Uri uri) {
    String packageName = CustomTabsUtil.getPackageNameToUse(activity);

    //If we cant find a package name, it means there is no browser that supports
    //Chrome Custom Tabs installed. So, we fallback to the webview
    if (packageName == null) {
      return false;
    } else {
      customTabsIntent.intent.setPackage(packageName);
      CustomTabsUtil.addKeepAliveExtra(activity, customTabsIntent.intent);
      customTabsIntent.launchUrl(activity, uri);
      return true;
    }
  }

  /**
   * Unbinds the Activity from the Custom Tabs Service.
   *
   * @param activity the activity that is connected to the service.
   */
  public void unbindCustomTabsService(Activity activity) {
    if (mConnection == null) {
      return;
    }
    activity.unbindService(mConnection);
    mClient = null;
    mCustomTabsSession = null;
    mConnection = null;
  }

  /**
   * Creates or retrieves an exiting CustomTabsSession.
   *
   * @return a CustomTabsSession.
   */
  public CustomTabsSession getSession() {
    if (mClient == null) {
      mCustomTabsSession = null;
    } else if (mCustomTabsSession == null) {
      mCustomTabsSession = mClient.newSession(null);
    }
    return mCustomTabsSession;
  }

  /**
   * Binds the Activity to the Custom Tabs Service.
   *
   * @param activity the activity to be binded to the service.
   */
  public void bindCustomTabsService(Activity activity) {
    if (mClient != null) {
      return;
    }

    String packageName = CustomTabsUtil.getPackageNameToUse(activity);
    if (packageName == null) {
      return;
    }

    mConnection = new ServiceConnection(this);
    CustomTabsClient.bindCustomTabsService(activity, packageName, mConnection);
  }

  /**
   * @return true if call to mayLaunchUrl was accepted.
   * @see {@link CustomTabsSession#mayLaunchUrl(Uri, Bundle, List)}.
   */
  public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
    if (mClient == null) {
      return false;
    }

    CustomTabsSession session = getSession();
    if (session == null) {
      return false;
    }

    return session.mayLaunchUrl(uri, extras, otherLikelyBundles);
  }

  @Override
  public void onServiceConnected(CustomTabsClient client) {
    mClient = client;
    mClient.warmup(0L);
  }

  @Override
  public void onServiceDisconnected() {
    mClient = null;
    mCustomTabsSession = null;
  }
}
