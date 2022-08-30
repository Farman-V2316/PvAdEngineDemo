/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview.kenburns;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.newshunt.common.view.customview.NHRoundedCornerImageView;

/**
 * @author anshul.jain
 */
public class NhKenBurnsImageView extends NHRoundedCornerImageView {

  private final RectF mViewportRect = new RectF();
  private final RectF mDrawableRect = new RectF();
  private final RectF mDrawableTopCropRect = new RectF();
  private boolean zoomIn = true;
  private int TIME_DURATION = 5_000;
  private float MAX_SCALE = 0.2f;
  private float step = MAX_SCALE / (TIME_DURATION / FRAME_DELAY);
  private float ORIGINAL_SCALE = 1f;
  private float originalScale , scale;
  private KenBurnsState kenBurnsState = KenBurnsState.DISABLED;
  private static final long FRAME_DELAY = 1000 / 60;
  private String loadID;

  public NhKenBurnsImageView(Context context) {
    this(context, null);
  }

  public NhKenBurnsImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NhKenBurnsImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    super.setScaleType(ImageView.ScaleType.MATRIX);
  }


  @Override
  public void setScaleType(ScaleType scaleType) {
    // It'll always be center-cropped by default.
  }


  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);
        /* When not visible, onDraw() doesn't get called,
           but the time elapses anyway. */
    switch (visibility) {
      case VISIBLE:
        //If animation is disabled than do not change state to resume. whichever feature is using
        // it will change state if needed.
        if (kenBurnsState != KenBurnsState.DISABLED) {
          resume();
        }
        break;
      default:
        //If animation is disabled than do not change state to pause. whichever feature is using
        // it will change state if needed.
        if (kenBurnsState != KenBurnsState.DISABLED) {
          pause();
        }
        break;
    }
  }


  @Override
  public void setImageBitmap(Bitmap bm) {
    super.setImageBitmap(bm);
    handleImageChange();
  }


  @Override
  public void setImageResource(int resId) {
    super.setImageResource(resId);
    handleImageChange();
  }


  @Override
  public void setImageURI(Uri uri) {
    super.setImageURI(uri);
    handleImageChange();
  }


  @Override
  public void setImageDrawable(Drawable drawable) {
    super.setImageDrawable(drawable);
    handleImageChange();
  }


  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    restart();
  }


  @Override
  protected void onDraw(Canvas canvas) {
    if (kenBurnsState == KenBurnsState.RESUMED && !viewPortEmpty() && !drawableEmpty()) {
      Matrix matrix = getImageMatrix();
      matrix.reset();
      matrix.setRectToRect(mDrawableTopCropRect, mViewportRect, Matrix.ScaleToFit.FILL);

      if (scale <= originalScale) {
        zoomIn = true;
      } else if (scale >= originalScale + MAX_SCALE) {
        zoomIn = false;
      }

      scale += zoomIn ? step : -step;

      matrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
      setImageMatrix(matrix);
      postInvalidateDelayed(FRAME_DELAY);
    } else if (kenBurnsState == KenBurnsState.STOPPED && getDrawable() != null) {
      init();
    }
    super.onDraw(canvas);
  }


  public void init() {
    if (viewPortEmpty() || drawableEmpty()) {
      return;
    }
    Matrix m = getImageMatrix();
    m.reset();

    mDrawableTopCropRect.set(mDrawableRect);
    float viewportRatio = mViewportRect.width() / mViewportRect.height();
    float drawableRatio = mDrawableRect.width() / mDrawableRect.height();

    if (viewportRatio <= drawableRatio) {
      float diffInWidth =
          Math.abs(mDrawableRect.width() - (viewportRatio * mDrawableRect.height()));
      mDrawableTopCropRect.left = mDrawableRect.left + diffInWidth / 2;
      mDrawableTopCropRect.right = mDrawableRect.right - diffInWidth / 2;
    } else {
      float diffInHeight =
          Math.abs(mDrawableRect.height() - (mDrawableRect.width() / viewportRatio));
      mDrawableTopCropRect.bottom = mDrawableRect.bottom - diffInHeight;
    }

    m.setRectToRect(mDrawableTopCropRect, mViewportRect, Matrix.ScaleToFit.FILL);
    if (scale == 0.0f) {
      m.postScale(ORIGINAL_SCALE, ORIGINAL_SCALE, getWidth() / 2, getHeight() / 2);
    } else {
      m.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
    }

    setImageMatrix(m);
  }

  public void restart() {
    int width = getWidth();
    int height = getHeight();

    if (width == 0 || height == 0) {
      return; // Can't call restart() when view area is zero.
    }

    updateViewport(width, height);
    updateDrawableBounds();
  }

  private boolean viewPortEmpty() {
    return mViewportRect == null || mViewportRect.isEmpty() || mViewportRect.width() <= 0 ||
        mViewportRect.height() <= 0;
  }

  private boolean drawableEmpty() {
    return mDrawableRect == null || mDrawableRect.isEmpty() || mDrawableRect.width() <= 0 ||
        mDrawableRect.height() <= 0;
  }


  private void updateViewport(float width, float height) {
    mViewportRect.set(0, 0, width, height);
    init();
  }

  private void updateDrawableBounds() {
    Drawable d = getDrawable();
    if (d != null && d.getIntrinsicHeight() > 0 && d.getIntrinsicWidth() > 0) {
      mDrawableRect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
    }
    init();
  }

  private void handleImageChange() {
    updateDrawableBounds();
    resetScales();
  }

  public void resetScales() {
    originalScale = ORIGINAL_SCALE;
    scale = ORIGINAL_SCALE;
  }

  public void pause() {
    kenBurnsState = KenBurnsState.PAUSED;
    invalidate();
  }

  public void disable() {
    kenBurnsState = KenBurnsState.DISABLED;
    invalidate();
  }

  public void stop() {
    kenBurnsState = KenBurnsState.STOPPED;
    invalidate();
  }

  public void resume() {
    kenBurnsState = KenBurnsState.RESUMED;
    invalidate();
  }

  public void reset() {
    resetScales();
  }

  public void setLoadId(String id) {
    this.loadID = id;
  }

  public String getLoadId() {
    return this.loadID;
  }
}

enum KenBurnsState {
  DISABLED,
  PAUSED,
  RESUMED,
  STOPPED
}
