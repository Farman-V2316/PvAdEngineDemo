package com.newshunt.news.helper

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.ViewDataBinding
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.EmptyAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.SplashAdPersistenceHelper
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdsViewHolderFactory
import com.newshunt.adengine.view.helper.AdsViewHolderFactory.Companion.getViewBinding
import com.newshunt.common.helper.common.ApplicationStatus
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.AdDisplayType
import com.newshunt.dhutil.helper.preference.AppStatePreference

/**
 * @author arun.babu
 */
object SplashAdHelper {
    private const val TAG = "SplashAdHelper"

    @JvmStatic
    fun insertAd(activity: Activity?, baseAdEntity: BaseAdEntity,
                 adContainer: RelativeLayout): UpdateableAdView? {
        val cardType = AdsUtil.getCardTypeForAds(baseAdEntity)
        when (cardType) {
            -1 -> return null
            AdDisplayType.EMPTY_AD.index -> {
                AdLogger.d(TAG, "Empty ad received ${baseAdEntity.isEvergreenAd}")
                if (baseAdEntity is EmptyAd) {
                    with(AsyncAdImpressionReporter(baseAdEntity)) {
                        onAdInflated()
                        onCardView()
                    }
                }
                return null
            }
        }
        val updateableAdView = getUpdateableAdView(activity, cardType, adContainer)
        updateableAdView?.updateView(activity, baseAdEntity)
        updateableAdView?.onCardView(baseAdEntity)
        return updateableAdView
    }

    private fun getUpdateableAdView(context: Context?, cardType: Int,
                                    adContainer: RelativeLayout): UpdateableAdView? {
        if (context == null) {
            return null
        }
        var updateableAdView: UpdateableAdView? = null
        var viewDataBinding: ViewDataBinding? = null
        when (cardType) {
            AdDisplayType.HTML_AD_FULL.index -> {
                viewDataBinding = getViewBinding(cardType,
                        LayoutInflater.from(context), adContainer)
                viewDataBinding?.let {
                    updateableAdView = AdsViewHolderFactory.getViewHolder(
                            cardType,
                            it,
                            -1, adContainer
                    ) as? UpdateableAdView
                }
            }
        }
        viewDataBinding?.let {
            adContainer.removeAllViews()
            val rLParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            adContainer.addView(viewDataBinding.root, rLParams)
        }
        return updateableAdView
    }

    fun isCachedSplashAdAvailable(): Boolean {
        return SplashAdPersistenceHelper.isCachedSplashAdAvailable()
    }

    fun fetchDefaultSplash(isAppOpen: Boolean) {
        //If app is not registered, do not make ad request.
        if (!PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false)) {
            return
        }

        if (isAppOpen || ApplicationStatus.getVisibleActiviesCount() > 0) {
            AdRequest(AdPosition.SPLASH_DEFAULT, 1).apply {
                val adUsecase = GetAdUsecaseController(BusProvider.getUIBusInstance(), -1, needBusRegistration = false)
                adUsecase.requestAds(this)
            }
        }
    }
}