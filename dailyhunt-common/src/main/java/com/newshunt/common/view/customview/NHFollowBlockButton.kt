package com.newshunt.common.view.customview

import android.content.Context
import android.content.res.TypedArray
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.PreferenceManager

import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.follow.entity.FollowUnFollowReason
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dhutil.R
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.preference.AppStatePreference

/**
 * @author Rekha Rani
 * custom button to show Follow Block button and snackbar.
 */
class NHFollowBlockButton : ConstraintLayout, View.OnClickListener {

    companion object {

        const val DEFAULT_SNACKBAR_DISPLAY_DURATION = 5000L

    }



    private lateinit var followContainer: LinearLayout
    private lateinit var blockContainer: LinearLayout
    private lateinit var toggleOnText: NHTextView
    private lateinit var toggleOffText: NHTextView
    private lateinit var toggleOnImage: ImageView
    private lateinit var toggleOffImageView: ImageView
    private var state: Boolean = false
    private var followChangeListener: FollowChangeListerner? = null
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

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        var array: TypedArray? = null
        var iconSize = CommonUtils.getDimension(R.dimen.follow_btn_icon_w_h)
        try {
            when {
                attrs != null -> {
                    array = context.obtainStyledAttributes(
                        attrs, R.styleable.NHFollowButton,
                        defStyleAttr, 0
                    )
                    array?.let {
                        iconSize = it.getDimension(
                            R.styleable.NHFollowButton_nhIconSize,
                            CommonUtils.getDimension(R.dimen.follow_btn_icon_w_h).toFloat()
                        ).toInt()
                        isBorderVisible = it.getBoolean(
                            R.styleable
                                .NHFollowButton_isBorderVisible, true
                        )

                        val mode = it.getInteger(
                            R.styleable
                                .NHFollowButton_followButtonMode,
                            FollowButtonMode.DAY_AND_NIGHT_MODE.mode
                        )
                        followButtonMode = FollowButtonMode.getFollowButtonMode(mode)
                        isDHTV = it.getBoolean(R.styleable.NHFollowButton_isDHTV, false)
                        customFollowLayoutId =
                            it.getResourceId(R.styleable.NHFollowButton_customLayoutId, 0)
                    }
                }
            }
        } finally {
            array?.recycle()
        }

        initView()
    }

    private fun initView() {
        val layoutId = when {
            customFollowLayoutId != 0 -> {
                customFollowLayoutId
            }
            else -> {
                R.layout.layout_follow_block_button
            }
        }
        val v = LayoutInflater.from(context).inflate(layoutId, this, true)
        followContainer = v.findViewById(R.id.follow_container)
        blockContainer = v.findViewById(R.id.following_container)
        rootLayout = v.findViewById(R.id.toggle_button)
        toggleOnText = v.findViewById(R.id.toggle_on_text)
        toggleOffText = v.findViewById(R.id.toggle_off_text)
        toggleOnImage = v.findViewById(R.id.toggle_on_image)
        toggleOffImageView = v.findViewById(R.id.toggle_on_image_followed)



        updateState()
        setOnClickListener(this)
    }

    fun updateState() {
        isSelected = state
        showFollowButton()
    }


    private fun showFollowButton() {
        if (state) {
            blockContainer.visibility = View.VISIBLE
            followContainer.visibility = View.INVISIBLE

        } else {
            blockContainer.visibility = View.INVISIBLE
            followContainer.visibility = View.VISIBLE
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
        val snackBarMetaData = followChangeListener?.showSnackBarOnFollowChange(this)
        if (!state) {
            followSnackBar?.dismiss()
            if (snackBarMetaData != null) {
                followSnackBar =
                    CustomSnackBar.showUnfollowSnackBar(this, context, snackBarMetaData)
                AnalyticsHelper2.logFollowBlockSnackbarViewEvent(snackBarMetaData.referrer,
                    Constants.BLOCK)
                followSnackBar?.show()
            }
        } else {
            followSnackBar?.dismiss()
            if (snackBarMetaData != null) {
                followSnackBar = CustomSnackBar.showSnackBar(this, context, snackBarMetaData)
                AnalyticsHelper2.logFollowBlockSnackbarViewEvent(snackBarMetaData.referrer,
                    Constants.FOLLOW)
                followSnackBar?.show()
            }
        }
    }

    fun setOnFollowChangeListener(followChangeListener: FollowChangeListerner) {
        this.followChangeListener = followChangeListener
    }

    interface FollowChangeListerner {
        fun onFollowChange(newstate: Boolean, reason: FollowUnFollowReason?)
        fun showSnackBarOnFollowChange(nhFollowBlockButton: NHFollowBlockButton): FollowSnackBarMetaData?
        fun undoFollowOrBlockAction(referrer: PageReferrer?,followActionType: FollowActionType)
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
    constructor(
        val title: String,
        val actionMessage: String? = null,
        var nhFollowBlockButton: NHFollowBlockButton,
        val snackBarActionClickListener: NHFollowBlockButton.FollowChangeListerner? = null,
        val displayDuration: Long = PreferenceManager.getPreference(
            AppStatePreference
                .FOLLOWED_SNACKBAR_DISPLAY_DURATION,
                DEFAULT_SNACKBAR_DISPLAY_DURATION
        ),
        val referrer:PageReferrer? = null,
        val snackBarLayoutParams: SnackBarLayoutParams? = getDefaultSnackBarParams()
    )


}


