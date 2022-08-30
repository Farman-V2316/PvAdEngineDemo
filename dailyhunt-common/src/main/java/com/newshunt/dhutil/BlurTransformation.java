/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.newshunt.helper.FastBlur;

import java.security.MessageDigest;

/**
 * Created by anshul on 22/11/17.
 * A class for providing blur transformation to images used through Picasso.
 */


public class BlurTransformation extends BitmapTransformation {

  private static int MAX_RADIUS = 70;
  private static int DEFAULT_DOWN_SAMPLING = 1;

  private int radius;
  private int sampling;

  public BlurTransformation() {
    this(MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
  }

  public BlurTransformation(int downSampling) {
    this(MAX_RADIUS, downSampling);
  }

  public BlurTransformation(int radius, int sampling) {
    this.radius = radius;
    this.sampling = sampling;
  }

  @Override protected Bitmap transform(@NonNull BitmapPool pool,
                                       @NonNull Bitmap toTransform, int outWidth, int outHeight) {

    int width = toTransform.getWidth();
    int height = toTransform.getHeight();
    int scaledWidth = width / sampling;
    int scaledHeight = height / sampling;

    Bitmap bitmap = pool.get(scaledWidth, scaledHeight, Bitmap.Config.RGB_565);

    Canvas canvas = new Canvas(bitmap);
    canvas.scale(1 / (float) sampling, 1 / (float) sampling);
    Paint paint = new Paint();
    paint.setFlags(Paint.FILTER_BITMAP_FLAG);
    canvas.drawBitmap(toTransform, 0, 0, paint);

    bitmap = FastBlur.blur(bitmap, radius, true);

    return bitmap;
  }

  @Override
  public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

  }
}