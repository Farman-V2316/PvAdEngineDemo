package com.dailyhunt.tv.players.interfaces;

import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * Created by Vinod
 */

public interface PlayerExoCallbacks extends PlayerCallbacks {

  void setPlayer(SimpleExoPlayer exoPlayer);

  void showVideoLoading(Boolean state);

  Boolean requestInstreamAd();

}
