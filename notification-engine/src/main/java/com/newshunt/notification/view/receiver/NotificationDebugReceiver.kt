/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.notification.view.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism
import com.newshunt.notification.helper.NOTIFICATION_FILTER_ALL
import com.newshunt.notification.helper.NotificationHandler

/**
 * @author shrikant.agrawal
 */
class NotificationDebugReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.getBundleExtra("bundle")
        bundle?.let {
            NotificationHandler.handleNotificationData(
                NotificationDeliveryMechanism.TEST, it, false,
                NOTIFICATION_FILTER_ALL)
        }
    }
}