/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.internal.rest;

import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUpgradeInfo;

import io.reactivex.Single;
import retrofit2.http.GET;

/**
 * @author jayanth
 */
public interface PlayerUpgradeAPI {

  @GET("/api/v1/upgrade/dynamic/version?entity=PLAYERS_INFO")
  Single<ApiResponse<PlayerUpgradeInfo>> getPlayerInfo();
}
