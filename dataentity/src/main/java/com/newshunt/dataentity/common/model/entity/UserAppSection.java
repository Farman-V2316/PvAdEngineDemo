/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;

import java.io.Serializable;

/**
 * @author santhosh.kc
 */
public class UserAppSection implements Serializable {

  private static final long serialVersionUID = -3579837327834580182L;

  private AppSection type;
  private String id;
  private String appSectionEntityKey;
  private String contentUrl;

  public UserAppSection() {

  }

  private UserAppSection(AppSection type, String id, String appSectionEntityKey, String
      contentUrl) {
    this.type = type;
    this.id = id;
    this.appSectionEntityKey = appSectionEntityKey;
    this.contentUrl = contentUrl;
  }

  public AppSection getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getAppSectionEntityKey() {
    return appSectionEntityKey;
  }

  public String getContentUrl() {
    return contentUrl;
  }

  public static class Builder {
    private AppSection type;
    private String id;
    private String appSectionEntityKey;
    private String contentUrl;

    public Builder section(AppSection type) {
      this.type = type;
      return this;
    }

    public Builder sectionId(String sectionId) {
      id = sectionId;
      return this;
    }

    public Builder entityKey(String appSectionEntityKey) {
      this.appSectionEntityKey = appSectionEntityKey;
      return this;
    }

    public Builder sectionContentUrl(String contentUrl) {
      this.contentUrl = contentUrl;
      return this;
    }

    public Builder from(UserAppSection buildFrom) {
      type = buildFrom.type;
      id = buildFrom.id;
      appSectionEntityKey = buildFrom.appSectionEntityKey;
      contentUrl = buildFrom.contentUrl;
      return this;
    }

    public UserAppSection build() {
      return new UserAppSection(type, id, appSectionEntityKey, contentUrl);
    }

  }
}
