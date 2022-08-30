package com.dailyhunt.tv.exolibrary;

import com.dailyhunt.tv.exolibrary.listeners.OnBufferUpdateListener;
import com.dailyhunt.tv.exolibrary.listeners.OnCompletionListener;
import com.dailyhunt.tv.exolibrary.listeners.OnErrorListener;
import com.dailyhunt.tv.exolibrary.listeners.OnPreparedListener;
import com.dailyhunt.tv.exolibrary.listeners.OnSeekCompletionListener;
import com.dailyhunt.tv.exolibrary.listeners.VideoControlsFullScreenListener;
import com.dailyhunt.tv.exolibrary.listeners.VideoControlsSoundListener;
import com.dailyhunt.tv.exolibrary.listeners.VideoControlsPlayPauseListener;
import com.dailyhunt.tv.exolibrary.listeners.VideoTimeListener;
import com.google.android.exoplayer2.ExoPlaybackException;

import java.util.LinkedList;


public class ExoMediaDelegate {
  private VideoControlsFullScreenListener videoControlsFullScreenListener;
  private OnCompletionListener onCompletionListener;
  private OnPreparedListener preparedListener;
  private LinkedList<OnSeekCompletionListener> seekCompletionListener = new LinkedList<>();
  private LinkedList<VideoControlsPlayPauseListener> playPauseListener = new LinkedList<>();
  private LinkedList<VideoControlsSoundListener> soundListeners = new LinkedList<>();
  private LinkedList<OnErrorListener> errorListener = new LinkedList<>();
  private LinkedList<OnBufferUpdateListener> bufferUpdateListeners = new LinkedList<>();
  private VideoTimeListener videoTimeListener;

  public void setSeekCompletionListener(final OnSeekCompletionListener seekCompletionListener) {
    this.seekCompletionListener.add(seekCompletionListener);
  }

  public void setVideoControlsSoundListeners(final VideoControlsSoundListener soundListeners) {
    if (this.soundListeners != null) {
      this.soundListeners.add(soundListeners);
    }
  }

  public void setOnCompletionListener(final OnCompletionListener onCompletionListener) {
    this.onCompletionListener = onCompletionListener;
  }

  public void setPreparedListener(final OnPreparedListener preparedListener) {
    this.preparedListener = preparedListener;
  }

  public void setVideoControlsFullScreenListener(
      final VideoControlsFullScreenListener videoControlsFullScreenListener) {
    this.videoControlsFullScreenListener = videoControlsFullScreenListener;
  }

  public void setErrorListener(final OnErrorListener errorListener) {
    this.errorListener.add(errorListener);
  }

  public void setBufferUpdateListener(final OnBufferUpdateListener bufferUpdateListener) {
    this.bufferUpdateListeners.add(bufferUpdateListener);
  }


  public void setPlayPauseListener(final VideoControlsPlayPauseListener playPauseListener) {
    if (this.playPauseListener != null) {
      this.playPauseListener.add(playPauseListener);
    }
  }

  public void setVideoTimeListener(final VideoTimeListener videoTimeListener) {
      this.videoTimeListener = videoTimeListener;
  }

  public void onFullscreenButtonClick() {
    if (videoControlsFullScreenListener != null) {
      videoControlsFullScreenListener.onFullScreenClicked();
    }
  }

  public void onCompletion() {
    if (onCompletionListener != null) {
      onCompletionListener.onCompletion();
    }
  }

  public void onLoopComplete() {
    if (onCompletionListener != null) {
      onCompletionListener.onLoopComplete();
    }
  }

  public void onPrepare() {
    if (preparedListener != null) {
      preparedListener.onPrepared();
    }
  }

  public void onBufferingState(boolean showLoader){
    if (bufferUpdateListeners != null) {
      for (OnBufferUpdateListener bufferUpdateListener : this.bufferUpdateListeners) {
        bufferUpdateListener.onBufferingUpdate(showLoader ? 0 : 100);
      }
    }
  }

  public void seekTo(final long position) {
    if (seekCompletionListener != null) {
      for (OnSeekCompletionListener completionListener : seekCompletionListener) {
        completionListener.onSeekComplete();
      }
    }
  }

  public void onPlayPause() {
    if (playPauseListener != null) {
      for (VideoControlsPlayPauseListener videoControlsPlayPauseListener : this.playPauseListener) {
        videoControlsPlayPauseListener.onPlayerPlayPause();
      }
    }
  }

  public void muteAudio() {
    if (soundListeners != null) {
      for (VideoControlsSoundListener soundListener : this.soundListeners) {
        soundListener.setAudioToMute();
      }
    }
  }

  public void unMuteAudio() {
    if (soundListeners != null) {
      for (VideoControlsSoundListener soundListener : this.soundListeners) {
        soundListener.setAudioToUnMute();
      }
    }
  }

  public void error(final ExoPlaybackException e) {
    if (errorListener != null) {
      for (OnErrorListener onErrorListener : errorListener) {
        onErrorListener.onError(e);
      }
    }
  }

  public void onRestart() {
    if (playPauseListener != null) {
      for (VideoControlsPlayPauseListener videoControlsPlayPauseListener : this.playPauseListener) {
        videoControlsPlayPauseListener.onRestart();
      }
    }
  }

  public void onTimeUpdate(long position) {
    if (videoTimeListener != null) {
      videoTimeListener.onTimeUpdate("", position);
    }
  }
}
