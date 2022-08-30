/*
 * Created by Rahul Ravindran at 25/9/19 11:44 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by karthik on 20/11/17.
 */

public class TimespentEventSqliteHelper extends SQLiteOpenHelper {

    public static final String SQLITE_DB_NAME = "newshunt.timespent";
    public static final int SQLITE_DB_VERSION = 1;

    //tables
    public static final String TABLE_NAME = "timespent";

    //columns
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final String COLUMN_PARAM_NAME = "paramname";
    public static final String COLUMN_PARAM_VALUE = "paramvalue";

    public TimespentEventSqliteHelper(Context context) {
        super(context, SQLITE_DB_NAME, null, SQLITE_DB_VERSION);
    }

    private String getCreateTableSQL() {
        return "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_EVENT_ID + " TEXT, "
                + COLUMN_PARAM_NAME + " TEXT, "
                + COLUMN_PARAM_VALUE + " TEXT "
                + ");";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateTableSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
