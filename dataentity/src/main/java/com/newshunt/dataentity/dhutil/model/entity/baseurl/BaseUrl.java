/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.baseurl;

import java.io.Serializable;

/**
 * Represents the base urls information currently available on the server.
 *
 * @author amarjit
 */
public class BaseUrl implements Serializable {

  private static final long serialVersionUID = 2504970952502533048L;

  /**
   * application base url
   */
  private String applicationUrl;

  /**
   * application secure base url
   */
  private String applicationSecureUrl;

  /**
   * applicationRelativeUrl base url
   */
  private String applicationRelativeUrl;

  /**
   * notificationTrigger base url
   */
  private String notificationTriggerUrl;

  /**
   * notificationNews base url
   */
  private String notificationNewsUrl;

  /**
   * Analytics base url
   */
  private String analyticsUrl;

  /**
   * feedback base url
   */
  private String feedbackUrl;

  /**
   * widget base url
   */
  private String widgetUrl;

  /**
   * advertisement base url
   */
  private String advertisementUrl;

  /**
   * Apps on device post endpoint
   */
  private String appsOnDeviceUrl;

  /**
   * source post endpoint
   */
  private String sourcePostUrl;

  /**
   * Referrer post endpoint
   */
  private String referrerPostUrl;

  /**
   * Firebase post endpoint
   */
  private String firebasePostUrl;

  /**
   * AppsFlyer post endpoint
   */
  private String appsFlyerPostUrl;

  /**
   * Client info post endpoint
   */
  private String clientInfoPostUrl;

  /**
   * First page view post endpoint
   */
  private String firstPageViewPostUrl;

  /**
   * Advertisement end point for p0 ads
   */
  private String premiumAdvertisementUrl;

  //end point for pull Notifications
  private String pullNotificationUrl;

  //end point for pull notification v2
  private String pullNotificationUrlFromProfileRelease;

  //end point for notification full sync
  private String fullSyncUrl;

  //end point for Ads Handshake
  private String adsHandshakeUrl;
  private String userServiceBaseUrl;
  private String userServiceSecuredBaseUrl;

  private String imageUploadBaseUrl;
  private String groupsBaseUrl;

  private String socialUrl;

  private String secureSocialUrl;

  private String likeUrl;

  /**
   * viral base url
   */
  private String viralBaseUrl;

  private String searchBaseUrl;

  private String autoCompleteBaseUrl;

  private String followHomeBaseUrl;

  private String notificationChannelUrl;

  /**
   * Intentionally keeping the name short so its not obvious in the payload
   */
  private String cseBaseUrl;

  private String postCreationUrl;

  private String postDeletionUrl;

  private String postReportUrl;

  private String ogServiceUrl;

  private String reportGroupUrl;

  private String reportMemberUrl;

  private String reportProfileUrl;

  private String localZoneUrl;

  public String getApplicationUrl() {
    return applicationUrl;
  }

  public void setApplicationUrl(String applicationUrl) {
    this.applicationUrl = applicationUrl;
  }

  public String getNotificationTriggerUrl() {
    return notificationTriggerUrl;
  }

  public void setNotificationTriggerUrl(String notificationTriggerUrl) {
    this.notificationTriggerUrl = notificationTriggerUrl;
  }

  public String getNotificationNewsUrl() {
    return notificationNewsUrl;
  }

  public void setNotificationNewsUrl(String notificationNewsUrl) {
    this.notificationNewsUrl = notificationNewsUrl;
  }

  public String getAnalyticsUrl() {
    return analyticsUrl;
  }

  public void setAnalyticsUrl(String analyticsUrl) {
    this.analyticsUrl = analyticsUrl;
  }

  public String getFeedbackUrl() {
    return feedbackUrl;
  }

  public void setFeedbackUrl(String feedbackUrl) {
    this.feedbackUrl = feedbackUrl;
  }

  public String getWidgetUrl() {
    return widgetUrl;
  }

  public void setWidgetUrl(String widgetUrl) {
    this.widgetUrl = widgetUrl;
  }

  public String getAdvertisementUrl() {
    //TODO: PANDA: added manually for testing
    return "http://qa-money.newshunt.com/publicVibe/v1/pgi/html.json";
  }

  public void setAdvertisementUrl(String advertisementUrl) {
    this.advertisementUrl = advertisementUrl;
  }

  public String getAppsOnDeviceUrl() {
    return appsOnDeviceUrl;
  }

  public void setAppsOnDeviceUrl(String appsOnDeviceUrl) {
    this.appsOnDeviceUrl = appsOnDeviceUrl;
  }

  public String getPremiumAdvertisementUrl() {
    return premiumAdvertisementUrl;
  }

  public void setPremiumAdvertisementUrl(String premiumAdvertisementUrl) {
    this.premiumAdvertisementUrl = premiumAdvertisementUrl;
  }

  public String getSourcePostUrl() {
    return sourcePostUrl;
  }

  public void setSourcePostUrl(String sourcePostUrl) {
    this.sourcePostUrl = sourcePostUrl;
  }

  public String getReferrerPostUrl() {
    return referrerPostUrl;
  }

  public void setReferrerPostUrl(String referrerPostUrl) {
    this.referrerPostUrl = referrerPostUrl;
  }

  public String getFirebasePostUrl() {
    return firebasePostUrl;
  }

  public void setFirebasePostUrl(String firebasePostUrl) {
    this.firebasePostUrl = firebasePostUrl;
  }

  public String getClientInfoPostUrl() {
    return clientInfoPostUrl;
  }

  public void setClientInfoPostUrl(String clientInfoPostUrl) {
    this.clientInfoPostUrl = clientInfoPostUrl;
  }

  public String getFirstPageViewPostUrl() {
    return firstPageViewPostUrl;
  }

  public void setFirstPageViewPostUrl(String firstPageViewPostUrl) {
    this.firstPageViewPostUrl = firstPageViewPostUrl;
  }

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public String getPullNotificationUrl() {
    return pullNotificationUrl;
  }

  public void setPullNotificationUrl(String pullNotificationUrl) {
    this.pullNotificationUrl = pullNotificationUrl;
  }

  public String getAdsHandshakeUrl() {
    return adsHandshakeUrl;
  }

  public void setAdsHandshakeUrl(String adsHandshakeUrl) {
    this.adsHandshakeUrl = adsHandshakeUrl;
  }

  public String getApplicationRelativeUrl() {
    return applicationRelativeUrl;
  }

  public void setApplicationRelativeUrl(String applicationRelativeUrl) {
    this.applicationRelativeUrl = applicationRelativeUrl;
  }

  public String getAppsFlyerPostUrl() {
    return appsFlyerPostUrl;
  }

  public String getSocialUrl() {
    return socialUrl;
  }

  public void setSocialUrl(String socialUrl) {
    this.socialUrl = socialUrl;
  }

  public String getSecureSocialUrl() {
    return secureSocialUrl;
  }

  public void setSecureSocialUrl(String secureSocialUrl) {
    this.secureSocialUrl = secureSocialUrl;
  }

  public String getLikeUrl() {
    return likeUrl;
  }

  public void setLikeUrl(String likeUrl) {
    this.likeUrl = likeUrl;
  }

  public void setAppsFlyerPostUrl(String appsFlyerPostUrl) {
    this.appsFlyerPostUrl = appsFlyerPostUrl;
  }

  public String getViralBaseUrl() {
    return viralBaseUrl;
  }

  public void setViralBaseUrl(String viralBaseUrl) {
    this.viralBaseUrl = viralBaseUrl;
  }

  public String getSearchBaseUrl() {
    return searchBaseUrl;
  }

  public void setSearchBaseUrl(String searchBaseUrl) {
    this.searchBaseUrl = searchBaseUrl;
  }

  public String getAutoCompleteBaseUrl() {
    return autoCompleteBaseUrl;
  }

  public void setAutoCompleteBaseUrl(String autoCompleteBaseUrl) {
    this.autoCompleteBaseUrl = autoCompleteBaseUrl;
  }

  public String getFollowHomeBaseUrl() {
    return followHomeBaseUrl;
  }

  public void setFollowHomeBaseUrl(String followHomeBaseUrl) {
    this.followHomeBaseUrl = followHomeBaseUrl;
  }

  public String getNotificationChannelUrl() {
    return notificationChannelUrl;
  }

  public void setNotificationChannelUrl(String notificationChannelUrl) {
    this.notificationChannelUrl = notificationChannelUrl;
  }

  public String getUserServiceBaseUrl() {
    return userServiceBaseUrl;
  }

  public void setUserServiceBaseUrl(String userServiceBaseUrl) {
    this.userServiceBaseUrl = userServiceBaseUrl;
  }

  public String getUserServiceSecuredBaseUrl() {
    return userServiceSecuredBaseUrl;
  }

  public String getGroupsBaseUrl() {
    return groupsBaseUrl;
  }

  public void setGroupsBaseUrl(String groupsBaseUrl) {
    this.groupsBaseUrl = groupsBaseUrl;
  }

  public void setUserServiceSecuredBaseUrl(String userServiceSecuredBaseUrl) {
    this.userServiceSecuredBaseUrl = userServiceSecuredBaseUrl;
  }

  public String getPullNotificationUrlFromProfileRelease() {
    return pullNotificationUrlFromProfileRelease;
  }

  public void setPullNotificationUrlFromProfileRelease(
      String pullNotificationUrlFromProfileRelease) {
    this.pullNotificationUrlFromProfileRelease = pullNotificationUrlFromProfileRelease;
  }

  public String getFullSyncUrl() {
    return fullSyncUrl;
  }

  public void setFullSyncUrl(String fullSyncUrl) {
    this.fullSyncUrl = fullSyncUrl;
  }

  public String getApplicationSecureUrl() {
    return applicationSecureUrl;
  }

  public void setApplicationSecureUrl(String applicationSecureUrl) {
    this.applicationSecureUrl = applicationSecureUrl;
  }

  public String getImageUploadBaseUrl() {
    return imageUploadBaseUrl;
  }

  public void setImageUploadBaseUrl(String imageUploadBaseUrl) {
    this.imageUploadBaseUrl = imageUploadBaseUrl;
  }

  public void setContactSyncBaseUrl(String contactSyncBaseUrl) {
    this.cseBaseUrl = contactSyncBaseUrl;
  }

  public String getContactSyncBaseUrl() {
    return cseBaseUrl;
  }

  public String getPostCreationUrl() {
    return postCreationUrl;
  }

  public void setPostCreationUrl(String postCreationUrl) {
    this.postCreationUrl = postCreationUrl;
  }

  public String getPostDeletionUrl() {
    return postDeletionUrl;
  }

  public void setPostDeletionUrl(String postDeletionUrl) {
    this.postDeletionUrl = postDeletionUrl;
  }

  public String getPostReportUrl() {
    return postReportUrl;
  }

  public void setPostReportUrl(String postReportUrl) {
    this.postReportUrl = postReportUrl;
  }

  public String getOgServiceUrl() {
    return ogServiceUrl;
  }

  public void setOgServiceUrl(String ogServiceUrl) {
    this.ogServiceUrl = ogServiceUrl;
  }

  public String getReportGroupUrl() {
    return reportGroupUrl;
  }

  public void setReportGroupUrl(String reportGroupUrl) {
    this.reportGroupUrl = reportGroupUrl;
  }

  public String getReportMemberUrl() {
    return reportMemberUrl;
  }

  public void setReportMemberUrl(String reportMemberUrl) {
    this.reportMemberUrl = reportMemberUrl;
  }

  public String getReportProfileUrl() {
    return reportProfileUrl;
  }

  public void setReportProfileUrl(String reportProfileUrl) {
    this.reportProfileUrl = reportProfileUrl;
  }

  public String getLocalZoneUrl() {
    return localZoneUrl;
  }

  public void setLocalZoneUrl(String localZoneUrl) {
    this.localZoneUrl = localZoneUrl;
  }

}