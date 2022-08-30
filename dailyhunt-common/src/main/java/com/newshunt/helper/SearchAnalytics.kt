/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.helper

import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.analytics.entity.NhAnalyticsSearchEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.cachedapi.CacheApiKeyBuilder.md5Hex
import com.newshunt.common.helper.common.Constants
import com.newshunt.dhutil.analytics.AnalyticsHelper
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dataentity.news.model.entity.PageType

/**
 *
 * Helper class and functions related to search analytics
 *
 * @author satosh.dhanymaraju
 */


object SearchAnalyticsHelper {


    private const val ct: String = "search"

    private var ci: String = ""

    var query = ""
        private set

    fun logSearchExecuted(requestId: String,
                          query: String,
                          referrer: PageReferrer,
                          total: Int,
                          itemType: String,
                          experiments: Map<String, String>?,
                          itemId: String) {
        ci = "${System.currentTimeMillis()}~${md5Hex(query)}"
        this.query = query
        val itemIdValue: Pair<NhAnalyticsEventParam, Any> = AnalyticsParam.ENTITY_ID to itemId
        val params: Map<NhAnalyticsEventParam, Any> = mapOf(
                NhAnalyticsSearchEventParam.SEARCH_ID to ci,
                NhAnalyticsSearchEventParam.TYPE to itemType,
                NhAnalyticsSearchEventParam.QUERY to query,
                itemIdValue,
                NhAnalyticsSearchEventParam.RESULT_ITEM_COUNT to total,
                NhAnalyticsSearchEventParam.REQUEST_ID to requestId
        ) + searchParams()
        AnalyticsHelper.logSearchEvent(NhAnalyticsAppEvent.SEARCH_EXECUTED, params, referrer,
                experiments?: mapOf())
    }

    fun logSearchInitiated(requestId: String, referrer: PageReferrer, hasUserTyped: String, experiments:
    Map<String, String>? = null) {
        val params: Map<NhAnalyticsEventParam, String> = hashMapOf(
                NhAnalyticsSearchEventParam.HAS_USER_TYPED to hasUserTyped,
                NhAnalyticsSearchEventParam.REQUEST_ID to requestId)
        AnalyticsHelper.logSearchEvent(NhAnalyticsAppEvent.SEARCH_INITIATED, params, referrer,
                experiments ?: hashMapOf())
    }


    @JvmStatic
    fun addSearchParams(nhAnalyticsEventSection: NhAnalyticsEventSection,
                        params: MutableMap<NhAnalyticsEventParam, Any?>) {
        if (nhAnalyticsEventSection == NhAnalyticsEventSection.SEARCH) {
            params.putAll(searchParams())
        }
    }

    @JvmStatic
    fun addSearchParamsForTs(nhAnalyticsEventSection: NhAnalyticsEventSection,
                             params: MutableMap<String, Any>) {
        if (nhAnalyticsEventSection == NhAnalyticsEventSection.SEARCH) {
            params.putAll(searchParams().mapKeys { it.key.name })
        }
    }

    private fun searchParams(): Map<NhAnalyticsEventParam, Any> = mapOf(
            NhAnalyticsSearchEventParam.CT to ct,
            NhAnalyticsSearchEventParam.CI to ci
    )

    fun getParamsForLocationSearch(query: String, itemCount: Int): Map<NhAnalyticsEventParam, Any?> {
        val params: Map<NhAnalyticsEventParam, Any?> = mapOf(
                NhAnalyticsSearchEventParam.SEARCH_ID to SearchAnalyticsHelper.ci,
                NhAnalyticsSearchEventParam.TYPE to Constants.LOCATION_SEARCH_ITEM_TYPE,
                NhAnalyticsSearchEventParam.QUERY to query,
                NhAnalyticsSearchEventParam.RESULT_ITEM_COUNT to itemCount
        ) + SearchAnalyticsHelper.searchParams()
        return params
    }
}

fun pageTypeToSection(pageType: PageType?,pageReferrer: PageReferrer?) : NhAnalyticsEventSection {
    pageType ?: return referrerToSection(pageReferrer)
    return when (pageType.pageType) {
        PageType.SEARCH.pageType -> NhAnalyticsEventSection.SEARCH
        PageType.FEED.pageType -> NhAnalyticsEventSection.FOLLOW
        else -> referrerToSection(pageReferrer)
    }
}

fun referrerToSection(referrer: PageReferrer?) : NhAnalyticsEventSection {
    referrer ?: NhAnalyticsEventSection.NEWS

    return when {
        CommonNavigator.isFromFollowHome(referrer) -> NhAnalyticsEventSection.FOLLOW
        CommonNavigator.isFromSearch(referrer) -> NhAnalyticsEventSection.SEARCH
        else -> NhAnalyticsEventSection.NEWS
    }
}
