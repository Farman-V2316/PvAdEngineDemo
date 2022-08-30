package com.newshunt.dataentity.news.model.entity.server.asset;

import java.io.Serializable;
import java.util.Map;

/**
 * Exo Player Asset require to play Exo Video Content
 *
 * @author vinod.bc
 */

public class NewsVideoAsset extends PlayerAsset implements Serializable {

  private int loopCount;
  //used by exoplayer to differentiate between live video and normal
  private boolean liveStream;
  private boolean applyPreBufferSetting;

  //These required for instream ads
  private String adUrl;
  private String adcmsId;
  private String advId;
  private String adDescriptionUrl;
  private boolean disableAds;
  private String fileType;

  public NewsVideoAsset(String id, PlayerType assetType, SourceInfo sourceInfo,
                        String playerUrl, boolean autoPlay, String sourceVideoId,
                        long durationLong, boolean useIFrameForYTVideos, int width, int height,
                        int loopCount, boolean liveStream, boolean applyPreBufferSetting,
                        String adUrl, String fileType, String adcmsId, String advId,
                        String adDescriptionUrl, boolean disableAds, boolean muteMode,
                        Map<String, String> replaceableParams, String languageKey,
                        String categories, String channelKey, String adExtras) {

    super(id, assetType, sourceInfo, playerUrl, autoPlay, sourceVideoId,
        durationLong, useIFrameForYTVideos, width, height, muteMode, replaceableParams,
        languageKey, categories, channelKey, adExtras);

    this.loopCount = loopCount;
    this.liveStream = liveStream;
    this.adUrl = adUrl;
    this.adcmsId = adcmsId;
    this.advId = advId;
    this.applyPreBufferSetting = applyPreBufferSetting;
    this.fileType = fileType;
    this.adDescriptionUrl = adDescriptionUrl;
    this.disableAds = disableAds;
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

  public boolean isLiveStream() {
    return liveStream;
  }

  public void setPlayerType(String playerType) {
    this.fileType = fileType;
  }

  public String getPlayerType() {
    return fileType;
  }

  public boolean isApplyPreBufferSetting() {
    return applyPreBufferSetting;
  }

  public void setApplyPreBufferSetting(boolean applyPreBufferSetting) {
    this.applyPreBufferSetting = applyPreBufferSetting;
  }

}
