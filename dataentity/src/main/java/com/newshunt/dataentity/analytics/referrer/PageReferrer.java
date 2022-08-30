/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.analytics.referrer;

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;

import java.io.Serializable;

/**
 * Tells how user landed on particular page.
 *
 * @author shreyas.desai
 */
public class PageReferrer implements Serializable {

  private static final long serialVersionUID = 1519301222062572266L;
  private static final String LOG_TAG = PageReferrer.class.getSimpleName();
  private NhAnalyticsReferrer referrer = NhGenericReferrer.NULL;
  private String id;
  // Applicable for category
  private String subId;
  private NhAnalyticsUserAction referrerAction;
  private PageReferrer castedPageReferrer;
  private NHReferrerSource referrerSource;

  public PageReferrer() {
  }

  public PageReferrer(PageReferrer pageReferrer) {
    if (pageReferrer == null) {
      return;
    }
    this.referrer = pageReferrer.referrer;
    this.id = pageReferrer.id;
    this.subId = pageReferrer.subId;
    this.referrerAction = pageReferrer.referrerAction;
    referrerSource = pageReferrer.referrerSource;
  }

  public PageReferrer(NhAnalyticsReferrer referrer) {
    this(referrer, null);
  }

  public PageReferrer(NhAnalyticsReferrer referrer, String id) {
    this(referrer, id, null);
  }

  public PageReferrer(NhAnalyticsReferrer referrer, String id, String subId) {
    this(referrer, id, subId, null);
  }

  public PageReferrer(NhAnalyticsReferrer referrer, String id, String subId,
                      NhAnalyticsUserAction referrerAction) {
    this(referrer, id, subId, referrerAction, null);
  }

  public PageReferrer(NhAnalyticsReferrer referrer, String id, String subId,
                      NhAnalyticsUserAction referrerAction, NHReferrerSource nhReferrerSource) {
    this.referrer = referrer;
    this.id = id;
    this.subId = subId;
    this.referrerAction = referrerAction;
    referrerSource = nhReferrerSource;
    if (referrerSource == null) {
      referrerSource = referrer.getReferrerSource();
    }
  }

  public NhAnalyticsReferrer getReferrer() {
    return referrer;
  }

  public void setReferrer(NhAnalyticsReferrer referrer) {
    this.referrer = referrer;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSubId() {
    return subId;
  }

  public void setSubId(String subId) {
    this.subId = subId;
  }

  public NhAnalyticsUserAction getReferrerAction() {
    return referrerAction;
  }

  public void setReferrerAction(NhAnalyticsUserAction referrerAction) {
    this.referrerAction = referrerAction;
  }

  public NHReferrerSource getReferrerSource() {
    if (referrerSource == null && referrer != null) {
      return referrer.getReferrerSource();
    }
    return referrerSource;
  }

  public void setReferrerSource(NHReferrerSource referrerSource) {
    this.referrerSource = referrerSource;
  }

  @Override
  public boolean equals(Object pageReferrer) {

    castedPageReferrer = (PageReferrer) pageReferrer;

    if (castedPageReferrer == null || !(castedPageReferrer instanceof PageReferrer)) {
      return false;
    }

    if (referrer != castedPageReferrer.getReferrer()) {
      return false;
    }

    if (id != null && !id.equals(castedPageReferrer.getId())) {
      return false;
    }

    if (super.equals(pageReferrer)) {
      return true;
    }

    return true;
  }

}
