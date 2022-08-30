/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.PasswordEncryption;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.info.LocationInfoHelper;
import com.newshunt.common.helper.preference.AppBackUpPreferences;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.AppInstallType;
import com.newshunt.dataentity.common.model.entity.identifier.UniqueIdentifier;
import com.newshunt.dataentity.common.model.entity.status.ClientInfo;
import com.newshunt.dataentity.common.model.entity.status.ConnectionInfo;
import com.newshunt.dataentity.common.model.entity.status.LocationInfo;
import com.newshunt.dataentity.dhutil.model.entity.status.CurrentClientInfo;
import com.newshunt.dataentity.dhutil.model.entity.status.Version;
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dhutil.helper.preference.NhAppStatePreference;
import com.newshunt.dhutil.helper.preference.UserDetailPreference;

/**
 * Provides current client info details.
 *
 * @author shreyas.desai
 */
public class CurrentClientInfoHelper {

  public static CurrentClientInfo createCurrentClientInfo(Context context,
                                                          boolean isUpgradeUser,
                                                          boolean isRegistration,
                                                          Version version) {
    return createCurrentClientInfo(context, isUpgradeUser, false,
        isRegistration, version);
  }

  public static CurrentClientInfo createCurrentClientInfo(
      Context context, boolean isUpgradeUser, boolean appOpenEvent,
      boolean isRegistration, Version version) {

    ClientInfo clientInfo = ClientInfoHelper.getClientInfo();
    ConnectionInfo connectionInfo = ConnectionInfoHelper.getConnectionInfo();
    LocationInfo locationInfo = LocationInfoHelper.getLocationInfo(false);
    CurrentClientInfo currentClientInfo = new CurrentClientInfo(
        clientInfo, locationInfo, connectionInfo);
    currentClientInfo.setAppOpenEvent(appOpenEvent);
    currentClientInfo.setVersion(version);
    String androidId = ClientInfoHelper.getAndroidId();
    String encryptedAndroidId = Constants.EMPTY_STRING;
    try {
      encryptedAndroidId = PasswordEncryption.encrypt(androidId);
    } catch (Exception e) {
      Logger.caughtException(e);
    }

    currentClientInfo.setAndroidId(encryptedAndroidId);

    String referrer = getReferrerString();
    currentClientInfo.setReferrer(referrer);

    currentClientInfo.setPackageName(context.getPackageName());

    if (isUpgradeUser) {
      // now we are using the news launch count instead of headlines launch count to determine
      // whether to launch the newspapers tab or headlines tab.
      currentClientInfo.setHeadlineViews(AppUserPreferenceUtils.getNewsLaunchCount());

      Integer headlinesStoryViewCount =
          PreferenceManager.getPreference(UserDetailPreference.HEADLINES_STORY_VIEW_COUNT, 0);
      currentClientInfo.setHeadlineStoryClicks(headlinesStoryViewCount);
    }

    /**
     * i) Registration && It is NH -> DH upgrade -- Make headlines and story clicks count as 0
     -- THis is done as Registration API is hit before the SQL DB migration
     -- Server requires this flag to be present , so that nh -> dh upgrade logic can be done.
     * ii) Not Registration , Always Pass the IS_DH_2_DH_REINSTALL flag , that came in the response
     */
    boolean isNh2DhUpgrade =
        PreferenceManager.containsPreference(NhAppStatePreference.SHOW_AIRTEL_AFRICA);
    if (isRegistration) {
      if (isNh2DhUpgrade) {
        currentClientInfo.setHeadlineViews(0);
        currentClientInfo.setHeadlineStoryClicks(0);
      }
      PackageManager pkgMgr = CommonUtils.getApplication().getPackageManager();
      if (pkgMgr != null) {
        currentClientInfo.setAppInstaller(
            pkgMgr.getInstallerPackageName(AppConfig.getInstance().getPackageName()));
      }
    } else {
      currentClientInfo.setDh2DhReInstall(
          PreferenceManager.getPreference(AppStatePreference.IS_DH_2_DH_REINSTALL, false));
    }

    if (isRegistration) {
      if (isNh2DhUpgrade) {
        currentClientInfo.setInstallType(AppInstallType.UPGRADE_NH_TO_DH.name());
      } else {
        currentClientInfo.setInstallType(AppInstallType.INSTALL.name());
      }
    } else if (PreferenceManager.getPreference(AppStatePreference.SEND_INSTALL_TYPE_AS_UPGRADE, false)) {
      currentClientInfo.setInstallType(AppInstallType.UPGRADE_DH_TO_DH.name());
    }

    //Requirement from Server , to pass mime types
    currentClientInfo.setMimeTypes(Constants.MIME_TYPES_SUPPORTED);

    /**
     * Logic to send the flag , "isEditionConfirmed" only when
     * When it is after Registration , followed first Handshake
     * i.e Install /Reinstall / NH --> DH case only
     */
    if (PreferenceManager.getPreference(AppStatePreference.TO_SEND_EDITION_CONFIRMATION, false)) {
      currentClientInfo.setIsEditionConfirmed(Boolean.TRUE);
    }

    if (isRegistration) {
      currentClientInfo.setUserData(
          PreferenceManager.getPreference(AppBackUpPreferences.BACKUP_USER_DATA, Constants.EMPTY_STRING));
    }

    currentClientInfo.setAutoPlayUserPreference(AutoPlayHelper.getAutoPlayPreference());
    //Add the acquisition referrers only in handshake, if needed
    if (PreferenceManager.getPreference(AppStatePreference.SEND_ACQ_PARAMS_HANDSHAKE, true) || isRegistration) {
      String appsFlyerReferrer =
          PreferenceManager.getPreference(AppStatePreference.INSTALL_APPSFLYER_REFERRER,
              Constants.EMPTY_STRING);
      String firebaseReferrer =
          PreferenceManager.getPreference(AppStatePreference.INSTALL_FIREBASE_REFERRER,
              Constants.EMPTY_STRING);
      String facebookReferrer =
          PreferenceManager.getPreference(AppStatePreference.ACQUISITION_CAMPAIGN_PARAMS,
              Constants.EMPTY_STRING);
      currentClientInfo.addAcquisitionReferrer(Constants.ACQ_REF_UTM_SOURCE, getInstallSource());
      currentClientInfo.addAcquisitionReferrer(Constants.ACQ_GOOG_REFERRER, getReferrerString());
      if (!CommonUtils.isEmpty(firebaseReferrer)) {
        currentClientInfo.addAcquisitionReferrer(Constants.ACQ_FIREBASE_REFERRER, firebaseReferrer);
      }
      if (!CommonUtils.isEmpty(appsFlyerReferrer)) {
        currentClientInfo.addAcquisitionReferrer(Constants.ACQ_APPSFLYER_REFERRER,
            appsFlyerReferrer);
      }
      if (!CommonUtils.isEmpty(facebookReferrer)) {
        currentClientInfo.addAcquisitionReferrer(Constants.ACQ_FB_REFERRER, facebookReferrer);
      }
    }
    return currentClientInfo;
  }

  public static String getReferrerString() {
    String installReferrer = PreferenceManager.getPreference(AppStatePreference.INSTALL_REFERRER,
        Constants.EMPTY_STRING);
    if (!DataUtil.isEmpty(installReferrer)) {
      return installReferrer;
    }

    return getInstallSource();
  }

  public static String getInstallSource() {
    PreloadInfoProvider provider = PreloadInfoProviderFactory.create();
    return "utm_source=" + (provider != null ? provider.getInstallSource() : AppConfig.getInstance()
        .getDefaultUtmSource());
  }

  public static UniqueIdentifier getDiffUniqueIdentifierData(String plainUniqueIdentifierString,
                                                             UniqueIdentifier newUid) {
    //If null then create instance and send.
    if (TextUtils.isEmpty(plainUniqueIdentifierString)) {
      return newUid;
    }
    try {
      UniqueIdentifier oldPlainUid = new Gson().fromJson(plainUniqueIdentifierString,
          UniqueIdentifier.class);
      UniqueIdentifier newPlainUId = ClientInfoHelper.getUnEncryptedUniqueIdentifier();
      //If not same then send
      if (!oldPlainUid.equals(newPlainUId)) {
        return ClientInfoHelper.getDiff(oldPlainUid, newPlainUId, newUid);
      }
      return null;
    } catch (Throwable e) {
      Logger.caughtException(e);
      return newUid;
    }
  }
}
