/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.internal.rest;


import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppLaunchConfigResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author santhosh.kc
 */
public interface AppLaunchRulesAPI {

  @GET("api/v2/upgrade/applaunch/rules")
  Observable<ApiResponse<AppLaunchConfigResponse>> getAppLaunchRules(@Query("version") String version,
                                                                     @Query("langCode") String userLangCodes,
                                                                     @Query("acqType") String acqType);
}
