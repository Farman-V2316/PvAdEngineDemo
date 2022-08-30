/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.ArticleTimeSpentTrackEntity
import com.newshunt.news.model.daos.RecentArticleTrackerDao
import io.reactivex.Observable

/**
 * @author amit.chaudhary
 */
class InsertRecentArticleTrackUsecase(private val recentArticleTrackerDao: RecentArticleTrackerDao) : BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        val timeSpentTrackEntity = p1.getSerializable(Constants.BUNDLE_TIME_SPENT_TRACK_ENTITY)
                as? ArticleTimeSpentTrackEntity
        timeSpentTrackEntity ?: return Observable.error(Throwable("can not track null entity"))
        return Observable.fromCallable {
            recentArticleTrackerDao.customInsert(listOf(timeSpentTrackEntity))
            true
        }
    }
}