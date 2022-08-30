/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.view.view;

import android.content.Intent;

import com.newshunt.dataentity.notification.asset.DataStreamResponse;

/**
 * Created by anshul on 18/09/17.
 */

public interface StickyNotificationView {

  void buildNotification(boolean isUpdate, boolean enableHeadsUpNotificatio, String state);

  void handleStreamDataSuccess(DataStreamResponse dataStreamResponse);


  void handleStreamDataError(DataStreamResponse dataStreamResponse);

  void handleAction(String action, Intent intent);

  void setServiceCallbackAsNull();

  void handleAudioChanged(Intent intent);
}
