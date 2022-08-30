/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.info;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Pair;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.preference.AppCredentialPreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.status.DeviceInfo;

/**
 * Provides methods to access device info.
 *
 * @author shreyas.desai
 */
public class DeviceInfoHelper {

  private static int uid = -1;

  private static DeviceInfo deviceInfoInstance;

  public static DeviceInfo getDeviceInfo() {

    if (deviceInfoInstance == null) {
      synchronized (DeviceInfoHelper.class) {
        if (deviceInfoInstance == null) {
          deviceInfoInstance = new DeviceInfo();
          deviceInfoInstance.setWidth(CommonUtils.getDeviceScreenWidth());
          deviceInfoInstance.setHeight(CommonUtils.getDeviceScreenHeight());

          deviceInfoInstance.setDeviceId(getDeviceId());

          String osVersion = Build.VERSION.RELEASE;
          deviceInfoInstance.setOsVersion(osVersion);

          String appVersion = getAppVersion();
          deviceInfoInstance.setAppVersion(appVersion);
          deviceInfoInstance.setClient(AppConfig.getInstance().getClient());

          deviceInfoInstance.setDensity(CommonUtils.getDeviceDensity());

          deviceInfoInstance.setModel(Build.MODEL);
          deviceInfoInstance.setManufacturer(Build.MANUFACTURER);
        }
      }
    }

    return deviceInfoInstance;
  }

  /**
   * Method to return the Unique Device Id.
   * <p/>
   * Logic : a -->b-->c-->d-->e-->Save on the file
   * a) Read Shared pref first.
   * b) Read from the Credentials file [Second param]
   * c) Read tel manager for IMEI number
   * d) Read Sim Serial number
   * e) Read Subscriber Id
   *
   * @return -- Unique Id
   */
  private static String getDeviceId() {

    String deviceId =
        PreferenceManager.getPreference(AppCredentialPreference.DEVICE_ID, Constants.EMPTY_STRING);
    if (!DataUtil.isEmpty(deviceId)) {
      return deviceId;
    }

    Pair<String, String> credentials = CredentialsHelper.getCredentialsFromFile();
    if (credentials != null && !DataUtil.isEmpty(credentials.second)) {
      // Let's save so next time we return the same from pref mgr directly to avoid file access
      PreferenceManager.savePreference(AppCredentialPreference.DEVICE_ID, credentials.second);
      return credentials.second;
    }

    // Finally will get android hex for device. This is not foolproof but
    // we need something other than null
    if (deviceId == null || deviceId.isEmpty()) {
      deviceId = Settings.Secure.getString(CommonUtils.getApplication().getContentResolver(),
          Settings.Secure.ANDROID_ID);
    }

    // Let's save so next time we return the same.
    PreferenceManager.savePreference(AppCredentialPreference.DEVICE_ID, deviceId);
    CredentialsHelper.saveUdIdOnFile(deviceId);

    return deviceId;
  }

  /**
   * @return Network operate currently the device is connected to.
   */
  public static String getOperatorName(Context context) {
    TelephonyManager telephonyManager =
        ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
    return telephonyManager.getNetworkOperatorName();
  }

  /**
   * @return Pair of device level and app level data consumed.
   */
  public static Pair<Long, Long> getDataConsumed() {
    Long deviceDataConsumed = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
    if (DeviceInfoHelper.uid == -1) {
      DeviceInfoHelper.uid = CommonUtils.getApplication().getApplicationInfo().uid;
    }
    Long appDataConsumed = TrafficStats.getUidRxBytes(DeviceInfoHelper.uid) +
        TrafficStats.getUidTxBytes(DeviceInfoHelper.uid);
    return new Pair(deviceDataConsumed, appDataConsumed);
  }

  /**
   * @return Application's version code from the {@code PackageManager}.
   */
  public static String getAppVersion() {
    return AppConfig.getInstance().getAppVersion();
  }
}
