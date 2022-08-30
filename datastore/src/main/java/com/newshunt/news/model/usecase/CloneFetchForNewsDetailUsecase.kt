/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.news.model.daos.FetchDao
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * Call ofter card click, before opening news-detail
 * @author satosh.dhanyamraju
 */
class CloneFetchForNewsDetailUsecase @Inject constructor(@Named("section") private val section: String,
                                                         private val fetchDao: FetchDao,
                                                         @Named("location") private val location: String) : BundleUsecase<String> {
    override fun invoke(p1: Bundle): Observable<String> {
        val entityId = p1.getString(B_ENTITY_ID, null)
        return if (entityId == null) Observable.just(Constants.EMPTY_STRING)
        else Observable.fromCallable {
            val keepIds = p1.getStringArrayList(Constants.BUNDLE_KEEP_POST_IDS)?: emptyList<String>()
            return@fromCallable fetchDao.cloneFetchForNewsDetail(entityId, location, section, keepIds, CLONE_SUFFIX)
        }
    }

    companion object {
        const val B_ENTITY_ID = "B_FEEDPAGE_ENTITY"
        const val CLONE_SUFFIX = "_detail"
        fun cloneLocationForDetail(location: String) = "${location}_detail"
    }
}