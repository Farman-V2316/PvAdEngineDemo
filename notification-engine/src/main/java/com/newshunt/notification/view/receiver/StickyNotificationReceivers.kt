/*
 *  Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.notification.view.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.sticky.StickyAudioCommentaryStateReceiver
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.StickyNavModelType
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.notification.helper.NotificationUtils
import com.newshunt.notification.helper.StickyAudioPlayController
import com.newshunt.notification.model.manager.StickyNotificationsManager

/**
 * @author santhosh.kc
 */
class StickyNotificationFinishReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        Logger.d(StickyNotificationsManager.TAG, "StickyNotificationFinishReceiver - Enter")
        when (intent?.action) {
            NotificationConstants.INTENT_ACTION_STICKY_NOTIFICATION_FINISH -> {
                Logger.d(StickyNotificationsManager.TAG, "Received broadcast intent for finish")
                val id = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID)
                val type = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE)
                Logger.d(StickyNotificationsManager.TAG, "going to call manager to remove from DB" +
                        " notification with " + (id ?: "empty id") + " and type : " + (type
                        ?: "empty type"))
                type?.let {
                    when(it){
                        StickyNavModelType.NEWS.stickyType ->{NotificationUtils.convertNewsStickyItemsToNormalNotificationsInBackground(false)
                        }
                        else -> {}
                    }
                }
                StickyNotificationsManager.onNotificationComplete(id, type)
            }
            NotificationConstants.INTENT_ACTION_STICKY_EXPIRY_TIME_CHANGED -> {
                Logger.d(StickyNotificationsManager.TAG, "Received broadcase intent for sticky " +
                        "expiry time changed")
                val id = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID)
                val type = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE)
                val expiryTime = intent.getLongExtra(NotificationConstants
                        .INTENT_EXTRA_STICKY_EXPIRY_TIME, 0)
                StickyNotificationsManager.onExpiryTimeChanged(id, type, expiryTime)
            }
            DailyhuntConstants.STICKY_NOTIFICATION_CLOSE_ACTION -> {
                Logger.d(StickyNotificationsManager.TAG, "Received broadcast intent for " +
                        "notification dismissed from tray")
                val id = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID)
                val type = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE)
                Logger.d(StickyNotificationsManager.TAG, "going to opt out " +
                        " notification with " + (id ?: "empty id") + " and type : " + (type
                        ?: "empty type"))
                val optOutDeeplink = intent?.getStringExtra(NotificationConstants.INTENT_STICKY_NOTIFICATION_OPT_OUT_DEEPLINK)
                val snackMeta = intent?.getSerializableExtra(NotificationConstants.SNACK_BAR_META);
                val pageReferrer = PageReferrer(NhGenericReferrer.NOTIFICATION, id, null,
                        NhAnalyticsUserAction.CLICK)
                if (!CommonUtils.isEmpty(optOutDeeplink)) {
                    val targetIntent = CommonNavigator.getDeepLinkLauncherIntent(
                            optOutDeeplink, false, pageReferrer)
                    targetIntent.putExtra(Constants.FLAG_STICKY_NOTIFICATION_LANDING, true)
                    if(snackMeta != null) {
                        targetIntent.putExtra(NotificationConstants.SNACK_BAR_META, snackMeta)
                    }
                    targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(targetIntent)
                    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                }
                StickyNotificationsManager.onNotificationDimissedFromTrayByUser(id, type)
            }
            NotificationConstants.INTENT_ACTION_REMOVE_FROM_TRAY_JOB_DONE -> {
                Logger.d(StickyNotificationsManager.TAG, "Received broadcast intent for " +
                        "notification removed from tray job done")
                val id = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID)
                val type = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE)
                type?.let {
                    when(it){
                        StickyNavModelType.NEWS.stickyType ->{NotificationUtils.convertNewsStickyItemsToNormalNotificationsInBackground(false)
                        }
                        else -> {}
                    }
                }
                StickyNotificationsManager.onNotificationRemovedFromTrayJobDone(id, type)
            }
            NotificationConstants.INTENT_ACTION_STICKY_NOTIFICATION_RESCHEDULE -> {
                Logger.d(StickyNotificationsManager.TAG, "Received broadcast intent for " +
                        "notification rescheduled")
                val id = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_ID)
                val type = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE)
                val newStartTime = intent?.getLongExtra(NotificationConstants
                        .INTENT_EXTRA_STICKY_RESCHEDULE_TIME, 0) ?: return
                StickyNotificationsManager.onNotificationRescheduled(id, type, newStartTime)
            }
        }
        Logger.d(StickyNotificationsManager.TAG, "StickyNotificationFinishReceiver - Exit")
    }
}

object StickyAudioCommentaryStateAppCallback : StickyAudioCommentaryStateReceiver.Callback {
    override fun onCommentaryStateChanged(updatedIntent: Intent?) {
        val stickyAudioCommentary = updatedIntent?.getSerializableExtra(NotificationConstants.INTENT_EXTRA_STICKY_AUDIO_STATE)
        if (stickyAudioCommentary != null) {
            Logger.d(StickyNotificationsManager.TAG, "Received intent sticky commentary audio update " +
                    "$stickyAudioCommentary")
        } else {
            Logger.d(StickyNotificationsManager.TAG, " Audio stream is not opted by user or " +
                    "notification is removed")
        }
        StickyAudioPlayController.getAudioCommentaryLiveData().postValue(stickyAudioCommentary)
    }
}

class StickyNotificationStartReceiver(): BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        Logger.d(StickyNotificationsManager.TAG, "StickyNotificationStartReceiver - Enter")
        when (intent?.action) {
            NotificationConstants.INTENT_ACTION_STICKY_SERVICE_STARTED -> {
                val type = intent?.getStringExtra(NotificationConstants.INTENT_EXTRA_STICKY_TYPE)
                Logger.d(StickyNotificationsManager.TAG, "Received broadcast intent for started notification of type $type")
                type?.let{
                    StickyNotificationsManager.onNotificationStarted(it)
                }
            }
            else -> {}
        }
    }

}