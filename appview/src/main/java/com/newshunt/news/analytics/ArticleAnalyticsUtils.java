/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.analytics;

import com.newshunt.dataentity.news.analytics.NewsReferrer;
import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class helps for analyticsEvent over article event
 *
 * @author shashikiran.nr on 3/2/2016.
 */
public class ArticleAnalyticsUtils {

  /**
   * Update the page referrer.
   *
   * @param referrer Referrer object.
   */
  private static void updateReferrer(NewsReferrer referrer) {
    NhAnalyticsAppState.getInstance().setReferrer(referrer);
  }

  //AnalyaticEvent for Delete Article from saved list ........
  public static void  storyDeleteArticleEvent(String articleCount) {
    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    if (CommonUtils.isEmpty(articleCount)) {
      articleCount = Constants.EMPTY_STRING;
    }
    paramsMap.put(NhAnalyticsNewsEventParam.SAVELIST_DELETEARTICLE_COUNT, articleCount);
    updateReferrer(NewsReferrer.SAVED_ARTICLES);
    AnalyticsClient.log(NhAnalyticsNewsEvent.SAVELIST_DELETEARTICLE,
        NhAnalyticsEventSection.NEWS, paramsMap);
  }
}
