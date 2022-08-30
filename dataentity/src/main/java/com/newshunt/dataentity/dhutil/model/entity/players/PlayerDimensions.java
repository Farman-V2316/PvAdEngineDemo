package com.newshunt.dataentity.dhutil.model.entity.players;

/**
 * Created by vinod.bc on 7/31/2016.
 */

import java.io.Serializable;
import java.util.List;

/**
 * @author rohit
 */
public class PlayerDimensions implements Serializable {
  private static final long serialVersionUID = 8325728151454393157L;

  private String resolutionBucket;
  private String dimensionVersion;
  private String adaptivePlaceHolder;
  private List<PlayerVideoQuality> videoQualities;
  private List<PlayerVideoQuality> audioQualities;
  private List<PlayerItemQuality> imageQualities;
  private List<PlayerItemQuality> gifQualities;
  private List<PlayerItemQuality> thumbnailQualities;
  private PlayerVideoQuality adaptiveSettings;

  //Add by Client to save rework on change of class TVVideoQuality
  private List<PlayerItemQuality> videoItemQualities;

  public List<PlayerItemQuality> getGifQualities() {
    return gifQualities;
  }

  public void setGifQualities(List<PlayerItemQuality> gifQualities) {
    this.gifQualities = gifQualities;
  }

  public List<PlayerItemQuality> getImageQualities() {
    return imageQualities;
  }

  public List<PlayerItemQuality> getVideoItemQualities() {
    return videoItemQualities;
  }

  public void setImageQualities(List<PlayerItemQuality> imageQualities) {
    this.imageQualities = imageQualities;
  }

  public String getResolutionBucket() {
    return resolutionBucket;
  }

  public void setResolutionBucket(String resolutionBucket) {
    this.resolutionBucket = resolutionBucket;
  }

  public String getDimensionVersion() {
    return dimensionVersion;
  }

  public void setDimensionVersion(String dimensionVersion) {
    this.dimensionVersion = dimensionVersion;
  }

  public List<PlayerVideoQuality> getVideoQualities() {
    return videoQualities;
  }

  public void setVideoQualities(List<PlayerVideoQuality> videoQualities) {
    this.videoQualities = videoQualities;
  }

  public List<PlayerVideoQuality> getAudioQualities() {
    return audioQualities;
  }

  public void setAudioQualities(List<PlayerVideoQuality> audioQualities) {
    this.audioQualities = audioQualities;
  }

  public List<PlayerVideoQuality> getVideoQualitiesForExo() {
    return videoQualities;
  }

  public String getAdaptivePlaceHolder() {
    return adaptivePlaceHolder;
  }

  public void setAdaptivePlaceHolder(String adaptivePlaceHolder) {
    this.adaptivePlaceHolder = adaptivePlaceHolder;
  }

  public PlayerVideoQuality getAdaptiveSettings() {
    return adaptiveSettings;
  }

  public void setAdaptiveSettings(PlayerVideoQuality adaptiveSettings) {
    this.adaptiveSettings = adaptiveSettings;
  }

  public List<PlayerItemQuality> getThumbnailQualities() {
    return thumbnailQualities;
  }

  public void setThumbnailQualities(
      List<PlayerItemQuality> thumbnailQualities) {
    this.thumbnailQualities = thumbnailQualities;
  }
}

