/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.analytics.referrer;

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;

import static com.newshunt.dataentity.analytics.referrer.NHGenericReferrerSource.*;

/**
 * Generic Referrer's logic to be used across the Applications
 *
 * @author ranjith.suda
 */
public enum NhGenericReferrer implements NhAnalyticsReferrer {

  //Notification tray
  NOTIFICATION("NOTIFICATION", NOTIFICATION_TRAY),

  //Notification inbox
  NOTIFICATION_INBOX("NOTIFICATION_INBOX", NOTIFICATION_INBOX_VIEW),

  //Deeplink screen
  ORGANIC_SOCIAL("ORGANIC_SOCIAL", DEEPLINK),
  DEEP_LINK("DEEP_LINK", DEEPLINK),
  APP_INDEXING("APP_INDEXING", DEEPLINK),
  FIREBASE("FIREBASE", DEEPLINK),
  APPSFLYER("APPSFLYER", DEEPLINK),

  //Splash screen
  SPLASH("SPLASH", SPLASH_VIEW),

  ORGANIC("ORGANIC"),

  //Web home
  WEB_HOME("WEB_HOME", WEB_SECTION_VIEW),

  //Web Item
  WEB_ITEM("WEB_ITEM"),

  //Menu Screen
  MENU("MENU"),

  // for news details
  CARD_WIDGET("CARD_WIDGET", NEWS),

  VIRAL_DETAIL("VIRAL_DETAIL", NEWS),

  STORY_DETAIL("STORY_DETAIL", NEWS),
  STORY_CARD("STORY_CARD", NEWS),

  SETTINGS("SETTINGS"),
  APPBAR("APPBAR"),

  // undefined
  NULL("null"),
  ENTITY_LIST("entity_list"),
  FEED("feed"),
  FEED_ERROR_LIST("feed_error_list"),
  FEED_FOLLOWED_CAROUSEL("feed_followed_carousel"),
  FEED_EXPLORE("feed_explore"),
  FOLLOW_SNACKBAR("follow_snackbar"),
  HAMBURGER_MENU("hamburger_menu"),
  COACHMARK("coachmark"),

  //For Loco
  LOCO("LOCO"),
  PROFILE("PROFILE"),
  SIGNIN_VIEW("SIGNIN_VIEW"),
  LOCAL_CARD("local_card"),
  LOCATION_SELECTION_CARD("location_selection_card"),

  // For create post
  CREATE_POST_HOME("CREATE_POST_HOME", CREATE_POST_VIEW),

  CP_SOCIAL_LINK_SHARE("CP_SOCIAL_LINK_SHARE", CREATE_POST_VIEW),

  DIALOGBOX("dialogbox"),
  //Groups
  GROUP_HOME("group_home"),
  GROUP_FEED("group_feed"),
  GROUP_SETTINGS("group_settings"),
  MEMBER_LIST("member_list"),
  INVITE_SCREEN("invite_screen"),
  GROUP("group"),
  WALKTHROUGH("walkthrough"),
  APPROVALS("approvals"),
  LAUNCH_SIGN_IN("launch_signin"),
  IMPORT_CONTACT("import_contact"),
  PROFILE_FPV("profile-fpv"),
  EDIT_PROFILE("edit_profile"),
  BLOCKED_SOURCES_NO_FEED_ITEMS_ERROR("error_screen"),
  ONBOARDING("onboarding"),
  NEWS_STICKY_CROSS("cross_delete_news_sticky"),
  DISABLE_SNACKBAR("disable_snackbar"),
  TYPE_OPEN_NEWSITEM_ADJUNCT_STICKY("type_open_newsitem_adjunct_sticky"),
  THEME_CHANGE("theme_change");



  private String referrerName;

  private NHReferrerSource referrerSource;

  NhGenericReferrer(String referrerName) {
    this(referrerName, null);
  }

  NhGenericReferrer(String referrerName, NHReferrerSource referrerSource) {
    this.referrerName = referrerName;
    this.referrerSource = referrerSource;
  }

  @Override
  public String getReferrerName() {
    return referrerName;
  }

  @Override
  public NHReferrerSource getReferrerSource() {
    return referrerSource;
  }

}
