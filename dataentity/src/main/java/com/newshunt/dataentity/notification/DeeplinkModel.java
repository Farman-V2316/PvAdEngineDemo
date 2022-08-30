/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification;


import com.newshunt.common.helper.common.Constants;

import java.io.Serializable;

/**
 * @author santhosh.kc
 */
public class DeeplinkModel extends BaseModel implements Serializable {
  private static final long serialVersionUID = 6189346579187744175L;

  private String deeplinkUrl;
  private NotificationSectionType sectionType;
  private NotificationLayoutType layoutType;
  private boolean fallbackToHomeOnFailure;
  private boolean isAdjunct;
  private int popupDisplayType = Constants.NOT_SHOW_ADJUNCT_LANG_DISPLAY; // 0 for A screen, 1 for B screen and 2 for no display

  public String getDeeplinkUrl() {
    return deeplinkUrl;
  }

  public void setDeeplinkUrl(String deeplinkUrl) {
    this.deeplinkUrl = deeplinkUrl;
  }

  public NotificationSectionType getSectionType() {
    return sectionType;
  }

  public void setSectionType(
      NotificationSectionType sectionType) {
    this.sectionType = sectionType;
  }

  public NotificationLayoutType getLayoutType() {
    return layoutType;
  }

  public void setLayoutType(
      NotificationLayoutType layoutType) {
    this.layoutType = layoutType;
  }

  public boolean isFallbackToHomeOnFailure() {
    return fallbackToHomeOnFailure;
  }

  public void setFallbackToHomeOnFailure(boolean fallbackToHomeOnFailure) {
    this.fallbackToHomeOnFailure = fallbackToHomeOnFailure;
  }

  @Override
  public BaseModelType getBaseModelType() {
    return BaseModelType.DEEPLINK_MODEL;
  }

  public boolean isAdjunct() {
    return isAdjunct;
  }

  public void setAdjunct(boolean adjunct) {
    isAdjunct = adjunct;
  }

  public int getPopupDisplayType() {
    return popupDisplayType;
  }

  public void setPopupDisplayType(int popupDisplayType) {
    this.popupDisplayType = popupDisplayType;
  }
}
