/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Coookie jar to pass no cookies in api request. Currently used for cricket score updates
 *
 * @author: shrikant.agrawal on 08/29/17.
 */

public class EmptyCookieJar implements CookieJar {


  private List<Cookie> emptyCookieList = Collections.emptyList();

  @Override
  public void saveFromResponse(HttpUrl httpUrl, List<Cookie> cookies) {
  }

  @Override
  public List<Cookie> loadForRequest(HttpUrl httpUrl) {
    return emptyCookieList;
  }

}
