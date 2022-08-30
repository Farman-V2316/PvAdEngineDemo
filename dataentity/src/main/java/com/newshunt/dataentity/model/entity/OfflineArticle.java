/*
 * Copyright (c) 2020 . All rights reserved.
 */
package com.newshunt.dataentity.model.entity;

import com.newshunt.dataentity.news.model.entity.server.asset.AssetType;

/**
 * POJO that will save to DB. Contains required fields from BaseAsset. DO NOT MODIFY
 * @author satosh.dhanyamraju
 */
@Deprecated
public class OfflineArticle {
  /**
   * identifier of the asset
   */
  private String id;
  /**
   * type of the asset such as photo, story, etc.
   */
  private AssetType type;
   /**
   * saved time of the content
   */
  private long interactionTime;

  private String groupType;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AssetType getType() {
    return type;
  }

  public void setType(AssetType type) {
    this.type = type;
  }

  public long getInteractionTime() {
    return interactionTime;
  }
  public void setInteractionTime(long interactionTime) {
    this.interactionTime = interactionTime;
  }

  public String getGroupType() {
    return groupType;
  }

  public void setGroupType(String groupType) {
    this.groupType = groupType;
  }
}
