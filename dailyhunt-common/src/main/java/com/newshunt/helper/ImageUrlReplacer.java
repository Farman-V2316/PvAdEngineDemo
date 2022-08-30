/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.helper;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

/**
 * Helper class to replace the image download url with desired width and height.
 *
 * @author santhosh.kc
 */
public class ImageUrlReplacer {

  private static double IMAGE_DIMENSTION_MULTIPLIER;
  public static final String RESOLUTION_CHARACTER = "x";
  public static final String DEFAULT_IMAGE_EXTENSION = "webp";
  private static float CONTENT_IMAGE_ASPECT_RATIO;

  public static final String DEFAULT_EMBEDDED_IMAGE_MACRO = "#DH_EMB_IMG_REP#";
  private static final String IMAGE_SCALE_TYPE_MACRO = "#DH_EMB_IMG_CMD#";
  private static final String IMAGE_EXTENSION_MACRO = "#DH_EMB_IMG_EXT#";
  private static String EMBEDDED_IMAGE_MACRO;

  private static final String IMAGE_SCALE_TYPE = "resize";

  private static final String DEFAULT_EMBEDDED_IMAGE_SLOW = "50x50";
  private static String EMBEDDED_IMAGE_SLOW;
  private static boolean isImageMultiplierValid;

  static {
    updateMacros();
  }

  public static void updateMacros() {
    String multiplierString = PreferenceManager.getPreference(AppStatePreference
        .IMAGE_DIMENSION_MULTIPLIER, Constants.EMPTY_STRING);
    isImageMultiplierValid = !CommonUtils.isEmpty(multiplierString);
    double value = 1.0f;
    if (isImageMultiplierValid) {
      try {
        value = Double.parseDouble(multiplierString);
      } catch (NumberFormatException e) {
        isImageMultiplierValid = false;
        Logger.caughtException(e);
      }
    }
    IMAGE_DIMENSTION_MULTIPLIER = value;

    CONTENT_IMAGE_ASPECT_RATIO =
        PreferenceManager.getPreference(AppStatePreference.NEWS_DETAIL_IMAGE_ASPECT_RATIO,
            Constants.DEFAULT_IMG_AR);

    EMBEDDED_IMAGE_MACRO = PreferenceManager.getPreference(AppStatePreference.EMBEDDED_IMAGE_MACRO,
        DEFAULT_EMBEDDED_IMAGE_MACRO);
    EMBEDDED_IMAGE_SLOW = PreferenceManager.getPreference(AppStatePreference.EMBEDDED_IMAGE_SLOW,
        DEFAULT_EMBEDDED_IMAGE_SLOW);
  }

  public static boolean isImageDimensionMultiplierReceived() {
    return isImageMultiplierValid;
  }

  public static String getEmbeddedImageSlow() {
    return EMBEDDED_IMAGE_SLOW;
  }

  public static float getContentImageAspectRatio() {
    return CONTENT_IMAGE_ASPECT_RATIO;
  }

  public static String replaceHTMLWithImageMacro(String html, String replacement) {
    if (CommonUtils.isEmpty(html)) {
      return html;
    }

    return html.replaceAll(EMBEDDED_IMAGE_MACRO, replacement);
  }

  public static String getQualifiedUrl(@NonNull final String url,
                                       @NonNull final Pair<Integer, Integer> desiredDimension) {
    if (desiredDimension == null) {
      return url;
    }

    return getQualifiedUrl(url, getDimensionString(desiredDimension.first) + RESOLUTION_CHARACTER +
        getDimensionString(desiredDimension.second));
  }

  public static String getQualifiedUrl(@NonNull final String url,
                                       @NonNull final String replacementResolution) {
    return getQualifiedUrl(url, replacementResolution, null);
  }

  public static String getDimensionString(int dimension) {
    //the value must the dimension in density pixels multiplied by the multiplier factor
    // configured by server
    return String.valueOf((int) Math.floor(dimension * IMAGE_DIMENSTION_MULTIPLIER));
  }


  public static String getQualifiedUrl(@NonNull String url,
                                       @NonNull final String replacementResolution,
                                       @NonNull final String scaleType) {
    if (CommonUtils.isEmpty(url) || CommonUtils.isEmpty(replacementResolution)) {
      return url;
    }

    String decodedUrl = null;
    try {
      decodedUrl = URLDecoder.decode(url, Constants.TEXT_ENCODING_UTF_8);
    } catch (UnsupportedEncodingException e) {
      Logger.caughtException(e);
    }

    if (CommonUtils.isEmpty(decodedUrl) || !decodedUrl.contains(EMBEDDED_IMAGE_MACRO)) {
      return url;
    }
    if (!CommonUtils.isEmpty(scaleType)) {
      //Replace image scale type in the url
      url = url.replace(IMAGE_SCALE_TYPE_MACRO, scaleType);
    }

    if(!CommonUtils.isEmpty(url)) {
      //Replace image extension in the url
      url = url.replace(IMAGE_EXTENSION_MACRO, DEFAULT_IMAGE_EXTENSION);
    }

    return url.replace(EMBEDDED_IMAGE_MACRO, replacementResolution);
  }

  /**
   * Constructs a image url by replacing scale type, resolution and extension of image
   * This is only used for Viral and Live TV images
   *
   * @param imageUrl
   * @param width    - width of view
   * @param height   - height of view
   * @return
   */
  public static String getQualifiedImageUrl(String imageUrl, int width, int height) {

    String resolution = getDimensionString(width) + RESOLUTION_CHARACTER +
        getDimensionString(height);

    if(!CommonUtils.isEmpty(imageUrl)) {
      imageUrl = imageUrl.replace(IMAGE_EXTENSION_MACRO, DEFAULT_IMAGE_EXTENSION);
    }
    return getQualifiedUrl(imageUrl, resolution, IMAGE_SCALE_TYPE);
  }

    /**
     * Constructs a image url by replacing scale type, resolution and extension of image
     *
     * @param imageUrl    - imageUrl
     * @param width       - width of view
     * @param aspectRatio - aspectRatio of view
     * @return
     */
    public static String getQualifiedImageUrl(String imageUrl, int width, float aspectRatio) {
        if (Float.compare(aspectRatio, 0) == 0) {
            aspectRatio = Constants.IMAGE_ASPECT_RATIO_16_9;
        }
        int height = (int) (width / aspectRatio);
        return getQualifiedImageUrl(imageUrl, width, height);
    }
}
