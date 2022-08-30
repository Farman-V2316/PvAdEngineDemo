package com.newshunt.dataentity.news.model.entity.server.asset;

import java.io.Serializable;

/**
 * Created by vinod on 23/04/18.
 */

public class SourceInfo implements Serializable {

  private String sourceId;

  private String sourceName;

  private String sourceType;

  private String sourceSubType;

  private String legacyKey;

  private String playerKey;


  //Variable flag used in EXO to send client-Information
  private boolean addClientInfo;

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public boolean isAddClientInfo() {
    return addClientInfo;
  }

  public void setAddClientInfo(boolean addClientInfo) {
    this.addClientInfo = addClientInfo;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getSourceSubType() {
    return sourceSubType;
  }

  public void setSourceSubType(String sourceSubType) {
    this.sourceSubType = sourceSubType;
  }

  public String getLegacyKey() {
    return legacyKey;
  }

  public void setLegacyKey(String legacyKey) {
    this.legacyKey = legacyKey;
  }

  public String getPlayerKey() {
    return playerKey;
  }

  public void setPlayerKey(String playerKey) {
    this.playerKey = playerKey;
  }
}
