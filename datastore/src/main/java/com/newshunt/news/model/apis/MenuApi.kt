/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.apis

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.social.entity.MenuDictionaryEntity1
import com.newshunt.dataentity.social.entity.MenuPayload
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * @author satosh.dhanyamraju
 */
interface MenuApi {

    @GET("api/v2/menu/feedback/dictionary")
    fun getMenuDictionary(@Query("appLanguage") appLanguage: String = UserPreferenceUtil.getUserNavigationLanguage(),
                          @Query("version") version: String? = null):
            Observable<ApiResponse<MenuDictionaryEntity1>>

    @POST
    fun postL1(@Url url: String, @Body payload : MenuPayload): Observable<ApiResponse<Any>?>
}