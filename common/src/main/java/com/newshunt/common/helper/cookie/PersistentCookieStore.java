/*
 * Copyright (c) 2015 Fran Montiel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.newshunt.common.helper.cookie;

import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A persistent cookie store which implements the Apache HttpClient CookieStore interface.
 * Cookies are stored and will persist on the user's device between application sessions since they
 * are serialized and stored in SharedPreferences. Instances of this class are
 * designed to be used with AsyncHttpClient#setCookieStore, but can also be used with a
 * regular old apache HttpClient/HttpContext if you prefer.
 */
public class PersistentCookieStore implements CookieStore {

  private static final String LOG_TAG = "PersistentCookieStore";
  private static final String COOKIE_PREFS = "CookiePrefsFile";
  private static final String COOKIE_NAME_PREFIX = "cookie_";

  private static PersistentCookieStore instance;

  private HashMap<String, ConcurrentHashMap<String, HttpCookieWrapper>> cookies;
  private SharedPreferences cookiePrefs;

  private PersistentCookieStore() {
    init();
  }

  public static PersistentCookieStore getInstance() {
    if (instance == null) {
      synchronized (PersistentCookieStore.class) {
        if (instance == null) {
          instance = new PersistentCookieStore();
        }
      }
    }
    return instance;
  }

  public void migrateHalfBakedCookies(String url) {
    if (TextUtils.isEmpty(url)) {
      return;
    }

    try {
      Uri uri = Uri.parse(url);
      ConcurrentHashMap<String, HttpCookieWrapper> cookiesMap = cookies.get(uri.getHost());
      if (cookiesMap == null) {
        return;
      }
      Set<String> cookieSet = cookiesMap.keySet();
      for (String cookieName : cookieSet) {
        HttpCookieWrapper cookieWrapper = cookies.get(uri.getHost()).get(cookieName);
        if (cookieWrapper != null && cookieWrapper.getCookie().getPath() == null) {
          cookieWrapper.getCookie().setPath("/");

          SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
          prefsWriter.putString(uri.getHost(),
              TextUtils.join(Constants.COMMA_CHARACTER, cookies.get(uri.getHost()).keySet()));
          prefsWriter.putString(COOKIE_NAME_PREFIX + cookieName,
              encodeCookie(new SerializableHttpCookieV2(cookieWrapper)));
          prefsWriter.apply();
        }
      }
    } catch (Exception ex) {
      Logger.w(LOG_TAG, "Error migrating cookies", ex);
    }
  }

  /**
   * Initialize the persistent cookie store.
   */
  private void init() {
    cookiePrefs = CommonUtils.getApplication().getSharedPreferences(COOKIE_PREFS, 0);
    cookies = new HashMap<String, ConcurrentHashMap<String, HttpCookieWrapper>>();

    // Load any previously stored cookies into the store
    Map<String, ?> prefsMap = cookiePrefs.getAll();
    for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
      if (entry.getValue() != null &&
          !((String) entry.getValue()).startsWith(COOKIE_NAME_PREFIX)) {
        String[] cookieNames =
            TextUtils.split((String) entry.getValue(), Constants.COMMA_CHARACTER);
        for (String name : cookieNames) {
          String encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null);
          if (encodedCookie != null) {
            HttpCookieWrapper decodedCookie = decodeCookie(encodedCookie);
            if (decodedCookie != null) {
              if (!cookies.containsKey(entry.getKey())) {
                cookies.put(entry.getKey(), new ConcurrentHashMap<String, HttpCookieWrapper>());
              }
              cookies.get(entry.getKey()).put(name, decodedCookie);
            }
          }
        }

      }
    }
  }

  @Override
  public void add(URI uri, HttpCookie cookie) {
    HttpCookieWrapper cookieWrapper = new HttpCookieWrapper(cookie, System.currentTimeMillis());
    String name = getCookieToken(uri, cookie);

    // Save cookie into local store, or remove if expired
    if (!cookie.hasExpired()) {
      if (!cookies.containsKey(uri.getHost()) || cookies.get(uri.getHost()) == null) {
        cookies.put(uri.getHost(), new ConcurrentHashMap<String, HttpCookieWrapper>());
      }
      cookies.get(uri.getHost()).put(name, cookieWrapper);
    } else {
      if (cookies.containsKey(uri.getHost())) {
        cookies.get(uri.getHost()).remove(name);
      }
    }

    if (!cookies.containsKey(uri.getHost())) {
      return;
    }

    // Save cookie into persistent store
    SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
    prefsWriter.putString(uri.getHost(),
        TextUtils.join(Constants.COMMA_CHARACTER, cookies.get(uri.getHost()).keySet()));
    prefsWriter.putString(COOKIE_NAME_PREFIX + name,
        encodeCookie(new SerializableHttpCookieV2(cookieWrapper)));
    prefsWriter.apply();
  }

  protected String getCookieToken(URI uri, HttpCookie cookie) {
    return cookie.getName() + cookie.getDomain();
  }

  @Override
  public List<HttpCookie> get(URI uri) {
    Logger.d(LOG_TAG, "GetCookie -" + uri);
    ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
    try {
      // Wrapped around try catch to avoid crash reported in crashlytics for changes happening
      // between containsKey and get
      ret.addAll(getCookies(uri));
    } catch (NoSuchElementException e) {
      //Do nothing
    }
    return ret;
  }

  @Override
  public boolean removeAll() {
    SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
    prefsWriter.clear();
    prefsWriter.apply();
    cookies.clear();
    return true;
  }


  @Override
  public boolean remove(URI uri, HttpCookie cookie) {
    String name = getCookieToken(uri, cookie);

    if (cookies.containsKey(uri.getHost()) && cookies.get(uri.getHost()).containsKey(name)) {
      cookies.get(uri.getHost()).remove(name);

      SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
      if (cookiePrefs.contains(COOKIE_NAME_PREFIX + name)) {
        prefsWriter.remove(COOKIE_NAME_PREFIX + name);
      }
      prefsWriter.putString(uri.getHost(),
          TextUtils.join(",", cookies.get(uri.getHost()).keySet()));
      prefsWriter.apply();

      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<HttpCookie> getCookies() {
    return getCookies(null);
  }

  @Override
  public List<URI> getURIs() {
    ArrayList<URI> ret = new ArrayList<URI>();
    for (String key : cookies.keySet()) {
      try {
        ret.add(new URI(key));
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    return ret;
  }

  /**
   * Serializes Cookie object into String
   *
   * @param cookie cookie to be encoded, can be null
   * @return cookie encoded as String
   */
  protected String encodeCookie(SerializableHttpCookieV2 cookie) {
    if (cookie == null) {
      return null;
    }
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      ObjectOutputStream outputStream = new ObjectOutputStream(os);
      outputStream.writeObject(cookie);
    } catch (IOException e) {
      Logger.d(LOG_TAG, "IOException in encodeCookie", e);
      return null;
    }

    return byteArrayToHexString(os.toByteArray());
  }

  /**
   * Returns cookie decoded from cookie string
   *
   * @param cookieString string of cookie as returned from http request
   * @return decoded cookie or null if exception occured
   */
  protected HttpCookieWrapper decodeCookie(String cookieString) {
    byte[] bytes = hexStringToByteArray(cookieString);
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    HttpCookieWrapper cookieWrapper = null;
    try {
      PackageChangeOIS packageChangeOIS = new PackageChangeOIS(byteArrayInputStream);
      Object object = packageChangeOIS.readObject();
      if (object instanceof SerializableHttpCookieV2) {
        cookieWrapper = ((SerializableHttpCookieV2) object).getCookie();
      } else {
        HttpCookie cookie = ((SerializableHttpCookie) object).getCookie();
        cookieWrapper = new HttpCookieWrapper(cookie, 0);
      }
    } catch (IOException e) {
      Logger.d(LOG_TAG, "IOException in decodeCookie", e);
    } catch (ClassNotFoundException e) {
      Logger.d(LOG_TAG, "ClassNotFoundException in decodeCookie", e);
    }
    return cookieWrapper;
  }

  /**
   * Using some super basic byte array &lt;-&gt; hex conversions so we don't have to rely on any
   * large Base64 libraries. Can be overridden if you like!
   *
   * @param bytes byte array to be converted
   * @return string containing hex values
   */
  protected String byteArrayToHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte element : bytes) {
      int v = element & 0xff;
      if (v < 16) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(v));
    }
    return sb.toString().toUpperCase(Locale.US);
  }

  /**
   * Converts hex values from strings to byte array
   *
   * @param hexString string of hex-encoded values
   * @return decoded byte array
   */
  protected byte[] hexStringToByteArray(String hexString) {
    int len = hexString.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) +
          Character.digit(hexString.charAt(i + 1), 16));
    }
    return data;
  }

  private List<HttpCookie> getCookies(URI uri) {
    return CustomCookieManager.getCookies(uri.toString());
  }

  public List<HttpCookieWrapper> getCookieWrappers() {
    ArrayList<HttpCookieWrapper> ret = new ArrayList<>();
    try {
      for (String key : cookies.keySet()) {
        ret.addAll(cookies.get(key).values());
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return ret;
  }
}