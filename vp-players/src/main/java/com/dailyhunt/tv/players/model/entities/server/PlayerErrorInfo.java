package com.dailyhunt.tv.players.model.entities.server;


import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset;

import java.io.Serializable;

/**
 * Created by Jayanth on 09/05/18.
 */
public class PlayerErrorInfo implements Serializable {
  private String itemId;
  private PlayerAsset assetType;
  private String videoFileType;
  private String errorMessage;

  public PlayerErrorInfo(PlayerAsset item, String errorMessage) {
    this.itemId = item.getId();
    this.errorMessage = errorMessage;
  }


  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public PlayerAsset getAssetType() {
    return assetType;
  }

  public void setAssetType(PlayerAsset assetType) {
    this.assetType = assetType;
  }

  public String getVideoFileType() {
    return videoFileType;
  }

  public void setVideoFileType(String videoFileType) {
    this.videoFileType = videoFileType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
