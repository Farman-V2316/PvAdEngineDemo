/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.internal.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.common.model.entity.BaseDataResponse;
import com.newshunt.dataentity.notification.AdsNavModel;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.BaseModelType;
import com.newshunt.dataentity.notification.DeeplinkModel;
import com.newshunt.dataentity.notification.ExploreNavModel;
import com.newshunt.dataentity.notification.FollowNavModel;
import com.newshunt.dataentity.notification.GroupNavModel;
import com.newshunt.dataentity.notification.LiveTVNavModel;
import com.newshunt.dataentity.notification.NavigationModel;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism;
import com.newshunt.dataentity.notification.NotificationPlacementType;
import com.newshunt.dataentity.notification.NotificationSectionType;
import com.newshunt.dataentity.notification.ProfileNavModel;
import com.newshunt.dataentity.notification.SearchNavModel;
import com.newshunt.dataentity.notification.SocialCommentsModel;
import com.newshunt.dataentity.notification.StickyNavModel;
import com.newshunt.dataentity.notification.TVNavModel;
import com.newshunt.dataentity.notification.WebNavModel;
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset;
import com.newshunt.dataentity.notification.asset.CricketNotificationAsset;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.dhutil.helper.AppSettingsProvider;
import com.newshunt.notification.helper.NotificationLogger;
import com.newshunt.notification.helper.NotificationSyncHelperKt;
import com.newshunt.notification.helper.NotificationUtils;
import com.newshunt.notification.model.dao.NotificationDao;
import com.newshunt.notification.model.entity.NotificationDeleteEntity;
import com.newshunt.notification.model.entity.NotificationEntity;
import com.newshunt.notification.model.entity.NotificationId;
import com.newshunt.notification.model.entity.NotificationPresentEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation for NotificationDao
 *
 * @author santosh.kumar on 3/7/2016.
 */
/**
 * This SQLite Dao Implementation is moved to Room implementation of NotificationDao.
 */
@Deprecated
public class NotificationDaoImpl implements NotificationDao {

  public static final long NOTIFICATION_EXPIRY_TIME = 7 * 24 * 60 * 60 * 1000; // 7 days
  private static final Gson gson = new Gson();
  private static NotificationDaoImpl instance;
  private final NotificationSQLIteHelper dbHelper;
  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
  private SQLiteDatabase database;
  private static final String DELETE_NOT_PRESENT_NOTIFICATION_ID =
      "DELETE FROM "
          + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " "
          + "WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_ID + " NOT IN "
          + " (SELECT " + NotificationSQLIteHelper.COLUMN_NOTI_ID + " FROM "
          + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + ")";

  private NotificationDaoImpl() {
    dbHelper = NotificationSQLIteHelper.getInstance(CommonUtils.getApplication());
    deleteOldNonDeferredNotifications();
    deleteOldDeferredNotifications();
    clearExpiredNotifications();
  }

  public static synchronized NotificationDaoImpl getInstance() {
    if (null == instance) {
      instance = new NotificationDaoImpl();
    }
    return instance;
  }

  public void addDeferredNotification(BaseModel notificationModel, boolean postUIUpdate) {
    autoOpen();
    ContentValues values = setNotificationInfo(notificationModel);
    if (values == null) {
      return;
    }
    try {
      readWriteLock.writeLock().lock();
      database.insertWithOnConflict(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, null,
          values, SQLiteDatabase.CONFLICT_REPLACE);

      ContentValues idValue = new ContentValues();
      idValue.put(NotificationSQLIteHelper.COLUMN_NOTI_ID,
          notificationModel.getBaseInfo().getUniqueId());
      idValue.put(NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID,
          notificationModel.getBaseInfo().getId());
      database.insertWithOnConflict(NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID, null,
          idValue, SQLiteDatabase.CONFLICT_REPLACE);
    } finally {
      readWriteLock.writeLock().unlock();
    }

    // updating the UI

    if (postUIUpdate) {
      Handler handler = new Handler(Looper.getMainLooper());
      handler.post(() -> AppSettingsProvider.INSTANCE.getNotificationLiveData().postValue(getUnseenNotificationCount()>0));
    }
  }

  @Override
  public void addNotification(BaseModel notificationModel, boolean postUIUpdate) {
    autoOpen();
    ContentValues values = setNotificationInfo(notificationModel);
    if (values == null) {
      return;
    }
    boolean needToInsertInfo = !isDuplicateNotificationInfo(notificationModel);
    try {
      readWriteLock.writeLock().lock();
      if (needToInsertInfo) {
        database.insertWithOnConflict(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO,
            null, values,
            SQLiteDatabase.CONFLICT_REPLACE);
      }
      ContentValues idValue = new ContentValues();
      idValue.put(NotificationSQLIteHelper.COLUMN_NOTI_ID,
          notificationModel.getBaseInfo().getUniqueId());
      idValue.put(NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID,
          notificationModel.getBaseInfo().getId());
      if (notificationModel.getFilterValue() == NotificationSyncHelperKt.NOTIFICATION_FILTER_ALL &&
          !notificationModel.isFullSync()) {
        boolean isSocial =
            (notificationModel.getBaseInfo() != null) &&
                ("social".equalsIgnoreCase(notificationModel.getBaseInfo().getNotifType()));
        if (isSocial) {
          idValue.put(NotificationSQLIteHelper.COLUMN_NOTI_FILTER_TYPE,
              NotificationSyncHelperKt.NOTIFICATION_FILTER_SOCIAL);
        } else {
          idValue.put(NotificationSQLIteHelper.COLUMN_NOTI_FILTER_TYPE,
              NotificationSyncHelperKt.NOTIFICATION_FILTER_CONTENT);
        }
      } else {
        idValue.put(NotificationSQLIteHelper.COLUMN_NOTI_FILTER_TYPE,
            notificationModel.getFilterValue());
      }
      database.insertWithOnConflict(NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID, null,
          idValue, SQLiteDatabase.CONFLICT_REPLACE);
    } finally {
      readWriteLock.writeLock().unlock();
    }

    // updating the UI

    if (postUIUpdate) {
      Handler handler = new Handler(Looper.getMainLooper());
      handler.post(new Runnable() {
        @Override
        public void run() {
          AppSettingsProvider.INSTANCE.getNotificationLiveData().postValue(getUnseenNotificationCount()>0);
        }
      });
    }
  }

  @Override
  public void deleteOldNonDeferredNotifications() {
    autoOpen();
    long expiryTime = System.currentTimeMillis() - NOTIFICATION_EXPIRY_TIME;
    String deleteQuery =
        "DELETE FROM "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP +
            " < " + expiryTime;
    try {
      readWriteLock.writeLock().lock();
      database.execSQL(deleteQuery);
      database.execSQL(DELETE_NOT_PRESENT_NOTIFICATION_ID);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void deleteOldDeferredNotifications() {
    autoOpen();
    long expiryTime = System.currentTimeMillis() - NOTIFICATION_EXPIRY_TIME;
    String deleteQuery =
        "DELETE FROM "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is not null AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME +
            " < " + expiryTime;
    try {
      readWriteLock.writeLock().lock();
      database.execSQL(deleteQuery);
      database.execSQL(DELETE_NOT_PRESENT_NOTIFICATION_ID);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void clearIdList() {
    autoOpen();
    try {
      readWriteLock.writeLock().lock();
      database.execSQL("DELETE FROM " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void clearIdListWithFilter(int filter) {
    autoOpen();
    try {
      readWriteLock.writeLock().lock();
      database.execSQL("DELETE FROM " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID +
          " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_FILTER_TYPE + "&" + filter +
          " IS NOT 0");
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  public void clearExpiredNotifications() {
    autoOpen();
    long currentTime = System.currentTimeMillis();
    String deleteQuery =
        "DELETE FROM "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " WHERE "
            + NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME + " is not null AND "
            + NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME + " < " + currentTime;
    try {
      readWriteLock.writeLock().lock();
      database.execSQL(deleteQuery);
      database.execSQL(DELETE_NOT_PRESENT_NOTIFICATION_ID);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  /**
   * This function will return all the notifications which are not deferred and are not of type
   * sticky. To show in the notification inbox and tray we will always call this function.
   */
  @Override
  public List<BaseModel> getNonDeferredNotifications(boolean excludeTrayOnly) {
    autoOpen();
    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " where " + NotificationSQLIteHelper.COLUMN_NOTI_TYPE + " is not '" +
            NotificationConstants.NOTIFICATION_TYPE_STICKY
            + "' AND " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null ORDER BY " +
            NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP + " DESC";

    String excludeTrayOnlySelectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " where " + NotificationSQLIteHelper.COLUMN_NOTI_TYPE + " is not '" +
            NotificationConstants.NOTIFICATION_TYPE_STICKY
            + "' AND "
            + NotificationSQLIteHelper.COLUMN_NOTI_PLACEMENT + " is not '" +
            NotificationPlacementType.TRAY_ONLY
            + "' AND "
            + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null ORDER BY " +
            NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP + " DESC";
    try {
      readWriteLock.readLock().lock();
      return executeGetNotificationQuery(
          excludeTrayOnly ? excludeTrayOnlySelectQuery : selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public List<NotificationEntity> getAllNotificationInfoEntries() {
      String query = "SELECT * FROM notification_info";
      try {
          readWriteLock.readLock().lock();
        Logger.d("NotificationDB","Start Notification Info Entries");
          return executeGetNotificationInfoQuery(query);
      } finally {
        Logger.d("NotificationDB","End Notification Info Entries");
          readWriteLock.readLock().unlock();
      }
  }

  public List<NotificationPresentEntity> getAllNotificationPresentEntries() {
      String query = "SELECT * FROM notification_present_id";
      try {
          readWriteLock.readLock().lock();
          return executeGetPresentNotificationQuery(query);
      } finally {
           readWriteLock.readLock().unlock();
      }
  }

    public ArrayList<NotificationDeleteEntity> getAllNotificationDeleteEntries() {
        String query = "SELECT * FROM notification_delete";
        try {
            readWriteLock.readLock().lock();
            return executeGetDeleteNotificationQuery(query);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }


  /**
   * A method to return all the notifcations which are
   * sticky -> type = 'sticky'
   * non deferred -> display_time = 0
   * expiry time has not passed -> expiredTime> current time or expiryTime is null or expiryTime
   * is empty
   * not dismissed by the user -> not removed from tray =0
   *
   * @return
   */
  public List<BaseModel> getNonDeferredStickyNotifications() {
    long currentTime = System.currentTimeMillis();
    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " where " + NotificationSQLIteHelper.COLUMN_NOTI_TYPE + "='"
            + NotificationConstants.NOTIFICATION_TYPE_STICKY
            + "' AND " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null "
            + " AND " + NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY + "=0"
            + " AND (" + NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME + ">" + currentTime
            + " OR " + NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME + " is null OR " +
            NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME + "='' )";
    try {
      readWriteLock.readLock().lock();
      return executeGetNotificationQuery(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  /**
   * A method to return all the notifcations which are
   * sticky -> type = 'sticky'
   * non deferred -> display_time = 0
   * expiry time has not passed -> expiredTime> current time or expiryTime is null or expiryTime
   * is empty
   *
   * @return
   */
  public List<BaseModel> getNonExpiredStickyNotifications() {
    long currentTime = System.currentTimeMillis();
    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " where " + NotificationSQLIteHelper.COLUMN_NOTI_TYPE + "='"
            + NotificationConstants.NOTIFICATION_TYPE_STICKY
            + "' AND " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null "
            + " AND (" + NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME + ">" + currentTime
            + " OR " + NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME + " is null OR " +
            NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME + "='' )";
    try {
      readWriteLock.readLock().lock();
      return executeGetNotificationQuery(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  //This function will return all the notifications which are  deferred. This will be used for
  // scheduling the jobs in case of mobile switch on after off and phone upgrade.
  @Override
  public List<BaseModel> getDeferredNotifications() {
    autoOpen();
    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " where " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME +
            " is not null ORDER " +
            "BY " + NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP + " DESC";
    try {
      readWriteLock.readLock().lock();
      return executeGetNotificationQuery(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  //This will return both deferred and non-deferred notifications.
  private int getAllNotificationsCount() {
    autoOpen();
    String query =
        "SELECT COUNT(*) FROM "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID;
    try {
      readWriteLock.readLock().lock();
      return executeQueryAndGetCount(query);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }


  @Override
  public ArrayList<NewsNavModel> getNonDeferredNonStickyNewsSectionNotifications() {
    autoOpen();
    NotificationSectionType.NEWS.toString();
    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_SECTION + " = '" +
            NotificationSectionType.NEWS + "'   AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_TYPE + " is not '" +
            NotificationConstants.NOTIFICATION_TYPE_STICKY + "'"
            + " ORDER BY " + NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP + " DESC";

    try {
      readWriteLock.readLock().lock();
      return executeGetNewsNotificationQuery(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public List<BaseModel> getTopNonDeferredNonStickyNotificationsForTray(int limit) {
    autoOpen();
    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_STATE + " = '" +
            NotificationConstants.NOTIFICATION_STATUS_UNREAD + "'"
            + " AND " + NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY + " = '" + 0 +
            "'  AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_TYPE + " is not '" +
            NotificationConstants.NOTIFICATION_TYPE_STICKY + "'"
            + " ORDER BY " + NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP + " DESC"
            + " LIMIT " + limit;
    try {
      readWriteLock.readLock().lock();
      return executeGetNotificationQuery(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public List<BaseModel> getUnpostedNonDeferredNonStickyNotification() {
    autoOpen();
    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_STATE + " = '" +
            NotificationConstants.NOTIFICATION_STATUS_UNREAD + "'"
            + " AND " + NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY + " ='0'"
            + " AND " + NotificationSQLIteHelper.COLUMN_NOTI_PENDING_POSTING + "='1'  AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_TYPE + " is not '" +
            NotificationConstants.NOTIFICATION_TYPE_STICKY + "'"
            + " ORDER BY " + NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP;

    try {
      readWriteLock.readLock().lock();
      return executeGetNotificationQuery(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public List<BaseModel> getGroupedNonDeferredNonStickyNotifications() {
    autoOpen();
    String notificationPlacementFilter =
        queryConditionForAllowedPlacement(NotificationPlacementType.TRAY_ONLY.name(),
            NotificationPlacementType.TRAY_AND_INBOX.name());

    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_STATE + " = '" +
            NotificationConstants.NOTIFICATION_STATUS_UNREAD + "'"
            + " AND " + NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY + " ='0'"
            + " AND " + NotificationSQLIteHelper.COLUMN_NOTI_IS_GROUPED + "='1'  AND " +
            "(" + notificationPlacementFilter + ")" + " AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_TYPE + " is not '" +
            NotificationConstants.NOTIFICATION_TYPE_STICKY + "'"
            + " ORDER BY " + NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP;

    try {
      readWriteLock.readLock().lock();
      return executeGetNotificationQuery(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public static String queryConditionForAllowedPlacement(String... args) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      sb.append(NotificationSQLIteHelper.COLUMN_NOTI_PLACEMENT);
      sb.append(" IS ");
      sb.append("'");
      sb.append(args[i]);
      sb.append("'");
      if (i != args.length - 1) {
        sb.append(" OR ");
      }
    }
    return sb.toString();
  }

  @Override
  public List<BaseModel> getNonGroupedNonDeferredNonStickyNotification() {
    autoOpen();
    String notificationPlacementFilter =
        queryConditionForAllowedPlacement(NotificationPlacementType.TRAY_ONLY.name(),
            NotificationPlacementType.TRAY_AND_INBOX.name());

    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_STATE + " = '" +
            NotificationConstants.NOTIFICATION_STATUS_UNREAD + "'"
            + " AND " + NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY + " ='0'"
            + " AND " + NotificationSQLIteHelper.COLUMN_NOTI_IS_GROUPED + "='0'  AND " +
            "(" + notificationPlacementFilter + ")" + " AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_TYPE + " is not '" +
            NotificationConstants.NOTIFICATION_TYPE_STICKY + "'"
            + " ORDER BY " + NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP;

    try {
      readWriteLock.readLock().lock();
      return executeGetNotificationQuery(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void markNotificationAsRead(final String notId) {
    CommonUtils.runInBackground(() -> {
      autoOpen();
      ContentValues values = new ContentValues();
      values.put(NotificationSQLIteHelper.COLUMN_NOTI_STATE,
          NotificationConstants.NOTIFICATION_STATUS_READ);
      values.put(NotificationSQLIteHelper.COLUMN_NOTI_SEEN, true);
      String selection = NotificationSQLIteHelper.COLUMN_NOTI_ID + "=?";
      String[] selectionArgs = {String.valueOf(notId)};
      try {
        readWriteLock.writeLock().lock();
        database.update(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, values,
            selection, selectionArgs);
      } finally {
        readWriteLock.writeLock().unlock();
      }
    });
  }

  @Override
  public void markAllNotificationAsSeen() {
    autoOpen();
    ContentValues values = new ContentValues();
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_SEEN, true);
    String whereClause = NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null AND " +
        NotificationSQLIteHelper.COLUMN_NOTI_TYPE + " is not '" +
        NotificationConstants.NOTIFICATION_TYPE_STICKY + "'";

    try {
      readWriteLock.writeLock().lock();
      database.update(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, values, whereClause, null);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void markNotificationAsPostedToTray(int notId) {
    autoOpen();
    ContentValues values = new ContentValues();
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_PENDING_POSTING, false);
    String selection = NotificationSQLIteHelper.COLUMN_NOTI_ID + "=?";
    String[] selectionArgs = {String.valueOf(notId)};
    try {
      readWriteLock.writeLock().lock();
      database.update(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, values,
          selection, selectionArgs);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void markNotificationAsDeletedFromTray(int notId) {
    autoOpen();
    ContentValues values = new ContentValues();
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY, true);
    String selection = NotificationSQLIteHelper.COLUMN_NOTI_ID + "=?";
    String[] selectionArgs = {String.valueOf(notId)};
    try {
      readWriteLock.writeLock().lock();
      database.update(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, values,
          selection, selectionArgs);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void markGroupedNotificationAsDeletedFromTray() {
    autoOpen();
    String updateQuery =
        "UPDATE "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO
            + " SET " + NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY + "=1"
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_IS_GROUPED + "='1'";
    try {
      readWriteLock.writeLock().lock();
      database.execSQL(updateQuery);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  /**
   * This method marks all the notifications as removed from tray which are not sticky and which
   * are not deferred.
   */
  @Override
  public void markAllNotificationAsDeletedFromTray() {
    autoOpen();
    String updateQuery =
        "UPDATE "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO
            + " SET " + NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY + "=1"
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_STATE + " = '" +
            NotificationConstants.NOTIFICATION_STATUS_UNREAD + "' AND " + NotificationSQLIteHelper
            .COLUMN_NOTI_DISPLAY_TIME + " is null AND " + NotificationSQLIteHelper
            .COLUMN_NOTI_TYPE + " is not '" + NotificationConstants.NOTIFICATION_TYPE_STICKY + "'";
    try {
      readWriteLock.writeLock().lock();
      database.execSQL(updateQuery);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }


  @Override
  public int getUnseenNotificationCount() {
    /***
     * @note : this function is only used for getting count for unseen notification and set badge
     * on notification inbox icon. but if we use this for any different purpose than will need to
     * add condition to include "TRAY_ONLY" notification if required.
     */
    autoOpen();
    String query =
        " SELECT COUNT(*) FROM " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID +
            " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME + " is null AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_SEEN + "='0' AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_TYPE + " is not '" +
            NotificationConstants.NOTIFICATION_TYPE_STICKY + "' AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_PLACEMENT + " is not '" +
            NotificationPlacementType.TRAY_ONLY.name()
            + '\'';
    try {
      readWriteLock.readLock().lock();
      return executeQueryAndGetCount(query);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void markAllNotificationsAsSynced() {
    autoOpen();
    ContentValues values = new ContentValues();
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_SYNCED, true);
    try {
      readWriteLock.writeLock().lock();
      database.update(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, values,
          null, null);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  /**
   * This method is used to get a list of all notifications which are not synced. These
   * notification ids are passed as a parameter in a pull request.
   *
   * @param limit
   * @return
   */
  @Override
  public List<String> getUnsyncedNotificationIdList(int limit) {
    autoOpen();
    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_SYNCED + " = '0'"
            + " ORDER BY " + NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP + " DESC "
            + (limit > 0 ? " LIMIT " + limit : "");
    return executeGetNotificationIdListQuery(selectQuery);
  }

  @Override
  public void deleteNotification(int notId) {
    autoOpen();
    String selection = NotificationSQLIteHelper.COLUMN_NOTI_ID + "=?";
    String[] selectionArgs = {String.valueOf(notId)};
    try {
      readWriteLock.writeLock().lock();
      database.delete(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, selection, selectionArgs);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  public void deleteNotifications(String[] notIds) {
    autoOpen();
    String selection =
        NotificationSQLIteHelper.COLUMN_NOTI_ID + " IN (" +
            new String(new char[notIds.length - 1]).replace("\0", "?,") + "?)";
    try {
      readWriteLock.writeLock().lock();
      database.delete(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, selection, notIds);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public int deleteSocialNotification() {
    int result = -1;
    autoOpen();
    String selection = NotificationSQLIteHelper.COLUMN_NOTI_SECTION + "=?";
    String[] selectionArgs = {String.valueOf(NotificationSectionType.SOCIAL_SECTION)};
    try {
      readWriteLock.writeLock().lock();
      result = database.delete(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, selection,
          selectionArgs);
    } finally {
      readWriteLock.writeLock().unlock();
    }
    return result;
  }

  @Override
  public BaseModel getNotification(int notId) {
    autoOpen();
    List<BaseModel> notifications;
    String selectQuery =
        "SELECT * FROM "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_ID + " = '" + notId + "'";
    notifications = executeGetNotificationQuery(selectQuery);
    if (null != notifications && notifications.size() > 0) {
      return notifications.get(0);
    }
    return null;
  }

  @Override
  public NotificationDeliveryMechanism getNotificationDeliveryTypeByBaseInfoId(String baseInfoId) {
    NotificationDeliveryMechanism deliveryMechanism = null;
    autoOpen();
    String selectQuery =
        "SELECT " + NotificationSQLIteHelper.COLUMN_NOTI_DELIVERY_MECHANISM + " FROM "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID + " = '" + baseInfoId + "'";

    Cursor cursor = null;
    try {
      readWriteLock.readLock().lock();
      cursor = database.rawQuery(selectQuery, null);
      if (cursor != null && cursor.moveToFirst()) {
        deliveryMechanism = NotificationDeliveryMechanism.fromDeliveryType(cursor.getInt(0));
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      readWriteLock.readLock().unlock();
    }
    return deliveryMechanism;
  }

  @Override
  public boolean isDuplicateNotificationInfo(BaseModel baseModel) {
    if (null == baseModel || null == baseModel.getBaseInfo()) {
      return false;
    }
    String baseInfoId = baseModel.getBaseInfo().getId();

    autoOpen();
    String selectQuery =
        "SELECT COUNT(" + NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID + ") FROM "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID + " = '" + baseInfoId + "'";

    return executeQueryAndGetCount(selectQuery) > 0;
  }

  @Override
  public boolean isNotificationDeleted(String baseInfoId) {
    autoOpen();
    String selectQuery =
        "SELECT COUNT(" + NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID + ") FROM "
            + NotificationSQLIteHelper.TABLE_DELETED_NOTIFICATION
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID + " = '" + baseInfoId + "'";

    return executeQueryAndGetCount(selectQuery) > 0;
  }

  @Override
  public List<NotificationId> getNotificationToDelete() {
    autoOpen();
    String selectQuery =
        "select * from " + NotificationSQLIteHelper.TABLE_DELETED_NOTIFICATION +
            " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_SYNCED + " is 0";

    try {
      readWriteLock.readLock().lock();
      return executeGetDeletedNotificationQuery(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void deleteAllDeleteNotificationSynced() {
    autoOpen();
    String query = "DELETE FROM "
        + NotificationSQLIteHelper.TABLE_DELETED_NOTIFICATION + " "
        + "WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_SYNCED + " IS 1";

    try {
      readWriteLock.writeLock().lock();
      database.execSQL(query);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void markAllNotificationSynced() {
    autoOpen();
    ContentValues values = new ContentValues();
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_SYNCED, true);
    String whereClause = NotificationSQLIteHelper.COLUMN_NOTI_SYNCED + " is 0";

    try {
      readWriteLock.writeLock().lock();
      database.update(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, values, whereClause, null);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void insertNotificationToDelete(List<NotificationId> notificationIds) {
    autoOpen();
    try {
      readWriteLock.writeLock().lock();
      for (NotificationId notificationId : notificationIds) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID, notificationId.getId());
        contentValues.put(NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP, notificationId.getTs());
        contentValues.put(NotificationSQLIteHelper.COLUMN_NOTI_SYNCED, false);
        database.insertWithOnConflict(NotificationSQLIteHelper.TABLE_DELETED_NOTIFICATION, null,
            contentValues, SQLiteDatabase.CONFLICT_REPLACE);
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public boolean isDuplicateNotification(BaseModel baseModel) {
    if (null == baseModel || null == baseModel.getBaseInfo()) {
      return false;
    }
    String baseInfoId = baseModel.getBaseInfo().getId();

    autoOpen();
    String selectQuery =
        "SELECT COUNT(" + NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID + ") FROM "
            + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID + " = '" + baseInfoId + "'";

    return executeQueryAndGetCount(selectQuery) > 0;

  }

  private void autoOpen() {
    if (null == database || !database.isOpen()) {
      database = dbHelper.getWritableDatabase();
    }
  }

  private int executeQueryAndGetCount(String query) {
    readWriteLock.readLock().lock();
    Cursor cursor = database.rawQuery(query, null);
    int count = 0;
    try {
      if (cursor != null && cursor.moveToFirst()) {
        count = cursor.getInt(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      readWriteLock.readLock().unlock();
      if (cursor != null) {
        cursor.close();
      }
    }
    return count;
  }

  private List<BaseModel> executeGetNotificationQuery(String selectQuery) {
    List<BaseModel> notifications = new ArrayList<>();
    Cursor cursor = null;
    try {
      readWriteLock.readLock().lock();
      cursor = database.rawQuery(selectQuery, null);

      if (cursor == null) {
        return notifications;
      }
      if (cursor.moveToFirst()) {
        notifications = readNotifications(cursor);
      }
      return notifications;
    } catch (Exception e) {
      Logger.caughtException(e);
      return notifications;
    } finally {
      readWriteLock.readLock().unlock();
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private List<NotificationEntity> executeGetNotificationInfoQuery(String selectQuery) {
    Logger.d("NotificationDB","End Notification Info Entries Query");
    List<NotificationEntity> notifications = new ArrayList<>();
    Cursor cursor = null;
    try {
      readWriteLock.readLock().lock();
      cursor = database.rawQuery(selectQuery, null);

      if (cursor == null) {
        Logger.d("NotificationDB","Cursor is Null");
        return notifications;
      }
      if (cursor.moveToFirst()) {
        Logger.d("NotificationDB","End Notification Info Entries Start Reading");
        notifications = readInfoNotifications(cursor);
      }
      return notifications;
    } catch (Exception e) {
      Logger.d("NotificationDB","Inside executeGetNotificationInfoQuery");
      Logger.caughtException(e);
      return notifications;
    } finally {
      readWriteLock.readLock().unlock();
      if (cursor != null) {
        cursor.close();
      }
    }
  }

    private List<NotificationPresentEntity> executeGetPresentNotificationQuery(String selectQuery) {
        List<NotificationPresentEntity> notifications = new ArrayList<>();
        Cursor cursor = null;
        try {
            readWriteLock.readLock().lock();
            cursor = database.rawQuery(selectQuery, null);

            if (cursor == null) {
                return notifications;
            }
            if (cursor.moveToFirst()) {
                notifications = readPresentNotifications(cursor);
            }
            return notifications;
        } catch (Exception e) {
            Logger.caughtException(e);
            return notifications;
        } finally {
            readWriteLock.readLock().unlock();
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private ArrayList<NotificationDeleteEntity> executeGetDeleteNotificationQuery(String selectQuery) {
        ArrayList<NotificationDeleteEntity> notifications = new ArrayList<>();
        Cursor cursor = null;
        try {
            readWriteLock.readLock().lock();
            cursor = database.rawQuery(selectQuery, null);

            if (cursor == null) {
                return notifications;
            }
            if (cursor.moveToFirst()) {
                notifications = readDeleteNotifications(cursor);
            }
            return notifications;
        } catch (Exception e) {
            Logger.caughtException(e);
            return notifications;
        } finally {
            readWriteLock.readLock().unlock();
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private ArrayList<NotificationId> executeGetDeletedNotificationQuery(String selectQuery) {
    ArrayList<NotificationId> notifications = new ArrayList<>();
    Cursor cursor = null;
    try {
      readWriteLock.readLock().lock();
      cursor = database.rawQuery(selectQuery, null);

      if (cursor == null) {
        return notifications;
      }
      if (cursor.moveToFirst()) {
        notifications = readDeletedNotifications(cursor);
      }
      return notifications;
    } catch (Exception e) {
      Logger.caughtException(e);
      return notifications;
    } finally {
      readWriteLock.readLock().unlock();
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private List<String> executeGetNotificationIdListQuery(String selectQuery) {
    List<String> notificationIds = new ArrayList<>();
    Cursor cursor = null;
    try {
      readWriteLock.readLock().lock();
      cursor = database.rawQuery(selectQuery, null);

      if (cursor == null) {
        return notificationIds;
      }
      while (cursor.moveToNext()) {
        notificationIds.add(cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
            .COLUMN_NOTI_BASE_ID)));
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    } finally {
      readWriteLock.readLock().unlock();
      if (cursor != null) {
        cursor.close();
      }
    }
    return notificationIds;
  }

  private List<BaseModel> readNotifications(Cursor cursor) {
    ArrayList<BaseModel> notifications = new ArrayList<>();
    do {
      String sectionStr = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
          .COLUMN_NOTI_SECTION));
      NotificationSectionType sectionType = NotificationSectionType.getSectionType(sectionStr);
      byte[] dataBlob =
          cursor.getBlob(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_DATA));
      String dataJson = new String(dataBlob);
      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.enableComplexMapKeySerialization().create();
      BaseInfo baseInfo = null;
      BaseModel baseModel = null;
      String notificationType = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
          .COLUMN_NOTI_TYPE));
      String notificationSubType = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
          .COLUMN_NOTI_SUB_TYPE));
      try {
        baseModel = extraBaseModel(notificationType, notificationSubType, sectionType, dataJson,
            gson);
      } catch (Exception e) {
        Logger.caughtException(e);
        baseModel = NotificationUtils.handleWrongExpiryTimeValue(baseModel, sectionType,
            dataJson, gson);
      }

      if (baseModel != null) {
        baseInfo = baseModel.getBaseInfo();
      }

      if (baseInfo != null) {
        //Update the required values
        int state =
            cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_STATE));
        int isRemovedFromTray = cursor.getInt(
            cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY));
        int isGrouped =
            cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_IS_GROUPED));
        int isSynced =
            cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_SYNCED));
        int deliveryMechanism = cursor.getInt(
            cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_DELIVERY_MECHANISM));

        baseInfo.setState(state);
        baseInfo.setIsRemovedFromTray(isRemovedFromTray == 1);
        baseInfo.setIsGrouped(isGrouped == 1);
        baseModel.getBaseInfo().setDeliveryType(
            NotificationDeliveryMechanism.fromDeliveryType(deliveryMechanism));
        baseModel.getBaseInfo().setIsSynced(isSynced == 1);
      }

      if (baseModel != null) {
        notifications.add(baseModel);
      }
    } while (cursor.moveToNext());


    return notifications;
  }

    private ArrayList<NotificationPresentEntity> readPresentNotifications(Cursor cursor) {
        ArrayList<NotificationPresentEntity> notifications = new ArrayList<>();
        do {
            String id = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
                    .COLUMN_NOTI_ID));
            String baseId = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
                    .COLUMN_NOTI_BASE_ID));
            int filterType = cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper
                    .COLUMN_NOTI_FILTER_TYPE));
            notifications.add(new NotificationPresentEntity(0,id,baseId,filterType));
        } while (cursor.moveToNext());

        return notifications;
    }

    private ArrayList<NotificationEntity> readInfoNotifications(Cursor cursor) {
    ArrayList<NotificationEntity> notifications = new ArrayList<>();
    do {
      Logger.d("NotificationDB","Start Adding NotificationEntity");
      String sectionStr = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
              .COLUMN_NOTI_SECTION));
      NotificationSectionType sectionType = NotificationSectionType.getSectionType(sectionStr);
      byte[] dataBlob =
              cursor.getBlob(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_DATA));
      String notificationType = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
              .COLUMN_NOTI_TYPE));
      String notificationSubType = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
              .COLUMN_NOTI_SUB_TYPE));
      String timeStamp = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP));
      int priority = cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_PRIORITY));
      String expiryTime = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME));
      int seen = cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_SEEN));


      int state =
              cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_STATE));
      int isRemovedFromTray = cursor.getInt(
              cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY));
      int isGrouped =
              cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_IS_GROUPED));
      int isSynced =
              cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_SYNCED));
      int deliveryMechanism = cursor.getInt(
              cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_DELIVERY_MECHANISM));

      String displayTime = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME));
      int headsUp = cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_SHOWN_AS_HEADSUP));
      int removedByApp = cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_BY_APP));
      int pendingPosting = cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_PENDING_POSTING));
      String placement = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_PLACEMENT));
      String type = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_TYPE));
      String subType = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_SUB_TYPE));
      ;
      int id = cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper
              .COLUMN_NOTI_ID));
      String baseId = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
              .COLUMN_NOTI_BASE_ID));
      notifications.add(new NotificationEntity(0,String.valueOf(id),timeStamp,priority,sectionStr,dataBlob,expiryTime,state,isRemovedFromTray==1,isGrouped==1,
              seen==1,deliveryMechanism,isSynced==1,baseId,displayTime,headsUp==1,removedByApp==1,pendingPosting==1,
              placement,type,subType,null,true,0,null));
      Logger.d("NotificationDB","NotificationEntity Added");
    } while (cursor.moveToNext());

    return notifications;
  }


    private ArrayList<NotificationDeleteEntity> readDeleteNotifications(Cursor cursor) {
        ArrayList<NotificationDeleteEntity> notifications = new ArrayList<>();
        do {
            String baseId = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
                    .COLUMN_NOTI_BASE_ID));
            int filterType = cursor.getInt(cursor.getColumnIndex(NotificationSQLIteHelper
                    .COLUMN_NOTI_SYNCED));
            String timeStamp = cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper
                    .COLUMN_NOTI_TIME_STAMP));
            notifications.add(new NotificationDeleteEntity(0,baseId,filterType==0,timeStamp));
        } while (cursor.moveToNext());

        return notifications;
    }



    private ArrayList<NotificationId> readDeletedNotifications(Cursor cursor) {
    ArrayList<NotificationId> notifications = new ArrayList<>();
    do {
      NotificationId notificationId = null;
      String id =
          cursor.getString(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID));
      long ts =
          cursor.getLong(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP));
      try {
        notificationId = new NotificationId(id, ts);
      } catch (Exception e) {
        Logger.caughtException(e);
      }

      if (notificationId != null) {
        notifications.add(notificationId);
      }
    } while (cursor.moveToNext());


    return notifications;
  }

  private ArrayList<NewsNavModel> executeGetNewsNotificationQuery(String selectQuery) {
    ArrayList<NewsNavModel> notifications = new ArrayList<>();
    Cursor cursor = null;
    try {
      readWriteLock.readLock().lock();
      cursor = database.rawQuery(selectQuery, null);

      if (cursor == null) {
        return notifications;
      }
      if (cursor.moveToFirst()) {
        notifications = readNewsNotifications(cursor);
      }
      return notifications;
    } catch (Exception e) {
      Logger.caughtException(e);
      return notifications;
    } finally {
      readWriteLock.readLock().unlock();
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private ArrayList<NewsNavModel> readNewsNotifications(Cursor cursor) {
    ArrayList<NewsNavModel> notifications = new ArrayList<>();
    do {
      NewsNavModel navigationModel = null;
      byte[] dataBlob =
          cursor.getBlob(cursor.getColumnIndex(NotificationSQLIteHelper.COLUMN_NOTI_DATA));
      String dataJson = new String(dataBlob);
      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.enableComplexMapKeySerialization().create();
      try {
        navigationModel = gson.fromJson(dataJson, NewsNavModel.class);
      } catch (Exception e) {
        Logger.caughtException(e);
        navigationModel =
            (NewsNavModel) NotificationUtils.handleWrongExpiryTimeValue(navigationModel,
                NotificationSectionType.NEWS, dataJson, gson);
      }
      navigationModel.getBaseInfo().setState(cursor.getInt(cursor.getColumnIndex(
          NotificationSQLIteHelper.COLUMN_NOTI_STATE)));
      notifications.add(navigationModel);
    } while (cursor.moveToNext());
    return notifications;
  }

  private ContentValues setNotificationInfo(BaseModel notification) {

    if (notification == null || notification.getBaseModelType() == null) {
      return null;
    }
    ContentValues values = new ContentValues();
    BaseInfo baseInfo = null;
    switch (notification.getBaseModelType()) {
      case NEWS_MODEL:
      case TV_MODEL:
      case ADS_MODEL:
      case WEB_MODEL:
      case NAVIGATION_MODEL:
      case STICKY_MODEL:
      case LIVETV_MODEL:
      case EXPLORE_MODEL:
      case FOLLOW_MODEL:
      case DEEPLINK_MODEL:
      case SOCIAL_COMMENTS_MODEL:
      case PROFILE_MODEL:
      case SEARCH_MODEL:
      case GROUP_MODEL:
        baseInfo = notification.getBaseInfo();
        break;
      default:
        return null;
    }
    String jsonData = gson.toJson(notification);
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_DATA, jsonData.getBytes());
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_ID, baseInfo.getUniqueId());
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_PRIORITY, baseInfo.getPriority());
    //TODO anshul.jain Why is this value coming empty from Notification BE.?
    if (baseInfo.getSectionType() != null) {
      values.put(NotificationSQLIteHelper.COLUMN_NOTI_SECTION,
          baseInfo.getSectionType().toString());
    }
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_STATE,
        NotificationConstants.NOTIFICATION_STATUS_UNREAD);

    long timeStamp = baseInfo.getTimeStamp() > 0 ? baseInfo.getTimeStamp() : System.currentTimeMillis();
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_TIME_STAMP, timeStamp);
    long expiryTime = baseInfo.getExpiryTime() > 0 ? baseInfo.getExpiryTime() : (timeStamp + NOTIFICATION_EXPIRY_TIME);
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_EXP_TIME, expiryTime);

    values.put(NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY, false);
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_IS_GROUPED, false);
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_SEEN, false);
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_DELIVERY_MECHANISM,
        baseInfo.getDeliveryType().getValue());
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_SYNCED, baseInfo.isSynced());
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_BASE_ID, baseInfo.getId());
    if (baseInfo.isDeferred()) {
      values.put(NotificationSQLIteHelper.COLUMN_NOTI_DISPLAY_TIME, baseInfo.getV4DisplayTime());
    }
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_SHOWN_AS_HEADSUP, false);
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_TYPE, baseInfo.getType());
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_SUB_TYPE, baseInfo.getSubType());
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_PENDING_POSTING, 1);

    if (baseInfo.getPlacement() == null) {
      values.put(NotificationSQLIteHelper.COLUMN_NOTI_PLACEMENT,
          NotificationPlacementType.TRAY_AND_INBOX.name());
    } else {
      values.put(NotificationSQLIteHelper.COLUMN_NOTI_PLACEMENT,
          baseInfo.getPlacement().name());
    }
    return values;
  }

  public int getAnalyticsCount(AppSection appSection, boolean isRead) {
    if (appSection == null) {
      return 0;
    }
    autoOpen();
    String appSectionFilter = NotificationConstants.NOTIFICATION_SECTION_NEWS;

    String query =
        " SELECT COUNT(*) FROM " + NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID +
            " INNER JOIN " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " ON " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_PRESENT_ID + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + " = " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + "." +
            NotificationSQLIteHelper.COLUMN_NOTI_ID
            + " WHERE " + NotificationSQLIteHelper.COLUMN_NOTI_STATE + "='" +
            (isRead ? NotificationConstants
                .NOTIFICATION_STATUS_READ : NotificationConstants
                .NOTIFICATION_STATUS_UNREAD) + "' AND " + NotificationSQLIteHelper
            .COLUMN_NOTI_SECTION + " = '" + appSectionFilter + "'";
    try {
      readWriteLock.readLock().lock();
      return executeQueryAndGetCount(query);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  /**
   * Return true if the notification is already been read from the tray.
   *
   * @param notificationId
   * @return
   */
  public boolean isNotificationRemovedFromTray(int notificationId) {
    String selectQuery =
        "select " + NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY + " from " +
            "" + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " where " +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + "=" + notificationId;

    Cursor cursor = null;
    try {
      readWriteLock.readLock().lock();
      cursor = database.rawQuery(selectQuery, null);

      if (cursor == null) {
        return false;
      }
      if (cursor.moveToFirst()) {
        String isRemoved = cursor.getString(0);
        if (NotificationConstants.REMOVED_FROM_TRAY_VALUE.equals(isRemoved)) {
          return true;
        }
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    } finally {
      readWriteLock.readLock().unlock();
      if (cursor != null) {
        cursor.close();
      }
    }
    return false;
  }

  private BaseModel extraBaseModel(String notificationType, String
      notificationSubType, NotificationSectionType sectionType, String dataJson, Gson gson) {
    BaseModel baseModel = null;

    if (NotificationConstants.NOTIFICATION_TYPE_STICKY.equals(notificationType) && !CommonUtils.isEmpty
        (notificationSubType)) {

      switch (notificationSubType) {
        case NotificationConstants.STICKY_CRICKET_TYPE:
          Type collectionType =
              new TypeToken<StickyNavModel<CricketNotificationAsset, CricketDataStreamAsset>>() {
              }.getType();
          baseModel = gson.fromJson(dataJson, collectionType);
          break;
      }
      return baseModel;
    }
    switch (sectionType) {
      case APP:
        baseModel = gson.fromJson(dataJson, NavigationModel.class);
        break;
      case NEWS:
        baseModel = gson.fromJson(dataJson, NewsNavModel.class);
        break;
      case TV:
        baseModel = gson.fromJson(dataJson, TVNavModel.class);
        break;
      case LIVETV:
        baseModel = gson.fromJson(dataJson, LiveTVNavModel.class);
        break;
      case WEB:
        baseModel = gson.fromJson(dataJson, WebNavModel.class);
        break;
      case ADS:
        baseModel = gson.fromJson(dataJson, AdsNavModel.class);
        break;
      case EXPLORE_SECTION:
        baseModel = gson.fromJson(dataJson, ExploreNavModel.class);
        break;
      case FOLLOW_SECTION:
        baseModel = gson.fromJson(dataJson, FollowNavModel.class);
        break;
      case DEEPLINK_SECTION:
        baseModel = gson.fromJson(dataJson, DeeplinkModel.class);
        break;
      case SOCIAL_SECTION:
        baseModel = gson.fromJson(dataJson, SocialCommentsModel.class);
        break;
      case PROFILE_SECTION:
        baseModel = gson.fromJson(dataJson, ProfileNavModel.class);
        break;
      case SEARCH_SECTION:
        baseModel = gson.fromJson(dataJson, SearchNavModel.class);
        break;
      case GROUP_SECTION:
        baseModel = gson.fromJson(dataJson, GroupNavModel.class);
    }
    return baseModel;
  }

  /**
   * This method is used to update the notification with the following params.
   *
   * @param isRemoved  - Whether the notificaiton is removed from the tray
   * @param expiryTime - The update expiryTime of the notification
   * @param baseModel  - The notification
   */
  public void updateRemovedFromTrayAndExpiryTime(boolean isRemoved, long expiryTime, BaseModel
      baseModel) {
    if (baseModel == null || baseModel.getBaseInfo() == null) {
      return;
    }
    baseModel.getBaseInfo().setExpiryTime(expiryTime);
    ContentValues values = setNotificationInfo(baseModel);
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_FROM_TRAY, isRemoved);
    String selection = NotificationSQLIteHelper.COLUMN_NOTI_ID + "=?";
    String[] selectionArgs = {String.valueOf(baseModel.getBaseInfo().getUniqueId())};

    try {
      readWriteLock.writeLock().lock();
      database.update(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, values,
          selection, selectionArgs);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }


  /**
   * A utility function to tell whether a notification was shown up as heads up before or not.
   *
   * @param notificationId - Notification id.
   * @return - True if it was, false otherwise
   */
  public boolean isNotificationAlreadyShownAsHeadsUp(int notificationId) {
    autoOpen();
    String selectQuery =
        "SELECT " + NotificationSQLIteHelper.COLUMN_NOTI_SHOWN_AS_HEADSUP + " FROM " +
            NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " WHERE " +
            NotificationSQLIteHelper.COLUMN_NOTI_ID + "=" + notificationId + " AND " +
            NotificationSQLIteHelper.COLUMN_NOTI_SHOWN_AS_HEADSUP + "=" +
            NotificationConstants.NOTI_SHOWN_AS_HEADS_UP + " AND " + NotificationSQLIteHelper
            .COLUMN_NOTI_REMOVED_BY_APP + "=" + NotificationConstants.NOTI_REMOVED_FROM_TRAY_BY_APP;
    int queryCount;
    try {
      readWriteLock.readLock().lock();
      queryCount = executeQueryAndGetCount(selectQuery);
    } finally {
      readWriteLock.readLock().unlock();
    }
    return queryCount > 0;
  }

  /**
   * A helper function to mark that the notification was posted as heads up.
   *
   * @param notificationId -  Notification id.
   */
  public void markNotificationAsHeadsUp(int notificationId) {
    autoOpen();
    ContentValues values = new ContentValues();
    values.put(NotificationSQLIteHelper.COLUMN_NOTI_SHOWN_AS_HEADSUP, true);
    String selection = NotificationSQLIteHelper.COLUMN_NOTI_ID + "=?";
    String[] selectionArgs = {String.valueOf(notificationId)};
    readWriteLock.writeLock().lock();
    try {
      database.update(NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO, values,
          selection, selectionArgs);
    } finally {
      readWriteLock.writeLock().unlock();
    }
    NotificationLogger.logMarkNotificationAsHeadsUpInDB(notificationId);
  }

  /**
   * A helper function to mark that the notification was posted as heads up.
   *
   * @param notificationIds -  Notification id.
   */
  public void markNotificationAsRemovedFromTrayByApp(List<Integer> notificationIds) {
    autoOpen();
    String str = TextUtils.join(Constants.COMMA_CHARACTER, notificationIds);
    String updateQuery = "UPDATE " + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " SET " +
        "" + NotificationSQLIteHelper.COLUMN_NOTI_REMOVED_BY_APP + "=1 WHERE " +
        "" + NotificationSQLIteHelper.COLUMN_NOTI_ID + " IN (" + str + ")";
    readWriteLock.writeLock().lock();
    try {
      database.execSQL(updateQuery);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }


  /**
   * A helper function to mark a list of notifications as grouped.
   *
   * @param notificationIds -  Notification id.
   */
  public void markNotificationsAsGrouped(List<Integer> notificationIds) {
    autoOpen();
    String str = TextUtils.join(Constants.COMMA_CHARACTER, notificationIds);
    String updateQuery = "UPDATE " + NotificationSQLIteHelper.TABLE_NOTIFICATION_INFO + " SET " +
        "" + NotificationSQLIteHelper.COLUMN_NOTI_IS_GROUPED + "=1 WHERE " +
        "" + NotificationSQLIteHelper.COLUMN_NOTI_ID + " IN (" + str + ")";
    readWriteLock.writeLock().lock();
    try {
      database.execSQL(updateQuery);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }
}
