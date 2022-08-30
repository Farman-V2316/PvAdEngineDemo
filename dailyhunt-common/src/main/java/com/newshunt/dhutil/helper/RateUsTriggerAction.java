/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper;

/**
 * @author shashikiran.nr on 6/13/2016.
 */
public enum RateUsTriggerAction {
  BACK("back"),
  SWIPE("swipe"),
  SHARE("share"),
  CLICK("click");

  private String triggerAction;

  RateUsTriggerAction(String trigger_action) {
    this.triggerAction = trigger_action;
  }

  public static RateUsTriggerAction fromName(String trigger_action) {
    for (RateUsTriggerAction rateUsTriggerAction : RateUsTriggerAction.values()) {
      if (rateUsTriggerAction.triggerAction.equalsIgnoreCase(trigger_action)) {
        return rateUsTriggerAction;
      }
    }
    return null;
  }

  public String getTriggerAction() {
    return triggerAction;
  }
}
