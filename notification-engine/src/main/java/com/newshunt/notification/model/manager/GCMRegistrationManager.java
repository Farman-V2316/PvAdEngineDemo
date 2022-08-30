/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.manager;

import androidx.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.ExponentialRetryHelper;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.info.DeviceInfoHelper;
import com.newshunt.common.helper.preference.AppCredentialPreference;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.notification.analytics.NhGCMRegistrationAnalyticsUtility;
import com.newshunt.notification.analytics.NhRegistrationDestination;
import com.newshunt.notification.analytics.NhRegistrationEventStatus;
import com.newshunt.notification.model.service.NotificationService;
import com.newshunt.dataentity.notification.util.NotificationConstants;

/**
 * Responsible for registering to GCM and storing gcm registration Id in shared preference.
 *
 * @author santosh.kulkarni
 */
public class GCMRegistrationManager {
  private static GCMRegistrationManager gcmManager;
  private NotificationService listener;
  private String senderId;

  private GCMRegistrationManager(NotificationService listener, String senderId) {
    this.listener = listener;
    this.senderId = senderId;
  }

  public static GCMRegistrationManager getInstance(NotificationService listener,
                                                   @NonNull String senderId) {
    if (gcmManager == null) {
      synchronized (GCMRegistrationManager.class) {
        if (gcmManager == null) {
          gcmManager = new GCMRegistrationManager(listener, senderId);
        }
      }
    }
    return gcmManager;
  }

  public void init() {
    // Call the GCM server for register once per app session
    // Note: Repeated calls will return same old registration id.
    // Reference:
    // https://developers.google.com/android/reference/com/google/android/gms/gcm/GoogleCloudMessaging#register(java.lang.String...)
    // Also no need to check if Google Play Services exists as it is handled with try-catch
    // inside registerInBackground
    registerInBackground();
  }

  /**
   * Registers the application with GCM servers asynchronously.
   * <p/>
   * Stores the registration ID and app versionCode in the application's
   * shared preferences.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void registerInBackground() {

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        ExponentialRetryHelper retryHelper = ExponentialRetryHelper.getRetryHelper(this);
        try {
          String regId = FirebaseInstanceId.getInstance().getToken(NotificationConstants
              .SENDER_ID, NotificationConstants.SCOPE_FCM);
          Logger.d("GCM Registration RegID:", regId);
          if (!CommonUtils.isEmpty(regId)) {

            String storedGcmId = getStoredGcmId();
            if (!regId.equals(storedGcmId)) {
              NhGCMRegistrationAnalyticsUtility.updateGcmIdSentEventReported(false);
            }

            sendRegistrationIdToBackend(regId);
            storeRegistrationIdAndAppVersion(regId);

            if (retryHelper != null) {
              NhGCMRegistrationAnalyticsUtility.registerAttemptEvent(NhRegistrationDestination.GCM,
                  NhRegistrationEventStatus.SUCCESS, Constants.EMPTY_STRING, Constants.EMPTY_STRING,
                  retryHelper.getAttemptNumber());

              retryHelper.onSuccess(NhRegistrationDestination.GCM.toString());
            }
          } else {
            if (retryHelper != null) {
              NhGCMRegistrationAnalyticsUtility.registerAttemptEvent(NhRegistrationDestination.GCM,
                  NhRegistrationEventStatus.FAILURE, Constants.EMPTY_STRING, Constants.NULL,
                  retryHelper.getAttemptNumber());

              retryHelper.onFailure(NhRegistrationDestination.GCM.toString());
            }
          }
        } catch (Exception ex) {
          Logger.d("GCM Registration Error:", ex.getMessage());
          if (retryHelper != null) {
            NhGCMRegistrationAnalyticsUtility.registerAttemptEvent(NhRegistrationDestination.GCM,
                NhRegistrationEventStatus.FAILURE, Constants.EMPTY_STRING, ex.getMessage(),
                retryHelper.getAttemptNumber());

            retryHelper.onFailure(NhRegistrationDestination.GCM.toString());
          }
        }
      }
    };

    ExponentialRetryHelper retryHelper = new ExponentialRetryHelper(runnable,
        Constants.RETRY_INITIAL_INTERVAL, Constants.RETRY_MAX_INTERVAL, Constants.RETRY_MAX_ATTEMPT,
        Constants.RETRY_MULTIPLIER);
    retryHelper.start();
  }

  /**
   * Sends the registration ID to  server over HTTP.
   */
  private void sendRegistrationIdToBackend(final String regId) {
    if (null != listener) {
      listener.registerGCMId(regId);
    }
  }

  /**
   * Stores the registration ID and app versionCode in the application's
   * {@code SharedPreferences}.
   *
   * @param regId registration ID
   */
  private void storeRegistrationIdAndAppVersion(String regId) {
    PreferenceManager.savePreference(AppCredentialPreference.GCM_REG_ID, regId);

    // Though not used, not removing APP_VERSION as could be used for later implementations
    String appVersion = DeviceInfoHelper.getAppVersion();
    PreferenceManager.savePreference(GenericAppStatePreference.APP_VERSION, appVersion);
  }

  private String getStoredGcmId() {
    return PreferenceManager.getPreference(AppCredentialPreference.GCM_REG_ID,
        Constants.EMPTY_STRING);
  }
}
