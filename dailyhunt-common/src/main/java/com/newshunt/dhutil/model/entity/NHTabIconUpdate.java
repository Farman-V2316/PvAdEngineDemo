/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.entity;

import com.newshunt.dataentity.common.helper.common.ImageDownloadSourceType;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.dhutil.view.NHTabIconClickInterceptor;

/**
 * A Dynamic {@link com.newshunt.dhutil.view.customview.NHTabView} icon update POJO.
 * This same class can be used to reset back the NHTabView icon back to its default or whatever
 * configured
 *
 * @author santhosh.kc
 */
public class NHTabIconUpdate {

  private final UserAppSection userAppSection;

  private final ImageDownloadSourceType activeIconSourceType;
  private final String activeIconUrl;

  private final ImageDownloadSourceType inActiveIconSourceType;
  private final String inActiveIconUrl;

  private final String updateText;

  private final NHTabIconClickInterceptor clickInterceptor;

  private final IconDownloadCallback iconDownloadCallback;

  private final boolean isReset;// TODO(satosh.dhanyamraju): check other params when reset

  private NHTabIconUpdate(UserAppSection userAppSection,
                          ImageDownloadSourceType activeIconSourceType, String activeIconUrl,
                          ImageDownloadSourceType inActiveIconSourceType,
                          String inActiveIconUrl, String updateText,
                          NHTabIconClickInterceptor clickInterceptor, IconDownloadCallback
                              iconDownloadCallback, boolean isReset) {
    this.userAppSection = userAppSection;
    this.activeIconSourceType = activeIconSourceType;
    this.activeIconUrl = activeIconUrl;
    this.inActiveIconSourceType = inActiveIconSourceType;
    this.inActiveIconUrl = inActiveIconUrl;
    this.updateText = updateText;
    this.clickInterceptor = clickInterceptor;
    this.iconDownloadCallback = iconDownloadCallback;
    this.isReset = isReset;
  }

  public UserAppSection getUserAppSection() {
    return userAppSection;
  }

  public String getActiveIconUrl() {
    return activeIconUrl;
  }

  public NHTabIconClickInterceptor getClickInterceptor() {
    return clickInterceptor;
  }

  public String getInActiveIconUrl() {
    return inActiveIconUrl;
  }

  public String getUpdateText() {
    return updateText;
  }

  public boolean isReset() {
    return isReset;
  }

  public ImageDownloadSourceType getActiveIconSourceType() {
    return activeIconSourceType;
  }

  public ImageDownloadSourceType getInActiveIconSourceType() {
    return inActiveIconSourceType;
  }

  public IconDownloadCallback getIconDownloadCallback() {
    return iconDownloadCallback;
  }

  public static class Builder {
    private UserAppSection userAppSection;

    private ImageDownloadSourceType activeIconSourceType;
    private String activeIconUrl;

    private ImageDownloadSourceType inActiveIconSourceType;
    private String inActiveIconUrl;

    private String updateText;

    private NHTabIconClickInterceptor clickInterceptor;

    private IconDownloadCallback callback;

    private boolean isReset;

    public Builder(UserAppSection userAppSection) {
      this.userAppSection = userAppSection;
    }

    /**
     * Setter for activeIcon.
     * @param activeIconUrl - imageUrl, Note for {@link ImageDownloadSourceType}.RESOURCE, value
     *                      must be String.valueOf(android resource id)
     * @param sourceType - {@link ImageDownloadSourceType}
     * @return - Builder
     */
    public Builder activeIcon(String activeIconUrl, ImageDownloadSourceType sourceType) {
      this.activeIconUrl = activeIconUrl;
      activeIconSourceType = sourceType;
      return this;
    }

    /**
     * Setter for inActiveIcon.
     * @param inActiveIconUrl - imageUrl, Note for {@link ImageDownloadSourceType}.RESOURCE, value
     *                      must be String.valueOf(android resource id)
     * @param sourceType - {@link ImageDownloadSourceType}
     * @return - Builder
     */
    public Builder inActiveIcon(String inActiveIconUrl, ImageDownloadSourceType sourceType) {
      this.inActiveIconUrl = inActiveIconUrl;
      inActiveIconSourceType = sourceType;
      return this;
    }

    public Builder iconTitle(String title) {
      updateText = title;
      return this;
    }

    public Builder clickInterceptor(NHTabIconClickInterceptor clickInterceptor) {
      this.clickInterceptor = clickInterceptor;
      return this;
    }

    public Builder iconDownloadCallback(IconDownloadCallback iconDownloadCallback) {
      callback = iconDownloadCallback;
      return this;
    }

    public Builder isReset(boolean isReset) {
      this.isReset = isReset;
      return this;
    }

    public NHTabIconUpdate build() {
      return new NHTabIconUpdate(userAppSection, activeIconSourceType, activeIconUrl,
          inActiveIconSourceType, inActiveIconUrl, updateText, clickInterceptor, callback, isReset);
    }
  }

  public interface IconDownloadCallback {
    void onSuccess();

    void onFailure(String failedUrl, boolean isActiveIcon);
  }
}
