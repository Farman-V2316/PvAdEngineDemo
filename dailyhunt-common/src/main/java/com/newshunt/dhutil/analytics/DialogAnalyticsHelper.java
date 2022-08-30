/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.analytics;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.DialogBoxType;
import com.newshunt.analytics.entity.NhAnalyticsDialogEvent;
import com.newshunt.analytics.entity.NhAnalyticsDialogEventParam;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.model.entity.MemberRole;
import com.newshunt.dhutil.helper.RateUsConfigCheckHelper;
import com.newshunt.helper.SearchAnalyticsHelper;
import com.newshunt.news.analytics.NhAnalyticsAppState;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shashikiran.nr on 7/1/2016.
 */
public class DialogAnalyticsHelper {

  public static final String DIALOG_ACTION_OK = "Ok";
  public static final String DIALOG_ACTION_CANCEL = "Cancel";
  public static final String DIALOG_ACTION_DISMISS = "Dismiss";

  /**
   * Update the page referrer.
   *
   * @param referrer Referrer object.
   */
  private static void updateReferrer(NhAnalyticsReferrer referrer) {
    NhAnalyticsAppState.getInstance().setReferrer(referrer);
  }

  public static void triggerRateUsAnalyticsEvent(String trigger_action, String action_on_dialog,
                                                 NhAnalyticsReferrer referrer,
                                                 NhAnalyticsEventSection section,
                                                 NhAnalyticsDialogEvent event) {
    deployRateUsActionEvent(action_on_dialog, trigger_action, referrer, section, event, -1);
  }

  public static void triggerRateUsAnalyticsEvent(String trigger_action, String action_on_dialog,
                                                 NhAnalyticsReferrer referrer,
                                                 NhAnalyticsEventSection section,
                                                 NhAnalyticsDialogEvent event,
                                                 float rating) {
    deployRateUsActionEvent(action_on_dialog, trigger_action, referrer, section, event, rating);
  }

  /**
   * Method to trigger dialog view event for Astro
   *
   * @param section - Section of the Application
   */
  public static void deployAstroViewedEvent(NhAnalyticsEventSection section,
                                            DialogBoxType dialogBoxType) {
    Map paramsMap = new HashMap();
    if (dialogBoxType != null) {
      paramsMap.put(NhAnalyticsDialogEventParam.TYPE, dialogBoxType.getType());
    }
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_VIEWED, section, paramsMap);
  }

  /**
   * Method to trigger dialog action event for Astro
   *
   * @param section        - Section of the Application,
   * @param trigger_action - Action trigger by the user.
   */
  public static void deployAstroActionEvent(NhAnalyticsEventSection section,
                                            String trigger_action, DialogBoxType dialogBoxType) {
    Map paramsMap = new HashMap();

    if (dialogBoxType != null) {
      paramsMap.put(NhAnalyticsDialogEventParam.TYPE, dialogBoxType.getType());
    }
    paramsMap.put(NhAnalyticsDialogEventParam.TRIGGER_ACTION, trigger_action);
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_ACTION, section, paramsMap);
  }

  public static void deployRateUsViewedEvent(String trigger_action, NhAnalyticsReferrer referrer,
                                             NhAnalyticsEventSection section) {
    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    if (CommonUtils.isEmpty(trigger_action)) {
      trigger_action = Constants.EMPTY_STRING;
    }
    paramsMap.put(NhAnalyticsDialogEventParam.TYPE, DialogBoxType.RATEUS.getType());
    paramsMap.put(NhAnalyticsDialogEventParam.TRIGGER_ACTION, trigger_action);
    updateReferrer(referrer);
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_VIEWED, section, paramsMap);
  }

  public static void logDialogBoxViewedEvent(DialogBoxType type, PageReferrer referrer,
                                             NhAnalyticsEventSection section,
                                             MemberRole userProfile) {
    if(section == null) {
      section = NhAnalyticsEventSection.NEWS;
    }
    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    if (type != null) {
      paramsMap.put(NhAnalyticsDialogEventParam.TYPE, type.getType());
    }
    if (userProfile != null) {
      paramsMap.put(NhAnalyticsDialogEventParam.USER_PROFILE, userProfile.name());
    }
    SearchAnalyticsHelper.addSearchParams(section, paramsMap);
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_VIEWED, section,
        paramsMap, referrer);
  }

  public static void logDialogBoxActionEvent(DialogBoxType type, PageReferrer referrer, String action,
                                             NhAnalyticsEventSection section,
                                             MemberRole userProfile) {
    logDialogBoxActionEvent(type.getType(), referrer, action, section, userProfile);
  }

  public static void logDialogBoxActionEvent(String type, PageReferrer referrer, String action,
                                             NhAnalyticsEventSection section,
                                             MemberRole userProfile) {
    if(section == null) {
      section = NhAnalyticsEventSection.NEWS;
    }
    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    if (type != null) {
      paramsMap.put(NhAnalyticsDialogEventParam.TYPE, type);
    }
    if (userProfile != null) {
      paramsMap.put(NhAnalyticsDialogEventParam.USER_PROFILE, userProfile.name());
    }
    if (!CommonUtils.isEmpty(action)) {
      paramsMap.put(NhAnalyticsDialogEventParam.ACTION, action);
    }
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_ACTION, section,
        paramsMap, referrer);
  }

  private static void deployRateUsActionEvent(String action_on_dialog, String trigger_action,
                                              NhAnalyticsReferrer referrer,
                                              NhAnalyticsEventSection section,
                                              NhAnalyticsDialogEvent event, float rating) {
    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    if (CommonUtils.isEmpty(trigger_action)) {
      trigger_action = Constants.EMPTY_STRING;
    }
    if (rating >= 0) {
      paramsMap.put(NhAnalyticsDialogEventParam.RATING, rating);
    }
    paramsMap.put(NhAnalyticsDialogEventParam.TYPE, DialogBoxType.RATEUS.getType());
    paramsMap.put(NhAnalyticsDialogEventParam.ACTION, action_on_dialog);
    paramsMap.put(NhAnalyticsDialogEventParam.NEVERSHOW, RateUsConfigCheckHelper
        .isNeverShowChecked());
    paramsMap.put(NhAnalyticsDialogEventParam.TRIGGER_ACTION, trigger_action);
    updateReferrer(referrer);
    AnalyticsClient.log(event, section, paramsMap);
  }

  public static void deployDialogUsActionEvent(String action_on_dialog,
                                               DialogBoxType type,
                                               PageReferrer referrer,
                                               NhAnalyticsEventSection section,
                                               boolean isNeverShow) {
    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    paramsMap.put(NhAnalyticsDialogEventParam.TYPE, type.getType());
    paramsMap.put(NhAnalyticsDialogEventParam.ACTION, action_on_dialog);
    paramsMap.put(NhAnalyticsDialogEventParam.NEVERSHOW, isNeverShow);
    deployDialogUsActionEvent(paramsMap, referrer, section);
  }

  public static void deployDialogUsActionEvent(
      Map<NhAnalyticsEventParam, Object> paramsMap,
      PageReferrer referrer,
      NhAnalyticsEventSection section) {
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_ACTION, section, paramsMap, referrer);
  }

  public static void logAdjunctDialogBoxViewedEvent(PageReferrer referrer,
                                                    NhAnalyticsEventSection section,String adjLang) {

    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    paramsMap.put(NhAnalyticsAppEventParam.ADJUNCT_LANGUAGE,adjLang);
    paramsMap.put(NhAnalyticsAppEventParam.TYPE,Constants.ADJUNCT_STORY_DETAIL_POPUP);
    if(section == null) {
      section = NhAnalyticsEventSection.NEWS;
    }
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_VIEWED, section,
            paramsMap, referrer);
  }

  public static void logAdjunctDialogBoxActionEvent(PageReferrer referrer,
                                                    NhAnalyticsEventSection section,String adjLang,String action) {

    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    paramsMap.put(NhAnalyticsAppEventParam.ADJUNCT_LANGUAGE,adjLang);
    paramsMap.put(NhAnalyticsAppEventParam.TYPE,Constants.ADJUNCT_STORY_DETAIL_POPUP);
    paramsMap.put(NhAnalyticsAppEventParam.ACTION_TYPE,action);
    if(section == null) {
      section = NhAnalyticsEventSection.NEWS;
    }
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_ACTION, section,
            paramsMap, referrer);
  }

  public static void logLinkedInDialogBoxViewedEvent(PageReferrer referrer) {

    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    paramsMap.put(NhAnalyticsAppEventParam.TYPE,Constants.MODIFY_DEFAULT_SHARING_APP);
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_VIEWED, NhAnalyticsEventSection.APP,
            paramsMap, referrer);
  }

  public static void logLinkedInDialogBoxActionEvent(PageReferrer referrer,String action) {

    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    paramsMap.put(NhAnalyticsAppEventParam.TYPE,Constants.MODIFY_DEFAULT_SHARING_APP);
    paramsMap.put(NhAnalyticsAppEventParam.ACTION,action);
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_ACTION, NhAnalyticsEventSection.APP,
            paramsMap, referrer);
  }

}
