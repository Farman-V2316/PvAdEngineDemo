package com.newshunt.dataentity.notification;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.notification.util.NotificationConstants;

import java.io.Serializable;
import java.util.Map;

/**
 * Holds all the details which can be used to build navigation.
 *
 * @author santosh.kulkarni
 */
public class NavigationModel extends BaseModel implements Serializable {

  private static final long serialVersionUID = 1614796724587958123L;

  private String id;
  private String fKey;
  private String npKey;
  private String ctKey;
  private String newsId;
  private String bookId;
  private String bookListId;
  private String bookLanguage = Constants.EMPTY_STRING;
  private String imageLink;
  private NotificationSectionType sectionType;
  private NotificationLayoutType layoutType = NotificationLayoutType.NOTIFICATION_TYPE_SMALL;
  private String promoId;
  private boolean isUrdu;
  private String topicKey;
  private String bigImageLink;
  private String bigText;
  private int priority = NotificationConstants.DEFAULT_PRIORITY;
  private String imageLinkV2;
  private String bigImageLinkV2;
  private String inboxImageLink;
  //notification received time
  private long timeStamp;

  //Deferred notification fields
  private long v4DisplayTime;
  private boolean v4IsInternetRequired;

  // Test prep notificication fields
  private String uniMsg;
  private String msg;
  private long expiryTime;
  private String notifySrc;
  private String unitId;
  private String testId;
  private String studyMaterialId;
  private String collectionId;
  private String groupId;
  private String groupType;
  private String newsItemId;
  private String icon;
  private String image;
  private String myUnitsFilter;
  private String interestsGroupId;
  private String interestsId;
  private String heading;
  private String region;
  private String homePageGroupType;
  private String language;
  private String collectionUrl;
  private boolean retainInInbox;
  private String subGroupId;
  private String preferenceTabId;
  private String promotionId;
  private NotificationDeliveryMechanism deliveryType = NotificationDeliveryMechanism.PUSH;
  private boolean isSynced;
  private boolean applyLanguageFilter;
  private String[] languages;
  private String v4BackUrl;
  private String v4SwipeRelUrl;
  private String v4SwipePageLogic;
  private String v4SwipePageLogicId;
  private boolean isNotificationForDisplaying;
  private Map<String,String> experimentParams;
  private boolean doNotAutoFetchSwipeUrl;
  private String channelId;
  private String channelGroupId;
  private String filterType;
  private String notifType;    //“social” // empty for all other types as of now if comes social
  private String notifSubType;

  public String getFilterType() {
    return filterType;
  }

  public void setFilterType(String filterType) {
    this.filterType = filterType;
  }

  public String getPromotionId() {
    return promotionId;
  }

  public void setPromotionId(String promotionId) {
    this.promotionId = promotionId;
  }

  public boolean isRetainInInbox() {
    return retainInInbox;
  }

  public void setRetainInInbox(boolean retainInInbox) {
    this.retainInInbox = retainInInbox;
  }

  public String getPreferenceTabId() {
    return preferenceTabId;
  }

  public void setPreferenceTabId(String preferenceTabId) {
    this.preferenceTabId = preferenceTabId;
  }

  public String getSubGroupId() {
    return subGroupId;
  }

  public void setSubGroupId(String subGroupId) {
    this.subGroupId = subGroupId;
  }


  public String getNewsItemUrl() {
    return newsItemUrl;
  }

  public void setNewsItemUrl(String newsItemUrl) {
    this.newsItemUrl = newsItemUrl;
  }

  private String newsItemUrl;

  public String getGroupUrl() {
    return groupUrl;
  }

  public void setGroupUrl(String groupUrl) {
    this.groupUrl = groupUrl;
  }

  public String getCollectionUrl() {
    return collectionUrl;
  }

  public void setCollectionUrl(String collectionUrl) {
    this.collectionUrl = collectionUrl;
  }

  private String groupUrl;

  public String getCollectionId() {
    return collectionId;
  }

  public void setCollectionId(String collectionId) {
    this.collectionId = collectionId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getNewsItemId() {
    return newsItemId;
  }

  public void setNewsItemId(String newsItemId) {
    this.newsItemId = newsItemId;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPromoId() {
    return promoId;
  }

  public void setPromoId(String promoId) {
    this.promoId = promoId;
  }

  public NotificationLayoutType getLayoutType() {
    return layoutType;
  }

  public void setLayoutType(NotificationLayoutType layoutType) {
    this.layoutType = layoutType;
  }

  public boolean isUrdu() {
    return isUrdu;
  }

  public void setUrdu(boolean isUrdu) {
    this.isUrdu = isUrdu;
  }

  public NotificationSectionType getSectionType() {
    return sectionType;
  }

  public void setSectionType(NotificationSectionType sectionType) {
    this.sectionType = sectionType;
  }

  public String getImageLink() {
    return imageLink;
  }

  public void setImageLink(String imageLink) {
    this.imageLink = imageLink;
  }

  public NotificationSectionType getNotificationSectionType() {
    return sectionType;
  }

  public void setNotificationSectionType(NotificationSectionType sectionType) {
    this.sectionType = sectionType;
  }


  public String getfKey() {
    return fKey;
  }

  public void setfKey(String fKey) {
    this.fKey = fKey;
  }

  public String getNpKey() {
    return npKey;
  }

  public void setNpKey(String npKey) {
    this.npKey = npKey;
  }

  public String getCtKey() {
    return ctKey;
  }

  public void setCtKey(String ctKey) {
    this.ctKey = ctKey;
  }

  public String getNewsId() {
    return newsId;
  }

  public void setNewsId(String newsId) {
    this.newsId = newsId;
  }

  public String getBookId() {
    return bookId;
  }

  public void setBookId(String bookId) {
    this.bookId = bookId;
  }

  public String getBookListId() {
    return bookListId;
  }

  public void setBookListId(String bookListId) {
    this.bookListId = bookListId;
  }

  public String getBookLanguage() {
    return bookLanguage;
  }

  public void setBookLanguage(String bookLanguage) {
    this.bookLanguage = bookLanguage;
  }


  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getUniMsg() {
    return uniMsg;
  }

  public void setUniMsg(String uniMsg) {
    this.uniMsg = uniMsg;
  }

  public long getExpiryTime() {
    return expiryTime;
  }

  public void setExpiryTime(long expiryTime) {
    this.expiryTime = expiryTime;
  }

  public String getNotifySrc() {
    return notifySrc;
  }

  public void setNotifySrc(String notifySrc) {
    this.notifySrc = notifySrc;
  }

  public String getUnitId() {
    return unitId;
  }

  public void setUnitId(String unitId) {
    this.unitId = unitId;
  }

  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  public String getStudyMaterialId() {
    return studyMaterialId;
  }

  public void setStudyMaterialId(String studyMaterialId) {
    this.studyMaterialId = studyMaterialId;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getMyUnitsFilter() {
    return myUnitsFilter;
  }

  public void setMyUnitsFilter(String myUnitsFilter) {
    this.myUnitsFilter = myUnitsFilter;
  }

  public String getInterestsGroupId() {
    return interestsGroupId;
  }

  public void setInterestsGroupId(String interestsGroupId) {
    this.interestsGroupId = interestsGroupId;
  }

  public String getInterestsId() {
    return interestsId;
  }

  public void setInterestsId(String interestsId) {
    this.interestsId = interestsId;
  }

  public String getHeading() {
    return heading;
  }

  public void setHeading(String heading) {
    this.heading = heading;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getHomePageGroupType() {
    return homePageGroupType;
  }

  public void setHomePageGroupType(String homePageGroupType) {
    this.homePageGroupType = homePageGroupType;
  }

  public String getTopicKey() {
    return topicKey;
  }

  public void setTopicKey(String topicKey) {
    this.topicKey = topicKey;
  }

  public String getBigImageLink() {
    return bigImageLink;
  }

  public void setBigImageLink(String bigImageLink) {
    this.bigImageLink = bigImageLink;
  }

  public String getBigText() {
    return bigText;
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

  public long getV4DisplayTime() {
    return v4DisplayTime;
  }

  public void setV4DisplayTime(long v4DisplayTime) {
    this.v4DisplayTime = v4DisplayTime;
  }

  public boolean getV4IsInternetRequired() {
    return v4IsInternetRequired;
  }

  public void setV4IsInternetRequired(boolean v4IsInternetRequired) {
    this.v4IsInternetRequired = v4IsInternetRequired;
  }

  public String getGroupType() {
    return groupType;
  }

  public void setGroupType(String groupType) {
    this.groupType = groupType;
  }

  public int createUniqueIdForBooks() {
    StringBuffer uniqueStrBuf = new StringBuffer();
    if (sectionType != null) {
      uniqueStrBuf.append(sectionType);
    }
    if (sType != null) {
      uniqueStrBuf.append(sType);
    }
    if (bookLanguage != null) {
      uniqueStrBuf.append(bookLanguage);
    }
    if (bookListId != null) {
      uniqueStrBuf.append(bookListId);
    }
    if (promoId != null) {
      uniqueStrBuf.append(promoId);
    }

    if (uniqueStrBuf.length() == 0) {
      return (int) System.currentTimeMillis();
    }
    return uniqueStrBuf.toString().hashCode();
  }

  public int createUniqueIdForNews() {
    StringBuffer uniqueStrBuf = new StringBuffer();
    if (sectionType != null) {
      uniqueStrBuf.append(sectionType);
    }
    if (sType != null) {
      uniqueStrBuf.append(sType);
    }
    if (language != null) {
      uniqueStrBuf.append(language);
    }
    if (npKey != null) {
      uniqueStrBuf.append(npKey);
    }
    if (ctKey != null) {
      uniqueStrBuf.append(ctKey);
    }

    if (uniqueStrBuf.length() == 0) {
      return (int) System.currentTimeMillis();
    }
    return uniqueStrBuf.toString().hashCode();
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

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
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

  public String getV4BackUrl() {
    return v4BackUrl;
  }

  public String getV4SwipeUrl() {
    return v4SwipeRelUrl;
  }

  public void setV4BackUrl(String v4BackUrl) {
    this.v4BackUrl = v4BackUrl;
  }

  public void setV4SwipeUrl(String v4SwipeUrl) {
    this.v4SwipeRelUrl = v4SwipeUrl;
  }

  public void setV4SwipePageLogicId(String v4SwipePageLogicId) {
    this.v4SwipePageLogicId = v4SwipePageLogicId;
  }

  public String getV4SwipePageLogicId() {
    return v4SwipePageLogicId;
  }

  public void setV4SwipePageLogic(String v4SwipePageLogic) {
    this.v4SwipePageLogic = v4SwipePageLogic;
  }

  public String getV4SwipePageLogic() {
    return v4SwipePageLogic;
  }

  @Override
  public BaseModelType getBaseModelType() {
    return BaseModelType.NAVIGATION_MODEL;
  }

  public boolean isNotificationForDisplaying() {
    return isNotificationForDisplaying;
  }

  public void setNotificationForDisplaying(boolean notificationForDisplaying) {
    isNotificationForDisplaying = notificationForDisplaying;
  }

  public Map<String, String> getExperimentParams() {
    return experimentParams;
  }

  public void setExperimentParams(Map<String, String> experimentParams) {
    this.experimentParams = experimentParams;
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
}
