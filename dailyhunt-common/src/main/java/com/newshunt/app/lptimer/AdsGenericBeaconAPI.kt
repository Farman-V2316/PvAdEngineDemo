/*
* Copyright (c) 2021 Newshunt. All rights reserved.
*/
package com.newshunt.app.lptimer

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * @author Mukesh Yadav
 */
interface AdsGenericBeaconAPI {
    @POST
    @FormUrlEncoded
    fun updateGenericAdItemEvent(@Url requestUrl: String, @FieldMap params: Map<String, String>): Observable<ResponseBody>
}