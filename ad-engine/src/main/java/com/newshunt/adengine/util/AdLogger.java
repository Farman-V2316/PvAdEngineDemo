package com.newshunt.adengine.util;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Logger;

/**
 * Logger for ads to append same pattern to all ad logs.
 *
 * @author heena.arora
 */
public class AdLogger {
  private static final String pattern = "**************** - >";
  //PANDA:
  /*private static final boolean ENABLED = AppConfig.getInstance() != null && AppConfig.getInstance()
      .isLoggerEnabled();*/

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
