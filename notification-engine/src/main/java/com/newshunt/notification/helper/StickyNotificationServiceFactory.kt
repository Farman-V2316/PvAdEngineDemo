/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
*/
package com.newshunt.notification.helper

import com.newshunt.dataentity.notification.StickyNavModelType
import com.newshunt.notification.view.service.CricketStickyService
import com.newshunt.notification.view.service.GenericStickyService
import com.newshunt.notification.view.service.NewsStickyService

/**
 * Factory implementation to select the right class for Sticky services
 *
 * Created by srikanth.r on 10/19/21.
 */
object StickyNotificationServiceFactory {
    fun getServiceClass(stickyNavModelType: String?): Class<*> {
        return when (StickyNavModelType.from(stickyNavModelType)) {
            StickyNavModelType.CRICKET -> CricketStickyService::class.java
            StickyNavModelType.NEWS -> NewsStickyService::class.java
            else -> GenericStickyService::class.java
        }
    }
}