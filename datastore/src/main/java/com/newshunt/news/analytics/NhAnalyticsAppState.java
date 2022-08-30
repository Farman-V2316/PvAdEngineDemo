/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.analytics;

import android.content.Context;

import com.newshunt.common.helper.cookie.CustomCookieManager;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.section.NhAnalyticsSectionEndAction;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.info.DebugHeaderProvider;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains app state for the analytics events to be sent.
 *
 * @author shreyas.desai
 */
public class NhAnalyticsAppState implements Serializable {

  private static final long serialVersionUID = 8504872845249298351L;
  private static NhAnalyticsAppState instance;

  private NhAnalyticsReferrer sessionSource;
  private String sourceId;
  private NhAnalyticsReferrer eventAttribution;
  private String eventAttributionId;
  private NhAnalyticsReferrer referrer;
  private String referrerId;
  private String subReferrerId; // For category
  private NhAnalyticsUserAction action;
  private NhAnalyticsEventSection startSection = NhAnalyticsEventSection.UNKNOWN;
  private NhAnalyticsEventSection previousSection = NhAnalyticsEventSection.UNKNOWN;
  private NhAnalyticsSectionEndAction sectionEndAction = NhAnalyticsSectionEndAction.UNKNOWN;
  private Map<String, String> globalExperimentParams = new HashMap<>();

  private NhAnalyticsAppState() {
    updateGlobalExperimentParams();
  }

  public static NhAnalyticsAppState getInstance() {
    if (instance == null) {
      synchronized (NhAnalyticsAppState.class) {
        if (instance == null) {
          instance = new NhAnalyticsAppState();
        }
      }
    }
    return instance;
  }

  public static void saveAppState(Context context, NhAnalyticsReferrer currentPage) {
    PreferenceManager.savePreference(GenericAppStatePreference.APP_CURRENT_PAGE,
        currentPage.getReferrerName());
    PreferenceManager.savePreference(GenericAppStatePreference.APP_CURRENT_TIME,
        System.currentTimeMillis());
  }

  public static void addReferrerParams(PageReferrer referrer,
                                       Map<NhAnalyticsEventParam, Object> eventParamsMap) {
    if (referrer == null) {
      return;
    }
    NhAnalyticsAppState.getInstance().setReferrer(referrer.getReferrer(), referrer.getId(),
        referrer.getSubId());

    if (referrer.getReferrer() != null) {
      eventParamsMap.put(NhAnalyticsAppEventParam.REFERRER,
          referrer.getReferrer().getReferrerName());
    }
    eventParamsMap.put(NhAnalyticsAppEventParam.REFERRER_ID, referrer.getId());
    eventParamsMap.put(NhAnalyticsAppEventParam.SUB_REFERRER_ID, referrer.getSubId());
    eventParamsMap.put(NhAnalyticsAppEventParam.REFERRER_ACTION, referrer.getReferrerAction());
  }

  public NhAnalyticsReferrer getSessionSource() {
    return sessionSource;
  }

  public NhAnalyticsAppState setSessionSource(NhAnalyticsReferrer sessionSource) {
    this.sessionSource = sessionSource;
    DebugHeaderProvider.INSTANCE.setSessionSource(sessionSource.getReferrerName());
    return this;
  }

  public String getSourceId() {
    return sourceId;
  }

  public NhAnalyticsAppState setSourceId(String sourceId) {
    this.sourceId = sourceId;
    return this;
  }

  public NhAnalyticsReferrer getEventAttribution() {
    return eventAttribution;
  }

  public NhAnalyticsAppState setEventAttribution(NhAnalyticsReferrer eventAttribution) {
    this.eventAttribution = eventAttribution;
    return this;
  }

  public String getEventAttributionId() {
    return eventAttributionId;
  }

  public NhAnalyticsAppState setEventAttributionId(String eventAttributionId) {
    this.eventAttributionId = eventAttributionId;
    return this;
  }

  public NhAnalyticsReferrer getReferrer() {
    return referrer;
  }

  public void setReferrer(PageReferrer pageReferrer) {
    setReferrer(pageReferrer.getReferrer(), pageReferrer.getId(), pageReferrer.getSubId());
  }

  public NhAnalyticsAppState setReferrer(NhAnalyticsReferrer referrer) {
    return setReferrer(referrer, null, null);
  }

  public NhAnalyticsAppState setReferrer(NhAnalyticsReferrer referrer, String referrerId,
                                         String subReferrerId) {
    this.referrer = referrer;
    this.referrerId = referrerId;
    this.subReferrerId = subReferrerId;
    return this;
  }

  public String getReferrerId() {
    return referrerId;
  }

  public NhAnalyticsAppState setReferrerId(String referrerId) {
    this.referrerId = referrerId;
    return this;
  }

  public NhAnalyticsUserAction getAction() {
    return action;
  }

  public NhAnalyticsAppState setAction(NhAnalyticsUserAction action) {
    this.action = action;
    return this;
  }

  public String getSubReferrerId() {
    return subReferrerId;
  }

  public NhAnalyticsAppState setSubReferrerId(String subReferrerId) {
    this.subReferrerId = subReferrerId;
    return this;
  }

  public Map<NhAnalyticsEventParam, Object> getStateParams(boolean isOverrideReferrerAction) {
    Map<NhAnalyticsEventParam, Object> eventParamsMap = new HashMap<>();

    if (sessionSource != null) {
      eventParamsMap.put(NhAnalyticsAppEventParam.SESSION_SOURCE, sessionSource.getReferrerName());
      eventParamsMap.put(NhAnalyticsAppEventParam.SESSION_SOURCE_ID, sourceId);
    }

    if (eventAttribution != null) {
      eventParamsMap.put(NhAnalyticsAppEventParam.EVENT_ATTRIBUTION,
          eventAttribution.getReferrerName());
    }

    if (eventAttributionId != null) {
      eventParamsMap.put(NhAnalyticsAppEventParam.EVENT_ATTRIBUTION_ID, eventAttributionId);
    }

    if (referrer != null) {
      eventParamsMap.put(NhAnalyticsAppEventParam.REFERRER, referrer.getReferrerName());
    }

    if (referrer != null && !DataUtil.isEmpty(referrerId)) {
      eventParamsMap.put(NhAnalyticsAppEventParam.REFERRER_ID, referrerId);
    }

    if (referrer != null && !DataUtil.isEmpty(subReferrerId)) {
      eventParamsMap.put(NhAnalyticsAppEventParam.SUB_REFERRER_ID, subReferrerId);
    }

    if (action != null && isOverrideReferrerAction) {
      eventParamsMap.put(NhAnalyticsAppEventParam.REFERRER_ACTION, action);
    }

    eventParamsMap.put(NhAnalyticsAppEventParam.USER_OS_PLATFORM, AppConfig.getInstance().getClient());
    eventParamsMap.put(NhAnalyticsAppEventParam.TIME, String.valueOf(System.currentTimeMillis()));
    String partnerRef = DebugHeaderProvider.INSTANCE.getPartnerRef();
    if (!CommonUtils.isEmpty(partnerRef)) {
      eventParamsMap.put(NhAnalyticsAppEventParam.PARTNER_REF, partnerRef);
    }
    eventParamsMap.put(NhAnalyticsAppEventParam.FG_SESSION_ID, PreferenceManager.getPreference(AppStatePreference.FG_SESSION_ID, ""));
    eventParamsMap.put(NhAnalyticsAppEventParam.FG_SESSION_COUNT,
        PreferenceManager.getPreference(AppStatePreference.TOTAL_FOREGROUND_SESSION, 0L));
    eventParamsMap.put(NhAnalyticsAppEventParam.FG_SESSION_DURATION,
        PreferenceManager.getPreference(AppStatePreference.TOTAL_FOREGROUND_DURATION, 0L));
    eventParamsMap.put(NhAnalyticsAppEventParam.FTD_SESSION_COUNT,
        PreferenceManager.getPreference(AppStatePreference.FTD_SESSION_COUNT, 0L));
    eventParamsMap.put(NhAnalyticsAppEventParam.FTD_SESSION_TIME,
        PreferenceManager.getPreference(AppStatePreference.FTD_SESSION_TIME, 0L));

    return eventParamsMap;
  }

  public NhAnalyticsEventSection getStartSection() {
    return startSection;
  }

  public void setStartSection(NhAnalyticsEventSection startSection) {
    if(this.startSection != NhAnalyticsEventSection.UNKNOWN) {
      this.previousSection = this.startSection;
    }
    this.startSection = startSection;
  }

  public NhAnalyticsSectionEndAction getSectionEndAction() {
    return sectionEndAction;
  }

  public void setSectionEndAction(NhAnalyticsSectionEndAction sectionEndAction) {
    this.sectionEndAction = sectionEndAction;
  }

  public void updateGlobalExperimentParams() {
    Map<String, String> params = JsonUtils.fromJson(PreferenceManager.getPreference
            (GenericAppStatePreference.GLOBAL_EXPERIMENT_PARAMS, Constants.EMPTY_STRING),
        globalExperimentParams.getClass());
    if (params != null) {
      globalExperimentParams = params;
    }
  }

  public Map<String, String> getGlobalExperimentParams() {
    String value = CustomCookieManager.getCookieValue(NewsBaseUrlContainer.getApplicationUrl(),Constants.COOKIE_COMMON_DH);
    if (!CommonUtils.isEmpty(value)) {
      globalExperimentParams.put(Constants.COOKIE_COMMON_DH, value);
    }
    return globalExperimentParams;
  }

  public NhAnalyticsEventSection getPreviousSection() {
    return previousSection;
  }

}
