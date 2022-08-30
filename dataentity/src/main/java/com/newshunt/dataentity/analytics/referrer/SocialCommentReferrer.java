/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.analytics.referrer;

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;

/**
 * @author santhosh.kc
 */
public enum SocialCommentReferrer implements NhAnalyticsReferrer {
  COMMENT("COMMENT"),
  COMMENTS("COMMENTS"),
  COMMENT_BAR("COMMENT_BAR");

  private String name;

  SocialCommentReferrer(String name) {
    this.name = name;
  }

  @Override
  public String getReferrerName() {
    return name;
  }

  @Override
  public NHReferrerSource getReferrerSource() {
    return null;
  }
}
