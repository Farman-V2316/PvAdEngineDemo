/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.searchhint

import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.searchhint.entity.HintServiceEntity
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
/**
 * @author madhuri.pa
 *
 */

interface HintServiceApi {
    @GET("/api/v2/upgrade/dynamic/version?entity=SEARCH_HINTS")
    fun getHint(@Query(Constants.URL_QUERY_APP_LANG) appLanguage: String,
                @Query("version") version: String):
            Observable<ApiResponse<HintServiceEntity>>
}