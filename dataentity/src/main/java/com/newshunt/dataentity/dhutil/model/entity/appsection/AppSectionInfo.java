/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.appsection;

import com.newshunt.dataentity.common.model.entity.AppSection;

import java.io.Serializable;

/**
 * A simple POJO describing about app section info to be shown on NHTABVIEW
 *
 * @author santhosh.kc
 */
public class AppSectionInfo implements Serializable {

  private static final long serialVersionUID = -740741430412211242L;

  private AppSection type;

  public int globalPriority;

  public boolean isReplaceable;

  private String id;

  private String title;

  private String activeIconUrl;

  private String activeIconUrlNight;

  private String inactiveIconUrl;

  private String inactiveIconUrlNight;

  private String contentUrl;

  private String deeplinkUrl;

  private String activeIconFilePath;

  private String activeIconNightFilePath;

  private String inActiveIconFilepath;

  private String inActiveIconNightFilePath;

  private String activeTextColor;

  private String inactiveTextColor;

  private String pressedStateColor;

  private String pressedStateColorNight;

  private String refreshIconUrl;

  private String refreshIconUrlNight;

  private String refreshIconFilePath;

  private String refreshIconNightFilePath;

  private String badgeBgColor;

  private String badgeTextColor;

  private String bgColor;

  private String bgColorNight;

  private String activeBgColor;

  private String activeBgColorNight;

  private String bgType;

  private String strokeColor;

  private String strokeColorNight;

  private String highlightType;

  private HighlightParams highlightParams;

  private String langfilter;

  public AppSectionInfo() {

  }

  public AppSectionInfo(AppSectionInfo copyFrom) {
    type = copyFrom.type;
    id = copyFrom.id;
    title = copyFrom.title;
    activeIconUrl = copyFrom.activeIconUrl;
    activeIconUrlNight = copyFrom.activeIconUrlNight;
    inactiveIconUrl = copyFrom.inactiveIconUrl;
    inactiveIconUrlNight = copyFrom.inactiveIconUrlNight;
    contentUrl = copyFrom.contentUrl;
    activeIconFilePath = copyFrom.activeIconFilePath;
    activeIconNightFilePath = copyFrom.activeIconNightFilePath;
    inActiveIconFilepath = copyFrom.inActiveIconFilepath;
    inActiveIconNightFilePath = copyFrom.inActiveIconNightFilePath;
    activeTextColor = copyFrom.activeTextColor;
    inactiveTextColor = copyFrom.inactiveTextColor;
    pressedStateColor = copyFrom.pressedStateColor;
    refreshIconUrl = copyFrom.refreshIconUrl;
    refreshIconFilePath = copyFrom.refreshIconFilePath;
    badgeBgColor = copyFrom.badgeBgColor;
    badgeTextColor = copyFrom.badgeTextColor;
    bgColor = copyFrom.bgColor;
    bgColorNight = copyFrom.bgColorNight;
    bgType = copyFrom.bgType;
    strokeColor = copyFrom.strokeColor;
    strokeColorNight = copyFrom.strokeColorNight;
    highlightType = copyFrom.highlightType;
    highlightParams = copyFrom.highlightParams;
    langfilter = copyFrom.langfilter;
    deeplinkUrl = copyFrom.deeplinkUrl;
    activeBgColor = copyFrom.activeBgColor;
    activeBgColorNight = copyFrom.activeBgColorNight;
  }

  public AppSection getType() {
    return type;
  }

  public void setType(AppSection type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getActiveIconUrl() {
    return activeIconUrl;
  }

  public void setActiveIconUrl(String activeIconUrl) {
    this.activeIconUrl = activeIconUrl;
  }

  public String getInactiveIconUrl() {
    return inactiveIconUrl;
  }

  public void setInactiveIconUrl(String inactiveIconUrl) {
    this.inactiveIconUrl = inactiveIconUrl;
  }

  public String getContentUrl() {
    return contentUrl;
  }

  public void setContentUrl(String contentUrl) {
    this.contentUrl = contentUrl;
  }

  public String getActiveIconFilePath() {
    return activeIconFilePath;
  }

  public void setActiveIconFilePath(String activeIconFilePath) {
    this.activeIconFilePath = activeIconFilePath;
  }

  public String getInActiveIconFilepath() {
    return inActiveIconFilepath;
  }

  public void setInActiveIconFilepath(String inActiveIconFilepath) {
    this.inActiveIconFilepath = inActiveIconFilepath;
  }

  public String getActiveTextColor() {
    return activeTextColor;
  }

  public void setActiveTextColor(String activeTextColor) {
    this.activeTextColor = activeTextColor;
  }

  public String getInactiveTextColor() {
    return inactiveTextColor;
  }

  public void setInactiveTextColor(String inactiveTextColor) {
    this.inactiveTextColor = inactiveTextColor;
  }

  public String getPressedStateColor() {
    return pressedStateColor;
  }

  public void setPressedStateColor(String pressedStateColor) {
    this.pressedStateColor = pressedStateColor;
  }

  public String getRefreshIconUrl() {
    return refreshIconUrl;
  }

  public void setRefreshIconUrl(String refreshIconUrl) {
    this.refreshIconUrl = refreshIconUrl;
  }

  public String getRefreshIconFilePath() {
    return refreshIconFilePath;
  }

  public void setRefreshIconFilePath(String refreshIconFilePath) {
    this.refreshIconFilePath = refreshIconFilePath;
  }

  public String getBadgeBgColor() {
    return badgeBgColor;
  }

  public void setBadgeBgColor(String badgeBgColor) {
    this.badgeBgColor = badgeBgColor;
  }

  public String getBadgeTextColor() {
    return badgeTextColor;
  }

  public void setBadgeTextColor(String badgeTextColor) {
    this.badgeTextColor = badgeTextColor;
  }

  public String getActiveIconUrlNight() {
    return activeIconUrlNight;
  }

  public void setActiveIconUrlNight(String activeIconUrlNight) {
    this.activeIconUrlNight = activeIconUrlNight;
  }

  public String getInactiveIconUrlNight() {
    return inactiveIconUrlNight;
  }

  public void setInactiveIconUrlNight(String inactiveIconUrlNight) {
    this.inactiveIconUrlNight = inactiveIconUrlNight;
  }

  public String getActiveIconNightFilePath() {
    return activeIconNightFilePath;
  }

  public void setActiveIconNightFilePath(String activeIconNightFilePath) {
    this.activeIconNightFilePath = activeIconNightFilePath;
  }

  public String getInActiveIconNightFilePath() {
    return inActiveIconNightFilePath;
  }

  public void setInActiveIconNightFilePath(String inActiveIconNightFilePath) {
    this.inActiveIconNightFilePath = inActiveIconNightFilePath;
  }

  public String getRefreshIconUrlNight() {
    return refreshIconUrlNight;
  }

  public void setRefreshIconUrlNight(String refreshIconUrlNight) {
    this.refreshIconUrlNight = refreshIconUrlNight;
  }

  public String getRefreshIconNightFilePath() {
    return refreshIconNightFilePath;
  }

  public void setRefreshIconNightFilePath(String refreshIconNightFilePath) {
    this.refreshIconNightFilePath = refreshIconNightFilePath;
  }

  public String getPressedStateColorNight() {
    return pressedStateColorNight;
  }

  public void setPressedStateColorNight(String pressedStateColorNight) {
    this.pressedStateColorNight = pressedStateColorNight;
  }

  public String getBgColor() {
    return bgColor;
  }

  public void setBgColor(String bgColor) {
    this.bgColor = bgColor;
  }

  public String getBgColorNight() {
    return bgColorNight;
  }

  public void setBgColorNight(String bgColorNight) {
    this.bgColorNight = bgColorNight;
  }

  public String getBgType() {
    return bgType;
  }

  public void setBgType(String bgType) {
    this.bgType = bgType;
  }

  public String getStrokeColor() {
    return strokeColor;
  }

  public void setStrokeColor(String strokeColor) {
    this.strokeColor = strokeColor;
  }

  public String getStrokeColorNight() {
    return strokeColorNight;
  }

  public void setStrokeColorNight(String strokeColorNight) {
    this.strokeColorNight = strokeColorNight;
  }

  public String getHighlightType() {
    return highlightType;
  }

  public void setHighlightType(String highlightType) {
    this.highlightType = highlightType;
  }

  public HighlightParams getHighlightParams() {
    return highlightParams;
  }

  public void setHighlightParams(
      HighlightParams highlightParams) {
    this.highlightParams = highlightParams;
  }

  public String getLangfilter() {
    return langfilter;
  }

  public void setLangfilter(String langfilter) {
    this.langfilter = langfilter;
  }

  public String getActiveBgColor() {
    return activeBgColor;
  }

  public String getActiveBgColorNight() {
    return activeBgColorNight;
  }

  public String getDeeplinkUrl() {
    return deeplinkUrl;
  }

  public void setDeeplinkUrl(String deeplinkUrl) {
    this.deeplinkUrl = deeplinkUrl;
  }
}
