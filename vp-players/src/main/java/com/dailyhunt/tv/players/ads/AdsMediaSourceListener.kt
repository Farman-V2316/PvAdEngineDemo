/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.ads

import com.dailyhunt.tv.ima.listeners.ImaAdsListener
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.instream.IAdLogger
import com.newshunt.adengine.listeners.PlayerInstreamAdListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.io.IOException
import java.lang.ref.WeakReference


class AdsMediaSourceListener() : MediaSourceEventListener, ImaAdsListener {

    private lateinit var itemId: String
    private var adEntity: BaseDisplayAdEntity? = null
    private lateinit var playerInstreamAdListener: WeakReference<PlayerInstreamAdListener>
    private var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null

    private var companionAdSlots: List<CompanionAdSlot>? = null
    private var companionListener: CompanionAdSlot.ClickListener? = null
    private var adsTimeSpentOnLPHelper : AdsTimeSpentOnLPHelper? = null

    constructor(itemId: String, adEntity: BaseDisplayAdEntity?,
                playerInstreamAdListener: PlayerInstreamAdListener, adsTimeSpentOnLPHelper : AdsTimeSpentOnLPHelper?) : this() {
        IAdLogger.d(TAG, "AdsMediaSourceListener constructor : itemId : $itemId")
        this.itemId = itemId
        this.adEntity = adEntity
        this.adsTimeSpentOnLPHelper = adsTimeSpentOnLPHelper
        this.playerInstreamAdListener = WeakReference(playerInstreamAdListener)
        if (adEntity != null)
            asyncAdImpressionReporter = AsyncAdImpressionReporter(adEntity)
    }

    override fun setCompanionAdSlots(companionAdSlots: List<CompanionAdSlot>?) {
        this.companionAdSlots = companionAdSlots
    }

    override fun onAdEvent(adEvent: AdEvent) {

        if (adEvent.type != AdEvent.AdEventType.AD_PROGRESS) {
            IAdLogger.d(TAG, "onAdEvent : AdEvent :: ${adEvent.type} ")
            IAdLogger.d(TAG, "onAdEvent : podIndex :: ${adEvent.ad?.adPodInfo?.podIndex} &ItemId : $itemId")

            if (adEvent.type == AdEvent.AdEventType.LOG) {
                for ((key, value) in adEvent.adData) {
                    IAdLogger.d(TAG, "$key : $value")
                }
                return
            }
        }

        when (adEvent.type) {
            AdEvent.AdEventType.LOADED -> {
                //Remove previous companion ad content & listeners.
                companionAdSlots?.let {
                    it.forEach { slot ->
                        slot.removeClickListener(companionListener)
                    }
                    it[0].container.removeAllViews()
                }
            }
            AdEvent.AdEventType.RESUMED -> {
                playerInstreamAdListener.get()?.onAdResumed()
            }
            AdEvent.AdEventType.PAUSED -> {
                playerInstreamAdListener.get()?.onAdPaused()
            }
            AdEvent.AdEventType.TAPPED -> {
                playerInstreamAdListener.get()?.onAdTapped()
            }
            AdEvent.AdEventType.CLICKED -> {
                triggerAdBeaconUrl(adEvent.ad, adEntity, AdBeaconType.LandingBeacon, asyncAdImpressionReporter, adsTimeSpentOnLPHelper)
            }
            AdEvent.AdEventType.SKIPPED -> {
                //Do Nothing
                playerInstreamAdListener.get()?.onAdSkipped()
                playerInstreamAdListener.get()?.onAdComplete()
            }
            AdEvent.AdEventType.STARTED -> {
                var isCompanionFilled = false
                companionAdSlots?.forEach {
                    if (it.isFilled) {
                        isCompanionFilled = true
                        if (companionListener == null) {
                            companionListener = CompanionAdSlot.ClickListener {
                                triggerAdBeaconUrl(adEvent.ad, adEntity,
                                    AdBeaconType.CompanionClickBeacon, asyncAdImpressionReporter, adsTimeSpentOnLPHelper)
                            }
                        }
                        it.addClickListener(companionListener)
                    }
                }
                playerInstreamAdListener.get()?.onAdStarted(adEvent.ad, adEntity, isCompanionFilled)
                triggerAdBeaconUrl(adEvent.ad, adEntity, AdBeaconType.RequestAndBeacon, asyncAdImpressionReporter)
            }
            AdEvent.AdEventType.COMPLETED -> {
                playerInstreamAdListener.get()?.onAdComplete()
            }
            AdEvent.AdEventType.ALL_ADS_COMPLETED -> {
                playerInstreamAdListener.get()?.onAllAdComplete()
            }
            else -> {
            }
        }

    }

    override fun onLoadStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, loadEventInfo: MediaSourceEventListener.LoadEventInfo?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        IAdLogger.d(TAG, "onLoadStarted : data spec :: ${loadEventInfo?.dataSpec}")
    }

    override fun onDownstreamFormatChanged(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        IAdLogger.d(TAG, "onDownstreamFormatChanged :: $windowIndex")

    }

    override fun onUpstreamDiscarded(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        IAdLogger.d(TAG, "onUpstreamDiscarded :: $windowIndex")
    }

    override fun onLoadCanceled(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
                                loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                                mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        IAdLogger.d(TAG, "onLoadCanceled :: ${loadEventInfo?.dataSpec}")
//        playerInstreamAdListener.get()?.onAdError("Load Canceled")
//        triggerAdBeaconUrl(null, AdBeaconType.ErrorBeacon)
    }

    override fun onMediaPeriodCreated(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
        // Do nothing
    }

    override fun onMediaPeriodReleased(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
        // Do nothing
    }

    override fun onReadingStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
        // Do nothing
    }

    override fun onLoadCompleted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, loadEventInfo: MediaSourceEventListener.LoadEventInfo?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        IAdLogger.d(TAG, "onLoadCompleted")
//        IAdLogger.d(TAG, "***** data spec : " + dataSpec.toString())
//        IAdLogger.d(TAG, "***** trackType : " + trackType)
//        IAdLogger.d(TAG, "***** trackSelectionReason : " + trackSelectionReason)
//        IAdLogger.d(TAG, "***** trackSelectionData : " + trackSelectionData)
//        IAdLogger.d(TAG, "***** mediaStartTimeMs : " + mediaStartTimeMs)
//        IAdLogger.d(TAG, "***** mediaEndTimeMs : " + mediaEndTimeMs)
//        IAdLogger.d(TAG, "***** elapsedRealtimeMs : " + elapsedRealtimeMs)
//        IAdLogger.d(TAG, "***** loadDurationMs : " + loadDurationMs)
//        IAdLogger.d(TAG, "***** bytesLoaded : " + bytesLoaded)
    }

    override fun onLoadError(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
                             loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                             mediaLoadData: MediaSourceEventListener.MediaLoadData?,
                             error: IOException?, wasCanceled: Boolean) {
        IAdLogger.e(TAG, "onLoadError : error :: ${error?.message} , \ndata spec :: " +
                "${loadEventInfo?.dataSpec}")

        if (error?.cause is AdError) {
            playerInstreamAdListener.get()?.onAdError(error.message)
            triggerAdBeaconUrl(null, adEntity, AdBeaconType.ErrorBeacon, asyncAdImpressionReporter)
        }
    }

    companion object {
        fun triggerAdBeaconUrl(ad: Ad?, sdkAd: BaseAdEntity?, beaconType: AdBeaconType,
                               asyncAdImpressionReporter: AsyncAdImpressionReporter?,
                               adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper? = null) {
            try {
                //IAdLogger.d(TAG, "triggerAdBeaconUrl :: podIndex : $podIndex &ItemId: $itemId")
                if (sdkAd !is ExternalSdkAd) {
                    IAdLogger.d(TAG, "triggerAdBeaconUrl :: sdkAd is NULL")
                    return
                }

                //Custom tracking not available. Trigger normal beacons, if any.
                if (sdkAd.customTracking == null || sdkAd.customTracking?.tracking?.isEmpty()
                        !!) { //todo mukesh check assert
                    if (sdkAd.adPosition == AdPosition.INLINE_VIDEO) {
                        IAdLogger.d(TAG, "triggerAdBeaconUrl :: Beacon url as INLINE_VIDEO")
                    }
                    IAdLogger.d(TAG, "triggerAdBeaconUrl :: Normal trackers for $beaconType")
                    when (beaconType) {
                        AdBeaconType.RequestAndBeacon -> {
                            sdkAd.isShown = true
                            sdkAd.notifyObservers()
                            asyncAdImpressionReporter?.onCardView()
                        }
                        AdBeaconType.LandingBeacon -> {
                            asyncAdImpressionReporter?.onClickEvent()
                        }
                        else -> {
                        }
                    }
                    return
                }

                val id = AdsUtil.getPodAdId(ad)
                var trackingNode: ExternalSdkAd.Tracking? = null
                sdkAd.customTracking?.tracking?.let {
                    for(item in it) {
                        if(CommonUtils.equals(item.id, id)) {
                            trackingNode = item
                            break
                        }
                    }
                }

                trackingNode ?: return

                when (beaconType) {
                    AdBeaconType.RequestAndBeacon -> {
                        val beaconUrl = trackingNode?.beaconUrl
                        IAdLogger.d(TAG, "triggerAdBeaconUrl :: RequestAndBeacon $beaconUrl")
                        asyncAdImpressionReporter?.hitTrackerUrl(true, trackingNode?.requestUrl)
                        asyncAdImpressionReporter?.hitTrackerUrl(true, beaconUrl)
                    }
                    AdBeaconType.LandingBeacon -> {
                        adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(trackingNode?.adLPTimeSpentBeaconUrl)
                        val landingUrl = trackingNode?.landingUrl
                        IAdLogger.d(TAG, "triggerAdBeaconUrl :: LandingBeacon $landingUrl")
                        asyncAdImpressionReporter?.hitTrackerUrl(true, landingUrl)
                    }
                    AdBeaconType.ErrorBeacon -> {
                        val errorBeaconUrl = trackingNode?.errorBeaconUrl
                        IAdLogger.d(TAG, "triggerAdBeaconUrl :: ErrorBeacon $errorBeaconUrl")
                        asyncAdImpressionReporter?.hitTrackerUrl(true, errorBeaconUrl)
                    }
                    AdBeaconType.CompanionClickBeacon -> {
                        adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(trackingNode?.customCompanionTrackings?.companionLPTimeSpentBeaconUrl)
                        trackingNode?.customCompanionTrackings?.companionClickTracking?.forEach{
                            IAdLogger.d(TAG, "triggerAdBeaconUrl :: CompanionClickBeacon $it")
                            asyncAdImpressionReporter?.hitTrackerUrl(true, it)
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.caughtException(e)
            }
        }

        private const val TAG = "AdsMediaSourceListener"
    }
}

enum class AdBeaconType {
    RequestAndBeacon,
    LandingBeacon,
    ErrorBeacon,
    CompanionClickBeacon
}
