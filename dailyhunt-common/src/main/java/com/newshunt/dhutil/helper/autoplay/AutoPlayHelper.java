/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
*/

package com.newshunt.dhutil.helper.autoplay;

import androidx.annotation.IntDef;
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.sdk.network.connection.ConnectionType;
import com.newshunt.sdk.network.internal.NetworkSDKUtils;
import java.lang.annotation.Retention;
import java.util.concurrent.atomic.AtomicInteger;

import static com.newshunt.dhutil.helper.autoplay.AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_ALWAYS;
import static com.newshunt.dhutil.helper.autoplay.AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_DATA;
import static com.newshunt.dhutil.helper.autoplay.AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_OFF;
import static com.newshunt.dhutil.helper.autoplay.AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_WIFI;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * A helper class to handle the Auto play settings, store and retrieve from preferences.
 * Created by srikanth.ramaswamy on 16/11/17.
 */

public class AutoPlayHelper {
  @Retention(SOURCE)
  @IntDef({AUTO_PLAY_WIFI, AUTO_PLAY_OFF, AUTO_PLAY_ALWAYS, AUTO_PLAY_DATA})
  public @interface AutoPlayPreference {
    int AUTO_PLAY_WIFI = 1;
    int AUTO_PLAY_OFF = 2;
    int AUTO_PLAY_ALWAYS = 3;
    int AUTO_PLAY_DATA = 4;

  }

  private static final String ANALYTICS_AUTO_PLAY_WIFI = "on_wifi";
  private static final String ANALYTICS_AUTO_PLAY_ALWAYS = "on_data_wifi";
  private static final String ANALYTICS_AUTO_PLAY_OFF = "never";
  private static final String ANALYTICS_AUTO_PLAY_DATA = "on_data";

  public static final String ANALYTICS_AUTOPLAY_TYPE_ADS = "video_ads";

  private static AtomicInteger autoPlayPreference;

  static {
    autoPlayPreference = new AtomicInteger(PreferenceManager.getPreference
        (GenericAppStatePreference.AUTO_PLAY_PREFERENCE, AUTO_PLAY_ALWAYS));
  }

  /**
   * This method checks the user's preference and the connection type to determine if auto play
   * is allowed at the moment or not.
   * @return true if videos could be autoplayed. False otherwise.
   */
  public static boolean isAutoPlayAllowed() {
    //If user does not want to autoplay, return false irrespective of connection type
    if (autoPlayPreference.get() == AUTO_PLAY_OFF) {
      return false;
    }

    ConnectionType connectionType = ConnectionInfoHelper.getConnectionTypeWithTimeout();
    if (connectionType == null) {
      // If connection info is not available within timeout, use last known value
      connectionType = NetworkSDKUtils.getLastKnownConnectionType();
    }

    //Can't auto play when we have no connection, Check caching too disabled
    if (CommonUtils.equals(connectionType,ConnectionType.NO_CONNECTION) &&
        CacheConfigHelper.INSTANCE.getDisableCache()) {
      return false;
    }

    //If user's preference is to play always, return true irrespective of connection type
    if (autoPlayPreference.get() == AUTO_PLAY_ALWAYS) {
      return true;
    }

    //If user's preference is to play only on wifi and currently connected to mobile data, false
    if (autoPlayPreference.get() == AUTO_PLAY_WIFI && !CommonUtils.equals(ConnectionType.WI_FI,connectionType)) {
      return false;
    }
    //If user's preference is to play only on data and currently connected to wifi, false
    if (autoPlayPreference.get() == AUTO_PLAY_DATA && CommonUtils.equals(ConnectionType.WI_FI,connectionType)) {
      return false;
    }
    //Coming here means, we can auto play. User's preference and connection match
    return true;
  }

  /**
   * This method returns the user's autoplay preference
   * @return
   */
  public static @AutoPlayPreference int getAutoPlayPreference() {
    return autoPlayPreference.get();
  }
}
