/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.internal.rest;


import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionsResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit API to get server configured app sections
 *
 * @author santhosh.kc
 */
public interface AppSectionsAPI {

  @GET("api/v2/upgrade/appbar/bottom")
  Observable<ApiResponse<AppSectionsResponse>> getAppSections(@Query("version") String version,
                                                              @Query("appLanguage") String appLanguage);
}
