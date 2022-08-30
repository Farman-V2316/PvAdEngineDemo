/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.appsflyer

import android.util.Pair
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.gson.reflect.TypeToken
import com.newshunt.analytics.FirebaseAnalyticsHelper
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.*
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.preference.SavedPreference
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.*
import com.newshunt.dataentity.dhutil.model.entity.upgrade.RegisteredClientInfo
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper
import com.newshunt.dhutil.helper.launch.CampaignAcquisitionHelper
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.AppsFlyerEventPreference
import com.newshunt.dhutil.model.internal.service.AppsFlyerEventConfigServiceImpl
import com.newshunt.dhutil.model.service.AppsFlyerEventConfigService
import com.squareup.otto.Subscribe
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * Helper class to make calls to AppsFlyer SDK
 * <p>
 * Created by srikanth.ramaswamy on 12/20/19.
 */
private const val APPSFLYER_DEV_KEY = "G6pABFi7QjYZdszqDMDCEd" //API Key
private const val AF_CAMPAIGN = "campaign"
const val APPSFLYER_TAG = "AppsFlyerHelper"
private const val APPSFLYER_ADGROUP_ID = "adgroup_id"
private const val APPSFLYER_ADSET = "af_adset"
private const val APPSFLYER_ADSET_ID = "af_adset_id"
private const val APPSFLYER_AF_SITEID = "af_siteid"
private const val APPSFLYER_CAMPAIGN = "campaign"
private const val APPSFLYER_CAMPAIGN_ID = "campaign_id"
private const val APPSFLYER_MEDIA_SOURCE = "media_source"
private const val APPSFLYER_AGENCY = "agency"
private const val APPSFLYER_EVENT_NAME = "af_event_name"
private const val AF_INSTALL_TIMESTAMP = "install_timestamp"
private const val AF_CURRENT_TIMESTAMP = "current_timestamp"


object AppsFlyerHelper {
    private var initDone = false
    private var gcmToken: String? = Constants.EMPTY_STRING
    private var eventsConfigResponse: AppsFlyerEventsConfigResponse? = null
        @Synchronized get
        @Synchronized set(value) {
            if (value?.eventConfigMap == null) {
                versionedAPIService.resetVersion()
                return
            }
            Logger.d(APPSFLYER_TAG, "setEventsConfigResponse, version: " + value.version)
            field = value
            //Response now available, process all the queued events.
            processQueuedEvents()
        }
    private var fcmSenderId: String? = null
    private val versionedAPIService: AppsFlyerEventConfigService by lazy {
        AppsFlyerEventConfigServiceImpl(0)
    }
    private val eventQueue: ConcurrentLinkedQueue<Pair<AppsFlyerEvents, Map<String, Any>?>> by lazy {
        ConcurrentLinkedQueue<Pair<AppsFlyerEvents, Map<String, Any>?>>()
    }
    private val DUMMY_EVT_CONFIG = EventConfig(0, 0, 0, 0, triggerFirebaseEvent = false)
    private val eventPreferenceMap : Map<AppsFlyerEvents, SavedPreference>  = mapOf(AppsFlyerEvents.EVENT_TIMESPENT_TOP_20 to AppsFlyerEventPreference.EVENT_TIMESPENT_TOP_20_CONSUMED,
        AppsFlyerEvents.EVENT_TIMESPENT_TOP_40 to AppsFlyerEventPreference.EVENT_TIMESPENT_TOP_40_CONSUMED,
        AppsFlyerEvents.EVENT_TIMESPENT_TOP_60 to AppsFlyerEventPreference.EVENT_TIMESPENT_TOP_60_CONSUMED,
        AppsFlyerEvents.EVENT_TIMESPENT_TOP_20_SMALLER_THRESHOLD to AppsFlyerEventPreference.EVENT_TIMESPENT_TOP_20_SMALLER_THRESHOLD_CONSUMED,
        AppsFlyerEvents.EVENT_TIMESPENT_TOP_40_SMALLER_THRESHOLD to AppsFlyerEventPreference.EVENT_TIMESPENT_TOP_40_SMALLER_THRESHOLD_CONSUMED,
        AppsFlyerEvents.EVENT_TIMESPENT_TOP_60_SMALLER_THRESHOLD to AppsFlyerEventPreference.EVENT_TIMESPENT_TOP_60_SMALLER_THRESHOLD_CONSUMED,
        AppsFlyerEvents.EVENT_TIMESPENT_FIRST_SESSION  to AppsFlyerEventPreference.EVENT_TIMESPENT_FIRST_SESSION_CONSUMED,
        AppsFlyerEvents.EVENT_TOTAL_APP_LAUNCHES to AppsFlyerEventPreference.APP_LAUNCH_COUNT_CONSUMED,
        AppsFlyerEvents.EVENT_APP_OPEN_BETWEEN_24_48_HOURS to AppsFlyerEventPreference.FIRST_LAUNCH_IN_24_TO_48_HOURS_CONSUMED,
        AppsFlyerEvents.EVENT_APP_OPEN_BETWEEN_72_96_HOURS to AppsFlyerEventPreference.FIRST_LAUNCH_IN_72_TO_96_HOURS_CONSUMED,
        AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY1 to AppsFlyerEventPreference.EVENT_APP_OPENED_DAY1_CONSUMED,
        AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY3 to AppsFlyerEventPreference.EVENT_APP_OPENED_DAY3_CONSUMED,
        AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY7 to AppsFlyerEventPreference.EVENT_APP_OPENED_DAY7_CONSUMED,
        AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY15 to AppsFlyerEventPreference.EVENT_APP_OPENED_DAY15_CONSUMED,
        AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY30 to AppsFlyerEventPreference.EVENT_APP_OPENED_DAY30_CONSUMED,
        AppsFlyerEvents.EVENT_USER_LOGIN_GOOGLE to AppsFlyerEventPreference.USER_LOGIN_CONSUMED,
        AppsFlyerEvents.EVENT_USER_LOGIN_FACEBOOK to AppsFlyerEventPreference.USER_LOGIN_CONSUMED,
        AppsFlyerEvents.EVENT_USER_LOGIN_TRUECALLER to AppsFlyerEventPreference.USER_LOGIN_CONSUMED,
        AppsFlyerEvents.EVENT_USER_NEW_INSTALL to AppsFlyerEventPreference.EVENT_NEW_USER_INSTALL_CONSUMED,
        AppsFlyerEvents.EVENT_USER_RE_INSTALL to AppsFlyerEventPreference.EVENT_USER_RE_INSTALL_CONSUMED,
        AppsFlyerEvents.EVENT_NOTIFICATION_DELIVERY to AppsFlyerEventPreference.EVENT_NOTIFICATION_DELIVERY_CONSUMED,
        AppsFlyerEvents.EVENT_NOTIFICATION_CLICK to AppsFlyerEventPreference.EVENT_NOTIFICATION_CLICK_CONSUMED,
        AppsFlyerEvents.EVENT_APP_OPEN_AFTER_3_DAYS to AppsFlyerEventPreference.EVENT_APP_OPENED_AFTER_DAY_3_CONSUMED,
        AppsFlyerEvents.EVENT_FIRST_CONTENT_VIEWED to AppsFlyerEventPreference.FIRST_CONTENT_VIEW_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_ENGLISH to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_HINDI to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_MARATHI to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_BENGALI to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_GUJARATI to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_TAMIL to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_TELUGU to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_MALAYALAM to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_KANNADA to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED,
        AppsFlyerEvents.EVENT_LANG_SELECTED_PUNJABI to AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED)

    private val eventHashMap : MutableList<String> = mutableListOf(AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY1.eventName,
        AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY3.eventName,
        AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY7.eventName,
        AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY15.eventName,
        AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY30.eventName,
    )

    init {
        AndroidUtils.getMainThreadHandler().post {
            BusProvider.getUIBusInstance().register(this)
            versionedAPIService.getEventConfigLocal()
        }
    }

    /**
     * Initialize the parameters in AppsFlyer SDK.
     *
     * @param isOnboardingDone If onboarding done or not
     */
    fun initAppsFlyerSDK(isOnboardingDone: Boolean) {
        if (!isAppsFlyerEnabled(isOnboardingDone)) {
            return
        }
        val clientID = ClientInfoHelper.getServerConfirmedClientId()
        if (clientID.isNullOrBlank() || initDone) {
            Logger.e(APPSFLYER_TAG, "Skip init appsflyer: initDone = $initDone, clientId = $clientID")
            return
        }
        val appsFlyerLib = AppsFlyerLib.getInstance()
        appsFlyerLib.setDebugLog(AppConfig.getInstance()?.isLoggerEnabled ?: false)
        appsFlyerLib.setCustomerUserId(clientID)
        Logger.d(APPSFLYER_TAG, "Client ID $clientID set to AppsFlyer SDK")
        initDone = true

        if (gcmToken.isNullOrBlank().not()) {
            refreshGCMToken(gcmToken)
        }

        val listener = if (CommonNavigator.isFirstLaunch()) {
            if (!PreferenceManager.getPreference(AppsFlyerEventPreference.APPSFLYER_INIT_FIRED, false) ) {
                AnalyticsHelper.logAppsFlyerInitEvent()
                PreferenceManager.savePreference(AppsFlyerEventPreference.APPSFLYER_INIT_FIRED, true)
            }
            getAppsFlyerConversionListener()
        } else {
            null
        }
        appsFlyerLib.init(APPSFLYER_DEV_KEY, listener, CommonUtils.getApplication())
        CommonUtils.runInBackground {
            try {
                AppUserPreferenceUtils.setAppsFlyerUID(PasswordEncryption.encrypt(appsFlyerLib.getAppsFlyerUID(CommonUtils.getApplication())))
            } catch (e: Exception) {
                Logger.caughtException(e)
            }
        }
        appsFlyerLib.setCollectIMEI(false)
        appsFlyerLib.setCollectAndroidID(false)
        appsFlyerLib.start(CommonUtils.getApplication(), APPSFLYER_DEV_KEY)
        Logger.d(APPSFLYER_TAG, "initAppsFlyerSDK Done: senderID $fcmSenderId")
    }

    /**
     * Pass on the latest GCM token to AppsFlyer SDK, to enable uninstall tracking. This method
     * must be called from inside onTokenRefresh
     *
     * @param refreshedToken Refreshed token
     */
    fun refreshGCMToken(refreshedToken: String?) {
        gcmToken = refreshedToken
        if (!initDone || refreshedToken.isNullOrBlank()) {
            return
        }

        AppsFlyerLib.getInstance().updateServerUninstallToken(CommonUtils.getApplication(), refreshedToken)
        gcmToken = Constants.EMPTY_STRING
    }

    /**
     * Track an AppsFlyer event in the background
     * @param event  event
     * @param params optional params.
     */
    fun trackEvent(event: AppsFlyerEvents, params: Map<String, Any>?) {
        if (initDone) {
            CommonUtils.runInBackground { trackEvent(event, params, true) }
        }
    }

    /**
     * Process and fire all expired events, in the background
     */
    fun processExpiredEvents() {
        if (!initDone) {
            return
        }
        CommonUtils.runInBackground {
            Logger.d(APPSFLYER_TAG, "Processing expired events")
            for (event in AppsFlyerEvents.values()) {
                when (event) {
                    AppsFlyerEvents.EVENT_SPLASH_OPEN,
                    AppsFlyerEvents.EVENT_LANG_SELECTED,
                    AppsFlyerEvents.EVENT_FIRST_VIDEO_AD_IMPRESSION,
                    AppsFlyerEvents.EVENT_FIRST_AD_IMPRESSION,
                    AppsFlyerEvents.EVENT_FIRST_DETAIL_VIEW,
                    AppsFlyerEvents.EVENT_USER_LOGIN_GOOGLE,
                    AppsFlyerEvents.EVENT_USER_LOGIN_FACEBOOK,
                    AppsFlyerEvents.EVENT_USER_LOGIN_TRUECALLER,
                    AppsFlyerEvents.EVENT_USER_ENGAGEMENT_LIKE,
                    AppsFlyerEvents.EVENT_USER_ENGAGEMENT_SHARE,
                    AppsFlyerEvents.EVENT_USER_ENGAGEMENT_COMMENT,
                    AppsFlyerEvents.EVENT_USER_ENGAGEMENT_REPOST,
                    AppsFlyerEvents.EVENT_USER_NEW_INSTALL,
                    AppsFlyerEvents.EVENT_USER_RE_INSTALL,
                    AppsFlyerEvents.EVENT_NOTIFICATION_DELIVERY,
                    AppsFlyerEvents.EVENT_NOTIFICATION_CLICK,
                    AppsFlyerEvents.EVENT_FIRST_CONTENT_VIEWED,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_ENGLISH,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_BENGALI,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_GUJARATI,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_HINDI,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_KANNADA,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_MARATHI,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_MALAYALAM,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_PUNJABI,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_TAMIL,
                    AppsFlyerEvents.EVENT_LANG_SELECTED_TELUGU-> {}
                    else -> trackEvent(event, null, false)
                }
            }
        }
    }

    @Subscribe
    fun onAppsFlyerEventConfigReceived(response: AppsFlyerEventsConfigResponse?) {
        if (initDone) {
            eventsConfigResponse = response
        }
    }

    /**
     * Set the FCM sender Id.
     * @param fcmSenderId FCM sender Id of the app package
     */
    fun setFCMSenderId(fcmSenderId: String) {
        if (!CommonUtils.isEmpty(fcmSenderId)) {
            this.fcmSenderId = fcmSenderId
        }
    }

    private fun parseConversionData(conversionData: Map<String, Any?>) {
        if (CommonUtils.isEmpty(conversionData)) {
            return
        }
        var campaignInfo: MutableMap<String, String> = mutableMapOf()
        conversionData[APPSFLYER_ADGROUP_ID]?.let {
            campaignInfo[APPSFLYER_ADGROUP_ID] = it.toString()
        }
        conversionData[APPSFLYER_ADSET]?.let {
            campaignInfo[APPSFLYER_ADSET] = it.toString()
        }
        conversionData[APPSFLYER_ADSET_ID]?.let {
            campaignInfo[APPSFLYER_ADSET_ID] = it.toString()
        }
        conversionData[APPSFLYER_AF_SITEID]?.let {
            campaignInfo[APPSFLYER_AF_SITEID] = it.toString()
        }
        conversionData[APPSFLYER_CAMPAIGN]?.let {
            campaignInfo[APPSFLYER_CAMPAIGN] = it.toString()
        }
        conversionData[APPSFLYER_CAMPAIGN_ID]?.let {
            campaignInfo[APPSFLYER_CAMPAIGN_ID] = it.toString()
        }
        conversionData[APPSFLYER_AGENCY]?.let {
            campaignInfo[APPSFLYER_AGENCY] = it.toString()
        }
        conversionData[APPSFLYER_MEDIA_SOURCE]?.let {
            campaignInfo[APPSFLYER_MEDIA_SOURCE] = it.toString()
        }
        //make a new map and pass entire map to triggerInstallDetailsAPICall
        Logger.d(APPSFLYER_TAG, "Received conversion data")
        (conversionData[AF_CAMPAIGN] as? String?)?.let { campaign ->
            /**
             * If Google referrer is not set and pinged to B.E, we need to extract the campaign name and
             * ping B.E as the referrer
             */
            if (campaign.isNotEmpty()) {
                Logger.d(APPSFLYER_TAG, "Post AppsFlyer campaign as the referrer $campaign")
                PreferenceManager.savePreference(AppStatePreference.INSTALL_APPSFLYER_REFERRER, campaign)
                BusProvider.postOnUIBus(AppsFlyerReferrerEvent(campaign, campaignInfo))
                CampaignAcquisitionHelper.parseCampaignParameter(campaign)
            }
        }
    }

    private fun getAppsFlyerConversionListener(): AppsFlyerConversionListener {
        return object : AppsFlyerConversionListener {
            override fun onAppOpenAttribution(map: MutableMap<String, String>?) {
                AppsFlyerLib.getInstance().unregisterConversionListener()
            }

            override fun onConversionDataSuccess(map: MutableMap<String, Any?>?) {
                Logger.d(APPSFLYER_TAG, "onConversionDataSuccess, map.size: ${map?.size ?: 0}")
                if (!map.isNullOrEmpty()) {
                    parseConversionData(map)
                }
                val newMap = map?.mapValues {
                    it.value?.toString()
                }
                AnalyticsHelper.logAppsFlyerInstallEvent(newMap)
                AppsFlyerLib.getInstance().unregisterConversionListener()
            }

            override fun onConversionDataFail(error: String?) {
                logAppsFlyerFailureEvent("onConversionDataFail: $error")
                AppsFlyerLib.getInstance().unregisterConversionListener()
            }

            override fun onAttributionFailure(error: String?) {
                logAppsFlyerFailureEvent("onAttributionFailure: $error")
                AppsFlyerLib.getInstance().unregisterConversionListener()
            }
        }
    }

    private fun isAppsFlyerEnabled(isOnboardingDone: Boolean): Boolean {
        // Appsflyer is enabled only when: (It is enabled via build config AND enabled via register API).
        return AppConfig.getInstance()?.isAppsFlyerEnabled == true && isAppsFlyerEnabledFromBE(isOnboardingDone)
    }

    private fun isAppsFlyerEnabledFromBE(isOnboardingDone: Boolean): Boolean {
        val existence = AppsFlyerExistence.fromName(
                PreferenceManager.getPreference(GenericAppStatePreference.APPSFLYER_ENABLED_FROM_BE,
                        AppsFlyerExistence.DISABLED.toString()))

        return when (existence) {
            AppsFlyerExistence.ENABLED -> true
            AppsFlyerExistence.ENABLED_POST_ONBOARDING -> isOnboardingDone
            else -> return false
        }
    }

    private fun logAppsFlyerFailureEvent(str: String) {
        try {
            Logger.e(APPSFLYER_TAG, str)
            AnalyticsHelper.logAppsFlyerInitFailure(mapOf(NhAnalyticsAppEventParam.ERROR_TEXT.getName() to str))
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
    }

    /**
     * Delegates the work to handleEvent by filling the parameters for each event
     * @param event         Event
     * @param params        params
     * @param eventHappened Did the event happen or are processing old events?
     */
    private fun trackEvent(event: AppsFlyerEvents,
                           params: Map<String, Any>?,
                           eventHappened: Boolean) { //If the event is to be tracked before we have the response.
        if (eventsConfigResponse?.eventConfigMap == null) {
            if (eventHappened) {
                //Accumulate the event in a queue and handle it once we have the response
                eventQueue.offer(Pair(event, params))
                Logger.d(APPSFLYER_TAG,
                        "Queued event " + event.eventName + " coz event config response is null")
            }
            return
        }
        val eventName: String
        val installTimeStamp = PreferenceManager.getPreference(AppStatePreference.INSTALL_TIMESTAMP, 0L)
        val curTimestamp = System.currentTimeMillis()
        eventsConfigResponse?.eventConfigMap?.let { configMap ->
            when (event) {
                AppsFlyerEvents.EVENT_SPLASH_OPEN,
                AppsFlyerEvents.EVENT_DAYS_OPENED_WITHIN_THRESHOLD -> {
                    if (event == AppsFlyerEvents.EVENT_SPLASH_OPEN) {
                        fireEvent(event.eventName, null)
                    }
                    eventName = AppsFlyerEvents.EVENT_DAYS_OPENED_WITHIN_THRESHOLD.eventName
                    //Counter for this event to be incremented once in 24 hours. Hence timeWindow is 24
                    handleEvent(AppsFlyerEvents.EVENT_DAYS_OPENED_WITHIN_THRESHOLD, configMap[eventName],
                            AppsFlyerEventPreference.DAYS_OPENED_WITHIN_THRESHOLD, eventHappened,
                            TimeUnit.HOURS.toMillis(24.toLong()))
                }
                AppsFlyerEvents.EVENT_CONTENT_CONSUMED -> {
                    eventName = event.eventName
                    handleEvent(event, configMap[eventName], AppsFlyerEventPreference
                            .CONTENT_CONSUMED,
                            eventHappened, 0)
                }
                AppsFlyerEvents.EVENT_LANG_SELECTED -> {
                    //Don't honour events for upgrade users.
                    if (installTimeStamp == 0L) {
                        return
                    }
                    fireEvent(event.eventName, null)
                }
                AppsFlyerEvents.EVENT_FIRST_AD_IMPRESSION ->{
                    //Intentionally creating a dummy config here. This event does not have any config
                    handleEvent(event, EventConfig(-1, 1, -1, TimeUnit.HOURS.toMillis(24.toLong()), triggerFirebaseEvent = false),
                            AppsFlyerEventPreference.AD_IMPRESSION,
                            eventHappened,
                            TimeUnit.HOURS.toMillis(24.toLong()))
                }
                AppsFlyerEvents.EVENT_FIRST_VIDEO_AD_IMPRESSION -> {
                    //Intentionally creating a dummy config here. This event does not have any config
                    handleEvent(event, EventConfig(-1, 1, -1, TimeUnit.HOURS.toMillis(24.toLong()), triggerFirebaseEvent = false),
                            AppsFlyerEventPreference.VIDEO_AD_IMPRESSION,
                            eventHappened,
                            TimeUnit.HOURS.toMillis(24.toLong()))
                }
                AppsFlyerEvents.EVENT_FIRST_DETAIL_VIEW -> {
                    handleEvent(event, DUMMY_EVT_CONFIG, AppsFlyerEventPreference.SPV_CONSUMED, eventHappened, 0)
                }
                AppsFlyerEvents.EVENT_DAY_1_OPENED -> {
                    if ((curTimestamp - installTimeStamp) < TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS)
                            && CommonUtils.isNextCalDay(installTimeStamp, curTimestamp)) {
                        handleEvent(event, DUMMY_EVT_CONFIG, AppsFlyerEventPreference.DAY_1_OPENED_CONSUMED, true, 0)
                    }
                }
                AppsFlyerEvents.EVENT_LANG_SELECTED_ENGLISH,
                AppsFlyerEvents.EVENT_LANG_SELECTED_BENGALI,
                AppsFlyerEvents.EVENT_LANG_SELECTED_GUJARATI,
                AppsFlyerEvents.EVENT_LANG_SELECTED_HINDI,
                AppsFlyerEvents.EVENT_LANG_SELECTED_KANNADA,
                AppsFlyerEvents.EVENT_LANG_SELECTED_MARATHI,
                AppsFlyerEvents.EVENT_LANG_SELECTED_MALAYALAM,
                AppsFlyerEvents.EVENT_LANG_SELECTED_PUNJABI,
                AppsFlyerEvents.EVENT_LANG_SELECTED_TAMIL,
                AppsFlyerEvents.EVENT_LANG_SELECTED_TELUGU -> {
                    //Don't honour events for upgrade users.
                    if (installTimeStamp == 0L) {
                        return
                    }
                    handleEvent(event, configMap[event.eventName], AppsFlyerEventPreference.LANGUAGE_SELECTION_CONSUMED, eventHappened, 0)
                }
                AppsFlyerEvents.EVENT_USER_ENGAGEMENT_SHARE,
                AppsFlyerEvents.EVENT_USER_ENGAGEMENT_COMMENT,
                AppsFlyerEvents.EVENT_USER_ENGAGEMENT_LIKE,
                AppsFlyerEvents.EVENT_USER_ENGAGEMENT_REPOST -> {
                    configMap[event.eventName]?.let {
                        if(!CommonUtils.isTimeExpired(installTimeStamp, it.ttlMs)) {
                            fireEvent(event.eventName, null, it)
                        }
                    } ?: return
                }
                AppsFlyerEvents.EVENT_USER_LOGIN_GOOGLE,
                AppsFlyerEvents.EVENT_USER_LOGIN_FACEBOOK,
                AppsFlyerEvents.EVENT_USER_LOGIN_TRUECALLER -> {
                    handleEvent(event, configMap[event.eventName], AppsFlyerEventPreference.USER_LOGIN_CONSUMED, eventHappened, 0)
                }
                AppsFlyerEvents.EVENT_FIRST_CONTENT_VIEWED,
                AppsFlyerEvents.EVENT_APP_OPEN_BETWEEN_24_48_HOURS,
                AppsFlyerEvents.EVENT_APP_OPEN_BETWEEN_72_96_HOURS,
                AppsFlyerEvents.EVENT_USER_NEW_INSTALL,
                AppsFlyerEvents.EVENT_USER_RE_INSTALL -> {
                    eventPreferenceMap[event]?.let {
                        handleEvent(event, configMap[event.eventName], it, eventHappened, 0)
                    } ?: return
                }
                AppsFlyerEvents.EVENT_TOTAL_APP_LAUNCHES,
                AppsFlyerEvents.EVENT_TIMESPENT_TOP_20,
                AppsFlyerEvents.EVENT_TIMESPENT_TOP_40,
                AppsFlyerEvents.EVENT_TIMESPENT_TOP_60,
                AppsFlyerEvents.EVENT_TIMESPENT_TOP_20_SMALLER_THRESHOLD,
                AppsFlyerEvents.EVENT_TIMESPENT_TOP_40_SMALLER_THRESHOLD,
                AppsFlyerEvents.EVENT_TIMESPENT_TOP_60_SMALLER_THRESHOLD,
                AppsFlyerEvents.EVENT_TIMESPENT_FIRST_SESSION,
                AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY1,
                AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY3,
                AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY7,
                AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY15,
                AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY30,
                AppsFlyerEvents.EVENT_NOTIFICATION_DELIVERY,
                AppsFlyerEvents.EVENT_NOTIFICATION_CLICK,
                AppsFlyerEvents.EVENT_APP_OPEN_AFTER_3_DAYS -> {
                    eventPreferenceMap[event]?.let {
                        handleEvent(event, configMap[event.eventName], it, eventHappened, 0)
                    } ?: return
                }
            }
        }
    }

    /**
     * Handle each event, increment the counter and fire the event when it reaches the high
     * threshold or when the ttl expires
     * @param event         Event name
     * @param config        Config for the event from Versioned API
     * @param preference    Preference to store the current state of the counter
     * @param eventHappened Did the event happen now?
     * @param timeWindow    time window in ms within which the event to be counted only once
     */
    private fun handleEvent(event: AppsFlyerEvents, config: EventConfig?,
                            preference: SavedPreference, eventHappened: Boolean,
                            timeWindow: Long) {
        val installTimeStamp = PreferenceManager.getPreference(AppStatePreference.INSTALL_TIMESTAMP, 0L)
        //Dont honour events for upgrade users or if the config is not available.
        if (installTimeStamp == 0L || config == null) {
            return
        }
        val curTimestamp = System.currentTimeMillis()
        //First read the current state of this event from preference.
        var eventState = JsonUtils.fromJson(PreferenceManager.getPreference(preference, Constants.EMPTY_STRING),
                EventState::class.java)
        //Event handled for the first time.
        if (eventState == null) {
            eventState = EventState(0, 0, false)
        }
        //If we have already fired the event, no need to process any further
        if (eventState.consumed) {
            return
        }
        when (event) {
            AppsFlyerEvents.EVENT_FIRST_AD_IMPRESSION,
            AppsFlyerEvents.EVENT_FIRST_VIDEO_AD_IMPRESSION,
            AppsFlyerEvents.EVENT_DAY_1_OPENED,
            AppsFlyerEvents.EVENT_FIRST_DETAIL_VIEW,
            AppsFlyerEvents.EVENT_FIRST_CONTENT_VIEWED,
            AppsFlyerEvents.EVENT_USER_NEW_INSTALL,
            AppsFlyerEvents.EVENT_USER_RE_INSTALL,
            AppsFlyerEvents.EVENT_TIMESPENT_FIRST_SESSION-> {
                if (eventHappened) {
                    fireEvent(event.eventName, null, config)
                    updateEventState(event.eventName, eventState, preference, true)
                }
                return
            }
            AppsFlyerEvents.EVENT_TIMESPENT_TOP_20,
            AppsFlyerEvents.EVENT_TIMESPENT_TOP_20_SMALLER_THRESHOLD,
            AppsFlyerEvents.EVENT_TIMESPENT_TOP_40,
            AppsFlyerEvents.EVENT_TIMESPENT_TOP_40_SMALLER_THRESHOLD,
            AppsFlyerEvents.EVENT_TIMESPENT_TOP_60,
            AppsFlyerEvents.EVENT_TIMESPENT_TOP_60_SMALLER_THRESHOLD -> {
                if(!CommonUtils.isTimeExpired(installTimeStamp, config.ttlMs) &&
                    PreferenceManager.getPreference(AppStatePreference.TOTAL_FOREGROUND_DURATION, 0L) * 1000 > config.thresholdMs) { //FG time is stored as seconds and not milli seconds
                    fireEvent(event.eventName, null, config)
                    updateEventState(event.eventName, eventState, preference, true)
                }
                return
            }
            AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY1 -> {
                if(CommonUtils.isNextCalDay(installTimeStamp, System.currentTimeMillis())) {
                    fireEvent(event.eventName, null, config, System.currentTimeMillis().toString())
                    updateEventState(event.eventName, eventState, preference, true)
                }
                return
            }
            AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY3,
            AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY7,
            AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY15,
            AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY30 -> {
                if(CommonUtils.isNextXCalDay(installTimeStamp, System.currentTimeMillis(), config.thresholdDays)) {
                    fireEvent(event.eventName, null, config, System.currentTimeMillis().toString())
                    updateEventState(event.eventName, eventState, preference, true)
                }
                return
            }
            AppsFlyerEvents.EVENT_APP_OPEN_AFTER_3_DAYS -> {
                if(CommonUtils.isAtleastNextXCalDay(installTimeStamp, System.currentTimeMillis(), config.thresholdDays) &&
                    !CommonUtils.isTimeExpired(installTimeStamp, config.ttlMs)) {
                    fireEvent(event.eventName, null, config)
                    updateEventState(event.eventName, eventState, preference, true)
                }
                return
            }
            AppsFlyerEvents.EVENT_APP_OPEN_BETWEEN_24_48_HOURS -> {
                if (curTimestamp - installTimeStamp > TimeUnit.HOURS.toMillis(24) &&
                    curTimestamp - installTimeStamp < TimeUnit.HOURS.toMillis(48)) {
                    fireEvent(event.eventName, null, config)
                    updateEventState(event.eventName, eventState, preference, true)
                }
                return
            }
            AppsFlyerEvents.EVENT_APP_OPEN_BETWEEN_72_96_HOURS -> {
                if (curTimestamp - installTimeStamp > TimeUnit.HOURS.toMillis(72) &&
                    curTimestamp - installTimeStamp < TimeUnit.HOURS.toMillis(96)) {
                    fireEvent(event.eventName, null, config)
                    updateEventState(event.eventName, eventState, preference, true)
                }
                return
            }
            AppsFlyerEvents.EVENT_USER_LOGIN_TRUECALLER,
            AppsFlyerEvents.EVENT_USER_LOGIN_FACEBOOK,
            AppsFlyerEvents.EVENT_USER_LOGIN_GOOGLE,
            AppsFlyerEvents.EVENT_LANG_SELECTED_TELUGU,
            AppsFlyerEvents.EVENT_LANG_SELECTED_TAMIL,
            AppsFlyerEvents.EVENT_LANG_SELECTED_PUNJABI,
            AppsFlyerEvents.EVENT_LANG_SELECTED_MALAYALAM,
            AppsFlyerEvents.EVENT_LANG_SELECTED_MARATHI,
            AppsFlyerEvents.EVENT_LANG_SELECTED_KANNADA,
            AppsFlyerEvents.EVENT_LANG_SELECTED_HINDI,
            AppsFlyerEvents.EVENT_LANG_SELECTED_GUJARATI,
            AppsFlyerEvents.EVENT_LANG_SELECTED_BENGALI,
            AppsFlyerEvents.EVENT_LANG_SELECTED_ENGLISH,
            AppsFlyerEvents.EVENT_NOTIFICATION_DELIVERY,
            AppsFlyerEvents.EVENT_NOTIFICATION_CLICK -> {
                if(!CommonUtils.isTimeExpired(installTimeStamp, config.ttlMs)) {
                    fireEvent(event.eventName, null, config)
                    updateEventState(event.eventName, eventState, preference, true)
                }
                return
            }
            AppsFlyerEvents.EVENT_TOTAL_APP_LAUNCHES -> {
                if(PreferenceManager.getPreference(AppStatePreference.TOTAL_FOREGROUND_SESSION, 0L) > config.sessionThreshold &&
                    !CommonUtils.isTimeExpired(installTimeStamp, config.ttlMs)) {
                    fireEvent(event.eventName, null, config)
                    updateEventState(event.eventName, eventState, preference, true)
                }
                return
            }
            else -> {
                //If the ttl from install time is passed, fire the event with the level accumulated
                if (CommonUtils.isTimeExpired(installTimeStamp, config.ttlMs)) {
                    val suffix = computeEventSuffix(config, eventState.counter)
                    if (suffix != null) {
                        fireEvent(event.eventName + suffix, null)
                        updateEventState(event.eventName, eventState, preference, true)
                    }
                    return
                }
                //If the event happened now, increment the counter and update the state.
                if (eventHappened) {
                    /**
                     * Some events need to be counted once in a time window. For ex: days_opened needs to be
                     * counted once every 24 hours.
                     */
                    if (CommonUtils.isTimeExpired(eventState.lastUpdatedTime, timeWindow)) {
                        eventState.counter = eventState.counter + 1
                        eventState.lastUpdatedTime = System.currentTimeMillis()
                    }
                    if (eventState.counter >= config.high) { //If we have already reached the high level, fire the event right away.
                        updateEventState(event.eventName, eventState, preference, true)
                        fireEvent(event.eventName.plus(AppsFlyerEventSuffix.EVENT_SUFFIX_HIGH.suffix), null)
                    } else { //Just update the preference
                        updateEventState(event.eventName, eventState, preference, false)
                    }
                }
            }
        }
    }

    /**
     * Compute the level each event has reached based on the config and current counter
     * @param counterConfig  Event config
     * @param currentCounter current counter
     * @return
     */
    private fun computeEventSuffix(counterConfig: EventConfig,
                                   currentCounter: Int): String? {
        return if (currentCounter < counterConfig.low) {
            return null
        } else if (currentCounter >= counterConfig.low &&
                currentCounter < counterConfig.mid) {
            AppsFlyerEventSuffix.EVENT_SUFFIX_LOW.suffix
        } else if (currentCounter >= counterConfig.mid &&
                currentCounter < counterConfig.high) {
            AppsFlyerEventSuffix.EVENT_SUFFIX_MID.suffix
        } else {
            AppsFlyerEventSuffix.EVENT_SUFFIX_HIGH.suffix
        }
    }

    /**
     * Update the current state of the event in the preference.
     * @param eventName    event name
     * @param currentState current state
     * @param preference   Preference to be updated
     * @param consumed     consumed or not?
     */
    private fun updateEventState(eventName: String,
                                 currentState: EventState,
                                 preference: SavedPreference, consumed: Boolean) {
        Logger.d(APPSFLYER_TAG, "Event: $eventName EventState: $currentState")
        currentState.consumed = consumed
        PreferenceManager.savePreference(preference, JsonUtils.toJson(currentState))
    }

    /**
     * Post an event to AppsFlyer dashboard
     */
    private fun fireEvent(eventName: String, params: Map<String, Any>?, config: EventConfig? = null, currentTS: String? = null) {
        if (!initDone) { //Can ignore this call
            return
        }
        Logger.d(APPSFLYER_TAG, "Track Event: $eventName")
        AppsFlyerLib.getInstance().logEvent(CommonUtils.getApplication(), eventName, params)
        if(eventHashMap.contains(eventName)) {
            val map : HashMap<String, String> = HashMap()
            map[APPSFLYER_EVENT_NAME] = eventName
            map[AF_INSTALL_TIMESTAMP] = PreferenceManager.getPreference(AppStatePreference.INSTALL_TIMESTAMP, 0L).toString()
            val currTime = currentTS ?: System.currentTimeMillis()
            map[AF_CURRENT_TIMESTAMP] = currTime.toString()
            AnalyticsHelper.logAppsFlyerDevErrorEvent(map)
        }
        config?.let {
            if(it.triggerFirebaseEvent == true) {
                FirebaseAnalyticsHelper.logAppsFlyerEvents(eventName, null)
            }
        }
    }

    /**
     * Loop through the queue and process all the events we got while response was null. Do this in
     * background
     */
    private fun processQueuedEvents() {
        CommonUtils.runInBackground {
            Logger.d(APPSFLYER_TAG, "Processing queued events")
            if (!CommonUtils.isEmpty(eventQueue)) {
                while (!eventQueue.isEmpty()) {
                    val eventPair = eventQueue.poll()
                    eventPair?.first?.let {
                        trackEvent(it, eventPair.second, true)
                    }
                }
            }
        }
    }

    fun onAppStopEvents(startTime: Long, endTime: Long) {
        trackEvent(AppsFlyerEvents.EVENT_TIMESPENT_TOP_20,null)
        trackEvent(AppsFlyerEvents.EVENT_TIMESPENT_TOP_20_SMALLER_THRESHOLD,null)
        trackEvent(AppsFlyerEvents.EVENT_TIMESPENT_TOP_40,null)
        trackEvent(AppsFlyerEvents.EVENT_TIMESPENT_TOP_40_SMALLER_THRESHOLD,null)
        trackEvent(AppsFlyerEvents.EVENT_TIMESPENT_TOP_60,null)
        trackEvent(AppsFlyerEvents.EVENT_TIMESPENT_TOP_60_SMALLER_THRESHOLD,null)
        if(PreferenceManager.getPreference(AppStatePreference.TOTAL_FOREGROUND_SESSION, 0L) == 1L) {
            eventsConfigResponse?.eventConfigMap?.let { configMap ->
                val map = configMap[AppsFlyerEvents.EVENT_TIMESPENT_FIRST_SESSION.eventName]
                map?.let {
                    if((endTime - startTime) * 1000 > it.thresholdMs) { //end time and start time are stored as seconds
                        trackEvent(AppsFlyerEvents.EVENT_TIMESPENT_FIRST_SESSION, null)
                    }
                }
            }
        }
    }

    fun onAppStartEvents() {
        trackEvent(AppsFlyerEvents.EVENT_TOTAL_APP_LAUNCHES, null)
        trackEvent(AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY1, null)
        trackEvent(AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY3, null)
        trackEvent(AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY7, null)
        trackEvent(AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY15, null)
        trackEvent(AppsFlyerEvents.EVENT_APP_OPEN_ON_DAY30, null)
        trackEvent(AppsFlyerEvents.EVENT_APP_OPEN_AFTER_3_DAYS, null)
        trackEvent(AppsFlyerEvents.EVENT_APP_OPEN_BETWEEN_24_48_HOURS, null)
        trackEvent(AppsFlyerEvents.EVENT_APP_OPEN_BETWEEN_72_96_HOURS, null)
    }

    fun fireAppsflyerInstallEvents() {
        val json = PreferenceManager.getPreference(
            AppStatePreference.USER_REGISTRATION_INFO,
            Constants.EMPTY_STRING
        )
        val type = object : TypeToken<RegisteredClientInfo?>() {}.type
        val regResponse: RegisteredClientInfo? = JsonUtils.fromJson(json, type)
        Logger.d(APPSFLYER_TAG, "registration resp : $regResponse")
        regResponse?.let {
            if(it.newClient) {
                trackEvent(AppsFlyerEvents.EVENT_USER_NEW_INSTALL, null)
            } else {
                trackEvent(AppsFlyerEvents.EVENT_USER_RE_INSTALL, null)
            }
        } ?: trackEvent(AppsFlyerEvents.EVENT_USER_RE_INSTALL, null)
        PreferenceManager.remove(AppStatePreference.USER_REGISTRATION_INFO)
    }
}