/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.notification.domain;

import com.newshunt.common.domain.Usecase;
import com.newshunt.dataentity.common.model.entity.statusupdate.StatusUpdateResponse;
import com.newshunt.dataentity.common.model.entity.statusupdate.StatusUpdateType;

/**
 * Provides notification specific use cases.
 *
 * @author shreyas.desai
 */
public interface GetNotificationUpdateUsecase extends Usecase {

  void registerGCMId(String clientId, String gcmId , boolean enabled, boolean cricketEnabled);

  void updateNotificationStatus(String clientId, boolean enabled, boolean cricketEnabled,
                                StatusUpdateType requestType);

  void onStatusUpdate(StatusUpdateResponse statusUpdateResponse);

}
