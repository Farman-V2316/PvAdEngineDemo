/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.preference;

import com.newshunt.common.helper.preference.PreferenceType;
import com.newshunt.common.helper.preference.SavedPreference;

/**
 * Preferences related to app state.
 *
 * @author shreyas.desai
 */
public enum AppStatePreference implements SavedPreference {

  PRIMARY_LANGUAGE_NAME("prefPrimaryLanguageName"),
  EDITION_NAME("prefEditionName"),
  NEWS_BASE_URL("newshuntNewsBaseUrl"),
  INSTALL_REFERRER("nhInstallReferrer"),
  INSTALL_FIREBASE_REFERRER("nhInstallFirebaseReferrer"),
  INSTALL_APPSFLYER_REFERRER("nhInstallAppsFlyerReferrer"),
  APPLIED_THEME("appliedTheme"),
  APP_FIRST_LAUNCH("newshuntFirstLaunch"),
  EDITION_CONFIRMATION_COUNT("editionConfirmationCount"),
  HANDSHAKE_FIRST_CALL("handshakeFirstCall"),
  TERMS_CONDITIONS("termsConditions"),
  NEWS_HOME_FIRST_LAUNCH("newsHomeFirstLaunch"),
  OLD_NEWS_PAPERS("nhOldNewsPapers"),
  CURRENCY("currency"),
  UPGRADE_DIALOG_SHOWN("upgradeDialogShownDate"),
  INSTALL_SOURCE_EVENT_SENT("nhInstallEvent"),
  INSTALL_FIREBASE_EVENT_SENT("dh_firebase_InstallEvent"),
  INSTALL_APPSFLYER_EVENT_SENT("dh_appsflyer_InstallEvent"),
  INSTALL_GOOGLE_EVENT_SENT("dh_google_InstallEvent"),
  FIRST_PAGE_VIEW_EVENT("firstPageViewEvent"),
  FKEY("fKey"),
  OLD_PREFERENCE_SAVED("oldPreferenceSaved"),
  //Preference From server to control max number of notification to show in Tray
  MAX_NOTIFICATIONS_IN_TRAY("maxNotificationInTray"),
  // macro to be replaced when loading content in webview
  EMBEDDED_IMAGE_MACRO("embeddedImage"),
  // low-res image dimens for macro replacement
  EMBEDDED_IMAGE_SLOW("embeddedImageSlow"),
  // high-res image dimens for macro replacement
  EMBEDDED_IMAGE_FAST("embeddedImageFast"),
  //Preference From Server [Upgrade Response, saying whether it is DH -> DH Reinstall]
  IS_DH_2_DH_REINSTALL("dh_2_dh_reinstall"),
  //Preference for saving NH -> DH Upgrade State
  IS_NH_2_DH_UPGRADE("nh_2_dh_upgrade"),
  //Preference holding the App registration State.
  IS_APP_REGISTERED("dh_app_registrationState"),
  //Boolean State , Helps in Identifying , whether to send Edition Confirmation
  //(Reg / NH -> DH , First Handshake)
  TO_SEND_EDITION_CONFIRMATION("to_send_edition_confirmation"),
  LAST_KNOWN_APP_VERSION("lastKnownAppVersion"),
  NEWSPAGE_USER_VERSION("newspage_user_version"),
  NEWSPAGE_SERVER_VERSION("newspage_server_version"),
  DETECTED_LOCATION_CONFIRMATION("detected_location_confirmation"),
  IDEATE_FIRST_LAUNCH("ideate_first_launch"),
  MYBOOKS_FIRST_LAUNCH("mybooks_first_launch"),
  TOPIC_VERSION_UPDATE("topic_version_update"),
  ACTIVE_NOTIFICATIONS("action_notifications"),
  NEWSPAPER_GROUP_UPDATE("newspaper_group_update"),
  ENABLE_PERFORMANCE_ANALYTICS("enable_performance_analytics"),
  NEWTIP_REAPPEAR_TIME_MAP("newTipReappearTimeMap"),
  WEB_ITEM_SUPPORT_UPDATE("web_item_support_update"),
  APP_MODULE_VERSION_UPDATE("app_module_version_update"),
  TV_FIRST_LAUNCH("tv_first_launch"),
  LIVETV_FIRST_LAUNCH("live_tv_first_launch"),
  /**
   * time gap not to show older story in secs in story detail
   */
  OLDEST_STORY_DISPLAY_TIME_GAP("oldestStoryDisplayTimeGap"),
  /**
   * time gap not to show older story card in secs
   */
  OLDEST_LIST_DISPLAY_TIME_GAP("oldestListDisplayTimeGap"),
  /**
   * json map of disliked ids and their sync status
   */
  DISLIKED_STORY_IDS("disliked_story_ids"),
  DISLIKE_MIGRATION_0_1("dislike_mig_0_1"),
  DISLIKE_CONTENT_AVAILABLE("dislike_content_avl"),
  /**
   * Disable story dislike feature
   */
  PRELOAD_PAGES("preload_pages"),
  /**
   * Disable handling of 408 response
   */
  DISABLE_HANDLING_408_RESPONSE("disableHandling408Response"),
  /**
   * Model for the first page view
   */
  FIRST_PAGE_VIEW_SECTION("firstPageViewSection"),
  FIRST_PAGE_VIEW_PAGE("firstPageViewPage"),

  SIMILAR_STORIES_BASE_URL("similarStoriesBaseUrl"),

  COMSCORE_DELAY_IN_MILLS("comscore_delay_in_mills"),
  FIRE_TRACK_FROM_CACHE("fire_track_from_cache"),
  // AspectRatio for Main image in News detail page.
  NEWS_DETAIL_IMAGE_ASPECT_RATIO("news_detail_masthead_aspect_ratio"),
  FIRE_COMSCORE_TRACK_FROM_CACHE("fire_comscore_track_from_cache"),
  // Disable firebase app performance and monitoring
  DISABLE_FIREBASE_PERF("disable_firebase_perf"),
  SOFT_RELAUNCH_DELAY("soft_relaunch_delay"),
  HARD_RELAUNCH_DELAY("hard_relaunch_delay"),
  CLEARED_COOKIES("cleared_cookies"),
  PAGE_VIEW_STORE("page_view_store"),
  APP_START_COMPLETED("app_start_completed"),
  APP_DATA_VERSION("app_data_version"),
  SHARE_CONFIG_MAP("share_config_map"),
  FOLLOWED_VIRAL_TOPICS("followed_viral_topics"),
  IMAGE_DIMENSION_MULTIPLIER("image_download_dimension"),
  RESPECT_PRIVACY("respect_privacy"),
  PRIVACY_ACCEPTED("privacy_accepted"),
  PRIVACY_V2_ACCEPTED("privacy_accepted_v2"),
  LAST_PRIVACY_DIALOG_COUNT("last_privacy_dialog_count"),

  // App Launch Count for Permission
  LAST_PERMISSION_DIALOG_COUNT("last_permission_dialog_count"),

  // App Launch Count for Follow Coach
  LAST_FOLLOW_COACH_COUNT("last_follow_coach_count"),
  FOLLOW_EXPLORE_COACH_MARK("follow_explore_coach_mark"),

  NEWSHOME_TAB_LAST_ACCESS_TIME("newshome_tab_last_access_time"),
  LIVETV_EXIT_TIME("live_tv_exit_time"),
  BUZZ_EXIT_TIME("buzz_exit_time"),
  BOTTOM_BAR_FIXED("bottom_bar_fixed"),
  TV_SECTION_REFRESH_DATA("tv_section_refresh_data"),
  SOCIAL_LIKE_SYNCED("social_like_sync"),
  ACQUISITION_CAMPAIGN_PARAMS("campaign_recoparams"),
  ACQUISITION_CAMPAIGN_PARAMS_RECEIVED_TIMESTAMP("campaign_received_time"),
  ACQUISITION_CAMPAIGN_PARAMS_COUNT("campaign_recoparams_count"),
  VIDEO_ITEMS_RECENTLY_VIEWED("videos_recently_viewed"),
  LAST_ACCESS_VIDEO_THRESHOLD("last_access_video_threshold"),
  RECENT_VIDEOS_THRESHOLD_COUNT("recent_videos_threshold_count"),
  APP_CATEGORY_SUGGESTION("app_category_suggestions"),
  SEND_ACQ_PARAMS_HANDSHAKE("send_acq_params_handshake"),
  APP_FIRST_HANDSHAKE("app_first_handshake"),
  ACQUISITION_TYPE("acquisition_type"),
  APP_LAUNCH_RULES_PENDING("app_launch_rules_pending"),
  FB_DEFERRED_DEEPLINK("fb_deferred_deeplink"),
  PLAYERS_INFO("players_info"),
  FOLLOW_NEW_USER_NUDGE_COUNT("follow_new_user_nudge_count"),
  LAST_FOLLOW_NEW_USER_DATE("last_follow_news_user_date"),
  INSTALL_TIMESTAMP("install_timestamp"),
  LAST_NEWS_HOME("last_news_home"),
  FOLLOWED_SNACKBAR_DISPLAY_DURATION("followed_snackbar_display_duartion"),
  FOLLOW_SNACKBAR_INFO("follow_snackbar_info"),
  MAX_NOTI_FOR_SYSTEM_GROUPING("max_noti_in_tray_for_sys_grouping"),
  STICKY_AUDIO_CONTROL("sticky_audio_control"),
  EXEMPTED_NOTIFICATION_CHANNELS("exempted_notification_channels"),
  TEMPORARY_NOTIFICATION_CHANNELS("temporary_notification_channels"),
  TEMPORARY_NOTIFICATION_GROUPS("temporary_notification_groups"),
  PROFILE_TOOL_TIP_LAUNCH("profile_tool_tip_launch"),
  EDIT_PROFILE_TOOL_TIP("edit_profile_tool_tip"),
  EDIT_PROFILE_DATA("edit_profile_data"),
  PROFILE_NAME_CHAR_LIMT("profile_name_char_limt"),
  PROFILE_MAX_CARDS_GUEST("max_cards_view_for_guest"),
  PROFILE_DESC_CHAR_LIMIT("bio_max_chars"),
  GROUP_SETTINGS_TOOLTIP_SHOWN("group_settings_tooltip_shown"),
  SIGNIN_BEFORE_PROFILE_LAUNCH_COUNT("signin_before_profile_launchcount"),
  SIGNIN_SKIP_COUNTER("signin_skip_counter"),
  PROFILE_EXPLORE_COACHMARK("profile_explore_coachmark"),
  IS_TOPBAR_FIXED("is_topbar_fixed"),
  POST_CREATE_LOCATION_ENABLE("post_create_location_enable"),
  POST_CREATE_LOCATION_AUTOCOMPLETE_ENABLE("post_create_location_autocomplete_enable"),
  POST_CREATE_LOCATION_NEAR_BY_ENABLE("post_create_location_near_by_enable"),
  POST_CREATE_LOCATION_NEAR_BY_LIST("post_create_location_near_by_list"),
  POST_CREATE_LOCATION_NEAR_BY_CACHE_TIME("post_create_location_near_by_cache_time"),
  POST_CREATE_LOCATION_NEAR_BY_LAST_TIME_STAMP("post_create_location_near_by_last_time_stamp"),
  POST_CREATE_DEFAULT_LOCATION("post_create_default_location"),
  POST_CREATE_MAX_IMAGE_SIZE("post_create_max_image_size"),
  POST_CREATE_POLL_OPTION_LENGTH("post_create_poll_option_length"),
  POST_CREATE_FAIL_RETRY("post_create_fail_retry"),
  POST_CREATE_POLL_DURATION("post_create_poll_duration"),
  POST_CREATE_COMPRESS_IMAGE("post_create_compress_image"),
  POST_CREATE_COMPRESS_IMAGE_QUALITY("post_create_compress_image_quality"),
  POST_CREATE_NOTIFICATION_REMOVAL_DELAY("post_create_notification_removal_delay"),
  LAST_MARKER("last_marker"),
  FIRST_MARKER("first_marker"),
  REGISTER_OR_FIRST_HANDSHAKE_DONE("register_or_first_handshake_done"),
  FOLLOW_FILTERS("follow_filters"),
  SLOW_NETWORK_TIME("slow_network_time"),
  GOOD_NETWORK_TIME("good_network_time"),
  LAST_PARTNER_SERVICE_RUN_TIME("partner_service_run_time"),
  ID_OF_FORYOU_PAGE("id_of_foryou_page"),
  PAYLOAD_RECENT_PULLS_TIME_LIMIT("payload_recent_pulls_time_limit"),
  PAYLOAD_RECENT_DISLIKES_TIME_LIMIT("payload_recent_dislikes_time_limit"),
  PUBLIC_KEY_VERSION("public_key_version"),
  PUBLIC_KEY("encoded_public_key"),
  PUBLIC_ADS_KEY_VERSION("public_ads_key_version"),
  PUBLIC_ADS_KEY("encoded_public_ads_key"),
  HANDSHAKE_FINISH_TIME("handshake_finish_time"),
  HANDSHAKE_SCHEDULE_INTERVAL("handshake_schedule_interval"),
  SHARE_TOKEN("share_token"),
  SOCIAL_COACHMARK_DISPLAY_COUNT("social_coachmark_display_count"),
  IS_CREATE_POST_WALKTHROUGH_SHOWN("is_create_post_walkthrough_shown"),
  CONTACT_SYNC_ENABLED("cs_enabled"),
  CONTACT_SYNC_FREQUENCY_MS("cs_freq"),
  CONTACT_SYNC_BUCKET_SIZE("cs_bucket_size"),
  CONTACT_SYNC_LATEST_TIMESTAMP("cs_latest_timestamp"),
  FG_SESSION_TIMEOUT("fg_session_timeout"),
  TOTAL_FOREGROUND_SESSION("total_foreground_session"),
  TOTAL_FOREGROUND_DURATION("total_foreground_duration"),
  FTD_SESSION_COUNT("ftd_session_count"),
  FTD_SESSION_TIME("ftd_session_time"),
  FTD_LAST_SAVE_DATE("ftd_last_save_date"),
  DEVICE_UNIQUE_KEY("device_unique_key"),
  CONTACT_LITE_SYNC_DONE("contact_lite_sync_done"),
  LEGACY_ARTICLES_SYNC_DONE("legacy_articles_sync_done"),
  BOOKMARK_SYNC_LAST_DONE("bookmark_sync_last_done"),
  FOLLOW_NUDGE_LAST_SHOWN("follow_nudge_last_shown"),
  FG_SESSION_ID("fg_session_id"),
  NEED_CS_FULL_SYNC("need_cs_full_sync_v2"),
  RECENT_SECTION_LAUNCH_LIST("recent_section_launch_list"),
  NOTIFICATION_CTA("notification_cta"),
  ENABLE_SMALL_CARD("is_small_card"),
  NUDGE_SHOWN_IN_CURRENT_LAUNCH("nudge_shown_in_current_launch"),
  MIN_VIEW_VISIBILITY_FOR_SCV("min_view_visibility_for_scv"),
  MIN_VIEW_VISIBILITY_FOR_TS("min_view_visibility_for_ts"),
  MIN_SCREEN_VISIBILITY_FOR_SCV("min_screen_visibility_for_scv"),
  MIN_SCREEN_VISIBILITY_FOR_TS("min_screen_visibility_for_ts"),
  LOCATION_FETCH_INTERVAL("location_fetch_interval"),
  APP_SWIPE_UP_COACH_MARK_STATE("app_swipe_up_coach_mark_state"),
  AUTO_IMMERSIVE_TIME_SPAN ("auto_immersive_time_span"),
  AUTO_IMMERSIVE_ENABLED ("auto_immersive_enabled"),
  IS_LOCAL_ZONE_FIRST_LAUNCH ("is_local_zone_first_launch"),
  DEFAULT_CHANNEL_GROUPED_TRAY_NOTIFICATION("default_channel_grouped_tray_notification"),
  LANG_SCREEN_TYPE("lang_screen_type"),
  LANG_SCREEN_WAIT_SEC("lang_screen_wait_sec"),
  REGISTER_FAILED_ATLEAST_ONCE("register_failed_atleast_once"),
  REGISTER_MIGRATION_RESPONSE("register_migration_response"),
  REGISTER_MIGRATION_NEEDED("register_migration_needed"),
  SEND_INSTALL_TYPE_AS_UPGRADE("send_install_type_as_upgrade"),
  USER_CHANGED_SMALL_CARD_SETTING("user_changed_small_card_setting"), /*this preference will
  set on upgrade to 18x and on user prefs change, to prevent overwriting user preference from upgrade/config response */
  PREF_FEED_CACHE_DELAY("PREF_FEED_CACHE_DELAY"),/*this will be used to control CacheType.DELAYED_CACHE_AND_NETWORK */
  MAXIMUM_STALL_TIME_BEFORE_POST("maximum_stall_time_before_post"),
  NOTIFICATION_PREFETCH_SCHEDULE_AFTER_TIME("notification_prefetch_schedule_after_time"),
  MAXIMUM_ALLOWED_RETRIES_FOR_NOTIFICATION_PREFETCH("maximum_allowed_retries_for_notification_prefetch"),
  NOTIFICATION_PREFETCH_ENABLED("notification_prefetch_enabled"),
  NOTIFICATION_FONT_SIZE("notification_font_size"),
  IS_NOTIFICATION_UNGROUPING_ENABLED("notification_ungrouping_enabled"),
  UNGROUPED_FLOW_MAX_NOTIFICATIONS_IN_TRAY("ungroupedFlowMaxNotificationsInTray"),
  PARTNER_APP_WAKE_UP_INFORMATION("partner_app_wake_up_information"),
  WOKEN_UP_BY_PARTNER_SERVICE_FG_DURATION("woken_up_by_partner_service_fg_duration"),
  SHOW_IN_APP_RATING_FLOW("show_in_app_rating_flow"),
  ICONS_CONFIG_FEED_CARD("icons_config_feed_card"),
  MAXIMUM_NUMBER_OF_ITEMS_TO_BE_SHOWN_IN_NEWS_STICKY("news_sticky_max_items_count"),
  NEWS_STICKY_DND_TIME("news_sticky_dnd_time"),
  NOTIFICATION_SETTINGS_SELECTED_DND_DELAY_DAYS("news_stick_selected_dnd_delay_days"),
  NEWS_STICKY_QUERY_PARAMS("news_sticky_path_params"),
  NEWS_STICKY_AUTO_REFRESH_INTERVAL("news_sticky_auto_refresh_time"),
  CONFIG_SHOW_DISABLE_NEWS_STICKY_FOREVER("config_show_disable_news_sticky_forever"),
  THEME_APPLY_SYSTEM("theme_apply_system"),
  THEME_APPLIED_NEW("theme_applied_new"),
  PREV_THEME_APPLIED("prev_theme_applied"),
  THEME_SWITCHED_SNACKBAR_LIST_NEEDED("theme_switched_snackbar_list_needed"),
  THEME_SWITCHED_SNACKBAR_DETAIL_NEEDED("theme_switched_snackbar_detail_needed"),
  THEME_SWITCH_TOAST_LIST_NEEDED("theme_switch_toast_list_needed"),
  THEME_SWITCH_TOAST_DETAIL_NEEDED("theme_switch_toast_detail_needed"),
  LAST_KNOWN_DISABLE_LOGGING_STATUS_NEWS_STICKY("disable_logging_news_sticky"),
  NEWS_STICKY_AUTO_SCROLL_TIME("news_sticky_auto_scroll_time"),
  NOTIFICATION_CLEAR_ALL_TIME_THRESHOLD("notification_clear_all_time_threshold"),
  NOTIFICATION_CLEAR_ALL_COUNT_THRESHOLD("notification_clear_all_count_threshold"),
  NOTIFICATION_SETTINGS_SELECTED_TRAY_OPTION("notification_settings_selected_option"),
  NOTIFICATION_SETTINGS_UNGROUPING_MAX_NOTIFICATION_COUNT("notification_settings_max_ungrouping_count"),
  NOTIFICATION_SETTINGS_AUTO_TRAY_DELETE_DURATION("notification_auto_tray_delete_duration"),
  CRICKET_STICKY_ENABLED_STATE("cricket_sticky_enabled_state"),
  ELECTION_STICKY_ENABLED_STATE("election_sticky_enabled_state"),
  SELECTED_APP_TO_SHARE("selected_app_to_share"),
  PRIVACY_DIALOG_SHOWN_LAUNCH_COUNT("privacy_dialog_shown_launch_count"),
  NEWS_STICKY_ENABLED_STATE("news_sticky_enabled_state"),
  NOTIFICATION_TRAY_MANAGEMENT_SECTION_WAS_EVER_EXPANDED("tray_management_section_expanded"),
  DEFAULT_NEWS_STICKY_DISABLED_DAYS("default_news_sticky_disabled_days"),
  SERVER_LOCATION("server_location"),
  INITIAL_VIDEO_THUMBNAIL_DELAY("initial_video_thumbnail_delay"),
  VIDEO_THUMBNAIL_DELAY("video_thumbnail_delay"),
  EXO_PLAYER_LOAD_DELAY("exo_player_load_delay"),
  OTHER_PLAYER_LOAD_DELAY("other_player_load_delay"),
  USER_REGISTRATION_INFO("user_registration_info"),
  UNIQUE_NOTIFICATION_GROUP_DISABLED("unique_notification_group_disabled"),
  IN_APP_DISPLAY_DURATION("in_app_display_duration"),
  LINKEDIN_SHARE_MAX_TIMES_SHOW("linkedin_share_max_times_show"),
  LINKEDIN_SHARE_SHOWN_COUNT("linkedin_share_shown_count"),
  LINKEDIN_MIN_WAIT_DAYS_NEW_USERS("linkedin_min_wait_days_new_users"),
  LINKEDIN_MIN_WAIT_DAYS_UPGRADED_USERS("linkedin_min_wait_days_upgraded_users"),
  LINKEDIN_MIN_LAUNCHES_UPGRADED_USERS("linkedin_min_launches_upgarded_users"),
  LINKEDIN_MIN_DAYS_AFTER_LAST_SEEN("linkedin_min_days_after_last_scene"),
  LINKEDIN_FIRST_LAUNCH_OR_LATEST_UPGRADE_TIME("linkedin_first_launch_or_latest_upggrade_time"),
  LINKEDIN_SHOWN_TIME("linkedin_shown_time"),
  LINKEDIN_SHARE_ENABLED("linkedin_share_enabled"),
  LINKEDIN_SHARE_PKG_NAME("linkedin_share_pck_name"),
  LINKEDIN_SHARE_DIALOG_TITLE("linkedin_share_dialog_title"),
  NEVER_SHOW_DEFAULT_SHARE_APP_PROMPT_PKG("never_show_default_share_app_prompt_pkg"),
  HOME_LOADER_SHOWN("home_loader_shown"),
  HOME_LOADER_MIN_WAIT_MS("home_loader_min_wait_ms"),
  HOME_LOADER_MAX_WAIT_MS("home_loader_max_wait_ms"),
  WORK_MANAGER_INITIAL_INIT_DELAY("wm_initial_init_delay"),
  IS_NEW_INSTALL("is_new_install"),
  BW_EST_CONFIG("bw_est_config"),
  MIN_COLLECTION_FOR_REQUEST("min_collection_for_request"),
  COLLECTION_SECOND_ITEM_VISIBLE_PERCENTAGE("collection_second_item_visible_percentage"),
  SHOW_SOURCE_LOGO_AT_CARD_LEVEL("show_source_logo_at_card_level"),
  IS_TABS_SWIPE_ENABLED("is_tabs_swipe_enabled");
  private String name;

  AppStatePreference(String name) {
    this.name = name;
  }

  @Override
  public PreferenceType getPreferenceType() {
    return PreferenceType.APP_STATE;
  }

  @Override
  public String getName() {
    return name;
  }
}
