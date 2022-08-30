/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.util.R;

/**
 * Sourced from: http://www.codexpedia.com/android/android-round-corner-imageview/
 * https://github.com/codexpedia/android_custom_imageview_round_corners
 */
public class NHRoundedCornerImageView extends NHImageView {

  public static final int CORNER_NONE = 0;
  public static final int CORNER_TOP_LEFT = 1;
  public static final int CORNER_TOP_RIGHT = 2;
  public static final int CORNER_BOTTOM_RIGHT = 4;
  public static final int CORNER_BOTTOM_LEFT = 8;
  public static final int CORNER_ALL = 15;

  private int cornerRadius;
  private int roundedCorners;
  private int borderColor;
  private int borderSize;
  private int drawingMode = CORNER_NONE;

  private static final int CORNERS_TOP = CORNER_TOP_LEFT | CORNER_TOP_RIGHT;
  private static final int CORNERS_BOTTOM = CORNER_BOTTOM_LEFT | CORNER_BOTTOM_RIGHT;
  private static final int CORNERS_LEFT = CORNER_TOP_LEFT | CORNER_BOTTOM_LEFT;
  private static final int CORNERS_RIGHT = CORNER_TOP_RIGHT | CORNER_BOTTOM_RIGHT;

  public NHRoundedCornerImageView(Context context) {
    this(context, null);
  }

  public NHRoundedCornerImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NHRoundedCornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedCornerImageView);
    cornerRadius = a.getDimensionPixelSize(R.styleable.RoundedCornerImageView_cornerRadius, 0);
    roundedCorners = a.getInt(R.styleable.RoundedCornerImageView_roundedCorners, CORNER_NONE);
    borderColor = a.getColor(R.styleable.RoundedCornerImageView_borderColor, 0);
    borderSize = (int) a.getDimension(R.styleable.RoundedCornerImageView_borderSize, 0f);
    computeDrawingMode();
    setRoundedBackground();
    a.recycle();
  }

  public void setCornerRadius(int radius) {
    cornerRadius = radius;
    configureCornerRounding();
  }

  public int getRadius() {
    return cornerRadius;
  }

  public void setRoundedCorners(int corners) {
    roundedCorners = corners;
    configureCornerRounding();
  }

  public int getRoundedCorners() {
    return roundedCorners;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    configureCornerRounding();
  }

  private void setRoundedBackground() {
    if (cornerRadius > 0) {
      if (roundedCorners == CORNER_NONE) {
        return;
      } else if (roundedCorners == CORNER_ALL) {
        if ((borderColor != 0) && borderSize > 0) {
          setBackground(
              AndroidUtils.makeRoundedRectDrawable(cornerRadius, Color.TRANSPARENT, borderSize,
                  borderColor));
          setPadding(borderSize, borderSize, borderSize, borderSize);
          setCropToPadding(true);
        } else {
          setBackground(AndroidUtils.makeRoundedRectDrawable(cornerRadius, Color.TRANSPARENT, 0,
              Color.TRANSPARENT));
        }
      } else {
        setViewOutline();
      }
      setClipToOutline(true);
    }
  }

  private void computeDrawingMode() {
    if (roundedCorners == CORNER_ALL) {
      drawingMode = CORNER_ALL;
    } else if (roundedCorners == CORNER_NONE) {
      drawingMode = CORNER_NONE;
    } else if ((roundedCorners & CORNERS_TOP) == CORNERS_TOP) {
      drawingMode = CORNERS_TOP;
    } else if ((roundedCorners & CORNERS_BOTTOM) == CORNERS_BOTTOM) {
      drawingMode = CORNERS_BOTTOM;
    } else if ((roundedCorners & CORNERS_LEFT) == CORNERS_LEFT) {
      drawingMode = CORNERS_LEFT;
    } else if ((roundedCorners & CORNERS_RIGHT) == CORNERS_RIGHT) {
      drawingMode = CORNERS_RIGHT;
    }
  }

  private void configureCornerRounding() {
    computeDrawingMode();
    if (!isLegacyDrawingNeeded()) {
      setRoundedBackground();
    }
  }

  private boolean isLegacyDrawingNeeded() {
    return false;
  }

  /**
   * Hack to show rounded corners only for top/bottom/left/right edges. The view outline is
   * tweaked to hide the rounded corners which needn't be rounded.
   */
  private void setViewOutline() {
    setOutlineProvider(new ViewOutlineProvider() {
      @Override
      public void getOutline(View view, Outline outline) {
        if (view != null && outline != null) {
          int width = getWidth();
          int height = getHeight();
          int left = CORNERS_RIGHT == drawingMode ? -cornerRadius : 0;
          int right = CORNERS_LEFT == drawingMode ? width + cornerRadius : width;
          int top = CORNERS_BOTTOM == drawingMode ? -cornerRadius : 0;
          int bottom = CORNERS_TOP == drawingMode ? height + cornerRadius : height;
          outline.setRoundRect(left, top, right, bottom, cornerRadius);
        }
      }
    });
  }
}