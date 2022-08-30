package com.newshunt.dataentity.notification;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.notification.util.NotificationConstants;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by santosh.kumar on 10/30/2015.
 */
public class BaseInfo implements Serializable {

  private static final long serialVersionUID = 5324557055415760762L;

  private String id;
  private String messageExpTimeStamp;
  private String sType;
  private String dedupeKey;
  private String deleteType;
  private TimeRange timeRange;
  private List<String> postIds;
  private List<String> deeplinks;
  @Override
  public String toString() {
    return "BaseInfo{" +
        "id='" + id + '\'' +
        ", messageExpTimeStamp='" + messageExpTimeStamp + '\'' +
        ", sType='" + sType + '\'' +
        ", msg='" + msg + '\'' +
        ", uniMsg='" + uniMsg + '\'' +
        ", sectionType=" + sectionType +
        ", expiryTime=" + expiryTime +
        ", layoutType=" + layoutType +
        ", language='" + language + '\'' +
        ", languageCode='" + languageCode + '\'' +
        ", uniqueId=" + uniqueId +
        ", isUrdu=" + isUrdu +
        ", imageLink='" + imageLink + '\'' +
        ", imageLinkV2='" + imageLinkV2 + '\'' +
        ", bigImageLink='" + bigImageLink + '\'' +
        ", bigImageLinkV2='" + bigImageLinkV2 + '\'' +
        ", bigText='" + bigText + '\'' +
        ", priority=" + priority +
        ", tickerMessage='" + tickerMessage + '\'' +
        ", isBookDownloadNotification=" + isBookDownloadNotification +
        ", inboxImageLink='" + inboxImageLink + '\'' +
        ", state=" + state +
        ", isRemovedFromTray=" + isRemovedFromTray +
        ", isGrouped=" + isGrouped +
        ", timeStamp=" + timeStamp +
        ", edition='" + edition + '\'' +
        ", deliveryType=" + deliveryType +
        ", isSynced=" + isSynced +
        ", applyLanguageFilter=" + applyLanguageFilter +
        ", languages=" + Arrays.toString(languages) +
        ", buzzItemtype='" + buzzItemtype + '\'' +
        ", v4DisplayTime=" + v4DisplayTime +
        ", v4IsInternetRequired=" + v4IsInternetRequired +
        ", isDeferred=" + isDeferred +
        ", isDeferredForAnalytics=" + isDeferredForAnalytics +
        ", type='" + type + '\'' +
        ", subType='" + subType + '\'' +
        ", isNotificationForDisplaying=" + isNotificationForDisplaying +
        ", group='" + group + '\'' +
        ", experimentParams=" + experimentParams +
        ", queryParams=" + queryParams +
        ", deeplink='" + deeplink + '\'' +
        ", doNotAutoFetchSwipeUrl=" + doNotAutoFetchSwipeUrl +
        ", groupType='" + groupType + '\'' +
        ", channelId='" + channelId + '\'' +
        ", channelGroupId='" + channelGroupId + '\'' +
        ", placement=" + placement +
        ", iconUrls=" + iconUrls +
        ", viewCount='" + viewCount + '\'' +
        ", likeCount='" + likeCount + '\'' +
        ", shareCount='" + shareCount + '\'' +
        ", commentCount='" + commentCount + '\'' +
        ", urlParamsMap=" + urlParamsMap +
        ", notifySrc='" + notifySrc + '\'' +
        ", isGrouped='" + isGroupable + '\'' +
        ", isPriority='" + imp.toString() + '\'' +
        ", isEvent='" + tags.toString() + '\'' +
        ", sourceId='" + sourceId + '\'' +
        ", ignoreSourceBlock='" + ignoreSourceBlock + '\'' +
        ", startTime='" + startTime + '\'' +
        ", displayedAtTime='" + displayedAtTime + '\'' +
        ", disableLangFilter='" + disableLangFilter + '\'' +
    '}';
  }

  private String msg;
  private String uniMsg = Constants.EMPTY_STRING;
  private NotificationSectionType sectionType;
  private long expiryTime;
  private NotificationLayoutType layoutType;
  private String language;
  private String languageCode;
  private int uniqueId;
  private boolean isUrdu;
  private String imageLink = Constants.EMPTY_STRING;
  private String imageLinkV2;
  private String bigImageLink;
  private String bigImageLinkV2;
  private String bigText;
  private String filterType;
  private int priority = NotificationConstants.DEFAULT_PRIORITY;
  //notification ticker
  private String tickerMessage;
  private boolean isBookDownloadNotification;
  private String inboxImageLink;

  private int state = NotificationConstants.NOTIFICATION_STATUS_UNREAD;
  private boolean isRemovedFromTray = false;
  private boolean isGrouped = false;
  private long timeStamp;
  private String edition;
  private NotificationDeliveryMechanism deliveryType = NotificationDeliveryMechanism.PUSH;
  private boolean isSynced;
  private boolean applyLanguageFilter;
  private String[] languages;

  // variables added below are buzz related
  private String buzzItemtype;

  //variables added below are for deferred notifications.
  private long v4DisplayTime;
  private boolean v4IsInternetRequired;
  private boolean isDeferred;

  //For notification_action event, we need to save this variable in the db so that it can be used
  // when the user dismisses the notification.
  private boolean isDeferredForAnalytics;
  private String v4BackUrl;
  private String v4SwipeRelUrl;
  private String v4SwipePageLogic;
  private String v4SwipePageLogicId;
  // type can be sticky etc
  private String type;
  // subtype can be cricket, politics etc.
  private String subType;
  private boolean isNotificationForDisplaying;
  private String group;
  private Map<String, String> experimentParams;
  private Map<String, String> queryParams;
  private String deeplink;
  private boolean doNotAutoFetchSwipeUrl;
  private String groupType;
  private String channelId;
  private String channelGroupId;
  private String notifType;    //“social” // empty for all other types as of now if comes social
  // then avoid filtering notification
  /**
   * Notification Subtype for eg :- CONTENT, FOLLOW_SUGGESTION etc
   */
  private String notifSubType;

  //added fields in profile release 15.0.0 for social comments
  private NotificationPlacementType placement;
  private List<String> iconUrls;

  private boolean isGroupable;
  private NotificationImportance imp;
  private List<String> tags;
  private String sourceId = null;
  private boolean ignoreSourceBlock = false;
  private long startTime = 0;
  private long displayedAtTime = -1;
  private InAppNotificationInfo inAppNotificationInfo;
  private boolean disableLangFilter = false;

  public NotificationPlacementType getPlacement() {
    return placement;
  }

  public void setPlacement(NotificationPlacementType placement) {
    this.placement = placement;
  }

  public List<String> getIconUrls() {
    return iconUrls;
  }

  public void setIconUrls(List<String> iconUrls) {
    this.iconUrls = iconUrls;
  }

  public void setV4BackUrl(String v4BackUrl) {
    this.v4BackUrl = v4BackUrl;
  }

  public void setV4SwipeUrl(String v4SwipeUrl) {
    this.v4SwipeRelUrl = v4SwipeUrl;
  }

  public String getV4BackUrl() {
    return v4BackUrl;
  }

  public String getV4SwipeUrl() {
    return v4SwipeRelUrl;
  }

  public String getCommentCount() {
    return commentCount;
  }

  public void setCommentCount(String commentCount) {
    this.commentCount = commentCount;
  }

  public String getShareCount() {
    return shareCount;
  }

  public void setShareCount(String shareCount) {
    this.shareCount = shareCount;
  }

  public String getLikeCount() {
    return likeCount;
  }

  public void setLikeCount(String likeCount) {
    this.likeCount = likeCount;
  }

  public String getViewCount() {
    return viewCount;
  }

  public void setViewCount(String viewCount) {
    this.viewCount = viewCount;
  }

  public String getBuzzItemtype() {
    return buzzItemtype;
  }

  public void setBuzzItemtype(String buzzItemtype) {
    this.buzzItemtype = buzzItemtype;
  }

  private String viewCount;
  private String likeCount;
  private String shareCount;
  private String commentCount;

  //news share source related variables
  private Map<String, String> urlParamsMap;

  public BaseInfo() {

  }

  public BaseInfo(BaseInfo copy) {
    id = copy.id;
    messageExpTimeStamp = copy.messageExpTimeStamp;
    sType = copy.sType;
    msg = copy.msg;
    uniMsg = copy.uniMsg;
    sectionType = copy.sectionType;
    expiryTime = copy.expiryTime;
    layoutType = copy.layoutType;
    language = copy.language;
    uniqueId = copy.uniqueId;
    isUrdu = copy.isUrdu;
    imageLink = copy.imageLink;
    imageLinkV2 = copy.imageLinkV2;
    bigImageLink = copy.bigImageLink;
    bigImageLinkV2 = copy.bigImageLinkV2;
    bigText = copy.bigText;
    priority = copy.priority;
    tickerMessage = copy.tickerMessage;
    isBookDownloadNotification = copy.isBookDownloadNotification;
    inboxImageLink = copy.inboxImageLink;
    state = copy.state;
    isRemovedFromTray = copy.isRemovedFromTray;
    isGrouped = copy.isGrouped;
    timeStamp = copy.timeStamp;
    v4DisplayTime = copy.v4DisplayTime;
    v4IsInternetRequired = copy.v4IsInternetRequired;
  }

  public boolean isDeferredForAnalytics() {
    return isDeferredForAnalytics;
  }

  public void setDeferredForAnalytics(boolean deferredForAnalytics) {
    isDeferredForAnalytics = deferredForAnalytics;
  }

  public BaseInfo(int uniqueId) {
    this.uniqueId = uniqueId;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public boolean isUrdu() {
    return isUrdu;
  }

  public void setUrdu(boolean isUrdu) {
    this.isUrdu = isUrdu;
  }

  public String getNotifySrc() {
    return notifySrc;
  }

  public void setNotifySrc(String notifySrc) {
    this.notifySrc = notifySrc;
  }

  private String notifySrc;

  public String getImageLink() {
    return imageLink;
  }

  public void setImageLink(String imageLink) {
    this.imageLink = imageLink;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMessageExpTimeStamp() {
    return messageExpTimeStamp;
  }

  public void setMessageExpTimeStamp(String messageExpTimeStamp) {
    this.messageExpTimeStamp = messageExpTimeStamp;
  }

  public String getsType() {
    return sType;
  }

  public void setsType(String sType) {
    this.sType = sType;
  }

  public String getMessage() {
    return msg;
  }

  public void setMessage(String message) {
    this.msg = message;
  }

  public String getUniMsg() {
    return uniMsg;
  }

  public void setUniMsg(String uniMsg) {
    this.uniMsg = uniMsg;
  }

  public NotificationSectionType getSectionType() {
    return sectionType;
  }

  public void setSectionType(NotificationSectionType sectionType) {
    this.sectionType = sectionType;
  }

  public long getExpiryTime() {
    return expiryTime;
  }

  public void setExpiryTime(long expiryTime) {
    this.expiryTime = expiryTime;
  }

  public NotificationLayoutType getLayoutType() {
    return layoutType;
  }

  public void setLayoutType(NotificationLayoutType layoutType) {
    this.layoutType = layoutType;
  }

  public int getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(int uniqueId) {
    this.uniqueId = uniqueId;
  }

  public String getLanguage() {
    return language;
  }

  public String getBigImageLink() {
    return bigImageLink;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setBigImageLink(String bigImageLink) {
    this.bigImageLink = bigImageLink;
  }

  public String getBigText() {
    return bigText;
  }

  public String getGroupType() {
    return groupType;
  }

  public void setGroupType(String groupType) {
    this.groupType = groupType;
  }

  public void setBigText(String bigText) {
    this.bigText = bigText;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getTickerMessage() {
    return tickerMessage;
  }

  public void setTickerMessage(String tickerMessage) {
    this.tickerMessage = tickerMessage;
  }

  public boolean getIsBookDownloadNotification() {
    return isBookDownloadNotification;
  }

  public void setIsBookDownloadNotification(boolean bookDownloadNotification) {
    isBookDownloadNotification = bookDownloadNotification;
  }

  public boolean isRemovedFromTray() {
    return isRemovedFromTray;
  }

  public void setIsRemovedFromTray(boolean isRemovedFromTray) {
    this.isRemovedFromTray = isRemovedFromTray;
  }

  public boolean isGrouped() {
    return isGrouped;
  }

  public void setIsGrouped(boolean isGrouped) {
    this.isGrouped = isGrouped;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public boolean isRead() {
    return getState() == NotificationConstants.NOTIFICATION_STATUS_READ;
  }

  public boolean wasSkippedByUser() {
    return getState() == NotificationConstants.NOTIFICATION_STATUS_SKIPPED_BY_USER;
  }

  public String getImageLinkV2() {
    return imageLinkV2;
  }

  public void setImageLinkV2(String imageLinkV2) {
    this.imageLinkV2 = imageLinkV2;
  }

  public String getBigImageLinkV2() {
    return bigImageLinkV2;
  }

  public void setBigImageLinkV2(String bigImageLinkV2) {
    this.bigImageLinkV2 = bigImageLinkV2;
  }

  public String getInboxImageLink() {
    return inboxImageLink;
  }

  public void setInboxImageLink(String inboxImageLink) {
    this.inboxImageLink = inboxImageLink;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  public NotificationDeliveryMechanism getDeliveryType() {
    return deliveryType;
  }

  public void setDeliveryType(NotificationDeliveryMechanism deliveryType) {
    this.deliveryType = deliveryType;
  }

  public boolean isSynced() {
    return isSynced;
  }

  public void setIsSynced(boolean synced) {
    isSynced = synced;
  }

  public boolean isApplyLanguageFilter() {
    return applyLanguageFilter;
  }

  public void setApplyLanguageFilter(boolean applyLanguageFilter) {
    this.applyLanguageFilter = applyLanguageFilter;
  }

  public String[] getLanguages() {
    return languages;
  }

  public void setLanguages(String[] languages) {
    this.languages = languages;
  }

  public Map<String, String> getUrlParamsMap() {
    return urlParamsMap;
  }

  public void setUrlParamsMap(Map<String, String> urlParamsMap) {
    this.urlParamsMap = urlParamsMap;
  }

  public long getV4DisplayTime() {
    return v4DisplayTime;
  }

  public void setV4DisplayTime(long v4DisplayTime) {
    this.v4DisplayTime = v4DisplayTime;
  }

  public boolean isV4IsInternetRequired() {
    return v4IsInternetRequired;
  }

  public void setV4IsInternetRequired(boolean v4IsInternetRequired) {
    this.v4IsInternetRequired = v4IsInternetRequired;
  }

  public boolean isDeferred() {
    return isDeferred;
  }

  public void setDeferred(boolean deferred) {
    isDeferred = deferred;
  }

  public String getV4SwipePageLogic() {
    return v4SwipePageLogic;
  }

  public void setV4SwipePageLogic(String v4SwipePageLogic) {
    this.v4SwipePageLogic = v4SwipePageLogic;
  }

  public String getV4SwipePageLogicId() {
    return v4SwipePageLogicId;
  }

  public void setV4SwipePageLogicId(String v4SwipePageLogicId) {
    this.v4SwipePageLogicId = v4SwipePageLogicId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSubType() {
    return subType;
  }

  public void setSubType(String subType) {
    this.subType = subType;
  }

  public boolean isNotificationForDisplaying() {
    return isNotificationForDisplaying;
  }

  public void setNotificationForDisplaying(boolean notificationForDisplaying) {
    isNotificationForDisplaying = notificationForDisplaying;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public Map<String, String> getExperimentParams() {
    return experimentParams;
  }

  public void setExperimentParams(Map<String, String> experimentParams) {
    this.experimentParams = experimentParams;
  }

  public Map<String, String> getQueryParams() {
    return queryParams;
  }

  public void setQueryParams(Map<String, String> queryParams) {
    this.queryParams = queryParams;
  }

  public String getDeeplink() {
    return deeplink;
  }

  public void setDeeplink(String deeplink) {
    this.deeplink = deeplink;
  }

  public boolean isDoNotAutoFetchSwipeUrl() {
    return doNotAutoFetchSwipeUrl;
  }

  public void setDoNotAutoFetchSwipeUrl(boolean doNotAutoFetchSwipeUrl) {
    this.doNotAutoFetchSwipeUrl = doNotAutoFetchSwipeUrl;
  }

  public String getChannelId() {
    return channelId;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }

  public String getChannelGroupId() {
    return channelGroupId;
  }

  public void setChannelGroupId(String channelGroupId) {
    this.channelGroupId = channelGroupId;
  }

  public String getFilterType() {
    return filterType;
  }

  public void setFilterType(String filterType) {
    this.filterType = filterType;
  }

  public String getNotifType() {
    return notifType;
  }

  public void setNotifType(String notifType) {
    this.notifType = notifType;
  }

  public String getNotifSubType() {
    return notifSubType;
  }

  public void setNotifSubType(String notifSubType) {
    this.notifSubType = notifSubType;
  }

  public String getDedupeKey() {
    return dedupeKey;
  }

  public void setDedupeKey(String dedupeKey) {
    this.dedupeKey = dedupeKey;
  }

  public void setGroupable(boolean groupable) {
    this.isGroupable = groupable;
  }

  public boolean getGroupable() {
    return this.isGroupable;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public List<String> getTags() {
    return this.tags;
  }

  public void setImp(NotificationImportance imp) {
    this.imp = imp;
  }

  public NotificationImportance getImp() {
    return this.imp;
  }

  public List<String> getDeeplinks() {
    return deeplinks;
  }

  public void setDeeplinks(List<String> deeplinks) {
    this.deeplinks = deeplinks;
  }

  public List<String> getPostIds() {
    return postIds;
  }

  public void setPostIds(List<String> postIds) {
    this.postIds = postIds;
  }

  public String getDeleteType() {
    return deleteType;
  }

  public void setDeleteType(String deleteType) {
    this.deleteType = deleteType;
  }

  public TimeRange getTimeRange() {
    return timeRange;
  }

  public void setTimeRange(TimeRange timeRange) {
    this.timeRange = timeRange;
  }

  public String getSourceId(){
    return sourceId;
  }

  public void setSourceId(String sourceId){
    this.sourceId = sourceId;
  }

  public boolean getIgnoreSourceBlock(){
    return ignoreSourceBlock;
  }

  public void setIgnoreSourceBlock(boolean ignoreSourceBlock){
    this.ignoreSourceBlock = ignoreSourceBlock;
  }

  public long getStartTime(){
    return startTime;
  }

  public void setStartTime(long startTime){
    this.startTime = startTime;
  }

  public long getDisplayedAtTime() {
    return displayedAtTime;
  }

  public void setDisplayedAtTime(long displayedAtTime){
    this.displayedAtTime = displayedAtTime;
  }

  public InAppNotificationInfo getInAppInfo() {
    return inAppNotificationInfo;
  }

  public void setInAppInfo(InAppNotificationInfo inAppNotificationInfo){
    this.inAppNotificationInfo = inAppNotificationInfo;
  }

  public  boolean isDisableLangFilter(){
    return disableLangFilter;
  }

  public void setDisableLangFilter(boolean disableLangFilter){
    this.disableLangFilter = disableLangFilter;
  }
}


