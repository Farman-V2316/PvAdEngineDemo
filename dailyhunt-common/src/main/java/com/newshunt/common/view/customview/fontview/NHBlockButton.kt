/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview.fontview;

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.view.customview.fontview.NHBlockButton.BlockChangeListener
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.theme.ThemeUtils

/**
 * Constraint Layout implementation holding a image and text view to draw Block/UnBlock button.
 *
 * Use [BlockChangeListener] to listen to user toggling options.
 *
 * @author anshul.jain
 */

class NHBlockButton : ConstraintLayout, View.OnClickListener {

    private lateinit var toggleOnText: NHTextView
    private lateinit var toggleOffText: NHTextView
    private var state: Boolean = false
    private var blockChangeListener: BlockChangeListener? = null

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        initView()
    }


    private fun initView() {
        val v = LayoutInflater.from(context).inflate(R.layout.layout_block_button, this, true)
        toggleOnText = v.findViewById(R.id.toggle_on_text)
        toggleOffText = v.findViewById(R.id.toggle_off_text)
        updateState()
        setOnClickListener(this)
    }

    private fun updateState() {
        isSelected = state
        if (state) {
            toggleOnText.visibility = View.VISIBLE
            toggleOffText.visibility = View.INVISIBLE
        } else {
            toggleOnText.visibility = View.INVISIBLE
            toggleOffText.visibility = View.VISIBLE
        }
        updateBackgroundAndText(state)
    }

    private fun updateBackgroundAndText(isSelected: Boolean) {
        if (state) {
            background = CommonUtils.getDrawable(R.drawable.follow_toggle_background)
            val toggleOnTextColor = CommonUtils.getColor(com.newshunt.common.util.R.color.white_color)
            toggleOnText.setTextColor(toggleOnTextColor)
        } else {
            background = if (ThemeUtils.isNightMode()) CommonUtils.getDrawable(R.drawable
                    .follow_toggle_background_night) else CommonUtils.getDrawable(R.drawable.follow_toggle_background)
            val toggleOnTextColor = if (ThemeUtils.isNightMode()) CommonUtils.getColor(com.newshunt.common.util.R.color
                    .white_color) else CommonUtils.getColor(com.newshunt.common.util.R.color.white_color)
            toggleOffText.setTextColor(toggleOnTextColor)
        }
    }

    @JvmOverloads
    fun setState(newState: Boolean, ignoreIfSame: Boolean = false) {
        if (ignoreIfSame && newState == this.state) return
        this.state = newState
        updateState()
    }

    override fun onClick(v: View) {
        state = !state
        updateState()
        blockChangeListener?.onBlockChange(state)
    }

    fun setOnFollowChangeListener(blockChangeListener: BlockChangeListener) {
        this.blockChangeListener = blockChangeListener
    }

    interface BlockChangeListener {
        fun onBlockChange(newstate: Boolean)
    }

}

