/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Url

/**
 * @author raunak.yadav
 */
interface AdErrorAPI {

    @Multipart
    @POST
    fun hitErrorBeacon(@Url url: String?, @PartMap adErrorRequestBody: HashMap<String, RequestBody?>, @Part screenshot: MultipartBody.Part?): Call<ResponseBody>

}