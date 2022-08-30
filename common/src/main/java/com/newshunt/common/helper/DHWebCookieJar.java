/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper;

import android.webkit.CookieManager;

import com.newshunt.common.helper.common.NHWebViewUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Coookie jar to pass webview cookies in api request. Currently used for comscore url
 * requests request.
 *
 * @author: bedprakash.rout on 04/07/17.
 */

public class DHWebCookieJar implements CookieJar {

  @Override
  public void saveFromResponse(HttpUrl httpUrl, List<Cookie> cookies) {
    for (Object cookie : cookies) {
      String cookieString = cookie.toString();
      cookieString = cookieString.replace("domain=", "domain=.");
      // The cookies are stored to the Cookie Manager of the webview.
      CookieManager.getInstance().setCookie(httpUrl.toString(), cookieString);
    }
  }

  @Override
  public List<Cookie> loadForRequest(HttpUrl httpUrl) {
    // The cookies are picked up from webview cookie store for all request using this cookie jar.
    String[] webCookies = NHWebViewUtils.getWebViewCookiesForUrl(httpUrl.toString());
    List<Cookie> cookies = new ArrayList<>();
    for (String cookieString : webCookies) {
      Cookie cookie = Cookie.parse(httpUrl, cookieString);
      if (!CommonUtils.isEmpty(cookie.value())) {
        cookies.add(cookie);
      }
    }
    return cookies;
  }

}
