package com.newshunt.dataentity.news.model.entity.server.asset;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

/**
 * Player Asset require to play Video Content
 *
 * @author vinod.bc
 */

public class PlayerAsset implements Serializable {

  //Basic non modifiable fields
  private String id;
  private PlayerType videoType;
  private SourceInfo sourceInfo;
  private String videoUrl;
  private boolean autoPlay;
  private String sourceVideoId;
  private long durationLong;
  private boolean useIFrameForYTVideos;
  private PlayerThumbnailUrl thumbnail;
  @SerializedName("replaceableParams")
  private Map<String, String> replaceableParams;
  private String languageKey;
  private String categories;
  private String channelKey;
  private String adExtras;

  //Used by Player to track video state/scale
  private int width;
  private int height;
  private boolean inExpandMode;
  private ContentScale dataScale;
  private ContentScale dataExpandScale;
  private boolean muteMode;

  public PlayerAsset(String id, PlayerType videoType, SourceInfo sourceInfo,
                     String videoUrl, boolean autoPlay, String sourceVideoId, long durationLong,
                     boolean useIFrameForYTVideos, int width, int height, boolean muteMode,
                     Map<String, String> replaceableParams, String languageKey,
                     String categories, String channelKey, String adExtras) {
    this.id = id;
    this.videoType = videoType;
    this.sourceInfo = sourceInfo;
    this.videoUrl = videoUrl;
    this.autoPlay = autoPlay;
    this.sourceVideoId = sourceVideoId;
    this.durationLong = durationLong;
    this.useIFrameForYTVideos = useIFrameForYTVideos;
    this.width = width;
    this.height = height;
    this.muteMode = muteMode;
    this.replaceableParams = replaceableParams;
    this.languageKey = languageKey;
    this.categories = categories;
    this.channelKey = channelKey;
    this.adExtras = adExtras;
  }

  public String getId() {
    return id;
  }

  public PlayerType getType() {
    return videoType;
  }

  public SourceInfo getSourceInfo() {
    return sourceInfo;
  }

  public boolean isAutoPlay() {
    return autoPlay;
  }

  public String getSourceVideoId() {
    return sourceVideoId;
  }

  public boolean isUseIFrameForYTVideos() {
    return useIFrameForYTVideos;
  }

  public long getDurationLong() {
    return durationLong;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public boolean isInExpandMode() {
    return inExpandMode;
  }

  public void setInExpandMode(boolean inExpandMode) {
    this.inExpandMode = inExpandMode;
  }

  public ContentScale getDataScale() {
    return dataScale;
  }

  public void setDataScale(ContentScale dataScale) {
    this.dataScale = dataScale;
  }

  public ContentScale getDataExpandScale() {
    return dataExpandScale;
  }

  public void setDataExpandScale(ContentScale dataExpandScale) {
    this.dataExpandScale = dataExpandScale;
  }

  public boolean isMuteMode() {
    return muteMode;
  }

  public Map<String, String> getReplaceableParams() {
    return replaceableParams;
  }

  public PlayerThumbnailUrl getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(PlayerThumbnailUrl thumbnail) {
    this.thumbnail = thumbnail;
  }

  public String getLanguageKey() {
    return languageKey;
  }

  public void setLanguageKey(String languageKey) {
    this.languageKey = languageKey;
  }

  public String getCategories() {
    return categories;
  }

  public void setCategories(String categories) {
    this.categories = categories;
  }

  public String getChannelKey() {
    return channelKey;
  }

  public void setChannelKey(String channelKey) {
    this.channelKey = channelKey;
  }

  public String getAdExtras() {
    return adExtras;
  }
}
