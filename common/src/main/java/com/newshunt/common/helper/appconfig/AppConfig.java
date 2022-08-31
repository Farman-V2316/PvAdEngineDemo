/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.appconfig;

/**
 * Utility Class responsible to instantiate the Application Configuration from App module
 *
 * @author ranjith.suda
 */
public class AppConfig {

  private static AppConfig instance;
  private final AppConfigBuilder appConfigBuilder;

  private AppConfig(AppConfigBuilder appConfigBuilder) {
    this.appConfigBuilder = appConfigBuilder;
  }

  public static AppConfig getInstance() {
    if (instance == null) {
      return new AppConfig(new AppConfigBuilder().setPackageName("com.newsdistill.pvadenginedemo"));
    } else {
      return instance;
    }
  }

  public static AppConfig createInstance(AppConfigBuilder appConfigBuilder) {
    if (instance == null) {
      synchronized (AppConfig.class) {
        if (instance == null) {
          instance = new AppConfig(appConfigBuilder);
        }
      }
    }
    return instance;
  }

  public String getAppVersion() {
    return appConfigBuilder.appVersion;
  }

  public String getDefaultUtmSource() {
    return appConfigBuilder.defaultUtmSource;
  }

  public boolean isLoggerEnabled() {
    return appConfigBuilder.loggerEnabled;
  }

  public boolean isVariantBuild() {
    return appConfigBuilder.variantBuild;
  }

  public boolean isAppendIdToBookDesc() {
    return appConfigBuilder.appendIdToBookDesc;
  }

  public int getAppVersionCode() {
    return appConfigBuilder.appVersionCode;
  }

  public String getAdServerEndPoint() {
    return appConfigBuilder.adServerEndPoint;
  }

  public String getNewsAPIEndPoint() {
    return appConfigBuilder.newsAPIEndPoint;
  }

  public String getNewsAPISecureEndPoint() {
    return appConfigBuilder.newsAPISecureEndPoint;
  }

  public String getNewsAPIRelativeEndPoint() {
    return appConfigBuilder.newsAPIRelativeEndPoint;
  }

  public String getAnalyticsBeaconEndPoint() {
    return appConfigBuilder.analyticsBeaconEndPoint;
  }

  public String getPremiumAdEndPoint() {
    return appConfigBuilder.premiumAdEndPoint;
  }

  public String getSsoAPIEndPoint() {
    return appConfigBuilder.ssoAPIEndPoint;
  }

  public String getPackageName() {
    return appConfigBuilder.packageName;
  }

  public String getSourcePostUrl() {
    return appConfigBuilder.sourcePostUrl;
  }

  public String getReferrerPostUrl() {
    return appConfigBuilder.referrerPostUrl;
  }

  public String getFirebasePostUrl() {
    return appConfigBuilder.firebasePostUrl;
  }

  public String getClientInfoPostUrl() {
    return appConfigBuilder.clientInfoPostUrl;
  }

  public String getFirstPageViewPostUrl() {
    return appConfigBuilder.firstPageViewPostUrl;
  }

  public String getTVBaseUrl() {
    return appConfigBuilder.tvAPIEndPoint;
  }

  public String getLiveTVBaseUrl() {
    return appConfigBuilder.liveTVAPIEndPoint;
  }

  public String getDHTVBaseUrl() {
    return appConfigBuilder.dhtvAPIEndPoint;
  }

  public String getDHTVSearchBaseUrl() {
    return appConfigBuilder.dhtvSearchApiEndPoint;
  }

  public String getDHTVAutoCompleteBaseUrl() {
    return appConfigBuilder.dhtvAutoCompleteApiEndPoint;
  }

  public String getLocoBaseUrl() {
    return appConfigBuilder.dhlocoAPIEndPoint;
  }

  public String getPullNotificationUrl() {
    return appConfigBuilder.pullNotificationEndPoint;
  }

  public int getMinBaseInterval() {
    return appConfigBuilder.minBaseInterval;
  }

  public String getAdsHandshakeUrl() {
    return appConfigBuilder.adsHandshakeEndPoint;
  }

  public String getAppsFlyerPostUrl() {
    return appConfigBuilder.appsFlyerPostUrl;
  }

  public String getFullSyncUrl() {
    return appConfigBuilder.fullSyncUrl;
  }

  public String getSocialUrl() {
    return appConfigBuilder.socialFeaturesUrl;
  }

  public String getSecureSocialUrl() {
    return appConfigBuilder.secureSocialFeaturesUrl;
  }

  public String getLikeUrl() {
    return appConfigBuilder.likeFeaturesUrl;
  }

  public String getViralEndPoint() {
    return appConfigBuilder.vhAPIEndPoint;
  }

  public String getDefaultCampaignParams() { return appConfigBuilder.defaultCampaignParam; }

  public String getSearchEndPoint() {
    return appConfigBuilder.searchEndPoint;
  }

  public String getFollowHomeBaseUrl() {
    return appConfigBuilder.followHomeBaseUrl;
  }

  public String getAutoCompleteEndPoint() {
    return appConfigBuilder.autoCompleteEndPoint;
  }

  public boolean isMonkeyRestricted() {
    return appConfigBuilder.monkeyRestricted;
  }

  public boolean isMultiProcessEnabled() { return appConfigBuilder.multiProcessEnabled; }

  public boolean isAppsFlyerEnabled() {
    return appConfigBuilder.appsFlyerEnabled;
  }
  public boolean isAppIndexingEnabled() {
    return appConfigBuilder.appIndexingEnabled;
  }

  public int getOnboardingDesignVersion() { return appConfigBuilder.onboardingDesignVersion; }

  public boolean isGoBuild() {
    return appConfigBuilder.isGoBuild;
  }

  public String getClient() {
    return AppConfig.getInstance().isGoBuild() ? "android_lite" : "android";
  }

  public String getEnvType() {
    return appConfigBuilder.envType;
  }

  public String getNotificationChannelUrl() {
    return appConfigBuilder.notificationChannelUrl;
  }

  public String getUserServiceBaseUrl() { return  appConfigBuilder.userServiceBaseUrl;}

  public String getUserServiceSecuredBaseUrl() { return  appConfigBuilder.userServiceSecuredBaseUrl;}

  public String getGroupsBaseUrl() { return  appConfigBuilder.groupsBaseUrl;}

  public String getImageUploadBaseUrl() { return  appConfigBuilder.imageUploadBaseUrl;}

  public String getContactSyncBaseUrl() { return appConfigBuilder.contactSyncBaseUrl; }

  public String getPostCreationUrl() { return appConfigBuilder.postCreationBaseUrl;}

  public String getPostDeletionUrl() { return appConfigBuilder.postDeletionBaseUrl;}

  public String getPostReportUrl() { return appConfigBuilder.postReportBaseUrl;}

  public String getOgServiceUrl() { return appConfigBuilder.ogServiceBaseUrl;}

  public String getReportGroupUrl() { return appConfigBuilder.reportGroupUrl;}

  public String getReportMemberUrl() { return appConfigBuilder.reportMemberUrl;}

  public String getReportProfileUrl() { return appConfigBuilder.reportProfileUrl;}

  public String getLocalZoneUrl() { return appConfigBuilder.localZoneUrl;}

}