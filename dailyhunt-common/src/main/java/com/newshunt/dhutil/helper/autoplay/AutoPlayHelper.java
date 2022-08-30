/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
*/

package com.newshunt.dhutil.helper.autoplay;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.IntDef;

import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper;
import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.analytics.AutoPlayEventParam;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.sdk.network.connection.ConnectionType;
import com.newshunt.sdk.network.internal.NetworkSDKUtils;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.newshunt.dhutil.helper.autoplay.AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_ALWAYS;
import static com.newshunt.dhutil.helper.autoplay.AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_OFF;
import static com.newshunt.dhutil.helper.autoplay.AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_WIFI;
import static com.newshunt.dhutil.helper.autoplay.AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_DATA;

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
   * This method saves the user's preference to SharedPreferences and also updates in-memory
   * cache of the preference. This is saved in RAM to avoid reading from preferences too many times.
   * @param preference user's preference
   */
  public static void saveAutoPlayPreference(@AutoPlayPreference final int preference) {
    switch (preference) {
      case AUTO_PLAY_ALWAYS:
      case AUTO_PLAY_DATA:
      case AUTO_PLAY_OFF:
      case AUTO_PLAY_WIFI:
        autoPlayPreference.set(preference);
        PreferenceManager.savePreference(GenericAppStatePreference.AUTO_PLAY_PREFERENCE,
            preference);
        BusProvider.postOnUIBus(new AutoPlayPreferenceChangedEvent(preference));
      default:
        break;
    }
  }

  /**
   * This method launches the Auto play settings activity
   * @param activity Activity context
   */
  public static void launchAutoPlaySettingsActivity(Activity activity) {
    Intent intent = new Intent();
    intent.setAction(DailyhuntConstants.AUTO_PLAY_SETTINGS_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    activity.startActivity(intent);
  }

  /**
   * This method returns the user's autoplay preference
   * @return
   */
  public static @AutoPlayPreference int getAutoPlayPreference() {
    return autoPlayPreference.get();
  }

  /**
   * This method saves a preference whether or not Auto play must be displayed in settings
   * activity. This method is to save the B.E handshake response.
   * @param showAutoPlaySettings boolean to indicate the choice
   */
  public static void saveShowAutoPlaySetting(final boolean showAutoPlaySettings) {
    PreferenceManager.savePreference(GenericAppStatePreference.SHOW_AUTO_PLAY_SETTINGS,
        showAutoPlaySettings);
  }

  /**
   * This method tells whether Autoplay settings must be displayed on Settings Screen
   * @return true if autoplay must be shown. False otherwise.
   */
  public static boolean shouldShowAutoPlaySettings() {
    return PreferenceManager.getPreference(GenericAppStatePreference.SHOW_AUTO_PLAY_SETTINGS, true);
  }

  /**
   * This method tells whether user has deliberately turned OFF auto play
   * @return
   */
  public static boolean isAutoPlayDisabledByUser() {
    return autoPlayPreference.get() == AUTO_PLAY_OFF;
  }

  /**
   * Helper method to log Analytics event for video auto play preference
   * @param oldValue Old user preference
   * @param newValue New user perference
   * @param referrer Referrer
   * @param section Section
   * @param eventType Type parameter for the event
   */
  public static void logAutoPlayToggleEvent(@AutoPlayPreference final int oldValue,
                                            @AutoPlayPreference final int newValue,
                                            final PageReferrer referrer,
                                            final NhAnalyticsEventSection section,
                                            final String eventType) {
    Map<NhAnalyticsEventParam, Object> propertiesMap = new HashMap<>();
    propertiesMap.put(AutoPlayEventParam.TYPE, eventType);
    propertiesMap.put(AutoPlayEventParam.PREVIOUS_STATE, mapUserPrefToAnalyticsString(oldValue));
    propertiesMap.put(AutoPlayEventParam.NEW_STATE, mapUserPrefToAnalyticsString(newValue));
    AnalyticsClient.log(NhAnalyticsAppEvent.AUTOPLAY_MODE_CHANGED, section, propertiesMap,
        referrer);
  }

  private static String mapUserPrefToAnalyticsString(final @AutoPlayPreference int pref) {
    switch (pref) {
      case AUTO_PLAY_ALWAYS:
        return ANALYTICS_AUTO_PLAY_ALWAYS;
      case AUTO_PLAY_OFF:
        return ANALYTICS_AUTO_PLAY_OFF;
      case AUTO_PLAY_WIFI:
        return ANALYTICS_AUTO_PLAY_WIFI;
      case AUTO_PLAY_DATA:
        return ANALYTICS_AUTO_PLAY_DATA;
      default:
        return Constants.EMPTY_STRING;
    }
  }

  /**
   * Event to indicate a change in auto play settings
   */
  public static class AutoPlayPreferenceChangedEvent {
    private @AutoPlayPreference int preference;

    public AutoPlayPreferenceChangedEvent(int preference) {
      this.preference = preference;
    }
    public @AutoPlayPreference int getAutoPlayPreference() {
      return preference;
    }
  }
}
