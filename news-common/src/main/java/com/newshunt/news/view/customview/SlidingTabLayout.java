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
 */

package com.newshunt.news.view.customview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.TabClickEvent;
import com.newshunt.dataentity.common.pages.CampaignMeta;
import com.newshunt.dataentity.common.pages.GradientItem;
import com.newshunt.dhutil.helper.BoldStyleHelper;
import com.newshunt.dhutil.helper.theme.ThemeUtils;
import com.newshunt.news.analytics.NhAnalyticsAppState;
import com.newshunt.news.common.R;
import com.newshunt.news.model.repo.CardSeenStatusRepo;
import com.newshunt.news.view.adapter.SlidingTabLayoutAdapter;
import com.newshunt.sdk.network.image.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p/>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link Fragment} call
 * {@link #setViewPager(ViewPager)} providing it the ViewPager this layout
 * is being used for.
 * <p/>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int, List, int)}. The
 * alternative is via the {@link SlidingTabLayout.TabColorizer}
 * interface which provides you complete control over
 * which color is used for any individual position.
 * <p/>
 * The views used as tabs can be customized by calling {@link #setCustomTabView(int, int, int)},
 * providing the layout ID of your custom layout.
 */
public class SlidingTabLayout extends HorizontalScrollView {

  private static final int TITLE_OFFSET_DIPS = 42;
  private static final int TAB_VIEW_PADDING_DIPS = 6;
  private static final int TAB_VIEW_LR_PADDING_DIPS = 12;
  private static final int TAB_VIEW_TEXT_SIZE_DP = 14;
  private final SlidingTabStrip mTabStrip;
  private ArrayList<View> headerViews;
  private int mTitleOffset;
  private int selectedTabTextcolor;
  private int unselectedTabTextcolor;
  private ColorStateList colorSelector;

  private int mTabViewLayoutId;
  private int mTabViewTextViewId;
  private int mTabImageViewId;
  private int mTabHighlightImageId;
  private int mTabViewIconId;
  private boolean mDistributeEvenly;
  private boolean displayDefaultIconForEmptyTitle;

  public static final MutableLiveData<TabClickEvent> tabClickEventLiveData =
      new MutableLiveData<>();

  private ViewPager mViewPager;
  private SparseArray<String> mContentDescriptions = new SparseArray<String>();
  private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;
  private OnTabClickListener tabClickListener;
  private View extraView;
  private View.OnClickListener extraViewClickListener;
  private SlidingTabScrollListener slidingTabScrollListener;
  private boolean disableScroll;

  /**
   * Set custom view binders to customize {@link SlidingTabLayout}
   */
  public void setViewBinder(ViewBinder viewBinder) {
    this.viewBinder = viewBinder;
  }

  private ViewBinder viewBinder;

  /**
   * Use to disable/enabled auto scroll. This can be used to avoid autoscroll on reloading contents.
   */
  public void setDisableScroll(boolean disableScroll) {
    this.disableScroll = disableScroll;
  }

  public SlidingTabLayout(Context context) {
    this(context, null);
  }

  public SlidingTabLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    // For getting all view of tabs
    headerViews = new ArrayList<View>();
    // Disable the Scroll Bar
    setHorizontalScrollBarEnabled(false);
    // Make sure that the Tab Strips fills this View
    setFillViewport(true);

    setTitleOffet(TITLE_OFFSET_DIPS, context);

    mTabStrip = new SlidingTabStrip(context);
    if (ThemeUtils.isNightMode()) {
      mTabStrip.setBackgroundColor(CommonUtils.getColor(R.color.edit_text_fill_color_night));
    }
    addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  /**
   * Set the custom {@link SlidingTabLayout.TabColorizer} to be used.
   * <p/>
   * If you only require simple custmisation then you can use
   * {@link #setSelectedIndicatorColors(int, List, int)} to achieve
   * similar effects.
   */
  public void setCustomTabColorizer(TabColorizer tabColorizer) {
    mTabStrip.setCustomTabColorizer(tabColorizer);
  }

  public void setCustomSelectionPositionFinder(SelectionPositionFinder selectionPositionFinder) {
    mTabStrip.setCustomSelectionPositionFinder(selectionPositionFinder);
  }

  public void setSlidingTabScrollListener(SlidingTabScrollListener slidingTabScrollListener) {
    this.slidingTabScrollListener = slidingTabScrollListener;
  }

  public void setDistributeEvenly(boolean distributeEvenly) {
    mDistributeEvenly = distributeEvenly;
  }

  public void setDrawBottomLine(boolean drawBottomLine) {
    mTabStrip.setDrawBottomLine(drawBottomLine);
  }

  public void setDrawSelectionIndicator(boolean drawSelectionIndicator) {
    mTabStrip.setDrawSelectionIndicator(drawSelectionIndicator);
  }

  public void setTabSelectionLineHeight(int height) {
    mTabStrip.setTabSelectionLineHeight(height);
  }

  public void setLayoutGravity(int gravity) {
    mTabStrip.setGravity(gravity);
  }

  /**
   * Sets the colors to be used for indicating the selected tab. These colors are treated as a
   * circular array. Providing one color will mean that all tabs are indicated with the same color.
   */
  public void setSelectedIndicatorColors(int position, List<GradientItem> gradItems, int color) {
    mTabStrip.setSelectedIndicatorColors(position, gradItems, color);
  }

  public void displayDotAsImage(int position) {
    mTabStrip.showTabHighlightAsImage(position, mTabHighlightImageId, true);
  }

  public void hideDotAsImage(int position) {
    mTabStrip.showTabHighlightAsImage(position, mTabHighlightImageId, false);
  }

  /**
   * Added by datta.vitore
   * call to hide dot coachmark in home screen.
   *
   * @param position
   */
  public void hideDot(int position) {
    mTabStrip.hideCoachMarkDot(position);
  }

  /**
   * required to set any {@link ViewPager.OnPageChangeListener}
   * through this method. This is so
   * Set the {@link ViewPager.OnPageChangeListener}.
   * When using {@link SlidingTabLayout} you are
   * that the layout can update it's scroll position correctly.
   *
   * @see ViewPager#setOnPageChangeListener
   * (android.support.v4.view.ViewPager.OnPageChangeListener)
   */
  public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
    mViewPagerPageChangeListener = listener;
  }

  /**
   * required to display the default icon if the title of the tab is empty
   *
   * @param displayDefaultIconForEmptyTitle
   */
  public void setDisplayDefaultIconForEmptyTitle(boolean displayDefaultIconForEmptyTitle) {
    this.displayDefaultIconForEmptyTitle = displayDefaultIconForEmptyTitle;
  }

  /**
   * Set the custom layout to be inflated for the tab views.
   *
   * @param layoutResId Layout id to be inflated
   * @param textViewId  id of the {@link android.widget.TextView} in the inflated view
   */
  public void setCustomTabView(int layoutResId, int textViewId, int imageViewId) {
    setCustomTabView(layoutResId, textViewId, imageViewId, View.NO_ID);
  }

  public void setCustomTabView(int layoutResId, int textViewId, int imageViewId, int
      iconViewId) {
    mTabViewLayoutId = layoutResId;
    mTabViewTextViewId = textViewId;
    mTabImageViewId = imageViewId;
    mTabViewIconId = iconViewId;
  }

  /**
   * Sets the associated view pager. Note that the assumption here is that the pager content
   * (number of tabs and tab titles) does not change after this call has been made.
   */
  public void setViewPager(ViewPager viewPager) {
    mTabStrip.removeAllViews();

    mViewPager = viewPager;
    if (viewPager != null) {
      viewPager.setOnPageChangeListener(new InternalViewPagerListener());
      populateTabStrip();
    }
  }

  /**
   * Append more items to the sliding tab without disturbing existing views and their scroll
   * position.
   */
  public void appendItems(int from, int to) {
    final PagerAdapter adapter = mViewPager.getAdapter();
    final OnClickListener tabClickListener = new TabClickListener();

    for (int i = from; i <= to; i++) {
      addItem(i, adapter, tabClickListener);
    }
  }

  /**
   * Create a default view to be used for tabs. This is called if a custom tab view is not set via
   * {@link #setCustomTabView(int, int, int)}.
   */
  protected TextView createDefaultTabView(Context context) {
    NHTextView textView = new NHTextView(context);
    textView.setGravity(Gravity.CENTER);
    textView.setSingleLine(true);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TAB_VIEW_TEXT_SIZE_DP);
    textView.setLayoutParams(new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

    TypedValue outValue = new TypedValue();
    getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
        outValue, true);
    textView.setBackgroundResource(outValue.resourceId);

    int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
    int lr_padding = (int) (TAB_VIEW_LR_PADDING_DIPS * getResources().getDisplayMetrics().density);
    textView.setPadding(lr_padding, padding, lr_padding, padding);

    return textView;
  }

  public void setTabTextColor(int selectedTabTextcolor, int unselectedTabTextcolor) {
    this.selectedTabTextcolor = selectedTabTextcolor;
    this.unselectedTabTextcolor = unselectedTabTextcolor;
    setTabTextColor();
  }

  private void setTabTextColor() {

    int[][] states = new int[][]{
        new int[]{android.R.attr.state_selected}, // selected
        new int[]{android.R.attr.state_focused},  // focused
        new int[]{android.R.attr.state_pressed},  // pressed
        new int[]{-android.R.attr.state_selected}, // unselected
        new int[]{-android.R.attr.state_focused}, // not focused
        new int[]{-android.R.attr.state_pressed},  // unpressed

    };

    int[] colors = new int[]{
        selectedTabTextcolor,
        selectedTabTextcolor,
        selectedTabTextcolor,
        unselectedTabTextcolor,
        unselectedTabTextcolor,
        unselectedTabTextcolor
    };

    colorSelector = new ColorStateList(states, colors);
  }

  /**
   * Added by datta.vitore
   *
   * @return list of views in tab
   */
  public ArrayList<View> getHeaderViews() {
    return headerViews;
  }

  private void populateTabStrip() {
    final PagerAdapter adapter = mViewPager.getAdapter();
    final OnClickListener tabClickListener = new TabClickListener();
    for (int i = 0; i < adapter.getCount(); i++) {
      addItem(i, adapter, tabClickListener);
    }

    if (extraView != null) {
      mTabStrip.addView(extraView);
    }
  }

  private void addItem(int i, PagerAdapter adapter, final OnClickListener tabClickListener) {
    View tabView = null;
    NHTextView tabTitleView = null;
    ImageView tabImageView = null;
    ImageView tabIconView = null;

    if (mTabViewLayoutId != 0) {
      // If there is a custom tab view layout id set, try and inflate it
      tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip, false);
    }

    if (tabView == null) {
      tabView = createDefaultTabView(getContext());
    } else if (viewBinder != null) {
      viewBinder.bindView(tabView, i);
    }

    if (mTabViewTextViewId != 0) {
      tabTitleView = tabView.findViewById(mTabViewTextViewId);
      tabImageView = tabView.findViewById(mTabImageViewId);
    }

    if(mTabViewIconId != 0) {
      tabIconView = tabView.findViewById(mTabViewIconId);
    }

    if (tabTitleView == null && NHTextView.class.isInstance(tabView)) {
      tabTitleView = (NHTextView) tabView;
    }

    if (mDistributeEvenly) {
      LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabView.getLayoutParams();
      lp.width = 0;
      lp.weight = 1;
    }
    setSelectedIndicatorColors(i, null, selectedTabTextcolor);
    if (tabTitleView != null) {
      String tabTitle = Constants.EMPTY_STRING;
      if (adapter.getPageTitle(i) != null) {
        tabTitle = adapter.getPageTitle(i).toString();
      }

      //setting the default icon if there is no text to display in the tab
      if (displayDefaultIconForEmptyTitle && tabImageView != null &&
          TextUtils.isEmpty(tabTitle)) {
        tabImageView.setVisibility(View.VISIBLE);
        tabTitleView.setVisibility(View.GONE);
      } else if (tabImageView != null && adapter instanceof SlidingTabLayoutAdapter) {
        SlidingTabLayoutAdapter slidingTabLayoutAdapter = (SlidingTabLayoutAdapter) adapter;
        String pageIconUrl = slidingTabLayoutAdapter.getPageIconUrl(i);
        if (pageIconUrl != null) {
          Image.load(pageIconUrl).into(tabImageView);
          tabImageView.setVisibility(View.VISIBLE);
        }
        tabImageView.setImageTintMode(PorterDuff.Mode.SRC_IN);
        if (i == mViewPager.getCurrentItem()) {
          tabImageView.setImageTintList(
              ColorStateList.valueOf(getResources().getColor(R.color.white_color)));
        } else {
          tabImageView.setImageTintList(
              ColorStateList.valueOf(
                  getResources().getColor(R.color.explore_unselected_overlay)));
        }

        slidingTabLayoutAdapter.applyCustomStyles(tabView, i);
      }
      if (colorSelector != null) {
        tabTitleView.setTextColor(colorSelector);
      }
      tabTitleView.setText((tabTitle));

      if (adapter instanceof SlidingTabLayoutAdapter) {
        CampaignMeta item = ((SlidingTabLayoutAdapter) adapter).getCampaignMetaItem(i);
        Long publishTime = (item != null) ? item.getPublishTime() : null;
        Long expiryTime = (item != null) ? item.getExpiryTime() : null;
        if (publishTime != null && publishTime < System.currentTimeMillis() && expiryTime != null && expiryTime > System.currentTimeMillis()) {
          List<GradientItem> gradientColor = item.getColourGradient();
          List<GradientItem> indicatorColor = item.getIndicatorColour();

          if (gradientColor != null) {
            if(gradientColor.size()>1) {               // for gradient to work, minimum 2 colours are required.
              TextPaint paint = tabTitleView.getPaint();
              float width = paint.measureText(tabTitleView.getText().toString());
              int[] colourArray = new int[gradientColor.size()];
              float[] positionArray = new float[gradientColor.size()];

              for (int j = 0; j < gradientColor.size(); j++) {
                colourArray[j] = Color.parseColor(gradientColor.get(j).getColor());
                positionArray[j] = gradientColor.get(j).getPosition();
              }
              Shader textShader = new LinearGradient(0, 0, width, 0,
                      colourArray, (positionArray.length > 0) ? positionArray : null, Shader.TileMode.CLAMP);
              tabTitleView.getPaint().setShader(textShader);
            } else {
              tabTitleView.setTextColor(Color.parseColor(gradientColor.get(0).getColor()));
            }
          }
          if (indicatorColor != null) {
            if(indicatorColor.size() > 1){              // for gradient to work, minimum 2 colours are required.
              setSelectedIndicatorColors(i, indicatorColor, -1);
            } else {
              setSelectedIndicatorColors(i, null, Color.parseColor(indicatorColor.get(0).getColor()));
            }
          }
          String iconUrl = item.getIconUrl();
          if (!CommonUtils.isEmpty(iconUrl) && tabIconView != null) {
            tabIconView.setVisibility(View.VISIBLE);
            Image.load(iconUrl).placeHolder(R.drawable.ic_profile).into(tabIconView);
            tabTitleView.setPadding(CommonUtils.getDimension(R.dimen.tab_text_padding_start), CommonUtils.getDimension(R.dimen.tab_vertical_padding), 0, CommonUtils.getDimension(R.dimen.tab_layout_top_padding));
          }

        }
      }
      BoldStyleHelper.setBoldTextForBigCardIfApplicable(tabTitleView);
      headerViews.add(tabTitleView);
    }
    tabView.setOnClickListener(tabClickListener);
    String desc = mContentDescriptions.get(i, null);
    if (desc != null) {
      tabView.setContentDescription(desc);
    }
    mTabStrip.addView(tabView);
    if (i == mViewPager.getCurrentItem()) {
      tabView.setSelected(true);
    }
  }

  public void setContentDescription(int i, String desc) {
    mContentDescriptions.put(i, desc);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (mViewPager != null) {
      scrollToTab(mViewPager.getCurrentItem(), 0);
    }
  }

  public void scrollToTab(int tabIndex, int positionOffset) {
    if (disableScroll) {
      return;
    }

    final int tabStripChildCount = mTabStrip.getChildCount();
    if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
      return;
    }

    View selectedChild = mTabStrip.getChildAt(tabIndex);
    if (selectedChild != null) {
      int targetScrollX = selectedChild.getLeft() + positionOffset;

      if (tabIndex > 0 || positionOffset > 0) {
        // If we're not at the first child and are mid-scroll, make sure we obey the offset
        targetScrollX -= mTitleOffset;
      }

      scrollTo(targetScrollX, 0);
    }
  }

  public View getTabViewAt(int position) {
    if (mTabStrip.getChildCount() == 0 || position < 0 || position >= mTabStrip.getChildCount()) {
      return null;
    }

    return mTabStrip.getChildAt(position);
  }

  public void setTabClickListener(OnTabClickListener tabClickListener) {
    this.tabClickListener = tabClickListener;
  }

  /**
   * Allows complete control over the colors drawn in the tab layout. Set with
   * {@link #setCustomTabColorizer(SlidingTabLayout.TabColorizer)}.
   */
  public interface TabColorizer {

    /**
     * @return return the color of the indicator used when {@code position} is selected.
     */
    int getIndicatorColor(int position);

    List<GradientItem> getGradientItems(int position);
  }

  /**
   * Allows complete control over the view position. This will help case where extra custom views
   * are inserted to {@link SlidingTabLayout}.
     */
  public interface SelectionPositionFinder {

    /**
     * @return return the color of the indicator used when {@code position} is selected.
     */
    Pair<Integer, Integer> getViewPosition(View selectedView);
  }

  public interface SlidingTabScrollListener {
    void onScrollChange(int totalScrollableWidth, int scrollX);
  }

  private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
    private int mScrollState;

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      int tabStripChildCount = mTabStrip.getChildCount();
      if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
        return;
      }

      mTabStrip.onViewPagerPageChanged(position, positionOffset);

      View selectedTitle = mTabStrip.getChildAt(position);
      int extraOffset = (selectedTitle != null)
          ? (int) (positionOffset * selectedTitle.getWidth())
          : 0;
      scrollToTab(position, extraOffset);

      if (mViewPagerPageChangeListener != null) {
        mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
            positionOffsetPixels);
      }

      updateSelectionIndicators();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
      mScrollState = state;

      if (mViewPagerPageChangeListener != null) {
        mViewPagerPageChangeListener.onPageScrollStateChanged(state);
      }
    }

    @Override
    public void onPageSelected(int position) {
      if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
        mTabStrip.onViewPagerPageChanged(position, 0f);
        scrollToTab(position, 0);
      }
      for (int i = 0; i < mTabStrip.getChildCount(); i++) {
        mTabStrip.getChildAt(i).setSelected(position == i);
      }
      if (mViewPagerPageChangeListener != null) {
        mViewPagerPageChangeListener.onPageSelected(position);
      }
    }

  }

  private void updateSelectionIndicators() {
    final PagerAdapter adapter = mViewPager.getAdapter();
    if (adapter instanceof SlidingTabLayoutAdapter) {
      SlidingTabLayoutAdapter slidingTabLayoutAdapter = (SlidingTabLayoutAdapter) adapter;

      for (int i = 0; i < mTabStrip.getChildCount(); i++) {
        View child = mTabStrip.getChildAt(i);
        NHTextView tabTitleView = child.findViewById(mTabViewTextViewId);
        ImageView tabImageView = child.findViewById(mTabImageViewId);

        if (tabTitleView == null || tabImageView == null) {
          continue;
        }

        String pageIconUrl = slidingTabLayoutAdapter.getPageIconUrl(i);
        if (pageIconUrl != null) {
          Image.load(pageIconUrl).into(tabImageView);
          tabImageView.setVisibility(View.VISIBLE);
        }
        tabImageView.setImageTintMode(PorterDuff.Mode.SRC_IN);
        if (i == mViewPager.getCurrentItem()) {
          tabImageView.setImageTintList(
              ColorStateList.valueOf(getResources().getColor(R.color.white_color)));
        } else {
          tabImageView.setImageTintList(
              ColorStateList.valueOf(
                  getResources().getColor(R.color.explore_unselected_overlay)));
        }

        slidingTabLayoutAdapter.applyCustomStyles(child, i);
      }
    }
  }

  public interface ViewBinder {
    void bindView(View view, int position);
  }

  public interface OnTabClickListener {
    void onClick(View v, int position);
  }

  private class TabClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      for (int i = 0; i < mTabStrip.getChildCount(); i++) {
        if (extraView != null && v == extraView) {
          if (extraViewClickListener != null) {
            extraViewClickListener.onClick(v);
          }
          return;
        }

        if (v == mTabStrip.getChildAt(i)) {
          if (tabClickListener != null) {
            tabClickListener.onClick(v, i);
          }

          int oldPosition = mViewPager.getCurrentItem();
          NhAnalyticsAppState.getInstance().setAction(NhAnalyticsUserAction.CLICK);
          TabClickEvent event = new TabClickEvent(oldPosition, i, SlidingTabLayout.this.hashCode());
          BusProvider.getUIBusInstance()
              .post(event);
          tabClickEventLiveData.setValue(event);
          /* on clicking tab, Fragment onPageSelected call happens after new fragment is resumed.
          * On swiping to tab, onPageSelected call happens before, but this function wont get called.
          * Hence, we need to call extractAndUpdateState from both places. Duplicate calls are
          * redundant but not erroneous - 2nd call will be a no-op
          */
          CardSeenStatusRepo.extractAndUpdateState(mViewPager, i);
          mViewPager.setCurrentItem(i);
          return;
        }
      }
    }
  }

  /**
   * To change offset of selected tab
   *
   * @param titleOffsetDips
   */
  public void setTitleOffet(int titleOffsetDips, Context context) {
    if (context != null) {
      mTitleOffset = CommonUtils.getPixelFromDP(titleOffsetDips, context);
    }
  }

  public void setExtraView(int extraViewId) {
    final OnClickListener clickListener = new TabClickListener();
    this.extraView = LayoutInflater.from(getContext()).inflate(extraViewId, mTabStrip, false);
    mTabStrip.addView(extraView);
    extraView.setVisibility(VISIBLE);
    this.extraView.setOnClickListener(clickListener);
  }

  public void setExtraViewClickListener(OnClickListener extraViewClickListener) {
    this.extraViewClickListener = extraViewClickListener;
  }

  public void hideExtraView() {
    if (extraView != null) {
      mTabStrip.removeView(extraView);
    }
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    if (slidingTabScrollListener != null) {
      slidingTabScrollListener.onScrollChange(mTabStrip.getWidth(), l);
    }
  }
}