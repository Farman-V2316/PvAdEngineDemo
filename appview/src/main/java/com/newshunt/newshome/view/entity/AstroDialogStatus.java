/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.newshome.view.entity;

/**
 * This is used to represent the current status of the Astro Subscription.
 * Created by anshul on 20/2/17.
 */

public enum AstroDialogStatus {

  NEVER_SHOWN("never_shown"),
  DISMISSED_ONCE("dismissed_once"),
  DISMISSED_TWICE("dismissed_twice"),
  SUBSCRIPTION_SUCCESSFUL("subscription_successful");

  private String status;

  AstroDialogStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public static AstroDialogStatus getAstroDialogStatus(String eventType) {
    for (AstroDialogStatus astroDialogStatus : AstroDialogStatus.values()) {
      if (astroDialogStatus.getStatus().equalsIgnoreCase(eventType)) {
        return astroDialogStatus;
      }
    }
    return null;
  }
}
