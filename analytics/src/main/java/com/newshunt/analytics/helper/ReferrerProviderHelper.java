/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.helper;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.FixedLengthQueue;

/**
 * @author: bedprakash.rout on 7/8/2016.
 */
public class ReferrerProviderHelper implements PageReferrerProvider {

  private NhAnalyticsUserAction action;
  private FixedLengthQueue<PageReferrer> referrerQueue = new FixedLengthQueue<>(2);

  public void addReferrerByProvider(PageReferrer currentPage) {
    if (referrerQueue.getYongest() != null && referrerQueue.getYongest().equals(currentPage)) {
      return;
    }
    referrerQueue.add(currentPage);
  }

  public FixedLengthQueue<PageReferrer> getReferrerQueue() {
    return referrerQueue;
  }

  @Override
  public PageReferrer getProvidedPageReferrer() {
    if (referrerQueue == null || referrerQueue.getOldest() == null) {
      return null;
    }
    referrerQueue.getOldest().setReferrerAction(action);
    return referrerQueue.getOldest();
  }

  @Override
  public PageReferrer getYoungestPageReferrer() {
    if (referrerQueue == null || referrerQueue.getOldest() == null) {
      return null;
    }
    referrerQueue.getOldest().setReferrerAction(action);
    return referrerQueue.getYongest();
  }

  public void setAction(NhAnalyticsUserAction action) {
    this.action = action;
  }
}
