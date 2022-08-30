/*
 * Created by Rahul Ravindran at 26/9/19 7:12 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.search.model.service

import android.os.Bundle
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SearchSuggestionType
import com.newshunt.dataentity.search.SuggestionPayload
import com.newshunt.dataentity.search.SuggestionResponse
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.search.model.rest.TrendingApi
import io.reactivex.Observable
import javax.inject.Inject

/**
 * - can be dagger contained singleton
 * @author satosh.dhanymaraju
 */

val NEWS_TRENDING_URL: String = "api/v2/search/posts/trending/"
val CREATE_POST_TRENDING_URL = "api/v2/posts/search/trending/%type%"
val TRENDING_QUERY = "trending_query"

class TrendingUnifiedUseCase @Inject constructor(private val apiVersionHelper: VersionedApiHelper<ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>>,
                                                 private val trendingApi: TrendingApi,
                                                 private val requestType: SearchRequestType,
                                                 private val payload: SuggestionPayload?) :
        BundleUsecase<SuggestionResponse<List<SearchSuggestionItem>>> {


    companion object {
        val entity = VersionedApiEntity(VersionEntity.SEARCH_TRENDING)

    }

    init {
        entity.languageCode = UserPreferenceUtil.getUserLanguages()

    }

    override fun invoke(p1: Bundle): Observable<SuggestionResponse<List<SearchSuggestionItem>>> {
        val type = object : TypeToken<ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>>() {}.type
        val db = apiVersionHelper.fromCacheKt(entityType = entity.entityType,
                classOfT = type)
                .map { transform(it) }
                .onErrorResumeNext { t: Throwable -> Observable.empty() }

        val api = Observable.fromCallable {
            VersionedApiHelper.getLocalVersion(entityType = entity.entityType)
                    ?: Constants.EMPTY_STRING
        }.flatMap { version: String ->
            trendingApi.trending(
                    url = NEWS_TRENDING_URL,
                    appLanguage = UserPreferenceUtil.getUserNavigationLanguage(),
                    langCode = UserPreferenceUtil.getUserLanguages(),
                    version = version,
                    request = payload)
                    .map { transform(it) }
        }.onErrorResumeNext { t: Throwable -> Observable.empty()}
        return Observable.mergeDelayError(db, api)
    }

    private fun transform(response: ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>?): SuggestionResponse<List<SearchSuggestionItem>> {
        val trending = if (response?.data != null) response.data else SuggestionResponse(rows = emptyList())
        trending?.rows?.forEach {
            it.suggestionType = SearchSuggestionType.TRENDING
            if (it.typeName == SearchSuggestionType.HANDLE.name) it.typeName = SearchSuggestionType.HANDLE_UNIFIED.name
            else if (it.typeName == SearchSuggestionType.HASHTAG.name) it.typeName = SearchSuggestionType.HASHTAG_UNIFIED.name
        }
        return trending
    }
}


class TrendingHashtagUseCase(private val trendingApi: TrendingApi,
                             private val payload: SuggestionPayload? = null) :
        BundleUsecase<SuggestionResponse<List<SearchSuggestionItem>>> {

    override fun invoke(p1: Bundle): Observable<SuggestionResponse<List<SearchSuggestionItem>>> {
        val query = p1.getString(TRENDING_QUERY, "")
        if (query.isEmpty()) return Observable.just(SuggestionResponse(rows = emptyList()))
        return trendingApi.trending(
                url = CREATE_POST_TRENDING_URL.replace("%type%", getSuggestionType(query)),
                appLanguage = UserPreferenceUtil.getUserNavigationLanguage(),
                request = payload,
                langCode = UserPreferenceUtil.getUserLanguages())
                .map { transform(it) }
                .onErrorReturn { _: Throwable -> SuggestionResponse(rows = emptyList()) }
    }

    private fun transform(response: ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>?): SuggestionResponse<List<SearchSuggestionItem>> {
        val trending = if (response?.data != null) response.data else SuggestionResponse(rows = emptyList())
        return trending
    }

}

class TrendingHandleUseCase(private val trendingApi: TrendingApi,
                            private val payload: SuggestionPayload? = null) :
        BundleUsecase<SuggestionResponse<List<SearchSuggestionItem>>> {

    override fun invoke(p1: Bundle): Observable<SuggestionResponse<List<SearchSuggestionItem>>> {
        val query = p1.getString(TRENDING_QUERY, "")
        if (query.isEmpty()) return Observable.just(SuggestionResponse(rows = emptyList()))
        return trendingApi.trending(
                url = CREATE_POST_TRENDING_URL.replace("%type%", getSuggestionType(query)),
                appLanguage = UserPreferenceUtil.getUserNavigationLanguage(),
                request = payload,
                langCode = UserPreferenceUtil.getUserLanguages())
                .map { transform(it) }
                .onErrorReturn { SuggestionResponse(rows = emptyList()) }
    }

    private fun transform(response: ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>?): SuggestionResponse<List<SearchSuggestionItem>> {
        val trending = if (response?.data != null) response.data else SuggestionResponse(rows = emptyList())
        return trending
    }
}

private fun getSuggestionType(query: String): String {
    return when {
        query.startsWith("#") -> return "hashtag"
        query.startsWith("@") -> return "handle"
        else -> "hashtag"
    }
}