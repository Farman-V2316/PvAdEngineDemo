/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.news.model.daos.PostDao
import io.reactivex.Single
import javax.inject.Inject

/**
 * Should be executed on clicking viral card (opening detail page)
 * @author satosh.dhanyamraju
 */
class IncrementViewcountUsecase @Inject constructor(private val postDao: PostDao) : SingleUsecase<Bundle, Boolean> {
    private val LOG_TAG = "IncrementViewcountUsecase"
    override fun invoke(p1: Bundle): Single<Boolean> {
        val postId = p1.getString(Constants.BUNDLE_POST_ID)
        val parentPostId = p1.getString(Constants.BUNDLE_PARENT_ID)
        if (postId.isNullOrEmpty()) {
            Logger.e(LOG_TAG, "postId missing")
            return Single.just(false)
        }
        return Single.fromCallable {
            postDao.incViewCount(postId, parentPostId)
            true
        }
    }

}