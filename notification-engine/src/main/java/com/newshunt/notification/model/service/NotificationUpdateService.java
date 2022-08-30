/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.service;

import com.newshunt.dataentity.common.model.entity.statusupdate.StatusUpdateType;

/**
 * Provides services related to notification update.
 *
 * @author shreyas.desai
 */
public interface NotificationUpdateService {

  void registerGCMId(String baseUrl, String clientId, String gcmId, boolean enabled, boolean cricketEnabled);

  void updateNotificationStatus(String baseUrl, String clientId, boolean enabled, StatusUpdateType requestType);

}
