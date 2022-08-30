package com.dailyhunt.tv.players.analytics.enums;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;

/**
 * Created by vinod on 23/04/18.
 */

public enum AnalyticsEvent implements NhAnalyticsEvent {

  VIDEO_PLAYED(false),
  AD_REQUEST(false),
  VIDEO_PLAY_ERROR(false);

  private boolean isPageViewEvent;

  AnalyticsEvent(boolean isPageViewEvent) {
    this.isPageViewEvent = isPageViewEvent;
  }

  @Override
  public boolean isPageViewEvent() {
    return isPageViewEvent;
  }

  public void setIsPageViewEvent(boolean isPageViewEvent) {
    this.isPageViewEvent = isPageViewEvent;
  }
}
