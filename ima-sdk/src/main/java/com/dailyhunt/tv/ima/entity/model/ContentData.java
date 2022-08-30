package com.dailyhunt.tv.ima.entity.model;


import com.dailyhunt.tv.exolibrary.VideoQualitySettings;

/**
 * Data Model for holding the Content Data
 *
 * @author ranjith
 */

public class ContentData {

  private String videoUrl;
  private String adUrl;
  private boolean isEnableSettings;
  private boolean isLive;
  private boolean muteMode;
  private boolean addClientInfo;
  private boolean applyBufferSettings;

  private VideoQualitySettings videoQualitySettings;

  public ContentData(String videoUrl, String adUrl, boolean isEnableSettings) {
    this(videoUrl, adUrl, false, false, false, false, false);
  }

  public ContentData(String videoUrl, String adUrl, boolean isEnableSettings, boolean isLive,
                     boolean muteMode, boolean addClientInfo, boolean applyBufferSettings) {
    this.videoUrl = videoUrl;
    this.adUrl = adUrl;
    this.isEnableSettings = isEnableSettings;
    this.isLive = isLive;
    this.muteMode = muteMode;
    this.addClientInfo = addClientInfo;
    this.applyBufferSettings = applyBufferSettings;
  }

  public VideoQualitySettings getVideoQualitySettings() {
    return videoQualitySettings;
  }

  public void setVideoQualitySettings(final VideoQualitySettings videoQualitySettings) {
    this.videoQualitySettings = videoQualitySettings;
  }

  public String getAdUrl() {
    return adUrl;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  public boolean isEnableQualitySettings() {
    return isEnableSettings;
  }

  public boolean isLive() {
    return isLive;
  }

  public void setLive(boolean live) {
    isLive = live;
  }

  public boolean isMuteMode() {
    return muteMode;
  }

  public boolean isAddClientInfo() {
    return addClientInfo;
  }

  public boolean isApplyBufferSettings() {
    return applyBufferSettings;
  }

  public void setApplyBufferSettings(boolean applyBufferSettings) {
    this.applyBufferSettings = applyBufferSettings;
  }
}
