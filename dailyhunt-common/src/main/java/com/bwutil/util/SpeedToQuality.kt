/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.util

import com.bwutil.BitrateCalculations.findMatchingOperatorAndType
import com.bwutil.BwEstRepo
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.ConnectionInfoHelper
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.BwEstCfgDataProvider
import com.newshunt.dataentity.dhutil.model.entity.upgrade.AppConnectionQuality
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NetworkProviderQuality
import com.newshunt.dataentity.dhutil.model.entity.upgrade.adjust
import com.newshunt.sdk.network.connection.ConnectionType

/**
 * This class validates and coverts [map] to another data structure, to simplify querying.
 *
 * @author satosh.dhanyamraju
 */
class SpeedToQuality(map: HashMap<String?, String?>?,
                     private val logger: (String) -> Unit = { Logger.d("SpeedToQuality", it) },
                     private val networkProviderQuality: List<NetworkProviderQuality> =
                             BwEstCfgDataProvider.getStaticConfig()?.networkConfig?.networkProviderQuality
                                     ?: emptyList(),
                     private val operator: () -> String = { DeviceInfoHelper.getOperatorName(CommonUtils.getApplication()) },
                     private val connectionType : () -> String = { ConnectionInfoHelper.getConnectionType()}) {
    private val qualityToUpperLimit: List<Pair<String, Long>> = map?.mapNotNull { entry ->
        entry.key?.toLongOrNull()?.let { if (it > 0 && entry.value != null) entry.value!! to it else null }
    }?.sortedBy { it.second } ?: emptyList()

    fun qualityOf(bitrate: Long): String {
        if (bitrate < 0 || qualityToUpperLimit.size < 2) {
            logger("qualityOf($bitrate) Unknown. $qualityToUpperLimit")
            return BwEstRepo.CONNECTION_QUALITY_UNKNOWN
        }
        qualityToUpperLimit.forEach {
            if(bitrate <= it.second) return it.first.capped()
        }
        return qualityToUpperLimit.last().first.capped()
    }

    /* upper limit on quality against band. eg: 2G can't be 'veryfast' */
    fun String.capped(): String {
        val resultQ = when (connectionType()) {
            ConnectionType.TWO_G.connectionType, ConnectionType.THREE_G.connectionType, ConnectionType.FOUR_G.connectionType -> {
                networkProviderQuality.findMatchingOperatorAndType(operator(), connectionType())
                        ?.maxQuality.adjust() ?: this
            }
            else -> this
        }
        if(this == resultQ) return this
        val p = AppConnectionQuality.min(this, resultQ) ?: this
        logger("capped $this $p")
        return p
    }

    fun qualityOf(bitrate: Double): String = qualityOf(bitrate.toLong())

    fun ranges(): List<Triple<String, Long, Long>> {
        return qualityToUpperLimit.mapIndexed { index, pair ->
            val lower = if(index == 0) 0 else qualityToUpperLimit[index - 1].second.inc()
            val higher = if(index == qualityToUpperLimit.size -1) Long.MAX_VALUE else qualityToUpperLimit[index].second
            Triple(pair.first, lower, higher)
        }
    }
}