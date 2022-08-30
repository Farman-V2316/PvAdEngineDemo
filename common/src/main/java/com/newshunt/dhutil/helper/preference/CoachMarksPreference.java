/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.preference;

import com.newshunt.common.helper.preference.PreferenceType;
import com.newshunt.common.helper.preference.SavedPreference;

/**
 * Preferences related to coach marks.
 *
 * @author shreyas.desai
 */
public enum CoachMarksPreference implements SavedPreference {
  PREFERENCE_STICKY_COACH_MARKS("stickyCoachMarks", PreferenceType.COACH_MARKS),
  PREFERENCE_STICKY_COACH_MARK_BOOK("stickyCoachMarkBook", PreferenceType.COACH_MARKS),
  PREFERENCE_COACH_MARKS_NEWS_DETAIL("coachMarksNewsDetail", PreferenceType.COACH_MARKS),
  PREFERENCE_TOOLTIP_COACH_MARKS_TOPICS("tooltipCoachMarksTopics", PreferenceType.COACH_MARKS),
  PREFERENCE_TOOLTIP_COACH_MARKS_SOURCES("tooltipCoachMarksSources", PreferenceType.COACH_MARKS),
  PREFERENCE_TOOLTIP_COACH_MARKS_LANGUAGES(
      "tooltipCoachMarksLanguages", PreferenceType.COACH_MARKS),
  PREFERENCE_STICKY_COUNT_KEY("stickyCountKey", PreferenceType.COACH_MARKS),
  PREFERENCE_STICKY_COACH_MARK_EXAM_PREP("stickyCoachMarkTestPrep", PreferenceType.COACH_MARKS),
  PREFERENCE_DHTV_COACH_MARK_SWIPE_UP("dhtvSwipeUpCoachMark", PreferenceType.COACH_MARKS),
  PREFERENCE_DHTV_COACH_MARK_SWIPE_LEFT_RIGHT("dhtvSwipeLeftRightCoachMark", PreferenceType
      .COACH_MARKS),
  PREFERENCE_DHTV_COACH_MARK_PIP_TAP("dhtvPipTapCoachMark", PreferenceType.COACH_MARKS),
  PREFERENCE_DHTV_COACH_MARK_CHANNEL("dhtvChannelCoachMark", PreferenceType.COACH_MARKS),
  PREFERENCE_DHTV_COACH_MARK_SETTING("dhtvSettingCoachMark", PreferenceType.COACH_MARKS),
  PREFERENCE_DHTV_COACH_MARK_INTRO("dhtvIntroCoachMark", PreferenceType.COACH_MARKS),
  PREFERENCE_DHTV_COACH_MARK_ROTATE_DEVICE("dhtvRotateDeviceCoachMark", PreferenceType
      .COACH_MARKS),
  EMPTY("", PreferenceType.COACH_MARKS),
  PREFERENCE_LAST_COACH_MAKE_TIME_ELAPSED_FROM_FEED("lastCoachMarkTimeElapsedFromFeed", PreferenceType.COACH_MARKS),
  PREFERENCE_SPV_COUNT("spvCount", PreferenceType.COACH_MARKS),
  PREFERENCE_LAST_COACH_MARK_TIME_ELAPSED_FROM_NOTIFICATION("lastCoachMarkTimeElapsedFromFeed", PreferenceType.COACH_MARKS),
  PREFERENCE_SWIPE_COUNT("swipeCount", PreferenceType.COACH_MARKS),
  PREFERENCE_TOOL_TIP_REORDER_TAB("toolTipReorderTab", PreferenceType.COACH_MARKS),
  PREFERENCE_TOOL_TIP_REMOVE_TAB("toolTipRemoveTabs",PreferenceType.COACH_MARKS),
  PREFERENCE_TOOL_ADD_TAB("toolTipAddTabs",PreferenceType.COACH_MARKS);

  private String name;
  private PreferenceType preferenceType;

  CoachMarksPreference(String name, PreferenceType preferenceType) {
    this.name = name;
    this.preferenceType = preferenceType;
  }


  @Override
  public PreferenceType getPreferenceType() {
    return preferenceType;
  }

  @Override
  public String getName() {
    return name;
  }
}
