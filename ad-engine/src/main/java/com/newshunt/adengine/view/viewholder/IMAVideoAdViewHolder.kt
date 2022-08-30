/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.dailyhunt.tv.ima.callback.AdPlayerCallBack
import com.dailyhunt.tv.ima.entity.model.ContentAdType
import com.dailyhunt.tv.ima.entity.state.AdState
import com.dailyhunt.tv.ima.player.exo.VideoPlayerWithAdPlayback.AdControlsListener
import com.dailyhunt.tv.ima.playerholder.ContentPlayerHolder
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.databinding.LayoutImaVideoAdsBinding
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeData
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.AdEntityReplaceHandler
import com.newshunt.adengine.view.BackUpAdConsumer
import com.newshunt.adengine.view.helper.ImaAdHelper
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.VideoAdFallback
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dhutil.helper.preference.AdsPreference
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.model.entity.players.AutoPlayable
import com.newshunt.dhutil.scale
import com.newshunt.helper.player.AutoPlayManager
import com.newshunt.helper.player.PlaySettingsChangedEvent
import com.newshunt.helper.player.PlaySettingsListener
import java.io.File
import java.util.ArrayList

/**
 * ViewHolder to display video ads served via IMA sdk.
 *
 * @author raunak.yadav
 */
open class IMAVideoAdViewHolder(val viewBinding: LayoutImaVideoAdsBinding,
                                uniqueRequestId: Int,
                                private val parentLifecycleOwner: LifecycleOwner?,
                                private var adEntityReplaceHandler: AdEntityReplaceHandler? = null,
                                private val followsMute: Boolean = true) :
        AdsViewHolder(viewBinding, uniqueRequestId,parentLifecycleOwner), AdPlayerCallBack, PlaySettingsListener,
        AdControlsListener, AutoPlayable, BackUpAdConsumer, LifecycleObserver {

    private var view: View = viewBinding.root
    private val mediaViewLayout: RelativeLayout
    private val imageView: NHImageView
    private val parent: ViewGroup
    private val clickableViews: MutableList<View>
    private val bottomBanner: View
    private val bottomBrandBannerBar: View
    private val osvBottomBanner: View
    private val bannerTitle: TextView
    private val borderContainer: View
    private val autoplayVisibility: Int
    private var nativeHelper: ImaAdHelper? = null
    private var nativeAssets: NativeData? = null
    private var externalSdkAd: ExternalSdkAd? = null
    private var height: Int = 0
    private var hitBeaconOnReady = false
    private var isFallbackAdded: Boolean = false
    private var autoPlayManager: AutoPlayManager? = null
    private var videoVisiblePercentage: Int = 0

    override val asset: Any?
        get() = externalSdkAd

    init {

        viewBinding.lifecycleOwner = parentLifecycleOwner
        this.view.visibility = View.GONE

        bannerTitle = viewBinding.bannerTitle
        mediaViewLayout = viewBinding.mediaView
        imageView = viewBinding.bannerImage
        parent = viewBinding.mediaViewParent
        clickableViews = ArrayList()
        bottomBrandBannerBar = viewBinding.adBannerBrandBottombar.adBannerBrandBottombar
        bottomBanner = viewBinding.adBannerBottombar.adBannerBottombar
        osvBottomBanner = viewBinding.adBannerBrandBottombarOsv.adOsvBottombar
        borderContainer = viewBinding.borderContainer

        val adsUpgradeInfo: AdsUpgradeInfo? = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        autoplayVisibility = adsUpgradeInfo?.minVisibilityForAutoplay
                ?: AdConstants.MIN_VISIBILITY_AUTOPLAY_VIDEO
        clearableImageViews.add(imageView)
        parentLifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun updateView(activity: Activity, baseAdEntity: BaseAdEntity) {
        if (baseAdEntity !is ExternalSdkAd) {
            return
        }

        if (nativeHelper != null && nativeHelper?.handleMediaOnAdUpdate(baseAdEntity,
                        autoPlayManager) == true) {
            if (isFallbackAdded) {
                showFallbackImageAd()
            }
            viewBinding.setVariable(BR.adEntity, externalSdkAd)
            viewBinding.executePendingBindings()
            return
        }
        if (view is ViewGroup) {
            AdsUtil.removeClickListenerFromAllChilds(view as ViewGroup)
        }
        externalSdkAd = baseAdEntity
        if (nativeHelper == null) {
            adsTimeSpentOnLPHelper = AdsTimeSpentOnLPHelper()
            nativeHelper = ImaAdHelper(this, activity, this, followsMute,adsTimeSpentOnLPHelper)
        }
        nativeHelper?.updateAdEntity(baseAdEntity)
        externalSdkAd?.let {
            updateView(it, false)
        }
        clickableViews.clear()
        hitBeaconOnReady = false
        asyncAdImpressionReporter = null
        isFallbackAdded = false

        nativeAssets = nativeHelper?.getNativeAssets() ?: return

        bannerTitle.visibility = View.GONE


        mediaViewLayout.removeAllViews()

        view.visibility = View.VISIBLE

        height = getMediaViewHeight(AdsUtil.defaultWidthForWideAds)

        val mediaView = nativeHelper?.getMediaViewIfApplicable(mediaViewLayout)
        (mediaView as? ContentPlayerHolder)?.adProtocol?.apply {
            setImmersiveSpan(externalSdkAd?.immersiveTransitionSpan ?: PreferenceManager.getPreference(AdsPreference.IMMERSIVE_VIEW_TRANSITION_SPAN, -1))
            setQualifiesImmersive(externalSdkAd?.enableImmersiveView ?: false)
            setCompanionRefreshTime(externalSdkAd?.companionRefreshTime ?: PreferenceManager.getPreference(AdsPreference.IMMERSIVE_VIEW_REFRESH_TIME, -1))
        }

        if (mediaView != null) {
            setupDHMediaView(mediaView as ContentPlayerHolder)
        } else if (!DataUtil.isEmpty(nativeAssets?.wideImageUrl)) {
            onVideoAdFailedToLoad()
        } else {
            mediaViewLayout.visibility = View.GONE
            imageView.visibility = View.GONE
        }
        clickableViews.add(imageView)
        clickableViews.add(bottomBanner)
        clickableViews.add(bannerTitle)
        clickableViews.add(bottomBrandBannerBar)
        clickableViews.add(osvBottomBanner)

        nativeHelper?.registerViewForInteraction(view, clickableViews)
        externalSdkAd?.adReportInfo = AdsUtil.getAdReportInfo(nativeAssets)
        viewBinding.setVariable(BR.adEntity, externalSdkAd)
        viewBinding.setVariable(BR.item, nativeAssets)
        viewBinding.executePendingBindings()
    }

    private fun getMediaViewHeight(viewWidth: Int): Int {
        val width = externalSdkAd?.width
        val height = externalSdkAd?.height
        return if (width == 0 || height == 0) {
            AdsUtil.mediaViewDefaultHeight
        } else {
            AdLogger.d(AdsUtil.TAG,"ad id " + externalSdkAd?.id)
            //TODO: remove harcoded aspect ratio for height calculation
            AdsUtil.getHeightWithAspectRatio(width!!, height!!, AdConstants.ASPECT_RATIO_VIDEO_MIN,
                    viewWidth, -1f, true)
        }
    }

    private fun setupDHMediaView(adPlayerHolder: ContentPlayerHolder) {
        if (externalSdkAd?.adPosition == AdPosition.P0) {
            loadPlayer(adPlayerHolder)
        } else {
            AndroidUtils.getMainThreadHandler().post { loadPlayer(adPlayerHolder) }
        }

    }

    private fun loadPlayer(adPlayerHolder: ContentPlayerHolder) {
        if (nativeHelper?.setupDHMediaView(adPlayerHolder, view.context) == true) {
            imageView.visibility = View.GONE
            mediaViewLayout.layoutParams.height = height
            parent.layoutParams.height = height
        } else {
            onVideoAdFailedToLoad()
        }
    }

    private fun showDHMediaView() {
        imageView.visibility = View.GONE
        mediaViewLayout.layoutParams.height = height
        mediaViewLayout.visibility = View.VISIBLE

        externalSdkAd?.let {
            asyncAdImpressionReporter = AsyncAdImpressionReporter(it)
            asyncAdImpressionReporter?.onAdInflated()
            if (hitBeaconOnReady) {
                onCardView(it)
            }
            mediaViewLayout.postDelayed({
                videoVisiblePercentage = ViewUtils.getVisibilityPercentage(mediaViewLayout)
                if (videoVisiblePercentage > autoplayVisibility && autoPlayManager?.isCurrentPlayingAsset(externalSdkAd) != false) {
                    nativeHelper?.onPlayerInView()
                }
            }, 100)
        }
    }

    private fun onVideoAdFailedToLoad() {
        AdLogger.e(TAG, "IMA outstream ad failed to load " + adEntity?.uniqueAdIdentifier)
        // mark ad as shown to remove it from cache.
        markVideoAdShown()
        if (adEntityReplaceHandler != null) {
            adEntityReplaceHandler?.replaceAdEntityInViewHolder(this)
        } else {
            showFallbackImageAd()
        }
    }

    private fun showFallbackImageAd() {
        mediaViewLayout.visibility = View.GONE
        if (DataUtil.isEmpty(nativeAssets?.wideImageUrl)) {
            return
        }
        isFallbackAdded = true
        imageView.visibility = View.VISIBLE
        bannerTitle.visibility = View.GONE
        viewBinding.setVariable(BR.adsShareViewHelper, null)

        val videoAdFallback: VideoAdFallback? = nativeHelper?.videoAdFallback
        if (videoAdFallback != null) {
            nativeAssets?.ctaText = videoAdFallback.actionText
            nativeAssets?.sponsoredText = videoAdFallback.itemTag
        }
        imageView.layoutParams.height = height
        parent.layoutParams.height = height
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        // update data for beacons and landing.
        nativeHelper?.updateDataOnVideoError()

        if (hitBeaconOnReady) {
            nativeHelper?.onCardView()
        }
        viewBinding.executePendingBindings()
    }

    override fun onCardView(baseAdEntity: BaseAdEntity) {
        if (!baseAdEntity.isShown) {
            if (asyncAdImpressionReporter != null) {
                super.onCardView(baseAdEntity)
                hitBeaconOnReady = false
            } else {
                hitBeaconOnReady = true
            }
        }
        nativeHelper?.onCardView()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        onDestroy(nativeHelper)
        parentLifecycleOwner?.lifecycle?.removeObserver(this)
        adEntityReplaceHandler = null
        val cacheDir = File(CommonUtils.getApplication().externalCacheDir?.path, AdConstants.SCREENSHOT_DIR)
        CommonUtils.deleteTempFiles(cacheDir)
    }

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        super.onVisible(viewVisibilityPercentage, percentageOfScreen)
        videoVisiblePercentage = ViewUtils.getVisibilityPercentage(mediaViewLayout)
        if (videoVisiblePercentage < autoplayVisibility) {
            nativeHelper?.onPlayerOutOfView()
            return
        }
        if (autoPlayManager?.isCurrentPlayingAsset(externalSdkAd) != false) {
            nativeHelper?.onPlayerInView()
        }
    }

    override fun onInVisible() {
        videoVisiblePercentage = 0
        nativeHelper?.onPlayerOutOfView()
    }

    override fun recycleView() {
        super.recycleView()
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        onVisible(viewVisibilityPercentage, percentageOfScreen)
    }

    private fun markVideoAdShown() {
        externalSdkAd?.isShown = true
        externalSdkAd?.notifyObservers()
    }

    override fun onAdStateChanged(ad: Ad?, adState: AdState?, adType: ContentAdType,
                                  companionAdLoaded: Boolean) {
        if (adState == null) {
            return
        }
        when (adState) {
            AdState.AD_LOADED -> {
                nativeHelper?.updateDataOnVideoLoad(nativeAssets, ad)
                showDHMediaView()
                viewBinding.setVariable(BR.item, nativeAssets)
                viewBinding.executePendingBindings()
            }
            AdState.AD_PLAY_STARTED -> {
                val defaultBackground = ThemeUtils.getThemeColorByAttribute(view.context, R.attr.default_background)
                view.findViewById<View>(R.id.ad_player_holder)?.setBackgroundColor(defaultBackground)
                viewBinding.adTimer.visibility = View.VISIBLE
            }
            AdState.AD_ERROR, AdState.AD_UNKNOWN -> {
                viewBinding.adTimer.visibility = View.GONE
                if (externalSdkAd?.isShown == true) {
                    hitBeaconOnReady = true
                }
                if (!isFallbackAdded) {
                    onVideoAdFailedToLoad()
                }
            }
            AdState.ALL_ADS_COMPLETE ->
                viewBinding.adTimer.visibility = View.GONE
        }
    }

    override fun onPlaySettingsChanged(event: PlaySettingsChangedEvent) {
        nativeHelper?.handleMuteState()
    }

    // controlling this view's autoplay.
    override fun setAutoPlayManager(autoPlayManager: AutoPlayManager?) {
        this.autoPlayManager = autoPlayManager
    }

    override fun getAutoplayPriority(fresh: Boolean): Int {
        //No ad -> invalid priority
        if (externalSdkAd == null || nativeHelper == null) {
            return -1
        }
        if (fresh) {
            videoVisiblePercentage = ViewUtils.getVisibilityPercentage(mediaViewLayout)
        }
        // Check for autoplay eligibility
        return if (videoVisiblePercentage < autoplayVisibility || nativeHelper?.canPlayAd() == false) {
            -1
        } else videoVisiblePercentage * 2 + 1
    }

    override fun play() {
        nativeHelper?.onPlayerInView()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun pause() {
        nativeHelper?.onPlayerOutOfView()
    }

    // MediaPlayer explicitly played/paused.
    override fun onPlayTapped() {
        autoPlayManager?.replaceCurrentPlayingView(this)
    }

    override fun onPauseTapped() {
        //Do Nothing
    }

    override fun onAdProgress(timeLeft: String) {
        viewBinding.adTimer.text = timeLeft
    }

    override fun onBackupAdFetched(success: Boolean) {
        isFallbackAdded = success
        if (!success) {
            showFallbackImageAd()
        }
    }

    override fun canRelease(): Boolean {
        return false
    }

    override fun releaseVideo() {
        // do nothing, only for exoplayer
    }

    override fun getVisibilityPercentage(): Int {
        return videoVisiblePercentage
    }

    override fun getPositionInList(): Int {
        return adapterPosition
    }

    override fun resetVideoState() {

    }

    override fun isPLaying(): Boolean {
        return nativeHelper?.isAdPlaying == true
    }

    override fun immersiveADview(): View? {
        if(getVisibilityPercentage() > autoplayVisibility) {
            viewBinding.borderContainer.removeView(viewBinding.borderInnerContainer)
            return viewBinding.borderInnerContainer
        }
        return null
    }

    override fun reinsertAdview(view: View?) {
        view ?: return
        viewBinding.borderContainer.addView(view, 0)
        //reset view dimensions
        arrayOf(viewBinding.mediaViewParent, viewBinding.mediaView).scale(ViewGroup.LayoutParams.MATCH_PARENT, height)
        viewBinding.mediaView.findViewById<PlayerView>(R.id.videoPlayer)?.let {
            it.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        if (nativeHelper?.isVideoPlayableInCurrentState == true) {
            viewBinding.adTimer.visibility = View.VISIBLE
        }
        if (baseAdEntity?.adTemplate != AdTemplate.ENHANCED_HIGH)
            viewBinding.adReport.visibility = View.VISIBLE

        //fire for immersive ad update rule
        adEntity?.let {
            fireImmersiveUpdateRule(it, playedInImmersive = true)
        }
    }
}
private const val TAG = "IMAVideoAdViewHolder"