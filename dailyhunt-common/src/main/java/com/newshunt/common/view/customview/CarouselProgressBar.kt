/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.R
import java.lang.ref.WeakReference

private const val PER_PROGRESS_DIV = 200

class CarouselProgressBarView : LinearLayoutCompat, CarouselProgress, ValueAnimator.AnimatorUpdateListener, View.OnClickListener {

    override fun onClick(v: View?) {
        if (v is ProgressBar) {
            val index = v.tag as Int
            stopAnimation()
            setCurrentProgress(value = index * PER_PROGRESS_DIV, fill = true)
            callback?.selectPage(index)
        }
    }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        init(context, attributeSet, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        init(context, attributeSet, defStyleAttr)
    }

    fun init(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) {
        if (attributeSet != null) {
            var typedArray: TypedArray? = null
            try {
                typedArray = context.obtainStyledAttributes(attributeSet,
                        R.styleable.CarouselProgressBarView, defStyleAttr, 0)

                typedArray?.let {
                    if (it.hasValue(R.styleable.CarouselProgressBarView_progress_bg)) {
                        progressBackgroundRes = it.getResourceId(R.styleable
                                .CarouselProgressBarView_progress_bg, R.drawable.carousel_progress2)
                    }
                }
            } catch (e: Exception) {
                typedArray?.recycle()
            }
        }
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
    }

    private var duration: Long = 0
    private val progressBarList: MutableList<ProgressBar> = mutableListOf()
    private var circular: Boolean = false
    private var animator: ValueAnimator? = null
    private var currentIndex: Int = 0
    private var totalPageCount: Int = 0

    private var animationStopped: Boolean = false
    private var animationPaused: Boolean = false
    private var animationPlaying: Boolean = false
    private var animationStarted: Boolean = false

    private var progressHeight: Int = 0
    private var sideMargin: Int = 0
    private var progressWidth: Int = 0
    private var verticalMargin: Int = 0
    private var totalProgress: Int = 0
    private var progressBackgroundRes: Int = R.drawable.carousel_progress2

    private var filledProgressCount: Int = 0
    private var callback: CarouselProgressCallback? = null
    private var progressId: String? = null
    private var manualSwiped = false


    override fun setup(
            duration: Long,
            autoSwipe: Boolean,
            totalPageCount: Int,
            circular: Boolean,
            progressHeight: Int,
            sideMargin: Int,
            verticalMargin: Int,
            totalProgressWidth: Int,
            callback: CarouselProgressCallback,
            id: String
    ) {
        this.circular = circular
        this.totalPageCount = totalPageCount
        this.duration = duration * 1000
        this.progressHeight = progressHeight
        this.sideMargin = sideMargin
        this.verticalMargin = verticalMargin
        this.progressWidth = ((totalProgressWidth / totalPageCount) * 0.6f).toInt()
        this.callback = callback
        this.progressId = id
        this.manualSwiped = false

        totalProgress = PER_PROGRESS_DIV * totalPageCount
        animator?.removeAllUpdateListeners()
        animator?.pause()
        animator = null
        animator = ValueAnimator.ofInt(0, totalProgress)
        animator!!.addUpdateListener(WeakAnimationListener(this))
        animator!!.interpolator = LinearInterpolator()
        animator!!.duration = this.duration * totalPageCount
        if (circular) {
            animator!!.repeatMode = ValueAnimator.RESTART
            animator!!.repeatCount = ValueAnimator.INFINITE
        } else {
            animator!!.repeatCount = 0
        }

        progressBarList.forEachIndexed { i, p ->
            setupProgressBarSize(p, i)
        }

        if (progressBarList.size > totalPageCount) {
            progressBarList.filter {
                val index = it.tag as Int
                index >= totalPageCount
            }.forEach {
                removeView(it)
            }
            progressBarList.removeAll {
                val index = it.tag as Int
                index >= totalPageCount
            }
        } else if (progressBarList.size < totalPageCount) {
            val currentSize = progressBarList.size
            for (i in currentSize until totalPageCount) {
                val p = getProgressBar(i)
                progressBarList.add(p)
                addView(p)
            }
        }
        animationStopped = false
        animationPlaying = false
        animationPaused = false
        animationStarted = false
    }

    private fun getProgressBar(index: Int): ProgressBar {
        val p = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        p.isIndeterminate = false
        p.min = 0
        p.interpolator = LinearInterpolator()
        p.setOnClickListener(this)
        p.max = PER_PROGRESS_DIV
        p.progressDrawable = ContextCompat.getDrawable(context,
                progressBackgroundRes)
        setupProgressBarSize(p, index)
        return p
    }

    private fun setupProgressBarSize(p: ProgressBar, index: Int) {
        p.tag = index
        p.setPadding(sideMargin, verticalMargin, sideMargin, verticalMargin)
        p.layoutParams = LayoutParams(progressWidth + (2 * sideMargin), progressHeight + (verticalMargin * 2))
    }

    override fun startAnimation() {
        if (animationPlaying) {
            return
        }
        val va = animator ?: run {
            Logger.e(LOG_TAG, "Value animation is null")
            return
        }
        Logger.d(LOG_TAG, "Starting animation")
        va.currentPlayTime = 0
        va.removeAllUpdateListeners()
        va.addUpdateListener(WeakAnimationListener(this))
        va.start()
        animationPlaying = true
        animationPaused = false
        animationStopped = false
        animationStarted = true
    }

    override fun pauseAnimation() {
        if (animationPaused) {
            return
        }
        Logger.d(LOG_TAG, "Pause animation")
        if (animationStopped) {
            return
        }
        val va = animator ?: run {
            Logger.e(LOG_TAG, "Value animation is null")
            return
        }
        animationPlaying = false
        animationPaused = true
        va.removeAllUpdateListeners()
        va.pause()
    }

    override fun resumeAnimation() {
        if (animationPlaying) {
            return
        }
        if (animationStopped) {
            return
        }
        Logger.d(LOG_TAG, "Resume animation")
        val va = animator ?: run {
            Logger.e(LOG_TAG, "Value animation is null")
            return
        }
        animationPlaying = true
        animationPaused = false
        animationStopped = false
        va.removeAllUpdateListeners()
        va.addUpdateListener(WeakAnimationListener(this))
        va.resume()
    }

    override fun stopAnimation() {
        if (animationStopped) {
            return
        }
        Logger.d(LOG_TAG, "Stop animation")
        animator?.removeAllUpdateListeners()
        animator?.pause()
        setCurrentProgress(value = currentIndex * PER_PROGRESS_DIV, fill = true)
        animationPlaying = false
        animationPaused = false
        animationStopped = true
        animationStarted = false
    }

    private fun setCurrentProgress(value: Int, fill: Boolean) {
        currentIndex = value / PER_PROGRESS_DIV
        val lastProgressValue = value % PER_PROGRESS_DIV
        if (currentIndex < totalPageCount) {
            progressBarList[currentIndex].carouselCompatProgress(
                    if (fill) PER_PROGRESS_DIV else lastProgressValue)
        }
        if (currentIndex != filledProgressCount) {
            progressBarList.forEachIndexed { i, p ->
                if (i < currentIndex) {
                    p.carouselCompatProgress(PER_PROGRESS_DIV)
                } else if (i != currentIndex) {
                    p.carouselCompatProgress(0)
                }
            }
            filledProgressCount = currentIndex
        }
    }

    override fun onViewPagerPageSelected(index: Int) {
        Logger.d(LOG_TAG, "View pager page selected $index")
        if (index == currentIndex) {
            return
        }
        manualSwiped = true
        stopAnimation()
        setCurrentProgress(value = index * PER_PROGRESS_DIV, fill = true)
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        val currentValue = (animation?.animatedValue ?: 0) as Int
        val newSelectedPage = currentValue / PER_PROGRESS_DIV
        val updateCallbackPage: Boolean = newSelectedPage != currentIndex
        //Need to update progress first than page for various reason
        setCurrentProgress(value = currentValue, fill = false)
        if (updateCallbackPage) {
            //Stop animation on view more visible
            callback?.selectPage(newSelectedPage)
        }
    }

    override fun getProgressId(): String {
        return progressId ?: Constants.EMPTY_STRING
    }

    override fun isPaused(): Boolean {
        return animationPaused
    }

    override fun isStopped(): Boolean {
        return animationStopped
    }

    override fun isStarted(): Boolean {
        return animationStarted
    }

    override fun isPlaying(): Boolean {
        return animationPlaying
    }

    override fun manualSwiped(): Boolean {
        return manualSwiped
    }

}


interface CarouselProgress {
    fun setup(
            duration: Long,
            autoSwipe: Boolean,
            totalPageCount: Int,
            circular: Boolean,
            progressHeight: Int,
            sideMargin: Int,
            verticalMargin: Int,
            totalProgressWidth: Int,
            callback: CarouselProgressCallback,
            id: String
    )

    fun startAnimation()
    fun pauseAnimation()
    fun resumeAnimation()
    fun stopAnimation()
    fun onViewPagerPageSelected(index: Int)
    fun getProgressId(): String
    fun isPaused(): Boolean
    fun isStopped(): Boolean
    fun isStarted(): Boolean
    fun isPlaying(): Boolean
    fun manualSwiped(): Boolean
}


interface CarouselProgressCallback {
    fun selectPage(index: Int)
}

private const val LOG_TAG = "CarouselProgressBar"

fun ProgressBar.carouselCompatProgress(value: Int) {
    this.setProgress(value, false)
}

class WeakAnimationListener(listener: ValueAnimator.AnimatorUpdateListener) :
        ValueAnimator.AnimatorUpdateListener {
    private val refHolder: WeakReference<ValueAnimator.AnimatorUpdateListener> = WeakReference(listener)

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        val listener = refHolder.get() ?: kotlin.run {
            animation?.let {
                it.pause()
                it.removeUpdateListener(this)
                Logger.i(LOG_TAG, "Removed animation listener")
            }
            return
        }
        listener.onAnimationUpdate(animation)
    }
}