/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.dailyhunt.datastore.R
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.DbgCode
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.common.model.entity.cachedapi.CacheType
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Named

/**
 * Fetches foryou 1st page
 *
 * @author satosh.dhanyamraju
 */
class FPFetchUseCase
@Inject constructor(@Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("disableFpCache") val disableFpCache: Boolean,
                    private val fetchUsecase: FetchCacheUsecase,
                    private val fetchDao: FetchDao,
                    private val followEntityDao: FollowEntityDao,
                    @Named("clearFPDataOnEmptyResponse")
                    private val clearFPDataOnEmptyResponse: Boolean,
                    @Named(NewsConstants.BUNDLE_ENABLE_MAX_DURATION_TO_NOT_FETCH_FP)
                    private val enableMaxDurToNotFetchFP: Boolean) : BundleUsecase<NLResponseWrapper> {

    override fun invoke(p1: Bundle): Observable<NLResponseWrapper> {
        val pullToRfrsh = p1.getBoolean(B_PULL_TO_RFRSH, false)
        var fetchInfo: FetchInfoEntity? = null
        return Observable.fromCallable {
            fetchDao.insIgnore(FetchInfoEntity(entityId, location, section = section))
            val curTs = System.currentTimeMillis()
            fetchInfo = fetchDao.fetchInfo(entityId, location, section)
            val lastDestroy = fetchInfo?.lastViewDestroyTs ?: -1
            curTs - lastDestroy < MAX_DURATION_TO_NOT_FETCH_FP
        }.flatMap {
            val dataCount = fetchDao.cardCount(fetchInfo?.fetchInfoId ?: -1L)
            val page = fetchDao.lookupPage(entityId, section)
            if (it && !pullToRfrsh && enableMaxDurToNotFetchFP && dataCount > 0) { // do not make api call
                Logger.d(TAG, "not reqesting FP.")
                Observable.empty<NLResponseWrapper>()
            } else if(page == null) {
                Observable.empty<NLResponseWrapper>()
            } else {
                val cacheForRequestEnabled = p1.getBoolean(Constants.REQUEST_WITH_CACHE, true)
                val cacheType = if (disableFpCache) {
                    CacheType.NO_CACHE
                } else if (cacheForRequestEnabled) {
                    CacheType.DELAYED_CACHE_AND_NETWORK
                } else {
                    CacheType.IGNORE_CACHE_AND_UPDATE
                }
                fetchUsecase.invoke(CachedApiCacheRx.urlToKey(page.contentUrl),
                        FetchCardListFromUrlUsecase.bundle(page, p1), cacheType)
                        .map { nlRespWrapper : NLResponseWrapper ->

                            val nlResp = nlRespWrapper.nlResp
                            if (nlResp.rows.isNullOrEmpty() && nlResp.isFromNetwork) {
                                // 200 ok with empty response
                                val error = BaseError(DbgCode.DbgHttpCode(HttpURLConnection.HTTP_NO_CONTENT), message = "No content found")
                                throw ListNoContentException(error)
                            }
                            nlRespWrapper
                        }
            }
        }.onErrorResumeNext { it: Throwable? ->
            if (clearFPDataOnEmptyResponse && it is ListNoContentException) {
                //If this list needs data to be cleared when API gives a 204, delete the rows from DB.
                val fetchInfoEntity = FetchInfoEntity(entityId, location, npUrlOf1stResponse = null,
                        nextPageUrl = null, currentPageNum = 0, section = section)
                val feedPage = fetchDao.lookupPage(entityId, section)
                feedPage?.let {
                    // TODO(satosh.dhanyamraju): write a clear function
                    fetchDao.replaceFirstPage(fetchInfoEntity, listOf(), followEntityDao, it
                            .contentUrl, null)
                }
            }
            Observable.error(it)
        }
    }

    companion object {
        const val B_PULL_TO_RFRSH = "pullToRefresh"
        @VisibleForTesting
        const val MAX_DURATION_TO_NOT_FETCH_FP = 60_000L
        private const val TAG = "FPUsecase"
    }
}