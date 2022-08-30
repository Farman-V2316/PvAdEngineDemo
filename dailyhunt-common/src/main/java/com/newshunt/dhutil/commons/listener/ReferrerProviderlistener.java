/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.commons.listener;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;

import java.util.Map;

import androidx.annotation.Nullable;

/**
 * Created by shashikiran.nr on 7/13/2016.
 */
public interface ReferrerProviderlistener {
  @Nullable
  PageReferrer getProvidedReferrer();

  NhAnalyticsEventSection getReferrerEventSection();

  default PageReferrer getLatestPageReferrer() {
    return null;
  }

  default Map<NhAnalyticsEventParam, Object> getExtraAnalyticsParams() {
    return null;
  }
}
