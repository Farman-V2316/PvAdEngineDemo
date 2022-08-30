/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by karthik.r on 2019-08-22.
 */
public class Constants {
  public static final String EMPTY_STRING = "";

  public static final int ERROR_UNEXPECTED_INT = 1025;
  public static final String ERROR_NO_INTERNET = String.valueOf(1001);
  public static final String ERROR_CONVERSION = String.valueOf(1002);
  public static final String ERROR_HTTP_NOT_FOUND =
      String.valueOf(HttpURLConnection.HTTP_NOT_FOUND);
  public static final String ERROR_UNEXPECTED = String.valueOf(ERROR_UNEXPECTED_INT);
  public static final String ERROR_HTTP_NO_CONTENT =
      String.valueOf(HttpURLConnection.HTTP_NO_CONTENT);
  public static final String ERROR_NO_FEED_ITEMS_BECAUSE_OF_BLOCKED_SOURCES = String.valueOf(1026);

  public static final long DEFAULT_HTTP_CLIENT_TIMEOUT = 30_000;
  public static final long DEFAULT_HTTP_CONNECT_TIMEOUT = 10_000;

  public static final int TICKER_MAX_SIZE = 1000;
  public static final String TICKERURL = "http://stage-api-news.dailyhunt" +
      ".in/api/v2/tickers/posts/me?langCode=en,gu&pageDepth=0&entityType=HASHTAG&entityId=91581308b67fdfbcd24028a0c513bc37";

  public static final String NEWSPAPER = "Newspaper";
  public static final String CATEGORY = "category";
  public static final String STORY_ID = "StoryId";
  public static final String STORY_POSITION = "StoryPosition";
  public static final String PARENT_STORY_ID = "ParentStoryId";
  public static final String SOURCE_KEY = "sourceKey";
  public static final String BUNDLE_IS_CARD = "isCard";
  public static final String BUNDLE_STORY = "story";
  public static final String BUNDLE_EXTRA = "BUNDLE_EXTRA";
  public static final Long DEF_LIST_HEADER_DELAY = 600L; //10mins
  public static final int LAST_VERSION_BEFORE_PRIVACY_DIALOG = 396; // Corresponding to 9.0.3
  public static final float TITLE_LINE_SPACING = 1.1f;
  public static final String APP_ID = "DH_APP";
  public static final String SPLASH_FILE = "splash.png";
  public static final String SPLASH_DIR = "splash";
  public static final String DEV = "dev";
  public static final String PAGE_URL = "pageUrl";
  public static final String PAGE_ID = "pageId";
  public static final String LOCATION = "location";
  public static final String COLLECTION_ID = "collection_id";
  public static final String SUB_FORMAT = "sub_format";
  public static final String LIST_TYPE = "listType";
  public static final String IS_LANDING_STORY = "isLandingStory";
  public static final String LANDING_STORY_ID = "landingStoryId";
  public static final String REVIEW_ITEM = "reviewItem";
  public static final String GROUP_SECTION = "default";
  public static final String GROUP = "group";
  public static final String SUPPORT_ADS = "adsHelper";
  public static final String VIEW_ALL_URL = "viewAllUrl";
  public static final String SINGLE_PAGE = "singlePage";
  public static final String BUNDLE_CLEAR_ON_NO_CONTENT = "clearFPDataOnEmptyResponse";
  public static final String BUNDLE_ERROR_LAYOUT_ID = "errorLayoutId";
  public static final String HIDE_NO_CONTENT_SNACKBAR = "hideNoContentSnackbar";
  public static final String BUNDLE_IS_MY_POSTS_PAGE = "bundle_is_my_posts_page";
  public static final String BUNDLE_DELAY_SHOWING_FPE = "delay_showing_fpe";
  public static final String BUNDLE_ADDITIONAL_LOGTAG = "bundle_additional_logtag";
  public static final String CAN_SHOW_ITEM_DECORATION = "canShowItemDecoration";
  public static final String ITEM_LOCATION = "itemLocation";
  public static final String LIST_TRANSFORM_TYPE = "listTransformType";
  public static final String LIST_DIVIDER_HEIGHT = "lastDividerHeight";
  public static final String LIST_HORZ_PADDING = "listHorzPadding";
  public static final String LIST_TAB_TYPE = "tabtype";
  public static final String RESET_MUTE_STATE = "reset_mute_state";
  public static final String REPLACE_NOTI = "REPLACE_NOTI";
  public static final String LIST_VERTICAL_POSITION = "ListVerticalPosition";

  public static final String BUNDLE_CARDS_LIMIT = "cardsLimit";
  public static final String DISABLE_NP_CACHE = "disableNpCache";
  public static final String DISABLE_CACHE = "disableNpCache";
  public static final String DISABLE_FP_CACHE = "disableFpCache";
  public static final String LIST_TYPE_BOOKMARKS = "bookmarkList";
  public static final String WIDGET_TYPE_DETAIL = "relatedNews";

  public static final String UNIQUE_ID = "UNIQUE_ID";
  public static final String ADD_ID = "ADD_ID";
  public static final String GAID_OPT_OUT_STATUS = "gaidOptOutStatus";
  public static final String SPACE_STRING = " ";
  public static final String DOUBLE_SPACE_STRING = "  ";
  public static final String FORWARD_SLASH = "/";
  public static final String EQUAL_CHAR = "=";
  public static final String DOT = ".";
  public static final String EXCLAMATION_MARK = "!";
  public static final String COLLON = ":";
  public static final String SEMICOLON = ";";
  public static final String BR_TAG = "<br/>";
  public static final String ANCHER_OPEN_TAG = "<br/><a href=";
  public static final String CLOSE_TAG = ">";
  public static final String ANCHER_CLOSE_TAG = "</a><br/>";
  public static final String OPENING_BRACES = "(";
  public static final String CLOSING_BRACES = ")";
  public static final String BULLET = "\u2022";
  public static final String NULL = "null";
  public static final int COMMON_ID = 123;
  public static final double FONT_PADDING_FACTOR = 0.4;
  public static final int INVALID_INDEX = -1;
  public static final int NH_BROWSER_BACK_PRESS_REQUESTCODE = 100;
  public static final String HANDLE_BACK_PRESS_FUNCTION = "handleBackPress";
  public static final String HIDE_OR_SHOW_PANCHANG_INFO = "showHidePanchangInfo";


  public static final String NEW_LINE = "\n";

  public static final char SIZE_TOKEN = 'x';
  public static final String COMMA_CHARACTER = ",";
  public static final String PIPE_CHARACTER = "|";
  public static final String TEXT_ENCODING_UTF_8 = "UTF-8";

  public static final String NOTIFICATION_DEFAULT_ID = "-1";
  public static final int NOTIFICATION_DEFAULT_TIMESTAMP = -1;


  public static final String INTENT_TYPE_TEXT = "text/plain";
  public static final String HTML_MIME_TYPE = "text/html";
  public static final String INTENT_TYPE_IMAGE = "image/*";
  public static final String INTENT_MESSAGE = "Share with";

  public static final String GIF_EXTENSION = ".gif";
  public static final int MAX_NUMBER_OF_FILES = 30;
  public static final String MARATHI_LANGUAGE_CODE = "mr";
  public static final String ENGLISH_LANGUAGE_CODE = "en";
  public static final String TAMIL_LANGUAGE_CODE = "ta";
  public static final String TELUGU_LANGUAGE_CODE = "te";
  public static final String HINDI_LANGUAGE_CODE = "hi";
  public static final String GUJRATI_LANGUAGE_CODE = "gu";
  public static final String KANNADA_LANGUAGE_CODE = "kn";
  public static final String MALYALAM_LANGUAGE_CODE = "ml";
  public static final String BENGALI_LANGUAGE_CODE = "bn";
  public static final String URDU_LANGUAGE_CODE = "ur";
  public static final String ORIYA_LANGUAGE_CODE = "or";
  public static final String BHOJPURI_LANGUAGE_CODE = "bh";
  public static final String NEPALI_LANGUAGE_CODE = "ne";
  public static final String PUNJABI_LANGUAGE_CODE = "pa";
  public static final String VIDEO_TYPE = "VIDEO";
  public static final String VERSIONED_DB_LOCK = "VersionedDbLock";
  public static final int SHARE_DESCRIPTION_SIZE = 140;
  public static final String NH_COMMAND_PREFIX = "nhcommand://";
  public static final String URL_MARKET_FORMAT = "market";
  public static final String URL_HTTP_FORMAT = "http";
  public static final String URL_HTTPS_FORMAT = "https";
  public static final String URL_PATH_API = "api";
  public static final String URL_PATH_API_V2 = "v2";
  public static final String URL_QUERY_LANG_CODE = "langCode";
  public static final String URL_QUERY_APP_LANG = "appLanguage";
  public static final String URL_QUERY_RESOURCE_ID = "resourceId";

  //(bheemesh): Payment url append variable.
  public static final String BIT_AND = "&";


  public static final String STANDARD_EXTERNAL_URL = "http://www.google.com";
  public static final int NETWORK_TIMEOUT_MSEC = 60000;
  public static final String HEADER_USER_AGENT = "User-Agent";
  public static final String HEADER_CLIENT_TYPE = "client-type";
  public static final String HEADER_VALUE_ANDROID = "Android";
  public static final String HEADER_VALUE_ANDROID_SMALL = "android";
  public static final String ITEM_LOCATION_LOCAL = "local";

  public static final Map<String, String> COUNTRY_CODE_TO_EDITION = new HashMap<String, String>() {{
    put("bd", "bangladesh");
    put("af", "africa");
    put("in", "india");
  }};

  public static final String DEFAULT_APP_FONT = "SERIF";
  public static final String DEFAULT_FONT_NAME = "Noto Sans";

  public static final String ELLIPSIZE_END = "…";

  public static final String PLAY_STORE_REFERRER_VALUE = "utm_source%3Dcheckappupgrade";
  public static final String REFERRER = "referrer";

  /**
   * Action for opening books home
   */
  public static final String NEWS_HOME_ACTION = "NewsHomeOpen";
  public static final String TV_HOME_ACTION = "TVHomeOpen";
  public static final String SPLASH_ACTION = "SplashOpen";
  public static final String NEWS_DETAIL_ACTION = "NewsDetailOpen";
  public static final String CAROUSEL_DETAIL_ACTION = "CarouselDetailOpen";
  public static final String OP_DETAIL_ACTION = "OPDetailOpen";
  public static final String DEEP_LINK_ACTION = "DeepLinkOpen";
  public static final String SAVED_ARTICLES = "SavedArticles";
  public static final String NOTIFICATION_INBOX = "NotificationInbox";
  public static final String LIVE_HOME_ACTION = "LiveHomeOpen";
  public static final String DEFERRED_DEEP_LINK_ACTION = "DeferredDeeplinkOpen";
  public static final String ADD_COMMENT_ACTION = "AddCommentOpen";
  public static final String LIKES_LIST_OPEN_ACTION = "LikesListOpen";
  public static final String ENTITY_OPEN_ACTION = "EntityDetailOpen";
  public static final String ADD_PAGE_OPEN_ACTION = "AddPageOpen";
  public static final String REORDER_PAGE_OPEN_ACTION = "ReorderPageOpen";
  public static final String SIMILAR_STORIES_OPEN_ACTION = "SimilarStoriesOpen";
  public static final String ONBOARDING_ACTIVITY_OPEN_ACTION = "OnboardingOpen";
  public static final String WALKTHROUGH_ACTION = "WalkThroughAction";
  public static final String IMPORT_CONTACTS_ACTIONS = "ImportContactsAction";
  public static final String ALL_COMMENTS_ACTION = "allComments";
  public static final String GALLERY_PHOTO_ACTION = "galleryPhotoAction";
  public static final String VIEW_PHOTO_ACTION = "viewPhotoAction";
  public static final String HOME_LOADER_ACTION = "home_loader_action";
  public static final String EXIT_SPLASH_AD_ACTION = "ExitSplashAdAction";
  public static final String EXIT_SPLASH_AD_CLOSE_ACTION = "ExitSplashAdCloseAction";

  /*
   Naviagation event action for sharing post
  */
  public static final String SHARE_POST_ACTION = "sharePostAction";
  /*
   To be used for specifying package name of application where content should be shared
   */
  public static final String BUNDLE_SHARE_PACKAGE_NAME = "sharePackageName";

  /*
   To be used for specifying share ui type
   */
  public static final String SHARE_UI_TYPE = "share_ui_type";

  public static final String WEB_HOME_ACTION = "WebHomeOpen";


  public static final String HEADLINES = "headlines";
  public static final String VIRAL = "viral";


  public static final String ENTITY_TYPE = "pageType";
  public static final String ENTITY_TYPE_NEWSPAPER = "newspaper";
  public static final String ENTITY_TYPE_SOURCE = "SOURCE";
  public static final String ENTITY_TYPE_TOPIC = "topic";
  public static final String DEFAULT_LANGUAGE = "en";
  public static final String PREFERENCE_TYPE = "preferenceType";
  public static final String BUNDLE_NOTIFICATION_ID = "nhNotificationId";
  public static final String BUNDLE_NOTIFICATION_UNIQUE_ID = "NotificationUniqueId";
  public static final String BUNDLE_ACTIVITY_REFERRER = "activityReferrer";
  public static final String BUNDLE_LAUNCHED_FROM_SETTINGS = "isLanguageSettingMenu";
  public static final String IS_FROM_ADJUNCT_CROSS = "isFromAdjunctCross";
  public static final String BUNDLE_LAUNCHED_FROM_LOCATION_CARD = "isLocationCardMenu";
  public static final String BUNDLE_NAVIGATION_TYPE = "nhNavigationType";
  public static final String BUNDLE_SPLASH_RELAUNCH = "bundleSplashRelaunch";
  public static final String DEEP_LINK_DOUBLE_BACK_EXIT = "deeplinkDoubleBackExit";
  public static final String DEEP_LINK_SKIP_HOME_ROUTING = "deeplinkSkipHomeRouting";
  public static final String BUNDLE_DEEPLINK_EXTRA_PARAMS = "deeplinkExtraParams";
  public static final String BACK_URL_REFERRER = "backUrlReferrer";
  public static final String DEEPLINK_KEY = "deepLinkKey";
  public static final String BUNDLE_POST_ID = "postId";
  public static final String BUNDLE_AD_ID = "adId";
  public static final String BUNDLE_CONTENT_URL = "contentUrl";
  public static final String BUNDLE_CONTENT_URL_OPTIONAL = "BUNDLE_CONTENT_URL_OPTIONAL";
  public static final String BUNDLE_IS_PRIMARY_CONTENT = "primarycontent";
  public static final String BUNDLE_ID = "id";
  public static final String BUNDLE_DOWNLOAD_ALLOWED = "download_allowed";
  public static final String BUNDLE_IS_VIRAL = "is_viral";
  public static final String BUNDLE_LIKES_COUNTS = "bundle_likes_counts";
  public static final String BUNDLE_IS_LOCAL_ZONE = "is_local_zone";
  public static final String BUNDLE_LOCAL_SELECTED_LOCATION = "local_selected_location";

  public static final String BUNDLE_POST_IDS = "postIds";
  public static final String BUNDLE_KEEP_POST_IDS = "keeppostIds";
  public static final String BUNDLE_SHOW_COUNT = "BUNDLE_SHOW_COUNT";
  public static final String BUNDLE_SHARE_URL = "BUNDLE_SHARE_URL";
  public static final String BUNDLE_SHARE_TITLE = "BUNDLE_SHARE_TITLE";
  public static final String BUNDLE_DESCRIPTION = "BUNDLE_DESCRIPTION";
  public static final String BUNDLE_IMAGE_URL = "BUNDLE_IMAGE_URL";
  public static final String BUNDLE_SOURCE_URL = "BUNDLE_SOURCE_URL";
  public static final String BUNDLE_SOURCE_NAME = "BUNDLE_SOURCE_NAME";
  public static final String BUNDLE_TAG_HANDLE = "tagHandle";
  public static final String BUNDLE_TAG_HANDLE_NAME = "tagHandleName";
  public static final String BUNDLE_CREATE_POST_TAG_DATA = "createPostTagData";
  public static final String BUNDLE_CREATE_POST_NEXT_CARD_ID_FOR_LOCAL_CARD = "bundle_create_post_next_card_id_for_local_card";
  public static final String BUNDLE_PARENT_ID = "parentId";
  public static final String BUNDLE_POST_SOURCE_ASSET="bundle_post_source_asset";
  public static final String BUNDLE_IS_COMMENT_ONLY = "BUNDLE_IS_COMMENT_ONLY";
  public static final String BUNDLE_VIEW_TYPE = "viewType";
  public static final String BUNDLE_LIKE_TYPE = "liketype";
  public static final String BUNDLE_GUEST_COUNT = "guest_count";
  public static final String BUNDLE_MODE = "MODE";
  public static final String BUNDLE_START = "START";
  public static final String BUNDLE_EVENT_NAME = "BUNDLE_EVENT_NAME";
  public static final String BUNDLE_CREATION_TIME = "bundle_creation_time";
  public static final String BUNDLE_LEVEL = "bundle_post_level";
  public static final String BUNDLE_SOURCE_ID = "bundle_source_id";
  public static final String BUNDLE_SOURCE_ENTITY = "bundle_source_entity";
  public static final String BUNDLE_SOURCE_BLOCK = "bundle_source_block";
  public static final String BUNDLE_SOURCE_LANG = "bundle_source_lang";
  public static final String BUNDLE_SOURCE_TYPE = "bundle_source_type";
  public static final String BUNDLE_CLEAR_EXISTING = "bundle_clear_existing";

  public static final String SEARCH_PHOTO_GRID = "SEARCH_PHOTO_GRID";

  public static final String FIREBASE_DEEP_LINK_URL = "firebaseDeepLinkUrl";
  public static final String APPSFLYER_DEEP_LINK_URL = "appsFlyerDeepLinkUrl";
  public static final String APPSFLYER_DEEP_LINK_RESPONSE = "appsFlyerDeepLinkResponse";
  public static final String APPSFLYER_DEEP_LINK_HANDLED = "appsFlyerDeepLinkHandled";
  public static final String DEEP_LINK_URL = "deeplinkurl";
  public static final String CONTENT_URL = "contentUrl";
  public static final String POST_ENTITY_LEVEL = "postEntityLevel";
  public static final String SELECTED_DEEP_LINK_URL = "selected_deeplinkurl";
  public static final String REQUEST_METHOD = "REQUEST_METHOD";
  public static final String CONTENT_ID = "CONTENT_ID";

  public static final String SHARE_APP_DEFAULT = "http://dhunt.in/DWND";
  public final static String DAILYHUNT_PACKAGE = "com.eterno";
  public static final String APP_PLAY_STORE_HOST = "play.google.com";
  //Use this to build play store link to any package
  public static final String APP_PLAY_STORE_LINK_TEMPLATE =
      "https://play.google" + ".com/store/apps/details?id=";
  public static final String APP_PLAY_STORE_LINK = APP_PLAY_STORE_LINK_TEMPLATE + DAILYHUNT_PACKAGE;

  public static final String HAMBURGER_FAQ = "http://support.dailyhunt.in/support/home";

  public static final int FEEDBACK_CONTENT_LIMIT = 500;

  public static final String FEEDBACK_CONCERN_AREA_ACTION = "concernArea";
  public static final String FEEDBACK_COUNTRY_CODE_ACTION = "countryCode";

  public static final String FEEDBACK_CONTENT = "feedbackContent";
  public static final String FEEDBACK_EMAIL = "emailaddress";
  public static final String FEEDBACK_CONTACT = "contactNumber";

  public static final String FEEDBACK_OPTION_NEWS = "news";
  public static final String FEEDBACK_OPTION_BUZZ = "buzz";
  public static final String FEEDBACK_OPTION_SUGGESTIONS = "suggestions";
  public static final String FEEDBACK_OPTION_PUBLISH = "publish";
  public static final String FEEDBACK_OPTION_OTHERS = "others";

  public static final String TRIGGER_ACTION = "trigger_action";
  public static final String SECTION1 = "section1";
  public static final int SHARE_REQUEST_CODE = 2;
  public static final int ADJUNCT_LANGUAGE_REQUEST_CODE = 3010;
  public static final String FEEDBACK_OPEN = "FeedbackOpen";


  // Editions Key Constant [India]
  public static final String INDIA_KEY = "india";
  public static final String INDIA_NAME = "India";
  public static final String MIME_TYPES_SUPPORTED = "png,jpg,webp";

  public static final String WEB_CLIENT_ID =
      "17344308122-r2aasaqu36rr98cdeqfi3qgknh19r9na.apps.googleusercontent.com";

  //News
  public static final double KB = 1024.0;
  public static final String UNLOGGED_CACHE_BROWSED_TIME = "unlogged_cachebrowsed_time";
  public static final String WEB_VIEW_PACKAGE = "android.webkit.WebView";
  public static final String ON_PAUSE = "onPause";
  public static final String ON_RESUME = "onResume";

  //TV Section
  public static final String TV_BUNDLE_ASSET_ITEM_TYPE_GIF = "gif";
  public static final String TV_BUNDLE_ASSET_ITEM_TYPE_IMAGE = "photo";
  public static final String TV_BUNDLE_ASSET_ITEM_TYPE_VIDEO = "video";

  public static final String TV_BUNDLE_ASSET_GROUP = "content";
  public static final String TV_BUNDLE_LIVE_TV = "livetv";

  public static final String TV_BUNDLE_CHANNEL = "channel";
  public static final String TV_BUNDLE_PLAYLIST = "playlist";
  public static final String TV_BUNDLE_SHOW = "show";
  public static final String TV_BUNDLE_TAG = "tag";


  // DNS Caching
  public static final String DNS_LOOKUP_CACHE = "DNS_LOOKUP_CACHE";
  public static final String DNS_IP_FROM_SERVER = "DNS_IP_FROM_SERVER";
  public static final String DNS_LOOKUP_TIMEOUT = "DNS_LOOKUP_TIMEOUT";
  public static final String DNS_BG_LOOKUP_TIMEOUT = "DNS_BG_LOOKUP_TIMEOUT";
  public static final String DNS_TP_LOOKUP_TIMEOUT = "DNS_TP_LOOKUP_TIMEOUT";
  public static final String DNS_FIRST_CACHE_TTL = "DNS_FIRST_CACHE_TTL";
  public static final String DNS_SECOND_CACHE_TTL = "DNS_SECOND_CACHE_TTL";
  public static final String DISABLE_DNS_CACHING = "DISABLE_DNS_CACHING";
  public static final String HEARTBEAT_INTERVAL = "HEARTBEAT_INTERVAL";
  public static final String DNS_SERVERS = "DNS_SERVERS";

  //SSO Errors
  public static final String SSO_EMPTY_PASSWORD_ERROR = "Empty Password";

  //Retry logic variables
  public static final int RETRY_MAX_INTERVAL = 300;
  public static final int RETRY_MAX_ATTEMPT = 30;
  public static final int RETRY_INITIAL_INTERVAL = 2;
  public static final double RETRY_MULTIPLIER = 1.5;


  public static final String HASH_CHARACTER = "#";
  public static final String ZERO_STRING = "0";
  public static final String COUNT_ONE_STRING = "1";

  public static final String APP_INDEXING_REFERRER = "fromAppIndexing";

  /*
   * String array of size units
   */
  public static final String UNDERSCORE_CHARACTER = "_";
  public static final String TEXT_STRING = "string";

  public static final String DEFAULT_NOTIFICATION_TEXT = "defaultNotificationText";
  public static final int DEFAULT_NOTIFICATION_DURATION = 5 * 60 * 1000; //5 minutes in
  public static final int DEFAULT_NOTIFICATION_FG_FLAGS = 0b1111;
  // milliseconds
  public static final String PULL_NOTIFICATIONS_STATE = "upgrade";
  public static final String PULL_NOTIFICATION_JOB_TAG = "NotificationJobTag";
  public static final String PULL_NOTIFICATION_TAG = "Pull Notification";
  public static final String BUNDLE_ACTIVITY_REFERRER_FLOW = "activityReferrerFlow";
  public static final String BUNDLE_ACTIVITY_REFERRER_TYPE = "activityReferrerType";
  public static final String BUNDLE_ACTIVITY_REFERRER_FLOW_PARENT = "activityParentReferrerFlow";
  public static final String BUNDLE_ACTIVITY_REFERRER_LEAD = "activityReferrerLead";

  public static final String MENU_FRAGMENT_OPEN_ACTION = "MenuFragmentOpenAction";
  public static final String BUNDLE_MENU_CLICK_LOCATION = "menuClickLocation";
  public static final String BUNDLE_MENU_ENTITY_INFO = "menuEntityInfo";
  public static final String BUNDLE_TARGET_NAVIGATION_ID = "targetNavId";

  /**
   * Location of fetched list bundle key
   */
  @NonNull
  public static final String BUNDLE_LOCATION_ID = "locationid";

  /**
   * Entity id bundle key
   */
  @NonNull
  public static final String BUNDLE_ENTITY_ID = "entityid";

  public static final String FETCH_LOCATION_LIST = "list";
  public static final String FETCH_LOCATION_DETAIL = "detail";

  /**
   * To decide whether to show plus button for adding page on favourite list or not.
   * used for showing plus button when coming from add page activity to search activity to cards
   * fragment 2 -> cards adapter 2
   */
  public static final String SHOW_ADD_BUTTON_FOR_ENTITY = "showAddButtonForEntity";
  /**
   * Usecase parameter for commonMessageDialog for delete post usecase
   */
  public static final String DELETE_POST_DIALOG_USECASE = "delete_post";

  public static final String DELETE_LOCAL_CARD_USECASE = "delete_local_card";

  public static final String OFFLINE_FEED = "offline_feed";
  @NotNull
  public static final String LANGUAGE_SELECT_FEED_ITEM_ID = "language_select_feed_item_id";
  @NotNull
  public static final String LOCATION_SELECT_FEED_ITEM_ID = "location_select_feed_item_id";

  /**
   * To pass clicked list of l1 ids in ',' seperated string format to menu dependent usecases like
   * dislikeUsecase. now only one id is getting passed but keeping list for fututre case
   */
  public static final String BUNDLE_L1_IDS = "bundle_l1_idS";

  /**
   * To pass clicked list of l2 ids in ',' seperated string format to menu dependent usecases like
   * dislikeUsecase
   */
  public static final String BUNDLE_L2_IDS = "bundle_l2_ids";
  /*
   * To pass extra information regarding clicked asset and to other activities for executing
   * usecase for menu clicks.
   * */
  public static final String BUNDLE_MENU_ARGUMENTS = "bundle_menu_arguments";

  /**
   * To pass search location to searchCardsFragment
   */
  public static final String BUNDLE_SEARCH_LOCATION = "bundle_search_location";
  /**
   * To pass SSO.getInstance().encryptedSessionData to delete post usecase. could not use
   * directly because of dependency structure.
   * */
  public static final String BUNDLE_DELETE_HEADER="bundle_delete_header";

  /**
   * To show view more content on click of view more item in the list
   */
  public static final String CAROUSEL_LOAD_VIEW_MORE ="bundle_carousel_load_view_more";
  /*
   * To parse time spent track event entity
   * */
  public static final String BUNDLE_TIME_SPENT_TRACK_ENTITY = "bundle_time_spent_track_entity";

  /**
   * To show corosal in explicit signal
   */
  public static final String CAROUSEL_LOAD_EXPLICIT_SIGNAL ="bundle_carousel_load_explicit_signal";
  public static final String EVENT_CREATED_AT ="event_created_at";
  public static final String FOLLOW_EXPLICIT ="follow_explicit";
  public static final String BLOCK_EXPLICIT ="block_explicit";
  public static final String CAROUSEL_TYPE ="carousel_type";

  /*
  * To parse boolean flag to dislike usecase whether dislike is marked for recent dislike getting
  *  sent in payload of each feed item request
  * */
  public static final String BUNDLE_MARK_DISLIKE_FOR_PAYLOAD = "bundle_mark_dislike_for_payload";
  public static final String DISLIKE_FROM_REPORT = "dislike_from_report";

  /**
   * To parse boolean flag to usecase whether client can autoplay video at a moment or not
   */
  public static final String BUNDLE_CAN_AUTOPLAY_VIDEO = "bundle_can_autoplay_video";

  public static final Long DEFAULT_LOCAL_CARD_TTL = 600_000L;

  public static final String URL_PARAM_ALLOW_LOCAL_CARD = "allowLocalPost";

  // BaseError Error codes
  public static final String HTTP_304_NOT_MODIFIED = "HTTP_304_NOT_MODIFIED";
  public static final int HTTP_SUCCESS = 200;
  public static final int HTTP_UNKNOWN = -1;
  public static final int HTTP_BAD_REQUEST = 400;
  public static final int HTTP_UNAUTHORISED = 401;

  public static final String ERROR_HTTP_NOT_MODIFIED = String.valueOf(HttpURLConnection
      .HTTP_NOT_MODIFIED);
  public static final String ERROR_SERVER_ISSUE = String.valueOf(501);


  public static final String RETROFIT_BASE_URL_END_TOKEN = "/";

  public static String EP_APP_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=" +
      "in.dailyhunt.examprep&utm_source=Dailyhunt_app&utm_medium=dailyhunt_hamburger" +
      "&utm_campaign=dailyhunt_hamburger";

  public static String EP_APP_PACKAGE_NAME = "in.dailyhunt.examprep";
  public static final int MIN_BATTERY_PERCENT = 20;
  public static final String IS_FIRST_TIME_PULL_NOTIFICATION = "isFirstTimePullNotification";
  public static final String CAN_BE_REPLACED = "canbeReplace";

  public static final String LAST_SHARE_TIME = "share_time_";

  public static final String REFERRER_RAW = "REFERRER_RAW";
  public static final String REFERRER_RAW_SS = "ss";
  public static final int FIRST_TIME_PULL_DELAY = 3600; // In Seconds. Will use this value only if
  // server sends this value as 0.

  public final static String BUNDLE_DEFERRED_NOTIFICATION_ID = "deferrednotificationId";

  public final static String BUNDLE_DISPLAY_TIME = "displayTime";
  public final static String BUNDLE_EXPIRY_TIME = "expiryTime";
  public static final String IS_SELF_BOARDING = "IS_SELF_BOARDING";
  public static final String BUNDLE_LOCAL_CARD_ID = "local_card_id";

  //App launch config constants
  public final static String APP_SECTION = "appSection";
  public final static String APP_SECTION_ID = "appSectionId";
  public final static String APP_SECTION_LAUNCH_ENTITY = "appSectionLaunchEntity";

  public final static String KEY_NAVIGATION_TYPE = "navigationType";
  public final static String WEB_CONTENT_URL = "webContentUrl";
  public final static String WEB_SECTION_TYPE = "webSectionType";

  public static final String V4BACKURL = "v4BackUrl";
  public static final String V4SWIPEURL = "v4SwipeUrl";
  public static final String BUNDLE_NEWS_DETAIL_NON_SWIPEABLE = "news_detail_non_swipeable";

  public static final String APP_SECTION_HOME_ROUTER_OPEN = "appSectionHomeRouterOpen";
  public static final String NOTIFICATION_ROUTER_OPEN = "NotificationRouterOpen";
  public static final String NOTIFICATION_BASE_MODEL = "notifBaseModel";
  public static final String NOTIFICATION_BASE_MODEL_TYPE = "notifBaseModelType";
  public static final String NOTIFICATION_BASE_MODEL_STICKY_TYPE = "notifBaseModelStickyType";
  public static final String NOTIFICATION_BASE_MODEL_ARRAYLIST = "notifBaseModelArrayList";
  public static final String NOTIFICATION_TARGET_INTENT = "notifTargetIntent";

  //Use this to build the market link to any package on Google Play store
  public static final String MARKET_LINK_TEMPLATE = "market://details?id=";
  public static final String APP_MARKET_LINK = MARKET_LINK_TEMPLATE + DAILYHUNT_PACKAGE;
  public static final String GOOGLE_PLAYSTORE_PACKAGE = "com.android.vending";

  // Dialog actions
  public static final String DIALOG_ACCEPT = "accept";
  public static final String DIALOG_LATER = "later";
  public static final String DIALOG_REJECT = "reject";

  // Log collection related constants
  public static final String LOG_COLLECTION_PATH = "collectLogs";
  public static final String LOG_COLLECTION_IN_PROGRESS = "LOG_COLLECTION_IN_PROGRESS";
  public static final String LOG_COLLECTION_UPLOADING_PENDING = "LOG_UPLOADING_PENDING";
  public static final String LOG_COLLECTION_END_TIME = "LOG_COLLECTION_END_TIME";
  public static final String LOG_COLLECTION_LAST_COLLECTION_TIME =
      "LOG_COLLECTION_LAST_COLLECTION_TIME";
  public static final String LOG_COLLECTION_UPLOAD_URL_PARAM = "u";
  public static final String LOG_COLLECTION_AUTH_TOKEN = "LOG_COLLECTION_AUTH_TOKEN";
  public static final String LOG_COLLECTION_DURATION = "d";
  public static final String LOG_COLLECTION_UPLOAD_URL_VALUE = "LOG_COLLECTION_UPLOAD_URL";
  public static final String LOG_COLLECTION_FREQUENCY = "f";
  public static final String LOG_COLLECTION_AUTH_TOKEN_PARAM = "a";
  public static final String AUTHORIZATION = "Authorization";
  public static final String LOG_COLLECTION_BUFFER_SIZE_PARAM = "b";
  public static final String LOG_COLLECTION_EXPIRY_PARAM = "e";
  public static final String LOG_COLLECTION_CLIENT_ID = "c";
  public static final String LOG_COLLECTION_DIRECTORY = "logs";
  public static final String LOG_COLLECTION_FILE_NAME = "systemlogs.log";
  public static final String LOG_COLLECTION_LATEST_FILE_NAME = "lastest.log.file";
  public static final int TOAST_LENGTH_LONG = 3500;
  public static final int TOAST_LENGTH_SHORT = 2000;
  public static final long TWO_MB_IN_BYTES = 2097152; // 2 * 1024 * 1024


  public static final String ITEM_ID = "item_id";
  public static final String ITEM_TYPE = "item_type";
  public static final String EVENT_ITEM_TYPE = "event_item_type";
  public static final String INITIAL_COMMENTS = "initial_comments";
  public static final String COMMENT = "comment";
  public static final String COMMENT_ID = "comment_id";
  public static final String BUNDLE_COMMENTS_MODEL = "bundle_comments_model";
  public static final String BUNDLE_PARENT_COMMENTS_MODEL = "bundle_parent_comments_model";
  public static final String VIEW_TYPE = "view_type";
  public static final String SHOW_SHARE_VIEW = "show_share_view";
  public static final int MIN_COLLECTION_FOR_REQUEST = 1;

  public interface Extensions {
    String PNG = "png";
    String JPEG = "jpeg";
    String WEBP = "webp";
    String DOT_WEBP = ".webp";
    String DOT_JPG = ".jpg";
  }

  public static final String NOT_FOUND_IN_NETWORK = "Not found in network";
  public static final String NOT_FOUND_IN_CACHE = "Not found in cache";
  public static final String NOT_FOUND_IN_DB = "Not found in db";

  public static final String DOT_WITH_SPACE = " . ";

  public static final String HTTP_POST = "POST";
  public static final String HTTP_GET = "GET";

  public static final String HTTP_401_ERROR_STATUS = "status";
  public static final String HTTP_401_SPECIFIC_ERROR_CODE = "code";
  public static final String HTTP_401_SPECIFIC_ERROR_MESSAGE = "message";
  public static final String HTTP_401_ERROR_AUTH_01 = "AUTH01";
  public static final String HTTP_401_ERROR_AUTH_02 = "AUTH02";
  public static final String HTTP_401_ERROR_AUTH_03 = "AUTH03";
  public static final String HTTP_401_INVALID_RESPONSE_BODY = "HTTP_401_INVALID_RESPONSE_BODY";


  public static final String BOOKS_APP_PACKAGE_NAME = "in.dailyhunt.ebooks";
  public static final String BOOKS_APP_PLAY_STORE_LINK = APP_PLAY_STORE_LINK_TEMPLATE +
      BOOKS_APP_PACKAGE_NAME;
  public static final String BOOKS_APP_MARKET_LINK = MARKET_LINK_TEMPLATE + BOOKS_APP_PACKAGE_NAME;

  public static final String PROMO_ID_PARAM_KEY = "promo_id";

  public static final String BOOKS_APP_DEEPLINK_ACTIVITY = "com.newshunt.app.view.activity" +
      ".DeepLinkActivity";

  public static final String FLAG_STICKY_NOTIFICATION_LANDING = "sticky_notification_landing";
  public static final String HOME_INTENT = "home_intent";

  public static final long INVALID_TIME = 0L;

  public static final String VALIDATE_DEEPLINK = "VALIDATE_DEEPLINK";

  public static final int DEFAULT_NO_AUTO_REFRESH_TAB_RECREATE = 10;

  public static final String APPS_ON_DEVICE = "appsOnDevice";

  public static final String NEWS_LIST_SELECTED_INDEX = "NewsListIndex";
  public static final String COLLECTION_SELECTED_INDEX = "collectionIndex";
  public static final String BUNDLE_USE_COLLECTION = "useCollection";

  public static final String BUNDLE_UI_COMPONENT_ID = "bundleUiComponentId";
  public static final String VIRAL_MEME = "viralmeme";
  public static final String VIRAL_PHOTO_ACTION = "ViralPhotoZoom";
  public static final int REQ_VIRAL_FOLLOW = 1;

  public static final String CAROUSEL_RATING = "CAROUSEL_RATING";
  public static final String OPENING_BRACKET = "[";
  public static final String CLOSING_BRACKET = "]";

  public static final String WITH_DOT = "wdot";

  public static final String PICASSO_DISK_CACHE = "image_disk_cache";

  public static final long _7Days = TimeUnit.DAYS.toMillis(7);
  public static final int PREFETCH_IMAGE_COUNT = 6;
  public static final int PREFETCH_CAROUSEL_FULL_COUNT = 2;
  public static final int PREFETCH_CAROUSEL_THREE_FOURTH_COUNT = 3;
  public static final int PREFETCH_CAROUSEL_COUNT = 3;
  public static final int PREFETCH_CAROUSEL_LIST_COUNT = 3;


  public static final int DEFAULT_MAX_COMMENT_COUNT_STORY_DETAIL = 3;
  public static final String HEADER_SESSION_DATA = "dhat";

  public static final String PARAM_COUNT = "count";

  public static final String BUNDLE_DISCLAIMER_URL = "disclaimerUrl";
  public static final int VERSION_NEWSHUNT_NEWS_DB = 4;
  public static final String GRID_WIDGET_DISPLAY_TYPE = "GRID";
  public static final String RECOMMENDED_GRID_REFERRER = "news_publishers_home";
  public static final String NP_FOLLOW = "np_follow";
  public static final String NP_UNFOLLOW = "np_unfollow";
  public static final String TOPIC_FOLLOW = "topic_follow";
  public static final String TOPIC_UNFOLLOW = "topic_unfollow";
  public static final String LOCATION_FOLLOW = "location_follow";
  public static final String LOCATION_UNFOLLOW = "location_unfollow";

  public static final int ONE = 1;
  public static final String UGC_FEED_ASSET = "ugc_feed_asset";
  public static final String QUERY_OPERATOR = "?";
  public static final String QUERY_PARAM_PREFIX = "&";
  public static final String UGC_SELECTED_FEED_ASSET_POSTION = "ugc_selected_feed_asset_position";
  public static final String HANDSHAKE_RESPONSE_VERSION = "HANDSHAKE_RESPONSE_VERSION";
  public static final String HANDSHAKE_RESPONSE = "HANDSHAKE_RESPONSE";
  public static final String INTENT_ACTIONS_LAUNCH_FOLLOW_HOME = "FollowHomeOpen";
  public static final String INTENT_ACTION_LAUNCH_FOLLOW_EXPLORE = "FollowExploreOpen";
  public static final String INTENT_ACTION_LAUNCH_SEARCH = "LaunchSearchDH";

  // For DHTV section
  public static final String INTENT_ACTIONS_LAUNCH_DHTV_HOME =
      "com.dhtvapp.views.homescreen.DHTVHomeActivity";

  public static final String ACQ_REF_UTM_SOURCE = "source";
  public static final String ACQ_GOOG_REFERRER = "referrer";
  public static final String ACQ_FIREBASE_REFERRER = "firebase";
  public static final String ACQ_APPSFLYER_REFERRER = "appsflyer";
  public static final String ACQ_FB_REFERRER = "facebook";

  // Adding tag to webviews tracked by OM SDK. These webviews must be destroyed after a delay.
  public static final String OM_WEBVIEW_TAG = "om_webview_tag";

  public static final String GROUP_TYPE_COLLECTION = "COLLECTION";
  public static final String DOWNLOAD_PERCENT = "downloadPercent";
  public static final String CAN_SHARE = "canshare";
  public static final String DH_SHARE_HIDDEN_DIR = ".dh_share";
  public static final String DOWNLAOD_FAILED = "downloadFail";
  public static final String FILEPATH = "filepath";
  public static final String FILE_TYPE = "fileType";
  public static final String GIF = "gif";
  public static final String IMAGE = "image";
  public static final String DOWNLOAD_URL = "url";
  public static final String RECEIVER = "receiver";

  public static final String MM_CAROUSEL = "mm_carousel";
  public static final String CAROUSEL = "carousel";
  public static final String COLLECITON_LIST = "LIST";

  public static final String IS_SHARED = "IS_SHARED";
  public static final String IS_LIKED = "IS_LIKED";
  public static final String IS_UNLIKED = "IS_UNLIKED";
  public static final String IS_COMMENTED = "IS_COMMENTED";
  public static final String BUNDLE_EXPLORE_NAV_MODEL = "EXPLORE_NAV_MODEL";
  public static final String NEWS_CATEGORY_ACTION_WEBITEM = "showWebItem";

  public static final String MONKEY_LOG_TAG = "DHMonkey";
  public static final String BUNDLE_FOLLOW_TAB_LANDING_INFO = "bundle_follow_tab_landing_info";

  public static final String BUNDLE_SEARCH_CONTEXT = "bundle_search_context";
  public static final String BUNDLE_SEARCH_CONTEXT_PAYLOAD = "bundle_search_context_payload";
  public static final String BUNDLE_SEARCH_QUERY = "bundle_search_query";
  public static final String BUNDLE_SEARCH_MODEL = "bundle_search_model";
  public static final String SEARCH_QUERY_PARAM_KEY = "query";
  public static final String BUNDLE_SEARCH_HINT = "bundle_search_hint";
  public static final String BUNDLE_SEARCH_TYPE = "bundle_search_type";
  public static final String BUNDLE_SEARCH_REQUEST_TYPE = "bundle_search_req_type";
  public static final String EXP_SEARCH_NO_RES_REASON = "no_result_reason";
  public static final String EXP_SEARCH_NO_RES_REASON_CL_ERR = "client_error";
  public static final String EXP_SEARCH_NO_RES_REASON_USER_CANCEL = "user_cancel";
  public static final String SEARCH_HAS_USER_TYPED_NA = "NA";
  public static final String BUNDLE_QUERY_SUBMIT_TIME = "bundle_query_submit_time";
  public static final String BUNDLE_TAB_TYPE = "BUNDLE_TAB_TYPE";
  public static final String IN_LIST = "in_list";
  public static final String IN_STORY_DETAIL = "in_story_detail";
  public static final String ACCORDION = "accordion";
  public static final String FOLLOWED_ENTITIES = "followed_entities";
  public static final String BLOCKED_ENTITIES = "blocked_entities";
  public static final String FEED = "feed";
  public static final int COLD_START_ERROR_SCREEN_RESPONSE_CODE = 5;
  public static final String SOURCE_DEEPLINK_INITIALS = "http://m.dailyhunt.in/news/india/english/";
  public static final String DEEPLINK_SOURCE_IDENTIFIER = "-epaper-";
  public static final String DEEPLINK_CATEGORY_IDENTIFIER = "-updates-";
  public static final String SOURCE_EPAPER = "epaper";
  public static final String SOURCE_CHANNEL = "channel";
  public static final long RECENT_FOLLOWED_TIMESTAMP_IN_MILLIS = 5 * 60 * 1000;
  public static final String FE_PREFIX = "fe_";
  public static final String FF_PREFIX = "ff";

  //Constant used across dhtv and dailyhunt commons
  public static final String DHTV_FOLLOW_CHANNEL_GROUP_TYPE = "DHTV_CHANNEL";
  public static final String DHTV_HOME_LUNCH_ON_HOME_BACK = "dhtv_home_lunch_from_home_back";
  public static final String DHTV_CUR_DEEPLINKASSET_ITEM_ID = "dhtv_cur_deeplinkasset_item_id";

  public static final int ONBOARDING_DESIGN_VERSION_V1 = 1;
  public static final int ONBOARDING_DESIGN_VERSION_V2 = 2;
  public static final String ONBOARDING_SINGLE_SELECT = "single_select";
  public static final String ONBOARDING_MULTI_SELECT = "multi_select_menu";
  public static final String LANG_MULTISELECT = "LANG_MULTISELECT";
  public static final String LOCATION_SELECTION = "LOCATION_SELECTION";
  public static final String SETTINGS_LOCATION = "settings_location";

  public static final String INCLUDE_PUBLISHER_IN_SHARE_TEXT = "use_publisher_in_share_text";
  public static final String IS_LOCATION_SEARCH = "is_location_search";
  public static final String similarSourcesSeeAllDeeplink =
      "http://m.dailyhunt.in/follow/explore/sources";
  public static final String LOCATION_SEARCH_ITEM_TYPE = "location_filter";
  public static final String ITEM_TYPE_LOCATION_SEARCH = "locations_search_result";
  public static final int TERMS_SNACKBAR_SHOW_DELAY = 10000;
  public static final String MAX_VIDEO_HEIGHT_RATIO = "max_video_height_ratio";
  public static final String NON_LINEAR_FEED = "NonLinearFeed";
  public static final String RELATED_VIDEO_FEED = "RelatedVideoFeed";

  public static final long VIDEO_MIN_AD_RECENT_PLAYED_DURATION = 3 * 60000; // 3 Min delay
  // 5 Min video can always show ads.
  public static final long VIDEO_IGNORE_ITEM_DURATION = 60000 * 5L;
  public static final long VIDEO_MIN_NO_AD_VIDEO_LENGTH = 30000L; // Min 30sec length to allow ads.
  public static final long DEFAULT_WAIT_FOR_AD_DELAY = 3000L;

  public static final String RELATED_VIDEOS = "RELATED_VIDEOS";
  public static final String RELATED_NEWS = "RELATED_NEWS";
  public static final String ASSOCIATION = "ASSOCIATION";
  public static final String ADS = "ADS";

  public static final String MORE_STORIES_TYPE = "otherPerspectiveStories";
  public static final String MORE_STORIES_COLLECTION_NAME = "PerspectiveStories";
  public static final int DEFAULT_MIN_MORE_STORIES_COUNT = 4;
  public static final int DEFAULT_MAX_MORE_STORIES_COUNT = 5;
  public static final String PREF_MIN_MORE_STORIES_COUNT = "pref_min_more_stories_count";
  public static final String PREF_MAX_MORE_STORIES_COUNT = "pref_max_more_stories_count";

  public static final String SEARCH_CONTEXT_NEWSDETAIL = "screen:NewsDetail";
  public static final String SEARCH_CONTEXT_VIDEODETAIL = "screen:VideoDetail";
  public static final String SEARCH_TYPE_TAGS = "story_tags";
  public static final String SHOW_FOLLOW_BUTTON = "show_follow_button";
  public static final String POST_NEWS_PAGE_ENTITY_CHANGE_EVENTS =
      "POST_NEWS_PAGE_ENTITY_CHANGE_EVENTS";
  public static final String TYPE_FOLLOWED = "Followed";
  public static final String TYPE_UNFOLLOWED = "Unfollowed";
  public static final String FEATURE_MASK = "1879044085";
  public static final String FEATURE_MASK_V1 = "123";
  public static final float DEFAULT_IMG_AR = 1.7f;
  public static final String BUNDLE_SIGN_ON_UI_MODE = "bundleSignOnUiMode";
  public static final String BUNDLE_SIGN_IN_CUSTOM_HEADER = "bundleSignInCustomHeader";
  public static final String BUNDLE_SIGN_IN_TPV_NAME = "bundleSignInTpvName";
  public static final String BUNDLE_SIGN_IN_DELAY_PAGE_VIEW = "bundleSignInDelayPageView";
  public static final String BUNDLE_COUNT_SKIP_CLICK = "countSkipClicks";
  public static final String BUNDLE_SIGNIN_SUCCESS_PENDING_INTENT = "successPendingIntent";
  public static final String BUNDLE_SIGNIN_SKIP_PENDING_INTENT = "skipPendingIntent";
  public static final String BUNDLE_REFERRER_VIEW_IS_FVP = "bundle_referrer_view_is_fvp";
  public static final String LOGIN_TYPE = "login type";
  public static final String AUTO_LOGIN = "auto_login";
  public static final String FOLLOW_FILTER_TYPE_ALL = "ALL";
  public static final String BUNDLE_FOLLOW_SCREEN_STATE = "follow_screen_state";
  public static final String BUNDLE_LOGIN_RESULT_SUCCESSFUL = "bundle_login_result_successful";
  public static int REQ_CODE_TRUECALLER = 100;
  public static int REQ_CODE_GOOGLE = 122;
  public static int REQ_CODE_LOGIN_RESULT = 123;
  public static int REQ_CODE_EDIT_PROFILE_RESULT = 124;
  public static final int REQ_CODE_PROFILE = 12345;
  public static final int REQ_CODE_IMPORT_CONTACTS = 12349;
  public static final int REQ_CODE_GROUP = 14564;
  public static final int REQ_CODE_GRP_MEMBERS = 53672;
  public static String TRUECALLER_PACKAGE_NAME = "com.truecaller";
  public static final String RETRY_LOGIN = "retry login";
  public static final String DEEPLINK_URL = "deeplink_url";
  public static final String BROWSER_TYPE = "browserType";

  public static final String BUNDLE_UPDATED_PROFILE = "updated_profile";
  public static final String BUNDLE_ACTIVITY_TITLE = "activity_title";
  public static final String BUNDLE_IS_WEB_ITEM = "BUNDLE_IS_WEB_ITEM";
  public static final String INDIA_ISO_CODE_PROTOCOL_FORMAT = "91-";
  public static final String TRUECALLER_INDIA_ISO_CODE_FORMAT = "91";
  public static final String IS_INTERNAL_DEEPLINK = "isInternalDeeplink";
  public static final String BUNDLE_MY_PROFILE = "my_profile";
  public static final String SIGNED_IN = "signed_in";
  public static final String SIGNED_OUT = "signed_out";
  public static final String FPV = "FPV";
  public static final String TPV = "TPV";
  public static final String USER = "user";
  public static final String CREATOR = "creator";
  public static final String PROFILE_PUBLIC = "public";
  public static final String PROFILE_PRIVATE = "private";
  public static final String FOLLOWING = "following";
  public static final String FOLLOWER = "follower";
  public static final String SYSTEM = "system";
  public static final String PROFILE = "profile";

  public static final String AT_SYMBOL = "@";
  public static final String BLOCKED = "Blocked";
  public static final String UNBLOCKED = "Unblocked";
  public static final float IMAGE_ASPECT_RATIO_16_9 = 1.77f; //16:9

  public static final long DEFALUT_TIMER_PERIOD_INSECONDS = 60;
  public static final long MAX_ERROR_EVENT_PER_INTERVAL = 10;

  public static final String INTENT_ACTIONS_LAUNCH_CREATE_POST = "CreatePostOpen";

  public static final String BUNDLE_IN_DETAIL = "inDetail";
  public static final String BUNDLE_IN_DETAIL_COMMENT = "inDetailComment";
  public static final String BUNDLE_IS_FROM_HISTORY = "isFromHistory";
  public static final String BUNDLE_IS_FROM_NOTIFICATION = "isFromNotification";
  public static final String BUNDLE_HISTORY_SINCE_TIME = "historySinceTime";
  public static final String BUNDLE_GALLERY_EXTRA = "gallery_extras";
  public static final String BUNDLE_BOOTSTRAP_CARD = "bootstrap_card";
  public static final String BUNDLE_AD_EXTRA = "ad_extra";
  public static final String BUNDLE_AD_UI_WITH_BLUR = "ad_ui_blur_bg";

  public static final int CONTACT_SYNC_PAYLOAD_BUCKET_SIZE_DEFAULT = 2000;
  public static final long CONTACT_SYNC_FREQ_DEFAULT = TimeUnit.HOURS.toMillis(24);
  public static final long FOLLOW_NUDGE_FREQ_DEFAULT = TimeUnit.HOURS.toMillis(20);
  public static final String IS_IN_BOTTOM_SHEET = "is_bottom_sheet";
  public static final String IS_STORY_ITEM = "is_story_item";


  //EditProfile
  public static final String NAME = "name";
  public static final String MOBILE_NUMBER = "mobile_number";
  public static final String REQUEST_CODE = "request_code";
  public static final String LOGIN_PAYLOAD = "login_payload";
  public static final String BUNDLE_USER_ID = "bundle_userId";
  public static final String BUNDLE_FILTER = "bundle_filter";
  public static final String BUNDLE_FOLLOW_MODEL = "bundle_follow_model";
  public static final String QUERY_FILTER = "filter";
  public static final String QUERY_USERID = "userId";
  public static final String QUERY_TAB_TYPE = "tabType";
  public static final String QUERY_DEFAULT_TAB_ID = "defaultTabId";

  public static final String COOKIE_COMMON_DH = "commonDH";
  public static final String BUNDLE_IS_FPV = "bundle_isFpv";
  public static final String BUNDLE_USER_NAME = "bundle_user_name";

  //location
  public static final String SAVE_BUTTON = "save";
  public static final String SEE_ALL = "see_all";
  public static final String LOCATION_CLICK = "location_click";

  //Group
  public static final String CREATE_GROUP = "create_group";
  public static final String EDIT_PHOTO = "edit_photo";
  public static final String CREATE_GROUP_SHOWN = "create_group_shown";
  public static final String LOCAL_CARD_RETRY = "retry";
  public static final String EDIT_GROUP_SHOWN = "edit_group_shown";
  public static final String ANYONE_CAN_JOIN = "anyone_can_join";
  public static final String ANYONE_CAN_POST = "anyone_can_post";
  public static final String ANYONE_CAN_DISCOVER = "anyone_can_discover";
  public static final String LEAVE_GROUP_CLICK = "leave_group_click";
  public static final String DELETE_GROUP_CLICK = "delete_group_click";
  public static final String LEAVE_GROUP_CONFIRM = "leave_group_confirm";
  public static final String DELETE_GROUP_CONFIRM = "delete_group_confirm";
  public static final String INVITE = "invite";
  public static final String PHONEBOOK_CONTACTS = "phonebook_contacts";
  public static final String WHATSAPP = "whatsapp";
  public static final String FACEBOOK = "facebook";
  public static final String LINK = "link";
  public static final String MESSENGER = "messenger";
  public static final String BUNDLE_GROUP_INFO = "group_info";
  public static final String ANDROID = "android";
  public static final String DEFAULT = "default";

  /**
   * Aspect ratio of imageview for uiType VH_BIG
   */
  public static final float VH_BIG_IMAGE_ASPECT_RATIO = 1.0F / 1.20F; //WIDTH BY HEIGHT

  /**
   * Aspect ratio of imageview for uiType VH_SMALL
   */
  public static final float VH_SMALL_IMAGE_ASPECT_RATIO = 1.0f / 0.75f; //WIDTH BY HEIGHT

  /**
   * Aspect ratio of imageview for default cases
   */
  public static final float VH_DEFAULT_IMAGE_ASPECT_RATIO = 1.00F; //WIDTH BY HEIGHT

  public static final String LIST_TYPE_POSTS = "posts";

  public static final Long DEFAULT_FG_SESSION_TIMEOUT = 300L; // 5 mins

  public static final String BUNDLE_FOLLOWERS_COUNT = "bundle_followers_count";
  public static final String BUNDLE_SHOW_GUEST_FOOTER = "bundle_show_guest_footer";

  public static final String ABUSIVE = "ABUSIVE";
  public static final String SPAM = "SPAM";

  public static final String REQUEST_WITH_CACHE = "request_with_cache";

  public static final String YES = "Yes";
  public static final String NO = "No";
  public static final Long DEFAULT_BUTTON_CLICK_DISABLE_TIME_MS = 500L;
  public static final String IMPORT_FOLLOW_PAGE_ID = "Import_Follow";
  public static final String IMPORT_CONTACTS_DIRECT_LAUNCH = "import_contacts_direct_launch";
  public static final String CARDS_FRAG_DISABLE_PULL_TO_REFRESH = "disablePullToRefresh";

  public static final String ITEM_TYPE_QMC  = "question_multi_choice";
  public static final String NOTIFICATION_FILTER_TYPE_ALL = "ALL";

  public static final int TIMESPENT_PAUSE_DELAY = 1 * 60 * 1000;
  public static final String BUNDLE_IMPORT_CONTACTS_PENDING_INTENT = "postImportContactsPI";
  public static final String CONTACTS_RECOMENDATION_DEEPLINK = "http://m.dailyhunt.in/contactsrecommendation";
  public static final String BUNDLE_CONTACT_RECO_MODEL = "bundle_contact_reco_model";
  public static final String BUNDLE_IS_IMPORT_CONTACTS_FRAGMENT = "isImportContactsFragment";
  public static final String BUNDLE_AUTO_FOLLOW_FROM_NOTIFICATION = "auto_follow_from_notification";
  public static final String BUNDLE_AUTO_SHARE_FROM_NOTIFICATION = "auto_share_from_notification";
  public static final String BUNDLE_AUTO_JOIN_FROM_NOTIFICATION = "auto_join_from_notification";
  public static final String BUNDLE_AUTO_REPOST_FROM_NOTIFICATION = "auto_repost_from_notification";
  public static final String BUNDLE_NOTIFICATION_CTA_UI_TYPE = "bundle_notification_ui_type";
  public static final String BUNDLE_FLAG_SHOW_SKIP = "showSkipButton";
  public static final String BUNDLE_CID = "cid";
  public static final String SOURCE_ENTITY= "followed_entity";


  public static final String INVALID_POSTENTITY_ID = "INVALID_POSTENTITY_ID";

  public static final int DEFAULT_MIN_VIEW_VISIBLE_FOR_SCV = 30;
  public static final float DEFAULT_MIN_SCREEN_VISIBLE_FOR_SCV = 30f;
  public static final int DEFAULT_MIN_VIEW_VISIBLE_FOR_TS = 50;
  public static final float DEFAULT_MIN_SCREEN_VISIBLE_FOR_TS = 50f;
  public static final int MINIMUM_TIME_SPENT_FOR_PV = 1500;

  //Account Linking
  public static final String BUNDLE_LINKED_ACCOUNT_TYPES = "bundle_linked_account_types";
  public static final String BUNDLE_LINK_SPECIFIC_ACCOUNT = "bundle_link_specific_account";
  public static final String BUNDLE_ACCOUNT_LINKING_RESULT = "bundle_account_linking_result";
  public static final String BUNDLE_ENABLE_ONE_TOUCH_LOGIN = "bundle_enable_one_touch_login";
  public static final String BUNDLE_LINK_ACCOUNTS_POST_LOGIN = "bundle_link_accounts_post_login";

  public static final String REPORT_ADS_MENU_SELETED_L1_OPTION_ID = "report_ads_menu_seleted_l1_option_id";
  public static final String REPORTED_ADS_ENTITY = "reported_ads_entity";
  public static final String PARENT_UNIQUE_ADID_REPORTED_ADS_ENTITY = "parent_unique_adid_reported_ads_entity";
  public static final String REPORTED_ADS_POS = "reported_ads_pos";
  public static final int REPORTED_ADS_RESULT_CODE = 100;

  public static final String IS_LIVE = "is_live";

  public static final String USER_LOCATION_FETCH_TIME = "user_location_fetch_time";

  public static final String ACTION_ID = "ACTION_ID";
  public static final String DH_IMG = "DH_IMG";
  public static final String DH_IMG1 = "DH_IMG1";
  public static final String DH_IMG2 = "DH_IMG2";
  public static final String DH_IMG3 = "DH_IMG3";
  public static final String DH_M1 = "DH_M1";
  public static final String DH_M2 = "DH_M2";
  public static final String DH_M3 = "DH_M3";
  public static final String DH_DND = "DH_DND";
  public static final String DH_BDT = "DH_BDT";
  public static final String DH_PT = "DH_PT";
  public static final String DH_NT = "DH_NT";
  public static final String DESIGN_1 = "DESIGN_1";
  public static final String DESIGN_3 = "DESIGN_3";
    public static final String OGC = "OGC";

  public static final String CRASHLYTICS_KEY_PROCESS_NAME = "PROCESS_NAME";
  public static final String CRASHLYTICS_KEY_PROCESS_ID = "PROCESS_ID";

  public static final String SECTION_TV = "tv";
  public static final String SECTION_LOCAL = "local";
  public static final String IS_RELATED_VIDEO_REQUEST = "is_related_video_request";
  public static final String LOCAL_ZONE_PAGE_ID = "LOCAL_ZONE";
  public static final long DEFAULT_GAP_APP_UPDATE_PROMPTS = 172800L;
  public static final int DEFAULT_SPV_APP_UPDATE_PROMPT = 1;
  public static final int DEFAULT_MAX_PROMPT_PER_UPDATE = 3;
  public static final String JS_UPDATE_PH_NUMBER = "updatePhNumber";
  public static final long DEFAULT_LANG_SCREEN_WAIT = 2L;
  public static final String CARD_SIZE_SMALL = "SMALL";
  public static final long DEFAULT_FIRST_RETRY_DELAY = 0L;
  public static final int MAX_EXPONENTIAL_RETRIES = 10;
  public static final int MAX_GROUPED_NOTIFICATION = 100;
  public static final long PREF_FEED_CACHE_DELAY_DEFAULT = 2_000;/*millisec*/
  public static final long SPLASH_MIN_WAIT_TIME = 500L;
  public static final String LANG_SCREEN_TYPE = "lang_screen_type";
  public static final int WOKEN_UP_SERVICE_FG_NOTIFICATION_DURATION = 5000;
  public static final String SOURCE = "source";
  public static final String SOURCE_APP = "source.app";
  public static final Boolean SHOW_IN_APP_RATING_FLOW = false;
  //ads lp time spent
  public static final String TIME_SPENT_ON_LP_TIMER_STARTED  = "time_spent_on_lp_timer_started";
  public static final String ADS_TS_ON_LP_URL = "ads_ts_on_lp_url";
  public static final String ADS_TS_ON_LP_TIMESTAMP = "ads_ts_on_lp_timestamp" ;
  public static final long ONE_DAY_IN_MILLISECONDS = 86400000;

  //This is introduced - to override webviews user agent string for youtube embed player
  //As there is an issue with youtube embed full screen for android devices user agent
  public static final String USER_AGENT_STRING = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Mobile Safari/537.36";
  public static final int NOT_SHOW_ADJUNCT_LANG_DISPLAY = 2;
  public static final int SHOW_ADJUNCT_LANG_DISPLAY_TYPE_1 = 0;
  public static final int SHOW_ADJUNCT_LANG_DISPLAY_TYPE_2 = 1;

  public static final String ADJUNCT_LANG_FROM_TICK_CROSS = "adjunct_lang_from_tick_cross";
  public static final String ADJUNCT_LANG_TICK_CLICKED = "adjunct_lang_tick_clicked";
  public static final String ADJUNCT_LANG_FLOW="adjunct_lang_flow";
  public static final String ADJUNCT_LANGUAGE_STICKY_BANNER="adjunct_language_sticky_banner";
  public static final String ADJUNCT_LANGUAGE_HTML_BANNER="adjunct_language_html_banner";
  public static final String ADJUNCT_LANGUAGE="adjunct_language";
  public static final String ADJUNCT_LANGUAGE_SUBTYPE="STORY_IN_ADJUNCT_LANGUAGE";
  public static final String ADJUNCT_STORY_DETAIL_POPUP="adjunct_story_detail_popup";
  public static final String ADJUNCT_STICKY="adjunct_sticky";
  public static final String ADJUNCT_LANGUAGE_SNACKBAR_SETTINGS="adjunct_language_snackbar_settings";
  public static final int ADJUNCT_LANG_DEFAULT_NOTIFICATION_ID= 123456789;
  public static final String ALL_CHARACTER = "*";

  public static final String SECTION_ID="sectionId";
  public static final String SETTING_AUTOSCROLL_PATH = "settings#";

  public static final int NOTIFICATION_CLEAR_ALL_TIME_THRESHOLD = 500;
  public static final int NOTIFICATION_CLEAR_ALL_COUNT_THRESHOLD = 3;

  public static final int THEME_SNACKBAR_DETAIL = 30000;
  public static final int THEME_SNACKBAR_LIST = 10000;
  public static final String NOTIFICATION_ID = "notification_id";
  public static final String NOTIFICATION_DISPLAYED_TIMESTAMP = "notification_displayed_timestamp";
  public static int UNKNOWN = -1;
  public static final int ONLY_LIVE_TICKER = 1;
  public static final int GROUPED = 2;
  public static final int UNGROUPED = 3;
  public static int NO_NOTIFICATIONS = 4;

  public static final int DEFAULT_IMPLICIT_FOLLOW_MAX_CAP = 3;
  public static final int DEFAULT_IMPLICIT_FOLLOW_PV_COUNT = 4;
  public static final int DEFAULT_IMPLICIT_FOLLOW_SHARE_COUNT = 2;
  public static final int DEFAULT_IMPLICIT_BLOCK_COUNT = 2;
  public static final int ONE_DAY_IN_SECONDS = 86400;
  public static final int DEFAULT_IMPLICIT_ABSOLUTE_DAYS = 7;
  public static final int DEFAULT_IMPLICIT_ACTIVITY_COUNT = 3;
  final int DEFAULT_IMPLICIT_FOLLOW_ABSOLUTE_DAYS = 7;
  public static final int DEFAULT_IMPLICIT_FOLLOW_ACTIVITY_COUNT = 3;
  public static final int DEFAULT_IMPLICIT_BLOCK_ABSOLUTE_DAYS = 7;
  public static final int DEFAULT_IMPLICIT_BLOCK_ACTIVITY_COUNT = 3;
  public static final int FREQUENCY_OF_COACH_MARKS_IN_USERS_LIFE_TIME = 3;
  public static final int LEFT_SWIPE_NOT_RECORDED_IN_LAST_N_SPV = 24;
  public static final int MINIMUM_TIME_GAP_FOR_COACH_MARKS = 15;
  public static final long TIME_ELAPSED_FOR_COACH_MARKS = 24000;
  public static final int FEED_TO_DETAIL_IN_ONE_SESSION = 3;
  public static final String TEXT = "text";
  public static final String FREQUENCY_IN_LIFE_TIME = "frequencyInLifeTime";
  public static final String SWIP_NOT_RECORDED_IN_LAST_N_SPV = "leftSwipeNotRecordedInLastNspv";
  public static final String TIME_GAP_BETWEEN_COACH_MARKS = "minimumTimeGapForCoachMarks";
  public static final String TIME_ELAPSED_FOR_FIRST_COACH_MARK = "timeElapsedForFirstMark";
  public static final String MIN_FEED_TO_DETAIL_IN_ONE_SESSION = "feedToDetailInOneSession";
  public static final String NEWS = "news";
  public static final String SWIPE_COACH_MARKS = "swipe_coach_marks";
  public static final int NEWS_STICKY_DISABLED_DEFAULT_DAYS_VALUE = 7;

  public static final String AUTO = "auto";

  public static final int DEFAULT_IMPLICT_FOLLOW_SOFT_BLOCK_SIGNAL = 1;
  public static final int DEFAULT_COLD_FOLLOW_MAX_CAP = 3;
  public static final int DEFAULT_COLD_SIGNAL_FOLLOW_ABSOLUTE_DAYS = 7;
  public static final int DEFAULT_COLD_SIGNAL_CARD_POSITION = 10;
  public static final int DEFAULT_EXPLICIT_SIGNAL_MIN_CARD_POSITION = 5;
  public static final int DEFAULT_EXPLICIT_FOLLOW_MAX_CAP = 30;
  public static final int DEFAULT_EXPLICIT_BLOCK_MAX_CAP = 30;
  public static final int DEFAULT_EXPLICIT_COOLOFF_PERIOD_DAY = 1;
  public static final int  DEFAULT_COLD_SIGNAL_FOLLOW_MAX_CAP = 3;
  public static final int DEFAULT_IMPLICIT_BOTTOM_BAR_DURATION = 5;
  public static final int DEFAULT_INITIAL_SESSION_TO_SKIP = 1;
  public static final int DEFAULT_MIN_SESSION_GAP = 30;

  public static final long DEFAULT_COACH_TOOL_TIP_TIME = 5_000L;
  public static final String FOLLOW = "Follow";
  public static final String CAROUSAL_EXPLICIT_BLOCK = "block_explicit";
  public static final String BLOCK = "Block";
  public static final String UNDO = "UNDO";
  public static final String FOLLOW_RECOMMENED_URL = "/api/v2/recommended/user/follow?";
  public static final String BLOCK_RECOMMENED_URL = "/api/v2/recommended/user/block?";
  public static final String REQUEST_TYPE_POST = "POST";
  public static final String UNDO_LOWER_CASE = "undo";
  public static final String SECTION = "section";

  public static final int DEFAULT_MIN_APP_LAUNCHES_POST_UPGRADE = 3;

  public static final String SETTINGS_SCROLL_TO = "settings_scroll_to";
  public static final String HOME_FEED_TOP = "HOME_FEED_TOP";

  public static final String NOTIFICATION_SETTINGS_DEEPLINK_ACTION = "android.settings.APP_NOTIFICATION_SETTINGS";
  public static final String APP_PACKAGE = "app_package";
  public static final String APP_UID = "app_uid";
  public static final String EXTRA_APP_PACKAGE = "android.provider.extra.APP_PACKAGE";
  public static final String DEVICE_SETTINGS = "deviceSettings";

  public static final String POST_DEEPLINK_FOR_THEME="post_deeplink_for_theme";
  public static final long THEME_SCREEN_DISPLAY_DURATION = 3000L;



  public static final String SHARABLE_APP_PKG_NAME = "sharable_app_pkg_name";
  public static final String SHARABLE_APP_DIALOG_TITLE = "sharable_app_dialog_title";
  public static final String MAX_NUMBER_OF_TIMES_TO_SHOW_LINKEDIN_DIALOG = "maxNumberOfTimesToShowLinkedInDialog";
  public static final String MIN_DAYS_TO_SHOW_LINKEDIN_DIALOG_UPGRADED_USERS = "minDaysToShowLinkedInDialogForUpgradedUsers";
  public static final String MIN_DAYS_TO_SHOW_LINKEDIN_DIALOG_NEW_USERS = "minDaysToShowLinkedInDialogForNewUsers";
  public static final String MIN_LAUNCHES_TO_SHOW_LINKEDIN_DIALOG_UPGRADED_USERS = "minLaunchesToShowLinkedInDialogForUpgradedUsers";
  public static final String MIN_DAYS_USER_TO_WAIT_AFTER_LAST_SEEN = "minNumberOfDaysUserToWaitAfterLastSeen";
  public static final String SHOW_LINKEDIN_SHARE_DIALOG = "showLinkedInShareDialog";
  public static final String DEFAULT_SHARE_APP_PACKAGE_NAME = "appPackageName";
  public static final String LINKEDIN_DIALOG_TITLE = "dialogTitle";

  public static final long HOME_LOADER_MIN_WAIT_TIME_MS = 2000L;
  public static final long HOME_LOADER_MAX_WAIT_TIME_MS = 5000L;
  public static final long IN_APP_DISPLAY_DURATION = 5000L;

  public static final int RATE_SCREEN_NO_UPGRADE = 0;
  public static final int RATE_SCREEN_UPGRADE_NOT_SHOWN = 1;
  public static final int RATE_SCREEN_UPGRADE_SHOWN = 2;

  public static final String MODIFY_DEFAULT_SHARING_APP="Modify_Default_Sharing_App";
  public static final String CROSS_DELETE = "cross_delete";

  public static final String IN_APP="in_app";
  public static final String COMMUNICATION_API_PRIORITIZED="communication_api_prioritized";
  public static final String OTHER_IN_APP_PRIORITIZED="other_in_app_prioritized";
  public static final String NO_USER_SESSION="no_user_session";
  public static final String MINI_VIDEO_PLAYER_PRESENT="mini_video_player_present";

  //Delay used for delaying work manager init and scheduling of jobs from app startup
  public static final long WORK_MANAGER_INIT_AND_SCHEDULING_DELAY_MS = 5000l;
  public static final int ZERO = 0;
  public static final long FADE_TIME_OUT = 2000L;

  public static final String SEEN = "SEEN";
  public static final String NOT_SEEN = "NOT_SEEN";
  public static final String EXPIRED = "EXPIRED";
  public static final String GOOGLE_DEFERRED_DEEPLINK_PERF = "google.analytics.deferred.deeplink.prefs";
  public static final String GOOGLE_DEFERRED_DEEPLINK_PERF_KEY = "deeplink";


  public static final int COLLECTION_SECOND_ITEM_VISIBLE_PERCENTAGE = 20;
  public static final boolean SHOW_SOURCE_LOGO_AT_CARD_LEVEL = false;

  public static String NER_DESCRIPTION_URL = "ner_desc_url";
  public static final String PREF_DETAIL_CSS_FRIST_AKSHAR = "PREF_DETAIL_FRIST_AKSHAR_CSS";

}
