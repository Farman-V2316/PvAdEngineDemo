package com.newshunt.dataentity.common

import java.io.Serializable

/**
 * Request/Reponse param pojos for interacting with JS callbacks
 */
data class JsResponse(val id: String, val value: Boolean)

data class JsResponseLikesDislikes(
        val id : String,
        val dislike : Boolean,//true if dislike else false
        val like : String //The possible value of likeType; empty, if not liked
)
data class JsFollowAndDislikesRequest(val follows: List<String>? = null,
                                      val posts: List<String>? = null)

data class JsFollowAndDislikesResponse(val follows: List<JsResponse>? = null,
                                       val posts: List<JsResponseLikesDislikes>? = null)

data class JsOpenFeedRequest(val swipeableStories: List<JsSwipeableStories>,
                             val assetClicked: String,
                             val nextPageUrl: String? = null,
                             val nextPageContentRequestMethod: String? = null,
                             val intent: String? = null)

data class JsSwipeableStories(val id: String, val isVideo: Boolean, val experiments: Map<String, String>)

data class JsFollowStatus(val status: Int=0, val data: Boolean= true)

data class JsUpdateLikeRequest(
        val entityId : String,
        val entityType : String,
        val action : String, //liketype
        val actionToggle : Boolean
)

data class JsPostActionParam(val id: String = "", val action: String = "")

data class SnackMeta(val message:String?,
                      val ctaText:String?= null,
                      val ctaUrl:String?= null,
                      val duration:Int):Serializable

data class JsPhoneNumber(val phNumber: String = "")