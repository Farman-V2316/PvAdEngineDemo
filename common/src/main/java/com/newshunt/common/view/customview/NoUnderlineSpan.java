package com.newshunt.common.view.customview;

import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;

/**
 * Class to avoid/remove underline from spannable string
 *
 * @author anand.winjit
 */
public class NoUnderlineSpan extends UnderlineSpan {
  public NoUnderlineSpan() {
  }

  public NoUnderlineSpan(Parcel src) {
  }

  @Override
  public void updateDrawState(TextPaint ds) {
    ds.setUnderlineText(false);
  }
}