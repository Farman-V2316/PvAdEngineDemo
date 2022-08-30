/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.font;

import android.graphics.Typeface;

/**
 * Enum for all types of custom fonts which we are using in the app
 *
 * @author maruti.borker
 */
public enum FontType {

  // using otf:
  // http://kiteplans.info/2014/04/05/bug-embed-font-ttf-not-displaying-android-4-4-kitkat-4-3-jellybean/
  // http://stackoverflow.com/questions/20107072/custom-ttf-fonts-are-not-showing-properly-in-textview-on-android-4-4-kitkat
  NEWSHUNT_REGULAR("fonts/newshunt-regular.otf", Typeface.NORMAL),
  NEWSHUNT_BOLD("fonts/newshunt-bold.otf", Typeface.BOLD);

  private final String filename;

  public int getStyle() {
    return style;
  }

  private final int style;

  FontType(String filename, int style) {
    this.filename = filename;
    this.style = style;
  }

  public String getFilename() {
    return filename;
  }
}
