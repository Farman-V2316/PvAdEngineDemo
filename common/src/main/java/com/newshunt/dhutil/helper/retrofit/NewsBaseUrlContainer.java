/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.retrofit;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.CommonBaseUrls;
import com.newshunt.common.helper.common.CommonBaseUrlsContainer;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.baseurl.BaseUrl;
import com.newshunt.dataentity.dhutil.model.entity.upgrade.Upgrade;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

/**
 * Container for {@link BaseUrl}
 *
 * @author shreyas.desai
 */
public class NewsBaseUrlContainer {
  private static BaseUrl baseUrl;
  private static String defaultNavigationLanguage = "default";
  private static Upgrade upgrade = Upgrade.LATEST;
  private static final String LOG_TAG = "NewsBaseUrlContainer";
  public static final MutableLiveData<NewsBaseUrlInitException> statusEvents = new MutableLiveData<>();
  public static void init() {
    boolean updatedFromSavedUrls = false;

    String baseUrlJson =
        PreferenceManager.getPreference(AppStatePreference.NEWS_BASE_URL, Constants.EMPTY_STRING);
    try {
      if (!DataUtil.isEmpty(baseUrlJson)) {
        Gson gson = new Gson();
        BaseUrl savedBaseUrl = gson.fromJson(baseUrlJson, BaseUrl.class);
        if (savedBaseUrl != null) {
          updatedFromSavedUrls = updateBaseUrls(savedBaseUrl);
        }
      }
    } catch (Throwable e) {
      Logger.e(LOG_TAG, "init exception ", e);
      Logger.caughtException(e);
      PreferenceManager.remove(AppStatePreference.NEWS_BASE_URL);
      updatedFromSavedUrls = false;
      statusEvents.postValue(new NewsBaseUrlInitException("NewsBaseUrlContainer.init", e, baseUrlJson));
    }

    if (!updatedFromSavedUrls) {
      baseUrl = createDefaultBaseUrls();
    }
  }

  /**
   * @return default baseUrl config from build.
   */
  private static BaseUrl createDefaultBaseUrls() {
    updateBaseUrls(new BaseUrl());
    return baseUrl;
  }

  public static boolean updateBaseUrls(BaseUrl updateUrl) {
    if (null == updateUrl) {
      return false;
    }

    if (null == baseUrl) {
      baseUrl = new BaseUrl();
    }

    if (!CommonUtils.isEmpty(updateUrl.getAdvertisementUrl())) {
      baseUrl.setAdvertisementUrl(updateUrl.getAdvertisementUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getAdvertisementUrl())) {
      baseUrl.setAdvertisementUrl(AppConfig.getInstance().getAdServerEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getPremiumAdvertisementUrl())) {
      baseUrl.setPremiumAdvertisementUrl(updateUrl.getPremiumAdvertisementUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getPremiumAdvertisementUrl())) {
      baseUrl.setPremiumAdvertisementUrl(AppConfig.getInstance().getPremiumAdEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getAnalyticsUrl())) {
      baseUrl.setAnalyticsUrl(updateUrl.getAnalyticsUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getAnalyticsUrl())) {
      baseUrl.setAnalyticsUrl(AppConfig.getInstance().getAnalyticsBeaconEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getApplicationUrl())) {
      baseUrl.setApplicationUrl(updateUrl.getApplicationUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getApplicationUrl())) {
      baseUrl.setApplicationUrl(AppConfig.getInstance().getNewsAPIEndPoint());
    }

    if(!CommonUtils.isEmpty(updateUrl.getApplicationSecureUrl())) {
      baseUrl.setApplicationSecureUrl(updateUrl.getApplicationSecureUrl());
    } else if(CommonUtils.isEmpty(baseUrl.getApplicationSecureUrl())) {
      baseUrl.setApplicationSecureUrl(AppConfig.getInstance().getNewsAPISecureEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getApplicationRelativeUrl())) {
      baseUrl.setApplicationRelativeUrl(updateUrl.getApplicationRelativeUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getApplicationRelativeUrl())) {
      baseUrl.setApplicationRelativeUrl(AppConfig.getInstance().getNewsAPIRelativeEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getAppsOnDeviceUrl())) {
      baseUrl.setAppsOnDeviceUrl(updateUrl.getAppsOnDeviceUrl());
    } else if (!CommonUtils.isEmpty(updateUrl.getApplicationUrl())) {
      baseUrl.setAppsOnDeviceUrl(updateUrl.getApplicationUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getAppsOnDeviceUrl())) {
      baseUrl.setAppsOnDeviceUrl(AppConfig.getInstance().getNewsAPIEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getFeedbackUrl())) {
      baseUrl.setFeedbackUrl(updateUrl.getFeedbackUrl());
    } else if (!CommonUtils.isEmpty(updateUrl.getApplicationUrl())) {
      baseUrl.setFeedbackUrl(updateUrl.getApplicationUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getFeedbackUrl())) {
      baseUrl.setFeedbackUrl(AppConfig.getInstance().getNewsAPIEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getNotificationNewsUrl())) {
      baseUrl.setNotificationNewsUrl(updateUrl.getNotificationNewsUrl());
    } else if (!CommonUtils.isEmpty(updateUrl.getApplicationUrl())) {
      baseUrl.setNotificationNewsUrl(updateUrl.getApplicationUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getNotificationNewsUrl())) {
      baseUrl.setNotificationNewsUrl(AppConfig.getInstance().getNewsAPIEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getNotificationTriggerUrl())) {
      baseUrl.setNotificationTriggerUrl(updateUrl.getNotificationTriggerUrl());
    } else if (!CommonUtils.isEmpty(updateUrl.getApplicationUrl())) {
      baseUrl.setNotificationTriggerUrl(updateUrl.getApplicationUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getNotificationTriggerUrl())) {
      baseUrl.setNotificationTriggerUrl(AppConfig.getInstance().getNewsAPIEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getWidgetUrl())) {
      baseUrl.setWidgetUrl(updateUrl.getWidgetUrl());
    } else if (!CommonUtils.isEmpty(updateUrl.getApplicationUrl())) {
      baseUrl.setWidgetUrl(updateUrl.getApplicationUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getWidgetUrl())) {
      baseUrl.setWidgetUrl(AppConfig.getInstance().getNewsAPIEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getSourcePostUrl())) {
      baseUrl.setSourcePostUrl(updateUrl.getSourcePostUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getSourcePostUrl())) {
      baseUrl.setSourcePostUrl(AppConfig.getInstance().getSourcePostUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getReferrerPostUrl())) {
      baseUrl.setReferrerPostUrl(updateUrl.getReferrerPostUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getReferrerPostUrl())) {
      baseUrl.setReferrerPostUrl(AppConfig.getInstance().getReferrerPostUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getFirebasePostUrl())) {
      baseUrl.setFirebasePostUrl(updateUrl.getFirebasePostUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getFirebasePostUrl())) {
      baseUrl.setFirebasePostUrl(AppConfig.getInstance().getFirebasePostUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getAppsFlyerPostUrl())) {
      baseUrl.setAppsFlyerPostUrl(updateUrl.getAppsFlyerPostUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getAppsFlyerPostUrl())) {
      baseUrl.setAppsFlyerPostUrl(AppConfig.getInstance().getAppsFlyerPostUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getClientInfoPostUrl())) {
      baseUrl.setClientInfoPostUrl(updateUrl.getClientInfoPostUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getClientInfoPostUrl())) {
      baseUrl.setClientInfoPostUrl(AppConfig.getInstance().getClientInfoPostUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getFirstPageViewPostUrl())) {
      baseUrl.setFirstPageViewPostUrl(updateUrl.getFirstPageViewPostUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getFirstPageViewPostUrl())) {
      baseUrl.setFirstPageViewPostUrl(AppConfig.getInstance().getFirstPageViewPostUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getPullNotificationUrlFromProfileRelease())) {
      baseUrl.setPullNotificationUrlFromProfileRelease(
          updateUrl.getPullNotificationUrlFromProfileRelease());
    } else if (!CommonUtils.isEmpty(updateUrl.getPullNotificationUrl())) {
      baseUrl.setPullNotificationUrlFromProfileRelease(updateUrl.getPullNotificationUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getPullNotificationUrlFromProfileRelease())) {
      baseUrl.setPullNotificationUrlFromProfileRelease(
          AppConfig.getInstance().getPullNotificationUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getAdsHandshakeUrl())) {
      baseUrl.setAdsHandshakeUrl(updateUrl.getAdsHandshakeUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getAdsHandshakeUrl())) {
      baseUrl.setAdsHandshakeUrl(AppConfig.getInstance().getAdsHandshakeUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getFullSyncUrl())) {
      baseUrl.setFullSyncUrl(updateUrl.getFullSyncUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getFullSyncUrl())) {
      baseUrl.setFullSyncUrl(AppConfig.getInstance().getFullSyncUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getSocialUrl())) {
      baseUrl.setSocialUrl(updateUrl.getSocialUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getSocialUrl())) {
      baseUrl.setSocialUrl(AppConfig.getInstance().getSocialUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getSecureSocialUrl())) {
      baseUrl.setSecureSocialUrl(updateUrl.getSecureSocialUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getSecureSocialUrl())) {
      baseUrl.setSecureSocialUrl(AppConfig.getInstance().getSecureSocialUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getLikeUrl())) {
      baseUrl.setLikeUrl(updateUrl.getLikeUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getLikeUrl())) {
      baseUrl.setLikeUrl(AppConfig.getInstance().getLikeUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getViralBaseUrl())) {
      baseUrl.setViralBaseUrl(updateUrl.getViralBaseUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getViralBaseUrl())) {
      baseUrl.setViralBaseUrl(AppConfig.getInstance().getViralEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getSearchBaseUrl())) {
      baseUrl.setSearchBaseUrl(updateUrl.getSearchBaseUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getSearchBaseUrl())) {
      baseUrl.setSearchBaseUrl(AppConfig.getInstance().getSearchEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getAutoCompleteBaseUrl())) {
      baseUrl.setAutoCompleteBaseUrl(updateUrl.getAutoCompleteBaseUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getAutoCompleteBaseUrl())) {
      baseUrl.setAutoCompleteBaseUrl(AppConfig.getInstance().getAutoCompleteEndPoint());
    }

    if (!CommonUtils.isEmpty(updateUrl.getSecureSocialUrl())) {
      baseUrl.setSecureSocialUrl(updateUrl.getSecureSocialUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getSecureSocialUrl())) {
      baseUrl.setSecureSocialUrl(AppConfig.getInstance().getSecureSocialUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getFollowHomeBaseUrl())) {
      baseUrl.setFollowHomeBaseUrl(updateUrl.getFollowHomeBaseUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getFollowHomeBaseUrl())) {
      baseUrl.setFollowHomeBaseUrl(AppConfig.getInstance().getFollowHomeBaseUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getNotificationChannelUrl())) {
      baseUrl.setNotificationChannelUrl(updateUrl.getNotificationChannelUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getNotificationChannelUrl())) {
      baseUrl.setNotificationChannelUrl(AppConfig.getInstance().getNotificationChannelUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getUserServiceBaseUrl())) {
      baseUrl.setUserServiceBaseUrl(updateUrl.getUserServiceBaseUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getUserServiceBaseUrl())) {
      baseUrl.setUserServiceBaseUrl(AppConfig.getInstance().getUserServiceBaseUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getUserServiceSecuredBaseUrl())) {
      baseUrl.setUserServiceSecuredBaseUrl(updateUrl.getUserServiceSecuredBaseUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getUserServiceSecuredBaseUrl())) {
      baseUrl.setUserServiceSecuredBaseUrl(AppConfig.getInstance().getUserServiceSecuredBaseUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getGroupsBaseUrl())) {
      baseUrl.setGroupsBaseUrl(updateUrl.getGroupsBaseUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getGroupsBaseUrl())) {
      baseUrl.setGroupsBaseUrl(AppConfig.getInstance().getGroupsBaseUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getImageUploadBaseUrl())) {
      baseUrl.setImageUploadBaseUrl(updateUrl.getImageUploadBaseUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getImageUploadBaseUrl())) {
      baseUrl.setImageUploadBaseUrl(AppConfig.getInstance().getImageUploadBaseUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getPostCreationUrl())) {
      baseUrl.setPostCreationUrl(updateUrl.getPostCreationUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getPostCreationUrl())) {
      baseUrl.setPostCreationUrl(AppConfig.getInstance().getPostCreationUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getPostDeletionUrl())) {
      baseUrl.setPostDeletionUrl(updateUrl.getPostDeletionUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getPostDeletionUrl())) {
      baseUrl.setPostDeletionUrl(AppConfig.getInstance().getPostDeletionUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getPostReportUrl())) {
      baseUrl.setPostReportUrl(updateUrl.getPostReportUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getPostReportUrl())) {
      baseUrl.setPostReportUrl(AppConfig.getInstance().getPostReportUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getOgServiceUrl())) {
      baseUrl.setOgServiceUrl(updateUrl.getOgServiceUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getOgServiceUrl())) {
      baseUrl.setOgServiceUrl(AppConfig.getInstance().getOgServiceUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getContactSyncBaseUrl())) {
      baseUrl.setContactSyncBaseUrl(updateUrl.getContactSyncBaseUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getContactSyncBaseUrl())) {
      baseUrl.setContactSyncBaseUrl(AppConfig.getInstance().getContactSyncBaseUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getReportGroupUrl())) {
      baseUrl.setReportGroupUrl(updateUrl.getReportGroupUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getReportGroupUrl())) {
      baseUrl.setReportGroupUrl(AppConfig.getInstance().getReportGroupUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getReportMemberUrl())) {
      baseUrl.setReportMemberUrl(updateUrl.getReportMemberUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getReportMemberUrl())) {
      baseUrl.setReportMemberUrl(AppConfig.getInstance().getReportMemberUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getReportProfileUrl())) {
      baseUrl.setReportProfileUrl(updateUrl.getReportProfileUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getReportProfileUrl())) {
      baseUrl.setReportProfileUrl(AppConfig.getInstance().getReportProfileUrl());
    }

    if (!CommonUtils.isEmpty(updateUrl.getLocalZoneUrl())) {
      baseUrl.setLocalZoneUrl(updateUrl.getLocalZoneUrl());
    } else if (CommonUtils.isEmpty(baseUrl.getLocalZoneUrl())) {
      baseUrl.setLocalZoneUrl(AppConfig.getInstance().getLocalZoneUrl());
    }

    CommonBaseUrls commonBaseUrls = new CommonBaseUrls(baseUrl.getAnalyticsUrl(),
        baseUrl.getNotificationNewsUrl(), baseUrl.getNotificationTriggerUrl(),
        baseUrl.getPullNotificationUrlFromProfileRelease(), baseUrl.getNotificationChannelUrl());
    updateCommonUrls(commonBaseUrls);
    return true;
  }

  public static String getAdvertisementUrl() {
    return baseUrl.getAdvertisementUrl();
  }

  public static String getPremiumAdvertisementUrl() {
    return baseUrl.getPremiumAdvertisementUrl();
  }

  public static String getApplicationUrl() {
    return baseUrl.getApplicationUrl();
  }

  public static String getFullSyncUrl() {
    return baseUrl.getFullSyncUrl();
  }

  public static String getApplicationSecureUrl() {
    return baseUrl.getApplicationSecureUrl();
  }

  public static String getApplicationRelativeUrl() {
    return baseUrl.getApplicationRelativeUrl();
  }

  public static String getAdsHandshakeUrl() {
    return baseUrl.getAdsHandshakeUrl();
  }

  public static String getSourcePostUrl() {
    return baseUrl.getSourcePostUrl();
  }

  public static String getReferrerPostUrl() {
    return baseUrl.getReferrerPostUrl();
  }

  public static String getFirebasePostUrl() {
    return baseUrl.getFirebasePostUrl();
  }

  public static String getAppsFlyerPostUrl() {
    return baseUrl.getAppsFlyerPostUrl();
  }

  public static String getClientInfoPostUrl() {
    return baseUrl.getClientInfoPostUrl();
  }

  public static String getFirstPageViewPostUrl() {
    return baseUrl.getFirstPageViewPostUrl();
  }

  public static String getSocialFeaturesBaseUrl() {
    return baseUrl.getSocialUrl();
  }

  public static String getSecureSocialFeaturesUrl() {
    return baseUrl.getSecureSocialUrl();
  }

  public static String getLikeFeaturesUrl() {
    return baseUrl.getLikeUrl();
  }

  public static String getSearchBaseUrl() {
    return baseUrl.getSearchBaseUrl();
  }

  public static String getAppsOnDeviceUrl() {
    return baseUrl.getAppsOnDeviceUrl();
  }

  public static String getFollowHomeBaseUrl() {
    return baseUrl.getFollowHomeBaseUrl();
  }

  public static String getAutoCompleteBaseUrl() {
    return baseUrl.getAutoCompleteBaseUrl();
  }

  public static String getViralHuntUrl() {
    return baseUrl.getViralBaseUrl();
  }

  public static String getUserServiceBaseUrl() { return baseUrl.getUserServiceBaseUrl();}

  public static String getUserServiceSecuredBaseUrl() { return baseUrl.getUserServiceSecuredBaseUrl();}

  public static String getGroupsBaseUrl() { return baseUrl.getGroupsBaseUrl();}

  public static String getImageBaseUrl() { return baseUrl.getImageUploadBaseUrl();}

  public static String getContactSyncBaseUrl() { return baseUrl.getContactSyncBaseUrl(); }

  public static String getPostCreationBaseUrl() { return baseUrl.getPostCreationUrl();}

  public static String getPostDeletionBaseUrl() { return baseUrl.getPostDeletionUrl();}

  public static String getPostReportBaseUrl() { return baseUrl.getPostReportUrl();}

  public static String getOgServiceBaseUrl() { return baseUrl.getOgServiceUrl();}

  public static String getDefaultNavigationLanguage() {
    return defaultNavigationLanguage;
  }

  public static String getReportGroupUrl() { return baseUrl.getReportGroupUrl();}

  public static String getReportMemberUrl() { return baseUrl.getReportMemberUrl();}

  public static String getReportProfileUrl() { return baseUrl.getReportProfileUrl();}

  public static void setDefaultNavigationLanguage(String navigationLanguage) {
    if (!DataUtil.isEmpty(navigationLanguage)) {
      defaultNavigationLanguage = navigationLanguage;
    }
  }

  public static Upgrade getUpgrade() {
    return upgrade;
  }

  public static void setUpgrade(Upgrade upgradeStatus) {
    if (upgradeStatus != null) {
      upgrade = upgradeStatus;
    }
  }

  private static void updateCommonUrls(CommonBaseUrls commonBaseUrls) {
    CommonBaseUrlsContainer.createInstance(commonBaseUrls);
  }
}
