/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.download.config

import java.io.Serializable

/**
 * @Author umesh.isran
 *
 * Configuration Cache (item + video)
 */
data class CacheConfig(
        var cacheDirectoryMaxSizeInMb: Int = 200,
        var disableCache: Boolean = false,
        var prefetchConfigBuzzList: Map<String, PrefetchDownloadCountConfig?>? = null,
        var prefetchConfigNewsList: Map<String, PrefetchDownloadCountConfig?>? = null,
        var prefetchConfigVideoDetailV: Map<String, PrefetchDownloadCountConfig?>? = null,
        var downloadHighBitrateVariant: Boolean? = null,
        var disableForceHlsVariant: Boolean? = null
) : Serializable

data class PrefetchDownloadCountConfig(
        var noOfVideos: Int = 2,
        var prefetchDurationInSec: Float = 10F) : Serializable


