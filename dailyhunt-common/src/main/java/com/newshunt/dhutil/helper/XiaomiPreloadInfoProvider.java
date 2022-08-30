/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Logger;

import java.lang.reflect.Method;

/**
 * Protocol here:
 * 1. If user activates preloaded APK, directly return the install source from the APK
 * 2. If user does not activate but updates the APK, if preloaded, return a default source
 * 3. If user does not activate but uninstalls and reinstalls through some other source, return
 * the source in the APK being activated.
 *
 * Created by srikanth.ramaswamy on 04/30/2018.
 */
public class XiaomiPreloadInfoProvider implements PreloadInfoProvider {
  private static final String XIAOMI_INSTALL_SOURCE_DEFAULT = "Xiaomi_Preburn_Cur_";
  private static final String DAILYHUNT_HOME_PATTERN = "DailyhuntHome^%s^playstore";


  @Override
  public String getInstallSource() {
    String installSource = AppConfig.getInstance().getDefaultUtmSource();
    String appVersion = AppConfig.getInstance().getAppVersion();
    String defaultUtmSourceCandidate = String.format(DAILYHUNT_HOME_PATTERN, AppConfig.getInstance()
        .getAppVersion());

    /**
     * If user uninstalls and reinstalls via playstore or updates the preloaded APK, use the
     * XIAOMI_INSTALL_SOURCE_DEFAULT else return the installSource as is.
     */
    if (defaultUtmSourceCandidate.compareToIgnoreCase(installSource) == 0) {
      return isDailyhuntPreloaded() ? XIAOMI_INSTALL_SOURCE_DEFAULT + appVersion : installSource;
    }
    return installSource;
  }

  private boolean isDailyhuntPreloaded() {
    boolean result;
    try {
      Class<?> miui = Class.forName("miui.os.MiuiInit");
      Method method = miui.getMethod("isPreinstalledPackage", String.class);
      result = (Boolean) method.invoke(null, AppConfig.getInstance().getPackageName());
    } catch (Throwable e) {
      Logger.caughtException(e);
      return false;
    }
    return result;
  }
}
