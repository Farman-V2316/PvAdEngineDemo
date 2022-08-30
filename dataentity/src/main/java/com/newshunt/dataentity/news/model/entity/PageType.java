/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.news.model.entity;

import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.news.analytics.NewsReferrer;
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.news.analytics.NewsReferrer;

/**
 * Represents main sections of news app.
 *
 * @author shreyas.desai
 */
public enum PageType {
  // News pages
  // News pages
  /*Page type Topic is used by News-BE for any topic item, but Ads-BE wants topics as page type to
  respond the ad request, So I am adding Topics param here. Now when News-BE will
  say page type Topic, client will send page type as Topics to Ads-BE with ad request*/

  /*Page type Source is used by News-BE for any news paper item, but Ads-BE wants Sources as page type
   to respond the ad request. So when News-BE will say page type Source, client will send page type
   as Sources to Ads-BE with ad request*/
  HEADLINES("HEADLINES"),
  TOPIC("TOPIC"),
  SOURCE("SOURCE"),
  SOURCES("SOURCES"),
  SAVED_ARTICLES("SAVED_ARTICLES"),
  NOTIFICATIONS("NOTIFICATIONS"),
  LOCATION("LOCATION"),
  GALLERY("GALLERY"),
  SPLASH("SPLASH"),
  SUB_TOPIC("SUB_TOPIC"),
  SUB_LOCATION("SUB_LOCATION"),
  CATEGORY("CATEGORY"),
  INVALID("INVALID"),
  TOPICS("TOPICS"),
  BUZZGROUP("BUZZGROUP"),
  LIVETVGROUP("LIVETVGROUP"),
  BUZZSHOWS("BUZZSHOWS"),
  WEB_TOPIC("web_topic"),
  WEB_SUBTOPIC("web_subtopic"),
  WEB_LOCATION("web_location"),
  WEB_SUBLOCATION("web_sublocation"),
  SIMILARSTORIES("similarstories"),
  VIRAL("VIRAL"),
  EXPLORE("EXPLORE","explore"),
  FEED("FEED","feed"),
  SEARCH("SEARCH"),
  DHTV("DHTV_HOME"),
  FOLLOW("FOLLOW","follow"),
  PROFILE_HISTORY("PROFILE_HISTORY", "profile_history"),
  PROFILE_ACTIVITY("PROFILE_ACTIVITY","profile_activity"),
  GENERIC_ACTIVITY("GENERIC_ACTIVITY","generic_activity"),
  PROFILE_SAVED("PROFILE_SAVED","profile_saved"),
  PROFILE_SAVED_DETAIL("SEE_ALL", "see_all"),
  PROFILE_TPV_RESPONSES("PROFILE_TPV_RESPONSES","profile_tpv_responses"),
  PROFILE_MY_POSTS("PROFILE_MY_POSTS","profile_my_posts"),
  PROFILE_TPV_POSTS("PROFILE_TPV_POSTS","profile_tpv_posts"),
  HASHTAG("HASHTAG"),
  SOURCECAT("SOURCECAT");

  private String pageType;

  private String deeplinkValue;

  PageType(String type) {
    this.pageType = type;
  }

  PageType(String type, String deeplinkValue) {
    this.pageType = type;
    this.deeplinkValue = deeplinkValue;
  }

  public static PageType fromName(String type) {
    for (PageType pageType : PageType.values()) {
      if (pageType.pageType.equalsIgnoreCase(type)) {
        return pageType;
      }
    }
    return INVALID;
  }

  public String getPageType() {
    return pageType;
  }

  public static PageReferrer getPageReferrer(PageType pageType) {
    if (pageType == null) {
      return null;
    }
    switch (pageType) {
      case HEADLINES:
        return new PageReferrer(NewsReferrer.HEADLINES);
      case TOPIC:
        return new PageReferrer(NewsReferrer.TOPIC);
      case VIRAL:
        return new PageReferrer(NewsReferrer.VIRAL);
      case SOURCE:
        return new PageReferrer(NewsReferrer.CATEGORY);
      case SOURCES:
        return new PageReferrer(NewsReferrer.SOURCES);
      case SAVED_ARTICLES:
        return new PageReferrer(NewsReferrer.SAVED_ARTICLES);
      case NOTIFICATIONS:
        return new PageReferrer(NhGenericReferrer.NOTIFICATION);
      case GALLERY:
        return new PageReferrer(NewsReferrer.GALLERY);
      case SPLASH:
        return new PageReferrer(NhGenericReferrer.SPLASH);
      case LOCATION:
        return new PageReferrer(NewsReferrer.LOCATION);
      case SIMILARSTORIES:
        return new PageReferrer(NewsReferrer.SIMILAR_STORIES);
      case HASHTAG:
        return new PageReferrer(NewsReferrer.HASHTAG);
      default:
        return null;
    }
  }

  public String getDeeplinkValue() {
    return deeplinkValue;
  }
}
