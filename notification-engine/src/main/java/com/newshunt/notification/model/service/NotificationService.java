/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.service;

import com.newshunt.dataentity.notification.AdjunctLangNavModel;
import com.newshunt.dataentity.notification.AdjunctLangStickyNavModel;
import com.newshunt.dataentity.notification.AdsNavModel;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.ExploreNavModel;
import com.newshunt.dataentity.notification.FlushNavModel;
import com.newshunt.dataentity.notification.FollowNavModel;
import com.newshunt.dataentity.notification.GroupNavModel;
import com.newshunt.dataentity.notification.InAppNotificationModel;
import com.newshunt.dataentity.notification.LiveTVNavModel;
import com.newshunt.dataentity.notification.NavigationModel;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.SearchNavModel;
import com.newshunt.dataentity.notification.SilentNotificationModel;
import com.newshunt.dataentity.notification.SilentVersionedApiTriggerModel;
import com.newshunt.dataentity.notification.SilentVersionedApiUpdateModel;
import com.newshunt.dataentity.notification.SocialCommentsModel;
import com.newshunt.dataentity.notification.TVNavModel;
import com.newshunt.dataentity.notification.WebNavModel;

/**
 * Interface to provide gcm registration id and payload
 *
 * @author santosh.kulkarni
 */
public interface NotificationService {

  void startNotificationService();

  void registerGCMId(String regId);

  void cancelNotificationsInTray();

  void cancelNotificationsAndMarkAsDeletedFromTray();

  void updateNotificationsInTray(boolean updateOnlyGrouped);

  void handleGCMPayload(NavigationModel notification);

  void handleNewsGCMPayload(NewsNavModel notification);

  void handleWebGcmPayload(WebNavModel webNavModel);

  void handleTVGCMPayload(TVNavModel notification);

  void handleLiveTVGCMPayload(LiveTVNavModel notification);

  void handleSocialCommentsGCMPayload(SocialCommentsModel socialCommentsModel);

  void handleSearchGCMPayload(SearchNavModel searchNavModel);

  void handleExploreGCMPayload(ExploreNavModel exploreNavModel);

  void handleFollowGCMPayload(FollowNavModel followNavModel);

  void handleGroupGCMPayload(GroupNavModel groupNavModel);

  void handleFlushGCMPayload(FlushNavModel flushNavModel);

  void handleAdjunctLangSilentPayload(AdjunctLangNavModel adjunctLangNavModel);

  void handleAdjunctLangStickyPayload(AdjunctLangStickyNavModel adjunctLangStickyNavModel);

  void handleInAppNotificationPayload(InAppNotificationModel inAppNotificationModel);

  void handleGCMPayLoad(BaseModel baseModel);

  void handleGCMPayload(AdsNavModel adsNavModel);

  void handleSilentNotificationPayload(SilentNotificationModel silentNotificationModel);

  void handleSilentVersionAPIUpdatePayload(SilentVersionedApiUpdateModel model);

  void handleSilentVersionedAPITriggerPayload(SilentVersionedApiTriggerModel model);

  void showSummary();

  void stickyStartLedTrayUpdate();
}