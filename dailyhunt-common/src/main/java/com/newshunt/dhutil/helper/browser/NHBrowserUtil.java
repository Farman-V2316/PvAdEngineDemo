/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.browser;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.URLUtil;

import com.newshunt.deeplink.navigator.NhBrowserNavigator;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DeeplinkHelper;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider;
import com.newshunt.dhutil.helper.CustomTabActivityHelper;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dataentity.dhutil.model.entity.BrowserType;
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo;
import com.newshunt.dhutil.view.receiver.CustomTabsBroadcastReceiver;

import java.util.List;

import javax.annotation.Nonnull;

import androidx.browser.customtabs.CustomTabsIntent;


/**
 * An Utility class for NHBrowser
 *
 * @author santhosh.kc
 */
public class NHBrowserUtil {


  /**
   * check the useInternalBrowser attribute and select the browser to open url.
   *
   * @param url                    url to load
   * @param browserType            browser to use for loading give url
   * @param useWideViewPort        use wide view port or not
   * @param clearHistoryOnPageLoad clear history or not
   */
  public static void handleBrowserSelection(Activity activity, String url, BrowserType
      browserType, List<String> preferredBrowsers, boolean useWideViewPort, boolean clearHistoryOnPageLoad) {
    handleBrowserSelection(activity, url, browserType, preferredBrowsers, useWideViewPort,
        clearHistoryOnPageLoad, false);
  }

  /**
   * check the useInternalBrowser attribute and select the browser to open url.
   *
   * @param url                    url to load
   * @param browserType            browser to use for loading give url
   * @param useWideViewPort        use wide view port or not
   * @param clearHistoryOnPageLoad clear history or not
   * @param skipCheckingDHDeeplinks If true, open the deeplink directly, without checking if DH
   *                                can open this deeplink. Useful when we want to force opening
   *                                the link in a specific browser
   */
  public static void handleBrowserSelection(Activity activity, String url, BrowserType browserType,
                                            List<String> preferredBrowsers, boolean useWideViewPort,
                                            boolean clearHistoryOnPageLoad,
                                            boolean skipCheckingDHDeeplinks) {
    if (browserType == BrowserType.CUSTOM_TAB) {
      NHBrowserUtil.openWithChromeTabs(activity, url, useWideViewPort, clearHistoryOnPageLoad,
          skipCheckingDHDeeplinks);
    } else if (browserType == BrowserType.NH_BROWSER) {
      NHBrowserUtil.openWithNHBrowser(activity, url, useWideViewPort, clearHistoryOnPageLoad,
          skipCheckingDHDeeplinks);
    } else {
      launchBestBrowser(activity, preferredBrowsers, url, skipCheckingDHDeeplinks);
    }
  }

  private static void launchBestBrowser(final Activity activity, List<String> preferredBrowsers,
                                        final String url, boolean skipCheckingDHDeeplinks) {
    List<ComponentName> components;
    if (skipCheckingDHDeeplinks) {
      components = DeeplinkHelper.getInstalledBrowsers();
    } else {
      //Get all components capable of opening this url. If empty, Let system open the url
      DeeplinkHelper.DeeplinkActivities deepLinkResult =
          DeeplinkHelper.getAllDeeplinkActivities(url);
      components = deepLinkResult.getComponents();
      if (CommonUtils.isEmpty(components)) {
        NHBrowserUtil.openWithExtBrowser(activity, url);
        return;
      }

      //If it's an internal deeplink, DH will be the only component from the above method call
      if (deepLinkResult.isInternalDeeplink()) {
        NHBrowserUtil.openWithSpecificExternalBrowser(activity, url, components.get(0));
        return;
      }
    }

    //First get the preferred browsers from B.E
    if (CommonUtils.isEmpty(preferredBrowsers)) {
      //If preferred browsers are not sent, look in handshake
      AdsUpgradeInfo upgradeInfo = AdsUpgradeInfoProvider.getInstance().getAdsUpgradeInfo();
      if (upgradeInfo != null) {
        preferredBrowsers = upgradeInfo.getExternalBrowsers();
      }
    }

    if (CommonUtils.isEmpty(preferredBrowsers)) {
      //If no preferred browsers are set, Let system open the url
      NHBrowserUtil.openWithExtBrowser(activity, url);
    } else {
      //Check the best match between preferred browsers and browsers installed on the device
      for (String preferredBrowser : preferredBrowsers) {
        for (ComponentName componentName : components) {
          if (preferredBrowser.equalsIgnoreCase(componentName.getPackageName())) {
            NHBrowserUtil.openWithSpecificExternalBrowser(activity, url, componentName);
            return;
          }
        }
      }
      //No match between preferred browser and available browser. Let system open the url
      NHBrowserUtil.openWithExtBrowser(activity, url);
    }
  }

  /**
   * Utility function to open with NHBrowser
   * <p>
   * Important note - if caller of this function is from fragment, please make sure that isAdded
   * () returns true
   *
   * @param activity        calling activity
   * @param url             url to launch
   * @param useWideViewPort to use wide ViewPort in browser
   */
  public static void openWithNHBrowser(Context activity, String url, boolean useWideViewPort) {
    openWithNHBrowser(activity, url, useWideViewPort, true);
  }

  /**
   * Utility function to open with NHBrowser
   * <p>
   * Important note - if caller of this function is from fragment, please make sure that isAdded
   * () returns true
   *
   * @param activity               calling activity
   * @param url                    url to launch
   * @param useWideViewPort        to use wide ViewPort in browser
   * @param clearHistoryOnPageLoad flag to clear history on page load
   */
  public static void openWithNHBrowser(Context activity, String url, boolean useWideViewPort,
                                       boolean clearHistoryOnPageLoad) {
    openWithNHBrowser(activity, url, useWideViewPort, clearHistoryOnPageLoad, false);
  }

  /**
   * Utility function to open with NHBrowser
   * <p>
   * Important note - if caller of this function is from fragment, please make sure that isAdded
   * () returns true
   *
   * @param activity               calling activity
   * @param url                    url to launch
   * @param useWideViewPort        to use wide ViewPort in browser
   * @param clearHistoryOnPageLoad flag to clear history on page load
   * @param skipCheckingDHDeeplinks If true, open the deeplink directly, without checking if DH
   *                                can open this deeplink. Useful when we want to force opening
   *                                the link in a specific browser
   */
  public static void openWithNHBrowser(Context activity, String url, boolean useWideViewPort,
                                       boolean clearHistoryOnPageLoad,
                                       boolean skipCheckingDHDeeplinks) {
    if (CommonUtils.isEmpty(url) || activity == null) {
      return;
    }

    List<ComponentName> deepLinkActivities =
        skipCheckingDHDeeplinks ? null : DeeplinkHelper.getDeeplinkActivities(url);

    boolean launchNHBrowser;

    if (CommonUtils.isEmpty(deepLinkActivities)) {
      launchNHBrowser = true;
    } else if (deepLinkActivities.size() == 1) {
      try {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.putExtra(Constants.DEEP_LINK_DOUBLE_BACK_EXIT, true);
        intent.setComponent(deepLinkActivities.get(0));
        intent.putExtra(Constants.IS_INTERNAL_DEEPLINK, true);
        activity.startActivity(intent);
      } catch (ActivityNotFoundException e) {
        Logger.caughtException(e);
      }
      return;
    } else {
      launchNHBrowser = false;
    }

    if (launchNHBrowser && URLUtil.isValidUrl(url)) {
      Intent browserIntent = NhBrowserNavigator.getTargetIntent();
      browserIntent.putExtra(DailyhuntConstants.URL_STR, url);
      browserIntent.putExtra(DailyhuntConstants.USE_WIDE_VIEW_PORT, useWideViewPort);
      browserIntent.putExtra(DailyhuntConstants.CLEAR_HISTORY_ON_PAGE_LOAD, clearHistoryOnPageLoad);
      browserIntent.putExtra(Constants.VALIDATE_DEEPLINK, !skipCheckingDHDeeplinks);
      activity.startActivity(browserIntent);
    } else {
      openWithExtBrowser(activity, url);
    }
  }


  /**
   * Utility function launch url with external browser
   *
   * @param activity - calling activity
   * @param url      - target URL
   */
  public static void openWithExtBrowser(Context activity, String url) {
    try {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      activity.startActivity(browserIntent);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  /**
   * Utility function launch url with Chrome Custom tabs.
   *
   * @param activity        calling activity
   * @param url             target URL
   * @param useWideViewPort to use wide ViewPort in browser
   */
  public static void openWithChromeTabs(Context activity, String url, boolean useWideViewPort) {
    openWithChromeTabs(activity, url, useWideViewPort, true, false);
  }

  /**
   * Utility function launch url with Chrome Custom tabs.
   *
   * @param activity        calling activity
   * @param url             target URL
   * @param useWideViewPort to use wide ViewPort in browser
   * @param skipCheckingDHDeeplinks If true, open the deeplink directly, without checking if DH
   *                                can open this deeplink. Useful when we want to force opening
   *                                the link in a specific browser
   */
  public static void openWithChromeTabs(Context activity, String url, boolean useWideViewPort,
                                        boolean clearHistoryOnPageLoad,
                                        boolean skipCheckingDHDeeplinks) {
    boolean isCustomTabLaunched;
    if (URLUtil.isValidUrl(url) && activity instanceof Activity) {
      try {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.addDefaultShareMenuItem();
        intentBuilder.setShowTitle(true);

        Intent intent = new Intent(activity, CustomTabsBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, intent, PendingIntent
            .FLAG_UPDATE_CURRENT);
        intentBuilder.addMenuItem(CommonUtils.getString(R.string.menu_copy_link), pendingIntent);

        intentBuilder.setStartAnimations(activity, R.anim.slide_in_right, R.anim.slide_out_left);
        intentBuilder.setExitAnimations(activity, android.R.anim.slide_in_left,
            android.R.anim.slide_out_right);
        isCustomTabLaunched = CustomTabActivityHelper.openCustomTab(
            (Activity) activity, intentBuilder.build(), Uri.parse(url));
      } catch (Exception e) {
        Logger.caughtException(e);
        isCustomTabLaunched = false;
      }
    } else {
      isCustomTabLaunched = false;
    }

    //If opening with custom tab is not successful fallback to NHBrowser
    if (!isCustomTabLaunched) {
      openWithNHBrowser(activity, url, useWideViewPort, clearHistoryOnPageLoad,
          skipCheckingDHDeeplinks);
    }
  }

  /**
   * Helper method to open a url in a specific browser
   * @param context Activity context
   * @param url url to open
   * @param browserComponent Browser component
   */
  public static void openWithSpecificExternalBrowser(@Nonnull final Context context,
                                                     @Nonnull final String url,
                                                     @Nonnull final ComponentName browserComponent) {
    try {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      browserIntent.setComponent(browserComponent);
      context.startActivity(browserIntent);
    } catch (Exception e) {
      Logger.caughtException(e);
      openWithExtBrowser(context, url);
    }
  }
}