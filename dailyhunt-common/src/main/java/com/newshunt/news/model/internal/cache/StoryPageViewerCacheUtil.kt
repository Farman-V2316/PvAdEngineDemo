/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.news.model.internal.cache

import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.news.helper.StoryPageViewerCacheValue

/**
 * Converts events params to StoryPageViewerCacheValue
 * @return pair of item_id and StoryPageViewerCacheValue
 *
 * Created by Rahul Ravindran at 26/9/19 2:47 PM
 */
fun parseEventParams(params: Map<String, Any>): Pair<String, StoryPageViewerCacheValue>? {
    val itemId: String? = params[AnalyticsParam.ITEM_ID.getName()]?.toString()
    return itemId?.let {
        itemId to StoryPageViewerCacheValue(itemId,
                params[AnalyticsParam.GROUP_ID.getName()]?.toString(),
                params[AnalyticsParam.CONTENT_TYPE.getName()]?.toString(),
                params[NhAnalyticsNewsEventParam.CHUNKWISE_TS.getName()]?.toString(),
                params[NhAnalyticsNewsEventParam.ENGAGEMENT_PARAMS.getName()]?.toString(),
                params[AnalyticsParam.TIMESPENT.getName()]?.toString()?.toLong()
        )
    }
}