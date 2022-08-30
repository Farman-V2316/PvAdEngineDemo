/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.entity;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author shrikant.agrawal
 */
public class ConfigEntity implements Serializable {

  public static final List<Integer> DEFAULT_AUTO_REFRESH_INTERVALS = Arrays.asList(-1);
  public static final Long DEFAULT_TIME_DELAY_TO_REFRESH_BOTTOMBAR_ICON = 0L;
  public static final Long DEFAULT_TIME_DELAY_TO_HIDE_MORE_NEWS = 15L;
  public static final Long DEFAULT_TIME_GAP_MANUAL_REFRESH = 5L;

  public enum StoryDetailSwipeBehavior{
    HISTORY, // swipe in history only
    HISTORY_PROMPT_REFRESH // combo of 2 modes - 1) REFRESH_ONLY: if entered before separator,
    // HISTORY with switch-to-refresh-prompt : if entered after separator.
  }

  private List<Integer> autoRefreshIntervals;

  private Long timeDelayToBottomNewsBecomesRefreshIcon;

  private Long timeDelayToHideMoreNews;

  private Long timeGapForManualRefresh;

  // flag whether to show count in more news button.
  private boolean showNewStoryCountInMoreNews;

  private StoryDetailSwipeBehavior storyDetailSwipeBehavior;

  // max right swipe count to show switch-to-refresh-prompt
  private int storyDetailSwipeBehaviorSwipeCount;

  // time gap in between current and previous story to  show switch-to-refresh-prompt
  private long storyDetailSwipeBehaviorTimeGapFromPrevStory;

  // flag to show separator between new stories and olde stories in news list
  private boolean showSeparator;

  public List<Integer> getAutoRefreshIntervals() {
    return autoRefreshIntervals;
  }

  public void setAutoRefreshIntervals(List<Integer> autoRefreshIntervals) {
    this.autoRefreshIntervals = autoRefreshIntervals;
  }

  public Long getTimeDelayToBottomNewsBecomesRefreshIcon() {
    return timeDelayToBottomNewsBecomesRefreshIcon;
  }

  public void setTimeDelayToBottomNewsBecomesRefreshIcon(
      Long timeDelayToBottomNewsBecomesRefreshIcon) {
    this.timeDelayToBottomNewsBecomesRefreshIcon = timeDelayToBottomNewsBecomesRefreshIcon;
  }

  public Long getTimeDelayToHideMoreNews() {
    return timeDelayToHideMoreNews;
  }

  public void setTimeDelayToHideMoreNews(Long timeDelayToHideMoreNews) {
    this.timeDelayToHideMoreNews = timeDelayToHideMoreNews;
  }

  public Long getTimeGapForManualRefresh() {
    return timeGapForManualRefresh;
  }

  public void setTimeGapForManualRefresh(Long timeGapForManualRefresh) {
    this.timeGapForManualRefresh = timeGapForManualRefresh;
  }

  public boolean isShowNewStoryCountInMoreNews() {
    return showNewStoryCountInMoreNews;
  }

  public void setShowNewStoryCountInMoreNews(boolean showNewStoryCountInMoreNews) {
    this.showNewStoryCountInMoreNews = showNewStoryCountInMoreNews;
  }

  public StoryDetailSwipeBehavior getStoryDetailSwipeBehavior() {
    return storyDetailSwipeBehavior;
  }

  public void setStoryDetailSwipeBehavior(
      StoryDetailSwipeBehavior storyDetailSwipeBehavior) {
    this.storyDetailSwipeBehavior = storyDetailSwipeBehavior;
  }

  public int getStoryDetailSwipeBehaviorSwipeCount() {
    return storyDetailSwipeBehaviorSwipeCount;
  }

  public void setStoryDetailSwipeBehaviorSwipeCount(int storyDetailSwipeBehaviorSwipeCount) {
    this.storyDetailSwipeBehaviorSwipeCount = storyDetailSwipeBehaviorSwipeCount;
  }

  public long getStoryDetailSwipeBehaviorTimeGapFromPrevStory() {
    return storyDetailSwipeBehaviorTimeGapFromPrevStory;
  }

  public void setStoryDetailSwipeBehaviorTimeGapFromPrevStory(
      long storyDetailSwipeBehaviorTimeGapFromPrevStory) {
    this.storyDetailSwipeBehaviorTimeGapFromPrevStory =
        storyDetailSwipeBehaviorTimeGapFromPrevStory;
  }

  public boolean isShowSeparator() {
    return showSeparator;
  }

  public void setShowSeparator(boolean showSeparator) {
    this.showSeparator = showSeparator;
  }

  @NonNull
  public static ConfigEntity getDefaultConfig() {
    ConfigEntity configEntity = new ConfigEntity();
    configEntity.autoRefreshIntervals = new ArrayList<>(DEFAULT_AUTO_REFRESH_INTERVALS);
    configEntity.timeDelayToBottomNewsBecomesRefreshIcon =
        DEFAULT_TIME_DELAY_TO_REFRESH_BOTTOMBAR_ICON;
    configEntity.timeDelayToHideMoreNews = DEFAULT_TIME_DELAY_TO_HIDE_MORE_NEWS;
    configEntity.timeGapForManualRefresh = DEFAULT_TIME_GAP_MANUAL_REFRESH;
    return configEntity;
  }
}
