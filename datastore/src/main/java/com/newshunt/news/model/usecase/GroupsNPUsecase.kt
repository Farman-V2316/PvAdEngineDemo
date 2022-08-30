/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.GroupInfoDao
import io.reactivex.Observable
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Named
/**
 * @author raunak.yadav
 */
class GroupsNPUsecase
@Inject constructor(@Named("entityId") val entityId: String,
                    @Named("location") val location: String,
                    @Named("section") val section: String,
                    @Named("fetchUsecase") val fetchUsecase: BundleUsecase<NLResponseWrapper>,
                    private val fetchDao: FetchDao,
                    private val groupDao: GroupInfoDao) : BundleUsecase<NLResp> {
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
        }
        .flatMap {
            val url = it.getString("url")
            if (url != null) {
                fetchUsecase(it)
            } else {
                Observable.empty()
            }
        }
        .map {
            val nlResp = it.nlResp
            val fetchEntity = FetchInfoEntity(entityId, location, nlResp.nextPageUrl, nlResp.pageNumber, section = section)
            val groups = nlResp.rows.filterIsInstance<GroupInfo>()
            groupDao.appendNextPage(fetchDao, fetchEntity, groups, it.key)
            nlResp
        }
        .doOnError {
            // callback will be invoked in non-UI thread, so we can call dao methods directly.
            if (it is ListNoContentException && (it.error as? BaseError)?.statusAsInt == HttpURLConnection.HTTP_NO_CONTENT) {
                Logger.d(NPUsecase.TAG, "can make it null ${Thread.currentThread().name}")
                fetchDao.paginationTerminated(entityId, location, section)
            }
        }
    }

    companion object {
        const val TAG = "GroupsNPUsecase"
    }
}