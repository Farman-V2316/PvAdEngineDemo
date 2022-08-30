package com.newshunt.socialfeatures.helper.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * @author santhosh.kc
 */
public enum NHAnalyticsSocialCommentsEventParam implements NhAnalyticsEventParam {

  COMMENTS("comments"),
  TABTYPE("tabtype"),
  TABITEM_ID("tabitem_id"),
  ITEM_ID("item_id"),
  COMMENT_ITEM_ID("comment_item_id"),
  PARENT_ITEM_ID("parent_item_id"),
  ITEM_TYPE("item_type"),
  LIKE_EMOJI_TYPE("like_emoji_type"),
  TYPE("type");

  private String name;

  NHAnalyticsSocialCommentsEventParam(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
