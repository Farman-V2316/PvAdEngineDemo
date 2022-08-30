package com.newshunt.adengine.view.viewholder;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom View pager to adjust height of view pager item according to its content.
 *
 * @author heena.arora
 */
public class AdsViewPager extends ViewPager {

  public AdsViewPager(Context context) {
    super(context);
  }

  public AdsViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int height = 0;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
      int h = child.getMeasuredHeight();
      if (h > height) {
        height = h;
      }
    }

    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}
