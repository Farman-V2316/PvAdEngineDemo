package com.dailyhunt.tv.players.managers;

import com.google.gson.Gson;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.FileUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUnifiedWebPlayer;
import com.newshunt.dhutil.helper.PlayerDataProvider;

import java.io.IOException;
import java.util.List;

/**
 * Created by Jayanth on 09/05/18.
 */

public class PlayerScriptManager {

  private final static String FB_JAVASCRIPT_JS = "tvfbscript.js";
  private final static String YT_JAVASCRIPT_JS = "tviframescript.js";
  private static String fbJavaScriptStr = Constants.EMPTY_STRING;
  private static String ytJavaScriptStr = Constants.EMPTY_STRING;
  private static final String PLAYER_PREFIX = "DH_WEB_PLAYER_TYPE_";
  private final static String SAMPLE_HTML_PAGE = "vuclip.js";
  private static final String DH_FILE_EXT = ".js";
  private static final String DH_JS_FILE_SYNC = "DH_JS_FILE_SYNC_";

  private static PlayerScriptManager thisInstance;
  private String recentFetchedKey;

  public static synchronized PlayerScriptManager getInstance() {
    if (thisInstance == null) {
      synchronized (PlayerScriptManager.class) {
        thisInstance = new PlayerScriptManager();
      }
    }
    return thisInstance;
  }

  private PlayerScriptManager() {
    syncJsonFileFromAsset();
  }

  public void syncJsonFileFromAsset() {
    String json_sync = DH_JS_FILE_SYNC + AppConfig.getInstance().getAppVersion();
    if(!PreferenceManager.getBoolean(json_sync, false)) {
      //Load initial data
      loadInitialDataFromAsset();
      PreferenceManager.saveBoolean(json_sync, true);
    }
  }

  private void loadInitialDataFromAsset() {
    List<PlayerUnifiedWebPlayer> playerslist = PlayerDataProvider.getInstance().getPlayersList();
    if (CommonUtils.isEmpty(playerslist)) {
      return;
    }

    if (!CommonUtils.isEmpty(playerslist)) {
      for (int i = 0; i < playerslist.size(); i++) {
        syncJavaScriptDataFromAsset(playerslist.get(i));
        recentFetchedKey = null;
      }
    }
  }

  private void syncJavaScriptDataFromAsset(PlayerUnifiedWebPlayer player) {
    //NOTE:: Keep the <sourceKey>.js file in Asset to preload player data
    if (CommonUtils.isEmpty(player.getPlayerKey())) {
      return;
    }
    String strJS = getPlayerJS(player.getPlayerKey());
    if (!CommonUtils.isEmpty(strJS)) {
      return;
    }

    String playerData = getJavaScriptFromAsset(player.getPlayerKey() + DH_FILE_EXT);
    if (!CommonUtils.isEmpty(playerData)) {
      player.setData(playerData);
      savePlayerData(player);
    }
  }

  public String getPlayerJS(String playerType) {
    if (CommonUtils.isEmpty(playerType)) {
      return null;
    }
    String key = PLAYER_PREFIX + playerType;
    return PreferenceManager.getString(key);
  }

  public PlayerUnifiedWebPlayer getPlayerData(String playerType) {
    PlayerUnifiedWebPlayer playerInfo;
    if (CommonUtils.isEmpty(playerType)) {
      return null;
    }
    String strJS = getPlayerJS(playerType);
    if (CommonUtils.isEmpty(strJS)) {
      return null;
    }
    playerInfo = new Gson().fromJson(strJS, PlayerUnifiedWebPlayer.class);
    if (null != playerInfo && playerInfo.getPlayerKey() != null) {
      if (isPlayerDataFetchedNow(playerInfo) || isPlayerDataLatestVersion(playerInfo)) {
        return playerInfo;
      }
    }
    return null;
  }

  private boolean isPlayerDataFetchedNow(PlayerUnifiedWebPlayer curPlayer) {
    if(curPlayer != null && curPlayer.getPlayerKey() != null) {
      return curPlayer.getPlayerKey().equalsIgnoreCase(recentFetchedKey);
    }
    return false;
  }

  private boolean isPlayerDataLatestVersion(PlayerUnifiedWebPlayer curPlayer) {
    //true - Use the existing PlayerData
    //false - needs to fetch new copy of player data from server
    List<PlayerUnifiedWebPlayer> playerslist = PlayerDataProvider.getInstance().getPlayersList();
    if (CommonUtils.isEmpty(playerslist)) {
      return true;
    }

    PlayerUnifiedWebPlayer temp;
    for (int i = 0; i < playerslist.size(); i++) {
      temp = playerslist.get(i);
      if (null != temp && temp.getPlayerKey() != null &&
          temp.getPlayerKey().equalsIgnoreCase(curPlayer.getPlayerKey())) {
        if (temp.getVersion().equalsIgnoreCase(curPlayer.getVersion())) {
          return true;
        }
        return false;
      }
    }
    //New Player is not available, as handshake as not happened
    return true;
  }

  public void savePlayerData(PlayerUnifiedWebPlayer playerInfo) {
    if (null == playerInfo) {
      return;
    }
    String jsonData = new Gson().toJson(playerInfo);
    String key = PLAYER_PREFIX + playerInfo.getPlayerKey();
    PreferenceManager.saveString(key, jsonData);
    recentFetchedKey = playerInfo.getPlayerKey();
  }

  //************************ Managing player HTML in shared preference *******************

  public String getFBJavaScriptStr() {
    if (!CommonUtils.isEmpty(fbJavaScriptStr)) {
      return fbJavaScriptStr;
    }
    fbJavaScriptStr = getJavaScriptFromAsset(FB_JAVASCRIPT_JS);
    return fbJavaScriptStr;
  }

  public String getYTJavaScriptStr() {
    if (!CommonUtils.isEmpty(ytJavaScriptStr)) {
      return ytJavaScriptStr;
    }
    ytJavaScriptStr = getJavaScriptFromAsset(YT_JAVASCRIPT_JS);
    return ytJavaScriptStr;
  }

  public String getSampleHtmlForTest() {
    String htmlStr = getJavaScriptFromAsset(SAMPLE_HTML_PAGE);
    return htmlStr;
  }

  private String getJavaScriptFromAsset(String fileName) {
    String jsAsset = Constants.EMPTY_STRING;
    try {
      byte[] javaScript =
          FileUtil.readData(CommonUtils.getApplication().getAssets().open(fileName));
      jsAsset = new String(javaScript);
    } catch (IOException e) {
      Logger.caughtException(e);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return jsAsset;
  }

}
