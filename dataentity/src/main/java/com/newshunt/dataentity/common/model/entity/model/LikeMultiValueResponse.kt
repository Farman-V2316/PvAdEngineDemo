package com.newshunt.dataentity.common.model.entity.model

import com.newshunt.dataentity.common.asset.LikeAsset

/**
 * Created by karthik.r on 15/06/20.
 */
data class LikeMultiValueResponse(val guestUserCount: Int?) : MultiValueResponse<LikeAsset>()