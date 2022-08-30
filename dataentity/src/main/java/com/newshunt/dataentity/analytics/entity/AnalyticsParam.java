/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.analytics.entity;

/**
 * @author: bedprakash on 06/12/17.
 */

public enum AnalyticsParam implements NhAnalyticsEventParam {
  UI_TYPE("ui_type"),
  ITEM_TYPE("item_type"),
  ASSET_TYPE("asset_type"),
  ITEM_NAME("item_name"),
  GROUP_TYPE("group_type"), //Special Cards
  CONTENT_TYPE("content_type"),
  CARD_TYPE("card_type"),
  CARD_LABEL("card_label"),
  GROUP_ID("group_id"),
  IMAGECOUNT("imagecount"),
  NOTIFICATION_ID("notification_id"),
  NOTIFICATION_TIME("notification_time"),
  REFERRER_LEAD("referrer_lead"),
  REFERRER_LEAD_ID("referrer_lead_id"),
  ITEM_LANGUAGE("item_language"),
  IS_FROM_CACHE("is_cached"),
  TIME_TAKEN_FOR_NETWORK_OPERATION("time_taken_for_network_operation"),
  NETWORK_SERVICE_PROVIDER("network_service_provider"),
  COLLECTION_ID("collection_id"),
  COLLECTION_TYPE("collection_type"),
  COLLECTION_ITEM_TYPE("collection_item_type"),
  COLLECTION_ITEM_COUNT("collection_item_count"),
  ITEM_CATEGORY_ID("item_category_id"),
  ITEM_PUBLISHER_ID("item_publisher_id"),
  ITEM_ID("item_id"),
  CARD_POSITION("card_position"),
  TIMESPENT("timespent"),
  TIMESPENT_ARRAY("timespent_array"),
  REFERRER_RAW("referrer_raw"),
  IS_SCV("is_scv"),
  IS_SPV("is_spv"),
  IS_SHARED("is_shared"),
  IS_COMMENTED("is_commented"),
  IS_LIKED("is_liked"),
  IS_UNLIKED("is_unliked"),
  IS_CLICKED("is_clicked"),
  MESSAGE("message"),
  WIDGET_LANGUAGE("widget_language"),
  ITEM_COUNT("item_count"),
  ENTITY_NAME("entity_name"),
  ENTITY_ID("entity_id"),
  ENTITY_TYPE("entity_type"),
  ENTITY_SUBTYPE("entity_subType"),
  ENTITY_POSITION("entity_position"),
  COLLECTION_NAME("collection_name"),
  PARENT_ITEM_ID("parent_item_id"),
  PARENT_ITEM_TS("parent_item_ts"),
  EXTRA_DATA_CLIENT("extra_data_client"),
  CAROUSEL_ID("carousel_id"),
  ACTIVITY_TYPE("activity_type"),
  FORMAT("format"),
  SUB_FORMAT("subFormat"),
  TYPE("type"),
  ITEM_TAG_IDS("item_tag_ids"),
  PLAYER_KEY("player_key"),
  IN_DETAIL("in_detail"),
  IS_GIF("is_gif"),
  LOOP_COUNT("loop_count"),
  AUTO_TRANSITION("auto_transition"),
  TOTAL_LAG_IN_MILLI("total_lag_in_milli"),
  INITIAL_LOAD_TIME("initial_load_time"),
  BUFFER_TIME_MS("buffer_time_ms"),
  ENTITY_SOURCE_TYPE("entity_sourceType"),
  REFERRER_ITEM_ID("referrer_item_id"),
  REFERRER_ENTITY_SOURCE_TYPE("referrer_entity_sourceType");

  private final String name;

  AnalyticsParam(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
