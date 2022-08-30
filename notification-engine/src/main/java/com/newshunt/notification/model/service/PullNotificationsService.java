/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.service;

import com.newshunt.notification.model.internal.rest.server.PullNotificationRequest;

/**
 * Provides services related to pull notifications.
 *
 * @author anshul.jain
 */
public interface PullNotificationsService {

  void pullNotifications(String baseUrl, PullNotificationRequest pullNotificationRequest);
}
