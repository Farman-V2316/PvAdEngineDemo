/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.internal.rest;

import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.dhutil.model.entity.notifications.ChineseDeviceInfoResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * @author shrikant.agrawal
 */
public interface ChineseDeviceInfoAPI {

  @GET("/api/v2/upgrade/dynamic/version?entity=CHINESE_NOTI")
  Observable<ApiResponse<ChineseDeviceInfoResponse>> getChineseDeviceInfo();
}
