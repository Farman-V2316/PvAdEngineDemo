/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.socialfeatures.model.internal.service

import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.model.retrofit.RestAdapters
import com.newshunt.dataentity.common.follow.entity.FollowNamespace
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dhutil.helper.retrofit.CallbackWrapper
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.sdk.network.Priority
import com.newshunt.socialfeatures.model.internal.rest.VideoDownloadBeaconAPI
import okhttp3.OkHttpClient

/**
 * Created by umesh.isran on 12/3/2019.
 */
class VideoDownloadBeaconImpl(private val videoId: String?) {
    private val videoDownloadBeaconAPI = getVideoDownloadAdapter(Priority.PRIORITY_LOW, videoId)

    private val retrofitCallback: CallbackWrapper<ApiResponse<Any>>
        get() = object : CallbackWrapper<ApiResponse<Any>>() {
            override fun onSuccess(apiResponse: ApiResponse<Any>) {
                Logger.d("VideoDownload", "Success")
            }
            override fun onError(error: BaseError) {
                Logger.d("VideoDownload", "failure")
            }
        }

    fun hitDownloadBeacon() {
        if (!CommonUtils.isEmpty(videoId)) {
            videoDownloadBeaconAPI.hitVideoDownloadBeacon(
                    "VIDEO_$videoId", "DOWNLOAD", FollowNamespace.VIDEO.name)
                    .enqueue(retrofitCallback)
        }
    }

    private fun getVideoDownloadAdapter(priority: Priority, tag: Any?): VideoDownloadBeaconAPI {
        return RestAdapters.getBuilder(NewsBaseUrlContainer.getSecureSocialFeaturesUrl(),
                getClient(priority, tag).build()).build()
                .create(VideoDownloadBeaconAPI::class.java)
    }

    private fun getClient(priority: Priority, tag: Any?): OkHttpClient.Builder {
        val isGzipEnabled = PreferenceManager.getPreference(GenericAppStatePreference.ENABLE_GZIP_FOR_SOCIAL, false)
        return RestAdapterContainer.getInstance().getOkHttpClientBuilder(isGzipEnabled, priority, tag)
    }
}
