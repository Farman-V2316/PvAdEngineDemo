/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.helpers

import android.net.Uri
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import com.dailyhunt.tv.exolibrary.util.MediaSourceUtil
import com.dailyhunt.tv.ima.exo.AdsMediaSourceFactory
import com.dailyhunt.tv.ima.exo.ImaAdsLoader
import com.dailyhunt.tv.ima.helper.ImaUtils
import com.dailyhunt.tv.ima.listeners.ImaAdsListener
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.newshunt.adengine.instream.IAdLogger
import com.newshunt.adengine.listeners.PlayerInstreamAdListener
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.util.AdConstants
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import java.net.URLDecoder
import java.util.*


object ExoMediaSourceGenerator {
    private val TAG = ExoMediaSourceGenerator::class.java.simpleName
    val DEFAULT = DefaultBandwidthMeter()

    /**
     * Compose content mediaSource with Ads source.
     */
    fun getMappedAdMediaSource(videoAsset: ExoPlayerAsset, adsLoader: ImaAdsLoader?,
                               playerListener: PlayerInstreamAdListener): MediaSource {
        var mediaSource = getMappedMediaSourceOnly(videoAsset)
        if(videoAsset.isGif || videoAsset.loopCount > 0) {
            IAdLogger.d(TAG, "getMappedAdMediaSource Found Gif, returning mediaSource without Ads")
            return mediaSource;
        }
        IAdLogger.d(TAG, "getMappedAdMediaSource $adsLoader")
        adsLoader?.let {
            mediaSource = AdsMediaSource(
                    mediaSource, AdsMediaSourceFactory(),
                    adsLoader, playerListener.getExoPlayerView()
            )
        }
        return mediaSource
    }

    fun buildAdsLoader(adEntity: ExternalSdkAd?, adListener: ImaAdsListener,
                       companionView: ViewGroup?): ImaAdsLoader? {
        adEntity ?: return null

        val decodedAdUrl = getUrlDecoded(adEntity.external?.tagURL)
        decodedAdUrl ?: return null

        IAdLogger.d(TAG, "getUrlDecoded - $decodedAdUrl")

        val builder = ImaAdsLoader.Builder(CommonUtils.getApplication())
            .setAdEventListener(adListener)

        //Set max bitrate as per ads handshake.
        val maxBitRate = ImaUtils.getMaxBitrateFromHandshake()
        if (maxBitRate > 0) {
            builder.setMaxMediaBitrate(maxBitRate)
        }

        val uri = getAdUri(decodedAdUrl)
        val adsLoader = builder.buildForAdTag(uri)

        companionView ?: return adsLoader

        // parse companionSizes
        val sizes = getCompanionSize(uri.getQueryParameter(AdConstants.COMPANION_SIZES_QUERY_PARAM))
        if (!CommonUtils.isEmpty(sizes)) {
            // Set up slots for companion.
            val companionAdSlots = ArrayList<CompanionAdSlot>()
            sizes!!.forEach {
                val companionAdSlot = ImaSdkFactory.getInstance().createCompanionAdSlot()
                companionAdSlot.container = companionView
                companionAdSlot.setSize(it.first, it.second)
                companionAdSlots.add(companionAdSlot)
            }
            adListener.setCompanionAdSlots(companionAdSlots)
            adsLoader.setCompanionSlots(companionAdSlots)
        }
        return adsLoader
    }

    private fun getAdUri(adUrl: String?): Uri {
        if (adUrl == null) {
            return Uri.EMPTY
        }
        return Uri.parse(adUrl)
    }

    private fun getUrlDecoded(adUrl: String?): String? {
        if (CommonUtils.isEmpty(adUrl)) {
            return null
        }
        return URLDecoder.decode(adUrl, Constants.TEXT_ENCODING_UTF_8)
    }

    private fun getCompanionSize(sizes: String?): ArrayList<Pair<Int, Int>>? {
        sizes ?: return null
        val adSizes = ArrayList<Pair<Int, Int>>()
        try {
            val adSizesArgs = sizes.split(Constants.COMMA_CHARACTER)
            for (size in adSizesArgs) {
                if (size.contains(Constants.SIZE_TOKEN)) {
                    val args = size.split(Constants.SIZE_TOKEN)
                    val width = DataUtil.parseInt(args[0], 0)
                    val height = DataUtil.parseInt(args[1], 0)
                    if (width != 0 && height != 0) {
                        adSizes.add(Pair(width, height))
                    }
                }
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return adSizes
    }

    fun getMappedMediaSourceOnly(videoAsset: ExoPlayerAsset?): MediaSource {
        var mediaSource = MediaSourceUtil.getMappedSource(CommonUtils.getApplication(),
            getUriForSource(videoAsset?.videoUrl), videoAsset?.isLiveStream ?: false)
        if (videoAsset != null && videoAsset.loopCount > 0) {
            mediaSource = LoopingMediaSource(mediaSource, videoAsset.loopCount)
        }
        return mediaSource
    }

    private fun getUriForSource(url: String?): Uri {
        if (url.isNullOrBlank()) return Uri.EMPTY
        try {
            return Uri.parse(url)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
        return Uri.EMPTY
    }

}