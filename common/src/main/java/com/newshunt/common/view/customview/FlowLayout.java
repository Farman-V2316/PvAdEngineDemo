package com.newshunt.common.view.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by santosh.kumar on 5/2/2016.
 */
public class FlowLayout extends ViewGroup {

  public static final int DIRECTION_LEFT_TO_RIGHT = 1;
  public static final int DIRECTION_RIGHT_TO_LEFT = 2;

  private int line_height;
  private int direction = DIRECTION_LEFT_TO_RIGHT;

  public static class LayoutParams extends ViewGroup.LayoutParams {

    public final int horizontal_spacing;
    public final int vertical_spacing;

    /**
     * @param horizontal_spacing Pixels between items, horizontally
     * @param vertical_spacing   Pixels between items, vertically
     */
    public LayoutParams(int horizontal_spacing, int vertical_spacing) {
      super(0, 0);
      this.horizontal_spacing = horizontal_spacing;
      this.vertical_spacing = vertical_spacing;
    }
  }


  public FlowLayout(Context context) {
    super(context);
  }

  public FlowLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);

    final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
    int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
    final int count = getChildCount();
    int line_height = 0;

    int childHeightMeasureSpec;
    if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
      childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
    } else {
      childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    }
    int xpos = 0;
    int ypos = getPaddingTop();
    if (direction == DIRECTION_LEFT_TO_RIGHT) {
      xpos = getPaddingLeft();
      for (int i = 0; i < count; i++) {
        final View child = getChildAt(i);
        if (child.getVisibility() != GONE) {
          final LayoutParams lp = (LayoutParams) child.getLayoutParams();
          child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
              childHeightMeasureSpec);
          final int childw = child.getMeasuredWidth();
          line_height = Math.max(line_height, child.getMeasuredHeight() + lp.vertical_spacing);

          if (xpos + childw > width) {
            xpos = getPaddingLeft();
            ypos += line_height;
          }

          xpos += childw + lp.horizontal_spacing;
        }
      }
    } else {
      xpos = width + getPaddingLeft();
      for (int i = 0; i < count; i++) {
        final View child = getChildAt(i);
        if (child.getVisibility() != GONE) {
          final LayoutParams lp = (LayoutParams) child.getLayoutParams();
          child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
              childHeightMeasureSpec);
          final int childw = child.getMeasuredWidth();
          line_height = Math.max(line_height, child.getMeasuredHeight() + lp.vertical_spacing);

          if (i == 0) {
            xpos = xpos - childw;
          } else {
            if (xpos - childw < getPaddingLeft()) {
              xpos = width + getPaddingLeft() - childw;
              ypos += line_height;
            } else {
              xpos = xpos - childw - lp.horizontal_spacing;
            }

          }

        }

      }
    }
    this.line_height = line_height;

    if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
      height = ypos + line_height;

    } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
      if (ypos + line_height < height) {
        height = ypos + line_height;
      }
    }
    setMeasuredDimension(width, height);


  }

  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(1, 1); // default of 1px spacing
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    if (p instanceof LayoutParams) {
      return true;
    }
    return false;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final int count = getChildCount();
    final int width = r - l;
//        for (int i = 0; i < count; i++) {
//            final View child = getChildAt(i);
//            if (child.getVisibility() != GONE) {
//                final int childw = child.getMeasuredWidth();
//                final int childh = child.getMeasuredHeight();
//                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
//                if (xpos + childw > width) {
//                    xpos = getPaddingLeft();
//                    ypos += line_height;
//                }
//                child.layout(xpos, ypos, xpos + childw, ypos + childh);
//                xpos += childw + lp.horizontal_spacing;
//            }
//        }

    int xpos = 0;
    int ypos = getPaddingTop();
    if (direction == DIRECTION_LEFT_TO_RIGHT) {
      xpos = getPaddingLeft();
      for (int i = 0; i < count; i++) {
        final View child = getChildAt(i);
        if (child.getVisibility() != GONE) {
          final int childw = child.getMeasuredWidth();
          final int childh = child.getMeasuredHeight();
          final LayoutParams lp = (LayoutParams) child.getLayoutParams();
          line_height = Math.max(line_height, child.getMeasuredHeight() + lp.vertical_spacing);

          if (xpos + childw > width) {
            xpos = getPaddingLeft();
            ypos += line_height;
          }
          child.layout(xpos, ypos, xpos + childw, ypos + childh);
          xpos += childw + lp.horizontal_spacing;
        }
      }
    } else {
      xpos = width + getPaddingLeft();
      for (int i = 0; i < count; i++) {
        final View child = getChildAt(i);
        if (child.getVisibility() != GONE) {
          final int childw = child.getMeasuredWidth();
          final int childh = child.getMeasuredHeight();
          final LayoutParams lp = (LayoutParams) child.getLayoutParams();
          line_height = Math.max(line_height, child.getMeasuredHeight() + lp.vertical_spacing);

          if (i == 0) {
            xpos = xpos - childw;
          } else {
            if (xpos - childw < getPaddingLeft()) {
              xpos = width + getPaddingLeft() - childw;
              ypos += line_height;
            } else {
              xpos = xpos - childw - lp.horizontal_spacing;
            }

          }

          child.layout(xpos, ypos, xpos + childw, ypos + childh);

        }

      }
    }
  }

  public void setDirection(int direction) {
    this.direction = direction;
    requestLayout();
  }
}
