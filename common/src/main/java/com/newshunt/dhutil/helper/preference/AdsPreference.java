/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.preference;

import com.newshunt.common.helper.preference.PreferenceType;
import com.newshunt.common.helper.preference.SavedPreference;

/**
 * Preferences related to ads.
 *
 * @author neeraj.kumar
 */
public enum AdsPreference implements SavedPreference {
  // App launch count
  APP_LAUNCH_COUNT("app_launch_count_ads", PreferenceType.ADS),

  // persist swipe count across sessions
  SAVED_SWIPE_COUNT("savedSwipeCount", PreferenceType.ADS),

  // minimum session counts after which we start persisting swipe count across sessions
  MIN_SESSIONS_TO_PERSIST_SWIPE_COUNT("sessionCountToPersistSwipeCount", PreferenceType.ADS),

  //cacheLevel for different zones and on good and fast  networks.
  STORY_AD_CACHE_LEVEL_GOOD("storyPageCacheLevelGood", PreferenceType.ADS),
  SUPPLEMENT_AD_CACHE_LEVEL_GOOD("supplementAdCacheLevelGood", PreferenceType.ADS),
  CARD_P1_AD_CACHE_LEVEL_GOOD("cardP1CacheLevelGood", PreferenceType.ADS),
  MASTHEAD_AD_CACHE_LEVEL_GOOD("mastHeadCacheLevelGood", PreferenceType.ADS),
  DHTV_MH_AD_CACHE_LEVEL_GOOD("dhtvMHCacheLevelGood", PreferenceType.ADS),
  CARD_PP1_AD_CACHE_LEVEL_GOOD("cardPP1CacheLevelGood", PreferenceType.ADS),

  //cacheLevel for different zones and on average networks.
  STORY_AD_CACHE_LEVEL_AVERAGE("storyPageCacheLevelAverage", PreferenceType.ADS),
  SUPPLEMENT_AD_CACHE_LEVEL_AVERAGE("supplementAdCacheLevelAverage", PreferenceType.ADS),
  CARD_P1_AD_CACHE_LEVEL_AVERAGE("cardP1CacheLevelAverage", PreferenceType.ADS),
  MASTHEAD_AD_CACHE_LEVEL_AVERAGE("mastHeadCacheLevelAverage", PreferenceType.ADS),
  DHTV_MH_AD_CACHE_LEVEL_AVERAGE("dhtvMHCacheLevelAverage", PreferenceType.ADS),
  CARD_PP1_AD_CACHE_LEVEL_AVERAGE("cardPP1CacheLevelAverage", PreferenceType.ADS),

  //cacheLevel for different zones and on slow networks.
  STORY_AD_CACHE_LEVEL_SLOW("storyPageCacheLevelSlow", PreferenceType.ADS),
  SUPPLEMENT_AD_CACHE_LEVEL_SLOW("supplementAdCacheLevelSlow", PreferenceType.ADS),
  CARD_P1_AD_CACHE_LEVEL_SLOW("cardP1CacheLevelSlow", PreferenceType.ADS),
  MASTHEAD_AD_CACHE_LEVEL_SLOW("mastHeadCacheLevelSlow", PreferenceType.ADS),
  DHTV_MH_AD_CACHE_LEVEL_SLOW("dhtvMHCacheLevelSlow", PreferenceType.ADS),
  CARD_PP1_AD_CACHE_LEVEL_SLOW("cardP1CacheLevelSlow", PreferenceType.ADS),

  //Ads handshake response json string
  ADS_HANDSHAKE_RESPONSE_JSON("adsHandshakeResponseJsonString", PreferenceType.ADS),

  // Version for current ad profile.
  ADS_CONFIG_VERSION("adsConfigVersion", PreferenceType.ADS),

  // Version for current ad profile.
  ADS_CONTEXT_HANDSHAKE_VERSION("adsContextHandshakeVersion", PreferenceType.ADS),
  // Skip text for current buzz video ad.
  ADS_SKIP_TEXT("adsSkipText", PreferenceType.ADS),

  // Flag to freeze user operation on buzz video ads.
  ADS_FREEZE_USER_OPERATION_FLAG("adsFreezeUserOperationFlag", PreferenceType.ADS),

  VIDEO_AD_DISTANCE("videoAdDistance", PreferenceType.ADS),

  VIDEO_INITIAL_AD_OFFSET("videoInitialAdOffset", PreferenceType.ADS),

  CARD_P0_REFRESH_ENABLED("cardP0Refresh", PreferenceType.ADS),

  CARD_P1_NO_FILL_RETRY_DISTANCE("cardP1NoFillRetryDistance", PreferenceType.ADS),

  BATTERY_USAGE_INFO("batteryUsageInfo", PreferenceType.ADS),

  HEADSET_PLUGGED_INFO("headsetPluggedInfo", PreferenceType.ADS),

  DEVICE_DATA_POST_TS("deviceDataPostTS", PreferenceType.ADS),

  OMID_SERVICE_JS("omidServiceJs", PreferenceType.ADS),
  OMID_SESSION_CLIENT_JS("omidSessionClientJs", PreferenceType.ADS),

  ADS_STATS_FIRST_TS("adsStatsFirstTS", PreferenceType.ADS),

  ADS_STATS_TOTAL_SESSIONS("adsStatsTotalSessions", PreferenceType.ADS),
  ADS_STATS_TOTAL_ADS_SESSIONS("adsStatsTotalAdSessions", PreferenceType.ADS),

  ADS_STATS_TOTAL_ADS("adsStatsTotalAds", PreferenceType.ADS),
  ADS_STATS_TOTAL_MASTHEAD_ADS("adsStatsTotalMastheadAds", PreferenceType.ADS),
  REPORT_ADS_MENU("reportadsmenu", PreferenceType.ADS),

  IMMERSIVE_VIEW_TRANSITION_SPAN("immersiveViewConfigs", PreferenceType.ADS),
  IMMERSIVE_VIEW_DISTANCE("immersiveViewDistance", PreferenceType.ADS),
  IMMERSIVE_VIEW_REFRESH_TIME("immersiveViewRefreshTime", PreferenceType.ADS),

  AD_CAMPAIGN_FETCH_LAST_TS("adCampaignFetchLastTs", PreferenceType.ADS),
  AD_CAMPAIGN_LAST_MODIFIED_TS("adCampaignLastModifiedTs", PreferenceType.ADS),

  AD_ZIPPED_HTML_CACHE_COUNT("zippedHtmlAdCacheCount" , PreferenceType.ADS),

  ENABLE_TEST_MODE_ALL_ADS("enable_test_mode_all_ads", PreferenceType.ADS),

  AD_EVERGREEN_FETCH_LAST_TS("adEvergreenFetchLastTs", PreferenceType.ADS),
  AD_EVERGREEN_API_E_TAG("adEvergreenApiEtag", PreferenceType.ADS),

  AD_REGULAR_SPLASH_CONFIG("regularSplashAdConfig", PreferenceType.ADS);

  private final String name;
  private final PreferenceType preferenceType;

  AdsPreference(String name, PreferenceType preferenceType) {
    this.name = name;
    this.preferenceType = preferenceType;
  }

  @Override
  public PreferenceType getPreferenceType() {
    return preferenceType;
  }

  @Override
  public String getName() {
    return name;
  }
}
