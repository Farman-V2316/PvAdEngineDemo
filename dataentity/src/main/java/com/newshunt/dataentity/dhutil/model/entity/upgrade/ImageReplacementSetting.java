/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.upgrade;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * @author satosh.dhanyamraju
 */
public class ImageReplacementSetting implements Serializable {
  private static final long serialVersionUID = 3201263957306728096L;

  private EmbeddedImage embeddedImage;

  private Double imageDimensionMultiplier;

  private Map<String, String> imageQualitiesByNetwork;

  public EmbeddedImage getEmbeddedImage() {
    return embeddedImage;
  }

  public void setEmbeddedImage(EmbeddedImage embeddedImage) {
    this.embeddedImage = embeddedImage;
  }

  public Double getImageDimensionMultiplier() {
    return imageDimensionMultiplier == null ||
        (Double.compare(imageDimensionMultiplier, 0) == 0) ? 1.0f : imageDimensionMultiplier;
  }

  public void setImageDimensionMultiplier(double imageDimensionMultiplier) {
    this.imageDimensionMultiplier = imageDimensionMultiplier;
  }

  public Map<String, String> getImageQualitiesByNetwork() {
    return imageQualitiesByNetwork;
  }

  public void setImageQualitiesByNetwork(Map<String, String> imageQualitiesByNetwork) {
    this.imageQualitiesByNetwork = imageQualitiesByNetwork;
  }

  public String getQualityString(String networkSpeedKey) {
    if (CommonUtils.isEmpty(imageQualitiesByNetwork)) {
      return null;
    }
    return imageQualitiesByNetwork.get(networkSpeedKey);
  }
}
