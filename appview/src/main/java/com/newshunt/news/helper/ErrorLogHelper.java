/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import androidx.annotation.Nullable;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.analytics.NhAnalyticsUtility;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.TabEntity;
import com.newshunt.common.view.DbgCodeKt;
import com.newshunt.dhutil.analytics.AnalyticsHelper;
import com.newshunt.news.analytics.NewsAnalyticsHelper;

/**
 * @author shrikant.agrawal
 */
public class ErrorLogHelper {


  public static void logErrorEvent(BaseError error, NhAnalyticsUtility.ErrorViewType errorViewType,
                                   NhAnalyticsUtility.ErrorPageType pageType, TabEntity tabEntity,
                                   PageReferrer currentPageReferrer, int tabPosition) {
    NewsAnalyticsHelper.logErrorScreenViewEvent(
        AnalyticsHelper.getErrorResponseCode(error.getMessage()), errorViewType, pageType,
        error.getStatus(), error.getMessage(), error.getUrl(), tabEntity, currentPageReferrer,
        tabPosition, DbgCodeKt.dbgCode(error));
  }

  public static void logNewsDetailErrorEvent(@Nullable BaseError error, NhAnalyticsUtility.ErrorViewType
      errorViewType, PageReferrer referrer, @Nullable  TabEntity tabEntity) {
    if(error == null) return;
    NewsAnalyticsHelper.logErrorScreenViewEvent(
        AnalyticsHelper.getErrorResponseCode(error.getMessage()), errorViewType,
        NhAnalyticsUtility.ErrorPageType.STORY_DETAIL, error.getStatus(),
        error.getMessage(), error.getUrl(), null, referrer, -1, DbgCodeKt.dbgCode(error));
  }
}
