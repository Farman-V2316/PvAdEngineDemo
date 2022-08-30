/*
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * Created by anshul on 7/3/17.
 * An enum for the actions on the Astro Subscription Dialog.
 */

public enum AstroTriggerAction {

  SUBSCRIBE("subscribe"),
  CROSS_DISMISS("Dismiss");

  private final String triggerAction;

  AstroTriggerAction(String trigger_action) {
    this.triggerAction = trigger_action;
  }

  public static AstroTriggerAction fromName(String triggerAction) {
    for (AstroTriggerAction astroTriggerAction : AstroTriggerAction.values()) {
      if (CommonUtils.equalsIgnoreCase(astroTriggerAction.getTriggerAction(), triggerAction)) {
        return astroTriggerAction;
      }
    }
    return null;
  }

  public String getTriggerAction() {
    return triggerAction;
  }
}
