/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.entity;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;

/**
 * Regarding event on dialog action
 *
 * @author shashikiran.nr on 3/2/2016.
 */
public enum  NhAnalyticsDialogEvent implements NhAnalyticsEvent {

  IMAGE_ON_OFF_BOX_CLICKED(false),
  DIALOGBOX_VIEWED(false),
  DIALOGBOX_ACTION(false);


  private boolean isPageViewEvent;

  NhAnalyticsDialogEvent(boolean isPageViewEvent) {
    this.isPageViewEvent = isPageViewEvent;
  }

  public boolean isPageViewEvent() {
    return isPageViewEvent;
  }
}
