/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dhutil.helper.RateUsConfigCheckHelper;
import com.newshunt.dhutil.helper.RateUsDialogHelper;
import com.newshunt.dhutil.helper.RateUsTriggerAction;
import com.newshunt.dhutil.view.RateUsDialogActivity;
import com.newshunt.dataentity.news.analytics.NewsReferrer;

/**
 * Checks for conditions to show the rate us dialog
 * Created by shashikiran.nr on 7/1/2016.
 */
public class RateUsCheckHelperNews {

  public static void checkToShowRateUsOnOpeningOrSwipeStories(boolean fromOpen) {
    if (!RateUsConfigCheckHelper.canShowAppRateDialog()) {
      return;
    }

    if (fromOpen) {
      if (!RateUsDialogHelper.isUserOpenedNStories()) {
        return;
      }
      RateUsDialogActivity.Companion.openDialog(RateUsTriggerAction.CLICK, NewsReferrer.STORY_DETAIL,
              NhAnalyticsEventSection.NEWS);
    } else {
      if (!RateUsDialogHelper.isUserOpenedNStories() &&
              !RateUsDialogHelper.isTenMinSessionHappened()) {
        return;
      }
      RateUsDialogActivity.Companion.openDialog(RateUsTriggerAction.SWIPE, NewsReferrer.STORY_DETAIL,
              NhAnalyticsEventSection.NEWS);
    }
  }

  public static void checkToShowRateUsOnBackPressed() {
    if (!RateUsConfigCheckHelper.canShowAppRateDialog()) {
      return;
    }
    if (!RateUsDialogHelper.isTenMinSessionHappened()) {
      return;
    }
    RateUsDialogActivity.Companion.openDialog(RateUsTriggerAction.BACK, NewsReferrer.STORY_DETAIL,
            NhAnalyticsEventSection.NEWS);
  }

  public static boolean checkToShowRateUsOnShareOrOnNStoryShare() {
    if (!RateUsConfigCheckHelper.canShowAppRateDialog()) {
      return false;
    }
    if (!RateUsDialogHelper.isUserSharedNStory()) {
      return false;
    }
    RateUsDialogActivity.Companion.openDialog(RateUsTriggerAction.SHARE, NewsReferrer.STORY_DETAIL,
        NhAnalyticsEventSection.NEWS);
    return true;
  }

  public static void checkToShowRateUsOnVideoWatch(long duration) {
    if (!RateUsConfigCheckHelper.canShowAppRateDialog()) {
      return;
    }
    if (!RateUsDialogHelper.isTenMinSessionHappened() &&
            !RateUsDialogHelper.hasUserWatchedMinimumDurationForVideo(duration)) {
      return;
    }
    RateUsDialogActivity.Companion.openDialog(RateUsTriggerAction.CLICK, NewsReferrer.STORY_DETAIL,
        NhAnalyticsEventSection.NEWS);
  }
}
