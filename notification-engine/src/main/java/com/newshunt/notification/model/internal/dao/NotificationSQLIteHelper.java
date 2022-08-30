package com.newshunt.notification.model.internal.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.NotificationPlacementType;

/**
 * Created by santosh.kumar on 3/8/2016.
 */
public class NotificationSQLIteHelper extends SQLiteOpenHelper {

  public static String SQLITE_DB_NAME = "dailyhunt.notifications";
  public static int SQLITE_DB_VERSION = 10;
  public static String TABLE_NOTIFICATION_INFO = "notification_info";
  public static String TABLE_NOTIFICATION_PRESENT_ID = "notification_present_id";
  public static String TABLE_DELETED_NOTIFICATION = "notification_delete";
  private static volatile NotificationSQLIteHelper instance;

  public static final String NOTIFICATION_PK = "pk";
  public static final String COLUMN_NOTI_ID = "not_id";
  public static final String COLUMN_NOTI_TIME_STAMP = "not_time_stamp";
  public static final String COLUMN_NOTI_PRIORITY = "not_priority";
  public static final String COLUMN_NOTI_SECTION = "not_section";
  public static final String COLUMN_NOTI_DATA = "not_data";
  public static final String COLUMN_NOTI_EXP_TIME = "not_expiry_time";
  public static final String COLUMN_NOTI_STATE = "not_state";
  public static final String COLUMN_NOTI_REMOVED_FROM_TRAY = "not_removed_from_tray";
  public static final String COLUMN_NOTI_IS_GROUPED = "not_grouped";
  public static final String COLUMN_NOTI_SEEN = "not_seen";
  public static final String COLUMN_NOTI_DELIVERY_MECHANISM = "not_delivery_mechanism";
  public static final String COLUMN_NOTI_SYNCED = "not_synced";
  public static final String COLUMN_NOTI_BASE_ID = "not_base_id";
  public static final String COLUMN_NOTI_DISPLAY_TIME = "not_display_time";
  public static final String COLUMN_NOTI_SHOWN_AS_HEADSUP = "not_shown_as_headsup";
  public static final String COLUMN_NOTI_REMOVED_BY_APP = "not_removed_by_app";
  public static final String COLUMN_NOTI_PENDING_POSTING = "not_pending_posting";
  public static final String COLUMN_NOTI_PLACEMENT = "not_placement";

  //The values can be cricket_sticky, football_sticky, politics_sticky etc. This will be based on
  // the type value in the notification payload.
  public static final String COLUMN_NOTI_TYPE = "type";
  public static final String COLUMN_NOTI_SUB_TYPE = "subType";

  public static final String COLUMN_NOTI_FILTER_TYPE = "filter_type";
  //add subtype


  private static final String ALTER_NOTI_TABLE_ADD_DELIVERY_MECHANISM =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_DELIVERY_MECHANISM +
          " integer DEFAULT 0";
  private static final String ALTER_NOTI_TABLE_ADD_IS_SYNCED =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_SYNCED +
          " integer DEFAULT 1";
  private static final String ALTER_NOTI_TABLE_ADD_BASE_ID =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_BASE_ID + " text";
  private static final String ALTER_NOTI_TABLE_ADD_DISPLAY_TIME =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_DISPLAY_TIME + " " +
          "text";
  private static final String UPDATE_EXPIRY_TIME_COLUMN =
      "UPDATE " + TABLE_NOTIFICATION_INFO + " SET " + COLUMN_NOTI_EXP_TIME + "=REPLACE( " +
          COLUMN_NOTI_EXP_TIME + ", 'expiryTime' ,'') WHERE " + COLUMN_NOTI_EXP_TIME +
          " LIKE 'expiryTime%'";

  private static final String ALTER_NOTI_TABLE_ADD_NOTI_TYPE =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_TYPE + " " +
          "text";

  private static final String ALTER_NOTI_TABLE_ADD_NOTI_SUB_TYPE =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_SUB_TYPE + " " +
          "text";

  private static final String ALTER_NOTI_TABLE_ADD_NOTI_HEADS_UP =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_SHOWN_AS_HEADSUP +
          " " +
          "boolean DEFAULT 1";

  private static final String ALTER_NOTI_TABLE_ADD_NOTI_REMOVED_BY_APP =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_REMOVED_BY_APP +
          " " +
          "boolean DEFAULT 1";

  private static final String ALTER_NOTI_TABLE_ADD_NOTI_PENDING_POST =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_PENDING_POSTING +
          " boolean DEFAULT 0";

  private static final String ALTER_NOTI_TABLE_ADD_NOTI_PLACEMENT =
      "ALTER TABLE " + TABLE_NOTIFICATION_INFO + " ADD COLUMN " + COLUMN_NOTI_PLACEMENT +
          " text DEFAULT '" + NotificationPlacementType.TRAY_AND_INBOX + '\'';

  private static final String ALTER_NOTI_ID_TABLE_ADD_NOTI_TYPE =
      "ALTER TABLE " + TABLE_NOTIFICATION_PRESENT_ID + " ADD COLUMN " + COLUMN_NOTI_FILTER_TYPE +
          " text DEFAULT '" + Constants.EMPTY_STRING + '\'';

  private NotificationSQLIteHelper(Context context) {
    super(context, SQLITE_DB_NAME, null, SQLITE_DB_VERSION);
  }

  public static NotificationSQLIteHelper getInstance(Context context) {
    if (instance == null) {
      synchronized (NotificationSQLIteHelper.class) {
        if (instance == null) {
          instance = new NotificationSQLIteHelper(context);
        }
      }
    }
    return instance;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(getCreateNotificationInfoSQL());
    db.execSQL(getCreatePresentNotificationIdSql());
    db.execSQL(getCreateDeletedNotificationSql());
  }

  private String getCreateNotificationInfoSQL() {
    String query = "create table IF NOT EXISTS "
        + TABLE_NOTIFICATION_INFO + "(" + NOTIFICATION_PK
        + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_NOTI_ID + " text UNIQUE,"
        + COLUMN_NOTI_DATA + " BLOB,"
        + COLUMN_NOTI_EXP_TIME + " text,"
        + COLUMN_NOTI_PRIORITY + " integer,"
        + COLUMN_NOTI_SECTION + " text,"
        + COLUMN_NOTI_STATE + " integer,"
        + COLUMN_NOTI_TIME_STAMP + " text,"
        + COLUMN_NOTI_REMOVED_FROM_TRAY + " boolean,"
        + COLUMN_NOTI_SEEN + " boolean,"
        + COLUMN_NOTI_IS_GROUPED + " boolean,"
        + COLUMN_NOTI_DELIVERY_MECHANISM + " integer DEFAULT 0,"
        + COLUMN_NOTI_SYNCED + " integer DEFAULT 0,"
        + COLUMN_NOTI_BASE_ID + " text,"
        + COLUMN_NOTI_TYPE + " text,"
        + COLUMN_NOTI_SUB_TYPE + " text, "
        + COLUMN_NOTI_SHOWN_AS_HEADSUP + " boolean,"
        + COLUMN_NOTI_REMOVED_BY_APP + " boolean,"
        + COLUMN_NOTI_DISPLAY_TIME + " text, "
        + COLUMN_NOTI_PENDING_POSTING + " boolean,"
        + COLUMN_NOTI_PLACEMENT + " text "
        + ')';
    return query;
  }

  private String getCreatePresentNotificationIdSql() {
    String query =
        "create table IF NOT EXISTS " + TABLE_NOTIFICATION_PRESENT_ID + "(" + NOTIFICATION_PK
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NOTI_ID + " text UNIQUE,"
            + COLUMN_NOTI_BASE_ID + " text,"
            + COLUMN_NOTI_FILTER_TYPE + " integer"
            + ')';
    return query;
  }

  private String getCreateDeletedNotificationSql() {
    String query =
        "create table IF NOT EXISTS " + TABLE_DELETED_NOTIFICATION + "(" + NOTIFICATION_PK
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NOTI_BASE_ID + " text UNIQUE,"
            + COLUMN_NOTI_TIME_STAMP + " text default \'0\',"
            + COLUMN_NOTI_SYNCED + " boolean default 0" + ')';
    return query;
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 2) {
      db.execSQL(ALTER_NOTI_TABLE_ADD_DELIVERY_MECHANISM);
      db.execSQL(ALTER_NOTI_TABLE_ADD_IS_SYNCED);
      db.execSQL(ALTER_NOTI_TABLE_ADD_BASE_ID);
      // Initially base_id column will have null values.So issue(seeing duplicate
      // notification in tray) might occur in case we get the duplicate notification just after
      // the upgrade but once the table is refreshed, behaviour will be as expected.
    }
    if (oldVersion < 3) {
      db.execSQL(ALTER_NOTI_TABLE_ADD_DISPLAY_TIME);
      db.execSQL(UPDATE_EXPIRY_TIME_COLUMN);
    }

    if (oldVersion < 4) {
      db.execSQL(ALTER_NOTI_TABLE_ADD_NOTI_TYPE);
      db.execSQL(ALTER_NOTI_TABLE_ADD_NOTI_SUB_TYPE);
    }

    if (oldVersion < 5) {
      db.execSQL(ALTER_NOTI_TABLE_ADD_NOTI_HEADS_UP);
    }

    if (oldVersion < 6) {
      db.execSQL(ALTER_NOTI_TABLE_ADD_NOTI_REMOVED_BY_APP);
    }

    if (oldVersion < 7) {
      db.execSQL(ALTER_NOTI_TABLE_ADD_NOTI_PENDING_POST);
    }

    if (oldVersion < 8) {
      db.execSQL(ALTER_NOTI_TABLE_ADD_NOTI_PLACEMENT);
    }

    if (oldVersion < 9) {
      db.execSQL(getCreatePresentNotificationIdSql());
    }

    if (oldVersion == 9) {
      //This table was added in version 9 without this column.
      // version less than 9 will create complete table with this column so not required to alter
      // table
      db.execSQL(ALTER_NOTI_ID_TABLE_ADD_NOTI_TYPE);
    }

    if (oldVersion < 10) {
      db.execSQL(getCreateDeletedNotificationSql());
    }
    Logger.d("NotificationDB","Old Notification DB Upgradation Completed");

  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    try {
      CommonUtils.getApplication().deleteDatabase(SQLITE_DB_NAME);
    } catch (Exception ex) {
      // Do nothing
    }

    // Call super, so app will crash and restart will new DB file. This is one time fix.
    super.onDowngrade(db, oldVersion, newVersion);
  }
}
