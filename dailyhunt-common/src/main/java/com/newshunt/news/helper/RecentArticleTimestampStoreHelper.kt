/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.common.asset.ArticleTimeSpentTrackEntity
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.InsertRecentArticleTrackUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.toMediator2

/**
 * @author amit.chaudhary
 */
class RecentArticleTimestampStoreHelper {
    private val insertArticleTrackUsecase: MediatorUsecase<Bundle, Boolean> =
            InsertRecentArticleTrackUsecase(SocialDB.instance().recentArticleTrackerDao())
                    .toMediator2()
    private val TAG: String = "RecentArticleTimestampS"
    @JvmOverloads
    fun trackTimeSpentForArticle(params: Map<String, Any>,
                                 chunkWiseTimeSpent: Map<Int, Long>,
                                 engagementParams: Array<String>,
                                 totalTimeSpent: Long,
                                 referrer: String? = null) {
        val itemId: String = params[AnalyticsParam.ITEM_ID.getName()] as? String ?: kotlin.run {
            return
        }

        val format: String = params[AnalyticsParam.FORMAT.getName()] as? String ?: kotlin.run {
            return
        }

        val subFormat: String = params[AnalyticsParam.SUB_FORMAT.getName()] as? String
                ?: kotlin.run {
                    return
                }

        val trackEntity = ArticleTimeSpentTrackEntity(itemId = itemId,
                chunkwiseTs = chunkWiseTimeSpent.values.joinToString(Constants.COMMA_CHARACTER) {
                    it.toString()
                },
                engagementParams = engagementParams.joinToString(Constants.COMMA_CHARACTER),
                subFormat = subFormat,
                format = format,
                totalTimeSpent = totalTimeSpent,
                timestamp = System.currentTimeMillis(),
                referrer = referrer?:"")
        val arg = Bundle()
        arg.putSerializable(Constants.BUNDLE_TIME_SPENT_TRACK_ENTITY, trackEntity)
        insertArticleTrackUsecase.execute(arg)
        Logger.d(TAG, "trackTimeSpentForArticle: inserted $itemId")
    }
}