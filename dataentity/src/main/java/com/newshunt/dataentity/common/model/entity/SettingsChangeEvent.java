package com.newshunt.dataentity.common.model.entity;

/**
 * Events to be broadcast whenever settings will be changed.
 *
 * @author anand.winjit
 */
public class SettingsChangeEvent {
  private ChangeType changeType;
  private Long timeStamp = System.currentTimeMillis();
  private int handshakeType = -1;
  private Boolean adjunctHandshakeFlag = null;

  public SettingsChangeEvent(ChangeType changeType) {
    this.changeType = changeType;
  }

  public SettingsChangeEvent(ChangeType changeType, int handshakeType) {
    this.changeType = changeType;
    this.handshakeType = handshakeType;
  }

  public SettingsChangeEvent(ChangeType changeType, int handshakeType, Boolean adjunctHandshakeFlag) {
    this.changeType = changeType;
    this.handshakeType = handshakeType;
    this.adjunctHandshakeFlag = adjunctHandshakeFlag;
  }

  public ChangeType getChangeType() {
    return changeType;
  }

  public Long getTimeStamp() {
    return timeStamp;
  }

  public int getHandshakeType() {
    return handshakeType;
  }

  public Boolean getAdjunctHandshakeFlag(){
    return adjunctHandshakeFlag;
  }

  public enum ChangeType {
    THEME, LANGUAGES, NOTIFICATION, CURRENCY, APP_LANGUAGE, BOLD_STYLE,CARD_STYLE
  }
}
