/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.sticky.StickyAudioPlayControlInterface
import com.newshunt.common.view.view.DisplayStickAudio
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.model.entity.server.StickyAudioCommentary
import com.newshunt.notification.model.entity.server.audioCommentaryStateLiveData
import com.newshunt.notification.R
import com.newshunt.dataentity.notification.asset.CommentaryState
import com.newshunt.notification.model.manager.StickyNotificationsManager
import com.newshunt.dataentity.notification.util.NotificationConstants
import java.io.Serializable
import java.lang.ref.WeakReference

/**
 *
 * Sticky Audio Play Controller to control the position play/stop and as well as UI Position on
 * screen
 *
 * @author santhosh.kc
 */

private val TOP_MOVE_LIMIT = CommonUtils.getDimension(R.dimen.sticky_audio_control_top_limit)
private val BOTTOM_MOVE_LIMIT = CommonUtils.getDeviceScreenHeight() - CommonUtils.getDimension(R.dimen.bottom_bar_large_height)
private val INITIAL_POSTION = CommonUtils.getDeviceScreenHeight() - CommonUtils.getDimension(R.dimen.sticky_audio_control_def_position_from_bottom)
private const val COLLAPSE_ANIMATION = 0
private const val EXPAND_ANIMATION = 1

private val collapseAnimator = StickyAudioPlayController.WidgetAnimator(COLLAPSE_ANIMATION)
private val expandAnimator = StickyAudioPlayController.WidgetAnimator(EXPAND_ANIMATION)

object StickyAudioPlayController : StickyAudioPlayControlInterface {

    override fun getAudioCommentaryLiveData(): MutableLiveData<Any> {
        return audioCommentaryStateLiveData
    }

    override fun onStickyAudioCommentaryStateChanged(intent: Intent?, displayStickAudio: DisplayStickAudio) {
        val stickyAudioCommentary = intent?.getSerializableExtra(NotificationConstants.INTENT_EXTRA_STICKY_AUDIO_STATE)
        if (stickyAudioCommentary != null) {
            Logger.d(StickyNotificationsManager.TAG, "Received intent sticky commentary audio update " +
                    "$stickyAudioCommentary")
        } else {
            Logger.d(StickyNotificationsManager.TAG, " Audio stream is not opted by user or " +
                    "notification is removed")
        }

        showAudioControl(stickyAudioCommentary as StickyAudioCommentary, displayStickAudio)
    }

    override fun onActivityResumed(displayStickAudio: DisplayStickAudio, commentaryData: Any?) {
        Logger.d(StickyNotificationsManager.TAG, "StickyAudioPlayController - onActivityResume " +
                "Entry")
        showAudioControl(commentaryData as StickyAudioCommentary, displayStickAudio)
        Logger.d(StickyNotificationsManager.TAG, "StickyAudioPlayController - onActivityResume " +
                "Exit")
    }

    override fun onActivityPaused(displayStickAudio: DisplayStickAudio) {
        collapseAnimator.stopIfRunning()
        expandAnimator.stopIfRunning()
    }

    private fun showAudioControl(stickyAudioCommentary: StickyAudioCommentary,
                                 displayStickAudio: DisplayStickAudio) {
        if (stickyAudioCommentary.state == null || stickyAudioCommentary.state == CommentaryState.NOT_OPTED_IN) {
            Logger.d(StickyNotificationsManager.TAG, "Audio Commentary is null or not opted in, " +
                    "so removing from activity window")
            removeView(displayStickAudio)
            return
        }

        val lastPosition = getLastPosition()
        val collapsed = lastPosition?.collapsed ?: false

        if (shouldHide(lastPosition, stickyAudioCommentary)) {
            Logger.d(StickyNotificationsManager.TAG, "User has crossed on Audio Commentary " +
                    "floating button, so not showing up and returning")
            removeView(displayStickAudio)
            return
        }

        if (lastPosition == null || !CommonUtils.equals(lastPosition.id, stickyAudioCommentary.id) ||
                !CommonUtils.equals(lastPosition.type, stickyAudioCommentary.type) ||
                lastPosition.lastUserPlayRequestTime < stickyAudioCommentary.userPlayRequestTime) {
            logStickyAudioFloatingWidgetViewedEvent(stickyAudioCommentary.id,
                    stickyAudioCommentary.type, stickyAudioCommentary.audioLanguage,
                    stickyAudioCommentary.state, collapsed)
        }

        val context = displayStickAudio.getWindowContext()
        val addToManager = displayStickAudio.getFloatingView() == null
        val mFloatingView = displayStickAudio.getFloatingView()
                ?: LayoutInflater.from(context).inflate(R.layout.layout_floating_widget, null)
        displayStickAudio.setFloatingView(mFloatingView)

        //Add the view to the window.
        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT)

        //Specify the view position
        params.gravity = Gravity.TOP or Gravity.END        //Initially view will be added to
        // top-left corner

        params.x = lastPosition?.let { it.x } ?: 0
        params.y = lastPosition?.let { it.y } ?: INITIAL_POSTION
        params.packageName = context.packageName

        val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        //Add the view to the window
        if (addToManager) {
            mWindowManager.addView(mFloatingView, params)
        } else {
            mWindowManager.updateViewLayout(mFloatingView, params)
        }

        saveLastPosition(params.x, params.y, stickyAudioCommentary, collapsed)

        (mFloatingView as? StickyAudioCommentaryControlView)?.setParameters(mWindowManager,
                params, stickyAudioCommentary)


        val closeButton = mFloatingView.findViewById(R.id.close_btn) as ImageView
        val collapseButton = mFloatingView.findViewById(R.id.collapse_layout) as ConstraintLayout
        val collapse = mFloatingView.findViewById(R.id.collapse) as ImageView
        val expand = mFloatingView.findViewById(R.id.expand) as ImageView
        val expandView = mFloatingView.findViewById(R.id.expanded_view) as ConstraintLayout

        val playButton = mFloatingView.findViewById(R.id.play_btn) as ImageView
        val stopButton = mFloatingView.findViewById(R.id.stop_btn) as ImageView
        val audio_eqalizer = mFloatingView.findViewById(R.id.audio_eqalizer) as ImageView
        val match_name = mFloatingView.findViewById(R.id.match_name) as TextView
        val description = mFloatingView.findViewById(R.id.description) as TextView

        match_name.setText(stickyAudioCommentary.title)
        val eqalizerAnimation = audio_eqalizer.drawable as AnimationDrawable

        collapseAnimator.setParameters(mFloatingView, mWindowManager, stickyAudioCommentary, eqalizerAnimation)
        expandAnimator.setParameters(mFloatingView, mWindowManager, stickyAudioCommentary, eqalizerAnimation)

        closeButton.setOnClickListener {
            mFloatingView.visibility = View.GONE
            removeView(displayStickAudio)
            hidePlayControl(stickyAudioCommentary)
            val collapsedValue = getLastPosition()?.collapsed ?: false
            logStickyAudioFloatingWidgetDismissEvent(stickyAudioCommentary.id,
                    stickyAudioCommentary.type, collapsedValue, stickyAudioCommentary.audioLanguage,
                    stickyAudioCommentary.state)
        }

        if (lastPosition == null) {
            val handler = Handler()
            handler.postDelayed({
                val collapsedValue = getLastPosition()?.collapsed ?: false
                if (!collapsedValue) {
                    collapseAnimator.start(expandView.width, true)
                    expand.visibility = View.VISIBLE
                    collapse.visibility = View.GONE
                }
            }, 3000)

        }

        if (collapsed) {
            expand.visibility = View.VISIBLE
            collapse.visibility = View.GONE
        } else {
            expand.visibility = View.GONE
            collapse.visibility = View.VISIBLE
        }

        collapseButton.setOnClickListener {

            val collapsedValue = getLastPosition()?.collapsed ?: false
            if (collapsedValue) {
                if (collapseAnimator.isRunning() || expandAnimator.isRunning()) {
                    return@setOnClickListener
                }
                expandAnimator.start(expandView.width)
                collapse.visibility = View.VISIBLE
                expand.visibility = View.GONE

            } else {
                if (expandAnimator.isRunning() || collapseAnimator.isRunning()) {
                    return@setOnClickListener
                }
                collapseAnimator.start(expandView.width)
                expand.visibility = View.VISIBLE
                collapse.visibility = View.GONE
            }
        }

        playButton.setOnClickListener {
            stopButton.visibility = View.VISIBLE
            playButton.visibility = View.GONE
            val collapsedValue = getLastPosition()?.collapsed ?: false

            if (!collapsedValue) {
                eqalizerAnimation.start()
            }
            StickyNotificationsManager.playOrStopAudioCommentary(true)
            logStickyAudioFloatingWidgetPlayStopEvent(stickyAudioCommentary.id,
                    stickyAudioCommentary.type, stickyAudioCommentary.audioLanguage, collapsedValue,
                    stickyAudioCommentary.state, CommentaryState.PLAYING)
        }

        stopButton.setOnClickListener {
            playButton.visibility = View.VISIBLE
            stopButton.visibility = View.GONE
            eqalizerAnimation.stop()
            description.setText(CommonUtils.getString(R.string.live_commentary))
            StickyNotificationsManager.playOrStopAudioCommentary(false)
            val collapsedValue = getLastPosition()?.collapsed ?: false
            logStickyAudioFloatingWidgetPlayStopEvent(stickyAudioCommentary.id,
                    stickyAudioCommentary.type, stickyAudioCommentary.audioLanguage, collapsedValue,
                    stickyAudioCommentary.state, CommentaryState.STOPPED)
        }


        if (stickyAudioCommentary.state == CommentaryState.PLAYING) {
            stopButton.visibility = View.VISIBLE
            playButton.visibility = View.GONE
            description.setText(CommonUtils.getString(R.string.live_commentary))
            eqalizerAnimation.start()
        } else if (stickyAudioCommentary.state == CommentaryState
                        .BUFFERING) {
            stopButton.visibility = View.VISIBLE
            playButton.visibility = View.GONE
            description.setText(CommonUtils.getString(R.string.buffering))
            eqalizerAnimation.start()
        } else {
            playButton.visibility = View.VISIBLE
            stopButton.visibility = View.GONE
            description.setText(CommonUtils.getString(R.string.live_commentary))
            eqalizerAnimation.stop()

        }
    }

    internal class WidgetAnimator(private val mode: Int) : ValueAnimator.AnimatorUpdateListener,
            Animator.AnimatorListener {

        private var floatingWidgetRef: WeakReference<View>? = null
        private var windowManagerRef: WeakReference<WindowManager>? = null
        private var stickyAudioCommentary: StickyAudioCommentary? = null
        private var animator: ValueAnimator? = null
        private var equalizerDrawableRef: WeakReference<AnimationDrawable>? = null
        private var initialX = 0
        private var initialY = 0
        private var expandWidth: Int = 0
        private var automatic : Boolean = false

        fun setParameters(floatingWidget: View, windowManager: WindowManager,
                          stickyAudioCommentary: StickyAudioCommentary,
                          equalizerAnimation: AnimationDrawable) {
            floatingWidgetRef = WeakReference(floatingWidget)
            windowManagerRef = WeakReference(windowManager)
            equalizerDrawableRef = WeakReference(equalizerAnimation)
            this.stickyAudioCommentary = stickyAudioCommentary
        }

        fun start(expandWidth: Int, automatic : Boolean = false) {
            animator = ValueAnimator.ofInt(0, expandWidth)
            animator?.addUpdateListener(this)
            animator?.addListener(this)
            initialX = floatingWidgetRef?.get()?.let {
                (it.layoutParams as WindowManager.LayoutParams).x
            } ?: 0
            initialY = floatingWidgetRef?.get()?.let {
                (it.layoutParams as WindowManager.LayoutParams).y
            } ?: 0
            this.expandWidth = expandWidth
            animator?.duration = 200L
            animator?.start()
            this.automatic = automatic
        }

        fun isRunning() = animator?.let {it.isStarted || it.isRunning || it.isPaused} ?: false

        fun cancelIfRunning() {
            floatingWidgetRef = null
            windowManagerRef = null
            equalizerDrawableRef = null
            stickyAudioCommentary = null
            stopIfRunning()
            animator = null
            automatic = false
        }

        fun stopIfRunning() {
            animator?.let {
                if (it.isStarted || it.isRunning || it.isPaused) {
                    it.cancel()
                }
            }
        }

        override fun onAnimationUpdate(animation: ValueAnimator?) {
            val posX = if (mode == EXPAND_ANIMATION) {
                initialX + animation?.animatedValue as Int
            } else {
                initialX - animation?.animatedValue as Int
            }
            updateView(posX)
        }

        override fun onAnimationRepeat(animation: Animator?) {
            //Not applicable
        }

        override fun onAnimationEnd(animation: Animator?) {
            Logger.d(StickyNotificationsManager.TAG, "On Animation End - Entry")
            val finalX = if (mode == EXPAND_ANIMATION) initialX + expandWidth else initialX -
                    expandWidth
            updateView(finalX)
            if (mode == EXPAND_ANIMATION) {
                if (stickyAudioCommentary?.state == CommentaryState.PLAYING) {
                    equalizerDrawableRef?.get()?.start()
                }
            }
            saveLastPosition(finalX, initialY, stickyAudioCommentary, collapsed = mode != EXPAND_ANIMATION)

            stickyAudioCommentary?.let {
                logStickyAudioFloatingWidgetCollapseExpandActionEvent(it.id, it.type, !automatic,
                        it.audioLanguage, it.state, mode != EXPAND_ANIMATION)
            }

            Logger.d(StickyNotificationsManager.TAG, "On Animation End - Exit")
        }

        private fun updateView(posX : Int) {
            val floatingWidget = floatingWidgetRef?.get() ?: return
            val windowManager = windowManagerRef?.get() ?: return
            val windowParams = floatingWidget.layoutParams as WindowManager.LayoutParams
            windowParams.x = posX
            windowManager.updateViewLayout(floatingWidget, windowParams)
        }

        override fun onAnimationCancel(animation: Animator?) {
            Logger.d(StickyNotificationsManager.TAG, "On Animation Cancel - Entry")
            val finalX = if (mode == EXPAND_ANIMATION) initialX + expandWidth else initialX -
                    expandWidth
            updateView(finalX)
            saveLastPosition(finalX, initialY, stickyAudioCommentary, collapsed = mode != EXPAND_ANIMATION)
            Logger.d(StickyNotificationsManager.TAG, "On Animation Cancel - Exit")
        }

        override fun onAnimationStart(animation: Animator?) {
            equalizerDrawableRef?.get()?.stop()
        }
    }

    fun saveLastPosition(x: Int, y: Int, stickyAudioCommentary: StickyAudioCommentary?, collapsed: Boolean) {
        stickyAudioCommentary ?: return
        PreferenceManager.savePreference(AppStatePreference.STICKY_AUDIO_CONTROL,
                JsonUtils.toJson(StickyAudioPosition(x, y, stickyAudioCommentary.id,
                        stickyAudioCommentary.type, stickyAudioCommentary.userPlayRequestTime,
                        collapsed = collapsed)))
    }

    private fun shouldHide(stickyAudioPosition: StickyAudioPosition?, stickyAudioCommentary: StickyAudioCommentary): Boolean {
        stickyAudioPosition ?: return false
        return stickyAudioPosition.hidden && CommonUtils.equals(stickyAudioPosition.id, stickyAudioCommentary.id) &&
                CommonUtils.equals(stickyAudioPosition.type, stickyAudioCommentary.type) &&
                stickyAudioPosition.lastUserPlayRequestTime >= stickyAudioCommentary.userPlayRequestTime
    }

    private fun hidePlayControl(stickyAudioCommentary: StickyAudioCommentary) {
        val stickyAudioPosition = getLastPosition() ?: return
        PreferenceManager.savePreference(AppStatePreference.STICKY_AUDIO_CONTROL,
                JsonUtils.toJson(StickyAudioPosition(stickyAudioPosition.x, stickyAudioPosition.y,
                        stickyAudioCommentary.id, stickyAudioCommentary.type,
                        stickyAudioCommentary.userPlayRequestTime, hidden = true)))
    }

    override fun resetPlayControlVisibility() {
        PreferenceManager.savePreference(AppStatePreference.STICKY_AUDIO_CONTROL, Constants.EMPTY_STRING)
    }

    fun getLastPosition(): StickyAudioPosition? {
        val posString = PreferenceManager.getPreference(AppStatePreference.STICKY_AUDIO_CONTROL,
                Constants.EMPTY_STRING)
        return JsonUtils.fromJson(posString, StickyAudioPosition::class.java)
    }

    private fun removeView(displayStickAudio: DisplayStickAudio) {
        collapseAnimator.cancelIfRunning()
        expandAnimator.cancelIfRunning()
        val floatingView = displayStickAudio.getFloatingView() ?: return
        val context = displayStickAudio.getWindowContext()
        val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager.removeView(floatingView)
        displayStickAudio.setFloatingView(null)
    }
}

data class StickyAudioPosition(val x: Int, val y: Int, val id: String, val type: String,
                               val lastUserPlayRequestTime : Long = 0, val hidden: Boolean =
                                       false, val collapsed : Boolean = false) :
        Serializable

class StickyAudioCommentaryControlView : ConstraintLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var windowManager: WindowManager? = null

    private var params: WindowManager.LayoutParams? = null
    private var initialY: Int = 0
    private var initialTouchY: Float = 0.toFloat()
    private var stickyAudioCommentary: StickyAudioCommentary? = null

    fun setParameters(windowManager: WindowManager, params: WindowManager.LayoutParams,
                      stickyAudioCommentary: StickyAudioCommentary) {
        this.windowManager = windowManager
        this.params = params
        this.stickyAudioCommentary = stickyAudioCommentary
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        handleTouch(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return handleTouch(event)
    }

    private fun handleTouch(event: MotionEvent?): Boolean {
        event ?: return false

        if (expandAnimator.isRunning() || collapseAnimator.isRunning()) {
            // just consume the touch events when animation is running
            return true
        }

        params?.let {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    //remember the initial position.
                    initialY = it.y

                    //get the touch location
                    initialTouchY = event.rawY
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val collapsed = StickyAudioPlayController.getLastPosition()?.collapsed ?: false
                    StickyAudioPlayController.saveLastPosition(it.x, it.y, stickyAudioCommentary,
                            collapsed)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    //Calculate the X and Y coordinates of the view.
                    it.y = initialY + (event.rawY - initialTouchY).toInt()

                    if (it.y < TOP_MOVE_LIMIT) {
                        it.y = TOP_MOVE_LIMIT
                    }

                    if (it.y + this.height > BOTTOM_MOVE_LIMIT) {
                        it.y = BOTTOM_MOVE_LIMIT - this.height
                    }

                    //Update the layout with new X & Y coordinate
                    windowManager?.updateViewLayout(this, params)
                    return true
                }
                else -> {
                }
            }
        }
        return false
    }

}
