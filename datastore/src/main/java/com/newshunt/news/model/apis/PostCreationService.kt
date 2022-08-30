package com.newshunt.news.model.apis

import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.ImageUpload
import com.newshunt.dataentity.common.asset.PostCreation
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PostCreationService {
    @Headers("Content-Type: application/json")
    @POST("post/create/{type}/{id}")
    fun createPost(
        @Body post: PostCreation,
        @Path("type") type: String,
        @Path("id") id: String): Observable<Response<ApiResponse<PostEntity>>>
}

interface PostDeletionService {
    /*TODO : need to rename this function as its getting used for different type of format
       other than comment*/
    @POST("post/delete/{type}/{id}")
    fun deleteComment(
        @Path("id") id: String,
        @Path("type") type: String,
        @Header(Constants.HEADER_SESSION_DATA) header: String): Observable<ResponseBody>
}

interface PostReportService {
    @POST("post/report/{type}/{id}")
    fun reportComments(
        @Path("id") id: String,
        @Path("type") type: String,
        @Header(Constants.HEADER_SESSION_DATA) header: String): Observable<ResponseBody>
}

interface ImageUploadService {
    @Multipart
    @POST("api/v1/upload")
    fun uploadImages(
        @Part("rotation") rotation: RequestBody,
        @Part files: List<MultipartBody.Part>): Single<ImageUpload>

}