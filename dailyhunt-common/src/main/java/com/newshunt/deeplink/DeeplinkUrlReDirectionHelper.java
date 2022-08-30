/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dataentity.analytics.entity.DevEvent;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.dhutil.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @author santhosh.kc
 */
public class DeeplinkUrlReDirectionHelper {

  public static String getReDirectedUrl(String shortUrl) {
    return getReDirectedUrl(shortUrl, -1);
  }

  public static String getReDirectedUrl(String shortUrl, int uniqueId) {
    if (CommonUtils.isEmpty(shortUrl)) {
      return null;
    }

    if (DeeplinkUtils.isShortUrl(shortUrl)) {
      return getReDirectedUrlFromShortUrl(shortUrl, uniqueId);
    } else {
      return shortUrl;
    }
  }

  private static String getReDirectedUrlFromShortUrl(String shortUrl, int uniqueId) {
    String redirectedUrl = shortUrl;
    Map<String, String> paramsMapShortUrl;
    paramsMapShortUrl = UrlUtil.urlRequestParamToMap(UrlUtil.getQueryUrl(shortUrl));
    BusProvider.getRestBusInstance()
        .post(new DevEvent(DevEvent.EventType.API_REQUEST, DevEvent.API.DEV_NEWS_SHORT_URL,
            uniqueId));
    String dailyhuntScheme = CommonUtils.getString(R.string.scheme_dailyhunt) + "://";
    if (redirectedUrl.startsWith(dailyhuntScheme)) {
      redirectedUrl = redirectedUrl.replace(dailyhuntScheme, CommonUtils.getString(R.string
          .scheme_http) + "://");
    }

    if (DeepLinkParser.parseUrl(redirectedUrl) == null) {
      redirectedUrl = getRedirectedUrl(redirectedUrl, shortUrl);
      if (redirectedUrl == null) {
        return null;
      }
    }
    Map<String, String> paramsMapRedirectedUrl;
    paramsMapRedirectedUrl = UrlUtil.urlRequestParamToMap(UrlUtil.getQueryUrl(redirectedUrl));
    BusProvider.getRestBusInstance().post(new DevEvent(DevEvent.EventType.API_RESPONSE, DevEvent
        .API.DEV_NEWS_SHORT_URL, uniqueId));

    redirectedUrl = UrlUtil.getOverwrittenParamsUrl(
        paramsMapShortUrl, paramsMapRedirectedUrl, redirectedUrl);
    return redirectedUrl;
  }

  private static String getRedirectedUrl(String redirectedUrl, String shortUrl) {
    HttpURLConnection connection;
    try {
      while (true) {
        connection = (HttpURLConnection) new URL(redirectedUrl).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(Constants.NETWORK_TIMEOUT_MSEC);
        connection.setReadTimeout(Constants.NETWORK_TIMEOUT_MSEC);
        connection.setRequestProperty(Constants.HEADER_USER_AGENT,
            Constants.HEADER_VALUE_ANDROID);
        connection.setRequestProperty(Constants.HEADER_CLIENT_TYPE,
            Constants.HEADER_VALUE_ANDROID_SMALL);
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode >= 300 && responseCode < 400 &&
            connection.getHeaderField("Location") != null) {
          redirectedUrl = connection.getHeaderField("Location");
        } else {
          break;
        }
        if (DeepLinkParser.parseUrl(redirectedUrl) != null) {
          break;
        }
      }
    } catch (Exception e) {

      // that means not even one redirection loop was not completed
      if (redirectedUrl.equals(shortUrl)) {
        return null;
      }
    }
    return redirectedUrl;
  }
}
