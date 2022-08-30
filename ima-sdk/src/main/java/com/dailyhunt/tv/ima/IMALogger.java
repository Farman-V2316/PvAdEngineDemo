package com.dailyhunt.tv.ima;

import com.newshunt.common.helper.common.Logger;

/**
 * Logger for IMA SDK
 *
 * @author ranjith
 */

public class IMALogger {

  private static final String pattern = "@@@@@@@@@@@@@@@ - >";
  private static final boolean ENABLED = true;

  public static void v(String aTag, String aMessage) {
    if (ENABLED) {
      Logger.v(aTag, pattern + aMessage);
    }
  }

  public static void d(String aTag, String aMessage) {
    if (ENABLED) {
      Logger.d(aTag, pattern + aMessage);
    }
  }

  public static void w(String aTag, String aMessage) {
    if (ENABLED) {
      Logger.w(aTag, pattern + aMessage);
    }
  }

  public static void e(String aTag, String aMessage) {
    if (ENABLED) {
      Logger.e(aTag, pattern + aMessage);
    }
  }

  public static void i(String aTag, String aMessage) {
    if (ENABLED) {
      Logger.i(aTag, pattern + aMessage);
    }
  }
}
