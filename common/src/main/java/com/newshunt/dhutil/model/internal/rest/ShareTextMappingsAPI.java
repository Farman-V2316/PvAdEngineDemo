/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.internal.rest;

import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.common.model.entity.ShareTextMappingResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Provides API to fetch share text mapping Configuration.
 *
 * @author shashikiran.nr on 9/28/2017.
 */

public interface ShareTextMappingsAPI {

  @GET("api/v2/share/mappings")
  Observable<ApiResponse<ShareTextMappingResponse>> getShareTextMappingInfo();
}
