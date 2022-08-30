package com.newshunt.dataentity.dhutil.model.entity.players;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.dailyhunt.tv.exolibrary.download.config.CacheConfig;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author rohit
 */

public class PlayerUpgradeInfo implements Serializable {

  private static final long serialVersionUID = -1257421655060628154L;

  private Map<String, String> groupVersions;

  private PlayerDimensions dimensions;

  private boolean clickAutoPlay = true;

  private boolean swipeAutoPlay = false;

  //Added for exo and webPlayer re-direct url encryption
  private String dhStreamConfig1;

  private String dhStreamConfig2;

  private List<PlayerUnifiedWebPlayer> players;

  private List<PlayerEvents> playerEvents;

  private boolean enablePerfAnalytics;

  private boolean useOkHttpDS;

  //Used to delay other api request over video request
  private long slowNetworkTime;
  private long goodNetworkTime;

  private CacheConfig cacheConfig;

  public List<PlayerUnifiedWebPlayer> getPlayers() {
    return players;
  }

  public void setPlayers(List<PlayerUnifiedWebPlayer> players) {
    this.players = players;
  }

  public Map<String, String> getGroupVersions() {
    return groupVersions;
  }

  private String version;
  int uniqueRequestId;

  public void setGroupVersions(Map<String, String> groupVersions) {
    this.groupVersions = groupVersions;
  }

  public PlayerDimensions getDimensions() {
    return dimensions;
  }

  public void setDimensions(PlayerDimensions playerDimensions) {
    this.dimensions = playerDimensions;
  }

  public boolean isClickAutoPlay() {
    return clickAutoPlay;
  }

  public boolean isSwipeAutoPlay() {
    return swipeAutoPlay;
  }

  public void setSwipeAutoPlay(boolean swipeAutoPlay) {
    this.swipeAutoPlay = swipeAutoPlay;
  }

  public void setClickAutoPlay(boolean clickAutoPlay) {
    this.clickAutoPlay = clickAutoPlay;
  }

  public String getDhStreamConfig1() {
    return dhStreamConfig1;
  }

  public void setDhStreamConfig1(String dhStreamConfig1) {
    this.dhStreamConfig1 = dhStreamConfig1;
  }

  public String getDhStreamConfig2() {
    return dhStreamConfig2;
  }

  public void setDhStreamConfig2(String dhStreamConfig2) {
    this.dhStreamConfig2 = dhStreamConfig2;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setUniqueRequestId(int uniqueRequestId) {
    this.uniqueRequestId = uniqueRequestId;
  }

  public List<PlayerEvents> getPlayerEvents() {
    return playerEvents;
  }

  public void setPlayerEvents(
      List<PlayerEvents> playerEvents) {
    this.playerEvents = playerEvents;
  }

  public boolean isEnablePerfAnalytics() {
    return enablePerfAnalytics;
  }

  public boolean isUseOkHttpDS() {
    return useOkHttpDS;
  }

  public long getSlowNetworkTime() {
    return slowNetworkTime;
  }

  public void setSlowNetworkTime(long slowNetworkTime) {
    this.slowNetworkTime = slowNetworkTime;
  }

  public long getGoodNetworkTime() {
    return goodNetworkTime;
  }

  public void setGoodNetworkTime(long goodNetworkTime) {
    this.goodNetworkTime = goodNetworkTime;
  }


  public CacheConfig getCacheConfig() {
    return cacheConfig;
  }

  public void setCacheConfig(CacheConfig cacheConfig) {
    this.cacheConfig = cacheConfig;
  }
}