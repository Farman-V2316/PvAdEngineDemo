/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.dao;

import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism;
import com.newshunt.notification.model.entity.NotificationId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by santosh.kumar on 3/7/2016.
 */

/**
 * This SQLite Dao is moved to Room implementation of NotificationDao.
 */
@Deprecated
public interface NotificationDao {

  void addNotification(BaseModel notificationModel, boolean postUIUpdate);

  List<BaseModel> getNonDeferredNotifications(boolean excludeTrayOnly);

  List<BaseModel> getDeferredNotifications();

  List<NewsNavModel> getNonDeferredNonStickyNewsSectionNotifications();

  List<BaseModel> getTopNonDeferredNonStickyNotificationsForTray(int limit);

  List<BaseModel> getGroupedNonDeferredNonStickyNotifications();

  List<BaseModel> getNonGroupedNonDeferredNonStickyNotification();

  List<BaseModel> getUnpostedNonDeferredNonStickyNotification();

  void markNotificationAsRead(String notId);

  void markAllNotificationAsSeen();

  void markNotificationAsDeletedFromTray(int notId);

  void markNotificationAsPostedToTray(int notId);

  void markGroupedNotificationAsDeletedFromTray();

  void markAllNotificationAsDeletedFromTray();

  void markAllNotificationsAsSynced();

  List<String> getUnsyncedNotificationIdList(int limit);

  void deleteNotification(int notId);

  void deleteNotifications(String[] notIds);

  int deleteSocialNotification();

  BaseModel getNotification(int notId);

  NotificationDeliveryMechanism getNotificationDeliveryTypeByBaseInfoId(String baseInfoId);

  boolean isDuplicateNotification(BaseModel baseModel);

  boolean isDuplicateNotificationInfo(BaseModel baseModel);

  boolean isNotificationDeleted(String baseInfoId);

  List<NotificationId> getNotificationToDelete();

  void markAllNotificationSynced();

  void insertNotificationToDelete(List<NotificationId> notificationIds);

  void deleteAllDeleteNotificationSynced();

  int getUnseenNotificationCount();

  void deleteOldNonDeferredNotifications();

  void deleteOldDeferredNotifications();

  void clearIdList();

  void clearIdListWithFilter(int filter);
}
