/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.viral.model.entity.server;

import android.graphics.drawable.GradientDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.newshunt.dataentity.common.view.customview.FIT_TYPE;

import java.io.Serializable;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Background option model for all viral cards
 * Created by bedprakash on 17/11/17.
 */

public class BackgroundOption implements Serializable {

  private static final long serialVersionUID = 1L;

  @Retention(SOURCE)
  @StringDef({BackgroundType.BG_COLOR, BackgroundType.GRADIENT, BackgroundType.IMAGE_BG})
  public @interface BackgroundType {
    String BG_COLOR = "BG_COLOR";
    String GRADIENT = "GRADIENT";
    String IMAGE_BG = "IMAGE_BG";
//    String NINE_PATCH = "NINE_PATCH";
  }

  private String type = BackgroundType.BG_COLOR;

  // fields to query background config
  // type BgColor
  public String bgColor;

  // Gradient
  public String startColor;
  public String midColor;
  public String endColor;

  private String borderColor;

  /**
   * draw the gradient from the top to the bottom
   * TOP_BOTTOM,
   * <p>
   * draw the gradient from the top-right to the bottom-left
   * TR_BL,
   * <p>
   * draw the gradient from the right to the left
   * RIGHT_LEFT,
   * <p>
   * BR_TL,
   * <p>
   * draw the gradient from the bottom to the top
   * BOTTOM_TOP,
   * <p>
   * draw the gradient from the bottom-left to the top-right
   * BL_TR,
   * <p>
   * draw the gradient from the left to the right
   * LEFT_RIGHT,
   * <p>
   * draw the gradient from the top-left to the bottom-right
   * TL_BR,
   */
  private String gradientType = "BL_TR";

  // 9patch
  public String identifier;

  // Image Background
  // bgColor,height,width is expected for images as well
  public String imageUrl;
  public int width;
  public int height;

  //(TOP_CROP, FIT_DISP_WID, FIT_DISP_HEI, FIT_CENTER, FIT_XY, CENTER_CROP)
  private String fitType = FIT_TYPE.TOP_CROP.name();

  @Nullable
  public String getFitType() {
    return fitType;
  }

  public void setFitType(@NonNull FIT_TYPE fitType) {
    this.fitType = fitType.name();
  }

  @Nullable
  public String getGradientType() {
    return gradientType;
  }

  public void setGradientType(@NonNull GradientDrawable.Orientation gradientType) {
    this.gradientType = gradientType.name();
  }

  @BackgroundType
  public String getType() {
    return type;
  }

  public void setType(@BackgroundType String type) {
    this.type = type;
  }

  public String getBorderColor() {
    return borderColor;
  }
}
