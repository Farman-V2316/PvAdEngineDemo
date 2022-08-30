/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.ImageViewCompat
import com.google.android.material.snackbar.Snackbar
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.follow.entity.FollowUnFollowReason
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.ThemeUtils

/**
 * Constraint Layout implementation holding a image and text view to draw Follow/Unfollow button.
 *
 * Use <code>FollowChangeListerner</code> to listen to user toggling options.
 *
 * @author karthik.r
 */

class NHFollowButton : ConstraintLayout, View.OnClickListener {

    companion object {
        const val DEFAULT_SNACKBAR_DISPLAY_DURATION = 5000L
    }

    private lateinit var followContainer: LinearLayout
    private lateinit var followingContainer: LinearLayout
    private lateinit var toggleOnText: NHTextView
    private lateinit var toggleOffText: NHTextView
    private lateinit var toggleOnImage: ImageView
    private lateinit var toggleOffImageView: ImageView
    private var state: Boolean = false
    private var followChangeListener: FollowChangeListerner? = null
    private var isUrdu: Boolean = false
    private var isNewsList: Boolean = false
    private lateinit var rootLayout: ConstraintLayout
    private var followSnackBar: Snackbar? = null
    private var followButtonMode = FollowButtonMode.DAY_AND_NIGHT_MODE
    private var isBorderVisible = true
    private var isDHTV: Boolean = false
    private var customFollowLayoutId = 0

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
        var array: TypedArray? = null
        var iconSize = CommonUtils.getDimension(R.dimen.follow_btn_icon_w_h)
        try {
            when {
                attrs != null -> {
                    array = context.obtainStyledAttributes(attrs, R.styleable.NHFollowButton,
                            defStyleAttr, 0)
                    array?.let {
                        iconSize = it.getDimension(R.styleable.NHFollowButton_nhIconSize,
                                CommonUtils.getDimension(R.dimen.follow_btn_icon_w_h).toFloat()).toInt()
                        isBorderVisible = it.getBoolean(R.styleable
                                .NHFollowButton_isBorderVisible, true)
                        isUrdu = it.getBoolean(R.styleable.NHFollowButton_isUrdu, false)
                        isNewsList = it.getBoolean(R.styleable.NHFollowButton_isNewsList, false)
                        val mode = it.getInteger(R.styleable
                                .NHFollowButton_followButtonMode, FollowButtonMode.DAY_AND_NIGHT_MODE.mode)
                        followButtonMode = FollowButtonMode.getFollowButtonMode(mode)
                        isDHTV = it.getBoolean(R.styleable.NHFollowButton_isDHTV, false)
                        customFollowLayoutId = it.getResourceId(R.styleable.NHFollowButton_customLayoutId, 0)
                }
            }
        }
        } finally {
            array?.recycle()
        }

        initView(iconSize)
    }

    private fun initView(imageSize: Int) {
        val layoutId = when {
            customFollowLayoutId != 0 -> {
                customFollowLayoutId
            }
            isUrdu -> {
                R.layout.layout_follow_button_urdu
            }
            else -> {
                R.layout.layout_follow_button
            }
        }
        val v = LayoutInflater.from(context).inflate(layoutId, this, true)
        followContainer = v.findViewById(R.id.follow_container)
        followingContainer = v.findViewById(R.id.following_container)
        rootLayout = v.findViewById(R.id.toggle_button)
        toggleOnText = v.findViewById(R.id.toggle_on_text)
        toggleOffText = v.findViewById(R.id.toggle_off_text)
        toggleOnImage = v.findViewById(R.id.toggle_on_image)
        toggleOffImageView = v.findViewById(R.id.toggle_on_image_followed)
        toggleOffImageView.layoutParams.width = imageSize
        toggleOffImageView.layoutParams.height = imageSize
        toggleOnImage.layoutParams.width = imageSize
        toggleOnImage.layoutParams.height = imageSize

        if (isDHTV) {
            toggleOffImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            toggleOnImage.scaleType = ImageView.ScaleType.CENTER_CROP
            ImageViewCompat.setImageTintList(toggleOnImage,
                    ColorStateList.valueOf(ContextCompat.getColor(context, com.newshunt.common.util.R.color.white_color)))
        }

        if (isNewsList) {
            toggleOffImageView.visibility = View.VISIBLE
            toggleOffText.visibility = View.GONE
        } else {
            toggleOffText.visibility = View.VISIBLE
            toggleOffImageView.visibility = View.GONE
        }
        updateState()
        setOnClickListener(this)
    }

    private fun updateState() {
        isSelected = state
        if (isNewsList) {
            showFollowButtonWithoutText()
        } else {
            showFollowButtonWithText()
        }
    }

    private fun showFollowButtonWithoutText() {
        val params = rootLayout.layoutParams
        params.height = CommonUtils.getDimension(R.dimen.onboarding_item_vert_padding)
        rootLayout.layoutParams = params
        if (state) {
            followingContainer.visibility = View.VISIBLE
            followContainer.visibility = View.GONE
            if (isBorderVisible) {
                rootLayout.setPadding(CommonUtils.getDimension(com.newshunt.common.util.R.dimen.viral_social_icon_text_size), 0,
                        CommonUtils.getDimension(com.newshunt.common.util.R.dimen.viral_social_icon_text_size), 0)
            }
        } else {
            rootLayout.setPadding(CommonUtils.getDimension(R.dimen
                    .collection_landing_counts_marginbottom), 0, CommonUtils.getDimension(R
                    .dimen.viral_text_marginTop), 0)
            followingContainer.visibility = View.GONE
            followContainer.visibility = View.VISIBLE
        }
    }

    private fun showFollowButtonWithText() {
        updateBackgroundAndText(state)
        if (state) {
            followingContainer.visibility = View.VISIBLE
            followContainer.visibility = View.INVISIBLE

        } else {
            followingContainer.visibility = View.INVISIBLE
            followContainer.visibility = View.VISIBLE
        }
    }

    private fun updateBackgroundAndText(state: Boolean) {

        val darkThemeFollowButton = when (followButtonMode) {
            FollowButtonMode.NIGHT_MODE_ONLY -> true
            FollowButtonMode.DAY_AND_NIGHT_MODE -> ThemeUtils.isNightMode()
            else -> false
        }

        if (state) {
            background = CommonUtils.getDrawable(R.drawable.follow_toggle_background)
            val toggleOnTextColor = CommonUtils.getColor(com.newshunt.common.util.R.color.white_color)
            toggleOnText.setTextColor(toggleOnTextColor)
            DrawableCompat.setTint(toggleOnImage.getDrawable(), ContextCompat.getColor(context, com.newshunt.common.util.R.color.follow_color))
        } else {
            background = if (darkThemeFollowButton) CommonUtils.getDrawable(R.drawable
                    .follow_toggle_background_night) else CommonUtils.getDrawable(R.drawable.follow_toggle_background)
            val toggleOnTextColor = if (darkThemeFollowButton) CommonUtils.getColor(com.newshunt.common.util.R.color
                    .white_color) else CommonUtils.getColor(com.newshunt.common.util.R.color.follow_color)
            toggleOnText.setTextColor(toggleOnTextColor)
            DrawableCompat.setTint(toggleOnImage.getDrawable(), ContextCompat.getColor(context,
                    if (darkThemeFollowButton) com.newshunt.common.util.R.color.white_color else com.newshunt.common.util.R.color
                            .follow_color))
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
        followChangeListener?.onFollowChange(state, null)
        val snackBarMetaData = followChangeListener?.showSnackBarOnFollowChange()
        if (!state) {
            followSnackBar?.dismiss()
            if (snackBarMetaData != null) {
                followSnackBar = CustomSnackBar.showUnfollowSnackBar(this, context, snackBarMetaData)
                followSnackBar?.show()
            }
        } else {
            followSnackBar?.dismiss()
            if (snackBarMetaData != null) {
                followSnackBar = CustomSnackBar.showSnackBar(this, context, snackBarMetaData)
                followSnackBar?.show()
            }
        }
    }

    fun setOnFollowChangeListener(followChangeListener: FollowChangeListerner) {
        this.followChangeListener = followChangeListener
    }

    interface FollowChangeListerner {
        fun onFollowChange(newstate: Boolean, reason: FollowUnFollowReason?)
        fun showSnackBarOnFollowChange(): FollowSnackBarMetaData?
    }

    /**
     * Set string to use for toggle on and off
     */
    fun setToggleText(toggleOn: String, toggleOff: String) {
        toggleOnText.visibility = if (toggleOn.isEmpty()) {
            View.GONE
        } else {
            toggleOnText.text = toggleOn
            View.VISIBLE
        }
        toggleOffText.text = toggleOff
    }
}

enum class FollowButtonMode(val mode: Int) {
    DAY_MODE_ONLY(0),
    NIGHT_MODE_ONLY(1),
    DAY_AND_NIGHT_MODE(2);

    companion object {
        fun getFollowButtonMode(mode: Int): FollowButtonMode {
            FollowButtonMode.values().forEach { followButtonMode ->
                if (followButtonMode.mode == mode) {
                    return followButtonMode
                }
            }
            return DAY_AND_NIGHT_MODE
        }
    }
}

data class FollowSnackBarMetaData
@JvmOverloads
constructor(val title: String, val actionMessage: String? = null,
            val snackBarActionClickListener: SnackBarActionClickListener? =
                    getDefaultSnackBarClickListener(),
            val displayDuration: Long = PreferenceManager.getPreference(AppStatePreference
                    .FOLLOWED_SNACKBAR_DISPLAY_DURATION, NHFollowButton
                    .DEFAULT_SNACKBAR_DISPLAY_DURATION),
            val snackBarLayoutParams : SnackBarLayoutParams? = getDefaultSnackBarParams())