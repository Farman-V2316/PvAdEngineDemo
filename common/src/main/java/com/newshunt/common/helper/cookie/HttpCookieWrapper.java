package com.newshunt.common.helper.cookie;

import java.net.HttpCookie;

/**
 * Created by shrikant.agrawal on 2/8/2016.
 */
public class HttpCookieWrapper {
  // Since the positive and zero max-age have their meanings,
  // this value serves as a hint as 'not specify max-age'.
  // Copied from HttpCookie class
  private final static long MAX_AGE_UNSPECIFIED = -1;

  private final HttpCookie cookie;

  private final long timeStamp;

  public HttpCookieWrapper(HttpCookie cookie, long timeStamp) {
    this.cookie = cookie;
    this.timeStamp = timeStamp;
  }

  public HttpCookie getCookie() {
    return cookie;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof HttpCookieWrapper) {
      return cookie.equals(((HttpCookieWrapper) o).getCookie());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return cookie.hashCode();
  }

  /**
   * The logic to check cookie expiry is copied from HttpCookie class.
   * Reason for using this method instead of the one in HttpCookie class is: When we kill and open
   * the app, we read the persisted cookie from disk and while doing so, a new instance of HttpCookie
   * is created from SerializableHttpCookieV2.readObject(). This in turn resets the whenCreated private variable in its constructor.
   * This causes a side effect that cookie will not expire on time.
   * There is no way to read/modify the whenCreated variable in HttpCookie class.
   * @return true to indicate the underlying HTTP cookie has expired; otherwise, false
   */
  public boolean hasExpired() {
    if (cookie == null) {
      return true;
    }
    if (cookie.getMaxAge() == 0) return true;

    // if not specify max-age, this cookie should be
    // discarded when user agent is to be closed, but
    // it is not expired.
    if (cookie.getMaxAge() == MAX_AGE_UNSPECIFIED) return false;

    long deltaSecond = (System.currentTimeMillis() - timeStamp) / 1000;
    return deltaSecond > cookie.getMaxAge();
  }
}
