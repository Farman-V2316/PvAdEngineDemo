/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.analytics;

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

public enum NewsReferrer implements NhAnalyticsReferrer {

  //New values
  TICKER("TICKER"),
  AD("AD"),
  RECENT_PAPER_CARD("RECENT_PAPER_CARD"),

  //Widget
  WIDGET("WIDGET", NewsReferrerSource.WIDGET_BROADCAST_RECEIVER),

  //News Home
  HEADLINES("HEADLINES", NewsReferrerSource.NEWS_HOME_VIEW),
  NEWS_HOME("NEWS_HOME", NewsReferrerSource.NEWS_HOME_VIEW),
  TOPIC_SIMILAR_VIDEO("TOPIC_SIMILIAR_VIDEO", NewsReferrerSource.NEWS_HOME_VIEW),//used for buzz
  // association
  NEWS_PUBLISHERS_HOME("NEWS_PUBLISHERS_HOME", NewsReferrerSource.NEWS_HOME_VIEW),

  //News Detail View
  WIDGET_PFP("WIDGET_PFP", NewsReferrerSource.NEWS_DETAIL_VIEW),
  CARD_WIDGET("CARD_WIDGET", NewsReferrerSource.NEWS_DETAIL_VIEW),
  STORY_DETAIL("STORY_DETAIL", NewsReferrerSource.NEWS_DETAIL_VIEW),
  COMMENT_DETAIL("COMMENT_DETAIL", NewsReferrerSource.NEWS_DETAIL_VIEW),
  COMMENT_LIST("COMMENT_LIST", NewsReferrerSource.NEWS_DETAIL_VIEW),
  STORY_CARD("STORY_CARD" , NewsReferrerSource.NEWS_HOME_VIEW),
  GALLERY("GALLERY", NewsReferrerSource.NEWS_DETAIL_VIEW),
  ERROR_SCREEN("ERROR_SCREEN", NewsReferrerSource.NEWS_DETAIL_VIEW),
  NEWS_STORY_DETAIL("NEWS_STORY_DETAIL", NewsReferrerSource.NEWS_DETAIL_VIEW), //used for buzz
  COLLECTION("COLLECTION", NewsReferrerSource.NEWS_DETAIL_VIEW),
  LIKE_CAROUSEL("like_carousel"),
  // association video preview

  //Topic Preview
  TOPIC("TOPIC", NewsReferrerSource.TOPIC_PREVIEW),
  TOPIC_WEB_ITEM("TOPIC_WEB_ITEM", NewsReferrerSource.TOPIC_PREVIEW),
  SUB_TOPIC("SUB_TOPIC", NewsReferrerSource.TOPIC_PREVIEW),
  ENTITY_BROWSING("ENTITY_BROWSING",NewsReferrerSource.ENTITY_PREVIEW),

  //Location Preview
  LOCATION("LOCATION", NewsReferrerSource.LOCATION_PREVIEW),

  //News Paper
  NEWS_PAPER("NEWS_PAPER", NewsReferrerSource.NEWS_PAPER_VIEW),
  SOURCES("SOURCES", NewsReferrerSource.NEWS_PAPER_VIEW),
  CATEGORY("CATEGORY", NewsReferrerSource.NEWS_PAPER_VIEW),

  //Add Page View
  TRENDING_TOPICS("TRENDING_TOPICS", NewsReferrerSource.ADD_PAGE_VIEW),
  FEATURED_TOPICS("FEATURED_TOPICS", NewsReferrerSource.ADD_PAGE_VIEW),
  FAVORITE_TOPICS("FAVORITE_TOPICS", NewsReferrerSource.ADD_PAGE_VIEW),
  ADD_LOCATION("ADD_LOCATION", NewsReferrerSource.ADD_PAGE_VIEW),
  TABSELECTION_VIEW("TABSELECTION_VIEW", NewsReferrerSource.ADD_PAGE_VIEW),
  TOPIC_NEWS_LIST("TOPIC_NEWS_LIST", NewsReferrerSource.ADD_PAGE_VIEW),
  LOCATION_NEWS_LIST("LOCATION_NEWS_LIST", NewsReferrerSource.ADD_PAGE_VIEW),
  ADD_TAB_HOME("ADD_TAB_HOME",NewsReferrerSource.ADD_PAGE_VIEW),

  //Tab Reorder screen
  MANAGE_NEWS_HOME("MANAGE_NEWS_HOME", NewsReferrerSource.TABS_REORDER_VIEW),

  //Source Group View
  SOURCE_GROUP("SOURCE_GROUP", NewsReferrerSource.SOURCE_GROUP_VIEW),
  RELATED_TOPIC("RELATED_TOPIC"),
  SIMILAR_STORIES("SIMILAR_STORIES"),

  //Saved Articles
  SAVED_ARTICLES("SAVED_ARTICLES", NewsReferrerSource.SAVED_ARTICLES_VIEW),

  // TODO: Finalize referer source value
  VIRAL("VIRAL", NewsReferrerSource.NEWS_HOME_VIEW),

  // COLLECTION VIEW
  TOPIC_COLLECTION("TOPIC_COLLECTION", NewsReferrerSource.TOPIC_PREVIEW),
  TOPIC_COLLECTION_TS("TOPIC_COLLECTION_TS", NewsReferrerSource.TOPIC_PREVIEW),
  COLLECTION_PREVIEW("COLLECTION_PREVIEW", NewsReferrerSource.NEWS_DETAIL_VIEW),
  FEED("FEED",FollowReferrerSource.FOLLOW_HOME_VIEW),
  SEARCH("SEARCH",NewsReferrerSource.SEARCH_HOME_VIEW),
  TOPICS("TOPICS"),
  SINGLE_SELECT_ONBOARDING("single_select_onboarding"),
  DISCOVERY_EXPLORE("DISCOVERY_EXPLORE",FollowReferrerSource.FOLLOW_EXPLORE_VIEW),
  FOLLOW_STAR_SECTION("FOLLOW_STAR_SECTION",FollowReferrerSource.FOLLOW_HOME_VIEW),
  DISCOVERY_SUGGESTED_NP("DISCOVERY_SUGGESTED_NP",NewsReferrerSource.NEWS_DETAIL_VIEW),
  DISCOVERY_TOPICTAB("DISCOVERY_TOPICTAB"),
  CS_FOLLOWING("CS_FOLLOWING"),
  CS_FEED("CS_FEED"),
  SD_NP_FOLLOWED("SD_NP_FOLLOWED"),
  SOURCE_BROWSING_FOLLOWED("SOURCE_BROWSING_FOLLOWED"),
  CS_LOCATION_FY("CS_LOCATION_FY"),
  LOCATION_SEARCH("LOCATION_SEARCH"),
  LOCATION_SELECTION_PAGE("location_selection_page"),
  HASHTAG("HASHTAG"),
  VIDEO_DETAIL("VIDEO_DETAIL", NewsReferrerSource.VIDEO_DETAIL),
  LOCAL_VIDEO_DETAIL("LOCAL_VIDEO_DETAIL", NewsReferrerSource.VIDEO_DETAIL),

  WIDGET_CARD("WIDGET_CARD");

  private String name;

  private NHReferrerSource referrerSource;

  NewsReferrer(String name) {
    this(name, null);
  }

  NewsReferrer(String name, NHReferrerSource referrerSource) {
    this.name = name;
    this.referrerSource = referrerSource;
  }

  @Override
  public String getReferrerName() {
    return name;
  }

  @Override
  public NHReferrerSource getReferrerSource() {
    return referrerSource;
  }

  public static NewsReferrer getNewsReferrer(String referrerStr) {
    if (CommonUtils.isEmpty(referrerStr)) {
      return null;
    }

    for (NewsReferrer newsReferrer : NewsReferrer.values()) {
      if (newsReferrer.getReferrerName().equals(referrerStr)) {
        return newsReferrer;
      }
    }
    return null;
  }
}
