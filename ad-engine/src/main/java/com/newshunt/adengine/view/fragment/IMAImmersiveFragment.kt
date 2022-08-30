/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/

package com.newshunt.adengine.view.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ViewFlipper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.dailyhunt.tv.ima.player.exo.VideoPlayerWithAdPlayback
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeData
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsOpenUtility
import com.newshunt.app.helper.AdsTimeSpentOnLPHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dhutil.children
import com.newshunt.dhutil.scale

/**
 * @author Rahul Ravindran
 */
class IMAImmersiveFragment(val impressionReporter: AsyncAdImpressionReporter, val adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper?): DialogFragment() {
    private lateinit var immersiveContainer: LinearLayout
    companion object {
        val TAG = IMAImmersiveFragment::class.java.simpleName
        val AD_ENTITY = "AD_ENTITY"
        val NATIVE_ENTITY = "NATIVE_ENTITY"

        fun instance(bundle: Bundle,
                     asyncAdImpressionReporter: AsyncAdImpressionReporter, adsTimeSpentOnLPHelper: AdsTimeSpentOnLPHelper?): IMAImmersiveFragment {
            return IMAImmersiveFragment(asyncAdImpressionReporter, adsTimeSpentOnLPHelper).apply {
                arguments = bundle
            }
        }
    }

    var getSharedView: View? = null
        get() {
            val shared = immersiveContainer.getChildAt(0)
            immersiveContainer.removeView(shared)
            return shared
        }
    private var item: NativeData? = null
    private var adEntity: ExternalSdkAd? = null
    var dismissListenerCallback: DismissListenerImmersiveDialog? = null
    private lateinit var videoPlayerWithAdPlayback: VideoPlayerWithAdPlayback
    private lateinit var companionContainer: ViewFlipper
    private var previousCompanionViewPos:Int = -1

    private val flipListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> run {
        val currentViewPos = companionContainer.displayedChild
        if(currentViewPos != previousCompanionViewPos) {
            val child = companionContainer.getChildAt(currentViewPos)
            val specificCompanion = adEntity?.customTracking?.customCompanionTrackings?.get(child.tag as String)?.companionBeaconTracking
            fireTrackerBeacons(specificCompanion ?: emptyArray())
        }
        previousCompanionViewPos = currentViewPos
    }}


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            adEntity = getSerializable(AD_ENTITY) as? ExternalSdkAd
            item = getSerializable(NATIVE_ENTITY) as? NativeData
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { setDialogProps(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_ima_immersive_video_ads, container, false)
        immersiveContainer = view.findViewById(R.id.immersive_container)

        view.findViewById<ImageView>(R.id.immersive_back).setOnClickListener {
            dismissAllowingStateLoss()
        }
        view.findViewById<NHTextView>(R.id.immersive_ad_title).text = adEntity?.adReportInfo?.adTitle
        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListenerCallback?.onDismiss(isCtaClick = false)
        dismissListenerCallback = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if(::companionContainer.isInitialized) companionContainer.removeOnLayoutChangeListener(flipListener)
    }

    //attach view from VH
    fun attachVHToImmersive(view: View) {
        immersiveContainer.post{
            (view.rootView as ViewGroup).removeView(view)
            immersiveContainer.addView(view)

            val mediaViewParent = view.findViewById<ViewGroup>(R.id.media_view_parent)
            val mediaView = view.findViewById<RelativeLayout>(R.id.media_view)
            arrayOf(mediaViewParent,mediaView).scale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            val playerview = view.findViewById<PlayerView>(R.id.videoPlayer)
            if(adEntity?.width ?: 0 < adEntity?.height ?: 0) {
                playerview.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            }

            videoPlayerWithAdPlayback = view.findViewById(R.id.videoPlayerWithAdPlayback)
            videoPlayerWithAdPlayback.showImmersiveView(true)

            decideToShowCTAorCompanion(view)
            getView()?.visibility = View.VISIBLE
            fireTrackerBeacons(arrayOf(adEntity?.customTracking?.customImmersiveViewEventTrackers?.get(AdConstants.IMMERSIVE_VIEW_ENTER)?: Constants.EMPTY_STRING))
        }
    }

    // decides to show CTA click or companion
    private fun decideToShowCTAorCompanion(view: View) {
        companionContainer = view.findViewById<ViewFlipper>(R.id.companion_ad_container)
        val ctaBottomBar : View? = view.findViewById<ConstraintLayout>(R.id.ad_banner_bottombar)
                ?: view.findViewById<ConstraintLayout>(R.id.ad_banner_brand_bottombar)
        val ctaBottomBarImersive = view.findViewById<ConstraintLayout>(R.id.ad_banner_bottombar_immersive)
        val state = companionContainer.childCount > 0

        companionContainer.visibility = if (state) View.VISIBLE else View.GONE
        ctaBottomBar?.visibility = if (state) View.GONE else View.VISIBLE
        ctaBottomBarImersive.visibility = if (state) View.GONE else View.VISIBLE

        if (!state) {
            ctaBottomBarImersive.visibility = View.VISIBLE
            val ctaSponsorText = ctaBottomBarImersive.findViewById<NHTextView>(R.id.ad_attr)
            val ctaButtonText = ctaBottomBarImersive.findViewById<NHTextView>(R.id.cta_button)
            ctaSponsorText.text = item?.sponsoredText
            ctaButtonText.text = item?.ctaText

            ctaBottomBarImersive.setOnClickListener {
                //launch cta click here
                try {
                    AdsOpenUtility.handleBrowserSelection(activity, adEntity?.action, adEntity)
                } catch (e: Exception) {
                    Logger.caughtException(e)
                } finally {
                    //distinguish b/w learnmore click and CTA click. Learn more click handled from IMA sdk end
                    dismissListenerCallback?.onDismiss(isCtaClick = true, isClickNonSDKViewElement = true)
                    dismissListenerCallback = null
                    dismissAllowingStateLoss()
                }
            }
        } else {
            companionContainer.post {
                if (adEntity?.companionRefreshTime ?: 0> 0) {
                    videoPlayerWithAdPlayback.startFlipping()
                }
                if (adEntity?.shownImmersive == false) {
                    companionContainer.addOnLayoutChangeListener(flipListener)
                    //updated flag shownImmersive to restrict impression tracker fired again
                    adEntity?.shownImmersive = true
                }

                companionContainer.children.forEachIndexed { index, v ->
                    val companionClickTrack = adEntity?.customTracking?.customCompanionTrackings?.get(v.tag as String)
                    companionClickTrack ?: return@forEachIndexed
                    // create click listener only when listener is null
                    if(videoPlayerWithAdPlayback.companionSlots[index].second == null) {
                        val companionPair = videoPlayerWithAdPlayback.companionSlots[index]
                        val companionClickListener = CompanionAdSlot.ClickListener {
                            adsTimeSpentOnLPHelper?.startAdsTimeSpentOnLPTimer(companionClickTrack.companionLPTimeSpentBeaconUrl)
                            fireTrackerBeacons(companionClickTrack.companionClickTracking)
                            dismissListenerCallback?.onDismiss(isCtaClick = true)
                            dismissListenerCallback = null
                            dismissAllowingStateLoss()
                        }
                        companionPair.first.addClickListener(companionClickListener)
                        //create new pair from stored list and insert at same position in list
                        val newCompanionPair = Pair(companionPair.first, companionClickListener)
                        videoPlayerWithAdPlayback.companionSlots[index] = newCompanionPair
                    } else {

                    }
                }
            }
        }
    }

    //fire any tracker url in adresponse
    private fun fireTrackerBeacons(trackers: Array<String>) {
        trackers.filter{ it.isNotEmpty() }.forEach { url: String -> impressionReporter.hitTrackerUrl(url) }
    }

    //dialog UI props
    private fun setDialogProps(dialog: Dialog)  {
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setWindowAnimations(R.style.immersive_transition)
            setBackgroundDrawable(ColorDrawable(Color.BLACK))
            attributes?.let {
                it.dimAmount = 0.90f;
                it.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                this.attributes = it
            }
        }
    }

}


interface DismissListenerImmersiveDialog {
    fun onDismiss(isCtaClick: Boolean = true, isClickNonSDKViewElement:Boolean = false)
}

