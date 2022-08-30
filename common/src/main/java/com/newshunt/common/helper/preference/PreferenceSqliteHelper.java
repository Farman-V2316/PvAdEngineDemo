package com.newshunt.common.helper.preference;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by karthik.r on 02/04/19.
 */
public class PreferenceSqliteHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "preference.db";
  private static final int DATABASE_VERSION = 1;

  public static final String TABLE_PREFERENCE = "preference";

  public static final String COL_ID = "_id";
  public static final String COL_FILE = "filename";
  public static final String COL_KEY = "keyname";
  public static final String COL_DATA = "data";

  public PreferenceSqliteHelper(Context context) {
    super(context, DATABASE_NAME , null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    createNewsPageDb(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

  private void createNewsPageDb(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PREFERENCE + " (" +
        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        COL_DATA + " TEXT, " +
        COL_KEY + " TEXT, " +
        COL_FILE + " TEXT, " +
        " UNIQUE (" + COL_KEY + "," + COL_FILE  + "))");
  }

  public void insert(ContentValues values) {
    getWritableDatabase().insertWithOnConflict(TABLE_PREFERENCE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
  }

  public void update(ContentValues values, String selection, String[] selectionArgs) {
    getWritableDatabase().update(TABLE_PREFERENCE, values, selection, selectionArgs);
  }

  public void delete(String selection, String[] selectionArgs) {
    getWritableDatabase().delete(TABLE_PREFERENCE, selection, selectionArgs);
  }

  public Cursor query(String[] projection, String selection, String[] selectionArgs, String
      orderBy) {
    return  getReadableDatabase().query(TABLE_PREFERENCE, projection, selection, selectionArgs, null, null,
        orderBy);
  }

  public void execSQL(String sql) {
    getWritableDatabase().execSQL(sql);
  }
}
