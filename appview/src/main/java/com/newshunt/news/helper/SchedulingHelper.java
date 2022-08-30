package com.newshunt.news.helper;
/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.news.model.entity.ConfigEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author neeraj.kumar
 *         <p>
 *         This helper class does following tasks
 *         1. Schedule timer and send callback for next first page request
 *         2. Restrict consecutive first page requests for some configurable time
 *         3. Schedule timer and send callback to change bottom bar news button to refresh button
 */
public class SchedulingHelper {

  private static final int MINIMUM_AUTOREFRESH_DELAY = 30; // 30 seconds
  private static final String LOG_TAG = "SchedulingHelper";

  private final List<Integer> refreshIntervals;
  private long timeDelayToBottomNewsBecomeRefreshIcon;
  private final long timeGapForManualRefresh;
  private SchedulingCallbacks callback;

  private int refreshIndexCounter = -1;
  private Disposable refreshPageDisposable, bottomBarRefreshDisposable;
  private State bottomBarRefreshIconTimerState = State.NOT_STARTED;
  private State refreshDelayTimerState = State.NOT_STARTED;

  private long refreshPageRequestTime, schedulerStartTime;

  private enum State {
    NOT_STARTED, IN_PROGRESS, PAUSED, RESUMED, FINISHED
  }

  public SchedulingHelper(final List<Integer> refreshIntervals,
                          final long timeDelayToBottomNewsBecomeRefreshIcon,
                          final long timeGapForManualRefresh,
                          final SchedulingCallbacks callback) {
    if (CommonUtils.isEmpty(refreshIntervals)) {
      throw new IllegalArgumentException("At least one refresh time is required");
    }
    this.refreshIntervals = refreshIntervals;
    this.callback = callback;
    this.timeDelayToBottomNewsBecomeRefreshIcon = timeDelayToBottomNewsBecomeRefreshIcon;
    this.timeGapForManualRefresh = timeGapForManualRefresh;
    Logger.d(LOG_TAG, String.format("%s, %d, %d", this.refreshIntervals,
        this.timeDelayToBottomNewsBecomeRefreshIcon, this.timeGapForManualRefresh));
  }

  public SchedulingHelper(ConfigEntity configEntity) {
    this(configEntity.getAutoRefreshIntervals(),
        configEntity.getTimeDelayToBottomNewsBecomesRefreshIcon(),
        configEntity.getTimeGapForManualRefresh(), null);
  }

  public void setCallback(SchedulingCallbacks callback) {
    if (callback == null) {
      throw new IllegalArgumentException("callback should not be null");
    }
    this.callback = callback;
  }

  /**
   * Once the refresh page is requested, it should not be requested again for some configurable
   * time.
   */
  public boolean canReqRefreshPage() {
    return refreshPageRequestTime == 0 ||
        ((System.currentTimeMillis() - refreshPageRequestTime) / 1000 >= timeGapForManualRefresh);
  }

  public boolean canShowBottomBarRefresh() {
    return bottomBarRefreshIconTimerState == State.FINISHED;
  }

  /**
   * Once the refresh page is requested, save the request time.
   */
  public void refreshPageRequested() {
    refreshPageRequestTime = System.currentTimeMillis();
    Logger.d(LOG_TAG, "refreshPageRequested at " + refreshPageRequestTime);
    if (bottomBarRefreshIconTimerState == State.NOT_STARTED) {
      bottomBarRefreshIconTimerState = State.IN_PROGRESS;
      scheduleBottomBarRefreshTimer();
    }
  }

  /**
   * When more news button is hidden, schedule a timer to show the more news button after some time
   */
  public void moreNewsShown() {
    Logger.d(LOG_TAG, "moreNewsShown");
    start(false);
  }

  /**
   * When refresh page is shown to the user, schedule timer for next page refresh request.
   */
  public void refreshPageShown() {
    Logger.d(LOG_TAG, "refreshPageShown");
    start(true);
  }

  /**
   * When the error is shown, re-schedule the timer to request first page
   */
  public void errorShown() {
    Logger.d(LOG_TAG, "errorShown");
    start(false);
  }

  private void start(boolean moveToNextBucket) {
    switch (refreshDelayTimerState) {
      case NOT_STARTED:
      case IN_PROGRESS:
      case RESUMED:
      case FINISHED:
        refreshDelayTimerState = State.IN_PROGRESS;
        scheduleTimer(getAutoRefreshDelay(moveToNextBucket));
        break;

      case PAUSED:
        // do nothing
        break;
    }
  }

  /**
   * call this method to unsubscribe from the observables.
   */
  public void cancelScheduling() {

    if (refreshDelayTimerState == State.IN_PROGRESS || refreshDelayTimerState == State.RESUMED) {

      Logger.d(LOG_TAG, "cancelScheduling");
      refreshDelayTimerState = State.PAUSED;

      if (refreshPageDisposable != null && !refreshPageDisposable.isDisposed()) {
        refreshPageDisposable.dispose();
      }
    }

    // if bottomBarRefreshIconTimerState is in progress state or resumed state change it to
    // paused state and cancel the scheduler
    if (bottomBarRefreshIconTimerState == State.IN_PROGRESS
        || bottomBarRefreshIconTimerState == State.RESUMED) {
      bottomBarRefreshIconTimerState = State.PAUSED;
      if (bottomBarRefreshDisposable != null && !bottomBarRefreshDisposable.isDisposed()) {
        bottomBarRefreshDisposable.dispose();
      }
    }
  }

  /**
   * Call this method to resume the scheduling after you paused the scheduling by calling
   * cancelScheduling() method.
   */
  public void resumeRefreshScheduling() {
    if (refreshDelayTimerState == State.PAUSED) {
      refreshDelayTimerState = State.RESUMED;
      Logger.d(LOG_TAG, "resumeRefreshScheduling");
      scheduleTimer(getAutoRefreshDelay(false));
    }

    if (bottomBarRefreshIconTimerState == State.PAUSED) {
      bottomBarRefreshIconTimerState = State.RESUMED;
      scheduleBottomBarRefreshTimer();
    }
  }

  private void scheduleTimer(long delay) {
    if (refreshPageDisposable != null && !refreshPageDisposable.isDisposed()) {
      refreshPageDisposable.dispose();
    }

    Logger.d(LOG_TAG, "timer started with delay " + delay);
    refreshPageDisposable = Observable.just("scheduled timer")
        .delay(delay, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::requestRefreshPage, Logger::caughtException);

    if (refreshDelayTimerState == State.IN_PROGRESS) {
      schedulerStartTime = System.currentTimeMillis();
    }

  }

  private void scheduleBottomBarRefreshTimer() {

    long timeDelayToShowBottomRefreshIcon = getDelayToChangeNewsBottomBarToRefreshIcon();
    bottomBarRefreshDisposable = Observable.just("scheduled timer for bottom bar refresh icon")
        .delay(timeDelayToShowBottomRefreshIcon, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(t -> {
          bottomBarRefreshIconTimerState = State.FINISHED;
          callback.showBottomBarRefreshButton();
          Logger.d(LOG_TAG, "showBottomBarRefreshIcon ");
        }, Logger::caughtException);

    Logger.d(LOG_TAG,
        "showBottomBarRefreshIcon timer started at with delay " +
            timeDelayToShowBottomRefreshIcon);

  }

  private void requestRefreshPage(Object t) {
    schedulerStartTime = 0;
    refreshDelayTimerState = State.FINISHED;
    callback.refreshPage();
  }

  private long getAutoRefreshDelay(boolean moveToNextBucket) {
    if (moveToNextBucket && (refreshIndexCounter < refreshIntervals.size() - 1)) {
      refreshIndexCounter++;
    }

    if (refreshIndexCounter == -1) {
      refreshIndexCounter = 0;
    }

    long delay = refreshIntervals.get(refreshIndexCounter);
    if (delay < MINIMUM_AUTOREFRESH_DELAY) {
      delay = MINIMUM_AUTOREFRESH_DELAY;
    }

    if (refreshDelayTimerState == State.RESUMED) {
      long currentTimeInMillis = System.currentTimeMillis();
      if ((currentTimeInMillis - schedulerStartTime) / 1000 > delay) {
        return 0;
      } else {
        return delay - (currentTimeInMillis - schedulerStartTime) / 1000;
      }
    }
    return delay;
  }

  private long getDelayToChangeNewsBottomBarToRefreshIcon() {
    if (bottomBarRefreshIconTimerState == State.RESUMED) {
      long currentTimeInMillis = System.currentTimeMillis();
      if ((currentTimeInMillis - schedulerStartTime) / 1000 >
          timeDelayToBottomNewsBecomeRefreshIcon) {
        timeDelayToBottomNewsBecomeRefreshIcon = 0;
      } else {
        timeDelayToBottomNewsBecomeRefreshIcon = timeDelayToBottomNewsBecomeRefreshIcon -
            (currentTimeInMillis - schedulerStartTime) / 1000;
      }
    }
    return timeDelayToBottomNewsBecomeRefreshIcon;
  }

  public interface SchedulingCallbacks {
    void showBottomBarRefreshButton();

    void refreshPage();
  }
}
