/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.app.helper

import android.content.Context
import android.net.ConnectivityManager
import com.dailyhunt.tv.exolibrary.entities.StreamConfigAsset
import com.dailyhunt.tv.exolibrary.ui.CustomRenderersFactory
import com.dailyhunt.tv.exolibrary.util.ExoBufferSettings
import com.dailyhunt.tv.exolibrary.util.ExoUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.gson.Gson
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerDimensions
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUpgradeInfoResponse
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerVideoQuality
import com.newshunt.dhutil.helper.PlayerDataProvider
import com.newshunt.dhutil.helper.common.DailyhuntConstants


const val MIN_DURATION_RETAIN_AFTER_BUFFER_DISCARD = 2000
const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER = 1000

class ExoPlayerUtils {

    companion object {

        fun buildAudioPlayer(config: StreamConfigAsset? = null): SimpleExoPlayer {
            val adaptivetrackFactory = AdaptiveTrackSelection.Factory(
                getBandwidthMeter(),
                ExoBufferSettings.getHlsMinTimeForSwitchUpMs(),
                ExoBufferSettings.getHlsMaxTimeForSwitchDownMs(),
                MIN_DURATION_RETAIN_AFTER_BUFFER_DISCARD,
                AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION)
            val trackSelector = DefaultTrackSelector(adaptivetrackFactory)

            var bufferMax = 4000
            var bufferMin = 2000

            val audioQuality = getDesiredAudioQuality()
            if (audioQuality != null) {
                bufferMax = audioQuality.bufferMaxSize
                bufferMin = audioQuality.bufferMinSize
            }

            Logger.d("ExoBuffer", "bufferMin - " + bufferMin)
            Logger.d("ExoBuffer", "bufferMax - " + bufferMax)

            val renderersFactory = CustomRenderersFactory(CommonUtils.getApplication())

            val loadControlBuilder = DefaultLoadControl.Builder()
                .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
                .setBufferDurationsMs(bufferMin, bufferMax,
                    ExoBufferSettings.getInitialBufferMs(),
                    BUFFER_FOR_PLAYBACK_AFTER_REBUFFER)

            val exoPlayer = SimpleExoPlayer.Builder(CommonUtils.getApplication(), renderersFactory)
                .setTrackSelector(trackSelector).setLoadControl(loadControlBuilder.createDefaultLoadControl())
                .setBandwidthMeter(ExoUtils.BANDWIDTH_METER).build()
            return exoPlayer
        }

        fun getBandwidthMeter(): DefaultBandwidthMeter {
            return DefaultBandwidthMeter.Builder(CommonUtils.getApplication())
//                .setEventListener(Handler(), listener)
                .setInitialBitrateEstimate(C.NETWORK_TYPE_WIFI, 87000)
                .setInitialBitrateEstimate(C.NETWORK_TYPE_2G, 87000)
                .setInitialBitrateEstimate(C.NETWORK_TYPE_3G, 87000)
                .setInitialBitrateEstimate(C.NETWORK_TYPE_4G, 87000)
                .build()
        }

        val listener = BandwidthMeter.EventListener { elapsedMs, bytes, bitrate -> Logger.d("EXO_PLAYER_BANDWIDTH", "bytes : ${bytes} ; bitrate: ${bitrate} ; elapsedTime: ${elapsedMs}ms") }

        fun getDesiredAudioQuality(): PlayerVideoQuality? {

            var desiredAudioQuality: PlayerVideoQuality? = null
            val dimensions = getPlayerDimensions()
            if (dimensions == null) return null
            // For Auto mode
            desiredAudioQuality = dimensions.adaptiveSettings
            // Check the connection speed and based on that pickup nominal bitrate from other video
            // quality array

            val currentNetworkType = getNetworkType(CommonUtils.getApplication())
            val videoQualities = dimensions.audioQualities

            if (!CommonUtils.isEmpty(videoQualities) && currentNetworkType != null) {
                for (i in videoQualities.indices) {
                    if (currentNetworkType.index == videoQualities[i].networkType) {
                        desiredAudioQuality!!.nomialBitRateForHLSFirstvariant = videoQualities[i].nomialBitRateForHLSFirstvariant
                        desiredAudioQuality.bufferMinSize = videoQualities[i].bufferMinSize
                        desiredAudioQuality.bufferMaxSize = videoQualities[i].bufferMaxSize
                        break
                    }
                }
            }
            return desiredAudioQuality
        }

        fun getNetworkType(context: Context): PlayerNetworkType? {
            val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivityManager.activeNetworkInfo
            // No network connectivity then NetworkInfo becomes null. So below condition is applied.
            if (info == null || info.type == ConnectivityManager.TYPE_WIFI) {
                return PlayerNetworkType.NETWORK_TYPE_WIFI
            } else if (info.type == ConnectivityManager.TYPE_MOBILE) {
                return PlayerNetworkType.fromIndex(info.subtype)
            }
            return PlayerNetworkType.NETWORK_TYPE_UNKNOWN
        }

        fun getPlayerDimensions() : PlayerDimensions? {
            if (PlayerDataProvider.getInstance().playerDimensions == null) {
                PlayerDataProvider.getInstance().playerDimensions = getCachedDimensions(CommonUtils.getApplication())
            }
            return PlayerDataProvider.getInstance().playerDimensions
        }

        fun getCachedDimensions(context: Context): PlayerDimensions? {
            var playerDimensions: PlayerDimensions? = null
            try {
                val jsonData = PreferenceManager.getString(DailyhuntConstants.KEY_PLAYER_DIMENSION_JSON)
                if (!CommonUtils.isEmpty(jsonData)) {
                    playerDimensions = Gson().fromJson(jsonData, PlayerDimensions::class.java)
                }
                if (playerDimensions == null) {
                    playerDimensions = getDimensionsFromAsset("player_handshake_default.json",
                        context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return playerDimensions
        }

        private fun getDimensionsFromAsset(fileName: String, context: Context): PlayerDimensions? {
            val jsonData = CommonUtils.readFromAsset(fileName)
            val apiResponse = Gson().fromJson<PlayerUpgradeInfoResponse>(jsonData, PlayerUpgradeInfoResponse::class.java)
            if (null != apiResponse && null != apiResponse.getData()) {
                val upgradeInfo = apiResponse.getData()
                if (null != upgradeInfo && upgradeInfo.getDimensions() != null) {
                    val tvImageDimensions = upgradeInfo.getDimensions()
                    val jsonImageDimensionsData = Gson().toJson(tvImageDimensions)

                    //Update in preference so tat from next it'll be read directly from preferences
                    PreferenceManager.saveString(DailyhuntConstants.KEY_PLAYER_DIMENSION_JSON,
                        jsonImageDimensionsData)
                    return tvImageDimensions
                }
            }
            return null
        }
    }
}





enum class PlayerNetworkType(val index:Int, val value:String) {

    NETWORK_TYPE_UNKNOWN(0, "NETWORK_TYPE_UNKNOWN"),  // Unknown or default\
    NETWORK_TYPE_GPRS(1, "NETWORK_TYPE_GPRS"),         // ~ 100 kbps
    NETWORK_TYPE_EDGE(2, "NETWORK_TYPE_EDGE"),         // ~ 50-100 kbps
    NETWORK_TYPE_UMTS(3, "NETWORK_TYPE_UMTS"),         // ~ 400-7000 kbps
    NETWORK_TYPE_CDMA(4, "NETWORK_TYPE_CDMA"),         // ~ 14-64 kbps
    NETWORK_TYPE_EVDO_0(5, "NETWORK_TYPE_EVDO_0"),     // ~ 400-1000 kbps
    NETWORK_TYPE_EVDO_A(6, "NETWORK_TYPE_EVDO_A"),     // ~ 600-1400 kbps
    NETWORK_TYPE_1xRTT(7, "NETWORK_TYPE_1xRTT"),       // ~ 50-100 kbps
    NETWORK_TYPE_HSDPA(8, "NETWORK_TYPE_HSDPA"),       // ~ 2-14 Mbps
    NETWORK_TYPE_HSUPA(9, "NETWORK_TYPE_HSUPA"),       // ~ 1-23 Mbps
    NETWORK_TYPE_HSPA(10, "NETWORK_TYPE_HSPA"),        // ~ 700-1700 kbps
    NETWORK_TYPE_IDEN(11, "NETWORK_TYPE_IDEN"),        // ~25 kbps
    NETWORK_TYPE_EVDO_B(12, "NETWORK_TYPE_EVDO_B"),    // ~ 5 Mbps
    NETWORK_TYPE_LTE(13, "NETWORK_TYPE_LTE"),          // ~ 10+ Mbps
    NETWORK_TYPE_EHRPD(14, "NETWORK_TYPE_EHRPD"),      // ~ 1-2 Mbps
    NETWORK_TYPE_HSPAP(15, "NETWORK_TYPE_HSPAP"),      // ~ 10-20 Mbps
    NETWORK_TYPE_WIFI(99, "NETWORK_TYPE_WIFI");        // ~ wifi

    companion object {
        fun fromIndex(index: Int): PlayerNetworkType {
            for (newtworkType in PlayerNetworkType.values()) {
                if (newtworkType.index == index) {
                    return newtworkType
                }
            }
            return NETWORK_TYPE_UNKNOWN
        }
    }
}

