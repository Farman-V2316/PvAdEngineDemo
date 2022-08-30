package com.newshunt.socialfeatures.helper.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;

/**
 * @author santhosh.kc
 */
public enum NHSocialAnalyticsEvent implements NhAnalyticsEvent {
  CARD_WIDGET_VIEW,
  STORY_CARD_VIEW,
  STORY_LIST_VIEW,
  STORY_COMMENTED,
  COMMENT_LIKED,
  STORY_LIKED,
  COMMENT_DELETED,
  STORY_REPOST_CLICK;

  public boolean isPageViewEvent() {
    return false;
  }
}
