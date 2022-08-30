/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.appconfig;

import com.newshunt.common.helper.common.Constants;

/**
 * Utility Class , responsible for building the App Builder Config
 *
 * @author ranjith.suda
 */
public class AppConfigBuilder {

  boolean variantBuild;
  boolean loggerEnabled;
  boolean appendIdToBookDesc = false;
  boolean appIndexingEnabled;
  String defaultUtmSource = Constants.EMPTY_STRING;
  String appVersion = Constants.EMPTY_STRING;
  String adServerEndPoint = Constants.EMPTY_STRING;
  String newsAPIEndPoint = Constants.EMPTY_STRING;
  String newsAPISecureEndPoint = Constants.EMPTY_STRING;
  String newsAPIRelativeEndPoint = Constants.EMPTY_STRING;
  String analyticsBeaconEndPoint = Constants.EMPTY_STRING;
  String premiumAdEndPoint = Constants.EMPTY_STRING;
  String ssoAPIEndPoint = Constants.EMPTY_STRING;
  String sourcePostUrl = Constants.EMPTY_STRING;
  String referrerPostUrl = Constants.EMPTY_STRING;
  String firebasePostUrl = Constants.EMPTY_STRING;
  String appsFlyerPostUrl = Constants.EMPTY_STRING;
  String clientInfoPostUrl = Constants.EMPTY_STRING;
  String firstPageViewPostUrl = Constants.EMPTY_STRING;
  String packageName = Constants.EMPTY_STRING;
  String tvAPIEndPoint = Constants.EMPTY_STRING;
  String liveTVAPIEndPoint = Constants.EMPTY_STRING;
  String dhtvAPIEndPoint = Constants.EMPTY_STRING;
  String dhlocoAPIEndPoint = Constants.EMPTY_STRING;
  String vhAPIEndPoint = Constants.EMPTY_STRING;
  String pullNotificationEndPoint = Constants.EMPTY_STRING;
  String adsHandshakeEndPoint = Constants.EMPTY_STRING;
  String socialFeaturesUrl = Constants.EMPTY_STRING;
  String secureSocialFeaturesUrl = Constants.EMPTY_STRING;
  String likeFeaturesUrl = Constants.EMPTY_STRING;
  String defaultCampaignParam = Constants.EMPTY_STRING;
  String searchEndPoint = Constants.EMPTY_STRING;
  String autoCompleteEndPoint = Constants.EMPTY_STRING;
  String dhtvSearchApiEndPoint = Constants.EMPTY_STRING;
  String dhtvAutoCompleteApiEndPoint = Constants.EMPTY_STRING;
  String followHomeBaseUrl = Constants.EMPTY_STRING;
  String notificationChannelUrl = Constants.EMPTY_STRING;
  String userServiceBaseUrl = Constants.EMPTY_STRING;
  String userServiceSecuredBaseUrl = Constants.EMPTY_STRING;
  String groupsBaseUrl = Constants.EMPTY_STRING;
  String imageUploadBaseUrl = Constants.EMPTY_STRING;
  String contactSyncBaseUrl = Constants.EMPTY_STRING;
  String postCreationBaseUrl = Constants.EMPTY_STRING;
  String postDeletionBaseUrl = Constants.EMPTY_STRING;
  String postReportBaseUrl = Constants.EMPTY_STRING;
  String ogServiceBaseUrl = Constants.EMPTY_STRING;
  String reportGroupUrl = Constants.EMPTY_STRING;
  String reportMemberUrl = Constants.EMPTY_STRING;
  String reportProfileUrl = Constants.EMPTY_STRING;
  String fullSyncUrl = Constants.EMPTY_STRING;
  String localZoneUrl = Constants.EMPTY_STRING;

  boolean monkeyRestricted;
  boolean appsFlyerEnabled;
  boolean multiProcessEnabled;
  int onboardingDesignVersion = Constants.ONBOARDING_DESIGN_VERSION_V1;
  int minBaseInterval = 60 * 60; // 60 minutes
  int appVersionCode;
  boolean isGoBuild;
  String envType = Constants.DEV;

  public AppConfigBuilder() {
  }

  public AppConfigBuilder setVariantBuild(boolean variantBuild) {
    this.variantBuild = variantBuild;
    return this;
  }

  public AppConfigBuilder setLoggerEnabled(boolean loggerEnabled) {
    this.loggerEnabled = loggerEnabled;
    return this;
  }

  public AppConfigBuilder setAppendIdToBookDesc(boolean appendIdToBookDesc) {
    this.appendIdToBookDesc = appendIdToBookDesc;
    return this;
  }

  public AppConfigBuilder setAppVersionCode(int appVersionCode) {
    this.appVersionCode = appVersionCode;
    return this;
  }

  public AppConfigBuilder setDefaultUtmSource(String defaultUtmSource) {
    this.defaultUtmSource = defaultUtmSource;
    return this;
  }

  public AppConfigBuilder setAppVersion(String appVersion) {
    this.appVersion = appVersion;
    return this;
  }

  public AppConfigBuilder setAdServerEndPoint(String adServerEndPoint) {
    this.adServerEndPoint = adServerEndPoint;
    return this;
  }

  public AppConfigBuilder setNewsAPIEndPoint(String newsAPIEndPoint) {
    this.newsAPIEndPoint = newsAPIEndPoint;
    return this;
  }

  public AppConfigBuilder setNewsAPISecureEndPoint(String newsAPISecureEndPoint) {
    this.newsAPISecureEndPoint = newsAPISecureEndPoint;
    return this;
  }

  public AppConfigBuilder setAnalyticsBeaconEndPoint(String analyticsBeaconEndPoint) {
    this.analyticsBeaconEndPoint = analyticsBeaconEndPoint;
    return this;
  }

  public AppConfigBuilder setPremiumAdEndPoint(String premiumAdEndPoint) {
    this.premiumAdEndPoint = premiumAdEndPoint;
    return this;
  }

  public AppConfigBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public AppConfigBuilder setSourcePostUrl(String sourcePostUrl) {
    this.sourcePostUrl = sourcePostUrl;
    return this;
  }


  public AppConfigBuilder setReferrerPostUrl(String referrerPostUrl) {
    this.referrerPostUrl = referrerPostUrl;
    return this;
  }

  public AppConfigBuilder setFirebasePostUrl(String firebasePostUrl) {
    this.firebasePostUrl = firebasePostUrl;
    return this;
  }

  public AppConfigBuilder setClientInfoPostUrl(String clientInfoPostUrl) {
    this.clientInfoPostUrl = clientInfoPostUrl;
    return this;
  }

  public AppConfigBuilder setFirstPageViewPostUrl(String firstPageViewPostUrl) {
    this.firstPageViewPostUrl = firstPageViewPostUrl;
    return this;
  }

  public AppConfigBuilder setTVAPIEndPoint(String tvAPIEndPoint) {
    this.tvAPIEndPoint = tvAPIEndPoint;
    return this;
  }

  public AppConfigBuilder setLiveTVAPIEndPoint(String liveTVAPIEndPoint) {
    this.liveTVAPIEndPoint = liveTVAPIEndPoint;
    return this;
  }

  public AppConfigBuilder setDHTVAPIEndPoint(String dhtvAPIEndPoint) {
    this.dhtvAPIEndPoint = dhtvAPIEndPoint;
    return this;
  }

  public AppConfigBuilder setLocoAPIEndPoint(String dhlocoAPIEndPoint) {
    this.dhlocoAPIEndPoint = dhlocoAPIEndPoint;
    return this;
  }

  public AppConfigBuilder setDHTVSearchAPIEndPoint(String dhtvSearchAPIEndPoint) {
    this.dhtvSearchApiEndPoint = dhtvSearchAPIEndPoint;
    return this;
  }

  public AppConfigBuilder setDHTVAutoCompleteAPIEndPoint(String dhtvAutoCompleteAPIEndPoint) {
    this.dhtvAutoCompleteApiEndPoint = dhtvAutoCompleteAPIEndPoint;
    return this;
  }

  public AppConfigBuilder setPullNotificationEndPoint(String pullNotificationEndPoint) {
    this.pullNotificationEndPoint = pullNotificationEndPoint;
    return this;
  }

  public AppConfigBuilder setMinBaseInterval(int minBaseInterval) {
    this.minBaseInterval = minBaseInterval;
    return this;
  }

  public AppConfigBuilder setAdsHandshakeEndPoint(String adsHandshakeEndPoint) {
    this.adsHandshakeEndPoint = adsHandshakeEndPoint;
    return this;
  }

  public AppConfigBuilder setNewsAPIRelativeEndPoint(String newsAPIRelativeEndPoint) {
    this.newsAPIRelativeEndPoint = newsAPIRelativeEndPoint;
    return this;
  }

  public AppConfigBuilder setAppsFlyerPostUrl(String appsFlyerPostUrl) {
    this.appsFlyerPostUrl = appsFlyerPostUrl;
    return this;

  }

  public AppConfigBuilder setVhAPIEndPoint(String vhAPIEndPoint) {
    this.vhAPIEndPoint = vhAPIEndPoint;
    return this;
  }

  public AppConfigBuilder setSocialFeaturesUrl(String socialFeaturesUrl) {
    this.socialFeaturesUrl = socialFeaturesUrl;
    return this;
  }

  public AppConfigBuilder setSecureSocialFeaturesUrl(String secureSocialFeaturesUrl) {
    this.secureSocialFeaturesUrl = secureSocialFeaturesUrl;
    return this;
  }

  public AppConfigBuilder setLikeFeaturesUrl(String likeFeaturesUrl) {
    this.likeFeaturesUrl = likeFeaturesUrl;
    return this;
  }

  public AppConfigBuilder setDefaultCampaignParams(String campaignParams) {
    this.defaultCampaignParam = campaignParams;
    return this;
  }

  public AppConfigBuilder setSearchEndPoint(String searchEndPoint) {
    this.searchEndPoint = searchEndPoint;
    return this;
  }

  public AppConfigBuilder setAutoCompleteEndPoint(String autoCompleteEndPoint) {
    this.autoCompleteEndPoint = autoCompleteEndPoint;
    return this;
  }

  public AppConfigBuilder setMonkeyRestricted(boolean monkeyRestricted) {
    this.monkeyRestricted = monkeyRestricted;
    return this;
  }

  public AppConfigBuilder setAppsFlyerEnabled(boolean appsFlyerEnabled) {
    this.appsFlyerEnabled = appsFlyerEnabled;
    return this;
  }

  public AppConfigBuilder enableAppIndexing(boolean appIndexingEnabled){
    this.appIndexingEnabled = appIndexingEnabled;
    return this;
  }

  public AppConfigBuilder setOnboardingDesignVersion(int version) {
    this.onboardingDesignVersion = version;
    return this;
  }

  public AppConfigBuilder setFollowHomeBaseUrl(String followHomeBaseUrl) {
    this.followHomeBaseUrl = followHomeBaseUrl;
    return this;
  }

  public AppConfigBuilder setNotificationChannelUrl(String notificationChannelUrl) {
    this.notificationChannelUrl = notificationChannelUrl;
    return this;
  }

  public AppConfigBuilder setIsGoBuild(boolean isGoBuild) {
    this.isGoBuild = isGoBuild;
    return this;
  }

  public AppConfigBuilder setEnvType(String envType) {
    this.envType = envType;
    return this;
  }

  public AppConfigBuilder setUserServiceBaseUrl(String userServiceBaseUrl) {
    this.userServiceBaseUrl = userServiceBaseUrl;
    return this;
  }

  public AppConfigBuilder setUserServiceSecuredBaseUrl(String userServiceSecuredBaseUrl) {
    this.userServiceSecuredBaseUrl = userServiceSecuredBaseUrl;
    return this;
  }

  public AppConfigBuilder setGroupsBaseUrl(String groupsBaseUrl) {
    this.groupsBaseUrl = groupsBaseUrl;
    return this;
  }

  public AppConfigBuilder setImageUploadBaseUrl(String imageUploadBaseUrl) {
    this.imageUploadBaseUrl = imageUploadBaseUrl;
    return this;
  }

  public AppConfigBuilder setContactSyncBaseUrl(String cseBaseUrl) {
    this.contactSyncBaseUrl = cseBaseUrl;
    return this;
  }

  public AppConfigBuilder setPostCreationBaseUrl(String postCreationBaseUrl) {
    this.postCreationBaseUrl = postCreationBaseUrl;
    return this;
  }

  public AppConfigBuilder setPostDeletionBaseUrl(String postDeletionBaseUrl) {
    this.postDeletionBaseUrl = postDeletionBaseUrl;
    return this;
  }

  public AppConfigBuilder setPostReportBaseUrl(String postReportBaseUrl) {
    this.postReportBaseUrl = postReportBaseUrl;
    return this;
  }

  public AppConfigBuilder setOgServiceBaseUrl(String ogServiceBaseUrl) {
    this.ogServiceBaseUrl = ogServiceBaseUrl;
    return this;
  }

  public AppConfig createAppConfig() {
    return AppConfig.createInstance(this);
  }

  public AppConfigBuilder setReportGroupUrl(String reportGroupUrl) {
    this.reportGroupUrl = reportGroupUrl;
    return this;
  }

  public AppConfigBuilder setReportMemberUrl(String reportMemberUrl) {
    this.reportMemberUrl = reportMemberUrl;
    return this;
  }

  public AppConfigBuilder setReportProfileUrl(String reportProfileUrl) {
    this.reportProfileUrl = reportProfileUrl;
    return this;
  }

  public AppConfigBuilder setFullSyncUrl(String fullSyncUrl) {
    this.fullSyncUrl = fullSyncUrl;
    return this;
  }

  public AppConfigBuilder setLocalZoneUrl(String fullSyncUrl) {
    this.localZoneUrl = fullSyncUrl;
    return this;
  }

  public AppConfigBuilder setMultiProcessEnabled(boolean multiProcessEnabled) {
    this.multiProcessEnabled = multiProcessEnabled;
    return this;
  }

}
