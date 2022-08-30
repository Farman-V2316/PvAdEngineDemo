package com.dailyhunt.tv.players.analytics.constants;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;

/**
 * Created by vinod on 23/04/18.
 */

public enum PlayerAnalyticsEventParams implements NhAnalyticsEventParam {

  //Video Playback measurement
  START_TIME("start_time"),
  END_TIME("end_time"),
  PLAYER_TYPE("player_type"),
  PLAYBACK_MODE("playback_mode"),
  END_ACTION("end_action"),
  VIDEO_LENGTH("video_length"),
  PLAYBACK_DURATION("playback_duration"),
  START_ACTION("start_action"),
  EVENT_NAME("event_name"),
  EVENT_SECTION("event_section"),
  SYSTEM_VIDEO_START_TIME("system_video_start_time"),
  QUALITY_SETTING("quality_setting"),
  QUALITY_PLAYED("quality_played"),
  MAX_QUALITY("max_quality"),
  VIDEO_CONNECTION("video_connection"),
  FULL_SCREEN_MODE("full_screen_mode"),

  CAMPAIGN_ID("campaign_id"),
  OPENX_ZONE_ID("openx_zone_id"),
  REFERRER_LEAD("referrer_lead"),
  REFERRER_LEAD_ID("referrer_lead_id"),
  SHARE_TYPE("share_type"),
  LIKE_EMOJI_TYPE("like_emoji_type"),
  SHARE_UI("share_ui"),
  SEARCH_ID("search_id"),

  TYPE("type"),
  TABITEM_ATTRIBUTION("tabitem_attribution"),
  SECTION_TIME("section_time"),
  START_STATE("start_state"),
  SECTION_ATTRIBUTION("section_attribution"),

  BANNER_ID("banner_id"),
  ITEM_NAME("item_name"),
  UI_TYPE("ui_type"),
  ITEM_CHANNEL_ID("item_channel_id"),
  ITEM_SOURCE_KEY("source_key"),
  TIME_OFFSET("time_offset"),
  PLAYLIST_NAME("playlist_name"),
  PLAYLIST_ID("playlist_id"),
  VIDEO_CONTENT_LENGTH("video_content_length"),
  NUMBER_OF_VIDEOS("no_of_videos"),
  PARENT_ID("parent_id"),
  ITEM_PROGRAM_CODE("item_program_code"),
  ITEM_PROGRAM_TIME("item_program_time"),
  ITEM_PROGRAM_GENRE("item_program_genre"),
  EPG_ID("epg_id"),
  GROUP_KEY("group_key"),
  ITEM_LABEL("item_label"),
  HAS_EPG_IMAGE("has_epg_image"),
  U_RATING("u_rating"),
  CONTAINER_TYPE("container_type"),
  IS_MUTED("is_muted"),
  IS_LIVE("is_live"),
  DATA_URL("data_url"),
  APP_SECTION("app_section"),
  IS_AD_PLAYING("is_ad_playing"),
  AD_VIEWED("ad_viewed"),
  IS_AP_CARRIED("is_ap_carried"),
  AP_DURATION("ap_duration"),
  ERROR_MESSAGE_CAUSE("error_message_cause"),
  IS_GIF("is_gif"),
  LOOP_COUNT("loop_count"),
  IS_PREFETCH("is_prefetch"),
  IS_CACHED("is_cached"),
  USER_CONNECTION_QUALITY_SELECTED("user_connection_quality_selected"),
  DISABLE_CACHE("disable_cache"),
  CACHED_VIDEO_URL("cached_video_url"),
  CACHED_DURATION("cached_duration"),
  CACHED_PERCENTAGE("cached_percentage"),;


  private String name;


  PlayerAnalyticsEventParams(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

}


