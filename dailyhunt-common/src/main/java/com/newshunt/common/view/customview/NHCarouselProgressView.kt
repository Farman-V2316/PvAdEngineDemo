/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview

import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R
import java.util.*

/**
 * Carousel Progress Bar to show progress of carousel card transitions.
 * This is a collection of <code>ProgressBar</code> grouped into a single view.
 *
 * Use <code>CarouselProgressListener</code> to listen to user toggle events.
 *
 * @author karthik.r
 */
class NHCarouselProgressView : LinearLayout, View.OnClickListener {


    private val PERCENT_PROGRESS_WIDTH = 60
    private val PERCENT_SPACING_HALF = 18
    private val DEFAULT_PAGE_DURATION: Long = 8000

    val progressBarList: ArrayList<ProgressBar> = ArrayList()
    var clickPositionListener: CarouselProgressListener? = null
    private var smoothness = 20
    private var currentPosition: Int = 0
    private var pageDuration: Long = DEFAULT_PAGE_DURATION
    private var animationRunning = false
    private var countDownTimer: CountDownTimer? = null
    private var autoAnimate: Boolean = true
    private var startDuration: Long = 0
    private var pauseDuration: Long = 0
    private val verticalPadding = CommonUtils.getDimension(R.dimen.carousel_progress_bar_paddingTop)
    private val progressBarHeight = CommonUtils.getDimension(R.dimen.carousel_progress_bar_height)
    var isCircular: Boolean = false
    var manuallySwiped = false
    private var circularSwiped = false
    var itemClicked = false
    private var progressBackgroundRes: Int? = null

    override fun onClick(v: View?) {
        if (animationRunning) {
            countDownTimer?.cancel()
        }
        animationRunning = false
        if (clickPositionListener == null) {
            return
        }

        var reachedClickedItem = false
        progressBarList.forEachIndexed { index, element ->
            element.progress = if (reachedClickedItem) 0 else pageDuration.toInt()
            if (element == v) {
                reachedClickedItem = true
                circularSwiped = false
                clickPositionListener?.onCarouselPageSelected(index)
            }
        }
    }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr)
    }

    fun setNodeCount(nodeCount: Int) {
        if(nodeCount<=0){
            removeAllViews()
            progressBarList.clear()
            return
        }
        currentPosition = 0
        val width: Int = (CommonUtils.getDeviceScreenWidth() * PERCENT_PROGRESS_WIDTH) / (100 * nodeCount)
        val halfSpacing: Int = (CommonUtils.getDeviceScreenWidth() * PERCENT_SPACING_HALF) / (2 * 100 * (nodeCount))
        for (p in progressBarList) {
            getProgressView(width, halfSpacing, halfSpacing, p)
        }
        if (childCount < nodeCount) {
            for (i in (childCount + 1)..nodeCount) {
                addView(getProgressView(width, halfSpacing, halfSpacing))
            }
        } else if (childCount > nodeCount) {
            for (i in (nodeCount + 1)..childCount) {
                removeView(progressBarList.removeAt(progressBarList.size - 1))
            }

        }
    }

    /**
     * For showing progress in video cards
     */
    fun setAnimationParam(position: Int, duration: Long, startDuration: Long) {
        if (duration > 0) {
            this.pageDuration = duration
        }
        this.currentPosition = position
        this.startDuration = startDuration
    }

    /**
     * For pausing progress in videeo cards
     */
    fun pauseAnimation() {
        if (animationRunning) {
            animationRunning = false
            countDownTimer?.cancel()
        }
    }

    fun resumeAnimation() {
        if (animationRunning) {
            return
        }
        startAnimation(pauseDuration)
    }

    fun cancelAnimation() {
        if (animationRunning) {
            animationRunning = false
            countDownTimer?.cancel()
        }

        pauseDuration = 0
    }

    fun startAnimationWithDuration(duration: Long) {
        if (duration == 0L) {
            return
        }
        pageDuration = duration
        startAnimation()
    }

    fun setAnimationWithDuration(duration: Long) {
        if (animationRunning) {
            return
        }
        cancelAnimation()
        pageDuration = Math.max(duration, DEFAULT_PAGE_DURATION)
    }

    fun startAnimation() {
        startAnimation(0)
    }

    fun startAnimation(alreadyElapsed: Long) {
        if (animationRunning) {
            return
        }

        animationRunning = true
        // Start animation
        progressBarList.get(currentPosition).max = (startDuration + pageDuration).toInt()
        progressBarList.get(currentPosition).progress = startDuration.toInt()
        countDownTimer = object : CountDownTimer(pageDuration - alreadyElapsed, (pageDuration -
                alreadyElapsed) / smoothness) {

            override fun onTick(millisUntilFinished: Long) {
                pauseDuration = pageDuration - millisUntilFinished
                progressBarList.get(currentPosition).setProgress((pageDuration - millisUntilFinished + startDuration).toInt(), false)
            }

            override fun onFinish() {
                animationRunning = false
                progressBarList.get(currentPosition).progress = (startDuration + pageDuration).toInt()
                currentPosition++

                if (currentPosition == progressBarList.size) {
                    currentPosition = 0
                    if (isCircular) {
                        for (progressBar in progressBarList) {
                            progressBar.setProgress(0, false)
                        }
                        circularSwiped = true
                        clickPositionListener?.onAutoTransitionToNextItem(currentPosition)
                        if (autoAnimate) {
                            startAnimation()
                        }
                    }
                } else {
                    circularSwiped = false
                    clickPositionListener?.onAutoTransitionToNextItem(currentPosition)
                    if (autoAnimate) {
                        startAnimation()
                    }
                }
            }

        }.start()
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray: TypedArray? = null
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable
                    .NHCarouselProgressView, defStyleAttr, 0)

            typedArray?.let {
                if (it.hasValue(R.styleable.NHCarouselProgressView_progressBackground)) {
                    progressBackgroundRes = it.getResourceId(R.styleable
                            .NHCarouselProgressView_progressBackground, 0)
                }
            }
        } catch (e: Exception) {
            typedArray?.recycle()
        }
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        val dpi = context.resources.displayMetrics.densityDpi
        smoothness = if (dpi > DisplayMetrics.DENSITY_XHIGH) 30 else 20
        setOnClickListener(this)
    }

    private fun getProgressView(width: Int,
                                leftMargin: Int,
                                rightMargin: Int,
                                oldProgressBar: ProgressBar? = null): ProgressBar {

        val progressBar = oldProgressBar ?: ProgressBar(context, null, android.R.attr
                .progressBarStyleHorizontal)
        progressBar.setPadding(leftMargin, verticalPadding, rightMargin, verticalPadding)
        progressBar.layoutParams = LinearLayout.LayoutParams(width, progressBarHeight)
        if (oldProgressBar == null) {
            progressBarList.add(progressBar)
            progressBar.setOnClickListener(this)
            progressBar.progressDrawable = ContextCompat.getDrawable(context,
                    progressBackgroundRes ?: R.drawable.carousel_progress)
        } else {
            progressBar.progress = 0
        }
        return progressBar
    }

    fun onPageScrolled(position: Int, noAutoScroll: Boolean = false) {
        if (position < 0) {
            pauseAnimation()
            return
        }
        if (position >= progressBarList.size) {
            onPageScrolled(progressBarList.size - 1, true)
        }
        var positionToAnimate = position
        if ((position != currentPosition || manuallySwiped || noAutoScroll) && (!circularSwiped
                        || noAutoScroll || itemClicked)) {
            pauseAnimation()
            positionToAnimate += 1
            manuallySwiped = true
        }

        var reachedClickedItem = false
        progressBarList.forEachIndexed { index, element ->
            if (positionToAnimate == index) {
                reachedClickedItem = true
            }
            element.progress = if (reachedClickedItem) 0 else pageDuration.toInt()
        }
    }

    fun reset() {
        manuallySwiped = false
        currentPosition = 0
        isCircular = false
        circularSwiped = false
        itemClicked = false
        cancelAnimation()
    }
}

/**
 * Listener for user interactions on page changes.
 */
interface CarouselProgressListener {
    fun onAutoTransitionToNextItem(nextPageIndex: Int)
    fun onCarouselPageSelected(pageIndex: Int)
}