/*
 * Created by Rahul Ravindran at 26/9/19 7:12 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.search.model.service

import android.os.Bundle
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.search.RecentSearchEntity
import com.newshunt.news.model.daos.SearchServiceDao
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.search.viewmodel.SugItemList
import io.reactivex.Observable
import javax.inject.Inject

/**
 * @author satosh.dhanymaraju
 */


class RecentsReadUseCase @Inject constructor(private val ssDao: SearchServiceDao,
                                             private val requestType: SearchRequestType) : BundleUsecase<SugItemList> {

    companion object {
        val RS_QUERY = "query"
    }

    override fun invoke(p1: Bundle): Observable<SugItemList> {
        val query = p1.getString(RS_QUERY) ?: ""
        return Observable.fromCallable { ssDao.getSearchItems("$query%") }
    }
}

class RecentsInsertUseCase @Inject constructor(private val ssDao: SearchServiceDao) : BundleUsecase<Boolean> {
    companion object {
        val RS_ENTITY = "rs_entity"
    }
    override fun invoke(p1: Bundle): Observable<Boolean> {
        val entity = p1.getSerializable(RS_ENTITY) as RecentSearchEntity
        return Observable.fromCallable {
            ssDao.insReplace(entity)
            true
        }
    }
}

class RecentsDelUseCase @Inject constructor(private val ssDao: SearchServiceDao) : BundleUsecase<Boolean> {
    companion object {
        val RS_DEL_ALL = "rs_delete_all"
    }
    override fun invoke(p1: Bundle): Observable<Boolean> {
        val isDeleteAll = p1.getBoolean(RS_DEL_ALL)
        val entity = p1.getSerializable(RecentsInsertUseCase.RS_ENTITY) as? RecentSearchEntity
        return Observable.fromCallable {
            if (!isDeleteAll && entity != null) ssDao.deleteEntity(entity) else ssDao.deleteAll()
            true
        }
    }
}