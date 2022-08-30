package com.newshunt.dataentity.news.model.entity.server.asset;

import java.io.Serializable;
import java.util.Map;

/**
 * Exo Player Asset require to play Exo Video Content
 *
 * @author vinod.bc
 */

public class ExoPlayerAsset extends NewsVideoAsset implements Serializable {

  private boolean isGif;
  private int loopCount;
  private boolean hideControl;
  //used by exoplayer to differentiate between live video and normal
  private boolean liveStream;
  private boolean applyPreBufferSetting;

  //These required for instream ads
  private String adUrl;
  private String adcmsId;
  private String advId;
  private String adDescriptionUrl;
  private boolean disableAds;
  private String playerType;
  private String trackingUrl;

  public ExoPlayerAsset(String id, PlayerType assetType, SourceInfo sourceInfo,
                        String playerUrl, boolean autoPlay, String sourceVideoId,
                        long durationLong, boolean useIFrameForYTVideos, int width, int height,
                        boolean isGif, int loopCount, boolean hideControl,
                        boolean liveStream, boolean applyPreBufferSetting,
                        String adUrl, String playerType, String adcmsId, String advId,
                        String adDescriptionUrl, boolean disableAds, boolean muteMode,
                        Map<String, String> replaceableParams, String trackingUrl,
                        String languageKey, String categories, String channelKey, String adExtras) {

    super(id, assetType, sourceInfo, playerUrl, autoPlay, sourceVideoId, durationLong,
        useIFrameForYTVideos, width, height, loopCount, liveStream, applyPreBufferSetting,
        adUrl, playerType, adcmsId, advId, adDescriptionUrl, disableAds, muteMode,
        replaceableParams, languageKey, categories, channelKey, adExtras);

    this.isGif = isGif;
    this.loopCount = loopCount;
    this.hideControl = hideControl;
    this.liveStream = liveStream;
    this.adUrl = adUrl;
    this.adcmsId = adcmsId;
    this.advId = advId;
    this.applyPreBufferSetting = applyPreBufferSetting;
    this.playerType = playerType;
    this.adDescriptionUrl = adDescriptionUrl;
    this.disableAds = disableAds;
    this.trackingUrl = trackingUrl;
  }


  public String getAdUrl() {
    return adUrl;
  }

  public String getAdcmsId() {
    return adcmsId;
  }

  public String getAdvId() {
    return advId;
  }

  public String getAdDescriptionUrl() {
    return adDescriptionUrl;
  }

  public boolean isDisableAds() {
    return disableAds;
  }

  public int getLoopCount() {
    return loopCount;
  }

  public boolean isGif() {
    return isGif;
  }

  public boolean isHideControl() { return hideControl; }

  public boolean isLiveStream() {
    return liveStream;
  }

  public void setPlayerType(String playerType) {
    this.playerType = playerType;
  }

  public String getPlayerType() {
    return playerType;
  }

  public boolean isApplyPreBufferSetting() {
    return applyPreBufferSetting;
  }

  public void setApplyPreBufferSetting(boolean applyPreBufferSetting) {
    this.applyPreBufferSetting = applyPreBufferSetting;
  }

  public String getTrackingUrl() {
    return trackingUrl;
  }
}
