package com.newshunt.dataentity.dhutil.model.entity.upgrade

import android.app.PendingIntent

data class NotificationCTAResponse(val notificationCta: List<NotificationConfig>?,
                                   val version:String)

data class NotificationConfig(val notifType: String,
                              val attributes: List<NotificationAttributeItem>)

data class NotificationAttributeItem(val notifSubType: String,
                                     val cta: List<NotificationCtaObj>)

data class NotificationCtaObj(val type: String,
                           val placement: String? = null)

data class NotificationCtaUi(val ctaString: String,
                             val resourceId: Int,
                             val targetIntent: PendingIntent)