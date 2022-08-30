/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview.fontview;

import android.graphics.Typeface;

/**
 * Listener to apply NH Fonts.
 *
 * @author chetan.urmaliya.
 */
public interface NHFontView {
  /**
   * Sets current typeface.
   */
  void setCurrentTypeface(Typeface currentTypeface);

  /**
   * Sets padding.
   */
  void setPadding(boolean isIndic);
}
