package com.dailyhunt.tv.players.interfaces;

import android.content.Context;

/**
 * Created by santoshkulkarni on 25/05/17.
 */

public interface WebPlayerView {

  void hideProgress();

  void showError(String message);

  void onPlayerScriptfetchSuccess();

  Context getViewContext();
}
