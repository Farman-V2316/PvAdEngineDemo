package com.newshunt.dataentity.common.asset

import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.DetailCard
import com.newshunt.dataentity.social.entity.PhotoChild

data class Chunk2Pojo(
	val data: PostEntity? = null, // may/may-not contain loader
	val tsData: Long? = null,
	val error: Throwable? = null, // should be BaseError?
	val tsError: Long? = null
)

data class SuggestedFollowsPojo(val data: List<PostSuggestedFollow>? = null, // may/may-not contain loader
                                val tsData: Long? = null,
                                val error: Throwable? = null, // should be BaseError?
                                val tsError: Long? = null)

data class LikeListPojo(val data: List<LikeAsset>? = null, // may/may-not contain loader
                        val count: Int? = null, // may/may-not contain loader
                        val guestUserCount: Int? = null, // may/may-not contain loader
                        val loggedInUserCount: Int? = null, // may/may-not contain loader
						val total: Int? = null,
                        val tsData: Long? = null,
                        val discussions: List<CommonAsset>? = null,
                        var discussionNextPageUrl: String? = null,
                        var likesNextPageUrl: String? = null,
                        val error: Throwable? = null, // should be BaseError?
                        val tsError: Long? = null)

data class MoreStoriesPojo(val data: ArrayList<CommonAsset>? = ArrayList(),
                           val tsData: Long? = null,
                           val error: Throwable? = null, // should be BaseError?
                           val tsError: Long? = null,
                           var nextPageUrl: String? = null
)

data class PhotoChildPojo(val data: List<PhotoChild>? = null,
                          val tsData: Long? = null,
                          val error: Throwable? = null, // should be BaseError?
                          var index: Int? = -1,
                          val tsError: Long? = null)

data class DetailCardPojo(val data: DetailCard? = null,
						  val tsData: Long? = null,
						  val error: Throwable? = null, // should be BaseError?
						  var index: Int? = -1,
						  val tsError: Long? = null)

data class DiscussionPojo(val data: List<AllLevelCards>? = null,
						  val tsData: Long? = null,
						  val error: Throwable? = null, // should be BaseError?
						  var index: Int? = -1,
						  val tsError: Long? = null)