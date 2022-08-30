/*
 * Copyright (c) 2020 . All rights reserved.
 */
package com.newshunt.news.model.sqlite.legacy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.model.entity.OfflineArticle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author satosh.dhanyamraju
 */
@Deprecated
public class OfflineArticleSQLiteDao implements OfflineArticleDao {
  private static final String[] allColumns = {
      OfflineSQLiteHelper.COLUMN_ID, //0
      OfflineSQLiteHelper.COLUMN_STORY_ID, //1
      OfflineSQLiteHelper.COLUMN_STORY_DATA //2
  };
  private static final int STORY_DATA_INDEX = 2;
  private OfflineSQLiteHelper dbHelper;
  private SQLiteDatabase database;

  public OfflineArticleSQLiteDao(Context context) {
    Context context1 = context.getApplicationContext();
    dbHelper = new OfflineSQLiteHelper(context1);
  }

  @Override
  public void open() {
    if (database == null || !database.isOpen()) {
      database = dbHelper.getWritableDatabase();
    }
  }

  @Override
  public void close() {
    if (database != null) {
      dbHelper.close();
      database = null;
    }
  }

  @Override
  public List<OfflineArticle> getOfflineArticles(int maxCount) {
    List<OfflineArticle> stories = new ArrayList<>();
    Cursor cursor = null;
    try {
      cursor =
          database.query(OfflineSQLiteHelper.TABLE_STORIES, allColumns, null, null, null,
              null, OfflineSQLiteHelper.COLUMN_ID + " DESC");
      if (cursor == null  || !cursor.moveToFirst()) {
        return stories;
      }
      do {
        OfflineArticle story = convertStringToStory(cursor.getString(STORY_DATA_INDEX));
        stories.add(story);
      } while (cursor.moveToNext());
      return stories;//for now
    } catch (Exception e) {
      Logger.caughtException(e);
      return stories;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  @Override
  public void clearAll() {
    database.delete(OfflineSQLiteHelper.TABLE_STORIES, null, null);
  }

  private static OfflineArticle convertStringToStory(String str) {
    Gson gson = new Gson();
    return gson.fromJson(str, OfflineArticle.class);
  }
}
