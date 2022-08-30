/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.notification.analytics;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

/**
 * GCM registration analytics utility
 *
 * @author arun.babu
 */
public class NhGCMRegistrationAnalyticsUtility {

  public static void registerAttemptEvent(NhRegistrationDestination destination,
                                          NhRegistrationEventStatus status, String responseCode,
                                          String message, int retryCount) {
    if (!gcmIdSentEventReported()) {
      Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
      paramsMap.put(NhGCMRegistrationEventParam.ITEM_DESTINATION, destination);
      paramsMap.put(NhGCMRegistrationEventParam.ITEM_STATUS, status);
      paramsMap.put(NhGCMRegistrationEventParam.ITEM_RESPONSE_STATUS, responseCode);
      paramsMap.put(NhGCMRegistrationEventParam.ITEM_MESSAGE, message);
      paramsMap.put(NhGCMRegistrationEventParam.ITEM_ATTEMPT_NUMBER, retryCount);

      AnalyticsClient.log(NhRegistrationEvent.REGISTRATION_ATTEMPT, NhAnalyticsEventSection.APP,
          paramsMap);
    }
  }

  public static void updateGcmIdSentEventReported(boolean sent) {
    PreferenceManager.savePreference(GenericAppStatePreference.GCM_ID_SENT_EVENT_REPORTED, sent);
  }

  private static boolean gcmIdSentEventReported() {
    return PreferenceManager.getPreference(GenericAppStatePreference.GCM_ID_SENT_EVENT_REPORTED,
        false);
  }
}
