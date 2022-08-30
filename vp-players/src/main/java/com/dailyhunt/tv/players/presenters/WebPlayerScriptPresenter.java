package com.dailyhunt.tv.players.presenters;

import com.dailyhunt.tv.players.interfaces.*;
import com.dailyhunt.tv.players.managers.*;
import com.dailyhunt.tv.players.service.*;
import com.newshunt.common.presenter.*;
import com.newshunt.dataentity.dhutil.model.entity.players.*;
import com.squareup.otto.*;

/**
 * Created by Jayanth on 09/05/18.
 */

public class WebPlayerScriptPresenter extends BasePresenter {
  private final Bus uiBus;
  private boolean isRegistered;
  private PlayerUnifiedWebPlayer player;
  private WebPlayerView playerView;
  private int retryCount = 3;

  public WebPlayerScriptPresenter(WebPlayerView playerView, Bus uiBus, PlayerUnifiedWebPlayer
      player) {
    this.uiBus = uiBus;
    this.player = player;
    this.playerView = playerView;
  }

  @Override
  public void start() {
    if (!isRegistered) {
      uiBus.register(this);
      isRegistered = true;
    }
    requestPlayerScript();
  }


  public void requestPlayerScript() {
    if(retryCount > 0) {
      PlayerWebPlayerScriptServiceImpl tvPlayerScriptService =
          new PlayerWebPlayerScriptServiceImpl(playerView
              .getViewContext(), uiBus, player);
      tvPlayerScriptService.getPlayerScript();
      retryCount--;
    }
  }


  @Subscribe
  public void onPlayerScriptResponse(PlayerWebPlayerScriptResponse playerScriptResponse) {
    if (playerScriptResponse == null || playerScriptResponse.getPlayerScript() == null) {
      playerView.showError("Unable to play");
      return;
    }
    PlayerUnifiedWebPlayer playerDetails = playerScriptResponse.getPlayerScript().getData();
    PlayerScriptManager.getInstance().savePlayerData(playerDetails);
    playerView.hideProgress();
    playerView.onPlayerScriptfetchSuccess();

  }


  @Override
  public void stop() {
    if (isRegistered) {
      uiBus.unregister(this);
      isRegistered = false;
    }
  }
}
