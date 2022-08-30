/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.follow.entity

import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.news.analytics.FollowReferrer
import com.newshunt.dataentity.news.model.entity.server.asset.GroupType
import java.io.Serializable

/**
 * @author anshul.jain
 * A class which contains  the entities which can be followed.
 */

enum class FollowEntityType : Serializable {
    SOURCE, TOPIC, LOCATION, SHOW, CHANNEL, GROUP, USER;

    companion object {

        @JvmStatic
        fun from(value: String): FollowEntityType? {

            FollowEntityType.values().forEach { type ->
                when {
                    CommonUtils.equalsIgnoreCase(value, type.name) -> return type
                }
            }
            return null
        }

    }

}

enum class FollowEntitySubType {
    CREATOR, USER
}

enum class FollowNamespace {
    NEWS, VIDEO, MEME, DHTV
}

enum class FollowNavigationType(private val value: String,
                                private val referrer: FollowReferrer,
                                private val deeplinkValue: String,
                                private val uriValue: String) {
    SOURCE("source", FollowReferrer.FE_SOURCE, "sources", "source"),
    TOPIC("topic", FollowReferrer.FE_TOPIC, "topics", "topic"),
    LOCATION("location", FollowReferrer.FE_LOCATION, "locations", "location"),
    CREATOR("creator", FollowReferrer.FE_LOCATION, "creators", "creator");

    fun getValue(): String {
        return value
    }

    fun getReferrer(): FollowReferrer {
        return referrer
    }

    companion object {
        @JvmStatic
        fun from(value: String?): FollowNavigationType? {
            value ?: return null

            FollowNavigationType.values().forEach { type ->
                when {
                    CommonUtils.equalsIgnoreCase(value, type.value) -> return type
                }
            }
            return null
        }

        @JvmStatic
        fun fromDeeplinkValue(deeplinkValue: String?): FollowNavigationType {
            deeplinkValue ?: return SOURCE
            FollowNavigationType.values().forEach { type ->
                when {
                    CommonUtils.equalsIgnoreCase(deeplinkValue, type.deeplinkValue) -> return type
                }
            }
            return SOURCE
        }
    }
}

