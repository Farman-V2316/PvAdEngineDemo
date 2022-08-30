/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.cookie;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom Cookie Manager
 *
 * @author arun.babu
 */
public class CustomCookieManager extends CookieManager {

  private static final String LOG_TAG = "CustomCookieManager";
  private static final String SCHEME_HTTPS = "https";

  private static volatile CustomCookieManager instance;

  private CustomCookieManager() {
    super(PersistentCookieStore.getInstance(), CookiePolicy.ACCEPT_ALL);
    CookieHandler.setDefault(this);
  }

  public static CustomCookieManager getInstance() {
    if (instance == null) {
      synchronized (CustomCookieManager.class) {
        if (instance == null) {
          instance = new CustomCookieManager();
        }
      }
    }
    return instance;
  }

  public List<HttpCookieWrapper> getCookieWrappers() {
    return ((PersistentCookieStore) getCookieStore()).getCookieWrappers();
  }

  public static List<HttpCookie> getCookies(String url) {
    List<HttpCookieWrapper> cookieList = new ArrayList<>();
    List<HttpCookieWrapper> storedCookies = getInstance().getCookieWrappers();

    for (HttpCookieWrapper cookieWrapper : storedCookies) {
      if (isRelevantCookie(url, cookieWrapper)) {
        int index = cookieList.indexOf(cookieWrapper);
        if (index == -1) {
          cookieList.add(cookieWrapper);
          continue;
        } else {
          HttpCookieWrapper filteredCookie = cookieList.get(index);
          if (filteredCookie.getTimeStamp() < cookieWrapper.getTimeStamp()) {
            cookieList.remove(filteredCookie);
            cookieList.add(cookieWrapper);
          }
        }
      }
    }

    ArrayList<HttpCookie> ret = new ArrayList<>();
    for (HttpCookieWrapper wrapper : cookieList) {
      ret.add(wrapper.getCookie());
      Logger.d(LOG_TAG, "Cookie - " + wrapper.getCookie().getName() + "::"
          + wrapper.getCookie().getDomain() + "::" + wrapper.getCookie().getValue());
    }
    return Collections.unmodifiableList(ret);
  }

  public static String getCookieValue(String url, String cookieName) {
    List<HttpCookie> cookies = getCookies(url);
    for (HttpCookie cookie : cookies) {
      if (CommonUtils.equals(cookie.getName(), cookieName)) {
        return cookie.getValue();
      }
    }
    return Constants.EMPTY_STRING;
  }

  public static boolean isRelevantCookie(String url, HttpCookieWrapper cookieWrapper) {
    try {
      HttpCookie cookie = cookieWrapper.getCookie();
      URI uri = new URI(url);
      /**
       * Checking the expiry of both HttpCookie and HttpCookieWrapper to cover for cases where the
       * cookie creation time and cookie wrapper creation time differ.
       * TODO: Explore Newer cookie persistence methods with OkHttp
       */
      return (HttpCookie.domainMatches(cookie.getDomain(), uri.getHost())
          && secureMatches(cookie, uri)) && !cookieWrapper.hasExpired() && !cookie.hasExpired();
    } catch (Exception e) {
      return false;
    }
  }

  static boolean secureMatches(HttpCookie cookie, URI uri) {
    return !cookie.getSecure() || SCHEME_HTTPS.equalsIgnoreCase(uri.getScheme());
  }
}
