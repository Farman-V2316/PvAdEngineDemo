/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.analytics;

import android.app.Activity;
import android.util.Pair;

import com.newshunt.dataentity.common.pages.PageEntity;
import com.newshunt.dataentity.news.analytics.NewsReferrer;
import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.analytics.NhAnalyticsUtility;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.info.DeviceInfoHelper;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.TabEntity;
import com.newshunt.common.view.DbgCode;
import com.newshunt.dataentity.dhutil.analytics.NhAnalyticsCommonEventParam;
import com.newshunt.dataentity.news.analytics.StorySupplementSectionPosition;
import com.newshunt.dhutil.analytics.AnalyticsHelper;
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener;
import com.newshunt.news.util.NewsConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides utility methods for event logging.
 *
 * @author shreyas.desai
 */
public class NewsAnalyticsHelper {

  public static NhAnalyticsEventSection getReferrerEventSectionFromActivity(Activity activity) {
    return activity instanceof ReferrerProviderlistener? getReferrerEventSectionFrom(
        (ReferrerProviderlistener) activity) : NhAnalyticsEventSection.NEWS;
  }

  public static NhAnalyticsEventSection getReferrerEventSectionFrom(ReferrerProviderlistener
                                                     referrerProviderlistener) {
    return referrerProviderlistener == null? NhAnalyticsEventSection.NEWS :
        referrerProviderlistener.getReferrerEventSection();
  }

  public static void logErrorScreenViewEvent(NhAnalyticsUtility.ErrorResponseCode errorResponseCode,
                                             NhAnalyticsUtility.ErrorViewType viewType,
                                             NhAnalyticsUtility.ErrorPageType pageType,
                                             String httpErrorCode, String httpErrorMessage,
                                             String errorUrl, TabEntity tabEntity,
                                             PageReferrer referrer, int tabIndex,
                                             DbgCode dbgCode) {

    if (errorResponseCode == NhAnalyticsUtility.ErrorResponseCode.NO_INTERNET ||
        errorResponseCode == NhAnalyticsUtility.ErrorResponseCode.SERVER_ERROR) {
      // in case of no internet and server error no need to fire the event.
      return;
    }

    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsNewsEventParam.RESPONSE_CODE, errorResponseCode.getValue());
    eventParams.put(NhAnalyticsNewsEventParam.VIEW_TYPE, viewType.getViewType());
    eventParams.put(NhAnalyticsNewsEventParam.PAGE_TYPE, pageType.getPageType());
    eventParams.put(NhAnalyticsNewsEventParam.HTTP_ERROR_CODE, httpErrorCode);
    eventParams.put(NhAnalyticsNewsEventParam.HTTP_ERROR_MESSAGE, httpErrorMessage);
    eventParams.put(NhAnalyticsCommonEventParam.ERROR_URL, errorUrl);

    if (tabEntity != null) {
      eventParams.put(NhAnalyticsNewsEventParam.TABTYPE, CommonUtils.toLowerCase(tabEntity.getTabType()));
      eventParams.put(NhAnalyticsNewsEventParam.TABNAME, tabEntity.getName1());
      eventParams.put(NhAnalyticsNewsEventParam.TABITEM_ID, tabEntity.getTabId());
    }
    eventParams.put(NhAnalyticsNewsEventParam.TABINDEX, tabIndex);
    if(dbgCode != null) {
      eventParams.put(NhAnalyticsNewsEventParam.ERROR_CODE, dbgCode.get());
    }
    AnalyticsClient.log(NhAnalyticsAppEvent.ERRORSCREEN_VIEWED, NhAnalyticsEventSection.NEWS,
        eventParams, referrer);
  }


  public static void logPFPWidgetViewEvent(NhAnalyticsNewsEvent eventName, String sectionName,
                                           String storyId, StorySupplementSectionPosition position,
                                           Map<String, String> experiment,
                                           boolean isAssociation) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    if (isAssociation) {
      map.put(NhAnalyticsNewsEventParam.WIDGET_TYPE, NewsConstants.SIMILAR_VIDEO_SIMILAR);
    } else {
      map.put(NhAnalyticsNewsEventParam.WIDGET_TYPE, sectionName);
    }

    if (position != null) {
      map.put(NhAnalyticsNewsEventParam.WIDGET_PLACEMENT, position.getName());
    }

    AnalyticsClient.logDynamic(eventName, NhAnalyticsEventSection.NEWS,
        map, experiment, new PageReferrer(NewsReferrer.STORY_DETAIL, storyId), false);
  }


  /**
   * Utility function to log news home session end
   */
  public static void logSessionEnd() {
    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsAppEventParam.END_STATE, "app_exit");

    long startTime = PreferenceManager.getPreference(GenericAppStatePreference.APP_START_TIME, 0L);
    if (startTime > 0L) {
      long sessionLengthMillis = System.currentTimeMillis() - startTime;
      eventParams.put(NhAnalyticsNewsEventParam.CATEGORY_ID, sessionLengthMillis);
      PreferenceManager.remove(GenericAppStatePreference.APP_START_TIME);
    }

    long deviceDataConsumedAtSessionStart =
        PreferenceManager.getPreference(GenericAppStatePreference.DEVICE_DATA_CONSUMED, 0L);
    long appDataConsumedAtSessionStart =
        PreferenceManager.getPreference(GenericAppStatePreference.APP_DATA_CONSUMED, 0L);
    PreferenceManager.remove(GenericAppStatePreference.DEVICE_DATA_CONSUMED);
    PreferenceManager.remove(GenericAppStatePreference.APP_DATA_CONSUMED);
    Pair<Long, Long> dataConsumed = DeviceInfoHelper.getDataConsumed();
    if (dataConsumed.first - deviceDataConsumedAtSessionStart > 0 &&
        dataConsumed.second - appDataConsumedAtSessionStart > 0) {
      eventParams.put(NhAnalyticsAppEventParam.USER_SESSION_DATACONSUMED, dataConsumed.first -
          deviceDataConsumedAtSessionStart);
      eventParams.put(NhAnalyticsAppEventParam.DH_SESSION_DATACONSUMED, dataConsumed.second -
          appDataConsumedAtSessionStart);
      eventParams.put(NhAnalyticsAppEventParam.USER_BOOT_DATACONSUMED, dataConsumed.first);
      eventParams.put(NhAnalyticsAppEventParam.DH_BOOT_DATACONSUMED, dataConsumed.second);
    }

    AnalyticsClient.log(NhAnalyticsAppEvent.SESSION_END, NhAnalyticsEventSection.APP, eventParams);
  }

  /**
   * Utility function to log app exit
   */
  public static void logOrganicAppExit() {
    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsAppEventParam.EXIT_TYPE,
        NhAnalyticsUserAction.NORMAL_EXIT);
    eventParams.put(NhAnalyticsAppEventParam.LAST_PAGE, NhAnalyticsEventSection.NEWS.name());
    AnalyticsClient.log(NhAnalyticsAppEvent.APP_EXIT, NhAnalyticsEventSection.APP, eventParams);
  }

  public static void logTabsReorderEvent(List<PageEntity> reorderedTabs) {
    if (CommonUtils.isEmpty(reorderedTabs)) {
      return;
    }

    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();

    StringBuilder builder = new StringBuilder();
    for (PageEntity entity : reorderedTabs) {
      if (entity != null) {
        builder.append(entity.getEntityType())
            .append(Constants.UNDERSCORE_CHARACTER)
            .append(entity.getId())
            .append(Constants.COMMA_CHARACTER);
      }
    }
    builder.setLength(builder.length() - 1);

    paramsMap.put(NhAnalyticsNewsEventParam.TABS_ORDER, builder.toString());
    AnalyticsClient.log( NhAnalyticsNewsEvent.HOMETABS_REORDERED, NhAnalyticsEventSection.NEWS,
        paramsMap, new PageReferrer(NewsReferrer.MANAGE_NEWS_HOME));
  }

  public static void updateAppState(PageReferrer pageReferrer) {
    AnalyticsHelper.updateAppState(pageReferrer);
  }

}
