package com.newshunt.dataentity.notification;


import androidx.annotation.Nullable;

import com.newshunt.common.helper.common.Constants;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by santosh.kumar on 10/30/2015.
 */
public class NewsNavModel extends BaseModel implements Serializable {

  private static final long serialVersionUID = -3129964307467456300L;
  private String edition;
  private String npKey;
  private String ctKey;
  private String newsId;
  private String parentNewsId;
  private NotificationSectionType sectionType;
  private NotificationLayoutType layoutType;
  private String promoId;
  private boolean isUrdu;
  private String topicKey;
  private String subTopicKey;
  private String locationKey;
  private String subLocationKey;
  private String fKey;
  private String imageLink;
  private String language;
  @Nullable
  private String groupId;
  private String viralId;
  private Map<String,String> socialCommentParams;
  private String groupKey;
  private String entityType;
  private boolean isAdjunct;
  private int popupDisplayType = Constants.NOT_SHOW_ADJUNCT_LANG_DISPLAY; // 0 for A screen, 1 for B screen and 2 for no display


  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  public String getImageLink() {
    return imageLink;
  }

  public void setImageLink(String imageLink) {
    this.imageLink = imageLink;
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

  public NotificationSectionType getSectionType() {
    return sectionType;
  }

  public void setSectionType(NotificationSectionType sectionType) {
    this.sectionType = sectionType;
  }

  public NotificationLayoutType getLayoutType() {
    return layoutType;
  }

  public void setLayoutType(NotificationLayoutType layoutType) {
    this.layoutType = layoutType;
  }

  public String getPromoId() {
    return promoId;
  }

  public void setPromoId(String promoId) {
    this.promoId = promoId;
  }

  public boolean isUrdu() {
    return isUrdu;
  }

  public void setUrdu(boolean isUrdu) {
    this.isUrdu = isUrdu;
  }

  public String getTopicKey() {
    return topicKey;
  }

  public void setTopicKey(String topicKey) {
    this.topicKey = topicKey;
  }

  @Override
  public String getCategoryId() {
    return ctKey;
  }

  @Override
  public String getPublisherId() {
    return npKey;
  }

  @Override
  public String getTopicId() {
    return topicKey;
  }

  @Override
  public BaseModelType getBaseModelType() {
    return BaseModelType.NEWS_MODEL;
  }

  @Override
  public String getItemId() {
    return newsId;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getLocationKey() {
    return locationKey;
  }

  public void setLocationKey(String locationKey) {
    this.locationKey = locationKey;
  }

  public String getSubTopicKey() {
    return subTopicKey;
  }

  public void setSubTopicKey(String subTopicKey) {
    this.subTopicKey = subTopicKey;
  }

  public String getSubLocationKey() {
    return subLocationKey;
  }

  public void setSubLocationKey(String subLocationKey) {
    this.subLocationKey = subLocationKey;
  }

  public String getParentNewsId() {
    return parentNewsId;
  }

  public void setParentNewsId(String parentNewsId) {
    this.parentNewsId = parentNewsId;
  }

  public @Nullable String getGroupId() {
    return groupId;
  }

  public void setGroupId(@Nullable String groupId) {
    this.groupId = groupId;
  }

  public String getViralId() {
    return viralId;
  }

  public void setViralId(String viralId) {
    this.viralId = viralId;
  }

  public Map<String, String> getSocialCommentParams() {
    return socialCommentParams;
  }

  public void setSocialCommentParams(Map<String, String> socialCommentParams) {
    this.socialCommentParams = socialCommentParams;
  }

  public String getGroupKey() {
    return groupKey;
  }

  public void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public int getPopupDisplayType() {
    return popupDisplayType;
  }

  public void setPopupDisplayType(int popupDisplayType) {
    this.popupDisplayType = popupDisplayType;
  }

  public boolean isAdjunct() {
    return isAdjunct;
  }

  public void setAdjunct(boolean adjunct) {
    isAdjunct = adjunct;
  }
}
