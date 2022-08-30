package com.newshunt.dhutil.view.customview;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.newshunt.common.helper.common.Logger;

/**
 * Created by santoshkulkarni on 08/08/16.
 */
public class CustomViewPager extends ViewPager {

  private boolean enabled;

  public CustomViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.enabled = true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    try {
      if (this.enabled) {
        return super.onTouchEvent(event);
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return false;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    try {
      if (this.enabled) {
        return super.onInterceptTouchEvent(event);
      }
    } catch (Exception e) {
    }
    return false;
  }

  public boolean getPagingEnabled() {
    return enabled;
  }

  public void setPagingEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}