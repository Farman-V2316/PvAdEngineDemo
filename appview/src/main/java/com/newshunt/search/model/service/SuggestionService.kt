/*
 * Created by Rahul Ravindran at 26/9/19 7:12 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.search.model.service

import android.os.Bundle
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SearchSuggestionType
import com.newshunt.dataentity.search.SugResponse
import com.newshunt.dataentity.search.SuggestionPayload
import com.newshunt.dataentity.search.SuggestionResponse
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.search.model.rest.SuggestionApi
import io.reactivex.Observable
import javax.inject.Inject

/**
 * @author satosh.dhanymaraju
 */

const val AUTOCOMPLETE_URL = "api/v2/search/posts/autocomplete"
const val LOCATION_AUTOCOMPLETE_URL = "api/v2/search/local/autocomplete"

class SuggestionUseCase @Inject constructor(private val requestType: SearchRequestType,
                                            private val api: SuggestionApi) : BundleUsecase<SuggestionResponse<List<SearchSuggestionItem>>> {
    companion object {
        val SS_PAYLOAD = "ss_payload"
    }

    private val transform: (SugResponse) -> SugResponse = {
        if(requestType != SearchRequestType.CREATE_POST) {
            it.rows?.map {
                if(it.typeName == SearchSuggestionType.HANDLE.name) it.typeName = SearchSuggestionType.HANDLE_UNIFIED.name
                else if(it.typeName == SearchSuggestionType.HASHTAG.name) it.typeName = SearchSuggestionType.HASHTAG_UNIFIED.name
            }
            it
        } else it
    }

    override fun invoke(p1: Bundle): Observable<SuggestionResponse<List<SearchSuggestionItem>>> {
        var payload = p1.getSerializable(SS_PAYLOAD) as? SuggestionPayload

        payload ?: kotlin.run {
            return Observable.never()
        }

        return api.suggestions(
                url = if(requestType == SearchRequestType.LOCATION) LOCATION_AUTOCOMPLETE_URL else AUTOCOMPLETE_URL,
                payload = payload,
                appLanguage = UserPreferenceUtil.getUserNavigationLanguage(),
                langCode = UserPreferenceUtil.getUserLanguages())
                .onErrorReturn { ApiResponse()}
                .map {
                    if (it.data != null) it.data.copy(maxRecentSize = recentsSie())
                    else SuggestionResponse(
                            /*no need of recents in locations search*/
                            maxRecentSize = recentsSie(),
                            rows = emptyList())
                }
                .map { transform(it) }
    }

    fun recentsSie() = if (requestType == SearchRequestType.LOCATION) 0 else 3
}
