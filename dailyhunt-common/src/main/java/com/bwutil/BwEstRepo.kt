/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bwutil.BwEstRepo.Companion.INST
import com.bwutil.db.BwDb
import com.bwutil.db.BwMeasurementDao
import com.bwutil.entity.BwBitrates
import com.bwutil.entity.CQParams
import com.bwutil.util.DbBackedResource
import com.bwutil.util.ExecHelper
import com.bwutil.util.ExponentialGeometricAverage
import com.dailyhunt.tv.exolibrary.util.ExoUtils
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.newshunt.common.AppStateChangeEvent
import com.newshunt.common.helper.UserConnectionHolder
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.ConnectionInfoHelper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.BwEstCfgDataProvider
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import com.squareup.otto.Subscribe
import java.util.*
import kotlin.collections.set

/**
 * BandwidthEstimationRepository
 * Each instance holds one DB connection.
 * Recommended to keep a single instance for the whole app - [INST]
 * Handles making requests in bg thread.
 * Computes different metrics over stored measurements
 *
 * @author satosh.dhanyamraju
 */
class BwEstRepo @JvmOverloads constructor(
    context: Context,
    private val bitrateToSpeedConv: (Long) -> String,
    inMemoryOnly: Boolean = false/*used for testing*/,
    speedToQRanges: () -> List<Triple<String, Long, Long>> = BitrateCalculations::speedToQualityRanges,
    ttlSec: Long = BwEstCfgDataProvider.lifetimeBitrateCaptureWindowSec/*used for clean up of older data.*/,
    private val ema: ExponentialGeometricAverage = ExponentialGeometricAverage(0.2),
    private val execHelper: ExecHelper = ExecHelper(),
    private val currentNetwork: () -> String = ::currentNetworkKey): BandwidthMeter.EventListener {

    private val ttlMs: Long = ttlSec * 1000

    private val _db = ((if (inMemoryOnly) Room.inMemoryDatabaseBuilder(context, BwDb::class.java)
            .allowMainThreadQueries() else Room.databaseBuilder(context, BwDb::class.java, "bw.db")))
            .addCallback(CleanupCallback(ttlMs, execHelper)).build()
    @VisibleForTesting
    val _bwDao: BwMeasurementDao
        get() = _db.bwDao()

    private var latestBitrate = DbBackedResource(-1L, _5m, execHelper, fetchFromDB = {
        _bwDao.latestBitrate(currentNetwork()) ?: -1
    })

    /* not optimizing for very 1st time init because it is ema over hourly-mean and there is
    * already 5m refresh interval. */
    private val lfEma = DbBackedResource(ema, _5m, execHelper, fetchFromDB = {
        val lfBitrates = _bwDao.lifetimeBitratesAveraged(currentNetwork())
        ema.reset()
        lfBitrates.forEach { ema.addMeasurement(it.toDouble()) }
        ema
    })

    private val lfBitrateBucketMap = DbBackedResource(HashMap(), _5m, execHelper, fetchFromDB = {
        (HashMap<String, Int>()).apply {
            speedToQRanges().forEach {
                put(it.first, _bwDao.countBitrateBetween(it.second, it.third, currentNetwork()))
            }
        }
    })

    private val dbResources = arrayListOf(latestBitrate, lfEma, lfBitrateBucketMap)

    private var firstMeasurementReceivedAt : Long = -1L

    private var lastKnownNetwork : String? = null

    init {
        AndroidUtils.getMainThreadHandler().post {
            BusProvider.getUIBusInstance().register(this)
        }
        Logger.d(TAG, "init: setting listener")
        ExoUtils.bwEventListener = this
    }

    fun addBitrate(bitrateBitsPerSec: Long) {
        Logger.d(TAG, "addBitrate: bitratePerSec=$bitrateBitsPerSec")
        if(firstMeasurementReceivedAt == -1L) firstMeasurementReceivedAt = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val bw = BwBitrates(System.currentTimeMillis(), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), currentNetwork(), bitrateBitsPerSec/1000)
        execHelper.runIO {
            kotlin.runCatching {
                _bwDao.insert(bw)
                latestBitrate.update(bw.bitrate)
                lfBitrateBucketMap.compute {
                    val speedBucket = bitrateToSpeedConv(bw.bitrate)
                    val lookup = it.getOrPut(speedBucket, { 0 })
                    it[speedBucket] = lookup.inc()
                    it
                }
            }
        }
    }

    fun nwMeta(): CQParams {
        return BitrateCalculations.currentCQParams(latestBitrate.getVal(), bitrateToSpeedConv, firstMeasurementReceivedAt,
                lifetimeCQ(), lifetimeCqDistribution = lifetimeBucketMap()).withFallbacks()
    }

    fun curCQParams(): CQParams {
        val cqParams = BitrateCalculations.currentCQParams(latestBitrate.getVal(),
                bitrateToSpeedConv, firstMeasurementReceivedAt, lifetimeCQ()).withFallbacks()
        UserConnectionHolder.updateUserConnectionValues(cqParams.exoBitrate, cqParams.fbBitrate,
                cqParams.resultBitrate, cqParams.resultBitrateQuality, cqParams.connectionType)
        return cqParams
    }

    fun onNetworkChange() {
        val currentNetworkKey = currentNetworkKey()
        if(currentNetworkKey == lastKnownNetwork) {
            Logger.d(TAG, "onNetworkChange: already on $currentNetworkKey. Ignored")
            return
        }
        Logger.d(TAG, "onNetworkChange: $currentNetworkKey")
        refreshResources()
        curCQParams() // to refresh the values in UserConnectionHolder
        lastKnownNetwork = currentNetworkKey
    }

    fun deleteOlderThan(ttl: Long?) {
        Logger.v(TAG, "deleteOlderThan: $ttl")
        ttl?:return
        execHelper.runIO {
            _bwDao.deleteOlder(System.currentTimeMillis() - ttlMs)
        }
    }

    @VisibleForTesting
    fun lifetimeCQ(): Long {
        return lfEma.getVal().average.toLong()
    }

    @VisibleForTesting
    fun curCQ(): Long = latestBitrate.getVal()

    @VisibleForTesting
    fun lifetimeBucketMap() = lfBitrateBucketMap.getVal()

    @Subscribe
    fun onAppEvent(event: AppStateChangeEvent) {
        if(event.isFirstActivityCreated) refreshResources()
    }

    override fun onBandwidthSample(elapsedMs: Int, bytesTransferred: Long, bitrateEstimate: Long) {
        addBitrate(bitrateEstimate)
    }

    private fun refreshResources() {
        dbResources.forEach { it.refresh() }
    }

    /**
     * Below fallbacks are applied.
     * 1. if currentCQ is 'unknown', change it to 'good'
     * 2. Then, if lifetimeCQ is null or 'unknown', change it it currentCQ
     */
    private fun CQParams.withFallbacks(): CQParams {
        val fallbackCurrentCQ = if (resultBitrateQuality == CONNECTION_QUALITY_UNKNOWN) "good"
        else resultBitrateQuality
        val fallbackLifetimeCQ = if (lifetimeCQ == null || lifetimeCQ == CONNECTION_QUALITY_UNKNOWN)
            fallbackCurrentCQ else lifetimeCQ

        return if (fallbackCurrentCQ != resultBitrateQuality || fallbackLifetimeCQ != lifetimeCQ)
            copy(resultBitrateQuality = fallbackCurrentCQ, lifetimeCQ = fallbackLifetimeCQ)
        else this
    }

    private class CleanupCallback(private val ttlMs: Long = 0L, private val execHelper: ExecHelper) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            val exp = System.currentTimeMillis() - ttlMs
            execHelper.runIO { db.execSQL("DELETE FROM bw_bitrates WHERE ts < $exp") }
        }
    }
    companion object {

        /**
         * Prefer to use it across the app, instead of creating new objects
         */
        @JvmStatic
        var INST: BwEstRepo? = null
            @Synchronized get
            @Synchronized set(value) {
                if (INST == null) {
                    field = value
                }
            }
        private const val TAG: String = "BwEstRepo"
        private const val _5m = 300000L
        const val CONNECTION_QUALITY_UNKNOWN = "unknown"

        @JvmStatic
        fun currentConnectionQuality(): String {
            return INST?.curCQParams()?.resultBitrateQuality ?: CONNECTION_QUALITY_UNKNOWN
        }

        /** used to uniquely id a network, for storing and processing bitrate-measurements */
        private fun currentNetworkKey(): String {
            return "${NetworkSDKUtils.getActiveNetworkInfoStr(CommonUtils.getApplication())}-${ConnectionInfoHelper.getConnectionType()}"
        }

        fun createInstance() {
            if (INST == null) {
                INST = BwEstRepo(CommonUtils.getApplication(), { obj: Long ->
                    BitrateCalculations.connectionQualityFrom(
                        obj.toDouble()
                    )
                })
                INST?.curCQParams()
            }
        }
    }
}