/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.common.view.customview;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Heigh Aware webview notifies when height of webview is changed.
 * @author neeraj.kumar
 *
 * @deprecated Do not use this class in any new code, will be removed shortly.
 */
public class HeightAwareWebView extends NhWebView {


  public HeightAwareWebView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public HeightAwareWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public HeightAwareWebView(Context context) {
    super(context);
  }
}
