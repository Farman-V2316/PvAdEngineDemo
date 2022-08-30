/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview.fontview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.newshunt.common.helper.common.Logger;

/**
 * Variant of TextView without custom DH fonts.
 * Handle common errors with TextView
 * <p/>
 * Created by karthik on 06/02/18.
 */
public class NHSimpleTextView extends androidx.appcompat.widget.AppCompatTextView {
  public NHSimpleTextView(Context context) {
    super(context);
  }

  public NHSimpleTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NHSimpleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    try {
      return super.onTouchEvent(event);
    } catch (Exception ex) {
      Logger.caughtException(ex);
      // Catch Security Exception, Activity Not found exception, Malformed URL exception, NPE
    }

    // Consume the event after exception
    return true;
  }
}
