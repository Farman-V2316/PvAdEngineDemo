/*
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.notification;

import java.io.Serializable;

/**
 * Created by anshul on 8/3/17.
 * BaseModel for web related notifications and deeplinks.
 */

public class WebNavModel extends BaseModel implements Serializable {
  private String url;
  private String title;
  private String actionBarBackgroundColor;
  private String actionBarTitleTextColor;
  private boolean finishOnBackPress;
  private String id;
  private boolean disableActionBarMenu;
  private String appSection;
  private boolean logAnalyticsEvent;
  private boolean isBackButtonWhite = false;
  private String webPayLoad;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getActionBarBackgroundColor() {
    return actionBarBackgroundColor;
  }

  public void setActionBarBackgroundColor(String actionBarBackgroundColor) {
    this.actionBarBackgroundColor = actionBarBackgroundColor;
  }

  public String getActionBarTitleTextColor() {
    return actionBarTitleTextColor;
  }

  public void setActionBarTitleTextColor(String actionBarTitleTextColor) {
    this.actionBarTitleTextColor = actionBarTitleTextColor;
  }

  public boolean isFinishOnBackPress() {
    return finishOnBackPress;
  }

  public void setFinishOnBackPress(boolean finishOnBackPress) {
    this.finishOnBackPress = finishOnBackPress;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isDisableActionBarMenu() {
    return disableActionBarMenu;
  }

  public void setDisableActionBarMenu(boolean disableActionBarMenu) {
    this.disableActionBarMenu = disableActionBarMenu;
  }

  public String getAppSection() {
    return appSection;
  }

  public void setAppSection(String appSection) {
    this.appSection = appSection;
  }

  public boolean isLogAnalyticsEvent() {
    return logAnalyticsEvent;
  }

  public void setLogAnalyticsEvent(boolean logAnalyticsEvent) {
    this.logAnalyticsEvent = logAnalyticsEvent;
  }

  public boolean isBackButtonWhite() {
    return isBackButtonWhite;
  }

  public void setBackButtonWhite(boolean backButtonWhite) {
    isBackButtonWhite = backButtonWhite;
  }

  @Override
  public BaseModelType getBaseModelType() {
    return BaseModelType.WEB_MODEL;
  }

  public String getWebPayLoad() {
    return webPayLoad;
  }

  public void setWebPayLoad(String webPayLoad) {
    this.webPayLoad = webPayLoad;
  }

}


