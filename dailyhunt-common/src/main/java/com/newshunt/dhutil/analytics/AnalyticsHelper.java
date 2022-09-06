/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics;

import androidx.annotation.NonNull;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides helper methods for analytics.
 *
 * @author shreyas.desai
 */
public class AnalyticsHelper {

  public static void logExploreButtonClickEvent(ExploreButtonType buttonType, int position, String
      title) {
    logExploreButtonClickEvent(buttonType, position, title, null);
  }

  public static void logExploreButtonClickEvent(ExploreButtonType buttonType, int position, String
      title, String triggerAction) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.PAGE_VIEW_EVENT, false);

    String builder = buttonType.getType() + Constants.UNDERSCORE_CHARACTER + position +
        Constants.UNDERSCORE_CHARACTER + title;
    map.put(NhAnalyticsAppEventParam.TYPE, builder);

    if (!CommonUtils.isEmpty(triggerAction)) {
      map.put(NhAnalyticsAppEventParam.TRIGGER_ACTION, triggerAction);
    }

    //TODO: PANDA removed
    //AnalyticsClient.log(NhAnalyticsAppEvent.EXPLOREBUTTON_CLICK, NhAnalyticsEventSection.APP, map);
  }

  /**
   * Log the conversion data received in AppsFlyer callback
   *
   * @param conversionData
   */
  public static void logAppsFlyerInstallEvent(final Map<String, String> conversionData) {
    //AnalyticsClient.logDynamic(NhAnalyticsAppEvent.APPSFLYER_INSTALL, NhAnalyticsEventSection.APP,
    //    null, conversionData, false);
  }

  public static void logAppsFlyerInitEvent(){
    //AnalyticsClient.logDynamic(NhAnalyticsAppEvent.APPSFLYER_INIT, NhAnalyticsEventSection.APP,
    //    null, null, false);
  }

  public static void logAppsFlyerInitFailure(final Map<String, String> params) {
    //AnalyticsClient.logDynamic(NhAnalyticsAppEvent.APPSFLYER_FAILURE, NhAnalyticsEventSection.APP,
    //    null, params, false);
  }

  public static void logAppsFlyerDevErrorEvent(HashMap<String, String> params) {
    //AnalyticsClient.logDynamic(NhAnalyticsDevEvent.DEV_CUSTOM_ERROR, NhAnalyticsEventSection.APP,
    //        null, params, false);
  }

  public static PageReferrer getCurrentAppSectionPageReferrer() {
    UserAppSection currentAppSection = AppUserPreferenceUtils.getPreviousAppSection();
    if (currentAppSection == null) {
      return null;
    }

    RunTimeReferrer runTimeReferrer =
        new RunTimeReferrer(currentAppSection.getType().getName(), null);
    String referrerId = CommonUtils.isEmpty(currentAppSection.getAppSectionEntityKey())?
        currentAppSection.getId() : currentAppSection.getAppSectionEntityKey();
    return new PageReferrer(runTimeReferrer, referrerId);
  }

  public static void logDisplayThemeEvent(String displayTheme, String prevTheme, String newTheme,PageReferrer referrer){
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.DISPLAY_THEME,displayTheme);
    map.put(NhAnalyticsAppEventParam.NEW_MODE,newTheme);
    map.put(NhAnalyticsAppEventParam.OLD_MODE,prevTheme);
    //AnalyticsClient.log(NhAnalyticsAppEvent.DISPLAY_THEME_CHANGED, NhAnalyticsEventSection.APP, map,referrer);
  }
}
