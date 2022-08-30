/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.track;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO for track request db
 *
 * @author: bedprakash.rout on 03/08/17.
 */

public class TrackRequestDao {
  private static final int TRACK_REQUEST_DB_CACHE_LIMIT = 300;
  private static TrackRequestDao sInstance;
  private final TrackRequestSQLIteHelper dbHelper;
  private SQLiteDatabase database;

  private TrackRequestDao() {
    dbHelper = TrackRequestSQLIteHelper.getInstance(CommonUtils.getApplication());
  }

  public static TrackRequestDao getInstance() {
    if (null == sInstance) {
      synchronized (TrackRequestDao.class) {
        if (null == sInstance) {
          sInstance = new TrackRequestDao();
        }
      }
    }
    return sInstance;
  }

  synchronized void addRequest(@NonNull TrackRequest trackRequest) {
    autoOpen();
    ContentValues values = fillTrackInfo(trackRequest);
    long rowId = database.insertWithOnConflict(TrackRequestSQLIteHelper.TABLE_TRACK_REQUEST,
        null, values,
        SQLiteDatabase.CONFLICT_REPLACE);
    clearDBCacheIfExceed();
    trackRequest.id = rowId;
  }

  @SuppressWarnings("UnusedReturnValue")
  synchronized boolean deleteRequest(@NonNull TrackRequest request) {
    autoOpen();
    String selection = TrackRequestSQLIteHelper.COLUMN_TRACK_REQUEST_PK + "=?";
    String[] selectionArgs = new String[]{String.valueOf(request.id)};
    int result =
        database.delete(TrackRequestSQLIteHelper.TABLE_TRACK_REQUEST, selection, selectionArgs);
    return result != 0;
  }

  @NonNull
  private ContentValues fillTrackInfo(TrackRequest trackRequest) {
    ContentValues values = new ContentValues();
    values.put(TrackRequestSQLIteHelper.COLUMN_REQUEST_TYPE, trackRequest.getRequestType());
    values.put(TrackRequestSQLIteHelper.COLUMN_TRACK_URL, trackRequest.url);
    values.put(TrackRequestSQLIteHelper.COLUMN_FAILURE_COUNT, trackRequest.failureCount);
    return values;
  }

  private void autoOpen() {
    if (null == database || !database.isOpen()) {
      database = dbHelper.getWritableDatabase();
    }
  }

  synchronized List<TrackRequest> getALLRequests() {
    autoOpen();
    String selectQuery =
        "select * from " + TrackRequestSQLIteHelper.TABLE_TRACK_REQUEST;
    return executeGetQuery(selectQuery);
  }

  private List<TrackRequest> executeGetQuery(String selectQuery) {
    ArrayList<TrackRequest> requests = new ArrayList<>();
    Cursor cursor = null;
    try {
      cursor = database.rawQuery(selectQuery, null);

      if (cursor == null) {
        return requests;
      }
      if (cursor.moveToFirst()) {
        requests = readRequests(cursor);
      }
      return requests;
    } catch (Exception e) {
      Logger.caughtException(e);
      return requests;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private ArrayList<TrackRequest> readRequests(Cursor cursor) {
    ArrayList<TrackRequest> requests = new ArrayList<>();
    do {
      long requestId = cursor.getLong(cursor.getColumnIndex(TrackRequestSQLIteHelper
          .COLUMN_TRACK_REQUEST_PK));
      String url = cursor.getString(cursor.getColumnIndex(TrackRequestSQLIteHelper
          .COLUMN_TRACK_URL));
      String requestType = cursor.getString(cursor.getColumnIndex(TrackRequestSQLIteHelper
          .COLUMN_REQUEST_TYPE));
      int failureCount = cursor.getInt(cursor.getColumnIndex(TrackRequestSQLIteHelper
          .COLUMN_FAILURE_COUNT));
      TrackRequest request = new TrackRequest(requestId, url, requestType, failureCount, true);
      requests.add(request);
    } while (cursor.moveToNext());

    return requests;
  }

  private void clearDBCacheIfExceed() {
    int excessTrackRequestCount = getAllTrackRequestCount() - TRACK_REQUEST_DB_CACHE_LIMIT;
    if (excessTrackRequestCount > 0) {
      //Clear old entries beyond Cache Limit
      String deleteQuery =
          "DELETE FROM "
              + TrackRequestSQLIteHelper.TABLE_TRACK_REQUEST +
              " where " + TrackRequestSQLIteHelper.COLUMN_TRACK_REQUEST_PK + " in (select " +
              TrackRequestSQLIteHelper.COLUMN_TRACK_REQUEST_PK + " from " +
              TrackRequestSQLIteHelper.TABLE_TRACK_REQUEST + " order by "
              + TrackRequestSQLIteHelper.COLUMN_TRACK_REQUEST_PK +
              " LIMIT " + excessTrackRequestCount + ");";
      database.execSQL(deleteQuery);
    }
  }

  private int getAllTrackRequestCount() {
    autoOpen();
    String query =
        "SELECT COUNT(*) FROM " + TrackRequestSQLIteHelper.TABLE_TRACK_REQUEST;
    return executeQueryAndGetCount(query);
  }

  private int executeQueryAndGetCount(String query) {
    Cursor cursor = database.rawQuery(query, null);
    int count = 0;
    try {
      if (cursor != null && cursor.moveToFirst()) {
        count = cursor.getInt(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return count;
  }
}
