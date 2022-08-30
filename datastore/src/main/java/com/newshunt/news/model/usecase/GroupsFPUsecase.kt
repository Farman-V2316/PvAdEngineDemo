/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.GroupInfoDao
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

//TODO(raunak):
// 1. Re-evaluate if we can reuse the above usecases and fetchDaos for Groups purpose.
// 2. Testcases for group/member daos

/**
 * @author raunak.yadav
 */
class GroupsFPUsecase
@Inject constructor(@Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("fetchUsecase") private val fetchUsecase: BundleUsecase<NLResponseWrapper>,
                    private val fetchDao: FetchDao,
                    private val groupDao: GroupInfoDao,
                    @Named("clearFPDataOnEmptyResponse")
                    private val clearFPDataOnEmptyResponse: Boolean) : BundleUsecase<NLResponseWrapper> {

    override fun invoke(p1: Bundle): Observable<NLResponseWrapper> {
        return Observable.fromCallable {
            fetchDao.insIgnore(FetchInfoEntity(entityId, location, section = section))
        }.flatMap {
            fetchUsecase
                    .invoke(FetchCardListFromUrlUsecase.bundle(fetchDao.lookupPage(entityId, section)!!))
                    .map {
                        val nlResp = it.nlResp
                        val groups = nlResp.rows.filterIsInstance<GroupInfo>()
                        val fetchInfoEntity = FetchInfoEntity(entityId, location, npUrlOf1stResponse = nlResp.nextPageUrl,
                                nextPageUrl = nlResp.nextPageUrl, currentPageNum = nlResp.pageNumber, section = section)
                        groupDao.replaceFirstPage(fetchDao, fetchInfoEntity, groups, it.reqUrl)
                        it
                    }
        }.onErrorResumeNext { it: Throwable? ->
            if (clearFPDataOnEmptyResponse && it is ListNoContentException) {
                //If this list needs data to be cleared when API gives a 204, delete the rows from DB.
                val fetchInfoEntity = FetchInfoEntity(entityId, location, npUrlOf1stResponse = null,
                        nextPageUrl = null, currentPageNum = 0, section = section)
                val feedPage = fetchDao.lookupPage(entityId, section)
                feedPage?.let {
                    groupDao.replaceFirstPage(fetchDao, fetchInfoEntity, listOf(), it.contentUrl)
                }
            }
            Observable.error(it)
        }
    }
}