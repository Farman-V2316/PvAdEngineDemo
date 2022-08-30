/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper;

/**
 * @author shashikiran.nr on 6/13/2016.
 */
public enum RateUsDialogAction {
  FEEDBACK("feedback"),
  RATE_NOW("rate_now"),
  CROSS_DISMISS("cross_dismiss"),
  RATING_BAR("rating_bar");

  private String dialogAction;

  RateUsDialogAction(String action) {
    this.dialogAction = action;
  }

  public static RateUsDialogAction fromName(String action) {
    for (RateUsDialogAction rateUsDialogAction : RateUsDialogAction.values()) {
      if (rateUsDialogAction.dialogAction.equalsIgnoreCase(action)) {
        return rateUsDialogAction;
      }
    }
    return null;
  }

  public String getDialogAction() {
    return dialogAction;
  }

}
