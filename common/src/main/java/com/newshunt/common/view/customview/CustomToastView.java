/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newshunt.common.util.R;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;

/**
 * Created by anshul on 20/07/17.
 *
 * A custom view for showing toast. This is helpful when because of blocked notifications, toast
 * is not getting displayed.
 */

public class CustomToastView extends RelativeLayout {

  private String toastMessage;
  private int displayTime;
  private final static Handler handler = new Handler(Looper.getMainLooper());

  public CustomToastView(Context context) {
    super(context);
  }

  public CustomToastView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomToastView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private void setParams(String toastMessage, int displayTime) {
    this.toastMessage = toastMessage;
    this.displayTime = displayTime;
  }

  //Default custom toast
  public static void makeToast(final Context context, final String toastMessage, final int
      displayTime) {
    handler.post(
        () -> showToast(context, toastMessage, displayTime, R.layout.view_custom_toast, 0));
  }

  //Custom toast - change background and bottom margin
  public static void makeToast(final Context context, final String toastMessage, final int
      displayTime, final int layoutId, final int bottomMargin) {
    handler.post(() -> showToast(context, toastMessage, displayTime, layoutId, bottomMargin));
  }

  private static void showToast(Context context, String toastMessage, int displayTime, int
      layoutId, int bottomMargin) {
    if (context == null) {
      return;
    }
    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context
        .LAYOUT_INFLATER_SERVICE);
    CustomToastView view;
    try {
      view = (CustomToastView) layoutInflater.inflate(layoutId, null);
    } catch (Exception e) {
      return;
    }

    if (view == null) {
      return;
    }
    view.setParams(toastMessage, displayTime);

    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager
        .LayoutParams.TYPE_APPLICATION);
    layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
    layoutParams.format = PixelFormat.RGBA_8888;
    layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

    layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
    layoutParams.y = bottomMargin;
    try {
      windowManager.addView(view, layoutParams);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    final TextView textView = (TextView) findViewById(R.id.message);
    textView.setText((toastMessage));
    //FontHelper.setSpannableTextWithFont(textView, textView.getText().toString(),
        //FontType.NEWSHUNT_REGULAR);
    textView.setVisibility(View.VISIBLE);
    handler.postDelayed(() -> {
      if (textView != null) {
        textView.setText(Constants.EMPTY_STRING);
        setVisibility(GONE);
      }
    }, displayTime);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
    }
  }
}
