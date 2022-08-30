package com.newshunt.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.client.AttributeFilter
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.news.analytics.NhAnalyticsAppState
import java.util.*

object FirebaseAnalyticsHelper {

    private val fireBaseClient = FirebaseAnalytics.getInstance(CommonUtils.getApplication())

    fun logContentViewEvent(map: Map<NhAnalyticsEventParam, Any?>? = mutableMapOf()) {

        val paramsMap = mutableMapOf<NhAnalyticsEventParam, Any?>()
        map?.let { paramsMap.putAll(it) }
        paramsMap[NhAnalyticsAppEventParam.PAGE_VIEW_EVENT] = true
        AnalyticsClient.addStateParamsAndPermanentParams(paramsMap)
        val appStateParams = NhAnalyticsAppState.getInstance().getStateParams(true)
        appStateParams?.let {
            paramsMap.putAll(appStateParams)
        }
        val stringParam = AttributeFilter.filterForNH(paramsMap)
        fireBaseClient.logEvent(
            NhAnalyticsAppEvent.CONTENT_VIEW.name.toLowerCase(Locale.ENGLISH), convertMapToBundle(stringParam))
    }

    private fun convertMapToBundle(map: MutableMap<String, Any?>?, pageReferrer: PageReferrer? = null): Bundle {

        val bundle = Bundle()
        map ?: return bundle

        if(pageReferrer != null) {
            map[NhAnalyticsAppEventParam.REFERRER.name] = pageReferrer.referrer
            map[NhAnalyticsAppEventParam.REFERRER_ID.name] = pageReferrer.id
            map[NhAnalyticsAppEventParam.REFERRER_ACTION.name] = pageReferrer.referrerAction
        }

        val filteredMap = map.filterNotNullValues()
        for ((key, value) in filteredMap.entries) {
            val stringValue = value.toString()
            //firebase supports max 100 characters for value
            if (stringValue.length > 100) {
                bundle.putString(key, stringValue.substring(0, 100))
            } else {
                bundle.putString(key, stringValue)
            }
        }

        return bundle
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K, V> Map<K, V?>.filterNotNullValues() = filterValues { it != null } as Map<K, V>

    fun logContentSwipeEvent(map: Map<NhAnalyticsEventParam, Any?>? = mutableMapOf()) {
        val paramsMap = mutableMapOf<NhAnalyticsEventParam, Any?>()
        map?.let { paramsMap.putAll(it) }
        paramsMap[NhAnalyticsAppEventParam.PAGE_VIEW_EVENT] = true
        AnalyticsClient.addStateParamsAndPermanentParams(paramsMap)
        val appStateParams = NhAnalyticsAppState.getInstance().getStateParams(true)
        appStateParams?.let {
            paramsMap.putAll(appStateParams)
        }
        val stringParam = AttributeFilter.filterForNH(paramsMap)
        fireBaseClient.logEvent(
            NhAnalyticsAppEvent.CONTENT_SWIPE.name.toLowerCase(Locale.ENGLISH), convertMapToBundle(stringParam))
    }

    fun logAppsFlyerEvents(event: String, map: Map<NhAnalyticsEventParam, Any?>? = mutableMapOf()) {
        val paramsMap = mutableMapOf<NhAnalyticsEventParam, Any?>()
        map?.let { paramsMap.putAll(it) }
        AnalyticsClient.addStateParamsAndPermanentParams(paramsMap)
        val appStateParams = NhAnalyticsAppState.getInstance().getStateParams(true)
        appStateParams?.let {
            paramsMap.putAll(appStateParams)
        }
        val stringParam = AttributeFilter.filterForNH(paramsMap)
        fireBaseClient.logEvent(
            event.toLowerCase(Locale.ENGLISH), convertMapToBundle(stringParam))
    }

}