/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newshunt.dataentity.dhutil.model.entity.players;


import java.io.Serializable;

/**
 * @author rohit
 */
public class PlayerVideoQuality extends PlayerItemQuality implements Serializable {

    private int hlsMinTimeForSwitchUpMs;
    private int hlsMaxTimeForSwitchDownMs;
    private int nomialBitRateForHLSFirstvariant;
    private int bufferMinSize;
    private int bufferMaxSize;
    private int bufferSegmentSize;
    private int initialBufferMs;
    private int playbackDurationAfterRebuffer;
    private boolean useDefaultConfigForLivestreams = true;


    public int getPlaybackDurationAfterRebuffer() {
      return playbackDurationAfterRebuffer;
    }

    public void setPlaybackDurationAfterRebuffer(int playbackDurationAfterRebuffer) {
        this.playbackDurationAfterRebuffer = playbackDurationAfterRebuffer;
    }

    public int getInitialBufferMs() {
        return initialBufferMs;
    }

    public void setInitialBufferMs(int initialBufferMs) {
        this.initialBufferMs = initialBufferMs;
    }

    public int getBufferSegmentSize() {
      return bufferSegmentSize;
    }

    public void setBufferSegmentSize(int bufferSegmentSize) {
        this.bufferSegmentSize = bufferSegmentSize * 1024;
    }

  public int getNomialBitRateForHLSFirstvariant() {
    return nomialBitRateForHLSFirstvariant;
  }

  public void setNomialBitRateForHLSFirstvariant(int nomialBitRateForHLSFirstvariant) {
    this.nomialBitRateForHLSFirstvariant = nomialBitRateForHLSFirstvariant;
  }

  public int getHlsMinTimeForSwitchUpMs() {
      return hlsMinTimeForSwitchUpMs;
  }

  public void setHlsMinTimeForSwitchUpMs(int hlsMinTimeForSwitchUpMs) {
    this.hlsMinTimeForSwitchUpMs = hlsMinTimeForSwitchUpMs;
  }

  public int getHlsMaxTimeForSwitchDownMs() {
      return hlsMaxTimeForSwitchDownMs;
  }

  public void setHlsMaxTimeForSwitchDownMs(int hlsMaxTimeForSwitchDownMs) {
    this.hlsMaxTimeForSwitchDownMs = hlsMaxTimeForSwitchDownMs;
  }

  public int getBufferMinSize() {
    return bufferMinSize;
  }

    public boolean isUseDefaultConfigForLivestreams() {
        return useDefaultConfigForLivestreams;
    }

    public void setUseDefaultConfigForLivestreams(boolean useDefaultConfigForLivestreams) {
        this.useDefaultConfigForLivestreams = useDefaultConfigForLivestreams;
    }

    public void setBufferMinSize(int bufferMinSize) {
    this.bufferMinSize = bufferMinSize;
  }

  public int getBufferMaxSize() {
    return bufferMaxSize;
  }

  public void setBufferMaxSize(int bufferMaxSize) {
    this.bufferMaxSize = bufferMaxSize;
  }
}
