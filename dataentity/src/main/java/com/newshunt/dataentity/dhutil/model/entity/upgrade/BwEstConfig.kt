/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.upgrade

import com.google.gson.annotations.SerializedName

/**
 * Entity classs and enums used in bandwidth estimation
 *  @author satosh.dhanyamraju
 */
data class NetworkProviderQuality(
        val network: String? = null,
        val provider: String? = null,
        val quality: String? = null,
        val maxQuality: String? = null
)

fun String?.adjust(): String? {
        return if(this == "avg") "average" else this
}

val PRELOAD_SPEED_QUALITY_MAP : LinkedHashMap<String?, String?>?  = linkedMapOf(
        "200" to "slow",
        "1000" to "average",
        "2500" to "good",
        "5000" to "fast",
        "10000" to "veryfast"
)

/**
 * Used in bitrate calculations
 */
enum class AppConnectionQuality(val level: Int) {
        slow(0), average(1), good(2), fast(3), veryfast(4);

        companion object {
                /**
                 * returns smaller one comparing [level], if it can't find, returns null
                 */
                fun min(l: String?, r: String?): String? {
                        if (l == null || r == null) return null
                        val lV = values().find { it.name == l }
                        val rV = values().find { it.name == r }
                        return if (lV != null && rV != null) {
                                if (lV < rV) lV.name else rV.name
                        } else {
                                null
                        }
                }
        }
}

data class BwEstConfig(
        @SerializedName("network_config") val networkConfig: NetworkConfig?,
        var speedQualityMapV2: java.util.LinkedHashMap<String?, String?>? = PRELOAD_SPEED_QUALITY_MAP
        )