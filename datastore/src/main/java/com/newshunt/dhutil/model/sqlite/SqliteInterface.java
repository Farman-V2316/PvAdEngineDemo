/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.sqlite;

import android.database.sqlite.SQLiteDatabase;

/**
 * An interface for passing newly created instance of database.
 *
 * @author anshul.jain on 2/4/2016.
 */
public interface SqliteInterface {

  void onReceivedDatabaseInstance(SQLiteDatabase newDatabase);
}
