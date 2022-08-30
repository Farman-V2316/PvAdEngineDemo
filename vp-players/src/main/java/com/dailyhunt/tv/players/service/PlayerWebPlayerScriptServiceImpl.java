package com.dailyhunt.tv.players.service;

import android.content.*;

import com.dailyhunt.tv.players.api.*;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.model.retrofit.*;
import com.newshunt.dataentity.common.model.entity.*;
import com.newshunt.dataentity.common.model.entity.model.*;
import com.newshunt.dataentity.dhutil.model.entity.players.*;
import com.newshunt.dhutil.helper.retrofit.*;
import com.newshunt.sdk.network.*;
import com.squareup.otto.*;

/**
 * Created by Jayanth on 09/05/18.
 */

public class PlayerWebPlayerScriptServiceImpl {
  private Context context;
  private Bus uiBus;
  private PlayerUnifiedWebPlayer player;
  private PlayerWebPlayerScriptAPI playerScriptAPI = null;

  public PlayerWebPlayerScriptServiceImpl(Context context, Bus uiBus, PlayerUnifiedWebPlayer player) {
    this.context = context;
    this.uiBus = uiBus;
    this.player = player;
    playerScriptAPI = getPlayerScriptAPI(Priority.PRIORITY_HIGHEST, null);
  }


  public void getPlayerScript() {
    playerScriptAPI.getPlayerScript(player.getPlayerKey(), Constants.ANDROID)
        .enqueue(getRetrofitCallback());
  }

  private PlayerWebPlayerScriptAPI getPlayerScriptAPI(Priority priority, Object tag) {
    return RestAdapterContainer.getInstance().getRestAdapter(
        NewsBaseUrlContainer.getApplicationUrl(), priority, tag).create(PlayerWebPlayerScriptAPI.class);
  }

  private CallbackWrapper<ApiResponse<PlayerUnifiedWebPlayer>> getRetrofitCallback() {
    return new CallbackWrapper<ApiResponse<PlayerUnifiedWebPlayer>>() {
      @Override
      public void onSuccess(ApiResponse<PlayerUnifiedWebPlayer> response) {
        PlayerWebPlayerScriptResponse
            playerWebPlayerScriptResponse = new PlayerWebPlayerScriptResponse();
        playerWebPlayerScriptResponse.setPlayerScript(response);
        uiBus.post(playerWebPlayerScriptResponse);
      }

      @Override
      public void onError(BaseError error) {

        PlayerWebPlayerScriptResponse errorResponse = new PlayerWebPlayerScriptResponse();
        errorResponse.setBaseError(error);
        uiBus.post(errorResponse);
      }
    };
  }

}
