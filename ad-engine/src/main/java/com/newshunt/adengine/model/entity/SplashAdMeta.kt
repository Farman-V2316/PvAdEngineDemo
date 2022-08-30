/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity

import java.io.Serializable

/**
 * Meta required to verify validity of a splash ad.
 *
 * @author raunak.yadav
 */
data class SplashAdMeta(
    val adId: String,
    val campaignId: String, val bannerId: String,
    val startEpoch: Long? = null, val endEpoch: Long? = null,
    var showCount: Int, val span: Int,
    val isEmpty: Boolean = false
) : Serializable
