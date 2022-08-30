/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.newshunt.adengine.model.entity.AdUrl
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.usecase.FetchEvergreenAdsUsecase
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.EgEvent
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.squareup.otto.Subscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Trigger fetch of Evergreen ads if config allows.
 *
 * @author raunak.yadav
 */
object EvergreenAdsFetcher {
    var isRegistered = false
    private var syncInProgress: AtomicBoolean = AtomicBoolean(false)
    private var canRetry = AtomicBoolean(true)
    private val handler: Handler
    private val handlerThread = HandlerThread("EvergreenAdHandler")
    private const val MESSAGE_RETRY = 1
    private var handshakeDisposable: Disposable? = null

    init {
        handlerThread.start()
        handler = object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_RETRY -> {
                        if (canRetry.get()) {
                            canRetry.set(false)
                            AdLogger.d(TAG, "Evergreen Ads : retrying Fetch")
                            EvergreenAdsHelper.getEvergreenAdsUsecase()?.let {
                                fetchEgAds(it)
                            }
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    fun fetchEgAds(fetchUsecase: FetchEvergreenAdsUsecase) {
        if (syncInProgress.get()) {
            return
        }
        syncInProgress.set(true)

        if (!EvergreenAdsHelper.areAdsEnabled()) {
            AdLogger.d(TAG, "Evergreen ads are disabled")
            syncInProgress.set(false)
            return
        }

        // request new ads if minimum delay has passed.
        if (!EvergreenAdsHelper.canHitEvergreenApi()) {
            AdLogger.d(TAG, "Evergreen ads api cannot be hit right now.")
            syncInProgress.set(false)
            return
        }

        var url = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.evergreenAds?.endpoint
        if (url.isNullOrBlank()) {
            syncInProgress.set(false)
            AdLogger.d(TAG, "evergreen Ads sync fail : No endpoint in config")
            return
        }

        url = AdUrl(AdRequest(AdPosition.EVERGREEN,
            adsBaseUrl = AdMacroUtils.replaceMacro(url))).toString()
        AdLogger.d(TAG, "Triggering evergreen Ads sync : $url")

        handshakeDisposable = fetchUsecase.invoke(url)
            .subscribeOn(Schedulers.io())
            .subscribe({
                AdLogger.d(TAG, "Evergreen Ads n/w fetch success : $it")
                onSyncComplete(it)
            }, {
                AdLogger.d(TAG, "Evergreen Ads n/w fetch failed. ${it.message}")
                onSyncComplete(false)
            })
    }

    private fun onSyncComplete(success: Boolean) {
        handshakeDisposable?.dispose()
        syncInProgress.set(false)
        if (success) {
            EvergreenAdsHelper.persistSyncTime(System.currentTimeMillis())
        } else {
            EvergreenAdsHelper.clearPersistedETag()
            EvergreenAdsHelper.getRetryDelay()?.let {
                handler.sendEmptyMessageDelayed(MESSAGE_RETRY, it)
            }
        }
    }

    @Subscribe
    fun fetchEgAdsEvent(egEvent: EgEvent?) {
        AdLogger.e(TAG, "received event for EgAds prefetch from : ${egEvent?.tag}")
        EvergreenAdsHelper.getEvergreenAdsUsecase()?.let {
            fetchEgAds(it)
        }
    }

    fun initialize() {
        if (!isRegistered) {
            BusProvider.getAdBusInstance().register(this)
        }
    }
}

private const val TAG = "EvergreenAdsFetcher"