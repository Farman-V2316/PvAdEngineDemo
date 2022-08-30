/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity

import java.io.Serializable

/**
 * Records the reasons for why an evergreen ad was used as a fallback to any zone.
 * This data is added to the POST body of impression and adInflatedUrl.
 *
 * @author raunak.yadav
 */
data class EvergreenFallbackReason(val isRegularUser: Boolean, val regAdTimeout: Long,
                                   val isFirstAd: Boolean = false) : Serializable
