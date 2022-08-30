package com.dailyhunt.tv.exolibrary;

/**
 * Created by santoshkulkarni on 26/08/16.
 */
public final class VideoQualitySettings {

  private int hlsMinTimeForSwitchUpMs;
  private int hlsMaxTimeForSwitchDownMs;
  private int nomialBitRateForHLSFirstvariant;
  private int minBufferSize;
  private int maxBufferSize;

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

  public int getMinBufferSize() {
    return minBufferSize;
  }

  public void setMinBufferSize(int minBufferSize) {
    this.minBufferSize = minBufferSize;
  }

  public int getMaxBufferSize() {
    return maxBufferSize;
  }

  public void setMaxBufferSize(int maxBufferSize) {
    this.maxBufferSize = maxBufferSize;
  }
}
