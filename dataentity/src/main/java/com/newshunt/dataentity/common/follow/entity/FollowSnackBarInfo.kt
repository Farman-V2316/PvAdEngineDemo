/**
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.follow.entity

import androidx.core.util.Pair
import com.newshunt.dataentity.news.model.entity.PageType
import java.io.Serializable

/**
 * @author santhosh.kc
 */
class FollowSnackBarInfo private constructor(val followSnackBarEntityParam:
                                             FollowSnackBarEntity?) {

    companion object {
        fun getInstance(entity : FollowSnackBarEntity?): FollowSnackBarInfo {
            return FollowSnackBarInfo(entity)
        }
    }

    private var followSnackBarEntity: FollowSnackBarEntity? = null

    init {
        followSnackBarEntity = followSnackBarEntityParam
    }

    fun update(followSnackBarEntity: FollowSnackBarEntity) {
        this.followSnackBarEntity = followSnackBarEntity
    }

    fun getSnackBarDeeplink(): String? {
        followSnackBarEntity ?: return null
        return followSnackBarEntity?.deepLink
    }

    fun getFollowTabInfo(): Pair<String, String>? {
        followSnackBarEntity?.let {
            it.id ?: return null
            it.type ?: return null
            val pageType = PageType.fromName(it.type)
            if (pageType == PageType.INVALID)
                return null
            return Pair(it.id, it.type)
        }
        return null
    }
}

data class FollowSnackBarEntity(val id: String? = null, val type: String?, val deepLink:
String?) : Serializable