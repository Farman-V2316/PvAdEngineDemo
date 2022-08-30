/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.model.apis

import com.newshunt.dataentity.model.entity.ImageResponseBody
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageAPI {

    @Multipart
    @POST("api/v1/upload")
    fun upload(@Part("rotation") rotation: RequestBody, @Part file: MultipartBody.Part):
        Observable<ImageResponseBody>

}