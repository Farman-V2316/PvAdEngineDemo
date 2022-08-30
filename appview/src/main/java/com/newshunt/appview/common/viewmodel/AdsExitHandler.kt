package com.newshunt.appview.common.viewmodel

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.listeners.AdExitListener
import com.newshunt.adengine.model.AdInteraction
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger

/**
 * Ad Exit logic can be triggered only if the state != [AdExitState.COMPLETE]
 * It can be restarted only if state == [AdExitState.CANCELED]
 *
 * @author raunak.yadav
 */
class AdsExitHandler : AdExitListener {

    private var closeHandler: Handler = Handler(Looper.getMainLooper())
    var state = AdExitState.NONE

    override fun cancelExitApp(adInteraction: AdInteraction?) {
        if (state == AdExitState.COMPLETE) return
        Logger.d(TAG, "[Ad] Cancelling app exit")
        closeHandler.removeCallbacksAndMessages(null)
        state = if (adInteraction == AdInteraction.USER_CLICK) AdExitState.CANCELED_ON_CLICK else
            AdExitState.CANCELED
    }

    override fun closeToExitApp(adEntity: BaseDisplayAdEntity?, adInteraction: AdInteraction) {
        //Remove any posted message and close now.
        if (state == AdExitState.COMPLETE) return
        closeHandler.removeCallbacksAndMessages(null)
        trackAdClose(adEntity, adInteraction)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(Intent(Constants.EXIT_SPLASH_AD_CLOSE_ACTION)))
    }

    override fun closeToExitAppwithDelay(adEntity: BaseDisplayAdEntity?,
                                         adInteraction: AdInteraction,
                                         delay: Long,
                                         onAdCloseCallback: (() -> Unit)?) {
        if (state == AdExitState.COMPLETE) return
        state = AdExitState.STARTED
        Logger.d(TAG,
            "[${adEntity?.adPosition}][${adEntity?.uniqueAdIdentifier}] Exit the app with $delay.")
        closeHandler.postDelayed({
            trackAdClose(adEntity, adInteraction)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(Intent(Constants.EXIT_SPLASH_AD_CLOSE_ACTION)))
            onAdCloseCallback?.invoke()
        }, delay)
    }

    fun trackAdClose(adEntity: BaseDisplayAdEntity?, adInteraction: AdInteraction) {
        if (state == AdExitState.COMPLETE) return
        state = AdExitState.COMPLETE
        adEntity?.let {
            Logger.d(TAG, "[${adEntity.adPosition}][${adEntity.uniqueAdIdentifier}] Exit the " +
                    "app. [$adInteraction]")
            AsyncAdImpressionReporter(adEntity).onAdViewToggled(adInteraction)
        }
    }
}

enum class AdExitState {
 NONE, STARTED, CANCELED_ON_CLICK, CANCELED, COMPLETE
}
private const val TAG = "AdsExitHandler"