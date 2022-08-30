package com.newshunt.dataentity.dhutil.model.entity.players;

import java.io.Serializable;

/**
 * Created by santoshkulkarni on 15/05/17.
 */

public class PlayerUnifiedWebPlayer implements Serializable {

  private String version;

  private String playerKey; //playerKey is used only to download player JS
  private String sourceKey; //sourceKey is used as a part of autoplayPlayerTypes in request Payload[playerKey cannot be used here]
  private String sourceBaseUrl;
  private String data;
  private PlayerAutoplaySupport android;
  private String userAgentString;

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getPlayerKey() {
    return playerKey;
  }

  public void setPlayerKey(String playerKey) {
    this.playerKey = playerKey;
  }

  public String getSourceBaseUrl() {
    return sourceBaseUrl;
  }

  public void setSourceBaseUrl(String sourceBaseUrl) {
    this.sourceBaseUrl = sourceBaseUrl;
  }

  public PlayerAutoplaySupport getAndroidSupport() {
    return android;
  }

  public void setAndroidSupport(PlayerAutoplaySupport android) {
    this.android = android;
  }

  public String getSourceKey() {
    return sourceKey;
  }

  public void setSourceKey(String sourceKey) {
    this.sourceKey = sourceKey;
  }

  public String getUserAgentString() {
    return userAgentString;
  }

  public void setUserAgentString(String userAgentString) {
    this.userAgentString = userAgentString;
  }
}
