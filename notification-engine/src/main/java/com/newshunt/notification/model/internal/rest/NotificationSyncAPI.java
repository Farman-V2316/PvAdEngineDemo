package com.newshunt.notification.model.internal.rest;

import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.notification.model.entity.server.NotificationSyncResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by karthik.r on 2019-11-07.
 */
public interface NotificationSyncAPI {

  @GET
  Observable<ApiResponse<NotificationSyncResponse>> syncNotifications(@Url String requestUrl,
                                                                      @Query("clientId") String clientId,
                                                                      @Query("notificationEnabled") boolean notificationEnabled,
                                                                      @Query("filter") String filter,
                                                                      @Query("direction") String direction,
                                                                      @Query("fullSyncPageSize") Integer fullSyncPageSize,
                                                                      @Query("pageMarker") String pageMarker);
}
