/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.news.view.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.appview.R;

import java.util.Set;

/**
 * A ViewGroup to show supplement ads (taboola, ads)
 *
 * @author raunak.yadav
 */
/*
public class SupplementAdsView extends LinearLayout {

  public SupplementAdsView(Context context) {
    this(context, null);
  }

  public SupplementAdsView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SupplementAdsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public SupplementAdsView(Context context, AttributeSet attrs, int defStyleAttr,
                           int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public void enableTitleDisplay(boolean titleVisible) {
    View titleView = findViewById(R.id.supplement_section_title);
    if (titleView != null) {
      titleView.setVisibility(titleVisible ? View.VISIBLE : View.GONE);
    }
  }

  public void removeOutdatedAds(Set<String> outdatedTags) {
    if (CommonUtils.isEmpty(outdatedTags)) {
      return;
    }

    int childCount = getChildCount();
    // index 0 -> section title
    for (int i = childCount - 1; i > 0; i--) {
      String tag = (String) getChildAt(i).getTag();

      if (outdatedTags.contains(tag)) {
        removeViewAt(i);
      }
    }

    // If all ads have been removed, hide the title too.
    if (getChildCount() == 1) {
      enableTitleDisplay(false);
      setVisibility(View.GONE);
    }
  }
}
*/
