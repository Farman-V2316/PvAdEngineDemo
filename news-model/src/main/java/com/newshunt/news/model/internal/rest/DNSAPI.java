/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.internal.rest;

import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.common.model.entity.server.asset.DNSConfig;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Provides API to fetch DNS Configuration.
 *
 * @author karthik.r
 */
public interface DNSAPI {

  @GET("api/v2/upgrade/dns")
  Observable<ApiResponse<DNSConfig>> getDNSConfig(@Query("version") String version);
}
