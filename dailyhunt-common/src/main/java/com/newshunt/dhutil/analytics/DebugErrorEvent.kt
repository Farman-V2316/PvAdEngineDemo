/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics

import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.dhutil.analytics.NhAnalyticsCommonEventParam
import io.reactivex.exceptions.CompositeException

/**
 * Used for logging non-network errors such as Gson-parsing-failure
 *
 * @author karthik.r
 */
enum class DebugErrorEvent : NhAnalyticsEvent {

    CARD_LIST_RESPONSE_ERROR,
    VH_SYNC_RESPONSE_ERROR;

    override fun isPageViewEvent() = false

    override fun toString() = this.name
}


/**
 * Creates params-map and logs error event
 */
@JvmOverloads
fun logDebugErrorEvent(
	event: NhAnalyticsEvent,
	throwable: Throwable?,
	appSection: NhAnalyticsEventSection = NhAnalyticsEventSection.NEWS,
	refreshUrl: String? = null,
	uniqueRequestId: String? = null,
	pageType: String? = null) {
    try {
        val paramsMap: Map<NhAnalyticsEventParam, Any?> = mapOf(
                AnalyticsParam.MESSAGE to (throwable.originalMessage()),
                NhAnalyticsCommonEventParam.ERROR_URL to refreshUrl.orEmpty(),
                NhAnalyticsCommonEventParam.UNIQUE_ID to uniqueRequestId.orEmpty(),
                NhAnalyticsAppEventParam.PAGE_TYPE to pageType.orEmpty())
        AnalyticsClient.logError(event, appSection, paramsMap)
    } catch (e: Exception) {
        Logger.caughtException(e)
    }
}

fun CompositeException.compositeMessage() =
        this.exceptions?.map { it.message }?.joinToString(separator = "!!!")

fun Throwable?.originalMessage(): String {
    this ?: return ""
    return when (this) {
    // if composite exception, make string of all its sub-exceptions messages
        is CompositeException -> (this as? CompositeException)?.compositeMessage()
        is BaseError ->
            // baseerror can hold a throwable which could be CompositeException
            when (this.originalError) {
                is CompositeException -> (this.originalError as? CompositeException)?.compositeMessage()
                else -> this.originalError?.message
            }
        else -> null
    } ?: message ?: ""
}
