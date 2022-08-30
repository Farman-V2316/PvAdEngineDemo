/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.dataentity.notification.asset;

import java.io.Serializable;
import java.util.List;

/**
 * Created by anshul on 25/08/17.
 */

public class BaseNotificationAsset implements Serializable {

  private static final long serialVersionUID = 1L;

  private int autoRefreshInterval = 1000;

  private String type; //Values

  private String id;

  private CommentaryState state;

  private String streamUrl;

  private String deeplinkUrl;

  private OptOutMeta optOutMeta;

  private long startTime;

  private long expiryTime;

  private int priority;

  private String streamTrackUrl;

  private String audioUrl;

  private String audioLanguage;
  private String channel;

  private int timeWindowForStreamTrackUrl; // in seconds

  private List<String> br;

  private List<String> excludeNotificationTags;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStreamUrl() {
    return streamUrl;
  }

  public void setStreamUrl(String streamUrl) {
    this.streamUrl = streamUrl;
  }

  public int getAutoRefreshInterval() {
    return autoRefreshInterval;
  }

  public void setAutoRefreshInterval(int autoRefreshInterval) {
    this.autoRefreshInterval = autoRefreshInterval;
  }

  public long getExpiryTime() {
    return expiryTime;
  }

  public void setExpiryTime(long expiryTime) {
    this.expiryTime = expiryTime;
  }

  public String getStreamTrackUrl() {
    return streamTrackUrl;
  }

  public int getTimeWindowForStreamTrackUrl() {
    return timeWindowForStreamTrackUrl;
  }


  public List<String> getBranding() {
    return br;
  }

  public void setBranding(List<String> br) {
    this.br = br;
  }


  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getDeeplinkUrl() {
    return deeplinkUrl;
  }

  public void setDeeplinkUrl(String deeplinkUrl) {
    this.deeplinkUrl = deeplinkUrl;
  }

  public OptOutMeta getOptOutMeta() {
    return optOutMeta;
  }

  public void setOptOutMeta(OptOutMeta optOutMeta) {
    this.optOutMeta = optOutMeta;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getAudioUrl() {
    return audioUrl;
  }

  public void setAudioUrl(String audioUrl) {
    this.audioUrl = audioUrl;
  }

  public String getAudioLanguage() {
    return audioLanguage;
  }

  public void setAudioLanguage(String audioLanguage) {
    this.audioLanguage = audioLanguage;
  }

  public CommentaryState getState() {
    return state;
  }

  public void setState(CommentaryState state) {
    this.state = state;
  }


  public List<String> getExcludeNotificationTags() {
    return excludeNotificationTags;
  }

  public void setExcludeNotificationTags(List<String> tags) {
    this.excludeNotificationTags = tags;
  }
}
