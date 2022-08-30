package com.dailyhunt.tv.players.interfaces;

import android.widget.RelativeLayout;

import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction;
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction;
import com.dailyhunt.tv.players.helpers.PlayerEvent;

import androidx.lifecycle.MutableLiveData;


/**
 * Generic Item View holder and ItemPlayer Interaction protocol
 *
 * @author ranjith  ,vinod.bc
 */
public interface PlayerViewDH {

  MutableLiveData<PlayerEvent> getPlayerStateLiveData();

  void setViewLayoutParams(RelativeLayout.LayoutParams params);

  void setFullScreenMode(boolean fullScreenMode);

  boolean isInFullScreenMode();

  boolean hasVideoEnded();

  void restartVideo();

  void pause();

  void releaseAndSetReload();

  default void pauseWithOutAction() {}

  void resume();

  void onBackPressed();

  void releasePlayer();

  void unmutePlayerOnDeviceVolumeRaised();

  default void partiallyReleasePlayer() {}

  default long duration() {
        return 0L;
    }


  default long totalDuration() {
    return 0L;
  }

  // -- Analytics Purpose -- //
  void setEndAction(PlayerVideoEndAction endAction);

  void setStartAction(PlayerVideoStartAction startAction);

  default boolean getPlayerMuteState(){
    return false;
  }

  default Long getCurrentDuration() {
    return 0L;
  }

  void resumeOnNetworkError(boolean isPaused);

  default Boolean isAdDisplaying() {
    return false;
  }

  default boolean isPlaying() {
    return false;
  }

}
