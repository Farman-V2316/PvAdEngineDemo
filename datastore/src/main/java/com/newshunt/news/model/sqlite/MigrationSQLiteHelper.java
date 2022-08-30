/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is one time code for phoenix release. It helps migrate existing preferences
 * to server and new app.
 * <p/>
 *
 * @author shreyas.desai
 */
public class MigrationSQLiteHelper extends SQLiteOpenHelper {
  public static final String SQLITE_DB_NAME = "NEWSHUNT";
  public static final int DATABASE_VERSION = 45;

  public static final String TABLE_TOPIC_DETAILS = "TOPIC_DETAILS";
  public static final String TABLE_TOPICS = "TOPICS";
  public static final String TABLE_SECTIONS = "SECTIONS";
  public static final String TABLE_PAPERS = "PAPERS";
  public static final String TABLE_CATEGORIES = "CATEGORIES";

  public static final String TOPIC_DETAIL_PK = "id";
  public static final String TOPIC_DETAIL_KEY = "key";
  public static final String TOPIC_DETAIL_NP_KEY = "npKey";
  public static final String TOPIC_DETAIL_CT_KEYS = "ctKeys";
  public static final String TOPIC_DETAIL_IS_FAVORITE = "isFavorite";
  public static final String TOPIC_DETAIL_GROUP_VIEW_COUNT = "groupViewCount";
  public static final String TOPIC_DETAIL_SRC_VIEW_COUNT = "srcViewCount";
  public static final String TOPIC_DETAIL_LAST_READ_CAT = "lastReadCatKey";
  public static final String TOPIC_DETAIL_TYPE = "type";
  public static final String TOPIC_DETAIL_DISP_NAME = "displayName";
  public static final String TOPIC_DETAIL_BG_IMG_LINK = "bgImgLink";
  public static final String TOPIC_DETAIL_LINKED_GROUP_KEY = "linkedGroupKey";
  public static final String TOPIC_DETAIL_IS_UPDATING = "isUpdating";
  public static final String TOPIC_DETAIL_ORDER = "paperOrder";

  public static final String[] ALL_COLUMNS = new String[]{
      TOPIC_DETAIL_PK, TOPIC_DETAIL_KEY, TOPIC_DETAIL_NP_KEY, TOPIC_DETAIL_CT_KEYS,
      TOPIC_DETAIL_IS_FAVORITE, TOPIC_DETAIL_GROUP_VIEW_COUNT, TOPIC_DETAIL_SRC_VIEW_COUNT,
      TOPIC_DETAIL_LAST_READ_CAT, TOPIC_DETAIL_TYPE, TOPIC_DETAIL_DISP_NAME,
      TOPIC_DETAIL_BG_IMG_LINK, TOPIC_DETAIL_LINKED_GROUP_KEY, TOPIC_DETAIL_IS_UPDATING,
      TOPIC_DETAIL_ORDER
  };

  public MigrationSQLiteHelper(Context context) {
    super(context, SQLITE_DB_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {

  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
  }

  public void cleanOldData() {
    SQLiteDatabase sqLiteDatabase = getWritableDatabase();

    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_TOPIC_DETAILS);
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_TOPICS);
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SECTIONS);
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PAPERS);
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
    sqLiteDatabase.close();
  }
}
