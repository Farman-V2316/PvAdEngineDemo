/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.news.model.apis.PostDeletionService
import com.newshunt.news.model.daos.BookmarksDao
import com.newshunt.news.model.daos.HistoryDao
import io.reactivex.Observable
import java.util.Collections
import javax.inject.Inject

/**
 * @author amit.chaudhary
 */
class DeletePostUsecase @Inject constructor(private val postDeletionApi: PostDeletionService,
                                            private val dislikeUsecase: DislikeUsecase,
                                            private val bookmarksDao: BookmarksDao,
                                            private val historyDao: HistoryDao) :
        BundleUsecase<Boolean> {

    override fun invoke(args: Bundle): Observable<Boolean> {
        val post = args.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset ?: kotlin.run {
            Logger.e(LOG_TAG, "post can not be null")
            return Observable.error(Throwable("Post can not be null"))
        }

        val type = post.i_type()

        val header = args.getString(Constants.BUNDLE_DELETE_HEADER, Constants.EMPTY_STRING)
        args.putBoolean(Constants.BUNDLE_MARK_DISLIKE_FOR_PAYLOAD, false)

        return postDeletionApi.deleteComment(post.i_id(), type, header).flatMap {
            dislikeUsecase.invoke(args)
        }.map {
            val posts = Collections.singletonList(post.i_id())
            bookmarksDao.deletePost(posts)
            historyDao.deletePost(posts)
            it
        }
    }
}

private const val LOG_TAG = "DeletePostUsecase"