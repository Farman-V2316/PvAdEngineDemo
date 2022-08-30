/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.dataentity.notification.asset;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author shrikant on 27/08/17.
 */

public class CricketDataStreamAsset extends BaseDataStreamAsset implements Serializable {

  private static final int EXPIRED_FEATURE_MASK = 1;
  private static final int LIVE_FEATURE_MASK = 2;
  private static final int FINISHED_FEATURE_MASK = 4;
  private static final int TEST_MATCH_FEATURE_MASK = 8;
  private static final int SUPER_OVER_FEATURE_MASK = 16;
  private static final int FORCE_STOP_AUDIO_COMMENTARY = 32;
  private static final int SUSPENDED_FEATURE_MASK = 64;
  private static final int LOGGING_DISABLED_MASK = 128;

  @SerializedName("t11")
  private CricketScoreAsset team1FirstInningsScore;

  @SerializedName("t12")
  private CricketScoreAsset team1SecondInningsScore;

  @SerializedName("t21")
  private CricketScoreAsset team2FirstInningsScore;

  @SerializedName("t22")
  private CricketScoreAsset team2SecondInningsScore;

  @SerializedName("s")
  private String state;

  @SerializedName("v")
  private Long version;

  @SerializedName("rt")
  private Integer autoRefreshInterval;

  @SerializedName("f")
  private Integer features;

  @SerializedName("b")
  private ArrayList<String> balls;


  private long nextStartTime; // nextStartTime for this notification

  @SerializedName("et")
  private long expiryTime; // updated expiryTime of the match

  @SerializedName("t")
  private String title;

  @SerializedName("lt")
  private String liveTitle;

  @SerializedName("l1")
  private String line1Text;

  @SerializedName("l2")
  private String line2Text;

  @SerializedName("au")
  private String audioUrl;

  @SerializedName("al")
  private String audioLanguage;


  public CricketScoreAsset getTeam1FirstInningsScore() {
    return team1FirstInningsScore;
  }

  public void setTeam1FirstInningsScore(CricketScoreAsset team1FirstInningsScore) {
    this.team1FirstInningsScore = team1FirstInningsScore;
  }

  public CricketScoreAsset getTeam1SecondInningsScore() {
    return team1SecondInningsScore;
  }

  public void setTeam1SecondInningsScore(CricketScoreAsset team1SecondInningsScore) {
    this.team1SecondInningsScore = team1SecondInningsScore;
  }

  public CricketScoreAsset getTeam2FirstInningsScore() {
    return team2FirstInningsScore;
  }

  public void setTeam2FirstInningsScore(CricketScoreAsset team2FirstInningsScore) {
    this.team2FirstInningsScore = team2FirstInningsScore;
  }

  public CricketScoreAsset getTeam2SecondInningsScore() {
    return team2SecondInningsScore;
  }

  public void setTeam2SecondInningsScore(CricketScoreAsset team2SecondInningsScore) {
    this.team2SecondInningsScore = team2SecondInningsScore;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public boolean isExpired() {
    return features != null && (features & EXPIRED_FEATURE_MASK) > 0;
  }
  public boolean isSuspended(){
    return features != null && (features & SUSPENDED_FEATURE_MASK) > 0;
  }

  public Integer getAutoRefreshInterval() {
    return autoRefreshInterval == null ? 0 : autoRefreshInterval;
  }

  public void setAutoRefreshInterval(Integer autoRefreshInterval) {
    this.autoRefreshInterval = autoRefreshInterval;
  }

  public boolean isLive() {
    return features != null && (features & LIVE_FEATURE_MASK) > 0;
  }

  public boolean isFinished() {
    return features != null && (features & FINISHED_FEATURE_MASK) > 0;
  }

  public boolean isAudioCommentaryForceStopped() {
    return features != null && (features & FORCE_STOP_AUDIO_COMMENTARY) > 0;
  }

  public boolean isTestMatch() {
    return features != null && (features & TEST_MATCH_FEATURE_MASK) > 0;
  }

  public boolean isSuperOver() {
    return features != null && (features & SUPER_OVER_FEATURE_MASK) > 0;
  }

  public void setFeatures(Integer features) {
    this.features = features;
  }

  public long getNextStartTime() {
    return nextStartTime;
  }

  public void setNextStartTime(long nextStartTime) {
    this.nextStartTime = nextStartTime;
  }

  public long getExpiryTime() {
    return expiryTime;
  }

  public void setExpiryTime(long expiryTime) {
    this.expiryTime = expiryTime;
  }

  public String getTitle() {
    return title;
  }

  public String getLiveTitle() {
    return liveTitle;
  }

  public String getLine1Text() {
    return line1Text;
  }

  public String getLine2Text() {
    return line2Text;
  }

  public ArrayList<String> getBalls() {
    return balls;
  }

  public void setBalls(ArrayList<String> balls) {
    this.balls = balls;
  }

  @Override
  public long getScheduledExpiryTime() {
    return expiryTime;
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

  public boolean isLoggingNotificationEventsDisabled(){
    return features != null && (features & LOGGING_DISABLED_MASK) > 0;
  }
}
