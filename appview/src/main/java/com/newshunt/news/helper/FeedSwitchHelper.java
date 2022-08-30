/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Logger;

/**
 * This class writes logic to switch from history mode to refresh mode
 *
 * @author neeraj.kumar on 31/05/17.
 */

public class FeedSwitchHelper {
  private static final String LOG_TAG = "FeedSwitchHelper";
  public static final int INVALID = 0;
  public static class FeedSwitchEvent {
    private final int uniqueRequestId;
    private final String refreshUrl;

    public FeedSwitchEvent(int uniqueRequestId, String refreshUrl) {
      this.uniqueRequestId = uniqueRequestId;
      this.refreshUrl = refreshUrl;
    }

    public int getUniqueRequestId() {
      return uniqueRequestId;
    }

    public String getRefreshUrl() {
      return refreshUrl;
    }
  }

  private final String refreshUrl;
  private final int uniqueRequestId;
  private final int maxSwipeCount;
  private final long timeGapFromPrevStory;

  private int prevPosition = 0;
  private Long prevStoryTimestamp = 0L;

  private int swipeCount;

  public FeedSwitchHelper(String refreshUrl, int uniqueRequestId, int maxSwipeCount,
                          long timeGapFromPrevStory) {
    this.refreshUrl = refreshUrl;
    this.uniqueRequestId = uniqueRequestId;
    this.maxSwipeCount = maxSwipeCount;
    this.timeGapFromPrevStory = timeGapFromPrevStory;
  }

  public void updatePositionAndTimestamp(int position, long storyTimestamp) {
    Logger.d(LOG_TAG, "updatePositionAndTimestamp: " + position + "," + storyTimestamp);

    // on right swipe, increment the swipe count and send switch feed event on reaching the
    // defined count
    if (position >= prevPosition) {
      swipeCount += 1;
      if (maxSwipeCount!=INVALID && swipeCount >= maxSwipeCount) {
        sendSwitchEvent();
      }
    }
    prevPosition = position;

    //if story timestamp is older than previous story by a defined timegap, send switch feed event
    if (timeGapFromPrevStory != INVALID && prevStoryTimestamp != 0L &&
        (prevStoryTimestamp - storyTimestamp) / 1000 > timeGapFromPrevStory) {
      sendSwitchEvent();
    }
    prevStoryTimestamp = storyTimestamp;
  }

  private void sendSwitchEvent() {
    BusProvider.getUIBusInstance().post(new FeedSwitchEvent(uniqueRequestId, refreshUrl));
  }
}
