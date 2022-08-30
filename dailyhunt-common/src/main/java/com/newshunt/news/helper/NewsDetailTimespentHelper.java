/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.analytics.entity.AnalyticsParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.NewsDetailTimespentEvent;
import com.newshunt.dhutil.model.sqlite.TimespentEventSqliteHelper;
import com.newshunt.helper.SearchAnalyticsHelper;
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam;
import com.newshunt.news.model.internal.cache.StoryPageViewerCache;
import com.newshunt.news.model.repo.CardSeenStatusRepo;
import com.newshunt.news.util.NewsConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Helper class to handle Time Spent event generation and processing for story detail.
 *
 * @author karthik.r
 */
public class NewsDetailTimespentHelper {

  private static final String TAG = NewsDetailTimespentHelper.class.getSimpleName();

  private static NewsDetailTimespentHelper instance;

  private TimespentEventSqliteHelper timespentSqliteHelper;
  private final RecentArticleTimestampStoreHelper recentArticleTimestampStoreHelper;

  public static final String TIMESPENT_CHUNK_PREFIX = "timespent_";
  public static final String IS_PAUSED = "IS_PAUSED";
  public static final int TIMESPENT_PAUSE_DELAY = 2 * 60 * 1000;

  public static final String FULL_PAGE_LOADED = "FULL_PAGE_LOADED";
  public static final String IS_EOS_REACHED = "IS_EOS_REACHED";
  public static final String IS_RELATED_CLICKED = "IS_RELATED_CLICKED";
  public static final String IS_IMAGE_OPENED = "IS_IMAGE_OPENED";
  public static final String IS_GALLERY_OPENED = "IS_GALLERY_OPENED";
  public static final String PLAYBACK_DURATION = "PLAYBACK_DURATION";
  public static final String FONT_SIZE = "FONT_SIZE";
  public static final String SCREEN_SIZE = "SCREEN_SIZE";
  public static final String TOTAL_VIDEO_LENGTH = "TOTAL_VIDEO_LENGTH";
  public static final String IS_AD = "isAd";
  public static final String CONTENT_BOOSTED_AD_LP_URL = "CONTENT_BOOSTED_AD_LP_URL";

  public static final String HASHTAG_SEEN = "hashtag_seen";
  public static final String PERSPECTIVE_SEEN = "perspective_seen";
  public static final String DISCUSSIONS_SEEN = "discussion_seen";
  public static final String LIKES_SEEN = "likes";

  // Default values for each of the engagement params
  private final LinkedHashMap<String, String> ENGAGEMENT_PARAMS_DEFAULT_VALUES =
      new LinkedHashMap<String, String>() {
        {
          put(FULL_PAGE_LOADED, Boolean.FALSE.toString());
          put(IS_EOS_REACHED, Boolean.FALSE.toString());
          put(Constants.IS_SHARED, Boolean.FALSE.toString());
          put(IS_RELATED_CLICKED, Boolean.FALSE.toString());
          put(IS_IMAGE_OPENED, Boolean.FALSE.toString());
          put(IS_GALLERY_OPENED, Boolean.FALSE.toString());
          put(Constants.IS_LIKED, Boolean.FALSE.toString());
          put(Constants.IS_COMMENTED, Boolean.FALSE.toString());
          put(PLAYBACK_DURATION, Integer.toString(0));
          put(FONT_SIZE, Integer.toString(0));
          put(SCREEN_SIZE, Integer.toString(0));
          put(TOTAL_VIDEO_LENGTH, Integer.toString(0));
        }
      };

  private final List<String> ENGAGEMENT_PARAMS =
      new ArrayList<>(ENGAGEMENT_PARAMS_DEFAULT_VALUES.keySet());

  public static NewsDetailTimespentHelper getInstance() {
    if (instance == null) {
      synchronized (NewsDetailTimespentHelper.class) {
        if (instance == null) {
          instance = new NewsDetailTimespentHelper();
        }
      }
    }

    return instance;
  }

  private PublishSubject<NewsDetailTimespentEvent> subject;

  private NewsDetailTimespentHelper() {
    // Observe on non-UI thread for processing events in FIFO order as pushed by story page.
    subject = PublishSubject.create();
    subject.toFlowable(BackpressureStrategy.BUFFER)
        .observeOn(Schedulers.single())
        .doOnNext(value -> {
          try {
            if (value.isCreateEvent()) {
              NewsDetailTimespentEvent.NewsDetailCreateTimespentEvent event =
                  (NewsDetailTimespentEvent.NewsDetailCreateTimespentEvent) value;
              createTimespentEvents(event.getFragmentId(), event.getParams());
            } else if (value.isUpdateParamEvent()) {
              NewsDetailTimespentEvent.NewsDetailUpdateTimespentEvent event =
                  (NewsDetailTimespentEvent.NewsDetailUpdateTimespentEvent) value;
              updateTimespentEvent(event.getFragmentId(), event.getParamName(),
                  event.getParamValue());
            } else if (value.isSendEvent()) {
              NewsDetailTimespentEvent.NewsDetailSendTimespentEvent event =
                  (NewsDetailTimespentEvent.NewsDetailSendTimespentEvent) value;
              sendTimespentEvents(event.getFragmentId(), event.getParams(), event.isPaused(),
                  event.getExitAction());
            } else if (value.isClearStaleEvent()) {
              clearStaleEvents();
            } else if (value.isDeleteEvent()) {
              NewsDetailTimespentEvent.NewsDetailDeleteTimespentEvent event =
                  (NewsDetailTimespentEvent.NewsDetailDeleteTimespentEvent) value;
              deleteEvent(event.getFragmentId());
            }
          } catch (Exception ex) {
            Logger.e(TAG, "Error processing timespent event", ex);
          }

        }).subscribe();

    timespentSqliteHelper = new TimespentEventSqliteHelper(CommonUtils.getApplication());
    recentArticleTimestampStoreHelper = new RecentArticleTimestampStoreHelper();
  }

  /**
   * Utility to post create timespent event
   *
   * @param fragmentId Unique Id for a fragment
   * @param params     Map of initial params copied from corresponding story page view event.
   */
  public void postCreateTimespentEvent(Long fragmentId, Map<String, Object> params) {
    subject.onNext(new NewsDetailTimespentEvent.NewsDetailCreateTimespentEvent(fragmentId, params));
  }

  /**
   * Utility to post update an existing timespent event
   *
   * @param fragmentId Unique Id for a fragment
   * @param paramName  Name of the param
   * @param paramValue Value for the param
   */
  public void postUpdateTimespentEvent(Long fragmentId, String paramName, String
      paramValue) {
    subject.onNext(new NewsDetailTimespentEvent.NewsDetailUpdateTimespentEvent(fragmentId,
        paramName, paramValue));
  }

  /**
   * Utility to post request to post timespent event to analytics.
   *
   * @param fragmentId Unique Id for a fragment
   * @param timespent  Map of chunkwise timespent
   * @param isPaused   Flag to indicate if original event has to saved for future use.
   */
  public void postSendTimespentEvent(Long fragmentId,
                                     Map<Integer, Long> timespent,
                                     boolean isPaused,
                                     NhAnalyticsUserAction exitAction) {
    subject.onNext(
        new NewsDetailTimespentEvent.NewsDetailSendTimespentEvent(fragmentId, timespent,
            isPaused, exitAction));
  }

  /**
   * Utility to delete timespent event params.
   *
   * @param fragmentId Unique Id for a fragment
   */
  public void postDeleteTimespentEvent(Long fragmentId) {
    subject.onNext(
        new NewsDetailTimespentEvent.NewsDetailDeleteTimespentEvent(fragmentId));
  }

  /**
   * Utility to post request to clear all stale events from DB
   */
  public synchronized void postClearStaleEvents() {
    subject.onNext(new NewsDetailTimespentEvent.NewsDetailClearTimespentEvent());
  }

  /**
   * Create timespent event entry in DB
   *
   * @param fragmentId Unique Id for a fragment
   * @param params     Map of initial params copied from corresponding story page view event.
   */
  private synchronized void createTimespentEvents(final Long fragmentId,
                                                  final Map<String, Object> params) {
    SQLiteDatabase database = timespentSqliteHelper.getWritableDatabase();
    database.beginTransaction();
    for (String key : params.keySet()) {
      if (params.get(key) == null) {
        continue;
      }

      ContentValues values = new ContentValues();
      values.put(TimespentEventSqliteHelper.COLUMN_EVENT_ID, fragmentId);
      values.put(TimespentEventSqliteHelper.COLUMN_PARAM_NAME, key);
      values.put(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE, params.get(key).toString());
      database.insert(TimespentEventSqliteHelper.TABLE_NAME, null, values);
    }

    for (String key : ENGAGEMENT_PARAMS) {
      ContentValues values = new ContentValues();
      values.put(TimespentEventSqliteHelper.COLUMN_EVENT_ID, fragmentId);
      values.put(TimespentEventSqliteHelper.COLUMN_PARAM_NAME, key);
      values.put(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE,
          ENGAGEMENT_PARAMS_DEFAULT_VALUES.get(key));
      database.insert(TimespentEventSqliteHelper.TABLE_NAME, null, values);
    }

    database.setTransactionSuccessful();
    database.endTransaction();
  }

  /**
   * Update an existing timespent event with new params
   *
   * @param fragmentId Unique Id for a fragment
   * @param paramName  Name of the param
   * @param paramValue Value for the param
   */
  private void updateTimespentEvent(final Long fragmentId, final String paramName,
                                    final String paramValue) {
    SQLiteDatabase database = timespentSqliteHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE, paramValue);

    database.beginTransaction();
    int update = database.update(TimespentEventSqliteHelper.TABLE_NAME, values,
        TimespentEventSqliteHelper.COLUMN_EVENT_ID + " = ? AND " + TimespentEventSqliteHelper
            .COLUMN_PARAM_VALUE + " = ? ", new String[]{Long.toString(fragmentId), paramName});

    if (update <= 0) {
      values.put(TimespentEventSqliteHelper.COLUMN_EVENT_ID, Long.toString(fragmentId));
      values.put(TimespentEventSqliteHelper.COLUMN_PARAM_NAME, paramName);
      values.put(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE, paramValue);
      database.insert(TimespentEventSqliteHelper.TABLE_NAME, null, values);
    }
    database.setTransactionSuccessful();
    database.endTransaction();
  }

  /**
   * Send timespent event for given ID and specified chukwise time
   *
   * @param fragmentId Unique Id for a fragment
   * @param timespent  Map of chunkwise timespent
   * @param isPaused   Flag to indicate if original event has to saved for future use.
   */
  private void sendTimespentEvents(final long fragmentId,
                                   final Map<Integer, Long> timespent,
                                   final boolean isPaused,
                                   final NhAnalyticsUserAction exitAction) {
    updateTimechunk(fragmentId, timespent);
    SQLiteDatabase database = timespentSqliteHelper.getWritableDatabase();
    Cursor cursor = null;
    try {
      cursor = database.query(TimespentEventSqliteHelper.TABLE_NAME, null,
          TimespentEventSqliteHelper
              .COLUMN_EVENT_ID + " = ? ", new String[]{Long.toString(fragmentId)}, null, null,
          null);

      Map<String, Object> params = new HashMap<>();
      long[] timespentArray = new long[100];
      String[] engagementParams = new String[ENGAGEMENT_PARAMS.size()];
      int maxIndex = -1;
      boolean isPausedEarlier = false;
      if (cursor.moveToFirst()) {
        do {
          String paramName = cursor.getString(cursor.getColumnIndex(TimespentEventSqliteHelper
              .COLUMN_PARAM_NAME));
          if (paramName.startsWith(TIMESPENT_CHUNK_PREFIX)) {
            int chunkIndex = Integer.parseInt(paramName.replace(TIMESPENT_CHUNK_PREFIX, ""));
            long timeValue =
                cursor.getLong(
                    cursor.getColumnIndex(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE));

            timespentArray[chunkIndex] = timeValue;
            if (maxIndex < chunkIndex) {
              maxIndex = chunkIndex;
            }
          } else if (ENGAGEMENT_PARAMS.contains(paramName)) {
            String paramValue = cursor.getString(
                cursor.getColumnIndex(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE));
            engagementParams[ENGAGEMENT_PARAMS.indexOf(paramName)] = paramValue;
          } else if (IS_PAUSED.equals(paramName)) {
            String paramValue = cursor.getString(
                cursor.getColumnIndex(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE));
            isPausedEarlier = Boolean.parseBoolean(paramValue);
          } else {
            params.put(paramName, cursor.getString(
                cursor.getColumnIndex(TimespentEventSqliteHelper.COLUMN_PARAM_VALUE)));
          }
        } while (cursor.moveToNext());

        // Frame timespent array
        String timespentList = Constants.EMPTY_STRING;
        long totalTimespent = 0L;
        for (int i = 0; i <= maxIndex; i++) {
          totalTimespent += timespentArray[i];
          if (i == 0) {
            timespentList += timespentArray[i];
          } else {
            timespentList += "," + timespentArray[i];
          }
        }

        // Frame engagement params array
        String engagementParamsList = Constants.EMPTY_STRING;
        for (int i = 0; i < engagementParams.length; i++) {
          if (i == 0) {
            engagementParamsList += engagementParams[i];
          } else {
            engagementParamsList += "," + engagementParams[i];
          }
        }

        params.put(AnalyticsParam.TIMESPENT.getName(), totalTimespent);
        params.put(NhAnalyticsNewsEventParam.EXIT_ACTION.getName(), exitAction.name());
        if (!isPausedEarlier && totalTimespent > 0) {
          params.put(NhAnalyticsNewsEventParam.CHUNKWISE_TS.getName(), timespentList);
          params.put(NhAnalyticsNewsEventParam.ENGAGEMENT_PARAMS.getName(), engagementParamsList);
          Object remove = params.remove(NewsConstants.DH_SECTION);
          NhAnalyticsEventSection section =
              NhAnalyticsEventSection.valueOf(
                  remove != null ? remove.toString() : NhAnalyticsEventSection.NEWS.name());
          SearchAnalyticsHelper.addSearchParamsForTs(section, params);
          /*
           * To store time stamp track for story in DB (SOCIAL)
           * */
          recentArticleTimestampStoreHelper.trackTimeSpentForArticle(params, timespent,
              engagementParams, totalTimespent);
          StoryPageViewerCache.getInstance().onTimeSpent(params);
          AnalyticsClient.logProcessedDynamic(NhAnalyticsAppEvent.STORY_PAGE_VIEW.toString(),
              section.name(), params);
          Object itemId = params.get(Constants.ITEM_ID);
          if (itemId != null && !CommonUtils.isEmpty(itemId.toString())) {
            CardSeenStatusRepo.getDEFAULT().markSeen(itemId.toString());
          }
          if (params.get(IS_AD) != null && params.get(IS_AD).equals("true")) {
            AdsTimeSpentOnLPHelper.triggerTimeSpentOnLpForContentBoostedAd((String) params.get(CONTENT_BOOSTED_AD_LP_URL), totalTimespent,
                    (String) params.get(NhAnalyticsNewsEventParam.EXIT_ACTION.getName()));
          }
        }
      }
    } catch (Exception ex) {
      Logger.caughtException(ex);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    if (!isPaused) {
      database.beginTransaction();
      database.delete(TimespentEventSqliteHelper.TABLE_NAME, TimespentEventSqliteHelper
          .COLUMN_EVENT_ID + " = ? ", new String[]{Long.toString(fragmentId)});
      database.setTransactionSuccessful();
      database.endTransaction();
    } else {
      updateTimespentEvent(fragmentId, IS_PAUSED, Boolean.TRUE.toString());

      // Clear used time durations
      database.beginTransaction();
      database.delete(TimespentEventSqliteHelper.TABLE_NAME, TimespentEventSqliteHelper
          .COLUMN_EVENT_ID + " = ? AND " + TimespentEventSqliteHelper.COLUMN_PARAM_NAME + " like " +
          "'" + TIMESPENT_CHUNK_PREFIX + "%'", new String[]{Long.toString(fragmentId)});
      database.setTransactionSuccessful();
      database.endTransaction();
    }
  }

  /**
   * Update chunkwise timespent for specified fragment id.
   *
   * @param fragmentId         Unique Id for a fragment
   * @param chunkwiseTimespent Map of initial params copied from corresponding story page view event.
   */
  private void updateTimechunk(long fragmentId, Map<Integer, Long> chunkwiseTimespent) {
    if (chunkwiseTimespent == null) {
      return;
    }
    SQLiteDatabase database = timespentSqliteHelper.getWritableDatabase();
    Cursor cursor = null;
    try {
      cursor = database.query(TimespentEventSqliteHelper.TABLE_NAME, null,
          TimespentEventSqliteHelper
              .COLUMN_EVENT_ID + " = ? AND " + TimespentEventSqliteHelper.COLUMN_PARAM_NAME +
              " like '" + TIMESPENT_CHUNK_PREFIX + "%'", new String[]{
              Long.toString(fragmentId)
          }, null, null, null);

      // Add existing values to new values
      while (cursor.moveToNext()) {
        String chunkName = cursor.getString(cursor.getColumnIndex(TimespentEventSqliteHelper
            .COLUMN_PARAM_NAME));
        int chunkIndex = Integer.parseInt(chunkName.replace(TIMESPENT_CHUNK_PREFIX, ""));
        long chunkDuraton = cursor.getLong(cursor.getColumnIndex(TimespentEventSqliteHelper
            .COLUMN_PARAM_VALUE));

        Long newDuration = chunkwiseTimespent.get(chunkIndex);
        if (newDuration != null) {
          chunkDuraton = newDuration + chunkDuraton;
        }

        chunkwiseTimespent.put(chunkIndex, chunkDuraton);
      }
    } catch (Exception ex) {
      Logger.caughtException(ex);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    // Update values to DB
    for (Integer chunkIndex : chunkwiseTimespent.keySet()) {
      updateTimespentEvent(fragmentId, TIMESPENT_CHUNK_PREFIX + chunkIndex,
          Long.toString(chunkwiseTimespent.get(chunkIndex)));
    }
  }

  private synchronized void deleteEvent(Long fragmentId) {
    SQLiteDatabase database = timespentSqliteHelper.getReadableDatabase();
    database.beginTransaction();
    database.delete(TimespentEventSqliteHelper.TABLE_NAME, TimespentEventSqliteHelper
        .COLUMN_EVENT_ID + " = ? ", new String[]{Long.toString(fragmentId)});
    database.setTransactionSuccessful();
    database.endTransaction();
  }

  private synchronized void clearStaleEvents() {
    SQLiteDatabase database = timespentSqliteHelper.getReadableDatabase();
    Cursor cursor = database.query(true, TimespentEventSqliteHelper.TABLE_NAME,
        new String[]{TimespentEventSqliteHelper.COLUMN_EVENT_ID},
        (TimespentEventSqliteHelper.COLUMN_PARAM_VALUE + " != '" + NhAnalyticsAppEvent.VIDEO_PLAYED.name() + "'"),
        null, null, null, null, null);

    List<Long> pendingFragmentIds = new ArrayList<>();
    while (cursor.moveToNext()) {
      long fragmentId = cursor.getLong(0);
      pendingFragmentIds.add(fragmentId);
    }

    cursor.close();

    for (Long fragmentId : pendingFragmentIds) {
      sendTimespentEvents(fragmentId, null, false, NhAnalyticsUserAction.IDLE);
    }

    Logger.i(TAG, "Stale Timespent events cleared: " + pendingFragmentIds.size());
  }

  public static int getFontSizeForTimespentEvent(int actualFontSize) {
    int value = ((actualFontSize - NewsConstants.DEFAULT_FONT_SIZE) / 2) +
        NewsConstants.DEFAULT_PROGRESS_COUNT + 1;
    return value;
  }
}
