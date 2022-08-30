/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.analytics

import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsDevEvent
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.news.model.utils.InvalidCardsLogger

/**
 * Logs analytics event.
 * @author satosh.dhanyamraju
 */
object DefaultInvalidCardsLogger : InvalidCardsLogger {
    const val TAG = "DefaultInvalidCardsLogger"
    override fun log(message: String, pojo: Any) {
        val j = JsonUtils.toJson(pojo)
        val map = hashMapOf<String, String>(
                "message" to message,
                "item" to j
        )
        AnalyticsClient.logDynamic(NhAnalyticsDevEvent.DEV_CUSTOM_ERROR,
                NhAnalyticsEventSection.APP, null, map, false)
    }
}