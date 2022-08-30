/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.appsflyer

import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * POJOs and enums for AppsFlyer events
 * <p>
 * Created by srikanth.ramaswamy on 09/17/2018.
 */

/**
 * Server response POJO for Appsflyer config event
 */
data class AppsFlyerEventsConfigResponse(val version: String = Constants.EMPTY_STRING,
                                         var uniqueRequestId: Int? = 0,
                                         val eventConfigMap: Map<String, EventConfig>? = null)

/**
 * Each AppsFlyer event comes with this configuration. Threshold for low, medium, high and a ttl
 * in milliseconds. After the expiry of the ttl, we fire the event with whatever level user has
 * reached for each event.
 */
data class EventConfig(val low: Int,
                       val mid: Int,
                       val high: Int,
                       val ttlMs: Long,
                       val thresholdMs: Long = 0L, //target time spent for events in ms
                       val thresholdDays: Int = 0, //after target days i.e. 3 -> event occurrence after 3 days of install
                       val sessionThreshold: Int = 0, //minimum number of sessions required to fire event
                       val triggerFirebaseEvent: Boolean?)

/**
 * POJO to save the current state of each event
 */
data class EventState(var counter: Int,
                      var lastUpdatedTime: Long,
                      var consumed: Boolean) {

    override fun toString(): String {
        return "{counter: $counter, lastUpdatedTime: $lastUpdatedTime, consumed: $consumed}"
    }
}

/**
 * Enumeration of all the events supported
 */
enum class AppsFlyerEvents(val eventName: String) {
    EVENT_SPLASH_OPEN("DHAppOpen"),
    EVENT_CONTENT_CONSUMED("content_consumed"),
    EVENT_DAYS_OPENED_WITHIN_THRESHOLD("days_opened"),
    EVENT_LANG_SELECTED("language_selected"),
    EVENT_FIRST_AD_IMPRESSION("af_manhatten_a"),
    EVENT_FIRST_VIDEO_AD_IMPRESSION("af_manhatten_b"),
    EVENT_DAY_1_OPENED("App_opened_Day_1"),
    EVENT_FIRST_DETAIL_VIEW("detail_page_view"),
    EVENT_TIMESPENT_TOP_20("DH_E1"),
    EVENT_TIMESPENT_TOP_40("DH_E2"),
    EVENT_TIMESPENT_TOP_60("DH_E3"),
    EVENT_TIMESPENT_TOP_20_SMALLER_THRESHOLD("DH_E4"),
    EVENT_TIMESPENT_TOP_40_SMALLER_THRESHOLD("DH_E5"),
    EVENT_TIMESPENT_TOP_60_SMALLER_THRESHOLD("DH_E6"),
    EVENT_TIMESPENT_FIRST_SESSION("DH_E7"),
    EVENT_TOTAL_APP_LAUNCHES("DH_E8"),
    EVENT_APP_OPEN_BETWEEN_24_48_HOURS("DH_E9"),
    EVENT_APP_OPEN_BETWEEN_72_96_HOURS("DH_E10"),
    EVENT_APP_OPEN_ON_DAY1("DH_E11"), //DAY starts from 0, i.e. DAY0, DAY1 etc
    EVENT_APP_OPEN_ON_DAY3("DH_E12"),
    EVENT_APP_OPEN_ON_DAY7("DH_E13"),
    EVENT_APP_OPEN_ON_DAY15("DH_E14"),
    EVENT_APP_OPEN_ON_DAY30("DH_E15"),
    EVENT_USER_LOGIN_GOOGLE("DH_E16"),
    EVENT_USER_LOGIN_FACEBOOK("DH_E17"),
    EVENT_USER_LOGIN_TRUECALLER("DH_E18"),
    EVENT_USER_ENGAGEMENT_LIKE("DH_E19"),
    EVENT_USER_ENGAGEMENT_SHARE("DH_E20"),
    EVENT_USER_ENGAGEMENT_COMMENT("DH_E21"),
    EVENT_USER_ENGAGEMENT_REPOST("DH_E22"),
    EVENT_USER_NEW_INSTALL("DH_E23"),
    EVENT_USER_RE_INSTALL("DH_E24"),
    EVENT_NOTIFICATION_DELIVERY("DH_E25"),
    EVENT_NOTIFICATION_CLICK("DH_E26"),
    EVENT_APP_OPEN_AFTER_3_DAYS("DH_E27"),
    EVENT_FIRST_CONTENT_VIEWED("DH_E28"),
    EVENT_LANG_SELECTED_ENGLISH("DH_E29"),
    EVENT_LANG_SELECTED_HINDI("DH_E30"),
    EVENT_LANG_SELECTED_MARATHI("DH_E31"),
    EVENT_LANG_SELECTED_BENGALI("DH_E32"),
    EVENT_LANG_SELECTED_GUJARATI("DH_E33"),
    EVENT_LANG_SELECTED_TAMIL("DH_E34"),
    EVENT_LANG_SELECTED_TELUGU("DH_E35"),
    EVENT_LANG_SELECTED_MALAYALAM("DH_E36"),
    EVENT_LANG_SELECTED_KANNADA("DH_E37"),
    EVENT_LANG_SELECTED_PUNJABI("DH_E38")
}

/**
 * Suffix for each event based on the level user has reached as per the EventConfig definition
 * for the event
 */
enum class AppsFlyerEventSuffix(val suffix: String) {
    EVENT_SUFFIX_LOW("_low"),
    EVENT_SUFFIX_MID("_mid"),
    EVENT_SUFFIX_HIGH("_high");
}

/**
 * Server response in register API to tell whether or not and when Appsflyer can be initialized.
 */
enum class AppsFlyerExistence(val existence: String) {
    ENABLED("ENABLED"),    //Appsflyer will be initialized soon after register success
    ENABLED_POST_ONBOARDING("ENABLED_POST_ONBOARDING"),    //Appsflyer will be initialized only after onboarding is complete
    DISABLED("DISABLED");    //Appsflyer will not be initialized

    companion object {
        fun fromName(name: String): AppsFlyerExistence {
            values().forEach {
                if (CommonUtils.equalsIgnoreCase(it.existence, name)) {
                    return it
                }
            }
            return DISABLED
        }
    }
}

/**
 * Lang code suffixed to the event EVENT_LANG_SELECTED
 */
const val EVENT_PARAM_LANG_CODE = "langCode"

//Wrapper class to call OnAppRegistrationController
data class AppsFlyerReferrerEvent(val appsFlyerInstallReferrer: String, val appsFlyerCampaignInfo: Map<String, String>)
