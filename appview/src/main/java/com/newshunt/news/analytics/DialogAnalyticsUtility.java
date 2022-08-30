/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.news.analytics;

import com.newshunt.dataentity.news.analytics.NewsReferrer;
import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.analytics.entity.NhAnalyticsDialogEvent;
import com.newshunt.analytics.entity.NhAnalyticsDialogEventParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class helps for analyticsEvent over dialog event
 *
 * @author shashikiran.nr on 3/2/2016.
 */
public class DialogAnalyticsUtility {


  private static final String CLICK_NO = "no";
  private static final String CLICK_YES = "yes";

  private static final String IMAGE_ON = "imageon";
  private static final String IMAGE_OFF = "imageoff";

  /**
   * Update the page referrer.
   *
   * @param referrer Referrer object.
   */
  private static void updateReferrer(NhAnalyticsReferrer referrer) {
    NhAnalyticsAppState.getInstance().setReferrer(referrer);
  }

  /**
   * method to post analytics events for hamburger menu click.
   *
   */
  public static void deployAnalyticsEvent(NhAnalyticsDialogEventParam action_taken_key,
                                          String action_taken_value,
                                          NhAnalyticsDialogEventParam dialog_type_key,
                                          String dialog_type_value,
                                          NhAnalyticsDialogEvent dialogEvent) {

    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    if (CommonUtils.isEmpty(action_taken_value)) {
      action_taken_value = Constants.EMPTY_STRING;
    }

    if (CommonUtils.isEmpty(dialog_type_value)) {
      dialog_type_value = Constants.EMPTY_STRING;
    }
    paramsMap.put(action_taken_key, action_taken_value);
    paramsMap.put(dialog_type_key, dialog_type_value);

    updateReferrer(NewsReferrer.NEWS_PAPER);
    AnalyticsClient.log(dialogEvent,
        NhAnalyticsEventSection.NEWS, paramsMap);

  }

  /**
   *
   * helper method to post analytic events for button clicks on litemode dialogs (for both
   * enabling and disabling litemode)
   *
   * @param enableLiteMode  - whether lite-mode is enabled/disabled
   * @param isPosOrNegClick - whether user clicked yes or no
   */
  public static void articleImageOnAndOffEvent(boolean enableLiteMode,boolean isPosOrNegClick){

    String clickAction = isPosOrNegClick ? CLICK_YES : CLICK_NO;
    String dialogType = enableLiteMode ? IMAGE_OFF : IMAGE_ON;

    deployAnalyticsEvent(NhAnalyticsDialogEventParam.ACTION_TAKEN, clickAction,
        NhAnalyticsDialogEventParam.DIALOG_TYPE, dialogType,
        NhAnalyticsDialogEvent.IMAGE_ON_OFF_BOX_CLICKED);
  }
}
