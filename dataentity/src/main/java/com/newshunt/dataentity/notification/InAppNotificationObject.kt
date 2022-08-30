/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

/**
 * Created by kajal.kumari on 25/04/22.
 */
data class InAppNotificationInfo(val startTimeMs: Long? = 0L,
                                 val endTimeMs: Long? = 0L,
                                 val notificationText: String?= null,
                                 val notificationIconUrl: String?= null,
                                 val notificationIconDarkUrl: String?= null,
                                 val notificationCta: String ?= null,
                                 val notificationCtaLink: String ?= null,
                                 val notificaitonTextColorTemplateId: String?= null,
                                 val ctaTextColorTemplateId: String ?= null,
                                 val pos: String ?= null)