/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.entity

import java.io.Serializable

/**
 * Wraps all the metrics calculated by BwEstRepo
 *
 * @author satosh.dhanyamraju
 */
data class CQParams(
        val exoBitrate: Double,
        val fbBitrate: Double,
        val formulaId: String?,
        val formula: String?,
        val resultBitrate: Double,
        val resultBitrateQuality: String,
        val source: String,
        val lifetimeCQ: String? = null,
        val lifetimeCqDistribution: Map<String, Int>? = null,
        val connectionType: String
) : Serializable {
    private val serialVersionUID = 1L
}