/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.helper.analytics;

import java.io.Serializable;

/**
 * Generic Analytics Referrer need to be implemented by all Referrer's Entities
 * https://blogs.oracle.com/darcy/entry/enums_and_mixins
 *
 * @author ranjith.suda
 */
public interface NhAnalyticsReferrer extends Serializable {

  String getReferrerName();

  NHReferrerSource getReferrerSource();
}
