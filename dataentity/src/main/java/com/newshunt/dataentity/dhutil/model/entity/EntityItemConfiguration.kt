/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity

/**
 * @author anshul.jain
 */
data class EntityItemConfig(
        val count: Int,
        val timeLimit: Int /* value is in seconds*/) {
    companion object {
        val EMPTY = EntityItemConfig(0, 0)
    }
}

data class EntityConfiguration(
        val followNewspaper: EntityItemConfig?,
        val recentNewspaper: EntityItemConfig?,
        val followTopic: EntityItemConfig?,
        val followLocation: EntityItemConfig?,
        val dislike: EntityItemConfig?,
        val videos: EntityItemConfig?,
        val campaignRecoParams: EntityItemConfig?,
        val followChannel: EntityItemConfig?,
        val followShow: EntityItemConfig?,
        val followGroup: EntityItemConfig?
)

data class NonLinearConfigurations(val storyDetail:TimeConfiguration,
                                   val autoPlayVideo: TimeConfiguration,
                                   val dhTVVideo: TimeConfiguration)

data class TimeConfiguration(val request:Int = 15, val display:Int = 20)

data class PerspectiveThresholds(val autoExpandTsMin : Long = 5, val autoExpandTsMax : Long = 10)