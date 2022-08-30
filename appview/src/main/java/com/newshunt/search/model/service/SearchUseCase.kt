/*
 * Created by Rahul Ravindran at 26/9/19 7:12 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.search.model.service

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.asset.LocationIdParent
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.model.entity.APIException
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.search.AggrMultivalueResponse
import com.newshunt.dataentity.search.SearchRequest
import com.newshunt.dataentity.search.SearchUiEntity
import com.newshunt.dataentity.search.SearchUiResponse
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dhutil.helper.APIUtils
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.LocationEntityDao
import com.newshunt.news.model.daos.SearchFeedDao
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.utils.FilteroutUnknownCards
import com.newshunt.search.model.rest.SearchApi
import io.reactivex.Observable
import javax.inject.Inject

/**
 * @author satosh.dhanymaraju
 */
class SearchUseCase @Inject constructor(private val api: SearchApi,
                                        private val requestType: SearchRequestType,
                                        private val searchFeedDao: SearchFeedDao,
                                        private val fetchDao: FetchDao,
                                        private val followDao: FollowEntityDao,
                                        private val locationEntityDao: LocationEntityDao,
                                        private val filteroutUnknownCards: FilteroutUnknownCards) : BundleUsecase<SearchUiResponse> {
    companion object {
        private val LOG_TAG = SearchUseCase::class.java.simpleName
        const val SEARCH_REQUEST = "search_request"
    }
    override fun invoke(p1: Bundle): Observable<SearchUiResponse> {
        val searchReq = p1.getSerializable(SEARCH_REQUEST) as SearchRequest
        val url = pathurl(requestType)
        return api.search(url,
                searchReq.toSearchPayload(requestType),
                searchReq.suggestion,
                UserPreferenceUtil.getUserNavigationLanguage(),
                UserPreferenceUtil.getUserLanguages()
        ).lift(ApiResponseOperator())
                .map { resp ->
                    val aggrResp = resp.data ?: run {
                        return@map pushSearchError(Exception("empty data received"), searchReq)
                    }
                    //store(resp)
                    val firstpage = aggrResp.aggrs?.values?.let { pages ->
                        searchFeedDao.replacePages(pages)
                        pages.firstOrNull()
                    }
                    firstpage?.let {
                        val fe = FetchInfoEntity(it.id, Constants.FETCH_LOCATION_LIST, section = it.section)
                        val cards = filteroutUnknownCards.transf(aggrResp.rows)
                        // TODO(satosh.dhanyamraju): isInstance
                        fetchDao.replaceFirstPage(fe, cards.filterIsInstance<PostEntity>(), followDao, it.contentUrl)
                    }
                    val resp = transform(aggrResp)
                    SearchUiEntity(searchReq.suggestion, resp)
                }
    }

    /**
     * For location search result, only ids are sent, we need to lookup from DB and populate the details
     */
    private fun transform(aggrMultivalueResponse: AggrMultivalueResponse) : AggrMultivalueResponse {
        return if (requestType == SearchRequestType.LOCATION) {
            val l  = aggrMultivalueResponse.rows?.let {
                ArrayList(it.filterIsInstance<LocationIdParent>())
            } ?: return  aggrMultivalueResponse
            val l1: List<AnyCard> = locationEntityDao.readFromIds(l)
            aggrMultivalueResponse.rows = l1
            aggrMultivalueResponse
        } else {
            aggrMultivalueResponse
        }
    }

    private fun pushSearchError(cause: Throwable, request: SearchRequest): SearchUiResponse {
        Logger.e(LOG_TAG, "got exception: ${cause.message}", cause)
        val baseError = when (cause) {
            is APIException -> cause.error
            is BaseError -> cause
            else -> APIUtils.getError(cause)
        }
        val aggrResponse = AggrMultivalueResponse(error = baseError)
        return SearchUiResponse(request.suggestion, aggrResponse)
    }

    private fun pathurl(requestType: SearchRequestType): String = when (requestType) {
        SearchRequestType.NEWS -> NEWS_URL
        SearchRequestType.CREATE_POST -> HASTAG_URL
        SearchRequestType.GROUP -> NEWS_URL
        SearchRequestType.LOCATION -> LOCATIONS_SEARCH_URL
    }

}


private const val NEWS_URL = "api/v2/search/posts/query"
private const val HASTAG_URL = "dailyhunt/v1/internal/query"
private const val LOCATIONS_SEARCH_URL = "/api/v2/search/location/query"