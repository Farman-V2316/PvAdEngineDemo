/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.MemberDao
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named
/**
 * @author raunak.yadav
 */
class MembersFPUsecase
@Inject constructor(@Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("fetchUsecase") private val fetchUsecase: BundleUsecase<NLResponseWrapper>,
                    private val fetchDao: FetchDao,
                    private val memberDao: MemberDao,
                    @Named("clearFPDataOnEmptyResponse")
                    private val clearFPDataOnEmptyResponse: Boolean) : BundleUsecase<NLResponseWrapper> {

    override fun invoke(bundle: Bundle): Observable<NLResponseWrapper> {
        return Observable.fromCallable {
            fetchDao.insIgnore(FetchInfoEntity(entityId, location, section = section))
        }.flatMap {
            fetchUsecase
                    .invoke(FetchCardListFromUrlUsecase.bundle(fetchDao.lookupPage(entityId, section)!!))
                    .map {
                        val nlResp = it.nlResp
                        val members = nlResp.rows.filterIsInstance<Member>()
                        val fetchInfoEntity = FetchInfoEntity(entityId, location, npUrlOf1stResponse = nlResp.nextPageUrl,
                                nextPageUrl = nlResp.nextPageUrl, currentPageNum = nlResp.pageNumber, section = section)
                        memberDao.replaceFirstPage(fetchDao, fetchInfoEntity, members, it.reqUrl)
                        it
                    }
        }.onErrorResumeNext { it: Throwable? ->
            if (clearFPDataOnEmptyResponse && it is ListNoContentException) {
                //If this list needs data to be cleared when API gives a 204, delete the rows from DB.
                val fetchInfoEntity = FetchInfoEntity(entityId, location, npUrlOf1stResponse = null,
                        nextPageUrl = null, currentPageNum = 0, section = section)
                val feedPage = fetchDao.lookupPage(entityId, section)
                feedPage?.let {
                    memberDao.replaceFirstPage(fetchDao, fetchInfoEntity, listOf(), it.contentUrl)
                }
            }
            Observable.error(it)
        }
    }
}