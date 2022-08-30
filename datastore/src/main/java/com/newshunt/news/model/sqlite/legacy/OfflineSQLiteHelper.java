/*
 * Copyright (c) 2020 . All rights reserved.
 */

package com.newshunt.news.model.sqlite.legacy;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * {@link SQLiteOpenHelper} for saving stories to read offline.
 * @author satosh.dhanyamraju
 */
@Deprecated
public class OfflineSQLiteHelper extends SQLiteOpenHelper {

  public static final String SQLITE_DB_NAME = "newshunt.news.offline";
  public static final int SQLITE_DB_VERSION = 1;

  //tables
  public static final String TABLE_STORIES = "stories";

  //columns
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_STORY_ID = "story_id";
  public static final String COLUMN_STORY_DATA = "story_data";

  public OfflineSQLiteHelper(Context context) {
    super(context, SQLITE_DB_NAME, null, SQLITE_DB_VERSION);
  }


  private String getCreateStorySQL() {
    return "CREATE TABLE " + TABLE_STORIES + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + COLUMN_STORY_ID + " TEXT, "
        + COLUMN_STORY_DATA + " TEXT "
        + ");";
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(getCreateStorySQL());
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}