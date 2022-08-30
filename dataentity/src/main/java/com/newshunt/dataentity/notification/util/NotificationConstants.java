/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.notification.util;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.concurrent.TimeUnit;

/**
 * Notification and GCM related constants
 *
 * @author santosh.kulkarni
 */
public final class NotificationConstants {
  public static final String MESSAGE = "message";
  public static final String VERSION = "version";
  public static final String TYPE = "type";
  public static final String MESSAGE_V2 = "message_v2";
  public static final String MESSAGE_V3 = "message_v3";
  public static final String MESSAGE_V4 = "message_v4";
  public static final String MESSAGE_V5 = "message_v5";
  public static final String MESSAGE_V6 = "message_v6";
  public static final String IMAGELINK = "imageLink";
  public static final String IMAGELINK_V2 = "imageLinkV2";
  public static final String ENCODING = "UTF-8";
  public static final String INBOXIMAGELINK = "inboxImageLink";
  public static final String LANGUAGECODE = "langCode";
  public static final String DISPLAYTIME = "v4DisplayTime";
  public static final String EXPIRYTIME = "expiryTime";
  public static final String ISINTERNETREQUIRED = "v4IsInternetRequired";
  //TODO anshul.jain Check if we can use an existing variable.
  public static final String NOTIFICATION_DATA = "notificationData";
  public static final String BASE_MODEL_LIST = "baseModelList";
  public static final String NOTIFICATION_DATA_FIELD = "data";
  public static final String DIRECTION_UP = "UP";
  public static final String DIRECTION_DOWN = "DOWN";
  public static final int DEFAULT_PAGE_SIZE = 10;

  //big notification AnalyticsConstants
  public static final String BIGIMAGELINK = "bigImageLink";
  public static final String BIGIMAGELINK_V2 = "bigImageLinkV2";
  public static final String BIGTEXT = "bigText";
  public static final String PRIORITY = "priority";
  public static final int DEFAULT_PRIORITY = 0;

  public static final String EMPTY_STRING = "";
  // Action defined in Main App to receive  notification
  public static final String BROADCAST_ACTION_NOTIFICATION_RECEIVER =
      "com.newshunt.app.receivers.NotificationReceiver.NOTIFICATION_RECEIVER";
  // SENDER_ID - specific to NewsHunt app
  public static final String SENDER_ID = "881655734426";
  public static  final String SCOPE_FCM = "FCM";


  // These are the notification type constants , part of notification message
  public static final String APP_IDENTIFIER = "NH#^$";
  public static final String PARAM_DELIMITER = "~";
  public static final String NOTIFICATION_ID_PREFIX = "notId=not-";
  public static final String NOTIFICATION_ID_EXPIRY_PREFIX = "expiryTime";
  public static final String NOTIFICATION_ID_BACKURL_PREFIX = "v4BackUrl";
  public static final String NOTIFICATION_ID_SWIPEURL_PREFIX = "v4SwipeRelUrl";
  public static final String NOTIFICATION_ID_SWIPE_LOGIC_PREFIX = "v4SwipePageLogic";
  public static final String NOTIFICATION_ID_SWIPE_LOGIC_ID_PREFIX = "v4SwipePageLogicId";
  public static final String NOTIFICATION_MESSAGE_ID = "com.newshunt.notification.id";
  public static final String NOTIFICATION_INBOX = "NotificationInbox";
  public static final String NOTIFICATION_TESTPREP_MESSAGE_ID =
      "com.newshunt.notification.testprep.id";

  // TestPrep Notification Constants
  public static final String NOTIFICATION_VERSION_V2 = "v2";
  public static final String NOTIFICATION_VERSION_V3 = "v3";
  public static final String NOTIFICATION_VERSION_V4 = "v4";
  public static final String NOTIFICATION_VERSION_V5 = "v5";
  public static final String NOTIFICATION_VERSION_V6 = "v6";
  public static final String NOTIFICATION_TYPE_STICKY = "sticky_notification";
  public static final String NOTIFICATION_TYPE_ADJUNCT_STICKY = "adjunct_sticky";
  public static final String NOTIFICATION_TYPE_ACTION = "action";
  public static final String NOTIFICATION_TYPE_TESTPREP = "100";
  public static final String NOTIFICATION_TYPE_NEWS = "101";
  public static final String NOTIFICATION_TYPE_BOOKS = "102";
  public static final String NOTIFICATION_TYPE_TV = "500";
  public static final String NOTIFICATION_TYPE_LIVETV = "501";
  public static final String NOTIFICATION_TYPE_TRIGGER_PULL = "trigger_pull";
  public static final String NOTIFICATION_TYPE_SOCIAL = "social";
  public static final String NOTIFICATION_TYPE_FLUSH = "delete_notification";
  public static final String NEWS_STICKY_ID = "news_sticky_id";
  public static final String NOTIFICATION_TYPE_VERSION_UPDATE = "version_api_update";
  public static final String NOTIFICATION_TYPE_VERSION_TRIGGER = "version_api_update_trigger";
  public static final String NOTIFICATION_TYPE_IN_APP = "in_app_notification";

  public static final String NOTIFICATION_TYPE_ADJUNCT_LANG_PUSH = "add_adjunct_notification";
  public static final String NOTIFICATION_TYPE_ADJUNCT_USER_TO_SYS_PUSH = "user_to_sys_lang_notification";
  public static final String NOTIFICATION_TYPE_FLUSH_BLACKLIST_LANGUAGE = "flush_blacklisted_notification";

  public static final String TESTPREP_NOTIFICATION_MESSAGE = "message";

  public static final String NOTIFICATION_SECTION_BOOKS = "BOOKS";
  public static final String NOTIFICATION_SECTION_NEWS = "NEWS";
  public static final String NOTIFICATION_SECTION_TESTPREP = "TESTPREP";
  public static final String NOTIFICATION_SECTION_NEWS_DEFAULT_ID = "headlines";
  public static final String NOTIFICATION_SECTION_BOOKS_DEFAULT_ID = "bookshome";
  public static final String NOTIFICATION_SECTION_BUZZ_DEFAULT_ID = "buzzhome";
  public static final String NOTIFICATION_SECTION_LIVETV_DEFAULT_ID = "livetvome";
  public static final String NOTIFICATION_SECTION_ADS_DEFAULT_ID = "ads";
  public static final String NOTIFICATION_SECTION_NHBROWSER_DEFAULT_ID = "nhBrowser";
  public static final String NOTIFICATION_SECTION_DEALS_WALL = "deals_wall";
  public static final String NOTIFICATION_SECTION_SOCIAL_COMMENTS_DEFAULT_ID = "social_comments";
  public static final String NOTIFICATION_SECTION_EXPLORE_DEFAULT_ID = "explore";
  public static final String NOTIFICATION_SECTION_FOLLOW_DEFAULT_ID = "follow";
  public static final String NOTIFICATION_SECTION_PROFILE_DEFAULT_ID = "profile";
  public static final String NOTIFICATION_SECTION_GROUP_DEFAULT_ID = "group";
  public static final String NOTIFICATION_SECTION_GROUP_VIEW = "group_view";
  public static final String NOTIFICATION_SECTION_GROUP_CREATE = "group_create";
  public static final String NOTIFICATION_SECTION_GROUP_APPROVAL = "group_approval";
  public static final String IN_APP_NOTIFICATION_DEFAULT_ID = "in_app_silent_notification";

  public static final int NOTIFICATION_STATUS_READ = 0;
  public static final int NOTIFICATION_STATUS_UNREAD = 1;
  public static final int NOTIFICATION_STATUS_SKIPPED_BY_USER = 2;
  public static final int POST_UPLOAD_STATUS_FAILED = 0;
  public static final int POST_UPLOAD_STATUS_SUCCESS = 1;
  public static final int POST_UPLOAD_PROGRESS = 2;

  public static final String LAUNCHED_FROM_NOTIFICATION = "notification_launch";
  public static final int GROUP_NOTIFICATION_ID = -1;
  public static final String NOTIFICATION_TYPE_GROUP = "group_notification";
  public static final int NEWS_STICKY_NOTIF_ID = 12909090;

  public static final int NOTIFICATION_TITLE_START_OFFSET = 85;
  public static final int NOTIFICATION_TITLE_MAX_WIDTH = 420;
  public static final int NOTIFICATION_BIG_TEXT_MAX_LINES = 6;
  public static final int NOTIFICATION_TITLE_MAX_LINES = 2;
  public static final int NOTIFICATION_TITLE_FONT_SIZE = 16;
  public static final int NOTIFICATION_BIG_TEXT_FONT_SIZE = 14;
  public static final int NOTIFICATION_GROUPED_TEXT_SIZE = 14;
  public static final String NOTIFICATION_CRICKET_MATCH_ID = "matchNotificationId";

  //Notification tray ids
  public static final int NOTIFICATION_TRAY_ID_TO_OPEN_SPLASH = "DHApp".hashCode();
  public static final int NOTIFICATION_TRAY_ID_TO_OPEN_INBOX = "DHNotificationInbox".hashCode();

  public static final String NOTIFICATION_BUNDLE_STICKY_MODEL = "stickyModel";
  public static final String STICKY_CRICKET_TYPE = "cricket";
  public static final String STICKY_GENERIC_TYPE = "generic";
  public static final String STICKY_NEWS_TYPE = "news";
  public static final String STICKY_NONE_TYPE = "none";
  public static final String REMOVED_FROM_TRAY_VALUE = "1";
  public static final String NOTI_SHOWN_AS_HEADS_UP = "1";
  public static final String NOTI_REMOVED_FROM_TRAY_BY_APP = "1";
  public static final String BUNDLE_NOTIFICATION = "bundleNotification";


  public static final String NOTIFICATION_DEFAULT_GROUP_NAME = "Default";
  public static final String NOTIFICATION_DEFAULT_CHANNEL_ID = "Default";
  public static final String PREFETCH_NOTIFICATION_DEFAULT_CHANNEL_ID = "Updates Default";
  public static final String UPDATES_DEFAULT_CHANNEL_ID = "Updates Default";
  public static final String NOTIFICATION_DEFAULT_CHANNEL_NAME = "Default";

  public static final String NOTIFICATION_DONOT_AUTO_FETCH_SWIPEURL = "doNotAutoFetchSwipeUrl";
  public static final String INTENT_STICKY_NOTIFICATION_CANCEL_ONGOING = CommonUtils.getApplication()
      .getPackageName() + ".cancelOngoingStickyNotification";
  public static final String INTENT_ACTION_STICKY_NOTIFICATION_FINISH = CommonUtils.getApplication()
      .getPackageName() + ".actionStickyNotificationFinish";
  public static final String INTENT_ACTION_PLAY_STICKY_AUDIO =
      CommonUtils.getApplication().getPackageName() + ".playStickyAudio";
  public static final String INTENT_ACTION_STOP_STICKY_AUDIO =
      CommonUtils.getApplication().getPackageName() + ".stopStickyAudio";
  public static final String INTENT_ACTION_GO_TO_NEXT_ITEM =
      CommonUtils.getApplication().getPackageName() + ".goToNextItem";
  public static final String INTENT_ACTION_GO_TO_PREV_ITEM =
      CommonUtils.getApplication().getPackageName() + ".goToPrevItem";
  public static final String INTENT_ACTION_NEWS_STICKY_ITEM_CLICK =
      CommonUtils.getApplication().getPackageName() + ".newsStickyItemClick";
  public static final String INTENT_ACTION_SHOW_STICKY_ITEMS_AS_NORMAL_NOTIS =
      CommonUtils.getApplication().getPackageName() + ".showsStickyItemsAsNormalNotis";
  public static final String INTENT_ACTION_UPDATE_NOTIFICATION_TRAY =
      CommonUtils.getApplication().getPackageName() + ".updateNotificationTray";
  public static final String INTENT_ACTION_NEWS_STICKY_GO_TO_SETTINGS =
      CommonUtils.getApplication().getPackageName() + ".goToNotificationSettings";
  public static final String INTENT_ACTION_NOTIFICATION_CLEAR_ALL = CommonUtils.getApplication().getPackageName() + ".notificationClearAll";
  public static final String INTENT_EXTRA_NEWS_STICKY_ITEM_INDEX = "item_index";
  public static final String INTENT_EXTRA_NEWS_STICKY_ANALYTICS_ITEM_INDEX = "analytics_item_index";
  public static final String INTENT_EXTRA_STICKY_ID = "StickyId";
  public static final String INTENT_EXTRA_STICKY_TYPE = "StickyType";
  public static final String INTENT_EXTRA_FROM_NEWS_STICKY = "isFromNewsStickyNotification";
  public static final String INTENT_EXTRA_FROM_INBOX = "isFromNotificationInbox";
  public static final String INTENT_EXTRA_ITEM_ID = "item_id";
  public static final String INTENT_EXTRA_NOT_ID = "notification_id";
  public static final String INTENT_STICKY_NOTIFICATION_TRAY_ID = "stickyNotifTrayId";
  public static final String INTENT_STICKY_NOTIFICATION_OPT_OUT_DEEPLINK = "optOutDeeplink";
  public static final String SNACK_BAR_META = "snackMeta";

  public static final String INTENT_ACTION_STICKY_EXPIRY_TIME_CHANGED =
          "actionStickyNotificationExpiryTimeChanged";

  public static final String INTENT_EXTRA_STICKY_EXPIRY_TIME= "expiryTime";

  public static final String INTENT_ACTION_REMOVE_FROM_TRAY_JOB_DONE =
      CommonUtils.getApplication().getPackageName() + ".removeFromTrayJobDone";

  public static final String INTENT_ACTION_STICKY_SERVICE_STARTED =
      CommonUtils.getApplication().getPackageName() + ".stickyStarted";

  public static final String INTENT_ACTION_NOTIFICATION_CTA_FOLLOW =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_follow";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_COMMENT =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_comment";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_REPLY =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_reply";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_SHARE =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_share";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_JOIN =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_join";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_REPOST =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_repost";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_SHARE_SMALL =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_share_small";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_FOLLOW_SMALL =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_follow_small";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_COMMENT_SMALL =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_comment_small";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_REPLY_SMALL =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_reply_small";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_JOIN_SMALL =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_join_small";
  public static final String INTENT_ACTION_NOTIFICATION_CTA_REPOST_SMALL =
          CommonUtils.getApplication().getPackageName() + ".notification_cta_repost_small";

  public static final String STICKY_NOTIFICATION_REMOVE_FROM_TRAY_JOB_KEY = "removeStickyFromTray";
  public static final String INTENT_EXTRA_STICKY_NOTIFICATION_CANCEL_TRIGGER =
      "stickyNotifCancelTrigger";
  public static final String INTENT_ACTION_STICKY_NOTIFICATION_RESCHEDULE =
      CommonUtils.getApplication().getPackageName() + ".rescheduleStickyNotification";
  public static final String INTENT_EXTRA_STICKY_RESCHEDULE_TIME = "stickyRescheduleTime";
  public static final String INTENT_EXTRA_STICKY_AUDIO_STATE = "stickyAudioState";

  public static final String NOTIFICATION_TEMP_CHANNEL_NAME = "Other";
  public static final String NOTIFICATION_TEMP_GROUP_NAME = "Recents";

  public static final String MANUFACTURER_XIAOMI = "Xiaomi";

  public static final String CREATE_POST_NOTIFICATION_CHANNEL_IMAGE = "Image Post";
  public static final String CREATE_POST_NOTIFICATION_CHANNEL_TEXT = "Text Post";
  public static final String CREATE_POST_NOTIFICATION_CHANNEL_ADJUNCT_LANG = "Adjunct Lang Noti";
  public static final String ADJUNCT_LANG_ACTION = "adjunct_lang_action";
  public static final String ADJUNCT_NOTI_ID = "adjunct_noti_id";
  public static final String ADJUNCT_CTA_DEEPLINK_URL = "adjunct_cta_deeplink_url";
  public static final String ADJUNCT_NOTI_ACTION_TICK = "adjunct_noti_action_tick";
  public static final String CREATE_POST_NOTIFICATION_ID = "createPostNotificationId";
  public static final String CREATE_POST_ID = "createPostId";
  public static final String CREATE_POST_ACTION_RETRY = "com.eterno.CREATE_POST_ACTION_RETRY";
  public static final long NOTIFICATION_EXPIRY_TIME = TimeUnit.DAYS.toMillis(7);
  public static final String NEWS_STICKY_OPTIN_ID = "news_sticky_optin_id";
  public static final String INTENT_ACTION_NOTIFICATION_RECEIVED = "notification_added_to_tray";
}
