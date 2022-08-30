/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.analytics;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Event parameters applicable to News section.
 *
 * @author shreyas.desai
 */
public enum NhAnalyticsNewsEventParam implements NhAnalyticsEventParam {
  ITEM_SUB_ID("item_sub_id"),
  PUBLISHER_ID("publisher_id"),
  CATEGORY_ID("category_id"),
  TOPIC_ID("topic_id"),
  RELATED_ITEM_IDS("related_item_ids"),
  RELATED_ITEM_ID("related_item_id"),
  SHARE_TYPE("share_type"),
  SHARE_UI("share_ui"),
  SAVELIST_DELETEARTICLE_COUNT("savelist_deletearticle_count"),
  CACHE_SIZE_BYTE("cache_size_byte"),
  CACHE_TIME_MILLI("cache_time_milli"),
  FEED_FILTERS("feed_filters"),
  WIDGET_ENABLE("widget_enable"),
  WIDGET_DISABLE("widget_disable"),
  WIDGET_REFRESH("widget_refresh"),

  //Video Playback measurement
  START_TIME("start_time"),
  END_TIME("end_time"),
  PLAYER_TYPE("player_type"),
  PLAYBACK_MODE("playback_mode"),
  END_ACTION("end_action"),
  VIDEO_LENGTH("video_length"),
  PLAYBACK_DURATION("playback_duration"),
  REFERRER_ACTION("referrer_action"),
  TABTYPE("tabtype"),
  TABNAME("tabname"),
  TABITEM_ID("tabitem_id"),
  TABINDEX("tabindex"),
  TABITEM_ATTRIBUTION("tabitem_attribution"),
  FEED_LANGUAGE("feed_language"),
  PAGE_NUMBER("page_number"),
  BUTTON_TYPE("type"),
  BUTTON_ID("button_id"),
  LOCATION_ID("location_id"),
  RESPONSE_CODE("response_code"),
  VIEW_TYPE("view_type"),
  HTTP_ERROR_CODE("http_error_code"),
  HTTP_ERROR_MESSAGE("http_error_message"),
  PAGE_TYPE("page_type"),
  PV_ACTIVITY("pv_activity"),
  WORDCOUNT("wordcount"),
  STORY_SIZE("story_size"),
  ASSET_TYPE("asset_type"),
  IS_SHARED("is_shared"),
  WIDGET_TYPE("widget_type"),
  WIDGET_PLACEMENT("widget_placement"),
  WIDGET_DISPLAY_TYPE("widget_display_type"),
  LIST_ITEM_COUNT("list_item_count"),

  ACTION("action"),
  FETCH_TYPE("fetch_type"),
  CARD_COUNT("card_count"),
  LATEST_PAGENUMBER("latest_pagenumber"),
  PAGE_LAYOUT("page_layout"),
  USER_GENDER("user_gender"),
  USER_DOB("user_dob"),
  SUBSCRIPTION_TYPE("type"),

  //News Home
  TABS_ORDER("tabs_order"),
  REORDER_ATTRIBUTION("reorder_attribution"),
  OLD_HOME("old_home"),
  NEW_HOME("new_home"),

  IS_LITE("is_lite"),
  EXIT_ACTION("exit_action"),
  CHUNKWISE_TS("chunkwise_ts"),
  ENGAGEMENT_PARAMS("engagement_params"),
  TYPE("type"),
  FOLLOW_TYPE("follow_type"),
  LANDING_TYPE("landing_type"),

  ANIMATION_TYPE("animation_type"),
  FILTER_TYPE("filter_type"),
  ERROR_CODE("error_code"),
  FOLLOWING_COUNT("following_count"),
  CLICK_TYPE("click_type");
  private String name;

  NhAnalyticsNewsEventParam(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
