/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.PullDao
import com.newshunt.news.model.repo.CardSeenStatusRepo
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * Replaces first page in DB
 *
 * @author satosh.dhanyamraju
 */
class FPInserttoDBUsecase @Inject constructor(@Named("entityId") private val entityId: String,
                                              @Named("location") private val location: String,
                                              @Named("section") private val section: String,
                                              private val fetchDao: FetchDao,
                                              private val followEntityDao: FollowEntityDao,
                                              private val pullDao: PullDao,
                                              private val cpDao: CreatePostDao,
                                              @Named("isForyouPage") private val isForyouPage: Boolean,
                                              @Named("isMyPostsPage") private val isMyPostsPage: Boolean,
                                              @Named("localCardTtl") private val localCardTtl: Long,
                                              private val cssRepo: CardSeenStatusRepo) : BundleUsecase<NLResponseWrapper> {

    override fun invoke(p1: Bundle): Observable<NLResponseWrapper> {
        val nlRespWrapper = (p1.getSerializable(B_RESP) as? NLResponseWrapper)?: return Observable.empty()
        return Observable.fromCallable {
            val nlResp = nlRespWrapper.nlResp
            val posts = nlResp.rows.filterIsInstance<PostEntity>().map {
                it.updateCacheFlagAndGet(nlResp.isFromNetwork.not())
            }
            val fetchInfoEntity = FetchInfoEntity(entityId, location, npUrlOf1stResponse = nlResp.nextPageUrl,
                    nextPageUrl = nlResp.nextPageUrl, currentPageNum = nlResp.pageNumber, section = section)
            //mark old css
            val fetchIdB4 = fetchDao.fetchInfo(entityId, location, section)?.fetchInfoId ?: -1
            if (fetchIdB4 != -1L) {
                cssRepo.markDiscardedFromFetchId(fetchIdB4)
            }

            //ins
            if (isForyouPage || isMyPostsPage) {
                fetchDao.replaceFirstPageWithLocalCards(fetchInfoEntity, posts, followEntityDao,
                        nlRespWrapper.reqUrl, nlResp.adSpec, isForyouPage, isMyPostsPage, localCardTtl = localCardTtl, isNetworkResponse = nlResp.isFromNetwork)
            } else {
                fetchDao.replaceFirstPage(fetchInfoEntity, posts, followEntityDao,
                        nlRespWrapper.reqUrl, nlResp.adSpec)
            }
            val fetchId = fetchDao.fetchInfo(entityId, location, section)?.fetchInfoId ?: 0
            cssRepo.served(posts, fetchId)
            pullDao.insertOrReplaceRecentTab(entityId, section)
            if(nlResp.isFromNetwork) {
                pullDao.insertPullInfo(entityId, section)
            }
            if(isForyouPage || isMyPostsPage) cpDao.markLocalCardsAsShown()
            nlRespWrapper
        }
    }

    companion object {
        const val B_RESP = "b_resp"
    }
}

/**
 * A no op implementation which can be used as a proxy to FPInserttoDBUsecase in places where we
 * do not want to delay insertion of first page to DB. Currently used in member and group info
 * listing
 */
class NoOpFPInserttoDBUsecase @Inject constructor() : BundleUsecase<NLResponseWrapper> {
    override fun invoke(p1: Bundle): Observable<NLResponseWrapper> {
        return Observable.empty()
    }
}