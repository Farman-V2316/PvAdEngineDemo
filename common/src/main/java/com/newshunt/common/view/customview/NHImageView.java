/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.newshunt.common.util.R;
import com.newshunt.dataentity.common.view.customview.FIT_TYPE;
import com.newshunt.sdk.network.image.Image;
import com.newshunt.sdk.network.image.OnImageLoadListener;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Custom ImageView to crop the image according to FIT_TYPE
 *
 * @author arun.babu
 */
@SuppressLint("AppCompatCustomView")
public class NHImageView extends ImageView implements OnImageLoadListener {
  public static final float PORT_RATIO_LIMIT = (float) (3.0 / 4.0);
  public static final float WID_RATIO_LIMIT = 1;
  public static final float LAND_RATIO_LIMIT = (float) (4.25 / 3.0);

  private boolean enableOverlay = false;

  private FIT_TYPE mFitType = FIT_TYPE.CENTER_CROP;
  private Paint paint;
  private OnImageLoadListener loadListenerDelegate;

  public NHImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(attrs);
  }

  public NHImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public NHImageView(Context context) {
    super(context);
    init(null);
  }

  public void setFitType(FIT_TYPE type) {
    mFitType = type;
  }

  public void enableOverlay(boolean enableOverlay) {
    this.enableOverlay = enableOverlay;
  }

  @Override
  protected boolean setFrame(int viewL, int viewT, int viewR, int viewB) {
    Drawable drawable = getDrawable();
    if (null == drawable || drawable instanceof NinePatchDrawable || mFitType == FIT_TYPE.FIT_XY) {
      if (mFitType == FIT_TYPE.FIT_XY) {
        setScaleType(ScaleType.FIT_XY);
      }
      return super.setFrame(viewL, viewT, viewR, viewB);
    }

    setScaleMatrix(viewR - viewL, viewB - viewT, drawable.getIntrinsicWidth(),
        drawable.getIntrinsicHeight());
    return super.setFrame(viewL, viewT, viewR, viewB);
  }

  @Override
  public void setImageDrawable(@Nullable Drawable drawable) {
    if (drawable == null || mFitType == null || drawable instanceof NinePatchDrawable) {
      super.setImageDrawable(drawable);
      return;
    }

    float imgWid = drawable.getIntrinsicWidth();
    float imgHei = drawable.getIntrinsicHeight();
    setScaleMatrix(getWidth(), getHeight(), imgWid, imgHei);
    super.setImageDrawable(drawable);
  }

  private void setScaleMatrix(float viewWid, float viewHei, float imgWid, float imgHei) {
    float scaleFactor = 1;
    float transX;
    float transY;
    float newWid;
    float newHei;
    boolean heightChanged = false;

    switch (mFitType) {
      case TOP_CROP:
      case CENTER_CROP:
        float fitHorFactor = viewWid / imgWid;
        float fitVerFactor = viewHei / imgHei;
        scaleFactor = Math.max(fitHorFactor, fitVerFactor);
        break;
      case FIT_DISP_HEI:
        float imgRatio = imgWid / imgHei;
        if (imgRatio < PORT_RATIO_LIMIT) {
          viewHei = viewWid / PORT_RATIO_LIMIT;
          fitHorFactor = viewWid / imgWid;
          fitVerFactor = viewHei / imgHei;
          scaleFactor = Math.min(fitHorFactor, fitVerFactor);
        } else {
          viewHei = viewWid / imgRatio;
          scaleFactor = viewWid / imgWid;
        }
        heightChanged = true;
        break;
      case FIT_DISP_WID:
        imgRatio = imgWid / imgHei;
        viewHei = viewWid / LAND_RATIO_LIMIT;
        fitHorFactor = viewWid / imgWid;
        fitVerFactor = viewHei / imgHei;
        if (imgRatio > WID_RATIO_LIMIT) {
          scaleFactor = Math.max(fitHorFactor, fitVerFactor);
        } else {
          scaleFactor = Math.min(fitHorFactor, fitVerFactor);
        }
        heightChanged = true;
        break;
      case FIT_ASTRO:
        // For Astro, Image Height has to be third of width
        // Note no break here. FIT_CENTER logic also required
        viewHei = viewWid / 3;
      case FIT_CENTER:
        fitHorFactor = viewWid / imgWid;
        fitVerFactor = viewHei / imgHei;
        scaleFactor = Math.min(fitHorFactor, fitVerFactor);
        break;
      default:
        break;
    }

    newWid = imgWid * scaleFactor;
    newHei = imgHei * scaleFactor;
    transX = (viewWid - newWid) / 2;

    if (FIT_TYPE.TOP_CROP == mFitType || FIT_TYPE.FIT_DISP_WID == mFitType) {
      transY = 0;
    } else {
      transY = (viewHei - newHei) / 2;
    }

    Matrix matrix = getImageMatrix();
    matrix.setScale(scaleFactor, scaleFactor, 0, 0);
    matrix.postTranslate(transX, transY);
    setImageMatrix(matrix);
    if (heightChanged) {
      final int height = (int) viewHei;
      post(new NHRunnable(this, height));
    }
  }

  private void init(AttributeSet attrs) {

    if (isInEditMode()) {
      return;
    }

    if (getScaleType() == ScaleType.FIT_XY) {
      mFitType = FIT_TYPE.FIT_XY;
    } else {
      setScaleType(ScaleType.MATRIX);
    }

    if (attrs != null) {
      TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.NHImageView, 0, 0);
      try {
        enableOverlay = ta.getBoolean(R.styleable.NHImageView_enableOverlay, false);
      } finally {
        ta.recycle();
      }
    }

    paint = new Paint();
    paint.setColor(getResources().getColor(R.color.image_overlay));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (enableOverlay) {
      // Draw overlay over the image
      canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
  }

  private static class NHRunnable implements Runnable {

    private WeakReference<View> view;
    private int height;

    NHRunnable(View view, int height) {
      this.view = new WeakReference<>(view);
      this.height = height;
    }

    @Override
    public void run() {
      View weakView = view.get();
      if (null == weakView) {
        return;
      }
      ViewGroup.LayoutParams params = weakView.getLayoutParams();
      params.width = ViewGroup.LayoutParams.MATCH_PARENT;
      params.height = height;
      weakView.setLayoutParams(params);
    }
  }

  @Override
  public void onSuccess(Object resource) {
    if (loadListenerDelegate != null) {
      loadListenerDelegate.onSuccess(resource);
    }
  }

  @Override
  public void onError() {
    if (loadListenerDelegate != null) {
      loadListenerDelegate.onError();
    }
  }

  public Image.Loader load(String imageLoc) {
    NhImageLoader loader = new NhImageLoader(imageLoc);
    return loader;
  }

  class NhImageLoader extends Image.Loader {

    public NhImageLoader(String imagePath) {
      super(imagePath);
    }

    public NhImageLoader(File file) {
      super(file);
    }

    @Override
    public void into(ImageView imageView, ScaleType scaleType) {
      into(NHImageView.this, null, scaleType);
    }

    public void into(ImageView imageView, OnImageLoadListener onImageLoadListener,
                     ScaleType scaleType) {
      loadListenerDelegate = onImageLoadListener;
      super.into(NHImageView.this, NHImageView.this, scaleType);
    }

    public void into(ImageView imageView, OnImageLoadListener onImageLoadListener) {
      into(NHImageView.this, onImageLoadListener, null);
    }
  }
}
