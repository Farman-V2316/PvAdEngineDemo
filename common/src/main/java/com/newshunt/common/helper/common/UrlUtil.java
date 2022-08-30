/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import android.net.Uri;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Url util to get host and path of a url
 *
 * @author maruti.borker
 */
public class UrlUtil {
  public static String getPath(String endPoint) throws MalformedURLException {
    URL url = new URL(endPoint);
    String host = url.getHost();
    return endPoint.substring(endPoint.indexOf(host) + host.length() + 1);
  }

  public static String getHost(String endPoint) throws MalformedURLException {
    URL url = new URL(endPoint);
    String host = url.getHost();
    return endPoint.substring(0, endPoint.indexOf(host) + host.length() + 1);
  }

  public static String getLastPathSegment(String url) {
    Uri deepLinkUri = Uri.parse(url);
    return deepLinkUri.getLastPathSegment();
  }

  public static String getUrlWithQueryParamns(String url , Map<String, String> paramsMap) {
    String baseUrl = getBaseUrl(url);
    String queryUrl = getQueryUrl(url);
    Map<String, String> map = urlRequestParamToMap(queryUrl);
    map.putAll(paramsMap);
    return formatAsUrl(baseUrl, map);
  }

  public static String appendPath(@NonNull String baseUrl, @NonNull String path) {
    if (path.startsWith(Constants.FORWARD_SLASH)) {
      path = path.replaceFirst(Constants.FORWARD_SLASH, Constants.EMPTY_STRING);
    }
    Uri uri = Uri.parse(baseUrl).buildUpon().appendEncodedPath(path).build();
    return uri.toString();
  }

  public static String getQueryUrl(String url) {
    if (CommonUtils.isEmpty(url) || !url.contains("?")) {
      return Constants.EMPTY_STRING;
    }
    return url.substring(url.indexOf("?") + 1);
  }

  public static String getBaseUrl(String url) {
    if (CommonUtils.isEmpty(url)) {
      return Constants.EMPTY_STRING;
    }

    if (!url.contains("?")) {
      return url;
    }
    return url.substring(0, url.indexOf("?"));
  }

  @NonNull
  public static Map<String, String> urlRequestParamToMap(String queryUrl) {
    if (CommonUtils.isEmpty(queryUrl)) {
      return new LinkedHashMap<>();
    }
    Map<String, String> keyValueMapping = new LinkedHashMap<>();
    String[] params = queryUrl.split("&");
    for (String param : params) {
      if (CommonUtils.isEmpty(param)) {
        //bug fix : https://bugzilla.newshunt.com/eterno/show_bug.cgi?id=27778
        //queryUrl = "&s=a&ss=pd" => params = ["", "s=a", "ss=pd"]
        continue;
      }
      String[] keyValue = param.split("=");
      String key = keyValue[0];
      String value = keyValue.length == 2 ? keyValue[1] : "";
      keyValueMapping.put(key, value);
    }

    return keyValueMapping;
  }

  public static String formatAsUrl(String baseUrl, Map<String, String> pairs) {
    if (pairs == null) {
      return "";
    }

    StringBuilder urlBuilder = new StringBuilder(baseUrl).append("?");
    boolean appendAnd = false;
    for (String key : pairs.keySet()) {
      if (appendAnd) {
        urlBuilder.append("&");
      }
      urlBuilder.append(key).append("=").append(pairs.get(key));
      appendAnd = true;
    }
    return urlBuilder.toString();
  }

  public static String getOverwrittenParamsUrl(
    Map<String, String> primaryMap, Map<String, String> secondaryMap, String url) {
    if (null == secondaryMap) {
      secondaryMap = new HashMap<>();
    }
    if (!CommonUtils.isEmpty(url) && !CommonUtils.isEmpty(primaryMap)) {
      for (String key : primaryMap.keySet()) {
        secondaryMap.put(key, primaryMap.get(key));
      }
    }
    return formatAsUrl(getBaseUrl(url), secondaryMap);
  }
}