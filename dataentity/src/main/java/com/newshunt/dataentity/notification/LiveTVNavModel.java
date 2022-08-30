package com.newshunt.dataentity.notification;

import java.io.Serializable;

/**
 * Created by santosh.kumar on 7/27/2016.
 */
public class LiveTVNavModel extends BaseModel implements Serializable {

  private static final long serialVersionUID = -440451667778144100L;

  private NotificationSectionType sectionType;
  private NotificationLayoutType layoutType;

  public Long getDate() {
    return date;
  }

  public void setDate(Long date) {
    this.date = date;
  }

  private Long date;

  // BaseInfo baseInfo;
  private String unitId;
  private String collectionId;
  private String collectionTitle;
  private String groupId;
  private String buzzItemId;
  private String newsItemId;
  private String groupTitle;
  private String subGroupId;
  private String subGroupTitle;
  private String region;
  private boolean retainInInbox;
  private String preferenceTabId;
  private String promotionId;

  public String getTvItemLanguage() {
    return tvItemLanguage;
  }

  public void setTvItemLanguage(String tvItemLanguage) {
    this.tvItemLanguage = tvItemLanguage;
  }

  private String tvItemLanguage;

  public LiveTVNavModel() {
  }

//  public BaseInfo getNotification() {
//    return baseInfo;
//  }
//
//  public void setNotification(BaseInfo notification) {
//    this.baseInfo = notification;
//  }

  public String getUnitId() {
    return unitId;
  }

  public void setUnitId(String unitId) {
    this.unitId = unitId;
  }

  public String getCollectionId() {
    return collectionId;
  }

  public void setCollectionId(String collectionId) {
    this.collectionId = collectionId;
  }

  public String getCollectionTitle() {
    return collectionTitle;
  }

  public void setCollectionTitle(String collectionTitle) {
    this.collectionTitle = collectionTitle;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getGroupTitle() {
    return groupTitle;
  }

  public void setGroupTitle(String groupTitle) {
    this.groupTitle = groupTitle;
  }

  public String getSubGroupId() {
    return subGroupId;
  }

  public String getSubGroupTitle() {
    return subGroupTitle;
  }

  public void setSubGroupTitle(String subGroupTitle) {
    this.subGroupTitle = subGroupTitle;
  }

  public void setSubGroupId(String subGroupId) {
    this.subGroupId = subGroupId;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
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

  public String getPromotionId() {
    return promotionId;
  }

  public void setPromotionId(String promotionId) {
    this.promotionId = promotionId;
  }

  public NotificationLayoutType getLayoutType() {
    return layoutType;
  }

  public String getNewsItemId() {
    return newsItemId;
  }

  public void setNewsItemId(String newsItemId) {
    this.newsItemId = newsItemId;
  }

  public String getBuzzItemId() {
    return buzzItemId;
  }

  public void setBuzzItemId(String buzzItemId) {
    this.buzzItemId = buzzItemId;
  }

  public void setLayoutType(
      NotificationLayoutType layoutType) {
    this.layoutType = layoutType;
  }

  public NotificationSectionType getSectionType() {
    return sectionType;
  }

  public void setSectionType(
      NotificationSectionType sectionType) {
    this.sectionType = sectionType;
  }

  @Override
  public BaseModelType getBaseModelType() {
    return BaseModelType.LIVETV_MODEL;
  }
}
