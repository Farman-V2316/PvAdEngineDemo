/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.service;

import com.newshunt.dataentity.common.model.entity.server.asset.DNSConfig;
import com.newshunt.dataentity.dhutil.model.versionedapi.VersionMode;

import io.reactivex.Observable;

/**
 * Defines service required for fetching DNS configurations.
 *
 * @author karthik.r
 */
public interface DNSService {

  Observable<DNSConfig> getDNSContent(VersionMode versionMode);
}
