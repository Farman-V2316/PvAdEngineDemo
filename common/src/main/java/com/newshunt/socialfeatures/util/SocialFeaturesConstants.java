/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.socialfeatures.util;

/**
 * @author santhosh.kc
 */
public class SocialFeaturesConstants {

  public static final long ONE_THOUSAND_COMMENTS = 1000;
  public static final long ONE_MILLION_COMMENTS = ONE_THOUSAND_COMMENTS * ONE_THOUSAND_COMMENTS;
  public static final long ONE_BILLION_COMMENTS = ONE_MILLION_COMMENTS * ONE_THOUSAND_COMMENTS;
  public static final long ONE_TRILLION_COMMENTS = ONE_BILLION_COMMENTS *
      (long)ONE_THOUSAND_COMMENTS;

  public static final String ONE_KILO = "K";
  public static final String ONE_MILLION = "M";
  public static final String ONE_BILLION = "B";
  public static final String ONE_TRILLION = "T";

  //Parameters for commentParams
  public static final String NAMESPACE_KEY = "namespace";
  public static final String COMMENT_ID_PREFIX_KEY = "cid";

  //Intent Parameters
  public static final String BUNDLE_COMMENT_PARAMS = "bundle_comment_params";
  public static final String BUNDLE_NH_ANALYTICS_EVENT_SECTION = "socialAnalyticsEventSection";
  public static final String BUNDLE_BASE_ASSET_ANALYTICS_PARAMS = "baseAssetAnalyticsParams";
  public static String STORY_DETAIL_REFERRER_PREFIX = "storydetail&&";

  //Network Response codes
  public static final int CODE_RESPONSE_INVALID_SESSION = 11;

  // err messages
  public static final String NOT_LOGGED_IN = "not logged in";

  public static final String ENTITY_TYPE = "entityType";
  public static final String ACTION_TYPE = "actionType";
  public static final String ID = "id";
  public static final String CID = "cid";
  public static final float ZERO_COUNT_ALPHA = 0.5f;
  public static final float ALPHA_FULL = 1.0f;

  public static final String TABTYPE_COMMENTS = "comments";
  public static final String WIDGET_DISPLAY_TYPE_LIST = "LIST";
  public static final String WIDGET_TYPE_COMMENTS = "COMMENTS";
  public static final String WIDGET_TYPE_COMMENT = "COMMENT";
  public static final String COMMENT_TYPE_REPLY = "reply";
  public static final String COMMENT_TYPE_MAIN = "main";
  public static final String LIKE_EMOJI_TYPE = "like";
  public static final String UNLIKE_EMOJI_TYPE = "unlike";
  public static final String REPORT_SPAM_YES = "yes";
  public static final String REPORT_SPAM_COMMENT = "report_spam_comment";

}
