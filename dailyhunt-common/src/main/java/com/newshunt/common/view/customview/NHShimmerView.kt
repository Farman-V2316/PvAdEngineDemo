package com.newshunt.common.view.customview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.graphics.Paint
import androidx.annotation.NonNull
import android.graphics.RectF
import android.graphics.Color
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.theme.ThemeUtils


class NHShimmerView : View {

    private lateinit var animator: ValueAnimator
    private val ANIMATION_DURATION: Long = 500
    private var RGB_CODE : Int = 240
    private val MAX_OPACITY : Int = 155;
    private val MIN_OPACITY : Int = 100
    private var radius = 0f
    private lateinit var shaderPaint: Paint

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
        attrs?.let {
            getAttributes(context, attrs, 0)
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
        attrs?.let {
            getAttributes(context, attrs, defStyleAttr)
        }
    }

    @SuppressLint("NewApi")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleRes, defStyleAttr) {
        init(context)
        attrs?.let {
            getAttributes(context, attrs, defStyleAttr)
        }
    }

    private fun init(context: Context) {
        animator = ValueAnimator.ofFloat(-1f, 1f)
        animator.duration = ANIMATION_DURATION
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener(animatorUpdateListener)

        shaderPaint = Paint()
        shaderPaint.isAntiAlias = true

        RGB_CODE = ThemeUtils.getThemeDataByAttribute(context, R.attr.shimmer_rgb, 240)

        if (visibility == VISIBLE) {
            animator.start()
        }
    }

    private fun getAttributes(context: Context, attributeSet: AttributeSet, defStyle: Int) {
        var a = getContext().obtainStyledAttributes(attributeSet, R.styleable.NHShimmerView, 0, defStyle)
        try {
            radius = a.getDimension(R.styleable.NHShimmerView_rounded_radius, 0f)
        } finally {
            a.recycle()
        }
    }

    val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
        if (isAttachedToWindow && visibility == View.VISIBLE) {
            val f = animator.getAnimatedValue() as Float
            updateShader(width.toFloat(), f)
            invalidate()
        } else {
            animator.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    private fun updateShader(w: Float, opacityLevel: Float) {
        var opacity: Int = (MAX_OPACITY * opacityLevel).toInt()
        if (opacity < 0) {
            opacity *= -1
        }

        // Opacity should never go zero
        opacity += MIN_OPACITY

        shaderPaint.color = Color.argb(opacity, RGB_CODE, RGB_CODE, RGB_CODE)
    }

    override fun onVisibilityChanged(@NonNull changedView: View, visibility: Int) {
        when (visibility) {
            View.VISIBLE -> animator.start()
            View.INVISIBLE, View.GONE -> animator.cancel()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // we only need Alpha value in this bitmap
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas?.drawRoundRect(rect, radius, radius, shaderPaint)
    }
}