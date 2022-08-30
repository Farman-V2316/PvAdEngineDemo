/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.notification.domain;

import com.newshunt.common.domain.Usecase;

/**
 * Provides pull notification specific use cases.
 *
 * @author anshul.jain
 */
public interface PullNotificationUsecase extends Usecase {

  void pullNotifications();
}
