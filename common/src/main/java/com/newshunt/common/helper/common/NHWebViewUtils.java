/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.cookie.CustomCookieManager;
import com.newshunt.common.helper.listener.WebViewValueCallback;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.lang.ref.WeakReference;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * An Utility class for NHWebView.
 *
 * @author shashikiran.nr on 2/16/2017.
 */

public class NHWebViewUtils {

  private static String TAG = "NHWebViewUtils";

  /**
   * Utility function to handle links clicks on webview content
   *
   * @param view calling view
   * @param url  url to launch
   */
  public static boolean shouldOverrideUrlLoading(WebView view, String url, Boolean isFinish,
                                                 PageReferrer pageReferrer ) {
    // if url starts with http or https the load in same webview otherwise pass intent to os to
    // handle it.
    ComponentName componentName = null;
    if (CommonUtils.isEmpty(url)) {
      return false;
    } else if (url.toLowerCase().startsWith("http://") ||
        url.toLowerCase().startsWith("https://")) {
      List<ComponentName> components = DeeplinkHelper.getDeeplinkActivities(url);
      if (components.isEmpty()) {
        // Not a deeplink. Handle with NHBrowser.
        return false;
      }

      Set<String> deepLinkPatternsToBeExcluded = PreferenceManager.getPreference(
          GenericAppStatePreference.DEEP_LINK_PATTERNS_TO_BE_EXLCUDED, new HashSet<>());

      if (deepLinkPatternsToBeExcluded == null) {
        deepLinkPatternsToBeExcluded = new HashSet<>();
      }

      List<ComponentName> componentNamesToBeExcluded = new ArrayList<>();
      for (ComponentName component : components) {
        for (String deepLinkPattern : deepLinkPatternsToBeExcluded) {
          if (CommonUtils.isEmpty(deepLinkPattern) || component == null) {
            continue;
          }
          Pattern pattern = Pattern.compile(deepLinkPattern);
          Matcher matcher = pattern.matcher(component.getPackageName());
          if (matcher.matches()) {
            componentNamesToBeExcluded.add(component);
          }
        }
      }

      components.removeAll(componentNamesToBeExcluded);

      if (components.isEmpty()) {
        return false;
      }

      if (components.size() == 1) {
        // Unique deeplink found
        componentName = components.get(0);
      } else {
        // Multiple deeplink activities will be handled by Intent Chooser.
        componentName = null;
      }
    } else if (MailTo.isMailTo(url)) {
      try {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        MailTo mailToUri = MailTo.parse(url);
        i.setData(Uri.parse(url));
        i.putExtra(Intent.EXTRA_EMAIL, mailToUri.getTo());
        i.putExtra(Intent.EXTRA_CC, mailToUri.getCc());
        i.putExtra(Intent.EXTRA_SUBJECT, mailToUri.getSubject());
        i.putExtra(Intent.EXTRA_TEXT, mailToUri.getBody());

        if (view != null && view.getContext() != null) {
          view.getContext().startActivity(i);
          if (isFinish) {
            ((Activity) view.getContext()).finish();
          }
        }
        return true;
      } catch (ActivityNotFoundException e) {
        Logger.caughtException(e);
        return false;
      }
    }

    try {
      Intent i = new Intent(Intent.ACTION_VIEW);
      i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      i.setData(Uri.parse(url));
      if (componentName != null) {
        i.setComponent(componentName);
        if (pageReferrer != null &&
            AppConfig.getInstance().getPackageName().equals(componentName.getPackageName())) {
          i.putExtra(Constants.BACK_URL_REFERRER, pageReferrer);
        }
      }
      if (view != null && view.getContext() != null) {
        view.getContext().startActivity(i);
        if (isFinish) {
          ((Activity) view.getContext()).finish();
        }
      }
      return true;
    } catch (Exception e) {
      Logger.caughtException(e);
      return false;
    }
  }

  /**
   * Initialize the WebView with the basic attributes
   *
   * @param webView - The webview to be initialized
   */
  public static void initializeWebView(@NonNull final WebView webView) {
    webView.setPadding(0, 0, 0, 0);
    webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    webView.setVerticalScrollBarEnabled(false);
    webView.setHorizontalScrollBarEnabled(false);
    webView.getSettings().setUseWideViewPort(false);
    webView.getSettings().setSupportZoom(false);
    webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
    setDownloadListener(webView);
  }

  private static void setDownloadListener(@NonNull final WebView webView) {
    final Context context = CommonUtils.getApplication();
    webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
      try {
        final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
        final DownloadManager.Request request = new DownloadManager.Request(
            Uri.parse(url));

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        DownloadManager dm =
            (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) {
          dm.enqueue(request);
        }
        BusProvider.getUIBusInstance().post(new DownloadWebData(filename));
      } catch (Exception e) {
        Logger.caughtException(e);
      }
    });
  }

  /**
   * A utility method to destroy the WebView
   *
   * @param webView - the WebView to destroy
   */
  public static void destroyView(WebView webView) {
    webView.stopLoading();
    webView.removeAllViews();
    webView.destroy();
  }

  /**
   * This method is used to call Javascript functions from client;
   *
   * @param webView - The webview for which the calls are to be made
   * @param script  - The script i.e the function with arguments.
   */
  public static void callJavaScriptFunctionWithReturnValue(WebView webView, String script) {
    if (webView == null) {
      return;
    }

    callJavaScriptFunctionWithReturnValue(webView, script, Constants.INVALID_INDEX, null);
  }

  /**
   * This method is used to call Javascript functions from client;
   *
   * @param webView              - The webview for which the calls are to be made
   * @param script               - The function to be called in Javascript
   * @param requestCode          - Unique request code associated with every call.
   * @param webViewValueCallback - A callback function for determining the value returning by the
   *                             function in javascript.
   */
  public static void callJavaScriptFunctionWithReturnValue(WebView webView, String script, int
      requestCode, WebViewValueCallback webViewValueCallback) {

    Logger.d(TAG, "Calling the following Javascript function " + script);
    if (webView == null) {
      return;
    }

    //Do it on the UI thread.
    //http://stackoverflow.com/a/22611010/1237141
    webView.post(() -> {
      callJavaScriptFunction(webView, script, requestCode, webViewValueCallback);
    });
  }

  public static void callJavaScriptFunction(WebView webView, String script) {
    if (webView == null || CommonUtils.isEmpty(script)) {
      return;
    }
    callJavaScriptFunction(webView, script, Constants.INVALID_INDEX, null);
  }

  private static void callJavaScriptFunction(@NonNull WebView webView, String script,
                                             int requestCode,
                                             WebViewValueCallback webViewValueCallback) {
    WeakReference<WebViewValueCallback> weakReference = new WeakReference(webViewValueCallback);
    webView.evaluateJavascript(script, new ValueCallback<String>() {
      @Override
      public void onReceiveValue(String value) {
        WebViewValueCallback callback = weakReference.get();
        if (callback != null && requestCode != Constants.INVALID_INDEX) {
          callback.onValueReceived(requestCode, value);
        }
      }
    });
  }

  /**
   * This function is used to return a string by appending  parameters to the function name.
   * E.g if formatScript("init", "Dailyhunt" ,"application",10, true) is called, then it will return
   * the
   * following string init('Dailyhunt','application',10,true);
   */
  @NonNull
  public static String formatScript(@NonNull final String function,
                                    @Nullable final Object... params) {
    if (CommonUtils.isEmpty(function)) {
      return Constants.EMPTY_STRING;
    }

    final StringBuilder builder = new StringBuilder(function).append('(');
    final int length = params.length;
    for (int i = 0; i < params.length; ++i) {
      if (params[i] instanceof String) {
        builder.append("\'");
      }
      builder.append(params[i]);
      if (params[i] instanceof String) {
        builder.append("\'");
      }
      if (i != length - 1) {
        builder.append(Constants.COMMA_CHARACTER);
      }
    }

    builder.append(')');
    return builder.toString();
  }

  /**
   * This method is used to store WebView Cookies which are stored via android.webkit
   * .CookieManager to Http cookies which are stores via java.net.CookieManager.
   *
   * @param loadedUrl - The url for which the cookies are requested to save.
   */
  public static void storeWebViewCookiestoHttp(String loadedUrl, String domainName) {
    if (CommonUtils.isEmpty(loadedUrl) || CommonUtils.isEmpty(domainName)) {
      return;
    }

    String[] webViewCookies = getWebViewCookiesForUrl(loadedUrl);
    if (CommonUtils.isEmpty(webViewCookies)) {
      return;
    }
    //Create an instance of Http Cookie Manager.
    CustomCookieManager customCookieManager = CustomCookieManager.getInstance();
    CookieStore httpCookieStore = customCookieManager.getCookieStore();
    if (httpCookieStore == null) {
      return;
    }

    //Iterate through all the WebCookies and store them in Http cookies.
    for (String webViewCookie : webViewCookies) {
      // create cookie
      String[] webCookieWithNameAndValue = webViewCookie.split(Constants.EQUAL_CHAR);
      if (CommonUtils.isEmpty(webCookieWithNameAndValue) || webCookieWithNameAndValue.length != 2) {
        continue;
      }
      String cookieName = webCookieWithNameAndValue[0];
      String cookieValue = webCookieWithNameAndValue[1];
      if (CommonUtils.isEmpty(cookieName) || CommonUtils.isEmpty(cookieValue)) {
        continue;
      }
      HttpCookie cookie = new HttpCookie(cookieName, cookieValue);
      cookie.setDomain(domainName);
      cookie.setPath("/");
      try {
        httpCookieStore.add(new URI(loadedUrl), cookie);
      } catch (URISyntaxException e) {
        Logger.caughtException(e);
      }
    }
  }

  @NonNull
  public static String[] getWebViewCookiesForUrl(String url) {
    String cookies;
    try {
      CookieManager cookieManager = CookieManager.getInstance();
      cookies = cookieManager.getCookie(url);
    } catch (Exception ex) {
      Logger.caughtException(ex);
      cookies = Constants.EMPTY_STRING;
    }
    return getCookiesFromString(cookies);
  }

  @NonNull
  public static String[] getCookiesFromString(String cookies) {
    if (CommonUtils.isEmpty(cookies)) {
      return new String[0];
    }
    return cookies.split(Constants.SEMICOLON);
  }

  @NonNull
  public static String createCookieString(@NonNull ArrayMap<String, Cookie> cookies) {
    StringBuilder cookieString = new StringBuilder();
    Set<String> keySet = cookies.keySet();
    for (String key : keySet) {
      cookieString.append(cookies.get(key));
      cookieString.append(Constants.COMMA_CHARACTER);
    }
    return cookieString.length() > 0 ? cookieString.substring(0, cookieString.length() - 1) :
        Constants.EMPTY_STRING;
  }

  /**
   * This is a workaround to delete cookies from webview cookie store.
   * The webview cookie store doesn't have any api to delete cookies.
   * So instead of deleting cookies we override the cookie with a expired time and set the value
   * as "".
   * <p>
   * Although the API docs specify that setCookie ignores values that have expired, Android's
   * current implementation actually overwrites the cookie in this case. In
   * {@link com.newshunt.common.helper.DHWebCookieJar}
   * we don't pick up the cookies with value as "". So in future if the expired cookies don;t get
   * updated that will act as a fallback.
   *
   * @param url    - url on which cookies will be set
   * @param domain - cookie domain
   */
  public static void removeCookiesFromWebView(String url, String domain) {
    String[] cookies = NHWebViewUtils.getWebViewCookiesForUrl(url);
    for (String cookieString : cookies) {
      try {
        Cookie cookie = Cookie.parse(HttpUrl.parse(url), cookieString);
        cookie = new Cookie.Builder().name(cookie.name())
            .expiresAt(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000)
            .value(Constants.EMPTY_STRING)
            .domain(domain)
            .build();
        CookieManager.getInstance().setCookie(url, cookie.toString());
      } catch (Exception e) {
        Logger.e("NhWebViewUtils", "removeCookiesFromWebView ", e);
      }
    }
  }

}
