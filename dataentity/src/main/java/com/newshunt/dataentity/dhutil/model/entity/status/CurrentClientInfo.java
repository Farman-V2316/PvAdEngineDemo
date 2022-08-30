/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.status;

import androidx.annotation.NonNull;

import com.newshunt.dataentity.common.model.entity.AppInstallType;
import com.newshunt.dataentity.common.model.entity.identifier.UniqueIdentifier;
import com.newshunt.dataentity.common.model.entity.model.DomainCookieInfo;
import com.newshunt.dataentity.common.model.entity.status.ClientInfo;
import com.newshunt.dataentity.common.model.entity.status.ConnectionInfo;
import com.newshunt.dataentity.common.model.entity.status.LocationInfo;
import com.newshunt.dataentity.dhutil.model.entity.upgrade.LangInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Container class that has information about client's device, location and connection.
 *
 * @author shreyas.desai
 */
public class CurrentClientInfo implements Serializable {
  private static final long serialVersionUID = -4122354101899097337L;
  private ClientInfo clientInfo;
  private LocationInfo locationInfo;
  private ConnectionInfo connectionInfo;
  private String androidId;
  private String referrer;
  private String packageName;
  private Boolean appOpenEvent;
  private String edition;
  private Integer headlineStoryClicks;
  private Integer headlineViews;
  private Boolean dh2DhReInstall;
  private String mimeTypes;
  private String installType = AppInstallType.NA.name();
  private Boolean isEditionConfirmed;
  private UniqueIdentifier uniqueIdentifier;
  private Boolean tickerHeightDynamic = true;
  private Boolean multipleTickerSupport = true;
  private Boolean topicWebitemsSupported = true;
  private Boolean pgCdnSupported = true;
  private Version version;
  private Boolean inboxFeedSupported = true;
  private Boolean astroTopicSupported = true;
  private Boolean multiHomeSupported = true;
  private Boolean chronoInboxSupported = true;
  private Boolean contentBaseUrlForWebitem = true;
  private Boolean tickerDeeplinkSupported = true;
  private final boolean clientComscoreTrackSupported = true;
  private Boolean bcdnImageSupported = true;
  private ArrayList<DomainCookieInfo> clearedCookies;
  private Boolean locationCardSupported = true;
  private Boolean associationSupported = true;
  private Boolean viralSupported = true;
  private Boolean collectionCardSupported = true;
  private Integer autoPlayUserPreference;
  private Boolean liveTvCardSupported = true;
  private Boolean supportsTrackWith1stChunk = true;
      // BE can send 2nd chunk and its trackUrl along with story.
  private boolean showTopStoriesCarousel = true; // to enable three_fourth and full carousels
  private boolean followServiceSupported = true;
  private boolean preferenceCardSupported = true;
  private boolean dislikeL2NpMacroSupported = true;
// means client is replace "##NEWSPAPER_NAME##" in a dislike L2
  private Map<String, String> acquisitionReferrers;
  private boolean followSupported = true;//FM to get social counts object at asset level
  private boolean viralGIFSupported = true; //Support GIF in viral
  private boolean multimediaCarouselSupported = true;
  private boolean allHeroCardsSupported = true;//feature mask to show all as Hero cards
  private boolean isUITypeToggleable = true;//feature mast to toggle UI Type of card if toggleable
  private boolean followSectionSupported = true;//Removal of 9 dots
  private boolean playerManagerSupported = true; // Supported for back to back autoplay videos
  private boolean collectionListSupported = true;
      // for top stories collection shown as veritcal list
  private String appInstaller;
  private boolean webCardSupported = true;
  private boolean seeOtherPerspectiveSupported = true;
  private boolean recoRelatedStorySupported = true;
  private boolean discoveryCardSupported = true;
  private boolean versionedApiEmptyResponseSupported = true;
      // for handling the empty response in the versioned api from app version 13.1.x
  private String userData;

  //Feature mask to tell profile is supported from this app version
  private Boolean userProfileAndCreatorSupported = true;
  //Feature mask to tell UGC profiles are supported in this app version
  private Boolean ugcInternalProfileSupported = true;
  //Feature mask to tell Contacts+ and people you may know carousel supported
  private Boolean contactsBasedDiscoveryCarouselSupported = true;
  private Boolean groupsSupported = true; //Feature mask telling Groups is supported.
  private Boolean richReferralStringsSupported = true;
  private Boolean commentsOnCardSupported = true;
  private Boolean localZoneVideoSupported = true;
  private Boolean adSpecSupportedInWebItems = true;
  private Boolean inAppUpdatesSupported = true;
  private boolean numericCountsForLikeTypes = true; /* if true, BE will send  numeric-value for
  // LIKE, LOVE,HAPPY,WOW,SAD,ANGRY. Eg: "1234" instead of "1.2k" */

  private boolean adjunctLanguageSupported = true; /*feature mask for adjunct language related changes*/

  //Feature mask to tell video prefetch is supported from this app version
  private Boolean videoPrefetchSupported = true;

  private Boolean isNERSupported = true;

  private LangInfo langInfo;

  public CurrentClientInfo() {
    //Not used at all
  }

  public CurrentClientInfo(ClientInfo clientInfo, LocationInfo locationInfo,
                           ConnectionInfo connectionInfo) {
    this.clientInfo = clientInfo;
    this.locationInfo = locationInfo;
    this.connectionInfo = connectionInfo;
  }

  public ClientInfo getClientInfo() {
    return clientInfo;
  }

  public void setClientInfo(ClientInfo clientInfo) {
    this.clientInfo = clientInfo;
  }

  public LocationInfo getLocationInfo() {
    return locationInfo;
  }

  public void setLocationInfo(LocationInfo locationInfo) {
    this.locationInfo = locationInfo;
  }

  public ConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  public void setConnectionInfo(ConnectionInfo connectionInfo) {
    this.connectionInfo = connectionInfo;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  public String getAndroidId() {
    return androidId;
  }

  public void setAndroidId(String androidId) {
    this.androidId = androidId;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getReferrer() {
    return referrer;
  }

  public void setReferrer(String referrer) {
    this.referrer = referrer;
  }

  public Integer getHeadlineStoryClicks() {
    return headlineStoryClicks;
  }

  public void setHeadlineStoryClicks(Integer headlineStoryClicks) {
    this.headlineStoryClicks = headlineStoryClicks;
  }

  public Integer getHeadlineViews() {
    return headlineViews;
  }

  public void setHeadlineViews(Integer headlineViews) {
    this.headlineViews = headlineViews;
  }

  public Boolean getAppOpenEvent() {
    return appOpenEvent;
  }

  public void setAppOpenEvent(Boolean appOpenEvent) {
    this.appOpenEvent = appOpenEvent;
  }

  public void setDh2DhReInstall(Boolean dh2DhReInstall) {
    this.dh2DhReInstall = dh2DhReInstall;
  }

  public void setMimeTypes(String mimeTypes) {
    this.mimeTypes = mimeTypes;
  }

  public void setInstallType(String installType) {
    this.installType = installType;
  }

  public void setIsEditionConfirmed(Boolean isEditionConfirmed) {
    this.isEditionConfirmed = isEditionConfirmed;
  }

  public void setUserData(String userData) {
    this.userData = userData;
  }

  public Boolean getTickerHeightDynamic() {
    return tickerHeightDynamic;
  }

  public void setTickerHeightDynamic(Boolean tickerHeightDynamic) {
    this.tickerHeightDynamic = tickerHeightDynamic;
  }

  public Boolean getMultipleTickerSupport() {
    return multipleTickerSupport;
  }

  public void setMultipleTickerSupport(Boolean multipleTickerSupport) {
    this.multipleTickerSupport = multipleTickerSupport;
  }

  public UniqueIdentifier getUniqueIdentifier() {
    return uniqueIdentifier;
  }

  public void setUniqueIdentifier(UniqueIdentifier uniqueIdentifier) {
    this.uniqueIdentifier = uniqueIdentifier;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public Boolean getTopicWebitemsSupported() {
    return topicWebitemsSupported;
  }

  public void setTopicWebitemsSupported(Boolean topicWebitemsSupported) {
    this.topicWebitemsSupported = topicWebitemsSupported;
  }

  public Boolean getBcdnImageSupported() {
    return bcdnImageSupported;
  }

  public void setBcdnImageSupported(Boolean bcdnImageSupported) {
    this.bcdnImageSupported = bcdnImageSupported;
  }

  public ArrayList<DomainCookieInfo> getClearedCookies() {
    return clearedCookies;
  }

  public void setClearedCookies(
      ArrayList<DomainCookieInfo> clearedCookies) {
    this.clearedCookies = clearedCookies;
  }

  public void setAutoPlayUserPreference(Integer preference) {
    this.autoPlayUserPreference = preference;
  }

  public void addAcquisitionReferrer(@NonNull final String key, @NonNull final String value) {
    if (acquisitionReferrers == null) {
      acquisitionReferrers = new HashMap<>();
    }
    acquisitionReferrers.put(key, value);
  }

  public void setAppInstaller(final String installerPkg) {
    this.appInstaller = installerPkg;
  }

  public LangInfo getLangInfo() {
    return langInfo;
  }

  public void setLangInfo(LangInfo langInfo) {
    this.langInfo = langInfo;
  }
}
