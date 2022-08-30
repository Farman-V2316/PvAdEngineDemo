package com.newshunt.dataentity.news.model.entity.server.asset

import java.io.Serializable


/**
 * @author santhosh.kc
 */
enum class AnimationType {
    PAN_AND_ZOOM, NONE;
}

data class PlaceHolderAsset(val id: String,
                            val type: AssetType,
                            val notificationUniqueId: String,
                            val notificationId: String,
                            val timeStamp: Long?,
                            val experiments: Map<String, String>? = null) : Serializable