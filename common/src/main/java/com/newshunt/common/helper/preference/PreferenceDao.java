/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.preference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * Created by karthik.r on 02/04/19.
 */
public class PreferenceDao {
  private PreferenceSqliteHelper dbHelper;
  private SQLiteDatabase database;


  public PreferenceDao() {
    dbHelper = new PreferenceSqliteHelper(CommonUtils.getApplication());
  }

  public void open() {
    if (database == null || !database.isOpen()) {
      database = dbHelper.getWritableDatabase();
    }

  }

  public void close() {
    if (database != null) {
      dbHelper.close();
      database = null;
    }
  }

  public boolean contains(String filename, String key) {
    String selection = PreferenceSqliteHelper.COL_FILE + "=? AND "
        + PreferenceSqliteHelper.COL_KEY + "=?";
    String[] selectionArgs = new String[] {filename, key};
    Cursor cursor = null;
    try {
      cursor = dbHelper.query(null, selection, selectionArgs, null);
      return cursor != null && cursor.moveToFirst();
    }
    catch (Exception ex) {
      Logger.caughtException(ex);
    }
    finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return false;
  }

  public void savePreference(String filename, String key, String value) {
    ContentValues values = new ContentValues();
    values.put(PreferenceSqliteHelper.COL_FILE, filename);
    values.put(PreferenceSqliteHelper.COL_KEY, key);
    values.put(PreferenceSqliteHelper.COL_DATA, value);
    dbHelper.insert(values);
  }

  public String getPreference(String filename, String key) {
    String selection = PreferenceSqliteHelper.COL_FILE + "=? AND "
        + PreferenceSqliteHelper.COL_KEY + "=?";
    String[] selectionArgs = new String[] {filename, key};
    Cursor cursor = null;
    try {
      cursor = dbHelper.query(null, selection, selectionArgs, null);
      if (cursor.moveToFirst()) {
        return cursor.getString(cursor.getColumnIndex(PreferenceSqliteHelper.COL_DATA));
      }
    }
    catch (Exception ex) {
      Logger.caughtException(ex);
    }
    finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return null;
  }

  public void remove(String filename, String key) {
    String selection = PreferenceSqliteHelper.COL_FILE + "=? AND "
        + PreferenceSqliteHelper.COL_KEY + "=?";
    String[] selectionArgs = new String[] {filename, key};
    dbHelper.delete(selection, selectionArgs);
  }
}

