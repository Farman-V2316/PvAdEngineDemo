package com.newshunt.dataentity.common.model.entity.model

import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.DiscussionResponse
import com.newshunt.dataentity.common.asset.LikeAsset
import com.newshunt.dataentity.common.asset.PostEntity

/**
 * Created by karthik.r on 2019-09-26.
 */

data class LikesResponse(val counts: Counts2?,
                         val likes : LikesDetail?,
                         val discussions : DiscussionResponse?)


class LikesDetail(val count : Int,
                  val rows : List<LikeAsset>,
                  val guestUserCount: Int,
                  val total: Int,
                  val loggedInUserCount: Int,
                  val nextPageUrl : String?)

data class CommentParentResponse(val parent: PostEntity?,
                                 val data: PostEntity)