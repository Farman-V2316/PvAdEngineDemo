/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;

/**
 * Enum for all types of nhcommands
 *
 * @author maruti.borker
 */
public enum NHCommand {
  FEEDBACK("feedback"),
  LANGUAGE_EDITION("languageedition"),
  APP_LANGUAGE("applanguage"),
  RATE_US("rateus"),
  CHANGE_NEWSPAPER("changenewspaper"),
  OPEN_NEWSPAPER("opennewspaper"),
  OPEN_CATEGORY("opencategory"),
  OPEN_NEWS("opennews"),
  OPEN_TOPIC("opentopic"),
  OPEN_WEB_ITEM_RESOURCE("openwebitemfromresource"),
  SUBSCRIPTION_ACTIONS("subscriptionActions"),
  SELECTED_GENDER("selectedgender"),
  SHOW_WEB_ERROR("showError"),
  LOG_WEB_ANALYTICS("logWebAnalytics"),
  STORY_PHOTO_CLICK("showGallery");

  private String name;

  NHCommand(String name) {
    this.name = name;
  }

  public static NHCommand fromName(String name) {
    for (NHCommand nhCommand : NHCommand.values()) {
      if (nhCommand.name.equalsIgnoreCase(name)) {
        return nhCommand;
      }
    }
    return null;
  }

  public String getName() {
    return name;
  }

}
