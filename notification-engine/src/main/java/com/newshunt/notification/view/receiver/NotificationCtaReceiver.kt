package com.newshunt.notification.view.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.DebugHeaderProvider.partnerRef
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.AdjunctLangStickyNavModel
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.dataentity.notification.BaseModelType
import com.newshunt.dataentity.notification.NotificationCtaTypes
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.util.NewsConstants
import com.newshunt.notification.analytics.NhNotificationAnalyticsUtility
import com.newshunt.notification.analytics.NotificationActionAnalyticsHelper
import com.newshunt.notification.helper.NotificationRemoveFromTrayHelper
import com.newshunt.notification.helper.NotificationRouterHelper
import com.newshunt.notification.helper.NotificationServiceProvider
import com.newshunt.notification.model.entity.NotificationFilterType
import com.newshunt.notification.model.entity.NotificationInvalidType
import com.newshunt.notification.sqlite.NotificationDB

const val LOG_TAG = "NotificationCTA"

class NotificationCtaReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        Logger.d(LOG_TAG, "Received the notification cta callback receiver")

        if (intent == null) return

        val bundle = intent.extras ?: return

        Logger.d(LOG_TAG, "Processing the cta action")

        try {

            val adjunctNotification = bundle.getInt(NotificationConstants.ADJUNCT_NOTI_ID)
            if(adjunctNotification != 0) {
                val adjunctModelStr = bundle.getString(Constants.NOTIFICATION_BASE_MODEL)
                val adjunctModel = BaseModelType.convertStringToBaseModel(adjunctModelStr,BaseModelType.ADJUNCT_MESSAGE,null)
                val isAdjunctTick = bundle.getBoolean(NotificationConstants.ADJUNCT_NOTI_ACTION_TICK)
                bundle.getString(NotificationConstants.ADJUNCT_CTA_DEEPLINK_URL)
                    ?.let {
                        NotificationActionAnalyticsHelper.logAdjunctStickyNotificationExploreEvent(adjunctModel,isAdjunctTick)
                        handleAdjunctStickyNotification(context,adjunctNotification, it,adjunctModel)
                    }
                return
            }

            val notificationModelString = bundle.getSerializable(Constants.NOTIFICATION_BASE_MODEL) as String?
            val baseModelTypeString = bundle.getSerializable(Constants.NOTIFICATION_BASE_MODEL_TYPE) as String?
            val baseModelStickyTypeString = bundle.getSerializable(Constants.NOTIFICATION_BASE_MODEL_STICKY_TYPE) as String?
            val baseModelType = BaseModelType.getValue(baseModelTypeString)
            val notificationModel = BaseModelType.convertStringToBaseModel(notificationModelString, baseModelType,
                    baseModelStickyTypeString)

            //Fire Notification event as Client Filter when the baseModel is null.
            if (notificationModel == null || notificationModel.baseModelType == null) {
                NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType.INVALID,
                    NotificationInvalidType.BASE_MODEL_NULL.type + " In notification router", notificationModel)
                return
            }

            val referrerId = if (notificationModel.baseInfo != null) notificationModel.baseInfo.id else Constants.EMPTY_STRING
            val pageReferrer = PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
            val targetIntent = NotificationRouterHelper.routeToModel(notificationModel, context, pageReferrer = pageReferrer)
            val action = intent.action
            if (targetIntent != null && action != null) {
                routeForCtaAction(context, targetIntent, action, pageReferrer,baseModelType)
            }
            CommonUtils.runInBackground {

                NotificationDB.instance().getNotificationDao().markNotificationAsRead(notificationModel.baseInfo.uniqueId.toString())


                //In case of sticky notifications, we are already firing action events from other places.
                if (notificationModel.baseModelType != BaseModelType.STICKY_MODEL) {
                    NotificationActionAnalyticsHelper.logNotificationActionEvent(null, notificationModel, null, null, false)
                    NotificationDB.instance().getNotificationPrefetchInfoDao().deleteEntryForNotificationWithId(notificationModel.baseInfo.uniqueId.toString())
                }
            }

            NotificationServiceProvider.getNotificationService().showSummary()

            /* Notification click means, user started engaging with the app organically. Stop attributing to partner now! */
            partnerRef = null

            val uniqueId = intent.getIntExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, -1)
            if (uniqueId != -1) {
                val notMgr =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notMgr.cancel(uniqueId)
                CommonUtils.runInBackground {
                    NotificationRemoveFromTrayHelper.cancelTrayRemovalJobFor(uniqueId)
                    NotificationDB.instance().getNotificationDao().markNotificationAsDeletedFromTray(uniqueId)
                    NotificationDB.instance().getNotificationPrefetchInfoDao().deleteEntryForNotificationWithId(uniqueId.toString())
                }
            }
            context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        } catch (e: Exception) {
            Logger.caughtException(e)
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(NotificationFilterType.CRASH)
            return
        }
    }

    private fun routeForCtaAction(context: Context,targetIntent: Intent,action: String, referrer: PageReferrer, baseModelType: BaseModelType?) {
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        when(action) {
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_FOLLOW_SMALL,
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_FOLLOW -> {
                Logger.d(LOG_TAG, "Handling the follow CTA")
                AnalyticsHelper2.logNotificationCtaClickEvent(NotificationCtaTypes.FOLLOW, referrer)
                targetIntent.putExtra(Constants.BUNDLE_AUTO_FOLLOW_FROM_NOTIFICATION, true)
                context.startActivity(targetIntent)
            }
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_COMMENT_SMALL,
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_COMMENT -> {
                Logger.d(LOG_TAG, "Handling the comment CTA")
                AnalyticsHelper2.logNotificationCtaClickEvent(NotificationCtaTypes.COMMENT, referrer)
                val storyId = targetIntent.getStringExtra(Constants.STORY_ID)
                if (storyId != null) {
                    val intent = Intent(Constants.ALL_COMMENTS_ACTION)
                    intent.putExtra(Constants.BUNDLE_POST_ID, storyId)
                    val parentStoryId = intent.getStringExtra(NewsConstants.PARENT_STORY_ID)
                    if (parentStoryId != null) {
                        intent.putExtra(NewsConstants.PARENT_STORY_ID, parentStoryId)
                    }
                    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(intent)
                } else {
                    context.startActivity(targetIntent)
                }
            }
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_REPLY_SMALL,
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_REPLY -> {
                Logger.d(LOG_TAG, "Handling the reply CTA")
                AnalyticsHelper2.logNotificationCtaClickEvent(NotificationCtaTypes.REPLY, referrer)
                val intent = NotificationRouterHelper.getIntentForReply(targetIntent)
                intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            }
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_SHARE_SMALL,
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_SHARE -> {
                Logger.d(LOG_TAG, "Handling the share CTA")
                AnalyticsHelper2.logNotificationCtaClickEvent(NotificationCtaTypes.SHARE, referrer)
                targetIntent.putExtra(Constants.BUNDLE_AUTO_SHARE_FROM_NOTIFICATION, true)
                context.startActivity(targetIntent)
            }
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_JOIN_SMALL,
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_JOIN -> {
                Logger.d(LOG_TAG, "Handling the join CTA")
                AnalyticsHelper2.logNotificationCtaClickEvent(NotificationCtaTypes.JOIN, referrer)

//              Added the Group Notification Action Event for Join Click.
                if (baseModelType == BaseModelType.GROUP_MODEL) {
                    NhNotificationAnalyticsUtility.logGroupNotificationActionEvent()
                }

                targetIntent.putExtra(Constants.BUNDLE_AUTO_JOIN_FROM_NOTIFICATION, true)
                context.startActivity(targetIntent)
            }
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_REPOST_SMALL,
            NotificationConstants.INTENT_ACTION_NOTIFICATION_CTA_REPOST -> {
                Logger.d(LOG_TAG, "Handling the repost CTA")
                AnalyticsHelper2.logNotificationCtaClickEvent(NotificationCtaTypes.REPOST, referrer)
                val intent = NotificationRouterHelper.getIntentForShare(targetIntent)
                intent.putExtra(Constants.BUNDLE_AUTO_REPOST_FROM_NOTIFICATION, true)
                intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(targetIntent)
            }
            else -> {
                Logger.d(LOG_TAG, "Unknown Intent cant handle")
                context.startActivity(targetIntent)
            }
        }
    }

    private fun handleAdjunctStickyNotification(context: Context,notificationId:Int, deeplinkUrl:String,notificationModel:BaseModel?) {
        val notMgr =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notMgr.cancel(notificationId)
        CommonUtils.runInBackground {
            NotificationActionAnalyticsHelper.logNotificationActionEvent(null, notificationModel, null, null, false)
            NotificationDB.instance().getNotificationDao().markNotificationAsDeletedFromTray(notificationId)
        }
        val intent = CommonNavigator.getDeepLinkLauncherIntent(deeplinkUrl,false,null)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}