/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.net.Uri
import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.social.entity.FeedPage
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * reads bundle and check whether searchPayload exist or not if yes than create
 * searchPayload.
 * @author amit.chaudhary
 */
class BuildSearchPayloadUsecase @Inject constructor(@Named("searchQuery")
                                                    private val searchQuery: SearchQuery?) :
        BundleUsecase<Any> {

    override fun invoke(p1: Bundle): Observable<Any> {

        val feedPage = (p1.getSerializable(BuildPayloadUsecase.B_FEEDPAGE_ENTITY) as? FeedPage)
        val queryStr = feedPage?.contentUrl?.let {
            Uri.parse(it)?.getQueryParameter(Constants.SEARCH_QUERY_PARAM_KEY)
        } ?: Constants.EMPTY_STRING

        return Observable.fromCallable {
            searchQuery?.copy(
                    suggestion = queryStr,
                    searchPayloadContext = searchQuery.searchPayloadContext?.copy(
                            section = feedPage?.section,
                            entityType = feedPage?.entityType,
                            entityId = feedPage?.id
                    ))?.toSearchPayload()
        }
    }

    companion object {
        private const val LOG_TAG = "BuildSearchPayloadUsecase"
    }
}