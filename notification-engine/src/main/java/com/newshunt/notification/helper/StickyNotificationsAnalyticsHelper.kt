/*
 *  Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.app.analytics.AudioPlayerAnalyticsEventParams
import com.newshunt.app.analytics.NotificationCommonAnalyticsHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.isNoContentError
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.APIException
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.notification.BaseInfo
import com.newshunt.dataentity.notification.NavigationType
import com.newshunt.dataentity.notification.NewsNavModel
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism
import com.newshunt.dataentity.notification.OptReason
import com.newshunt.dataentity.notification.StickyNavModel
import com.newshunt.dataentity.notification.StickyNavModelType
import com.newshunt.dataentity.notification.asset.BaseDataStreamAsset
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import com.newshunt.dataentity.notification.asset.CommentaryState
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset
import com.newshunt.dataentity.notification.asset.CricketNotificationAsset
import com.newshunt.dataentity.notification.asset.GenericDataStreamAsset
import com.newshunt.dataentity.notification.asset.GenericNotificationAsset
import com.newshunt.dataentity.notification.asset.NewsStickyDataStreamAsset
import com.newshunt.dataentity.notification.asset.NewsStickyNotificationAsset
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.notification.analytics.NhAnalyticsNotificationEventParam
import com.newshunt.notification.analytics.NhNotificationParam
import com.newshunt.notification.analytics.NotificationActionAnalyticsHelper
import com.newshunt.notification.model.internal.dao.StickyNotificationEntity
import java.io.Serializable
import java.lang.Exception
import java.net.SocketTimeoutException

/**
 * @author santhosh.kc
 */

private val deliveryMechanism = mapOf(OptReason.USER to "OPT_IN", OptReason.SERVER to "SERVER_PUSH")
private const val PLAY_ACTION = "play"
private const val STOP_ACTION = "stop"
private const val STATE_COLLAPSED = "collapsed"
private const val STATE_EXPANDED = "expanded"
public const val EMPTY_RESPONSE = "empty_response"
public const val API_ERROR = "api_error"
public const val BLOCKED_OR_READ = "all_items_read_or_blocked"

enum class NotificationActionAnalytics(val action: String) : Serializable {
    CROSS_DELETE("cross_delete"),
    REFRESH("refresh"),
    WEB_OPT_OUT("web_opt_out"),
    FORCE_EXPIRE("force_expire"),
    SYSTEM_EXPIRE("system_expire"),
    OVERRIDDEN("overridden"),
    CLICK("click"),
    SUSPENDED("suspended"),
    CLEAR_ALL("deleted_dueTo_clearAll")
}

enum class StickyFloatingWidgetActionAnalytics(val action: String) : Serializable {
    VIEW("view"),
    TOGGLE("toggle"),
    CLICK("click"),
    CROSS("cross")
}

fun logStickyNotificationDeliveredEvent(stickyNavModel: StickyNavModel<BaseNotificationAsset,
        BaseDataStreamAsset>?) {
    stickyNavModel ?: return
    val map = HashMap<NhAnalyticsEventParam, Any?>()


    if(isLoggingDeliveredEventDisabledFor(stickyNavModel)){
        return
    }
    fillCommonAnalyticsParameters(stickyNavModel, map)
    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.NOTIFICATION_DELIVERED,
            NhAnalyticsEventSection.APP, map, null, false)
}

@JvmOverloads
fun  logStickyNotificationActionEvent(stickyNavModel: StickyNavModel<BaseNotificationAsset,
        BaseDataStreamAsset>?, action: NotificationActionAnalytics?, actionTime: Long, section: NhAnalyticsEventSection? = NhAnalyticsEventSection.APP) {
    action ?: return
    stickyNavModel ?: return

    if(isLoggingActionEventsDisabledFor(stickyNavModel, action)){
        return
    }
    val map = HashMap<NhAnalyticsEventParam, Any?>()

    fillCommonAnalyticsParameters(stickyNavModel, map)
    map[NhNotificationParam.NOTIFICATION_ACTION] = action.action

    val timeSpent = actionTime - stickyNavModel.trayDisplayTime

    if (timeSpent > 0) {
        map[AnalyticsParam.TIMESPENT] = timeSpent
    }
    var forceAsPvTrueEvent = false
    action?.let {
        if(action == NotificationActionAnalytics.CLICK || action == NotificationActionAnalytics.REFRESH){
            forceAsPvTrueEvent = true
        }
    }

    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.NOTIFICATION_ACTION,
            section, map, null, forceAsPvTrueEvent)
}

fun logStickyAudioFloatingWidgetCollapseExpandActionEvent(id: String?, type: String?, userClicked
: Boolean,
                                                          audioLanguage: String?, playState:
                                                          CommentaryState?, collapsed: Boolean) {
    fireStickyAudioFloatingWidgetActionEvent(id, type, audioLanguage, if (userClicked)
        StickyFloatingWidgetActionAnalytics.TOGGLE else StickyFloatingWidgetActionAnalytics.VIEW,
            if (collapsed) STATE_EXPANDED else STATE_COLLAPSED, if (collapsed) STATE_COLLAPSED
    else STATE_EXPANDED, playState, playState)
}

fun logStickyAudioFloatingWidgetDismissEvent(id: String?, type: String?, collapsed: Boolean,
                                             audioLanguage: String?, playState:
                                             CommentaryState?) {

    val collapsedString = if (collapsed) STATE_COLLAPSED else STATE_EXPANDED
    fireStickyAudioFloatingWidgetActionEvent(id, type, audioLanguage,
            StickyFloatingWidgetActionAnalytics.CROSS,
            collapsedString, collapsedString, playState, playState)
}

fun logStickyAudioFloatingWidgetViewedEvent(id: String?, type: String?, audioLanguage: String?,
                                            playState: CommentaryState?, collapsed: Boolean) {
    val collapsedString = if (collapsed) STATE_COLLAPSED else STATE_EXPANDED
    fireStickyAudioFloatingWidgetActionEvent(id, type, audioLanguage,
            StickyFloatingWidgetActionAnalytics.VIEW,
            collapsedString, collapsedString, playState, playState)
}

fun logStickyAudioFloatingWidgetPlayStopEvent(id: String?, type: String?, audioLanguage: String?,
                                              collapsed: Boolean, startPlayState:
                                              CommentaryState?, endPlayState: CommentaryState?) {
    val collapsedString = if (collapsed) STATE_COLLAPSED else STATE_EXPANDED
    fireStickyAudioFloatingWidgetActionEvent(id, type, audioLanguage,
            StickyFloatingWidgetActionAnalytics.CLICK,
            collapsedString, collapsedString, startPlayState, endPlayState)
}

private fun fireStickyAudioFloatingWidgetActionEvent(id: String?, type: String?,
                                                     audioLanguage: String?,
                                                     action: StickyFloatingWidgetActionAnalytics,
                                                     oldViewState: String, newViewState:
                                                     String, startPlayState: CommentaryState?,
                                                     endPlayState: CommentaryState?) {
    id ?: return
    type ?: return
    startPlayState ?: return
    endPlayState ?: return

    val map = HashMap<NhAnalyticsEventParam, Any?>()

    map[NhAnalyticsAppEventParam.ITEM_ID] = id
    map[AnalyticsParam.ITEM_LANGUAGE] = audioLanguage ?: UserPreferenceUtil.getUserNavigationLanguage()
    map[AudioPlayerAnalyticsEventParams.START_ACTION] = if (startPlayState == CommentaryState
                    .PLAYING || startPlayState == CommentaryState.BUFFERING)
        PLAY_ACTION else STOP_ACTION
    map[AudioPlayerAnalyticsEventParams.END_ACTION] = if (endPlayState == CommentaryState.PLAYING
            || endPlayState == CommentaryState.BUFFERING)
        PLAY_ACTION else STOP_ACTION

    map[NhAnalyticsAppEventParam.FLOATING_ACTION] = action.action
    map[NhAnalyticsAppEventParam.MODE_NEW] = newViewState
    map[NhAnalyticsAppEventParam.MODE_OLD] = oldViewState

    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.FLOATINGICON_ACTION, NhAnalyticsEventSection
            .APP, map, null, false)
}

private fun fillCommonAnalyticsParameters(stickyNavModel: StickyNavModel<BaseNotificationAsset,
        BaseDataStreamAsset>, map: HashMap<NhAnalyticsEventParam, Any?>) {
    val stickyNavModelType = StickyNavModelType.from(stickyNavModel.stickyType)

    map[NhAnalyticsAppEventParam.NOTIFICATION_TYPE] = stickyNavModelType.analyticsStickyType
    map[NhAnalyticsAppEventParam.NOTIFICATION_SUB_TYPE] =
            (stickyNavModel.getBaseNotificationAsset() as? GenericNotificationAsset)?.template

    map[NhNotificationParam.NOTIFICATION_PRIORITY] = stickyNavModel.priority
    map[NhNotificationParam.NOTIFICATION_CHANNEL_ID] = stickyNavModel.channelId?: Constants.EMPTY_STRING

    stickyNavModel.optReason?.let {
        map[NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM] = deliveryMechanism[it]
    }
    stickyNavModel.getBaseNotificationAsset()?.let {
        map[NhAnalyticsAppEventParam.NOTIFICATION_ID] = it.id
        map[NhAnalyticsAppEventParam.ITEM_ID] = it.id
        map[NhNotificationParam.NOTIFICATION_LOAD_TIME] = it.startTime
        map[NhNotificationParam.NOTIFICATION_DISPLAY_TIME] = stickyNavModel.trayDisplayTime
        map[NhNotificationParam.NOTIFICATION_EXPIRY_TIME] = it.expiryTime
    }
}

fun logStickyExploreButtonClicks(params: Map<NhAnalyticsEventParam, Any>?, referrer: PageReferrer?){
    val map = HashMap<NhAnalyticsEventParam, Any?>()
    params?.let{
        map.putAll(it)
    }

    AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK,
            NhAnalyticsEventSection.NOTIFICATION, map, referrer)
}

fun logStickyActionEvent(params: Map<NhAnalyticsEventParam, Any>?, stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?, deliveryMechanism: NotificationDeliveryMechanism?, baseModel: NewsNavModel?, action: String){
    if(isLoggingActionEventsDisabledFor(stickyNavModel, NotificationActionAnalytics.CLICK)){
        return
    }
    CommonUtils.runInBackground(object : Runnable{
        override fun run() {
            val map = HashMap<NhAnalyticsEventParam, Any?>()
            params?.let{
                map.putAll(it)
            }
            var type: String? = null
            stickyNavModel?.let{
                val stickyNavModelType = StickyNavModelType.from(stickyNavModel.stickyType)
                type = stickyNavModelType.analyticsStickyType
            }

            deliveryMechanism?.let{
                map.put(NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM, it.name)
            }

            if (baseModel == null) {
                return
            }

            val navigationTypeCode = DataUtil.parseInt(baseModel.getsType(), -1)
            if (navigationTypeCode == -1) {
                return
            }

            val navigationType = NavigationType.fromIndex(navigationTypeCode)
            var itemId: String? = null
            when (navigationType) {
                NavigationType.TYPE_OPEN_NEWSITEM -> itemId = baseModel.newsId
                NavigationType.TYPE_OPEN_VIRAL_ITEM -> itemId = baseModel.viralId
                NavigationType.TYPE_OPEN_NEWS_LIST, NavigationType.TYPE_OPEN_NEWS_LIST_CATEGORY -> {
                    itemId = baseModel.npKey
                    if (!DataUtil.isEmpty(baseModel.ctKey)) {
                        map[NhAnalyticsNotificationEventParam.ITEM_SUB_ID] = baseModel.ctKey
                    }
                }
                else -> {itemId = if(!CommonUtils.isEmpty(baseModel.itemId)) baseModel.itemId else baseModel.baseInfo.id}
            }

            map[AnalyticsParam.ITEM_ID] = itemId

            val referrer = PageReferrer(NhGenericReferrer.NOTIFICATION, itemId)
            NotificationActionAnalyticsHelper.logNotificationActionEvent(baseModel, action, map, type, referrer, true)
        }

    })
}

fun logStickyStickyDeliveredEvent(params: Map<NhAnalyticsEventParam, Any>?, stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?, deliveryMechanism: NotificationDeliveryMechanism?, baseModel: NewsNavModel?){
    stickyNavModel ?: return

    if(isLoggingDeliveredEventDisabledFor(stickyNavModel)){
        return
    }
    var map = HashMap<NhAnalyticsEventParam, Any?>()

    stickyNavModel?.let{
        val stickyNavModelType = StickyNavModelType.from(stickyNavModel.stickyType)
        map[NhAnalyticsAppEventParam.NOTIFICATION_TYPE] = stickyNavModelType.analyticsStickyType
    }

    deliveryMechanism?.let{
        map.put(NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM, it.name)
    }

    params?.let{
        map.putAll(it)
    }

    if (baseModel == null || baseModel.baseInfo == null) {
        return
    }

    val navigationTypeCode = DataUtil.parseInt(baseModel.getsType(), -1)
    if (navigationTypeCode == -1) {
        return
    }

    val navigationType = NavigationType.fromIndex(navigationTypeCode)

    if (baseModel.layoutType != null &&
            !CommonUtils.isEmpty(baseModel.layoutType.name)) {
        map[NhAnalyticsNotificationEventParam.NOTIFICATION_LAYOUT] = baseModel.layoutType.name
    }

    var itemId: String? = null
    when (navigationType) {
        NavigationType.TYPE_OPEN_NEWSITEM -> itemId = baseModel.newsId
        NavigationType.TYPE_OPEN_VIRAL_ITEM -> itemId = baseModel.viralId
        NavigationType.TYPE_OPEN_NEWS_LIST, NavigationType.TYPE_OPEN_NEWS_LIST_CATEGORY -> {
            itemId = baseModel.npKey
            if (!DataUtil.isEmpty(baseModel.ctKey)) {
                map[NhAnalyticsNotificationEventParam.ITEM_SUB_ID] = baseModel.ctKey
            }
        }
        else -> {itemId = if(!CommonUtils.isEmpty(baseModel.itemId)) baseModel.itemId else baseModel.baseInfo.id}
    }

    map[AnalyticsParam.ITEM_ID] = itemId

    val baseInfo: BaseInfo = baseModel.baseInfo
    if (deliveryMechanism != null) {
        map[NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM] = deliveryMechanism.name
    }
    if (!CommonUtils.isEmpty(baseInfo.id)) {
        map[NhAnalyticsAppEventParam.NOTIFICATION_ID] = baseInfo.id
    }


    if (baseInfo.v4DisplayTime > 0) {
        map[NhNotificationParam.NOTIFICATION_IS_DEFERRED] = true
    }

    if (baseInfo?.notifType != null) {
        map[NhNotificationParam.NOTIF_TYPE] = baseInfo.notifType
    }

    if (baseInfo?.notifSubType != null) {
        map[NhNotificationParam.NOTIF_SUBTYPE] = baseInfo.notifSubType
    }

    val event = NhAnalyticsAppEvent.NOTIFICATION_DELIVERED

    var experimentalParamsMap: Map<String, String>? = null
    if (baseInfo != null) {
        experimentalParamsMap = baseInfo.experimentParams
    }

    NotificationCommonAnalyticsHelper.addDisplayAndExpiryParamsToMap(baseModel, map)

    val referrer = PageReferrer(NhGenericReferrer.NOTIFICATION, itemId)
    AnalyticsClient.logDynamic(event, NhAnalyticsEventSection.NOTIFICATION, map,
            experimentalParamsMap, referrer, false)
    AnalyticsClient.flushPendingEvents()
}

fun isLoggingDeliveredEventDisabledFor(stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?): Boolean{
    var isLoggingDisabled = false
    if(stickyNavModel == null){
        return true
    }
    if(stickyNavModel.getBaseNotificationAsset() != null) {
        stickyNavModel.getBaseNotificationAsset()?.type?.let{
            when(it){
                StickyNavModelType.NEWS.stickyType -> {
                    ((stickyNavModel.getBaseNotificationAsset()) as? NewsStickyNotificationAsset)?.let { newsStickyNotificationAsset ->
                        isLoggingDisabled = newsStickyNotificationAsset.disableEvents
                    }

                    (stickyNavModel.getBaseStreamAsset() as? NewsStickyDataStreamAsset)?.let{newsStickyDataStreamAsset ->
                        isLoggingDisabled = (isLoggingDisabled || newsStickyDataStreamAsset.disableEvents)
                    }
                }
                StickyNavModelType.CRICKET.stickyType -> { ((stickyNavModel.getBaseNotificationAsset() as? CricketNotificationAsset))?.let { cricketNotificationAsset ->
                    isLoggingDisabled = cricketNotificationAsset.isLoggingNotificationEventsDisbaled
                }

                    (stickyNavModel.getBaseStreamAsset() as? CricketDataStreamAsset)?.let{cricketDataStreamAsset ->
                        isLoggingDisabled = (isLoggingDisabled || cricketDataStreamAsset.isLoggingNotificationEventsDisabled)
                    }
                }
                StickyNavModelType.GENERIC.stickyType -> {
                    ((stickyNavModel.getBaseNotificationAsset() as? GenericNotificationAsset))?.let { genericNotificationAsset ->
                        isLoggingDisabled = genericNotificationAsset.isLoggingNotificationEventsDisabled()
                    }

                    (stickyNavModel.getBaseStreamAsset() as? GenericDataStreamAsset)?.let{genericDataStreamAsset ->
                        isLoggingDisabled = (isLoggingDisabled || genericDataStreamAsset.isLoggingNotificationEventsDisabled())
                    }
                }
                else ->{}
            }
        }
    }

    return isLoggingDisabled
}

fun isLoggingActionEventsDisabledFor(stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>?, action: NotificationActionAnalytics?): Boolean{
    var isLoggingDisabled = false
    if(stickyNavModel == null){
        return true
    }
    if(stickyNavModel.getBaseNotificationAsset() != null) {
        stickyNavModel.getBaseNotificationAsset()?.type?.let{
            when(it){
                StickyNavModelType.NEWS.stickyType -> {
                    ((stickyNavModel.getBaseNotificationAsset()) as? NewsStickyNotificationAsset)?.let { newsStickyNotificationAsset ->
                        isLoggingDisabled = newsStickyNotificationAsset.disableEvents
                    }

                    (stickyNavModel.getBaseStreamAsset() as? NewsStickyDataStreamAsset)?.let{newsStickyDataStreamAsset ->
                        isLoggingDisabled = (isLoggingDisabled || newsStickyDataStreamAsset.disableEvents)
                    }
                }
                StickyNavModelType.CRICKET.stickyType -> { ((stickyNavModel.getBaseNotificationAsset() as? CricketNotificationAsset))?.let { cricketNotificationAsset ->
                    isLoggingDisabled = cricketNotificationAsset.isLoggingNotificationEventsDisbaled
                }

                    (stickyNavModel.getBaseStreamAsset() as? CricketDataStreamAsset)?.let{cricketDataStreamAsset ->
                        isLoggingDisabled = (isLoggingDisabled || cricketDataStreamAsset.isLoggingNotificationEventsDisabled)
                    }
                }
                StickyNavModelType.GENERIC.stickyType -> {
                    ((stickyNavModel.getBaseNotificationAsset() as? GenericNotificationAsset))?.let { genericNotificationAsset ->
                        isLoggingDisabled = genericNotificationAsset.isLoggingNotificationEventsDisabled()
                    }

                    (stickyNavModel.getBaseStreamAsset() as? GenericDataStreamAsset)?.let{genericDataStreamAsset ->
                        isLoggingDisabled = (isLoggingDisabled || genericDataStreamAsset.isLoggingNotificationEventsDisabled())
                    }
                }
                else ->{}
            }
        }
    }

    when(action){
        NotificationActionAnalytics.SYSTEM_EXPIRE ->{}
        else -> {isLoggingDisabled = false}
    }

    return isLoggingDisabled
}


fun logStickyUndisplayedEvent(stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>? = null, stickyNotificationEntity: StickyNotificationEntity? = null, error: Throwable? = null, reason: String? = null){


    val map = HashMap<NhAnalyticsEventParam, Any?>()

    if(stickyNavModel != null){

        if(isLoggingDeliveredEventDisabledFor(stickyNavModel)){
            return
        }
        stickyNavModel?.optReason?.let {
            map[NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM] = deliveryMechanism[it]
        }

        stickyNavModel?.stickyType?.let{
            val stickyNavModelType = StickyNavModelType.from(it)
            map[NhAnalyticsAppEventParam.NOTIFICATION_TYPE] = stickyNavModelType.analyticsStickyType
        }
    }else{
        stickyNotificationEntity ?: return
        var disabledState = false
        try{
            disabledState = PreferenceManager.getPreference(AppStatePreference.LAST_KNOWN_DISABLE_LOGGING_STATUS_NEWS_STICKY, false)
        }catch(ex: Exception){
            Logger.caughtException(ex)
        }
        if(disabledState){
            return
        }

        stickyNotificationEntity?.type?.let {
            val stickyNavModelType = StickyNavModelType.from(it)
            map[NhAnalyticsAppEventParam.NOTIFICATION_TYPE] = stickyNavModelType.analyticsStickyType
        }

        stickyNotificationEntity?.optReason?.let {
            map[NhNotificationParam.NOTIFICATION_DELIVERY_MECHANISM] = deliveryMechanism[it]
        }
    }

    if(!CommonUtils.isEmpty(reason)){
        map[NhNotificationParam.NOTIF_UNDELIVERED_REASON] = reason
    }else if(error != null){
        var message = Constants.EMPTY_STRING
        if(error is BaseError){
           val baseError = error as BaseError
            if(baseError.isNoContentError()){
                message = EMPTY_RESPONSE
            }else{
                message = API_ERROR
            }
        }else if(error is APIException){
            val apiException = error as APIException
            if(apiException.error?.status?:null != null && apiException.error.status.equals(Constants.ERROR_HTTP_NO_CONTENT)){
                message = EMPTY_RESPONSE
            }else{
                message = API_ERROR
            }
        }else if(error is ListNoContentException){
            if(error.error?.status?:null != null && error.error?.status.equals(Constants.ERROR_HTTP_NO_CONTENT)){
                message = EMPTY_RESPONSE
            }else{
                message = API_ERROR
            }
        }
        else if(error is Exception){
            message = API_ERROR
        }
        map[NhNotificationParam.NOTIF_UNDELIVERED_REASON] = message
    }

    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.STICKY_NOTIFICATION_NOT_DISPLAYED, NhAnalyticsEventSection.NOTIFICATION, map,
            null, null, false)

}
