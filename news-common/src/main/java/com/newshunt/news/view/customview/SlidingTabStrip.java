/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Link to SlidingTabStrip code-
 * https://developer.android.com/samples/SlidingTabsBasic/src/com.example.android.common/view/SlidingTabStrip.html
 */

package com.newshunt.news.view.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.pages.GradientItem;
import com.newshunt.news.common.R;

import java.util.HashMap;
import java.util.List;

class SlidingTabStrip extends LinearLayout {

  private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 1;
  private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26;
  private static final int SELECTED_INDICATOR_THICKNESS_DIPS = 4;
  private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5;

  private final int mBottomBorderThickness;
  private final Paint mBottomBorderPaint;

  private int mSelectedIndicatorThickness;
  private final Paint mSelectedIndicatorPaint;

  private final int mDefaultBottomBorderColor;
  private final SimpleTabColorizer mDefaultTabColorizer;
  private int mSelectedPosition;
  private float mSelectionOffset;
  private SlidingTabLayout.TabColorizer mCustomTabColorizer;
  private SlidingTabLayout.SelectionPositionFinder mSelectionPositionFinder;
  private boolean drawBottomLine = true;
  private boolean drawSelectionIndicator = true;

  SlidingTabStrip(Context context) {
    this(context, null);
  }

  SlidingTabStrip(Context context, AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);

    final float density = getResources().getDisplayMetrics().density;

    TypedValue outValue = new TypedValue();
    context.getTheme().resolveAttribute(android.R.attr.colorForeground, outValue, true);
    final int themeForegroundColor = outValue.data;

    mDefaultBottomBorderColor = setColorAlpha(themeForegroundColor,
        DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);

    mDefaultTabColorizer = new SimpleTabColorizer();
    mDefaultTabColorizer.setIndicatorColor(0, DEFAULT_SELECTED_INDICATOR_COLOR);

    mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density);
    mBottomBorderPaint = new Paint();
    mBottomBorderPaint.setColor(mDefaultBottomBorderColor);

    mSelectedIndicatorThickness = (int) (SELECTED_INDICATOR_THICKNESS_DIPS * density);
    mSelectedIndicatorPaint = new Paint();
  }

  /**
   * Set the alpha value of the {@code color} to be the given {@code alpha} value.
   */
  private static int setColorAlpha(int color, byte alpha) {
    return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
  }

  /**
   * Blend {@code color1} and {@code color2} using the given ratio.
   *
   * @param ratio of which to blend. 1.0 will return {@code color1}, 0.5 will give an even blend,
   *              0.0 will return {@code color2}.
   */
  private static int blendColors(int color1, int color2, float ratio) {
    final float inverseRation = 1f - ratio;
    float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
    float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
    float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
    return Color.rgb((int) r, (int) g, (int) b);
  }

  void setCustomTabColorizer(SlidingTabLayout.TabColorizer customTabColorizer) {
    mCustomTabColorizer = customTabColorizer;
    invalidate();
  }

  void setCustomSelectionPositionFinder(SlidingTabLayout.SelectionPositionFinder selectionPositionFinder) {
    mSelectionPositionFinder = selectionPositionFinder;
    invalidate();
  }

  void setSelectedIndicatorColors(int position, List<GradientItem> gradItems, int color) {
    // Make sure that the custom colorizer is removed
    mCustomTabColorizer = null;
    if (gradItems != null) {
      if(gradItems.size()>1) {
        mDefaultTabColorizer.setGradientItems(position, gradItems);
      } else {
        mDefaultTabColorizer.setIndicatorColor(position, Color.parseColor(gradItems.get(0).getColor()));
      }
    } else {
      mDefaultTabColorizer.setIndicatorColor(position, color);
    }
    invalidate();
  }

  void onViewPagerPageChanged(int position, float positionOffset) {
    mSelectedPosition = position;
    mSelectionOffset = positionOffset;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    final int height = getHeight();
    final int childCount = getChildCount();
    final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
        ? mCustomTabColorizer
        : mDefaultTabColorizer;

    // Thick colored underline below the current selection
    if (childCount > 0) {

      Pair<Integer, Integer> positions = getViewPosition(mSelectedPosition);
      if (positions == null) {
        return;
      }

      int left = positions.first;
      int right = positions.second;

      List<GradientItem> gradItems = tabColorizer.getGradientItems(mSelectedPosition);
      int color = tabColorizer.getIndicatorColor(mSelectedPosition);

      if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {
        // Draw the selection partway between the tabs
        Pair<Integer, Integer> newPositions = getViewPosition(mSelectedPosition + 1);
        if (newPositions != null) {
          left = (int) (mSelectionOffset * newPositions.first + (1.0f - mSelectionOffset) * left);
          right = (int) (mSelectionOffset * newPositions.second + (1.0f - mSelectionOffset) * right);
        }
      }

      if (drawSelectionIndicator) {
        if(gradItems != null && gradItems.size()>1) {
          int[] colourArray = new int[gradItems.size()];
          float[] positionArray = new float[gradItems.size()];
          for (int j = 0; j < gradItems.size(); j++) {
            colourArray[j] = Color.parseColor(gradItems.get(j).getColor());
            positionArray[j] = gradItems.get(j).getPosition();
          }
          Shader textShader = new LinearGradient(left, 0, right, 0, colourArray, (positionArray.length > 0) ? positionArray : null, Shader.TileMode.CLAMP);
          mSelectedIndicatorPaint.setShader(textShader);
        }else {
          mSelectedIndicatorPaint.setShader(null);
          mSelectedIndicatorPaint.setColor(color);
        }
        canvas.drawRect(left, height - mSelectedIndicatorThickness, right, height, mSelectedIndicatorPaint);
      }
    }

    // Thin underline along the entire bottom edge
    if (drawBottomLine) {
      canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);
    }
  }

  private Pair<Integer, Integer> getViewPosition(int index) {
    View selectedTitle = getChildAt(index);
    if (selectedTitle == null) {
      return null;
    }

    if (mSelectionPositionFinder != null) {
      return mSelectionPositionFinder.getViewPosition(selectedTitle);
    }

    int parentLeft = selectedTitle.getLeft();
    int parentRight = selectedTitle.getRight();

    int childLeft = 0;
    int childRight = 0;

    if (selectedTitle instanceof ViewGroup) {
      childLeft = parentLeft + CommonUtils.getPixelFromDP(CommonUtils.getDimensionInDp(R.dimen.sliding_tab_strip_size), CommonUtils
              .getApplication());
      childRight = parentRight - CommonUtils.getPixelFromDP(CommonUtils.getDimensionInDp(R.dimen.sliding_tab_strip_size), CommonUtils
              .getApplication());
    }

    int left = Math.max(parentLeft, childLeft);
    int right = childRight > 0 ? Math.min(parentRight, childRight): parentRight;
    return new Pair<>(left, right);
  }

  /**
   * Added by datta.vitore
   * Displays the dot below the tab text based on selected sticky.
   */
  public void showTabHighlightAsImage(int position, int tabHighlightImageId, boolean show) {
    if (mSelectedPosition != 0 || tabHighlightImageId == View.NO_ID) {
      return;
    }
    View currentTabView = getChildAt(position);
    if (currentTabView == null) {
      return;
    }
    View highlightView = currentTabView.findViewById(tabHighlightImageId);
    if (highlightView == null) {
      return;
    }
    highlightView.setVisibility(show? View.VISIBLE : View.GONE);
  }

  public void setDrawBottomLine(boolean drawBottomLine) {
    this.drawBottomLine = drawBottomLine;
  }

  public void setDrawSelectionIndicator(boolean drawSelectionIndicator) {
    this.drawSelectionIndicator = drawSelectionIndicator;
  }

  public void setTabSelectionLineHeight(int tabSelectionLineHeight) {
    this.mSelectedIndicatorThickness = tabSelectionLineHeight;
  }

  /**
   * Added by datta.vitore
   * Hide the dot below tab text.
   *
   * @param position
   */
  public void hideCoachMarkDot(int position) {
    View nextTitle = getChildAt(position);
    TextView textView = (TextView) nextTitle;
    textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
        null);
    textView.refreshDrawableState();
  }

  private static class SimpleTabColorizer implements SlidingTabLayout.TabColorizer {
    HashMap<Integer, Integer> mIndicatorColors = new HashMap<>();
    HashMap<Integer, List<GradientItem>> mGradientItems = new HashMap<>();


    @Override
    public final int getIndicatorColor(int position) {
      return mIndicatorColors.get(position);
    }

    void setIndicatorColor(int position, int color) {
      mIndicatorColors.put(position, color);
    }

    @Override
    public final List<GradientItem> getGradientItems(int position) {
      if(mGradientItems.containsKey(position)) {
        return mGradientItems.get(position);
      } else
        return null;
    }

    void setGradientItems(int position, List<GradientItem> items) {
      mGradientItems.put(position, items);
    }
  }
}