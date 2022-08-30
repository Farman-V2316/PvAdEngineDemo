/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil

import com.bwutil.entity.CQParams
import com.bwutil.util.SpeedToQuality
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.ConnectionInfoHelper
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.upgrade.AppConnectionQuality
import com.newshunt.dataentity.dhutil.model.entity.upgrade.BitrateExpression
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NetworkProviderQuality
import com.newshunt.dataentity.dhutil.model.entity.upgrade.PRELOAD_SPEED_QUALITY_MAP
import com.newshunt.dataentity.dhutil.model.entity.upgrade.adjust
import com.newshunt.dhutil.helper.BwEstCfgDataProvider
import com.newshunt.sdk.network.connection.ConnectionManager
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * This class handles all bitrate calculations for debugging and instrumentation
 *
 * @author satosh.dhanyamraju
 */
object BitrateCalculations {
    private const val TAG: String = "BitrateCalculations"
    private const val SOURCE_TYPE_MEASURED = "measured"
    private const val SOURCE_TYPE_LIFETIME = "lifetime"
    private const val SOURCE_TYPE_NETWORK_MAP = "network-type-map"

    fun currentCQParams(exoBitrate: Long,
                        bitrateToSpeedConv: (Long) -> String,
                        firstMeasurementReceivedAt: Long,
                        lifetimeCQ: Long,
                        nsdkBitrate: Double = ConnectionManager.getInstance().bandwidth,
                        qualityMap: List<NetworkProviderQuality>? = BwEstCfgDataProvider.networkProviderQuality,
                        operator: () -> String = { DeviceInfoHelper.getOperatorName(CommonUtils.getApplication()) },
                        connectionType: () -> String = { ConnectionInfoHelper.getConnectionType() },
                        transitionThreshold: Long = BwEstCfgDataProvider.cardTransitionThresholdSec * 1000,
                        formula: (Double, Double) -> Pair<Double, BitrateExpression?> = { a, b ->
                            getEstimatedBitrateInKbps(a, b)
                        },
                        lifetimeCqDistribution: Map<String, Int>? = null
    ): CQParams {
        val (bitrateForCalc, source) =
                chooseBitrateForCalculation(firstMeasurementReceivedAt, exoBitrate, lifetimeCQ, transitionThreshold)
        val cqParams = if (bitrateForCalc == -1L) {
            val quality = qualityMap.findMatchingOperatorAndType(operator(), connectionType())
                    ?.let {
                        if (it.maxQuality.adjust() != null)
                            AppConnectionQuality.min(it.quality.adjust(), it.maxQuality.adjust())
                        else it.quality.adjust()
                    }
                    ?: BwEstRepo.CONNECTION_QUALITY_UNKNOWN
            CQParams(
                    -1.0,
                    nsdkBitrate,
                    null,
                    null,
                    -1.0,
                    quality,
                    SOURCE_TYPE_NETWORK_MAP,
                    bitrateToSpeedConv(lifetimeCQ),
                    lifetimeCqDistribution,
                    connectionType())

        } else {
            val bitrateForCalcDouble = bitrateForCalc.toDouble()
            val (resultBitrate, bitrateExpression) = formula(bitrateForCalcDouble, nsdkBitrate)
            CQParams(
                    bitrateForCalcDouble,
                    nsdkBitrate,
                    bitrateExpression?.id,
                    bitrateExpression?.formula,
                    resultBitrate,
                    bitrateToSpeedConv(resultBitrate.toLong()),
                    source,
                    bitrateToSpeedConv(lifetimeCQ),
                    lifetimeCqDistribution,
                    connectionType())
        }
        Logger.d(TAG, "currentCQParams: $cqParams")
        return cqParams
    }

    private fun chooseBitrateForCalculation(
            firstMeasurementReceivedAt: Long,
            exoBitrate: Long,
            lifetimeCQ: Long,
            transitionThreshold: Long
    ): Pair<Long, String> {
        return when {
            // 1. if we have exo estimates for transitionThreshold duration, use it
            firstMeasurementReceivedAt != -1L && exoBitrate > 0 &&
                    (System.currentTimeMillis() - firstMeasurementReceivedAt > transitionThreshold) ->
                exoBitrate to SOURCE_TYPE_MEASURED
            // 2. if lifeTime estimates available use it
            lifetimeCQ != -1L -> lifetimeCQ to SOURCE_TYPE_LIFETIME
            // 3. -1L will cause it to pick a value from static map
            else -> -1L to ""
        }
    }

    fun List<NetworkProviderQuality>?.findMatchingOperatorAndType(
            operator: String,
            connectionType: String
    ): NetworkProviderQuality? {
        val found = this?.find {
            (it.network == connectionType || it.network == "Any")  /*exact match or match 'Any'*/
                    && (it.provider == operator || it.provider == "Any")
        }
        Logger.v(TAG, "findMatchingOperatorAndType() called with: operator = \'$operator\', connectionType = \'$connectionType\', returned=$found")
        return found
    }
    private val engine: ScriptEngine by lazy {  ScriptEngineManager().getEngineByName("rhino")  }

    @Synchronized
    fun getEstimatedBitrateInKbpsForAnalytics(exoBitratePerSec: Double, nsdkBitrate: Double): Pair<Double, BitrateExpression?> {

        //Start of bitrate expression calculation
        var resultBitrate : Double
        var expression = BwEstCfgDataProvider.bitrateExpression
        try {
            var expression = BwEstCfgDataProvider.bitrateExpression

            if(exoBitratePerSec > 0 && nsdkBitrate > 0 && nsdkBitrate > exoBitratePerSec) {
                //In very slow network(10 Kbps), nsdk is giving speed range [1000 -2000kbps]
                expression = BwEstCfgDataProvider.bitrateExpressionException
            }

            var equation = expression?.formula
            Logger.d(TAG, "formula selected to calculate speed: " + equation)
            Logger.d(TAG, "exo speed: " + exoBitratePerSec)
            Logger.d(TAG, "FB speed : $nsdkBitrate")
            equation = equation?.replace("e", "" + exoBitratePerSec)
            equation = equation?.replace("n", "" + nsdkBitrate)
            resultBitrate = engine.eval(equation) as Double
            Logger.d(TAG, "resultBitrate : " + resultBitrate)
            return resultBitrate to expression
        } catch(e: Exception) {
            Logger.e(TAG, "Exception case ", e)
            Logger.caughtException(e)
        }
        //End of bitrate expression calculation

        //Fallback in case exception in the above code
        //Formula exoBitrate * exoWeighatge + networkBitrate * networkWeightage
        var exoBitrate = exoBitratePerSec * BwEstCfgDataProvider.exoWeightage
        var nwBitrate = nsdkBitrate * BwEstCfgDataProvider.networkWeightage
        resultBitrate = exoBitrate + nwBitrate
            Logger.d(TAG, "exoBitrate : $exoBitrate, nwBitrate : $nwBitrate & resultBitrate : $resultBitrate")
        val fallbackBitrateExp = BitrateExpression("", "e*${BwEstCfgDataProvider.exoWeightage}+n*${BwEstCfgDataProvider.networkWeightage}")
        Logger.d(TAG, " fall back logic calculation :: exoBitrate : $exoBitrate, nwBitrate : $nwBitrate & resultBitrate : $resultBitrate, exp=$fallbackBitrateExp")
        return resultBitrate to fallbackBitrateExp
    }

    @Synchronized // called from multiple threads
    fun getEstimatedBitrateInKbps(exoBitratePerSec: Double, nsdkBitrate: Double, expr: BitrateExpression? = BwEstCfgDataProvider.bitrateExpressionV2): Pair<Double, BitrateExpression?> {
        val nonNullExpr = expr?:return getEstimatedBitrateInKbpsForAnalytics(exoBitratePerSec, nsdkBitrate)
        try {
            val script = "${nonNullExpr.formula}; f42($exoBitratePerSec, $nsdkBitrate)"
            val result = engine.eval(script) as Double
            Logger.d(TAG, "getEstimatedBitrateInKbps: $exoBitratePerSec, $nsdkBitrate, $result, $nonNullExpr")
            return result to nonNullExpr
        } catch (e: Exception) {
            Logger.caughtException(e)
            return getEstimatedBitrateInKbpsForAnalytics(exoBitratePerSec, nsdkBitrate)
        }
    }

    @JvmStatic
    fun connectionQualityFrom(bitrate: Double): String {
        val map = BwEstCfgDataProvider.getStaticConfig()?.speedQualityMapV2  ?:
            PRELOAD_SPEED_QUALITY_MAP
        val s2q = SpeedToQuality(map)
        return s2q.qualityOf(bitrate)
    }

    fun speedToQualityRanges(): List<Triple<String, Long, Long>> {
        val map = BwEstCfgDataProvider.getStaticConfig()?.speedQualityMapV2
                ?: PRELOAD_SPEED_QUALITY_MAP
        return SpeedToQuality(map).ranges()
    }

}