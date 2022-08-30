/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 * */
package com.newshunt.videos.model.entity

/**
 * @author shrikant.agrawal
 */
data class VideoListResponse(val rows: MutableList<Any>?,
                        val nextPageUrl : String?,
                        val config : VideoListConfig?)

data class VideoListConfig(val minFirstPositionVideo: Int?,
                           val ttl : Int?,
                           val maxVideosCacheSize : Map<String, Int>)