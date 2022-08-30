package com.newshunt.appview.common.postcreation.view.customview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.newshunt.appview.R


class PollOptionRelativeLayout: RelativeLayout {

    companion object{
        val STATE_ERROR = arrayOf(R.attr.state_error)
    }

   private var isError: Boolean = false

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
            context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        if (isError)
           mergeDrawableStates(drawableState, STATE_ERROR.toIntArray())
        return drawableState
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        var array: TypedArray? = null
        try {
            when {
                attrs != null -> {
                    array = context.obtainStyledAttributes(attrs, R.styleable.PollOptionRelativeLayout,
                            defStyleAttr, 0)
                    array?.let {
                        isError = it.getBoolean(R.styleable.PollOptionRelativeLayout_state_error, false)
                    }
                }
            }
        } finally {
            array?.recycle()
        }
    }

    fun isError(error: Boolean){
        isError = error
        refreshDrawableState()
    }
}