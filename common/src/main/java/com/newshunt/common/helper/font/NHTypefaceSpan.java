package com.newshunt.common.helper.font;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

import com.newshunt.common.helper.common.Logger;

/**
 * Custom typeface span to take care of applying the typeface to paint object when callbacks for
 * update draw state or measure call is made by the system
 * <p/>
 *
 * @author amit.kankani
 */
class NHTypefaceSpan extends TypefaceSpan {

  private final Typeface newType;

  public NHTypefaceSpan(String family, Typeface type) {
    super(family);
    newType = type;
  }

  @Override
  public void updateDrawState(TextPaint ds) {
    applyCustomTypeFace(ds, newType);
  }

  @Override
  public void updateMeasureState(TextPaint paint) {
    applyCustomTypeFace(paint, newType);
  }

  private static void applyCustomTypeFace(Paint paint, Typeface tf) {
    try {
      if (null == tf || null == paint) {
        return;
      }

      int oldStyle;
      Typeface old = paint.getTypeface();
      if (old == null) {
        oldStyle = 0;
      } else {
        oldStyle = old.getStyle();
      }

      int fake = oldStyle & ~tf.getStyle();
      if ((fake & Typeface.BOLD) != 0) {
        paint.setFakeBoldText(true);
      }

      if ((fake & Typeface.ITALIC) != 0) {
        paint.setTextSkewX(-0.25f);
      }

      paint.setTypeface(tf);
    } catch (Exception e) {
      // blanket try catch - because even after null checks - we found the app to be crashing
      // at times in crashlytics in previous app
      // Just for font problem on some devices, don't want to have crash everywhere
      // If we hit this exception, on devices with font problem only, the problem will remain
      // for the particular call only
      Logger.e("NHTypefaceSpan::applyCustomTypeFace", e.toString());
    }
  }
}
