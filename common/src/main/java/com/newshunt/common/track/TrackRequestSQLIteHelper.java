/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.track;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author: bedprakash.rout on 03/08/17.
 */

class TrackRequestSQLIteHelper extends SQLiteOpenHelper {

  public static final String TRACK_REQUEST_DB = "dailyhunt.track";
  private static final int DB_VERSION = 1;

  // table name
  public static final String TABLE_TRACK_REQUEST = "track_request";

  // column names
  public static final String COLUMN_TRACK_REQUEST_PK = "track_request_id";
  public static final String COLUMN_TRACK_URL = "track_url";
  public static final String COLUMN_REQUEST_TYPE = "request_type";
  public static final String COLUMN_FAILURE_COUNT = "failure_count";

  private static TrackRequestSQLIteHelper sInstance;


  public static TrackRequestSQLIteHelper getInstance(Context context) {
    if (sInstance == null) {
      synchronized (TrackRequestSQLIteHelper.class) {
        if (sInstance == null) {
          sInstance = new TrackRequestSQLIteHelper(context, TRACK_REQUEST_DB, null, DB_VERSION);
        }
      }
    }
    return sInstance;
  }

  private TrackRequestSQLIteHelper(Context context, String name,
                                   SQLiteDatabase.CursorFactory factory, int version) {
    super(context, name, factory, version);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(getCreateTrackRequestDbSQL());
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //initial version. nothing to do now.
  }


  private String getCreateTrackRequestDbSQL() {
    String query = "create table IF NOT EXISTS "
        + TABLE_TRACK_REQUEST + "("
        + COLUMN_TRACK_REQUEST_PK + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_TRACK_URL + " text,"
        + COLUMN_FAILURE_COUNT + " INTEGER,"
        + COLUMN_REQUEST_TYPE + " text)";
    return query;
  }


}
