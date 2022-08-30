/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.model.service

import com.newshunt.appview.common.group.model.apis.ImageAPI
import com.newshunt.dataentity.model.entity.ImageResponseBody
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

interface ImageUploadService {
    fun uploadImage(requestBody: RequestBody, file: MultipartBody.Part):
        Observable<ImageResponseBody>
}

class ImageUploadServiceImpl @Inject constructor(private val imageAPI: ImageAPI) : ImageUploadService {

    override fun uploadImage(requestBody: RequestBody, file: MultipartBody.Part)
        : Observable<ImageResponseBody> {
        return imageAPI.upload(requestBody, file)
    }

}