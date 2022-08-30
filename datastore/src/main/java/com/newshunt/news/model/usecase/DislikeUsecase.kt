/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dataentity.social.entity.DislikeEntity
import com.newshunt.news.model.daos.DislikeDao
import com.newshunt.news.model.daos.PostDao
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * @author amit.chaudhary
 * */
class DislikeUsecase @Inject constructor(private val dislikeDao: DislikeDao,
                                         private val postDao: PostDao,
                                         private val followBlockUpdateUsecase:FollowBlockUpdateUsecase) : BundleUsecase<Boolean> {
    override fun invoke(args: Bundle): Observable<Boolean> {
        val postIds = args.getStringArrayList(Constants.BUNDLE_POST_IDS)

        val isDislikeFromReport = args.getBoolean(Constants.DISLIKE_FROM_REPORT)
        val directPost = args.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset

        val l1Ids = args.getString(Constants.BUNDLE_L1_IDS)
                ?.split(Constants.COMMA_CHARACTER)
                ?.map { it.trim() }
        /* l1_id_1,l1_id_2,l1_id_3 (Stringformat) */

        val l2Ids = args.getString(Constants.BUNDLE_L2_IDS)
                ?.split(Constants.COMMA_CHARACTER)
                ?.map { it.trim() }
        /* l2_id_1,l2_id_2,l2_id_3 (Stringformat) */

        val markedForPayload = args.getBoolean(Constants.BUNDLE_MARK_DISLIKE_FOR_PAYLOAD,true)

        val postEntities: Observable<List<CommonAsset>> = if (directPost != null) {
            Observable.just(listOf(directPost))
        } else if (postIds != null && postIds.isNotEmpty()) {
            Observable.fromCallable { postDao.postEntitiesById(postId = postIds).map { it as CommonAsset } }
        } else {
            Observable.error(Throwable("Direct post and post Id both are null"))
        }

        return postEntities.map { commonAssetList ->
            dislikeDao.insReplace(commonAssetList.map {
                it.toDislikeEntity2(System.currentTimeMillis(),
                        l1Ids = l1Ids,
                        l2Ids = l2Ids,
                        markedForPayload = markedForPayload)
            })
            commonAssetList
        }.flatMap {
            Observable.fromIterable(it)
        }.flatMap { asset ->
            asset.i_source()?.id?.let {
                if(isDislikeFromReport) {
                    followBlockUpdateUsecase.invoke(SourceFollowBlockEntity(sourceId = it, reportCount = 1, postSourceEntity = asset.i_source(), updateTimeStamp = System.currentTimeMillis(), sourceLang = asset.i_langCode() ?: Constants.DEFAULT_LANGUAGE, updateType = FollowActionType.BLOCK))
                } else {
                    followBlockUpdateUsecase.invoke(SourceFollowBlockEntity(sourceId = it, showLessCount = 1, postSourceEntity = asset.i_source(), updateTimeStamp = System.currentTimeMillis(),sourceLang = asset.i_langCode() ?: Constants.DEFAULT_LANGUAGE, updateType = FollowActionType.BLOCK))
                }
            }
        }
    }
}

@VisibleForTesting
fun CommonAsset.toDislikeEntity2(createTime: Long,
                                 l1Ids: List<String>? = null,
                                 l2Ids: List<String>? = null,
                                 markedForPayload: Boolean = true): DislikeEntity {
    return DislikeEntity(
            postId = this.i_id(),
            format = this.i_format(),
            subFormat = this.i_subFormat(),
            sourceEntityType = this.i_source()?.entityType,
            createdAt = createTime,
            sourceId = this.i_source()?.id,
            sourceSubType = this.i_source()?.type,
            options = l1Ids,
            optionsL2 = l2Ids,
            markedForPayload = markedForPayload
    )
}

/**
 * Usecase implementation to delete a disliked item from the DB
 */
class UndoDislikeUsecase @Inject constructor(private val dislikeDao: DislikeDao) : BundleUsecase<Boolean> {
    override fun invoke(args: Bundle): Observable<Boolean> {
        val postIds = args.getStringArrayList(Constants.BUNDLE_POST_IDS) ?: kotlin.run {
            Logger.e(LOG_TAG, "Post ids can not be empty")
            return Observable.error(Throwable("Post ids can not be empty"))
        }
        return Observable.fromCallable {
            dislikeDao.delete(postIds)
            true
        }
    }
}

private const val LOG_TAG = "DislikeUsecase"