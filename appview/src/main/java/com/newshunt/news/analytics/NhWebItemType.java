/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.analytics;

/**
 * @author shrikant.agrawal
 */
public enum NhWebItemType {

  WEBITEM_CATEGORY("webitem_category"),
  WEBITEM_TOPIC("webitem_topic"),
  WEB("web");

  String type;

  NhWebItemType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
