/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.status;

import com.google.gson.annotations.SerializedName;
import com.newshunt.dataentity.dhutil.model.entity.upgrade.LangUpdateInfo;

import java.io.Serializable;

/**
 * Provides client specific details.
 *
 * @author shreyas
 */
public class ClientInfo extends ClientBaseInfo implements Serializable {

  private static final long serialVersionUID = 2383713983408573649L;

  private String device;
  private int width;
  private int height;
  private String appVersion;
  private String brand;
  private String primaryLanguage;
  private String secondaryLanguages;
  private String osVersion;
  private String currency;
  private String gaid;
  private boolean gaidOptOutStatus;
  private String defaultNotificationLang;
  private String model;
  private String manufacturer;
  private String edition;
  private String appLanguage;
  @SerializedName("langUpdate")
  private LangUpdateInfo langUpdateInfo;

  public String getGaid() {
    return gaid;
  }

  public void setGaid(final String gaid) {
    this.gaid = gaid;
  }

  public String getDevice() {
    return device;
  }

  public void setDevice(final String device) {
    this.device = device;
  }

  public DeviceType getDeviceType() {
    return DeviceType.fromString(device);
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(final int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(final int height) {
    this.height = height;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(final String appVersion) {
    this.appVersion = appVersion;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(final String brand) {
    this.brand = brand;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public void setOsVersion(final String osVersion) {
    this.osVersion = osVersion;
  }

  public String getPrimaryLanguage() {
    return primaryLanguage;
  }

  public void setPrimaryLanguage(final String primaryLanguage) {
    this.primaryLanguage = primaryLanguage;
  }

  public String getSecondaryLanguages() {
    return secondaryLanguages;
  }

  public void setSecondaryLanguages(final String secondaryLanguages) {
    this.secondaryLanguages = secondaryLanguages;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(final String currency) {
    this.currency = currency;
  }

  public boolean getGaidOptOutStatus() {
    return gaidOptOutStatus;
  }

  public void setGaidOptOutStatus(final boolean gaidOptOutStatus) {
    this.gaidOptOutStatus = gaidOptOutStatus;
  }

  public String getDefaultNotificationLang() {
    return defaultNotificationLang;
  }

  public void setDefaultNotificationLang(final String defaultNotificationLang) {
    this.defaultNotificationLang = defaultNotificationLang;
  }

  public String getModel() {
    return model;
  }

  public void setModel(final String model) {
    this.model = model;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(final String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(final String edition) {
    this.edition = edition;
  }

  public String getAppLanguage() {
    return appLanguage;
  }

  public void setAppLanguage(final String appLanguage) {
    this.appLanguage = appLanguage;
  }

  public void copy(final ClientInfo clientInfo) {
    if (clientInfo == null) {
      return;
    }
    super.copy(clientInfo);
    device = clientInfo.device;
    width = clientInfo.width;
    height = clientInfo.height;
    appVersion = clientInfo.appVersion;
    brand = clientInfo.brand;
    primaryLanguage = clientInfo.primaryLanguage;
    secondaryLanguages = clientInfo.secondaryLanguages;
    osVersion = clientInfo.osVersion;
    currency = clientInfo.currency;
    gaid = clientInfo.gaid;
    gaidOptOutStatus = clientInfo.gaidOptOutStatus;
    defaultNotificationLang = clientInfo.defaultNotificationLang;
    model = clientInfo.model;
    manufacturer = clientInfo.manufacturer;
    edition = clientInfo.edition;
    appLanguage = clientInfo.appLanguage;
  }

  public LangUpdateInfo getLangUpdateInfo() {
    return langUpdateInfo;
  }

  public void setLangUpdateInfo(LangUpdateInfo langUpdateInfo) {
    this.langUpdateInfo = langUpdateInfo;
  }
}
