package com.newshunt.news.helper

/**
 * Created by karthik.r on 2019-11-12.
 */

/**
 * value POJO to store in [StoryPageViewerCache]
 */
data class StoryPageViewerCacheValue @JvmOverloads constructor(
        val itemId: String = "",
        val groupType: String? = null,
        val contentType: String? = null,
        val chunkwiseTs: String? = null,
        val engagementParams: String? = null,
        val totalTimespent: Long? = null,
        val timestamp: Long = System.currentTimeMillis()
)
