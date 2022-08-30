/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.viewholder

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.bwutil.BwEstRepo
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.exolibrary.listeners.VideoTimeListener
import com.dailyhunt.tv.players.analytics.CommonAdsAnalyticsHelper
import com.dailyhunt.tv.players.analytics.CommonVideoAnalyticsHelper
import com.dailyhunt.tv.players.analytics.VideoAnalyticsHelper
import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.dailyhunt.tv.players.constants.PlayerContants
import com.dailyhunt.tv.players.customviews.ExoPlayerWrapper2
import com.dailyhunt.tv.players.customviews.VideoPlayerWrapper
import com.dailyhunt.tv.players.customviews.VideoWrapperPlayCallbacks
import com.dailyhunt.tv.players.entity.PLAYER_STATE
import com.dailyhunt.tv.players.helpers.PlayerEvent
import com.dailyhunt.tv.players.interfaces.AutoplayPlayerCallbacks
import com.dailyhunt.tv.players.interfaces.PlayerAnalyticCallbacks
import com.dailyhunt.tv.players.model.entities.CallState
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.analytics.helper.ReferrerProvider
import com.newshunt.app.analytics.MarkStoryCardClickUsecase.Companion.VIDEO_BUFFER_TIME_TAG
import com.newshunt.app.analytics.MarkStoryCardClickUsecase.Companion.VIDEO_LOAD_TIME_TAG
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.helper.UserActionHelper
import com.newshunt.appview.common.ui.adapter.UpdateableCardView
import com.newshunt.appview.common.ui.adapter.VideoPrefetchCallback
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.video.helpers.ExoRequestHelper
import com.newshunt.appview.common.video.ui.helper.PlayerState
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.appview.common.video.utils.DHVideoUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.AutoplayVhBinding
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.ConfigType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.dhutil.model.entity.NonLinearConfigurations
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerType
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.dhutil.helper.BwEstCfgDataProvider
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.model.entity.players.AutoPlayable
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.helper.player.AutoPlayManager
import com.newshunt.helper.player.PlaySettingsChangedEvent
import com.newshunt.helper.player.PlayerControlHelper
import com.newshunt.news.helper.VideoPlayBackTimer
import com.newshunt.news.view.entity.RemovableCardView
import com.newshunt.sdk.network.NetworkSDK
import com.newshunt.sdk.network.connection.ConnectionSpeed
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Base class of Autoplay Viewholder
 *
 * Created  on 05/10/19.
 */
abstract class AbstractAutoplayViewHolder(private val binding: ViewDataBinding,
                                          val pageRef: PageReferrer?, val context: Context,
                                          val commonVideoRequester: VideoRequester?,
                                          val isInCollection: Boolean,
                                          val cardsViewModel: CardsViewModel,
                                          val parentLifecycleOwner: LifecycleOwner?,
                                          override val section: String,
                                          private val displayCardTypeIndex: Int,
                                          private var parentItem: CommonAsset?,
                                          override val uniqueScreenId: Int,
                                          private val videoPrefetch: VideoPrefetchCallback? = null) :
        AutoplayPagerAdapter.ViewHolder(binding.root, uniqueScreenId, section, pageRef), AutoplayPlayerCallbacks, VideoPlayerProvider,
        AutoPlayable, VideoWrapperPlayCallbacks, ReferrerProvider, PlayerAnalyticCallbacks,
        VisibilityAwareViewHolder, AutoPlayMuteClickListener, RemovableCardView,
        UpdateableCardView, VideoTimeListener, LifecycleObserver {

    private val LOG_TAG = "AbstractAutoplayViewHolder"

    protected lateinit var viewBinding: AutoplayVhBinding
    private var showTimeLeft = false

    protected var videoWrapper: VideoPlayerWrapper? = null
    protected val uiHandler: Handler
    protected var isViewHolderVisible: Boolean = false
    protected var visiblePercentage: Int = 0
    protected var hasUserLeftFragment: Boolean = false
    protected var isDetailShowing: Boolean = false
    protected var commonAsset: CommonAsset? = null
    private var nonLinearRequestTime: Int = 15
    private var nonLinearDisplayTime: Int = 20
    private var nonLinearFeedUrl: String? = null
    private var autoPlayManager: AutoPlayManager? = null
    private var isAdViewed = false
    private var isAdPlaying: Boolean = false
    private var isNLFCRequestDone = false
    private var commonVideoAnalyticsHelper = CommonVideoAnalyticsHelper()
    private var adsAnalyticsHelper = CommonAdsAnalyticsHelper()
    private var videoStartTime = 0L
    private var topFragmentId: Int? = null
    private var isEnteredDetail = false

    protected var playerHeight: Int = 0
    private var eventSection: NhAnalyticsEventSection = NhAnalyticsEventSection.TV
    private var isBusRegistered = false
    private var isFragmentPaused = false
    protected val autoPlayVisibility = PreferenceManager.getPreference(
            GenericAppStatePreference.MIN_VISIBILITY_FOR_ANIMATION, 90)
    private var isLive = false
    private var isHideCintrol = false
    var pos = 0

    private lateinit var runnableImmersive: Runnable
    private lateinit var runnableImmersiveTimeSpan: Runnable
    private val handlerImmersive: Handler = Handler()
    private var autoImmersiveTimeSpan: Long = -1
    private var autoImmersiveModeActive = false
    private val nlfHandler = Handler(Looper.getMainLooper())
    private var totalBufferTimer: VideoPlayBackTimer = VideoPlayBackTimer()
    protected var videoBufferTimeList = ArrayList<Long>()
    private var isVideoReady = false
    private var totalBufferTime: Long = 0L
    private var videoLoadTimeSCV: Long = 0L
    private var videoLoadStartTime = 0L
    var adsTimeSpentOnLPHelper : AdsTimeSpentOnLPHelper? =null
    private var isEnablePrefetchLogs = PreferenceManager.getPreference(GenericAppStatePreference.ENABLE_PREFETCH_LOGS, false)
    private var isEnableBandwidthLogs = PreferenceManager.getPreference(GenericAppStatePreference.ENABLE_BANDWIDTH_LOGS, false)

    init {
        Logger.i(LOG_TAG, "Creating the Video view holder")
        uiHandler = Handler()

        val nonLinearConfigurations = PreferenceManager.getPreference(GenericAppStatePreference.NON_LINEAR_CONFIGURATIONS, Constants.EMPTY_STRING);
        if (CommonUtils.isEmpty(nonLinearConfigurations)) {
            Logger.i(Constants.NON_LINEAR_FEED, "Non Linear Preferences are not present hence setting it to 0")
            nonLinearRequestTime = 0
            nonLinearDisplayTime = 0
        } else {
            val configurations = JsonUtils.fromJson(nonLinearConfigurations, NonLinearConfigurations::class.java)
            if (configurations != null) {
                nonLinearRequestTime = configurations.autoPlayVideo.request
                nonLinearDisplayTime = configurations.autoPlayVideo.display
            } else {
                Logger.i(Constants.NON_LINEAR_FEED, "Non Linear Preferences are not present hence returning");
            }
        }
        contentAdDelegate = ContentAdDelegate(uniqueScreenId)
        parentLifecycleOwner?.lifecycle?.addObserver(this)
        eventSection = AnalyticsHelper2.getSection(section)
        //Check if auto immersive enabled in handshake and create a handler for auto transition to immersive mode
        if (PreferenceManager.getPreference(AppStatePreference.AUTO_IMMERSIVE_ENABLED, false)) {
            autoImmersiveTimeSpan = PreferenceManager.getPreference(
                    AppStatePreference.AUTO_IMMERSIVE_TIME_SPAN, -1)
        }
    }

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        Logger.d(LOG_TAG, "bind $bindingAdapterPosition")
        this.cardPosition = cardPosition
        if (item !is CommonAsset) return
        isSCVFired = false
        isLive = item.i_videoAsset()?.liveStream ?: false
        isHideCintrol = item.i_videoAsset()?.hideControl ?: false
        nonLinearFeedUrl = item.i_nonLinearPostUrl()
        Logger.d(LOG_TAG, "NLFC URL - " + nonLinearFeedUrl)

        if (binding !is AutoplayVhBinding) {
            return
        }
        val adId = item.i_adId() ?: parentItem?.i_adId()
        contentAdDelegate?.bindAd(adId, item.i_id())
        viewBinding = binding
        viewBinding.setVariable(BR.item, item)
        viewBinding.setVariable(BR.adDelegate, contentAdDelegate)
        viewBinding.setVariable(BR.isInCollection, isInCollection)
        viewBinding.setVariable(BR.vp, this)
        viewBinding.setVariable(BR.state, PerspectiveState())
        viewBinding.setVariable(BR.muteClick, this)

        if (isInCollection) {
            viewBinding.setVariable(BR.parentItem, parentItem)
            viewBinding.setVariable(BR.vm, cardsViewModel)
            viewBinding.setVariable(BR.cardTypeIndex, displayCardTypeIndex)
            viewBinding.setVariable(BR.isDetailView, false)
        }
        viewBinding.setVariable(BR.isLive, isLive)

        updateView(item)

        if (lifecycleOwner != null) {
            viewBinding.lifecycleOwner = lifecycleOwner
        }
        viewBinding.executePendingBindings()

        analyticsItem = item
        displayCacheLogs()
        displayBandwidthLogs()
    }

    fun updateParentItem(parentItem: CommonAsset?) {
        this.parentItem = parentItem
    }

    private fun pauseVideo(endAction: PlayerVideoEndAction) {
        if (!isDetailShowing) {
            Logger.d(LOG_TAG,"Pause Video at pos : $bindingAdapterPosition")
            handlerImmersive.removeCallbacksAndMessages(null)
            videoWrapper?.pause()
            setVideoEndAction(endAction)
            hideLoader()
        }
    }

    override fun getLanguageKey(): String? {
        return commonAsset?.i_langCode()
    }

    private fun updateVideoOverlay(isAdOpen: Boolean) {
//        if (isAdOpen) {
//            overlayImageView.setImageResource(R.color.black_color)
//        } else {
//            val imageUrl = TvAppProvider.getInstance().tvAppInterface.getImageUrl(this.baseAsset!!)
//            Image.load(imageUrl).transform(BlurTransformation(4)).placeHolder(R.color.black_color).into(overlayImageView)
//        }
    }

    private fun hideThumbnailAndLoader() {
        hideLoader()
    }

    private fun resetFlags() {
        isAdViewed = false
        isAdPlaying = false
        isDetailShowing = false
        isNLFCRequestDone = false
        totalBufferTimer.reset()
        videoBufferTimeList.clear()
        videoLoadTime = 0
        totalBufferTime = 0
        isVideoReady = false
        videoLoadStartTime = 0L
        videoLoadTimeSCV = 0L
    }

    private fun updateView(postItem: CommonAsset) {
        Logger.d(LOG_TAG, "Update view is called for ${postItem.i_id()} pos : " + bindingAdapterPosition )
        if (commonAsset != null && commonAsset?.i_id() == postItem.i_id() && videoWrapper != null
                && videoWrapper?.getAutoplayVideoId().equals(postItem.i_id())) {
            Logger.d(LOG_TAG, "Returning as playerId are same")
            this.commonAsset = postItem
            return
        }
        if (this.commonAsset != null) {
            Logger.d(LOG_TAG, "Resetting dislike flags on new bind")
            resetFlags()
            if (videoWrapper != null) {
                this.commonAsset = postItem
                setVideoHeight()
            }
            videoReset()
        }

        if (isDetailShowing) {
            return
        }
        this.commonAsset = postItem

        this.commonAsset?.i_videoAsset()?.streamCachedDuration =
            ExoRequestHelper.getStreamCachedDuration(this.commonAsset?.i_id())

        Logger.d(LOG_TAG, "streamCachedDuration = " + this.commonAsset?.i_videoAsset()?.streamCachedDuration)

        val playerAsset = DHVideoUtils.getPlayerAsset(postItem)

        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels - 2 * context.resources.getDimensionPixelSize(R.dimen.story_card_padding_left)
        val maxContentHeight: Int = metrics.heightPixels - CommonUtils.getPixelFromDP(
                PlayerContants.TV_STATUS_BAR_HEIGHT * 2, context)
        val maxHeightRatio = PreferenceManager.getFloat(Constants.MAX_VIDEO_HEIGHT_RATIO, 1.0f)
        val newMaxHeight = min(maxContentHeight, (screenWidth * maxHeightRatio).toInt())

        val layoutParams = PlayerUtils.getScaledParamsForData(playerAsset, screenWidth, newMaxHeight)
        playerHeight = layoutParams.height

        viewBinding.body.newsImage.layoutParams.height = layoutParams.height
        viewBinding.body.newsImage.layoutParams.width = layoutParams.width

        viewBinding.body.mediaView.layoutParams.height = layoutParams.height

        if (isInCollection) {
            viewBinding.body.newsTitle.setLines(2)
        } else {
            viewBinding.body.newsTitle.maxLines = 3
        }

        if (videoWrapper == null) {
            Logger.i(LOG_TAG, "VideoPlayer view is null showing thumbnail")
            loadThumbnailImage()
            setLayoutChangeListener(viewBinding.body.newsImage, false)
            //TODO:: Vinod.bc, experimenting without video load on bind
            //loadPlayer()
            return
        }
        Logger.d(LOG_TAG, "updateView -> initVideoWrapper")
        initVideoWrapper()
    }

    protected fun loadThumbnailImage() {
        hideVideo()
    }

    private fun canPlayVideo(): Boolean {
        var menuIsVisible = autoPlayManager?.let { it.isMenuShown } ?: false
        return !(menuIsVisible || visiblePercentage < autoPlayVisibility || isVideoEnded())
    }

    private fun startVideoIfRequired() {
        Logger.d(LOG_TAG, "$bindingAdapterPosition startVideoIfRequired")
        var menuIsVisible = autoPlayManager?.let { it.isMenuShown } ?: false
        if (isDetailShowing || menuIsVisible || videoWrapper == null) {
            return
        }
        handlePlayerMuteStatus(false, false)
        val isAutoplayAllowed = AutoPlayHelper.isAutoPlayAllowed()
        Logger.d(VideoRequester.VIDEO_DEBUG, "Autoplay allowed is $isAutoplayAllowed")
        if (!isAutoplayAllowed) {
            Logger.d(LOG_TAG, "startVideoIfRequired : !isAutoplayAllowed")
            loadThumbnailImage()
            videoWrapper?.pause()
            return
        }

        Logger.d(LOG_TAG, "visibility percentage is $visiblePercentage")
        if (bindingAdapterPosition == 0 && visiblePercentage == 0) {
            Logger.d(LOG_TAG, "Handling of AutoPlay at First bindingAdapterPosition : $bindingAdapterPosition")
            //Handling First item as Autoplay i.e position 0 && user as not scrolled.
            visiblePercentage = calculateVisiblePer()
            Logger.d(LOG_TAG, "$bindingAdapterPosition After recaluclate visibilityPer:$visiblePercentage")
        }
        if (visiblePercentage < autoPlayVisibility) {
            Logger.d(LOG_TAG, "$bindingAdapterPosition startVideoIfRequired : visiblePercentage is less $visiblePercentage")
            Logger.d(LOG_TAG, "$bindingAdapterPosition startVideoIfRequired : Pausing Video !!")
            videoWrapper?.pause()
            commonVideoAnalyticsHelper.stopVideoPlayBackTimer()
            return
        }

        Logger.e(VideoRequester.VIDEO_DEBUG, "Video End ${isVideoEnded()} " +
                "Autoplay manager check is ${commonAsset?.i_id()}")
        Logger.d(LOG_TAG, "$bindingAdapterPosition startVideoIfRequired : isCurrentAsset : " +
                "${autoPlayManager?.isCurrentPlayingAsset(asset)}")

        if (!isVideoEnded() && autoPlayManager?.isCurrentPlayingAsset(asset) == true) {
            Logger.d(LOG_TAG, "$bindingAdapterPosition startVideoIfRequired : videoWrapper!!.resume")
            videoWrapper!!.resume()
        } else {
            Logger.d(LOG_TAG, "$bindingAdapterPosition startVideoIfRequired :  isVideoEnded")
            videoWrapper!!.pause()
            if (viewBinding.body.videoLyt.visibility == View.VISIBLE && !videoWrapper!!.isAdDisplaying()) {
                loadThumbnailImage()
            }
        }
    }

    override fun onMuteClick() {
        Logger.d(LOG_TAG, "onMuteClick")
        handlePlayerMuteStatus(true, true)
        commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.IS_MUTED.getName(), PlayerControlHelper.isListMuteMode.toString())
    }

    override fun onVideoInDetail() {
        NavigationHelper.autoplayInDetail.value = isDetailShowing
    }

    override fun onAutoPlayCardClick() {
        Logger.d(LOG_TAG, "onAutoPlayCardClick at $bindingAdapterPosition  postId : ${commonAsset?.i_id()}")
        isDetailShowing = true
        isEnteredDetail = true;
        topFragmentId = null
        if (!isVideoEnded()) {
            //AutoPlay Card Clicked
            videoWrapper?.pause()
        }

        if (parentLifecycleOwner != null) {
            //Resetting previouse values
            VideoHelper.topFragmentId.value = null
            VideoHelper.handleBackPressState.value = null
            VideoHelper.handleBackPressState.observe(parentLifecycleOwner, Observer {
                Logger.d(LOG_TAG, "handleBackPressState topFragmentId:  + $topFragmentId" +
                        " & it : $it , isDetailShowing: $isDetailShowing")
                if (it != null && it == topFragmentId) {
                    VideoHelper.handleBackPressState.removeObservers(parentLifecycleOwner)
                    VideoHelper.handleBackPressState.value = null
                    if (isDetailShowing) {
                        Logger.d(LOG_TAG, "handleBackPressState >> handleVideoBack")
                        handleVideoBack(null)
                    }
                }
            })

            VideoHelper.topFragmentId.observe(parentLifecycleOwner, Observer {
                if (it != null && isDetailShowing && topFragmentId == null) {
                    topFragmentId = it
                    Logger.d(LOG_TAG, "handleBackPressState setting topFragmentId : $topFragmentId")
                }
            })
        }
    }

    override fun loadThumbnailForBackup() {
        loadThumbnailImage()
    }

    override fun getNLFCRequestStatus(): Boolean {
        return isNLFCRequestDone
    }

    fun setNLFCRequestStatus(status: Boolean = false) {
        isNLFCRequestDone = status
    }

    override fun isAutoImmersiveMode(): Boolean {
        return autoImmersiveModeActive
    }

    override fun setImmersiveModeAsConsumed() {
        autoImmersiveModeActive = false
    }

    override fun handleVideoBack(videoAnalyticsHelper: Any?) {
        if (videoAnalyticsHelper is CommonVideoAnalyticsHelper) {
            commonVideoAnalyticsHelper = videoAnalyticsHelper
        }
        Logger.d(LOG_TAG, "handleVideoBack at $bindingAdapterPosition")

        isDetailShowing = false
        NavigationHelper.autoplayInDetail.value = isDetailShowing
        viewBinding.body.videoLyt.visibility = View.VISIBLE
        viewBinding.body.newsImage.visibility = View.GONE
        GlobalScope.launch(context = Dispatchers.Main) {
            if (!isInCollection)
                delay(100)
            initVideoWrapper()
        }
    }

    private fun handlePlayerMuteStatus(toggleMute: Boolean, isUserAction: Boolean) {
        val isMute = if (commonAsset?.i_videoAsset()?.hideControl == true ||
                commonAsset?.i_videoAsset()?.isGif == true)
            true
        else if (toggleMute) PlayerControlHelper.toggleMute()
        else PlayerControlHelper.isListMuteMode

        videoWrapper?.setPlayerMuteStatus(isMute, isUserAction, false)
        if (isMute) {
            viewBinding.body.muteButton.setImageResource(R.drawable.mute_button_unsel)
        } else {
            viewBinding.body.muteButton.setImageResource(R.drawable.mute_button_sel)
        }
        if (isUserAction) {
            BusProvider.getUIBusInstance().post(PlaySettingsChangedEvent(isMute, commonAsset!!.i_id()))
        }
        if (!isMute) {
            PlayerControlHelper.isDetailMuteMode = false
        }
    }

    private fun setVideoStartAction(startAction: PlayerVideoStartAction) {
        this.videoWrapper!!.setStartAction(startAction)
        commonVideoAnalyticsHelper.videoStartAction = startAction
        commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.START_ACTION.getName(), startAction.name)
    }

    private fun setVideoEndAction(endAction: PlayerVideoEndAction) {
        GlobalScope.launch(Dispatchers.IO) {
            videoWrapper?.setEndAction(endAction)
            if (isAdPlaying) {
                adsAnalyticsHelper.logVPEvent(endAction, videoWrapper?.currentDuration ?: 0)
            } else {
                commonVideoAnalyticsHelper.updateParam(
                        AnalyticsParam.INITIAL_LOAD_TIME.getName(), videoLoadTime.toString())
                commonVideoAnalyticsHelper.logVPEvent(endAction, videoWrapper?.currentDuration ?: 0,
                        commonAsset, eventSection)
            }
        }
    }

    override fun getVideoAnalyticsHelper(): Any {
        return commonVideoAnalyticsHelper
    }

    override fun updateAdditionCardParams(): Map<String, Any>? {
        val hashMap = HashMap<NhAnalyticsEventParam, Any?>()
        VideoAnalyticsHelper.addReferrerParams(hashMap, referrerFlow, referrerLead, pageReferrer)
        SearchAnalyticsHelper.addSearchParams(eventSection, hashMap)
        val map = VideoAnalyticsHelper.addCardParams(hashMap, commonAsset, true, true)
        VideoAnalyticsHelper.addExperimentParams(commonAsset, map)
        return map
    }

    override fun getExperiment(): Map<String, String>? {
        return commonAsset?.i_experiments()
    }

    override fun toggleUIForFullScreen(isFullScreen: Boolean) {
        Logger.d(LOG_TAG, "toggleUIForFullScreen")
    }

    override fun isVideoInNewsList(): Boolean {
        Logger.d(LOG_TAG, "")
        return true
    }

    override fun getVideoPlayerWrapper(): Any? {
        return videoWrapper
    }

    override fun setVideoPlayerWrapper(wrapper: Any?) {
        if (wrapper != null && wrapper is VideoPlayerWrapper) {
            videoWrapper = wrapper
        }
    }

    override fun getPageReferrer(): PageReferrer? {
        return pageRef
    }

    override fun getReferrerLead(): PageReferrer? {
        return pageReferrer
    }

    override fun getReferrerFlow(): PageReferrer? {
        return pageReferrer
    }

    override fun getReferrerRaw(): String? {
        return null
    }

    override fun getLifeCycleOwner(): LifecycleOwner? {
        return parentLifecycleOwner
    }

    override fun isViewInForeground(): Boolean {
        if (autoPlayManager?.isMenuShown == true) {
            Logger.d(LOG_TAG, "${bindingAdapterPosition} isViewInForeground Menu ${autoPlayManager?.isMenuShown}")
            return false
        }
        if (autoPlayManager?.isCurrentPlayingAsset(asset) == true) {
            Logger.d(LOG_TAG, "${bindingAdapterPosition} isViewInForeground >> ${canPlayVideo()} " +
                    "visiblePercentage = $visiblePercentage")
            return canPlayVideo()
        }
        Logger.d(LOG_TAG, "${bindingAdapterPosition} isViewInForeground View != AutoPlayManger")
        return false
    }


    override fun onUserLeftFragment() {
        Logger.d(LOG_TAG, "$bindingAdapterPosition<< onUserLeftFragment User left fragment for id ")
        uiHandler.removeCallbacksAndMessages(null)
        nlfHandler.removeCallbacksAndMessages(null)
        visiblePercentage = 0
        hasUserLeftFragment = true
        pauseVideo(PlayerVideoEndAction.SWIPE)
    }

    private fun calculateVisiblePer(): Int {
        return ViewUtils.getVisibilityPercentage(if (viewBinding.body.videoLyt.height > 0)
            viewBinding.body.videoLyt else viewBinding.body.newsImage)
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        Logger.d(LOG_TAG, "$bindingAdapterPosition  >> onUserEnteredFragment " +
                "percentage: $viewVisibilityPercentage")
        uiHandler.removeCallbacksAndMessages(null)
        hasUserLeftFragment = false
        if (isInCollection) {
            visiblePercentage = viewVisibilityPercentage
        } else {
            visiblePercentage = calculateVisiblePer()
        }
        if (viewVisibilityPercentage > 0) {
            contentAdDelegate?.onCardView(adAdapterPosition = adapterPosition)
        }
        if (isDetailShowing) {
            Logger.d(LOG_TAG, "${hashCode()}  >> onUserEnteredFragment " +
                    "isDetailShowing: $isDetailShowing")
            return
        }

        registerBus()
    }

    override fun onInVisible() {
        Logger.d(LOG_TAG, "$bindingAdapterPosition << onInVisible - On invisible fragment called")
        visiblePercentage = 0
        isViewHolderVisible = false

        uiHandler.removeCallbacksAndMessages(null)
        nlfHandler.removeCallbacksAndMessages(null)

        pauseVideo(PlayerVideoEndAction.SWIPE)
        unregisterBuses()
    }

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        super.onVisible(viewVisibilityPercentage, percentageOfScreen)
        Logger.d(LOG_TAG,
                "$bindingAdapterPosition >> onVisible Visibilty = $viewVisibilityPercentage on Screen  " +
                        "$percentageOfScreen")
        isViewHolderVisible = true
        if (hasUserLeftFragment) {
            Logger.d(LOG_TAG, "User has left the fragment do not update now")
            if (!AutoPlayHelper.isAutoPlayAllowed()) {
                loadThumbnailImage()
                if (videoWrapper != null) {
                    releaseVideo()
                }
            }
            return
        }
        visiblePercentage = calculateVisiblePer()
        Logger.d(LOG_TAG,
                "onVisible After calc visiblePercentage= $visiblePercentage")
        registerBus()
    }

    private fun setLayoutChangeListener(mediaView: View, onVideoBack: Boolean) {
        mediaView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int,
                                        oldTop: Int, oldRight: Int, oldBottom: Int) {
                Logger.d(LOG_TAG, "setLayoutChangeListener : onLayout change called for mediaview")
                if (onVideoBack && mediaView is VideoPlayerWrapper) {
                    Logger.d(LOG_TAG, "setLayoutChangeListener : startVideoIfRequired")
                    startVideoIfRequired()
                } else {
                    Logger.d(LOG_TAG, "setLayoutChangeListener : else")
                    viewBinding.rootView.minHeight = playerHeight + viewBinding.rootView.paddingTop +
                            viewBinding.rootView.paddingBottom + viewBinding.body.newsTitle.measuredHeight +
                            viewBinding.body.newsTitle.paddingTop
//                            if (viewBinding.hideContentBar.visibility == View.VISIBLE) {
//                                hideContentBar.measuredHeight
//                            } else {
//                                0
//                            }
                    Logger.d(LOG_TAG, "setLayoutChangeListener : minHeight : ${viewBinding.rootView.minHeight}")
                }
                mediaView.removeOnLayoutChangeListener(this)
            }
        })
    }

    private fun hideVideo() {
        uiHandler.removeCallbacksAndMessages(null)
        handlerImmersive.removeCallbacksAndMessages(null)
        nlfHandler.removeCallbacksAndMessages(null)
        Logger.d(LOG_TAG, "hiding video")
        viewBinding.body.videoProgressbar.visibility = View.GONE
        viewBinding.body.muteButton.visibility = View.GONE
        viewBinding.body.videoLyt.visibility = View.GONE
        viewBinding.body.newsImage.visibility = View.VISIBLE
        viewBinding.body.videoPlayIcon.visibility = View.VISIBLE
        viewBinding.body.videoViews.visibility = View.GONE
        showDuration(View.GONE)
    }

    private fun showDuration(visibility: Int) {
        if (isLive) {
            if (CardsBindUtils.canShowLiveTag(commonAsset)) {
                viewBinding.body.videoLiveTag.visibility = visibility
            } else {
                viewBinding.body.videoLiveTag.visibility = View.GONE
            }
            viewBinding.body.videoDuration.visibility = View.GONE
        } else {
            viewBinding.body.videoLiveTag.visibility = View.GONE
            if (!isHideCintrol)
                viewBinding.body.videoDuration.visibility = visibility
        }
    }

    private fun showVideo() {
        Logger.d(LOG_TAG, "showing video")
        val isAdOpen = videoWrapper?.isAdDisplaying() ?: false
        updateVideoOverlay(isAdOpen)
        hideLoader()
        viewBinding.body.videoLyt.visibility = View.VISIBLE
        if (!DHVideoUtils.isExoPlayer(commonAsset?.i_videoAsset())) {
            viewBinding.body.newsImage.visibility = View.GONE
        }
        viewBinding.body.videoPlayIcon.visibility = View.GONE
        if (!isHideCintrol) {
            viewBinding.body.muteButton.visibility = View.VISIBLE
             if(!isLive) {
                 if(CommonUtils.isEmpty(CardsBindUtils.getJustViewCount(commonAsset?.i_counts()))) {
                     viewBinding.body.videoViews.visibility = View.GONE
                 } else {
                     viewBinding.body.videoViews.visibility = View.VISIBLE
                     viewBinding.body.videoViews.setSpannableTextWithLangSpecificTypeFaceChanges(
                             CardsBindUtils.getJustViewCount(commonAsset?.i_counts()),
                             TextView.BufferType.SPANNABLE, commonAsset?.i_langCode() )
                 }
             }  else {
                 viewBinding.body.videoViews.visibility = View.GONE
             }
        }
        videoWrapper.let {
            if (videoWrapper is ExoPlayerWrapper2 && (videoWrapper as ExoPlayerWrapper2).getPlayerState() == PLAYER_STATE.STATE_BUFFERING) {
                showLoader()
            }
        }
    }

    fun onPlaySettingsChanged(event: PlaySettingsChangedEvent) {
        if (commonAsset?.i_id() == event.id || commonAsset?.i_videoAsset()?.hideControl == true ||
                commonAsset?.i_videoAsset()?.isGif == true) return
        PlayerControlHelper.isListMuteMode = event.isMute
        handlePlayerMuteStatus(toggleMute = false, isUserAction = false)
    }

    private fun isVideoEnded(): Boolean {
        return videoWrapper?.hasVideoEnded() == true
    }

    fun onConnectivityChanged(connectionSpeedEvent: ConnectionSpeedEvent) {
        Logger.d(LOG_TAG, "onConnectivityChanged at pos : $bindingAdapterPosition")
        //Delay is used to get the updated status of connection
        uiHandler.postDelayed({
            if (connectionSpeedEvent?.connectionSpeed == ConnectionSpeed.NO_CONNECTION) {
                if(!isPrefetchedContentAvailable()) {
                    pauseVideo(PlayerVideoEndAction.PAUSE)
                    loadThumbnailImage()
                }
            } else if (!AutoPlayHelper.isAutoPlayAllowed()) {
                pauseVideo(PlayerVideoEndAction.PAUSE)
                loadThumbnailImage()
            } else if (AutoPlayHelper.isAutoPlayAllowed() && !isVideoEnded()) {
                if(autoPlayManager?.isCurrentPlayingAsset(asset) == true) {
                    Logger.d(LOG_TAG, "onConnectivityChanged at pos : $bindingAdapterPosition, video resuming")
                    play()
                } else if(autoPlayManager?.isCurrentPlayingViewCreated() == false) {
                    Logger.d(LOG_TAG, "onConnectivityChanged at pos : $bindingAdapterPosition, player view was not created - create now")
                    autoPlayManager?.updateFocusedPlayer()
                }
            }
        }, 100)
    }

    private fun isPrefetchedContentAvailable(): Boolean {
        val playedDuration = videoWrapper?.currentDuration ?: 0
        if(commonAsset?.i_videoAsset()?.streamCachedDuration == 0F) {
            commonAsset?.i_videoAsset()?.streamCachedDuration =
                ExoRequestHelper.getStreamCachedDuration(this.commonAsset?.i_id())
        }
        val cachedDuration = commonAsset?.i_videoAsset()?.streamCachedDuration ?: 0F
        if((playedDuration / 1000) < cachedDuration) {
            return true
        }
        return false
    }

    fun onReceive(callState: CallState) {
        if (commonAsset == null || videoWrapper == null /*|| commonVideoRequester.screenId != callState.screenId*/) {
            // because the video is not initialized
            return
        }
        when (callState.state) {
            TelephonyManager.CALL_STATE_RINGING, TelephonyManager.CALL_STATE_OFFHOOK -> {
                pauseVideo(PlayerVideoEndAction.PAUSE)
                loadThumbnailImage()
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // let the user start the video
                startVideoIfRequired()
            }
            else -> {
            }
        }
    }

    override fun onDisplayClick() {
        // Do nothing
    }

    override fun onRequestChangeOrientation(orientation: Int) {
        val activity: AppCompatActivity = itemView1.context as AppCompatActivity
        activity.requestedOrientation = orientation
    }

    private fun onVideoError() {
        Logger.d(LOG_TAG, "onVideoError")
        if(!isVideoReady && videoLoadTimeSCV == 0L && videoLoadStartTime != 0L) {
            //If video has not started playing
            videoLoadTimeSCV = System.currentTimeMillis() - videoLoadStartTime
        }
        uiHandler.post { loadThumbnailImage() }
        videoLoadError()
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_ERROR, commonAsset?.i_id())
        Logger.d(VIDEO_LOAD_TIME_TAG, "onVideoError videoLoadTimeSCV : $videoLoadTimeSCV")
    }

    abstract fun videoLoaded()

    abstract fun videoLoadError()

    open fun videoReset() {
        Logger.d(LOG_TAG, "videoReset > player release $bindingAdapterPosition")
        releasePlayer()
        videoWrapper = null
    }

    private fun onVideoReady() {
        videoLoadTime = System.currentTimeMillis() - videoStartTime
        videoLoadTimeSCV = videoLoadTime
        videoStartTime = System.currentTimeMillis()
        Logger.d(LOG_TAG, "onVideoReady is called")
        Logger.d(VIDEO_LOAD_TIME_TAG, "onVideoReady videoLoadTimeSCV : $videoLoadTimeSCV")
        isVideoReady = true
        videoLoaded()
        initVideoWrapper()
    }

    private fun setVideoHeight() {
        val playerAsset = DHVideoUtils.getPlayerAsset(commonAsset)
        val metrics = context.resources.displayMetrics
        val parentWidth = metrics.widthPixels - 2 * context.resources.getDimensionPixelSize(R.dimen.story_card_padding_left)
        val maxContentHeight: Int = metrics.heightPixels - CommonUtils.getPixelFromDP(
                PlayerContants.TV_STATUS_BAR_HEIGHT * 2, context)
        val maxHeightRatio = PreferenceManager.getFloat(Constants.MAX_VIDEO_HEIGHT_RATIO, 1.0f)
        val newMaxHeight = min(maxContentHeight, (parentWidth * maxHeightRatio).toInt())

        val layoutParams = PlayerUtils.getScaledParamsForData(playerAsset, parentWidth, newMaxHeight)

        videoWrapper!!.removeFromParent()
        videoWrapper!!.setPageReferrer(pageReferrer)
        videoWrapper!!.setLayoutParamsForWrapper(ConstraintLayout.LayoutParams(parentWidth, layoutParams.height))
    }

    private fun initVideoWrapper() {
        if (videoWrapper == null) return // this can happen if the video is released

        if(!CommonUtils.isNetworkAvailable(CommonUtils.getApplication()) &&
            !isPrefetchedContentAvailable()) {
            Logger.d(LOG_TAG, "isEligibleToPrefetch == false && isNetworkAvailable == false")
            loadThumbnailImage()
            videoWrapper?.pause()
            return
        }

        Logger.d(LOG_TAG, "init video wrapper function pos : $bindingAdapterPosition")
        setVideoHeight()
        setPlayerCallbacks()

        val mediaView = videoWrapper!!.getPlayerView()
        updateVideoOverlay(videoWrapper?.isAdDisplaying() == true)

        videoWrapper!!.resetCallbacks(this, this)
        videoWrapper!!.setVideoTimeListener(this)
        if (AutoPlayHelper.isAutoPlayAllowed()) {
            setVideoStartAction(PlayerVideoStartAction.AUTOPLAY)
        } else {
            setVideoStartAction(PlayerVideoStartAction.CLICK)
        }

        videoWrapper!!.onAttachToNewsList()
        Logger.i(LOG_TAG, "attaching the video view in view holder $videoWrapper")
        viewBinding.body.videoLyt.removeAllViews()
        mediaView.visibility = View.VISIBLE
        viewBinding.body.videoLyt.addView(mediaView)

        visiblePercentage = calculateVisiblePer()

        if (!AutoPlayHelper.isAutoPlayAllowed() || isVideoEnded()) {
            Logger.d(LOG_TAG, "initVideoWrapper - looks like video ended at : $bindingAdapterPosition")
            loadThumbnailImage()
            pauseVideo(PlayerVideoEndAction.COMPLETE)
            setLayoutChangeListener(viewBinding.body.newsImage, false)
        } else {
            Logger.d(LOG_TAG, "initVideoWrapper - showVideo at : $bindingAdapterPosition")
            showVideo()
            setLayoutChangeListener(mediaView, false)
            startVideoIfRequired()
        }
    }

    private fun displayCacheLogs() {
        if(!Logger.loggerEnabled() || !isEnablePrefetchLogs) {
            return
        }

        if (!viewBinding.body.exoLogsParent.isShown) {
            viewBinding.body.exoLogsParent.visibility = View.VISIBLE
        }

        var connectionSpeed = BwEstRepo.currentConnectionQuality()
        val isEligibleToPrefetch = DHVideoUtils.isEligibleToPrefetchInDetail(commonAsset)
        val cachedUrl = if(isEligibleToPrefetch) ExoRequestHelper.getStreamCachedUrl(commonAsset?.i_id()) else null
        viewBinding.body.isPrefetched.text = "IsEligibleForPrefetch =  ${DHVideoUtils.isEligibleToPrefetchInDetail(commonAsset)}" +
                ",  IsCached =  ${ExoRequestHelper.isItemPrefetched(commonAsset?.i_id())} , downloadError + ${commonAsset?.i_videoAsset()?.downloadErrorMsg}" +
            "\nId =  ${commonAsset?.i_id()},  selectedQuality =  ${ExoRequestHelper.getSelectedQuality(commonAsset?.i_id())},  Conn Info =  $connectionSpeed" +
                "\n\nDisableCacheConfig =  ${CacheConfigHelper.disableCache}" +
                ",  IsPrefetchConfig =  ${commonAsset?.i_videoAsset()?.isPrefetch}" +
                ",  IsAdded =  ${ExoRequestHelper.isItemAdded(commonAsset?.i_id())}"

        viewBinding.body.prefetchPercentageDuration.text =
                    "PrefetchDuration Config =  ${CacheConfigHelper.getPrefetchDurationConfig(commonAsset?.i_videoAsset())}" +
                    ",  CachedDuration =  ${ExoRequestHelper.getStreamCachedDuration(commonAsset?.i_id())}"

        if (section == PageSection.NEWS.section) {
            viewBinding.body.listPrefetchConfig.text =
                "News List Prefetch" +
                        "\nConfig count =  ${CacheConfigHelper.getNumberOfVideoToPrefetch(ConfigType.NEWS_LIST, connectionSpeed)}" +
                        ", Added count =  ${ExoRequestHelper.getItemCount(ConfigType.NEWS_LIST)}" +
                        ", Cached count =  ${ExoRequestHelper.getCompletedPrefetchCount(ConfigType.NEWS_LIST)}"
        } else {
            viewBinding.body.listPrefetchConfig.text =
                "Buzz List Prefetch" +
                        "\nConfig count:  ${CacheConfigHelper.getNumberOfVideoToPrefetch(ConfigType.BUZZ_LIST, connectionSpeed)}" +
                        ", Added count :  ${ExoRequestHelper.getItemCount(ConfigType.BUZZ_LIST)}" +
                        ", Cached count:  ${ExoRequestHelper.getCompletedPrefetchCount(ConfigType.BUZZ_LIST)}"
        }

        viewBinding.body.listPrefetchConfig.text = viewBinding.body.listPrefetchConfig.text.toString() +
        "\n\nVideo url =  ${commonAsset?.i_videoAsset()?.url}" +
                "\nCached url =  $cachedUrl"
    }

    private fun displayBandwidthLogs() {
        if(!Logger.loggerEnabled() || !isEnableBandwidthLogs) {
            return
        }

        if (!viewBinding.body.exoLogsParent.isShown) {
            viewBinding.body.exoLogsParent.visibility = View.VISIBLE
        }
        val curCQParams = BwEstRepo.INST?.curCQParams()

        viewBinding.body.isPrefetched.text = "\nBandwidth Estimation logs"

        val bandwidthStr = "\nN/w Sdk =   " + (curCQParams?.fbBitrate) + " Kbps" +
                "\nExo Bitrate =   " + (curCQParams?.exoBitrate) + " Kbps" +
                "\nFormula =   ${curCQParams?.exoBitrate} * ${BwEstCfgDataProvider.exoWeightage} " +
                "+ ${(curCQParams?.fbBitrate)} * ${BwEstCfgDataProvider.networkWeightage}" +
                "\nCalculated Bitrate =   " + (curCQParams?.resultBitrate) + " Kbps" +
                "\nConnectionSpeed =   " + curCQParams?.resultBitrateQuality;
        viewBinding.body.prefetchPercentageDuration.text = bandwidthStr

    }

    abstract fun setPlayerCallbacks()

    abstract fun releasePlayer()

    override fun recycleView() {
        Logger.i(VideoRequester.VIDEO_DEBUG, "Recycle view is called for $bindingAdapterPosition")
        pauseVideo(PlayerVideoEndAction.SCROLL)
        unregisterBuses()
        visiblePercentage = 0
    }

    override fun showLoader() {
        if (isVideoEnded()) {
            hideLoader()
            return
        }
        uiHandler.post {
            viewBinding.body.videoProgressbar.visibility = View.VISIBLE
        }
    }

    override fun hideLoader() {
//        uiHandler.post {
            viewBinding.body.videoProgressbar.visibility = View.GONE
//        }
    }

    abstract fun createPlayer()


    private fun loadPlayer() {
//        if (!canLoadPlayerForGo()) {
//            return
//        }
        if (videoWrapper != null) {
            Logger.d(LOG_TAG, "loadPlayer, return as videoWrapper != null")
            return
        }
        Logger.d(LOG_TAG, "loadPlayer at $bindingAdapterPosition")
        showLoader()
        createPlayer()
        videoStartTime = System.currentTimeMillis()
        videoLoadStartTime = System.currentTimeMillis()
    }

    protected fun observeLiveData() {
        observePlayerStateLiveData()
        observeUserAction()
    }

    private fun observeUserAction() {
        if (parentLifecycleOwner != null) {
            UserActionHelper.userActionLiveData.observe(parentLifecycleOwner, Observer {
                if (!isDetailShowing) {
                    if (it == NhAnalyticsUserAction.NORMAL_EXIT) {
                        setVideoEndAction(PlayerVideoEndAction.APP_EXIT)
                    } else if (it == NhAnalyticsUserAction.MINIMIZE) {
                        setVideoEndAction(PlayerVideoEndAction.MINIMIZE)
                    }
                }
            })
        }
    }

    private fun observePlayerStateLiveData() {
        if (videoWrapper == null) {
            Logger.d(LOG_TAG, "loadPlayer, return as videoWrapper == null")
            return
        }
        if (parentLifecycleOwner != null) {
            Logger.d(LOG_TAG, "loadPlayer observing playerStateLiveData : ${videoWrapper?.playerStateLiveData}")
            videoWrapper?.playerStateLiveData?.observe(parentLifecycleOwner, Observer {
                handlePlayerState(it)
            })
        }
    }

    private fun handlePlayerState(playerEvent: PlayerEvent) {
        if (isDetailShowing) {
            return
        }
        Logger.d(LOG_TAG, "handlePlayerState - ${playerEvent.playerState} -> $bindingAdapterPosition")
        if (playerEvent.id != commonAsset?.i_id()) {
            Logger.d(LOG_TAG, "handlePlayerState - return as Id not equal")
            Logger.d(LOG_TAG, "handlePlayerState ${playerEvent.id} != ${commonAsset?.i_id()}")
            return
        }
        when (playerEvent.playerState) {
            PLAYER_STATE.STATE_BUFFERING -> {
                onBuffering(true)
                showLoader()
            }
            PLAYER_STATE.STATE_READY -> onVideoReady()
            PLAYER_STATE.STATE_VIDEO_START -> onVideoStart()
            PLAYER_STATE.STATE_PLAYING -> onVideoPlaying()
            PLAYER_STATE.STATE_PAUSED -> onVideoPaused(PlayerVideoEndAction.PAUSE)
            PLAYER_STATE.STATE_VIDEO_END -> onVideoEnd()
            PLAYER_STATE.STATE_ERROR -> onVideoError()
            PLAYER_STATE.STATE_AD_START -> onAdStart()
            PLAYER_STATE.STATE_AD_END -> onAdEnd()
            PLAYER_STATE.STATE_AD_CLICK -> onAdClick()
        }
    }

    private fun onBuffering(isBufferStart: Boolean) {
        if (isBufferStart) {
            totalBufferTimer.start()
            Logger.d(VIDEO_BUFFER_TIME_TAG, "totalBufferTime onBufferStart: " + totalBufferTimer.totalTime)
        } else {
            totalBufferTimer.stop()
            totalBufferTime = totalBufferTimer?.totalTime
            if(totalBufferTime > 0) {
                videoBufferTimeList.add(totalBufferTime)
                commonVideoAnalyticsHelper?.updateBufferDuration(totalBufferTime)
                totalBufferTimer.reset()
                Logger.d(VIDEO_BUFFER_TIME_TAG, "totalBufferTime onBufferStop: " + totalBufferTime)
            }
        }
    }

    private fun onVideoPlaying() {
        showVideo()
        startVideoEvent()
        onBuffering(false)
        uiHandler.removeCallbacksAndMessages(null)
        Logger.d(LOG_TAG,"Starting auto immersive mode timmer from onVideoPlaying")
        startImmersiveModeTimer()
        if (DHVideoUtils.isEmbedPlayer(commonAsset)) {
            startNlfTimer()
        }
    }

    private fun onAdPaused(endAction: PlayerVideoEndAction) {
        setVideoEndAction(endAction)
    }

    private fun onAdResumed() {
        startAdEvent()
    }

    private fun onAdEnd() {
        adsAnalyticsHelper.logVPEvent(PlayerVideoEndAction.COMPLETE,
                videoWrapper?.currentDuration ?: 0)
        commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.IS_AD_PLAYING.getName(), false.toString())
        isAdPlaying = false
        // check if video has ended too and call below
        if (isVideoEnded()) {
            loadThumbnailImage()
        }
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_AD_END, commonAsset?.i_id())
    }

    private fun onAdClick() {
        Logger.d(LOG_TAG, "onAdClick")
        viewBinding.body.constraintLyt.performClick()
    }

    private fun onAdStart() {
        setVideoEndAction(PlayerVideoEndAction.AD_START)
        hideThumbnailAndLoader()
        updateVideoOverlay(true)
        showDuration(View.GONE)

        startAdEvent()
        isAdPlaying = true
        isAdViewed = true
        commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.IS_AD_PLAYING.getName(), true.toString())
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_AD_START, commonAsset?.i_id())
    }

    private fun onVideoPaused(endAction: PlayerVideoEndAction) {
//        setVideoEndAction(endAction)
        nlfHandler.removeCallbacksAndMessages(null)
    }

    private fun onVideoStart() {
        if (!DHVideoUtils.isExoPlayer(commonAsset?.i_videoAsset())) {
            hideThumbnailAndLoader()
        }
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_VIDEO_START, commonAsset?.i_id())
        //If video item player type is embedded player or youtube player initialize immersive mode
        if (!isAutoImmersiveMode && commonAsset?.i_videoAsset()?.playerType in listOf(
                        PlayerType.DH_EMBED_WEBPLAYER.name, PlayerType.YOUTUBE.name, PlayerType.DH_WEBPLAYER.name)) {
            Logger.d(LOG_TAG,"Starting auto immersive mode timmer from onStart")
            startImmersiveModeTimer()
        }
        if (DHVideoUtils.isEmbedPlayer(commonAsset)) {
            startNlfTimer()
            startVideoEvent()
            onBuffering(false)
        }
    }


    private fun startVideoEvent() {
        GlobalScope.launch(Dispatchers.IO) {
            commonVideoAnalyticsHelper.startVPEvent(videoWrapper?.currentDuration ?: 0,
                    videoLoadTime, updateAdditionCardParams() as MutableMap<String, Any>)

            Logger.d(VIDEO_LOAD_TIME_TAG, "startVideoEvent videoLoadTime : " + videoLoadTime)
            commonVideoAnalyticsHelper.updateParam(
                    PlayerAnalyticsEventParams.AD_VIEWED.getName(), isAdViewed.toString())
            if (!isEnteredDetail) {
                commonVideoAnalyticsHelper.updateParam(AnalyticsParam.IN_DETAIL.getName(), false.toString())
            }
            commonVideoAnalyticsHelper.updateParam(
                    PlayerAnalyticsEventParams.IS_MUTED.getName(), PlayerControlHelper.isListMuteMode.toString())
            commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.IS_PREFETCH.getName(), DHVideoUtils.isEligibleToPrefetch(commonAsset).toString())
            commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.IS_CACHED.getName(), ExoRequestHelper.isItemPrefetched(commonAsset?.i_id()).toString())
            commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.USER_CONNECTION_QUALITY_SELECTED.getName(), commonAsset?.i_videoAsset()?.selectedQuality)
            commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.DISABLE_CACHE.getName(), CacheConfigHelper.disableCache.toString())
            commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.DISABLE_CACHE.getName(), CacheConfigHelper.disableCache.toString())
            commonVideoAnalyticsHelper.updateParam(
                PlayerAnalyticsEventParams.CACHED_VIDEO_URL.getName(), ExoRequestHelper.getStreamCachedUrl(commonAsset?.i_id()))
            commonVideoAnalyticsHelper.updateParam(PlayerAnalyticsEventParams.CACHED_DURATION.getName(),
                ExoRequestHelper.getStreamCachedDuration(commonAsset?.i_id()).toString())
            commonVideoAnalyticsHelper.updateParam(PlayerAnalyticsEventParams.CACHED_PERCENTAGE.getName(),
                ExoRequestHelper.getStreamCachedDuration(commonAsset?.i_id()).toString())

            if (isInCollection && commonAsset?.i_parentPostId() != null) {
                commonVideoAnalyticsHelper.updateParam(AnalyticsParam.COLLECTION_ID.name, commonAsset?.i_parentPostId())
                commonVideoAnalyticsHelper.updateParam(AnalyticsParam.COLLECTION_TYPE.name, Constants.MM_CAROUSEL)
            }
        }
    }

    private fun startAdEvent() {
        val hashMap = HashMap<NhAnalyticsEventParam, Any?>()
        VideoAnalyticsHelper.addReferrerParams(hashMap, referrerFlow, referrerLead, pageReferrer)
        val map = VideoAnalyticsHelper.addCardParams(hashMap, commonAsset, false, false, true)
        adsAnalyticsHelper.startVPEvent(videoWrapper?.currentDuration
                ?: 0, map as MutableMap<String, Any>)
    }

    private fun onVideoEnd() {
        loadThumbnailImage()
        VideoHelper.videoStateLiveData.value = PlayerState(PLAYER_STATE.STATE_VIDEO_END, commonAsset?.i_id())
    }

    override fun onAttachToWindow() {
        // do nothing
    }

    override fun onRemoveFromWindow() {
        if (isDetailShowing) return
        pauseVideo(PlayerVideoEndAction.SCROLL)
    }


    //AutoPlay management
    override val asset: Any?
        get() = commonAsset

    override fun getAutoplayPriority(fresh: Boolean): Int {
        if (commonAsset == null) {
            return -1
        }
        if (fresh) {
            visiblePercentage = ViewUtils.getVisibilityPercentage(
                    if (viewBinding.body.videoLyt.isShown && viewBinding.body.videoLyt.height > 0)
                        viewBinding.body.videoLyt else viewBinding.body.newsImage)
//            Logger.d(LOG_TAG, "getAutoplayPriority, pos : $bindingAdapterPosition")
//            Logger.d(LOG_TAG, "getAutoplayPriority, visiblePercentage : $visiblePercentage")
//            Logger.d(LOG_TAG, "getAutoplayPriority, autoPlayVisibility : $autoPlayVisibility")
        }
        // Check for autoplay eligibility
        return if (canPlayVideo()) {
            visiblePercentage * 2
        } else -1
    }

    override fun setAutoPlayManager(autoPlayManager: AutoPlayManager?) {
        this.autoPlayManager = autoPlayManager
    }

    fun getAutoPlayManager(): AutoPlayManager? {
        return autoPlayManager
    }

    fun startImmersiveModeTimer() {
        uiHandler.removeCallbacksAndMessages(null)
        if (autoImmersiveTimeSpan.toInt() > 0 && autoPlayManager?.isCurrentPlayingAsset(asset) == true) {
            handlerImmersive.removeCallbacksAndMessages(null)
            runnableImmersive = Runnable {
                autoImmersiveModeActive = true
                cardsViewModel.onAutoPlayVideoClick(viewBinding.body.constraintLyt, commonAsset,
                        parentItem, this@AbstractAutoplayViewHolder, contentAdDelegate)
            }

            runnableImmersiveTimeSpan = Runnable {
                if (!isDetailShowing) {
                    uiHandler.post(runnableImmersive)
                }
            }
            handlerImmersive.postDelayed(runnableImmersiveTimeSpan, autoImmersiveTimeSpan!!)
        }

    }


    override fun play() {
        Logger.d(LOG_TAG, "Play from the Autoplay manager $bindingAdapterPosition, visible : $isViewHolderVisible")
        if (!AutoPlayHelper.isAutoPlayAllowed() || isDetailShowing || (!isInCollection && !isViewHolderVisible)) {
            Logger.d(LOG_TAG, "Play() << return,  visible : $isViewHolderVisible")
            loadThumbnailImage()
            videoWrapper?.pause()
            return
        }
        if(!CommonUtils.isNetworkAvailable(CommonUtils.getApplication()) &&
            !isPrefetchedContentAvailable()) {
            Logger.d(LOG_TAG, "isEligibleToPrefetch == false && isNetworkAvailable == false")
            loadThumbnailImage()
            videoWrapper?.pause()
            return
        }

        if (videoWrapper != null &&
                !videoWrapper?.getAutoplayVideoId().equals(commonAsset?.i_id())) {
            Logger.i(LOG_TAG, "Video wrapper is different, Reset player >>")
            videoReset()
        }

        if (videoWrapper == null) {
            Logger.i(LOG_TAG, "Video wrapper is null loadPlayer >>")
            loadPlayer()
            initVideoWrapper()
        } else if (videoWrapper?.getParentView() != viewBinding.body.videoLyt) {
            initVideoWrapper()
        } else if (!isVideoEnded() && !isFragmentPaused) {
            videoWrapper?.setVideoTimeListener(this)
            videoWrapper?.resume()
            showVideo()
            Logger.d(LOG_TAG,"Start Immersive from play()")
            startImmersiveModeTimer()
        }
        handlePlayerMuteStatus(false, false)
    }

    /**
     * When multiple autoplay videos are visible, this pause method is triggered for a video
     */
    override fun pause() {
        Logger.d(LOG_TAG, "pause from the Autoplay manager $bindingAdapterPosition")

        uiHandler.removeCallbacksAndMessages(null)
        nlfHandler.removeCallbacksAndMessages(null)

        pauseVideo(PlayerVideoEndAction.SCROLL)
    }

    override fun onVideoResumed() {
        setVideoStartAction(PlayerVideoStartAction.RESUME)
    }

    override fun releaseVideo() {
        Logger.d(LOG_TAG, ">> releaseVideo at $bindingAdapterPosition")
        releasePlayer()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        super.onDestroyCb()
        Logger.d(LOG_TAG, "onDestroy is called hence releasing the video")
        commonVideoAnalyticsHelper.onFragmentDestroy()
        adsAnalyticsHelper.onFragmentDestroy()
        releaseVideo()
        viewBinding.body.videoLyt.removeAllViews()

        uiHandler.removeCallbacksAndMessages(null)
        handlerImmersive.removeCallbacksAndMessages(null)
        nlfHandler.removeCallbacksAndMessages(null)

        unregisterBuses()
        parentLifecycleOwner?.lifecycle?.removeObserver(this)
    }

    private fun registerBus() {
        if (!isBusRegistered) {
            BusProvider.getUIBusInstance().register(this)
            NetworkSDK.bus().register(this)
            isBusRegistered = true
        }
    }

    private fun unregisterBuses() {
        try {
            if (isBusRegistered) {
                BusProvider.getUIBusInstance().unregister(this)
                NetworkSDK.bus().unregister(this)
                isBusRegistered = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getVisibilityPercentage(): Int {
        return visiblePercentage
    }

    override fun onTimeUpdate(time: String?, position: Long) {
        nlfHandler.removeCallbacksAndMessages(null)
        displayCacheLogs()
        if (CommonUtils.isEmpty(time) || !showTimeLeft /*|| videoWrapper?.isAdDisplaying() == true*/
                || (viewBinding.body.newsImage.visibility == View.VISIBLE &&
                        viewBinding.body.videoLyt.visibility == View.GONE)) {
            showDuration(View.GONE)
            return
        }
        uiHandler.post {
            showDuration(View.VISIBLE)
            viewBinding.body.videoDuration.setSpannableTextWithLangSpecificTypeFaceChanges(time,TextView.BufferType.SPANNABLE, viewBinding.item?.i_langCode())
            if (viewBinding.body.videoProgressbar.isVisible && videoWrapper?.isPlaying() == true) {
                hideLoader()
            }
        }

        if (!isInCollection && !isNLFCRequestDone && nonLinearRequestTime > 0 &&
                position >= nonLinearRequestTime * 1000 && !CommonUtils.isEmpty(nonLinearFeedUrl)) {
            requestForNlf()
        }
    }

    private fun requestForNlf() {
        if (!isInCollection && !isNLFCRequestDone && nonLinearRequestTime > 0 &&
                !CommonUtils.isEmpty(nonLinearFeedUrl)) {
            Logger.d(LOG_TAG, "NLFC requested - " + nonLinearFeedUrl)
            isNLFCRequestDone = true
            cardsViewModel.updateNLFCAsset(commonAsset, bindingAdapterPosition)
            cardsViewModel.getNonLinearFeedCard(nonLinearFeedUrl!!, commonAsset?.i_id()
                    ?: Constants.EMPTY_STRING)
        }
    }

    private fun startNlfTimer() {
        if (!isInCollection && !isNLFCRequestDone && nonLinearRequestTime > 0 &&
                !CommonUtils.isEmpty(nonLinearFeedUrl)) {
            val timerDelay: Long = (nonLinearRequestTime * 1000).toLong()
            nlfHandler.removeCallbacksAndMessages(null)
            nlfHandler.postDelayed(Runnable {
                kotlin.run {
                    requestForNlf()
                }
            }, timerDelay)
        }
    }


    override fun showTimeLeft(isShowRemaingTime: Boolean) {
        showTimeLeft = isShowRemaingTime
    }

    override fun getPositionInList(): Int {
        return bindingAdapterPosition
    }

    override fun resetVideoState() {
        loadThumbnailImage()
        videoWrapper = null
    }

    fun canLoadPlayerForGo(): Boolean {
        if (!AppConfig.getInstance().isGoBuild) {
            return true
        }
        return false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        isFragmentPaused = true
        nlfHandler.removeCallbacksAndMessages(null)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        super.onStopCb()
        Logger.d(LOG_TAG, "onStop $bindingAdapterPosition")
        if (isDetailShowing) {
            videoWrapper?.pauseWithOutAction()
        } else {
            videoWrapper?.releaseAndSetReload()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        super.onResumeCb()
        Logger.d(LOG_TAG, "onResume $bindingAdapterPosition")
        isFragmentPaused = false
        adsTimeSpentOnLPHelper?.stopAdsTimeSpentOnLPTimerAndTriggerEvent()
    }

    override fun getVideoBufferTime(): String {
        //Format - "100,260,300"
        var bufferTimeStr = ""
        for(index in 0 until videoBufferTimeList.size) {
            bufferTimeStr = bufferTimeStr + "" + videoBufferTimeList[index]
            if(index < videoBufferTimeList.size - 1) {
                bufferTimeStr += ","
            }
        }
        Logger.d(VIDEO_BUFFER_TIME_TAG, "bufferTimeStr : " + bufferTimeStr)
        return bufferTimeStr
    }

    override fun getVideoLoadTime(): String {
        if(!isVideoReady && videoLoadTimeSCV == 0L && videoLoadStartTime != 0L) {
            //If video has not started playing
            videoLoadTimeSCV = System.currentTimeMillis() - videoLoadStartTime
        }
        Logger.d(VIDEO_LOAD_TIME_TAG, "pos : $bindingAdapterPosition getVideoLoadTime() : $videoLoadTimeSCV")
        return "" + videoLoadTimeSCV
    }

    fun getSharedElementView(): View {
        return viewBinding.body.mediaView
    }

    override fun onRenderedFirstFrame() {
        super.onRenderedFirstFrame()
    }
}

interface AutoPlayMuteClickListener {
    fun onMuteClick() {}
}