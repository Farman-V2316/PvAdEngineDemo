/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.common.model.entity.cachedapi.CacheType
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.PullDao
import com.newshunt.news.model.repo.CardSeenStatusRepo
import io.reactivex.Observable
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Named

/**
 * 1. convert response to entities
 * 2. call appr dao method
 * @author satosh.dhanyamraju
 */
class NPUsecase
@Inject constructor(@Named("entityId") val entityId: String,
                    @Named("location") val location: String,
                    @Named("section") val section: String,
                    @Named("disableNpCache") val disableNpCache: Boolean,
                    val fetchCacheUsecase: FetchCacheUsecase,
                    val fetchDao: FetchDao,
                    val followEntityDao: FollowEntityDao,
                    val pullDao: PullDao,
                    private val cssRepo: CardSeenStatusRepo) : BundleUsecase<NLResp> {
    override fun invoke(p1: Bundle): Observable<NLResp> {
        return Observable.fromCallable {
            val fetchInfo = fetchDao.fetchInfo(entityId, location, section) ?: run {
                Logger.e(TAG, "couldn't read fetchdao from db: $entityId, $location")
                return@fromCallable p1
            }
            val npUrl = fetchInfo.nextPageUrl ?: run {
                Logger.d(TAG, "nextPageUrl in null. Pagination terminated?")
                return@fromCallable p1
            }
            val lookupPage = fetchDao.lookupPage(fetchInfo.entityId, section)
            val pageEntity = lookupPage?.copy(
                    contentUrl = npUrl
            ) ?: run {
                Logger.e(TAG, "entity is null")
                return@fromCallable p1
            }
            FetchCardListFromUrlUsecase.bundle(pageEntity, p1, lookupPage?.contentUrl)
        }.flatMap {
            val url = it.getString("url")
            val cacheType = if (disableNpCache) CacheType.NO_CACHE else CacheType.USE_NETWORK_IF_NO_CACHE
            if (url != null) fetchCacheUsecase(CachedApiCacheRx.urlToKey(url), it, cacheType)
            else Observable.empty()
        }.map {
            val nlResp = it.nlResp
            val fetchEntity = FetchInfoEntity(entityId, location, nlResp.nextPageUrl, nlResp.pageNumber, section = section)
            val posts = nlResp.rows.filterIsInstance<PostEntity>().map { post ->
                post.updateCacheFlagAndGet(nlResp.isFromNetwork.not())
            }
            fetchDao.appendNextPage(fetchEntity, posts, followEntityDao, it.key)
            pullDao.incrementLastPullInfoPageCount(entityId, section)
            val fetchId = fetchDao.fetchInfo(entityId, location, section)?.fetchInfoId ?: 0
            cssRepo.served(posts, fetchId)
            nlResp
        }.doOnError {
            // callback will be invoked in non-UI thread, so we can call dao methods directly.
            if (it is ListNoContentException && (it.error as? BaseError)?.statusAsInt == HttpURLConnection.HTTP_NO_CONTENT) {
                Logger.d(TAG, "can make it null ${Thread.currentThread().name}")
                fetchDao.paginationTerminated(entityId, location, section)
            }
        }

    }

    companion object {
        const val TAG = "NPUsecase"
    }
}