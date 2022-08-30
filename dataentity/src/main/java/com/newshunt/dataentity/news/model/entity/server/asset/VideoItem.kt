/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.entity.server.asset

/**
 * @author shrikant.agrawal
 * Pojo for recently played video item
 */
data class VideoItem(val groupId: String?,
                     val itemId: String,
                     val sessionId: String?,
                     val uiType: String,
                     val durationInSec: Long,
                     val ts : Long) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false

        if (!(other is VideoItem)) return false
        if (other.itemId == itemId) return true
        return false
    }

    override fun hashCode(): Int {
        return itemId.hashCode()
    }
}


