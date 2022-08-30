/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.follow.entity

import java.io.Serializable

/**
 * @author aman.roy
 * Config related classes for follow and block suggestion for implicit and explicit signals.
 */
data class Follow(
    val minTimeGapInSeconds: Int? = 0,
    val minSessionGap: Int? = 0,
    val cardPosition: Int? = 0,
    val initialSessionToSkip: Int? = 0,
    val numberOfShares: Int? = 0,
    val maxFollowCapCS: Int? = 0,
    val numberOfPageViews: Int? = 0) : FollowBlockCommon() {
}

data class Block(val numberOfDislikes:Int? = 0) : FollowBlockCommon()

data class ColdStart(val follow: Follow? = null) : Serializable

open class FollowBlockCommon(
    val maxPerSession:Int? = 0,
    val maxLifeTimeCap:Int? = 0,
    val coolOffAbsoluteDays:Int? = 0,
    val coolOffActiveDays:Int? = 0,
    val coolOffPeriodInSecs:Int? = 0,
    val minCardPosition:Int? = 0) : Serializable

data class ImplicitSignal(
    val follow: Follow? = null,
    val block: Block? = null,
    val bottomDrawerDuration:Int? = 0) : Serializable

data class ExplicitSignal(
    val follow: Follow? = null,
    val block: Block? = null) : Serializable

data class FollowBlockLangConfig(
    val disableFollowBlockRecommendationAPICalls: Boolean? = false,
    val langFilter: List<String>? = null,
    val coldStart: ColdStart? = null,
    val implicitSignal: ImplicitSignal? = null,
    val explicitSignal: ExplicitSignal? = null) : Serializable


data class FollowBlockConfigWrapper(val followBlockConfig:List<FollowBlockLangConfig>? = null):Serializable