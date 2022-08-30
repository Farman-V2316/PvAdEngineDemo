/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
*/
package com.newshunt.notification.view.service

import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.notification.view.view.GenericStickyNotificationView
import com.newshunt.notification.view.view.StickyNotificationView

/**
 * Generic Sticky foreground service implementation for elections  and other events
 *
 * Created by srikanth.r on 10/13/21.
 */
class GenericStickyService : StickyNotificationService() {
    override fun inflateNotificationView(): StickyNotificationView {
        val view = GenericStickyNotificationView(stickyNavModel, refresher, this)
        view.buildNotification(false, true, null)
        return view
    }

    override fun onCreate() {
        super.onCreate()
        buildDummyNotification(NotificationConstants.STICKY_GENERIC_TYPE.hashCode())
    }
}