/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Represents event to be fired when user dislikes a card.
 *
 * @author satosh.dhanyamraju
 */

//TODO (santhosh.kc) to remove this file and directly use BaseAsset once buzz to news integrated
public class DislikeStoryCardEvent {

  // SerializedNames are copied from 12.0.5 mapping file to prevent crash.
  // https://fabric.io/verse-innovation-pvt-ltd--bangalore/android/apps/com.eterno/issues/5c057c90f8b88c2963669e79?time=last-seven-days

  @SerializedName("a")
  private String storyId;

  @SerializedName("b")
  private String groupId;

  @SerializedName("c")
  private String publisherId;

  @SerializedName("d")
  private String categoryId;

  @SerializedName("e")
  private String contentType;

  @SerializedName("f")
  private String itemType;

  @SerializedName("g")
  private String uiType;

  @SerializedName("h")
  private String landingType;

  @SerializedName("i")
  private String cardLabel;

  @SerializedName("j")
  private String groupType;

  @SerializedName("k")
  private Map<String, String> itemLanguage;

  @SerializedName("l")
  private Map<String, String> experiment;

  @SerializedName("m")
  private boolean isPerSession;

  @SerializedName("n")
  private long timeStamp;

  @SerializedName("o")
  private String options;

  public String getPublisherId() {
    return publisherId;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public Map<String, String> getItemLanguage() {
    return itemLanguage;
  }

  public DislikeStoryCardEvent(String storyId, String groupId, String publisherId,
                               String categoryId, Map<String, String> itemLanguage,
                               Map<String, String> experiment, String contentType, String
                                   itemType, String uiType, String landingType, String cardLabel,
                               String groupType, boolean isPerSession, String options) {
    this.storyId = storyId;
    this.groupId = groupId;

    this.contentType = contentType;
    this.itemType = itemType;
    this.uiType = uiType;
    this.landingType = landingType;
    this.cardLabel = cardLabel;
    this.groupType = groupType;

    this.publisherId = publisherId;
    this.categoryId = categoryId;
    this.itemLanguage = itemLanguage;
    this.experiment = experiment;
    this.isPerSession = isPerSession;
    this.options = options;
  }

  public String getStoryId() {
    return storyId;
  }

  public String getGroupId() {
    return groupId;
  }

  public Map<String, String> getExperiment() {
    return experiment;
  }

  public String getContentType() {
    return contentType;
  }

  public String getItemType() {
    return itemType;
  }

  public String getUiType() {
    return uiType;
  }

  public String getLandingType() {
    return landingType;
  }

  public String getCardLabel() {
    return cardLabel;
  }

  public String getGroupType() {
    return groupType;
  }

  public boolean isPerSession() {
    return isPerSession;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getOptions() {
    return options;
  }
}
