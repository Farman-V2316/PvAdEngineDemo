/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import android.app.Activity;
import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.core.util.Pair;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dhutil.helper.browser.NHBrowserUtil;
import com.newshunt.helper.ImageUrlReplacer;

import java.lang.ref.WeakReference;

/**
 * A Java object to be injected into Javascript
 *
 * @author santhosh.kc
 */
public class NewsDetailWebContentInterface extends NHWebViewJSInterface {

  private final WeakReference<Context> contextWeakReference;
  private String videoId;

  public NewsDetailWebContentInterface(WebView webView, Activity activity, Fragment fragment,
                                       PageReferrer pageReferrer, String videoId) {
    super(webView, activity, fragment, pageReferrer);
    contextWeakReference = new WeakReference<>(activity);
    this.videoId = videoId;
  }

  @JavascriptInterface
  public String getMainVideoId() {
    return videoId;
  }

  @JavascriptInterface
  public String getSlowImageReplacement() {
    return ImageUrlReplacer.getEmbeddedImageSlow();
  }

  @JavascriptInterface
  public String getFastImageReplacement() {
    Pair<Integer, Integer> newsDetailDimension = NewsListCardLayoutUtil
        .getNewsDetailMastHeadImageDimension();
    return ImageUrlReplacer.getDimensionString(newsDetailDimension.first) +
        ImageUrlReplacer.RESOLUTION_CHARACTER +
        ImageUrlReplacer.getDimensionString(newsDetailDimension.second);
  }


  @JavascriptInterface
  public void openUrlInNHBrowser(String url) {
    Context context = contextWeakReference.get();
    if (context == null) {
      return;
    }
    NHBrowserUtil.openWithNHBrowser(context, url, true);
  }

  @JavascriptInterface
  public void openUrlInExtBrowser(String url) {
    Context context = contextWeakReference.get();
    if (context == null) {
      return;
    }
    NHBrowserUtil.openWithExtBrowser(context, url);
  }

  @JavascriptInterface
  public void openUrlInChromeTabs(String url) {
    Context context = contextWeakReference.get();
    if (context == null) {
      return;
    }
    NHBrowserUtil.openWithChromeTabs(context, url, true);
  }
}
