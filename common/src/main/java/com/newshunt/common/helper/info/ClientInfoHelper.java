/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.info;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.PasswordEncryption;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.util.R;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.identifier.SimInfo;
import com.newshunt.dataentity.common.model.entity.identifier.TelephonyInfo;
import com.newshunt.dataentity.common.model.entity.identifier.UniqueIdentifier;
import com.newshunt.dataentity.common.model.entity.status.ClientInfo;
import com.newshunt.dataentity.common.model.entity.status.DeviceInfo;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

/**
 * Provides method to retrieve all client related information.
 *
 * @author shreyas.desai
 */
public class ClientInfoHelper {
  private static final int VERSION_NAME_LENGTH_LIMIT = 20;

  public static ClientInfo getClientInfo() {
    ClientInfo clientInfo = new ClientInfo();

    DeviceInfo deviceInfo = DeviceInfoHelper.getDeviceInfo();

    // Because server has set the limit of VERSION_NAME_LENGTH_LIMIT chars on client version
    String clientVer = AppConfig.getInstance().getAppVersion();
    if (!CommonUtils.isEmpty(clientVer) && clientVer.length() > VERSION_NAME_LENGTH_LIMIT) {
      clientVer = clientVer.substring(0, VERSION_NAME_LENGTH_LIMIT);
    }

    clientInfo.setAppVersion(clientVer);
    clientInfo.setDevice(AppConfig.getInstance().getClient());
    clientInfo.setHeight(Math.round(deviceInfo.getHeight()));
    clientInfo.setWidth(Math.round(deviceInfo.getWidth()));
    clientInfo.setOsVersion(deviceInfo.getOsVersion());
    try {
      clientInfo.setUdid(PasswordEncryption.encrypt(deviceInfo.getDeviceId()));
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    clientInfo.setBrand(deviceInfo.getBrand());
    try {
      clientInfo.setAndroidId(PasswordEncryption.encrypt(getAndroidId()));
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    try {
      clientInfo.setGaid(PasswordEncryption.encrypt(ClientInfoHelper.getGoogleAdId()));
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    clientInfo.setGaidOptOutStatus(ClientInfoHelper.getGaidOptOutStatus());
    clientInfo.setModel(deviceInfo.getModel());
    clientInfo.setManufacturer(deviceInfo.getManufacturer());

    String primaryLanguage = AppUserPreferenceUtils.getUserPrimaryLanguage();
    clientInfo.setPrimaryLanguage(primaryLanguage);

    String secondaryLanguages = AppUserPreferenceUtils.getUserSecondaryLanguages();
    clientInfo.setSecondaryLanguages(secondaryLanguages);

    String clientId = getClientId();
    clientInfo.setClientId(clientId);

    clientInfo.setUserId(AppUserPreferenceUtils.getUserId());

    String defaultNotificationLang = getDefaultNotificationLanguage();
    clientInfo.setDefaultNotificationLang(defaultNotificationLang);

    final String edition = AppUserPreferenceUtils.getEdition();
    clientInfo.setEdition(edition);

    final String appLanguage = AppUserPreferenceUtils.getUserNavigationLanguage();
    clientInfo.setAppLanguage(appLanguage);

    return clientInfo;
  }

  @NonNull
  public static String getClientId() {
    String clientId = AppUserPreferenceUtils.getClientId();
    if(!CommonUtils.isEmpty(clientId)){
      return clientId;
    }
    return getClientGeneratedClientId();
  }

  /**
   *
   * @return null if no-reg
   *
   */
  public static String getServerConfirmedClientId() {
    return AppUserPreferenceUtils.getClientId();
  }

  @NonNull
  public static String getClientGeneratedClientId() {
    return UserPreferenceUtil.getClientGeneratedClientId();
  }

  public static String getAndroidId() {
    Context context = CommonUtils.getApplication();
    return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
  }

  public static String getGoogleAdId() {
    return PreferenceManager.getString(Constants.ADD_ID);
  }

  public static void setGoogleAdId(final String googleAdId) {
    PreferenceManager.saveString(Constants.ADD_ID, googleAdId);
  }

  public static boolean getGaidOptOutStatus() {
    return PreferenceManager.getBoolean(Constants.GAID_OPT_OUT_STATUS, false);
  }

  public static void setGaidOptOutStatus(final boolean optOutStatus) {
    PreferenceManager.saveBoolean(Constants.GAID_OPT_OUT_STATUS, optOutStatus);
  }

  /**
   * Get the wifi address of Wifi
   *
   * @return
   */
  public static String getWifiMacAddress() {
    return Constants.EMPTY_STRING;
  }

  /**
   * This function gets the default locale of the device and if it matches one of the locales
   * supported by our app, then it returns that locale. Otherwise it returns "en".
   *
   * @return
   */
  public static String getDefaultNotificationLanguage() {

    String defaultLanguage = "en";
    //Took the language codes from values folder.
    String[] appSupportedLanguages = CommonUtils.getStringArray(R.array.languages_supported);

    Locale locale = Locale.getDefault();
    if (locale == null || appSupportedLanguages == null) {
      return defaultLanguage;
    }

    String langCode = locale.getLanguage();
    List userLanguageList = new ArrayList(Arrays.asList(appSupportedLanguages));
    if (!CommonUtils.isEmpty(langCode) && userLanguageList.contains(langCode)) {
      return langCode;
    }
    return defaultLanguage;
  }

  /**
   * Creates a @UniqueIdentifier object
   *
   * @return
   */
  public static UniqueIdentifier getUniqueIdentifier() {
    UniqueIdentifier uniqueIdentifier = new UniqueIdentifier();

    try {
      uniqueIdentifier.setAdId(PasswordEncryption.encrypt(getGoogleAdId()));
    } catch (Exception e) {
      uniqueIdentifier.setAdId(null);
    }


    try {
      uniqueIdentifier.setAndroidId(PasswordEncryption.encrypt(getAndroidId()));
    } catch (Exception e) {
      uniqueIdentifier.setAndroidId(null);
    }

    uniqueIdentifier.setSimInfos(new TelephonyInfo().getSimInfoList());

    try {
      uniqueIdentifier.setBuildId(PasswordEncryption.encrypt(Build.ID));
    } catch (Exception e) {
      uniqueIdentifier.setBuildId(null);
    }

    uniqueIdentifier.setWifiMacAddress(Constants.EMPTY_STRING);
    return uniqueIdentifier;
  }
  /**
   * Creates a @UniqueIdentifier object
   *
   * @return
   */
  public static UniqueIdentifier getUnEncryptedUniqueIdentifier() {
    UniqueIdentifier uniqueIdentifier = new UniqueIdentifier();

    try {
      uniqueIdentifier.setAdId(getGoogleAdId());
    } catch (Exception e) {
      uniqueIdentifier.setAdId(null);
    }


    try {
      uniqueIdentifier.setAndroidId(getAndroidId());
    } catch (Exception e) {
      uniqueIdentifier.setAndroidId(null);
    }

    uniqueIdentifier.setSimInfos(new TelephonyInfo().getSimInfoList());

    try {
      uniqueIdentifier.setBuildId(Build.ID);
    } catch (Exception e) {
      uniqueIdentifier.setBuildId(null);
    }

    uniqueIdentifier.setWifiMacAddress(Constants.EMPTY_STRING);
    return uniqueIdentifier;
  }

  public static UniqueIdentifier getDiff(UniqueIdentifier oldPlainUid, UniqueIdentifier newPlainUid,
                                         UniqueIdentifier newUid) {
    UniqueIdentifier diff = new UniqueIdentifier();
    if (!CommonUtils.equals(oldPlainUid.getAdId(), newPlainUid.getAdId())) {
      try {
        diff.setAdId(newUid.getAdId());
      } catch (Exception e) {
        diff.setAdId(null);
      }
    }

    if (!CommonUtils.equals(oldPlainUid.getWifiMacAddress(), newPlainUid.getWifiMacAddress())) {
      diff.setWifiMacAddress(newUid.getWifiMacAddress());
    }

    if (!CommonUtils.equals(oldPlainUid.getBuildSerialNumber(), newPlainUid.getBuildSerialNumber())) {
      try {
        diff.setBuildSerialNumber(newUid.getBuildSerialNumber());
      } catch (Exception e) {
        diff.setBuildSerialNumber(null);
      }
    }

    if (!CommonUtils.equals(oldPlainUid.getAndroidId(), newPlainUid.getAndroidId())) {
      try {
        diff.setAndroidId(newUid.getAndroidId());
      } catch (Exception e) {
        diff.setAndroidId(null);
      }
    }

    if (!CommonUtils.equals(oldPlainUid.getBuildId(), newPlainUid.getBuildId())) {
      diff.setBuildId(newUid.getBuildId());
    }

    List<SimInfo> oldSimInfos = oldPlainUid.getSimInfos();
    List<SimInfo> newSimInfos = newPlainUid.getSimInfos();
    if (oldSimInfos.size() != newSimInfos.size()) {
      diff.setSimInfos(newSimInfos);
    }

    for (int i = 0; i < oldSimInfos.size() && i < newSimInfos.size(); i++) {
      SimInfo simInfo1 = oldSimInfos.get(i);
      SimInfo simInfo2 = newSimInfos.get(i);
      if (!simInfo1.equals(simInfo2)) {
        diff.setSimInfos(newSimInfos);
        break;
      }
    }

    return diff;
  }

  /**
   * Compresses the given string using gzip
   *
   * @param string
   * @return
   * @throws IOException
   */
  public static String compressString(String string) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
    GZIPOutputStream gos = new GZIPOutputStream(os);
    gos.write(string.getBytes());
    gos.close();
    String compressed = os.toString();
    os.close();
    return compressed;
  }

  public static String getAppVersion() {
    // Because server has set the limit of VERSION_NAME_LENGTH_LIMIT chars on client version
    String clientVer = AppConfig.getInstance().getAppVersion();
    if (!CommonUtils.isEmpty(clientVer) && clientVer.length() > VERSION_NAME_LENGTH_LIMIT) {
      clientVer = clientVer.substring(0, VERSION_NAME_LENGTH_LIMIT);
    }
    return clientVer;
  }
}
