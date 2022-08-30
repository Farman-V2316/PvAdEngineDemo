/*
 * Created by Rahul Ravindran at 26/9/19 7:12 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.search.model.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.search.AggrMultivalueResponse
import com.newshunt.dataentity.search.SearchPayload
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SuggestionPayload
import com.newshunt.dataentity.search.SuggestionResponse
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Retrofit API's for pre-search and search
 *
 * @author satosh.dhanymaraju
 */
interface SuggestionApi {
    @POST
    fun suggestions(@Url url: String,
                    @Body payload: SuggestionPayload = SuggestionPayload(),
                    @Query("appLanguage") appLanguage: String? = null,
                    @Query("langCode") langCode: String? = null,
                    @Query("query") query: String? = null,
                    @Query("type") type: String? = null): Observable<ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>>
}

interface SearchApi {
    @POST
    fun search(@Url url: String,
               @Body() request: SearchPayload,
               @Query("query") query: String,
               @Query("appLanguage") appLanguage: String,
               @Query("langCode") langCode: String):
            Observable<Response<ApiResponse<AggrMultivalueResponse?>?>>
}

interface TrendingApi {
    @POST
    fun trending(@Url url: String,
                 @Query("appLanguage") appLanguage: String,
                 @Query("langCode") langCode: String,
                 @Body() request: SuggestionPayload?,
                 @Query("version") version: String = ""): Observable<ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>>
}

