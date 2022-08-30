/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.status;

import java.io.Serializable;

/**
 * Represents the version information currently available on the server. Clients
 * can use this information to check if upgrade is needed or not.
 *
 * @author amarjit
 */
public class Version implements Serializable {

  private static final long serialVersionUID = 778634638050678671L;

  /**
   * available application version
   */
  private String applicationVersion;

  /**
   * available version of the topics
   */
  private String topicsVersion;

  /**
   * available version of the sources (newspapers)
   */
  private String sourcesVersion;

  /**
   * available version of the editions
   */
  private String editionsVersion;

  /**
   * available version of the languages
   */
  private String languagesVersion;

  private String hintConfigVersion;

  private String menuDictionaryVersion;

  private String appLaunchConfigVersion;

  private String appSectionsVersion;

  private String bottomBarConfigVersion;

  private String dnsVersion;

  private String chineseDeviceInfoVersion;

  private String actionablePayloadVersion;

  private String appJsInfoVersion;

  private String shareTextMappingVersion;

  private String viralFeedbackVersion;

  private String playerUpgradeVersion;

  private String appsflyerEventConfigVersion;

  private String searchHintVersion;

  private String multiProcessConfigVersion;

  private String detailWidgetOrderingVersion;

  private String eventOptInVersion;

  private String notificationChannelVersion;

  private String aboutUsVersion;

  private String grpInviteConfigVersion; //Group invite config API version

  private String groupsApprovalConfigVersion;

  private String handshakeConfigVersion;

  private String notificationCtaVersion;

  private String newsStickyOptinVersion;

  private String adjunctLangVersion;

  private String notificationColorTemplateVersion;

  public String getSearchHintVersion() {
    return searchHintVersion;
  }

  public void setSearchHintVersion(String searchHintVersion) {
    this.searchHintVersion = searchHintVersion;
  }



  public String getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(String applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public String getAboutUsVersion() {
    return aboutUsVersion;
  }

  public void setAboutUsVersion(String aboutUsVersion) {
    this.aboutUsVersion = aboutUsVersion;
  }

  public String getTopicsVersion() {
    return topicsVersion;
  }

  public void setTopicsVersion(String topicsVersion) {
    this.topicsVersion = topicsVersion;
  }

  public String getSourcesVersion() {
    return sourcesVersion;
  }

  public void setSourcesVersion(String sourcesVersion) {
    this.sourcesVersion = sourcesVersion;
  }

  public String getEditionsVersion() {
    return editionsVersion;
  }

  public void setEditionsVersion(String editionsVersion) {
    this.editionsVersion = editionsVersion;
  }

  public String getLanguagesVersion() {
    return languagesVersion;
  }

  public void setLanguagesVersion(String languagesVersion) {
    this.languagesVersion = languagesVersion;
  }

  public String getMenuDictionaryVersion() {
    return menuDictionaryVersion;
  }

  public void setMenuDictionaryVersion(String menuDictionaryVersion) {
    this.menuDictionaryVersion = menuDictionaryVersion;
  }

  public String getHintConfigVersion() {
    return hintConfigVersion;
  }

  public void setHintConfigVersion(String hintConfigVersion) {
    this.hintConfigVersion = hintConfigVersion;
  }

  public String getBottomBarConfigVersion() {
    return bottomBarConfigVersion;
  }

  public void setBottomBarConfigVersion(String bottomBarConfigVersion) {
    this.bottomBarConfigVersion = bottomBarConfigVersion;
  }

  public String getAppLaunchConfigVersion() {
    return appLaunchConfigVersion;
  }

  public void setAppLaunchConfigVersion(String appLaunchConfigVersion) {
    this.appLaunchConfigVersion = appLaunchConfigVersion;
  }

  public String getAppSectionsVersion() {
    return appSectionsVersion;
  }

  public void setAppSectionsVersion(String appSectionsVersion) {
    this.appSectionsVersion = appSectionsVersion;
  }

  public String getDNSVersion() {
    return dnsVersion;
  }

  public void setDNSVersion(String dnsVersion) {
    this.dnsVersion = dnsVersion;
  }

  public String getChineseDeviceInfoVersion() {
    return chineseDeviceInfoVersion;
  }

  public void setChineseDeviceInfoVersion(String chineseDeviceInfoVersion) {
    this.chineseDeviceInfoVersion = chineseDeviceInfoVersion;
  }

  public String getActionablePayloadVersion() {
    return actionablePayloadVersion;
  }

  public void setActionablePayloadVersion(String actionablePayloadVersion) {
    this.actionablePayloadVersion = actionablePayloadVersion;
  }

  public String getShareTextMappingVersion() {
    return shareTextMappingVersion;
  }

  public void setShareTextMappingVersion(String shareTextMappingVersion) {
    this.shareTextMappingVersion = shareTextMappingVersion;
  }

  public String getViralFeedbackVersion() {
    return viralFeedbackVersion;
  }

  public void setViralFeedbackVersion(String viralFeedbackVersion) {
    this.viralFeedbackVersion = viralFeedbackVersion;
  }

  public String getPlayerUpgradeVersion() {
    return playerUpgradeVersion;
  }

  public void setPlayerUpgradeVersion(String playerUpgradeVersion) {
    this.playerUpgradeVersion = playerUpgradeVersion;
  }

  public String getAppsflyerEventConfigVersion() {
    return appsflyerEventConfigVersion;
  }

  public void setAppsflyerEventConfigVersion(String appsflyerEventConfigVersion) {
    this.appsflyerEventConfigVersion = appsflyerEventConfigVersion;
  }

  public String getMultiProcessConfigVersion() {
    return multiProcessConfigVersion;
  }

  public void setMultiProcessConfigVersion(String multiProcessConfigVersion) {
    this.multiProcessConfigVersion = multiProcessConfigVersion;
  }

  public String getDetailWidgetOrderingVersion() {
    return detailWidgetOrderingVersion;
  }

  public void setDetailWidgetOrderingVersion(String detailWidgetOrderingVersion) {
    this.detailWidgetOrderingVersion = detailWidgetOrderingVersion;
  }

  public String getEventOptInVersion() {
    return eventOptInVersion;
  }

  public void setEventOptInVersion(String eventOptInVersion) {
    this.eventOptInVersion = eventOptInVersion;
  }

  public String getGroupsApprovalConfigVersion() {
    return groupsApprovalConfigVersion;
  }

  public void setGroupsApprovalConfigVersion(String groupsApprovalConfigVersion) {
    this.groupsApprovalConfigVersion = groupsApprovalConfigVersion;
  }

  public String getHandshakeConfigVersion() {
    return handshakeConfigVersion;
  }

  public void setHandshakeConfigVersion(String handshakeConfigVersion) {
    this.handshakeConfigVersion = handshakeConfigVersion;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(getClass()).append(" [").append("applicationVersion=")
        .append(applicationVersion).append(", topicsVersion=").append(topicsVersion)
        .append(", sourcesVersion=").append(sourcesVersion).append(", editionsVersion=")
        .append(editionsVersion).append(", languagesVersion=").append(languagesVersion)
        .append(", dnsVersion=").append(dnsVersion)
        .append(", chineseDeviceInfoVersion=").append(chineseDeviceInfoVersion)
        .append(", shareTextMappingVersion=").append(shareTextMappingVersion)
        .append(", grpInviteConfigVersion=").append(grpInviteConfigVersion)
        .append(", groupsApprovalConfigVersion=").append(groupsApprovalConfigVersion)
        .append("]")
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Version)) {
      return false;
    }

    Version version = (Version) o;

    if (!hintConfigVersion.equals(version.hintConfigVersion)) {
      return false;
    }
    return menuDictionaryVersion.equals(version.menuDictionaryVersion);

  }

  @Override
  public int hashCode() {
    int result = hintConfigVersion.hashCode();
    result = 31 * result + menuDictionaryVersion.hashCode();
    return result;
  }

  public String getAppJsInfoVersion() {
    return appJsInfoVersion;
  }

  public void setAppJsInfoVersion(String appJsInfoVersion) {
    this.appJsInfoVersion = appJsInfoVersion;
  }

  public String getNotificationChannelVersion() {
    return notificationChannelVersion;
  }

  public void setNotificationChannelVersion(String notificationChannelVersion) {
    this.notificationChannelVersion = notificationChannelVersion;
  }

  public String getGrpInviteConfigVersion() {
    return grpInviteConfigVersion;
  }

  public void setGrpInviteConfigVersion(String grpInviteConfigVersion) {
    this.grpInviteConfigVersion = grpInviteConfigVersion;
  }

  public String getNotificationCtaVersion() {
    return notificationCtaVersion;
  }

  public void setNotificationCtaVersion(String notificationCtaVersion) {
    this.notificationCtaVersion = notificationCtaVersion;
  }

  public String getNewsStickyOptinVersion() {
    return newsStickyOptinVersion;
  }

  public void setNewsStickyOptinVersion(String newsStickyOptinVersion) {
    this.newsStickyOptinVersion = newsStickyOptinVersion;
  }

  public String getAdjunctLangVersion() {
    return adjunctLangVersion;
  }

  public void setAdjunctLangVersion(String adjunctLangVersion) {
    this.adjunctLangVersion = adjunctLangVersion;
  }

  public String getNotificationTemplateVersion() {
    return notificationColorTemplateVersion;
  }

  public void setNotificationTemplateVersion(String notificationTemplateVersion) {
    this.notificationColorTemplateVersion = notificationTemplateVersion;
  }
}
