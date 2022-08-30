/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.upgrade;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Vinod. BC
 *
 *   Configuration Network bitrate estimate (nw sdk + exo bitrate)
 */

public class NetworkConfig implements Serializable {

  @SerializedName("exo_weightage_double")
  private Double exoWeightage = 1.1; // exo weightage for Bitrate calculation

  @SerializedName("network_weightage_double")
  private Double networkWeightage = 2.0; // nw sdk weightage for Bitrate calculation

  @SerializedName("bitrate_expression")
  private BitrateExpression bitrateExpression;

  @SerializedName("bitrate_expression_exception")
  private BitrateExpression bitrateExpressionException;

  @SerializedName("bitrate_expression_lifetime")
  private BitrateExpression bitrateExpressionLifetime;

  @SerializedName("bitrate_expression_v2")
  private BitrateExpression bitrateExpressionV2;

  @SerializedName("lifetime_bitrate_capture_window_sec")
  private long lifetimeBitrateCaptureWindowSec = 604800;

  @SerializedName("coldstart_transition_threshold_sec")
  private long  cardTransitionThresholdSec = 10;

  @SerializedName("exo_sliding_percentile_max_weight")
  private int exoSlidingPercentileMaxWeight = 2000;

  @SerializedName("coldstart_network_provider_mapping")
  private List<NetworkProviderQuality> networkProviderQuality = null;

  @SerializedName("sliding_percentile_percentile")
  private float spPercentile = 0.5f;

  public Double getExoWeightage() {
    return exoWeightage;
  }

  public void setExoWeightage(Double exoWeightage) {
    this.exoWeightage = exoWeightage;
  }

  public Double getNetworkWeightage() {
    return networkWeightage;
  }

  public void setNetworkWeightage(Double networkWeightage) {
    this.networkWeightage = networkWeightage;
  }

  public BitrateExpression getBitrateExpression() {
    return bitrateExpression;
  }

  public void setBitrateExpression(
      BitrateExpression bitrateExpression) {
    this.bitrateExpression = bitrateExpression;
  }

  public BitrateExpression getBitrateExpressionException() {
    return bitrateExpressionException;
  }

  public void setBitrateExpressionException(
      BitrateExpression bitrateExpressionException) {
    this.bitrateExpressionException = bitrateExpressionException;
  }

  public BitrateExpression getBitrateExpressionLifetime() {
    return bitrateExpressionLifetime;
  }

  public void setBitrateExpressionLifetime(
      BitrateExpression bitrateExpressionLifetime) {
    this.bitrateExpressionLifetime = bitrateExpressionLifetime;
  }

  public void setCardTransitionThresholdSec(long cardTransitionThresholdSec) {
    this.cardTransitionThresholdSec = cardTransitionThresholdSec;
  }

  public void setExoSlidingPercentileMaxWeight(int exoSlidingPercentileMaxWeight) {
    this.exoSlidingPercentileMaxWeight = exoSlidingPercentileMaxWeight;
  }

  public void setLifetimeBitrateCaptureWindowSec(long lifetimeBitrateCaptureWindowSec) {
    this.lifetimeBitrateCaptureWindowSec = lifetimeBitrateCaptureWindowSec;
  }

  public long getCardTransitionThresholdSec() {
    return cardTransitionThresholdSec;
  }

  public int getExoSlidingPercentileMaxWeight() {
    return exoSlidingPercentileMaxWeight;
  }

  public long getLifetimeBitrateCaptureWindowSec() {
    return lifetimeBitrateCaptureWindowSec;
  }

  public void setNetworkProviderQuality(
      List<NetworkProviderQuality> networkProviderQuality) {
    this.networkProviderQuality = networkProviderQuality;
  }

  public List<NetworkProviderQuality> getNetworkProviderQuality() {
    return networkProviderQuality;
  }

  public float getSpPercentile() {
    return spPercentile;
  }

  public void setSpPercentile(float spPercentile) {
    this.spPercentile = spPercentile;
  }

  public BitrateExpression getBitrateExpressionV2() {
    return bitrateExpressionV2;
  }

  public void setBitrateExpressionV2(BitrateExpression bitrateExpressionV2) {
    this.bitrateExpressionV2 = bitrateExpressionV2;
  }
}
