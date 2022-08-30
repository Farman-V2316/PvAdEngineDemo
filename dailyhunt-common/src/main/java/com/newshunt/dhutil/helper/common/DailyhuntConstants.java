/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.common;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * Created by anshul on 05/04/17.
 * Constants for Dailyhunt app.
 */

public class DailyhuntConstants {

  public static final String URL_HTTP_FORMAT = "http://";
  public static final String URL_HTTPS_FORMAT = "https://";
  public static final int VIEW_ORDER = 1001;

  //Astro Related Constants
  public static final String BUNDLE_NEWS_HOME_CURRENT_INDEX = "newsHomeCurrentIndex";
  public static final String ASTRO_ACTIVITY_EVENT_ACTION_TYPE = "deepLink";
  public static final String ASTRO_TOPIC_ID = "id";
  public static final String ASTRO_VIEW_ORDER = "viewOrder";
  public static final int ASTRO_NUM_LAUNCHES_SECOND_TIME = 10;
  public static final String ASTRO_SUPPORTED_LANGUAGES = "supportedLanguages";
  public static final String ASTRO_LANGUAGES_PRIORITY = "astroLanguagesPriority";
  public static final String URL_STR = "url";
  public static final String USE_WIDE_VIEW_PORT = "useWideViewPort";
  public static final String ENCODING_UTF8 = "UTF-8";
  public static final String astroDateFormat = "yyyyMMdd";
  public static final String CLICK_SUBSCRIBE_BUTTON = "clickSubscribeButton";
  public static final String CLICK_CROSS_BUTTON = "clickCrossButton";
  public static final String CLICK_EDIT_BUTTON = "clickEditButton";
  public static final String CLICK_DATE_PICKER = "clickDatePicker";
  public static final String NH_BROWSER_ACTION = "nhBrowserOpen";
  public static final String BUNDLE_WEB_NAV_MODEL = "webModel";
  public static final String BUNDLE_SSO_NAV_MODEL = "ssoModel";
  public static final String PERMISSION_NAV_MODEL = "permissionModel";
  public static final String DEEPLINK_COMMENT_QUERY_PARAMS = "comment";
  public static final String DEEPLINK_ALL_COMMENTS_QUERY_PARAMS = "allComments";
  public static final String JS_CALLBACK_SET_GENDER = "dhSetGender";
  public static final String JS_CALLBACK_SET_DOB = "dhSetDateOfBirth";
  public static final String JS_CALLBACK_SET_USER_SUBSCRIBED_FAILED = "dhSetUserSubscribedFailed";
  public static final String JS_CALLBACK_SET_SUBSCRIBE_BUTTON_ENABLED =
      "dhSetSusbcribeButtonEnabled";
  public static final String DATE_PICKER = "datePicker";
  public static final int DEFAULT_ASTRO_DAY = 1;
  public static final int DEFAULT_ASTRO_MONTH = 0;
  public static final int DEFAULT_ASTRO_YEAR = 1990;
  public static final String ASTRO_PROMPT = "astro_prompt";
  public static final String ASTRO_FORM = "astro_form";
  public static final String STICKY_NOTIFICATION_REFRESH_ACTION =
      CommonUtils.getApplication().getPackageName() + ".sticky_notification_refresh";

  public static final String STICKY_NOTIFICATION_PLAY_ACTION =
      CommonUtils.getApplication().getPackageName() + ".sticky_notification_play";
  public static final String STICKY_NOTIFICATION_START_ACTION = CommonUtils.getApplication()
      .getPackageName() + ".sticky_notification_start";
  public static final String NOTIFICATION_SERVICE_START_ACTION =
      AppConfig.getInstance().getPackageName() + ".notification_service_start";

  public static final String PREFETCH_NOTIFICATION_ALARM_RECEIVER =
      AppConfig.getInstance().getPackageName() + ".prefetch_notification_alarm_receiver";

  public static final String PREFETCH_NOTIFICATION_SERVICE_START_ACTION =
      AppConfig.getInstance().getPackageName() + ".prefetch_notification_service_start";
  public static final int PREFECTCH_NOTIFICATION_ID = 200;
  public static final String PREFETCH_NOTIFICATION_SERVICE_STOP_ACTION =
      AppConfig.getInstance().getPackageName() + ".prefetch_notification_service_stop";
  public static final String STICKY_NOTIFICATION_CLICK_ACTION = CommonUtils.getApplication()
      .getPackageName() + ".sticky_notification_click";
  public static final String STICKY_NOTIFICATION_CLOSE_ACTION = CommonUtils.getApplication()
      .getPackageName() + ".sticky_notification_close";
  public static final String STICKY_NOTIFICATION_DISMISS_AND_SHOW_ACTION = CommonUtils.getApplication()
      .getPackageName() + ".sticky_notification_dismiss_and_show";
  public static final String CRICKET_SETTINGS_ACTIVITY_OPEN = CommonUtils.getApplication()
      .getPackageName() + ".cricket_settings_activity";
  public static final String STREAM_VERSION = "v";
  public static final String TRACK_COUNT = "count";
  public static final String FINISHED = "finished";
  public static final String SCHEDULED_LATER = "scheduled_later";
  public static final String JOB_PREFIX = "Noti_Remove_From_Tray_";
  public static final String CLEAR_HISTORY_ON_PAGE_LOAD = "clearHistoryOnPageLoad";
  public static final String CRASHLYTICS_KEY_NEW_AD_PROCESSING = "PROCESSING_AD";
  public static final String CRASHLYTICS_KEY_NEW_AD_RECEIVED = "RECEIVED_AD";

  // TODO : karthik.r 9.2.x Remove below two keys once issue fixed
  public static final String CRASHLYTICS_KEY_SHARE_VALUE = "SHARE_VALUE";
  public static final String CRASHLYTICS_KEY_NOTF_FG_CONFIG = "NOTIF_FG_CONFIG";
  public static final String CRASHLYTICS_KEY_LIKE_VALUE = "LIKE_VALUE";
  public static final String AUTO_PLAY_SETTINGS_ACTION =
      AppConfig.getInstance().getPackageName() + ".auto_play_settings";

  // constants for similar stories
  public static final String SIMILAR_STORIES = "similarStories";
  public static final String SIMILAR_STORY_MAIN = "MAIN";

  public static final String VIRAL_REFERRER_ID = "relatedViral";

  // Privacy Policy related constants
  public static final String PRIVACY_TITLE = "privacyTitle";
  public static final String PRIVACY_DESC = "privacyDesc";
  public static final String PRIVACY_POSITIVE_BTN = "privacyPositiveBtn";
  public static final String PRIVACY_NEGATIVE_BTN = "privacyNegativeBtn";
  public static final String PRIVACY_CAN_USER_IGNORE = "privacy_can_user_ignore";
  public static final String HOST_ID = "host_id";

  // Android M Permission related constants
  public static final String PERMISSION_REPEAT_COUNT = "gapCount";
  public static final String PERMISSION_TITLE = "permTitle";
  public static final String PERMISSION_DESC = "permDesc";
  public static final String OPEN_SETTINGS = "openSettings";
  public static final String SETTINGS_ACTION = "settingsAction";
  public static final String LOCATION_PERM_SUBTITLE = "locationPermSubtitle";
  public static final String STORAGE_PERM_SUBTITLE = "storagePermSubtitle";
  public static final String LOCATION_PERM_DESC = "locationPermDesc";
  public static final String STORAGE_PERM_DESC = "storagePermDesc";
  public static final String PERM_POSITIVE_BTN = "permissionPositiveBtn";
  public static final String PERM_NEGATIVE_BTN = "permissionNegativeBtn";

  //Player Constants to save in preference
  public static final String KEY_PLAYER_DIMENSION_JSON = "key_player_dimension_json";
  public static final String KEY_WEB_PLAYER_LIST_JSON = "key_web_player_list_json";

  public static final String FOLLOW_COACH_REPEAT_COUNT = "gapCount";
  public static final int FOLLOW_COACH_REPEAT_COUNT_DEFAULT = 5;
  public static final String COMM_FOLLOW_COACH_NEW_USER = "newUserMessage";
  public static final String COMM_FOLLOW_COACH_EXISTING_USER = "existingUserMessage";
  public static final String COMM_FOLLOW_NEW_USER_MESSAGE_TIMEOUT = "followNewUserMessageTimeout";
  public static final String COMM_FOLLOW_COACH_MAX_COUNT = "followCoachMaxCount";
  public static final int COMM_FOLLOW_COACH_MAX_COUNT_DEFAULT = 5;
  public static final String COMM_FOLLOW_COACH_SHOW_GENERIC = "showGenericMessage";

  public static final String TV_HOME_TAB_INDEX = "HOME_TAB_INDEX";
  public static final String BUNDLE_LIVETV_SHARED_ITEM_ID = "LIVETV_SHARED_ITEM_ID";
  public static final String BUNDLE_SHOW_KEYBOARD = "showKeyboard";
  public static final String TITLE = "title";
  public static final String MESSAGE = "message";
  public static final String POSITIVE = "positive";
  public static final String NEGATIVE = "negative";
  public static final String POSITIVE_TEXT = "positive_text";
  public static final String NEGATIVE_TEXT = "negative_text";
  public static final String EDIT_PROFILE_TOOLTIP_TITLE = "editProfileTooltipTitle";
  public static final String EDIT_PROFILE_TOOLTIP_MESSAGE = "editProfileTooltipMessage";
  public static final String PROFILE_COACHMARK_POSITIVE_TEXT="profileCoachmarkPositiveText";
  public static final String PROFILE_COACHMARK_NEGATIVE_TEXT="profileCoachmarkNegativeText";
  public static final String DISPLAY_TIME="displytime";
  public static final String MENU_PAYLOAD = "menu_payload";
  public static final String TEXT_PROFILE = "text_profile";
  public static final String TEXT_REPOST = "text_repost";
  public static final String TEXT_GROUPS = "text_groups";
  public static final String TEXT_TOPIC = "text_topic";
  public static final String TEXT_SHARE = "text_share";

}
