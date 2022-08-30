/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.font;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches fonts in memory for re-usability
 *
 * @author maruti.borker
 */
public class FontCache {
  private Map<FontType, Typeface> cache;
  private static volatile FontCache instance;

  private FontCache() {
    cache = new HashMap<>();
  }

  public static FontCache getInstance() {
    if (instance == null) {
      synchronized (FontCache.class) {
        if (instance == null) {
          instance = new FontCache();
        }
      }
    }
    return instance;
  }

  public Typeface getFontTypeFace(Context context, FontType fontType) {
    if (cache.containsKey(fontType)) {
      return cache.get(fontType);
    }
    Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontType.getFilename());
    cache.put(fontType, typeface);
    return typeface;
  }
}
