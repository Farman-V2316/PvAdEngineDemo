/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.util;


import com.newshunt.common.view.view.UniqueIdHelper;

public class NewsConstants {

  public static final String NEWSPAPER_CATEGORY_KEY = "CategoryKey";
  public static final String STORY_EXTRA = "Story";
  public static final String STORIES_EXTRA = "Story";
  public static final String PARENT_STORY_ID = "ParentStoriesId";
  public static final String PARENT_ENTITY_ID = "ParentEntityId";
  public static final String SOURCE_ID = "sourceId";
  public static final String SOURCE_TYPE = "sourceType";
  public static final String CHILD_FRAGMENT = "child_fragment";
  public static final String IS_IN_COLLECTION = "is_in_collection";
  public static final String IS_IN_CAROUSEL = "is_in_carousel";
  public static final String CARD_POSITION = "card_position";
  public static final String COLLECTION_ITEM_COUNT = "collection_item_count";
  public static final String POST_ENTITY_LEVEL = "post_entity_level";
  public static final String ITEM_TYPE_ITEM = "ITEM";
  public static final String TIMESPENT_EVENT_ID = "TIMESPENT_EVENT_ID";

  public static final String KEY_REFERRER_FLOW = "referrerFlow";
  public static final String KEY_REFERRER_FLOW_ID = "referrerFlowId";
  public static final String KEY_SEND_BOTH_CHUNK = "sendBothChunk";
  public static final String KEY_USE_WIDGET_POSITION = "useWidgetPosition";

  public static final String HTML_MIME_TYPE = "text/html";
  public static final String HTML_UTF_ENCODING = "UTF-8";

  // index of similar stories item the card
  public static final String TOPIC_KEY = "topicKey";
  public static final String SUB_TOPIC_KEY = "subTopicKey";
  public static final String LOCATION_KEY = "locationKey";
  public static final String SUB_LOCATION_KEY = "subLocationKey";
  public static final String LOCAL_SELECTED_LOCATION_KEY = "localSelectedLocationKey";

  public static final String BUNDLE_WEB_RESOURCE_ID = "bundleWebResourceId";
  public static final String BUNDLE_OPEN_FOLLOWED_ENTITY = "open_followed_entity";
  public static final String BUNDLE_SLIDING_TAB_ID = "sliding_tab_id";
  public static final String BUNDLE_ENABLE_MAX_DURATION_TO_NOT_FETCH_FP = "bundle_enable_max_duration_to_not_fetch_fp";

  public static final int DEFAULT_PROGRESS_COUNT = 1;
  public static final int DEFAULT_FONT_SIZE = 16;
  public static final int DEFAULT_TITLE_SIZE = 19;

  public static final String URDU_LANGUAGE_CODE = "ur";
  public static final String ENGLISH_LANGUAGE_CODE = "en";

  /**
   * TODO(Arun.babu): Get a valid api key before launch
   * Please replace this with a valid API key which is enabled for the
   * YouTube Data API v3 service. Go to the
   * <a href="https://console.developers.google.com/">Google Developers Console</a>
   * to register a new developer key.
   */
  public static final String YOUTUBE_DEVELOPER_KEY = "AIzaSyBsWYECGI9FQ5xU-FmwDlwyckxe71Y6MEQ";
  public static final int MRAID_AUTO_EXPAND_TIME = 1000;
  public static final String MRAID_PLACEMENT_PAGE = "page";

  public static final String BUNDLE_ACTIVITY_REFERRER = "activityReferrer";
  public static final String RESOURCE_ID_PLACEHOLDER = "${id}";

  public static final String SEARCH = "search";

  //describes what type of page a new list will consist of
  public static final String BUNDLE_NEWSPAGE = "NewsPageBundle";
  public static final String BUNDLE_FORCE_NIGHT_MODE = "force_night";
  public static final long TOOL_TIP_DELAY = 3000L;
  public static final String EXTRA_PAGE_ADDED = "page_added";
  public static final String EXTRA_SHOW_IMPORT_CONTACTS = "extra_show_import_contacts";

  public static final String BUNDLE_NEXT_PAGE_LOGIC = "next_page_logic";
  public static final String BUNDLE_NEXT_PAGE_LOGIC_ID = "next_page_logic_id";

  public static final String INTENT_NEWS_HOME_TAB = "IntentNewsHomeTab";
  public static final String INTENT_ACTION_SEE_ALL_ENTITY = "seeAllEntity";
  public static final String INTENT_ACTION_LAUNCH_NEWS_HOME_ROUTER = "NewsHomeRouterOpen";
  public static final String INTENT_ACTION_LAUNCH_ADD_PAGE = "OpenAddPageActivity";

  //For passing flag to AddPageActivity
  public static final String ADD_PAGE_ACTIVITY_OPEN_PAGE = "add_page_activity_open_page";
  public static final String QUERY_PARAMETER_APP_LANGUAGE = "appLanguage";
  public static final String QUERY_PARAMETER_LANGUAGE_CODE = "langCode";
  public static final String QUERY_PARAMETER_EDITION = "edition";

  public static final String BUNDLE_ADAPTER_POSITION = "adapter_position";
  public static final String BUNDLE_ENTITY_ID = "entity_id";
  public static final String BUNDLE_IS_NEWS_HOME = "is_news_home";
  public static final String TABITEM_ATTRIBUTION = "user";
  public static final String NEWS_PAGE_ENTITY = "news_page_entity";
  public static final String TAB_ENTITY = "tab_entity";
  public static final String LANDING_ADAPTER_POSITION = "landing_adapter_position";

  public static final String LANGUAGE_CODE = "langCode";
  public static final String EDITION_CODE = "edition";

  public static final String SHOW_SELECT_TOPIC_BUTTON = "showSelectTopicButton";
  public static final String SHOW_SELECT_LOCATION_BUTTON = "showSelectLocationButton";
  public static final String IS_ADJUNCT_LANG_NEWS = "isAdjunctLangNews";
  public static final String ADJUNCT_POPUP_DISPLAY_TYPE = "adjunctPopupDisplayType";
  public static final String ADJUNCT_LANGUAGE = "adjunctLanguage";

  public static final String NEWS = "News";

  public static final String COMM_TYPE_COACHMARK = "coachmark";
  public static final String COMM_EVENT_LAUNCH = "launch";
  public static final String COMM_EVENT_APP_LAUNCH = "appLaunch";
  public static final String COMM_EVENT_UPGRADE = "upgrade";
  public static final String COMM_MIN_OCCURENCES = "minNumberOfOccurences";
  public static final String COMM_MIN_OCCURENCES_GO = "minNumberOfOccurencesGo";
  public static final String COMM_MIN_APP_LAUNCH_COUNT = "minNumberOfAppLaunch";
  public static final String COMM_MIN_APP_LAUNCH_COUNT_GO = "minNumberOfAppLaunchGo";
  public static final String COMM_MESSAGE = "message";
  public static final String COMM_TITLE = "title";
  public static final String COMM_DISPLAY_TIME = "displayTime";
  public static final String COMM_GAP_COUNT = "gapCount";
  public static final String COMM_IMPORT_CONTACTS = "showImportContacts";
  public static final String COMM_ENABLE_IF_REGISTERED = "enableOnlyIfRegistered";


  public static final String LANGUAGE_FROM_DEEPLINK_URL = "langFromDP";
  public static final String LANGUAGE_CODE_FROM_DEEPLINK_URL = "langCodeFromDP";
  public static final String EDITION_FROM_DEEPLINK_URL = "editionFromDP";

  //TODO:(Shashikiran.nr) if any issue with using this as Test base url , Please change it.
  public static final String TEST_BASEURl = "http://api-news.dailyhunt.in/";
  public static final int STORY_1ST_CHUNK_MAX_CHARS = 500;

  public static final String BUNDLE_GROUP_ID = "groupId";

  public static final String SIMILAR_VIDEO_SIMILAR = "SIMILAR_VIDEO";
  public static final String SOURCE = "source";
  public static final String EXPLORE_TYPE_MUTE = "mute";
  public static final String EXPLORE_TYPE_UNMUTE = "unmute";

  public static final int ACTIONABLE_SNACKBAR_DURATION = 10_000; // 10 seconds

  public static final String TOOL_TIP_LAST_LAUNCH_APP_COUNT = "tool_tip_app_launch_count";
  public static final int TOOL_TIP_DEFAULT_GAP_COUNT = 1;

  public static final String VIRAL_ARTICLE_ID = "viral_article_id";
  public static final String BUNDLE_LAUNCH_DEEPLINK = "launchDeeplinkBundle";

  public static final String URL_PARAM_ITEM_ID = "itemId";
  public static final String URL_PARAM_CLIENT_ID = "clientId";

  public static final String BUNDLE_NOTF_DONOT_AUTO_FETCH_SWIPEURL =
      "BUNDLE_NOTF_DONOT_AUTO_FETCH_SWIPEURL";
  public static final String HTTP_FEED_CACHE_DIR = "http_api_cache_feed";
  public static final String BUNDLE_LAUNCHED_FROM_LANGUAGE_CARD = "bundleLaunchedFromLanguageCard";

  public static final int DEFAULT_TICKER_INTERVAL = 5000;

  public static final String ENTITY_KEY = "entityKey";
  public static final String ENTITY_TYPE = "entityType";
  public static final String ENTITY_TITLE = "entityTitle";
  public static final String SUB_ENTITY_KEY = "sub_entity_key";
  public static final String DH_SECTION = "dh_section";


  public static final String BUNDLE_U_R_IN_HOME = "BUNDLE_U_R_IN_HOME";
  public static final String BUNDLE_LOC_FROM_LIST = "BUNDLE_LOC_FROM_LIST";

  public static final String HTML_AD_MICRO = "--USER_CLICK_URL--";

  public static final int REQUEST_CODE_LOCATION_ACTIVITY = UniqueIdHelper.getInstance()
      .generateUniqueId();
  public static final int REQUEST_CODE_LOCATION_SEARCH = UniqueIdHelper.getInstance().generateUniqueId();

  public static final String STATE_ID = "state_id";
  public static final String REMOVE_CITY_ID = "remove_city_id";
  public static final String PAGE_TYPE = "page_type";

  public static final String FEED_CACHE_DELAY = "FEED_CACHE_DELAY";
  public static final String BUNDLE_LAND_ON_HOME_TAB = "bundle_land_on_home_tab";
}
