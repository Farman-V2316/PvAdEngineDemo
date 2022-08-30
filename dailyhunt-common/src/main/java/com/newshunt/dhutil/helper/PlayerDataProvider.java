/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper;

import com.dailyhunt.tv.players.utils.PlayerConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerDimensions;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerEvents;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUnifiedWebPlayer;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUpgradeInfo;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUpgradeInfoResponse;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;

import java.util.ArrayList;
import java.util.List;

public class PlayerDataProvider {

  private static PlayerDataProvider instance;
  private PlayerDimensions playerDimensions;
  private List<PlayerEvents> playerEvents;
  private List<PlayerUnifiedWebPlayer> playersList;

  private PlayerDataProvider() {
    //Do Nothing
  }

  public static PlayerDataProvider getInstance() {
    if (instance == null) {
      synchronized (PlayerDataProvider.class) {
        instance = new PlayerDataProvider();
      }
    }
    return instance;
  }

  /*
  Methods to sync PlayerDimension & Player List
*/
  public void syncPlayerData() {
    playersList = getPlayerListFromCache();
    playerDimensions = getDimensionsFromCache();

    if (CommonUtils.isEmpty(playersList) || playerDimensions == null) {
      //Read from asset folder
      PlayerUpgradeInfo upgradeInfo = getUpgradeResponseFromAsset();
      if (upgradeInfo != null) {
        if (CommonUtils.isEmpty(playersList)) {
          playersList = upgradeInfo.getPlayers();
        }
        if (playerDimensions == null) {
          playerDimensions = upgradeInfo.getDimensions();
        }
      }
    }
  }

  /*
      Methods to access & modify PlayerDimensions
  */
  public PlayerDimensions getPlayerDimensions() {
    return playerDimensions;
  }

  public void setPlayerDimensions(PlayerDimensions playerDimensions) {
    if (playerDimensions != null) {
      this.playerDimensions = playerDimensions;
      setDimensionsInCache(playerDimensions);
    }
  }

  private void setDimensionsInCache(PlayerDimensions imageDimensions) {
    //Overright with image dimensions available
    String jsonData = new Gson().toJson(imageDimensions);
    //Saving the TVUpgradeInfo Json
    PreferenceManager.saveString(DailyhuntConstants.KEY_PLAYER_DIMENSION_JSON, jsonData);
  }

  private PlayerDimensions getDimensionsFromCache() {
    PlayerDimensions dimensions = null;
    //Saving the TVUpgradeInfo Json
    String jsonStr = PreferenceManager.getString(DailyhuntConstants.KEY_PLAYER_DIMENSION_JSON,
        Constants.EMPTY_STRING);
    if (!CommonUtils.isEmpty(jsonStr)) {
      try {

      } catch (Exception e) {
        Logger.caughtException(e);
      }
    }

    return dimensions;
  }

  /*
    Methods to access & modify PlayerEvents
  */
  public PlayerEvents getPlayerEvents(String sourceKey) {
    if (CommonUtils.isEmpty(sourceKey) || CommonUtils.isEmpty(playerEvents)) {
      return null;
    }
    for (PlayerEvents playerEvent : playerEvents) {
      if (playerEvent != null && sourceKey.equals(playerEvent.getSourceKey())) {
        return playerEvent;
      }
    }
    return null;
  }

  public void setPlayerEvents(List<PlayerEvents> events) {
    playerEvents = events;
  }

  /*
    Methods to access & modify Web Player List
  */
  public List<PlayerUnifiedWebPlayer> getPlayersList() {
    return playersList;
  }

  public void setPlayersList(List<PlayerUnifiedWebPlayer> playerslist) {
    if (!CommonUtils.isEmpty(playerslist)) {
      this.playersList = playerslist;
      savePlayerListInCache(playerslist);
    }
  }

  private void savePlayerListInCache(List<PlayerUnifiedWebPlayer> playerslist) {
    //Overright with image dimensions available
    String jsonData = new Gson().toJson(playerslist);
    //Saving the TVUpgradeInfo Json
    PreferenceManager.saveString(DailyhuntConstants.KEY_WEB_PLAYER_LIST_JSON, jsonData);
  }

  private List<PlayerUnifiedWebPlayer> getPlayerListFromCache() {
    List<PlayerUnifiedWebPlayer> playerList = null;
    //Saving the TVUpgradeInfo Json
    String jsonStr = PreferenceManager.getString(DailyhuntConstants.KEY_WEB_PLAYER_LIST_JSON,
        Constants.EMPTY_STRING);
    if (!CommonUtils.isEmpty(jsonStr)) {
      try {
        playerList =
            (ArrayList<PlayerUnifiedWebPlayer>) new Gson().fromJson(jsonStr,
                new TypeToken<ArrayList<PlayerUnifiedWebPlayer>>() {
                }.getType());
      } catch (Exception e) {
        Logger.caughtException(e);
      }
    }

    return playerList;
  }

  private static PlayerUpgradeInfo getUpgradeResponseFromAsset() {
    String jsonData = CommonUtils.readFromAsset(PlayerConstants.PLAYER_JSON_FILENAME);
    if (jsonData != null) {
      try {
        PlayerUpgradeInfoResponse apiResponse =
            new Gson().fromJson(jsonData, PlayerUpgradeInfoResponse.class);
        if (null != apiResponse && null != apiResponse.getData()) {
          PlayerUpgradeInfo upgradeInfo = apiResponse.getData();
          return upgradeInfo;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  public String getUserAgentString(String playerKey) {
    if(!CommonUtils.isEmpty(playerKey) && !CommonUtils.isEmpty(playersList)) {
      for (int index = 0; index < playersList.size(); index++) {
        PlayerUnifiedWebPlayer playerDetails = playersList.get(index);
        if(playerDetails != null && playerKey.equalsIgnoreCase(playerDetails.getPlayerKey())) {
          return playerDetails.getUserAgentString();
        }
      }
    }
    return "";
  }
}
