/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper

import androidx.lifecycle.MutableLiveData
import com.newshunt.common.helper.UserConnectionHolder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.dhutil.model.entity.upgrade.BitrateExpression
import com.newshunt.dataentity.dhutil.model.entity.upgrade.BwEstConfig
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NetworkConfig
import com.newshunt.dataentity.dhutil.model.entity.upgrade.NetworkProviderQuality
import com.newshunt.dhutil.helper.preference.AppStatePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * A helper singleton class to access the StaticConfigEntity response.
 * Also exposes a live data to get the up to date StaticConfigEntity
 * Created by srikanth.r on 08/14/2021
 */
object BwEstCfgDataProvider {
    private const val TAG = "StaticConfigDataProvider"
    var bwEstConfig: BwEstConfig? = null
        @Synchronized get
        @Synchronized set(value) {
            field = value
            bwEstConfigLiveData.postValue(value)
        }
    var bwEstConfigLiveData: MutableLiveData<BwEstConfig?> = MutableLiveData()


    var exoWeightage = 1.1 // exo weightage for Bitrate calculation

    var networkWeightage = 2.0 // nw sdk weightage for Bitrate calculation

    var bitrateExpression: BitrateExpression? = null

    var bitrateExpressionV2: BitrateExpression? = null

    var bitrateExpressionException: BitrateExpression? = null

    var bitrateExpressionLifetime: BitrateExpression? = null

    var lifetimeBitrateCaptureWindowSec: Long = 604800

    var cardTransitionThresholdSec: Long = 10

    var exoSlidingPercentileMaxWeight: Int = 2000

    var networkProviderQuality: List<NetworkProviderQuality>? = null

    var percentile: Float = 0.5f

    init {
        Logger.d(TAG, "init >>")
        GlobalScope.launch {
            try {
                bwEstConfig = getStaticConfigFromPref()
            } catch (throwable: Throwable) {
                Logger.caughtException(throwable)
            }
        }
    }

    fun writeToCache(bwEstConfig: BwEstConfig?) {
        bwEstConfig?:return
        GlobalScope.launch {
            try {
                PreferenceManager.savePreference(
                    AppStatePreference.BW_EST_CONFIG,
                    JsonUtils.toJson(bwEstConfig)
                )
                updateNetworkConfig(bwEstConfig.networkConfig)
            } catch (throwable: Throwable) {
                Logger.caughtException(throwable)
            }
        }
    }

    fun updateNetworkConfig(networkConfig: NetworkConfig?) {
        Logger.d(TAG, "updateNetworkConfig networkConfig : $networkConfig")
        //Update the coldStart Config
        if (networkConfig != null) {
            exoWeightage = networkConfig.exoWeightage
            networkWeightage = networkConfig.networkWeightage
            bitrateExpression = networkConfig.bitrateExpression
            bitrateExpressionException = networkConfig.bitrateExpressionException
            bitrateExpressionLifetime = networkConfig.bitrateExpressionLifetime
            bitrateExpressionV2 = networkConfig.bitrateExpressionV2
            lifetimeBitrateCaptureWindowSec = networkConfig.lifetimeBitrateCaptureWindowSec
            cardTransitionThresholdSec = networkConfig.cardTransitionThresholdSec
            exoSlidingPercentileMaxWeight = networkConfig.exoSlidingPercentileMaxWeight
            UserConnectionHolder.exoSlidingPercentileMaxWeight = networkConfig.exoSlidingPercentileMaxWeight
            networkProviderQuality = networkConfig.networkProviderQuality
            percentile = networkConfig.spPercentile
        }

        if (Logger.loggerEnabled()) {
            Logger.d(TAG, "exoWeightage : $exoWeightage")
            Logger.d(TAG, "networkWeightage : $networkWeightage")
            Logger.d(TAG, "bitrateExpression : $bitrateExpression")
            Logger.d(TAG, "bitrateExpressionV2 : $bitrateExpressionV2")
            Logger.d(TAG, "bitrateExpressionException : $bitrateExpressionException")
            Logger.d(TAG, "bitrateExpressionLifetime : $bitrateExpressionLifetime")
            Logger.d(TAG, "lifetimeBitrateCaptureWindowSec : $lifetimeBitrateCaptureWindowSec")
            Logger.d(TAG, "cardTransitionThresholdSec : $cardTransitionThresholdSec")
            Logger.d(TAG, "exoSlidingPercentileMaxWeight : $exoSlidingPercentileMaxWeight")
            Logger.d(TAG, "networkProviderQuality : ${networkProviderQuality?.takeLast(5)}")
            Logger.d(TAG, "percentile: $percentile")
        }
    }

    @JvmStatic
    fun getStaticConfig(): BwEstConfig? {
        return bwEstConfig
    }

    private suspend fun getStaticConfigFromPref(): BwEstConfig? {
        return withContext(Dispatchers.Default) {
            val staticConfigJson = PreferenceManager.getPreference(AppStatePreference.BW_EST_CONFIG, Constants.EMPTY_STRING)
            if (staticConfigJson.isNullOrBlank()) {
                null
            }else {
                val cfg = JsonUtils.fromJson(staticConfigJson, BwEstConfig::class.java)
                updateNetworkConfig(cfg?.networkConfig)
                cfg
            }
        }
    }
}