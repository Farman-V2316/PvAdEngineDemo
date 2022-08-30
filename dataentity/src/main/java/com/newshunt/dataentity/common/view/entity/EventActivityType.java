/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.view.entity;

/**
 * Different types of events based in the communication events API.
 * Created by anshul on 17/2/17.
 */

public enum EventActivityType {

  INVALID("invalid"),
  ASTRO("astro"),
  PERMISSION("permission"),
  BATTERY_OPTIMIZATION_DIALOG("battery_optimization_dialog"),
  SOCIAL_COACHMARK("social_coachmark"),
  WALKTHROUGH("walkthrough"),
  IMPORT_CONTACTS("import_contacts"),
  PRIVACY_V2("privacy_v2"),
  LINKEDIN_SHARE("linkedin_share");

  EventActivityType(String type) {
    this.type = type;
  }

  private String type;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public static EventActivityType getEventActivityType(String eventType) {
    for (EventActivityType eventActivityType : EventActivityType.values()) {
      if (eventActivityType.getType().equalsIgnoreCase(eventType)) {
        return eventActivityType;
      }
    }
    return INVALID;
  }
}
