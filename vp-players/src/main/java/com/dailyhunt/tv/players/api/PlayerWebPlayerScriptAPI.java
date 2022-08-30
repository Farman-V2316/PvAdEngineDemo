package com.dailyhunt.tv.players.api;


import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUnifiedWebPlayer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Jayanth on 09/05/18.
 */

public interface PlayerWebPlayerScriptAPI {

  @GET("api/v3/player/source/js/{sourceKey}")
  Call<ApiResponse<PlayerUnifiedWebPlayer>> getPlayerScript(
      @Path("sourceKey") String sourceKey,
      @Query("deviceType") String deviceType);
}
