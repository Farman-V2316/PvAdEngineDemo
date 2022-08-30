/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview

import android.app.Activity
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.util.R.string
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dhutil.R
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.increaseTouch
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.squareup.otto.Subscribe
import java.lang.ref.WeakReference

/**
 * Custom snack bar to be shown on follow
 * @author Madhuri.pa
 */

private const val TAG = "FollowSnackBar"

interface SnackBarActionClickListener {
    fun onSnackBarAction(context: Context, messageDisplayed: String)
}

interface FollowSnackBarLayoutInterface {
    fun getSnackBarLayoutParams(): SnackBarLayoutParams?
}

class CustomSnackBar {
    companion object {

        @JvmStatic
        fun showSnackBar(
            view: View, context: Context, snackBarMetaData:
            FollowSnackBarMetaData?
        ): Snackbar? {
            snackBarMetaData ?: return null
            val snackBarMessage = CommonUtils.getString(string
                    .follow_item_added_snackbar_text, snackBarMetaData.title)
            val htmlString = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(snackBarMessage), HtmlCompat
                    .FROM_HTML_MODE_LEGACY) as Spannable

            val duration = if (snackBarMetaData.displayDuration <= 0)
                NHFollowButton.DEFAULT_SNACKBAR_DISPLAY_DURATION.toInt() else snackBarMetaData
                    .displayDuration.toInt()

            val actionMessageTxt = snackBarMetaData.actionMessage
                    ?: CommonUtils.getString(string.read_now)

            val snackBar = Snackbar.make(view, Constants.EMPTY_STRING, duration)
            snackBar.addCallback(SnackBarCallback)
            val snackBarView = snackBar.view as Snackbar.SnackbarLayout
            val params = snackBarView.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = snackBarMetaData.snackBarLayoutParams?.bottomMargin ?: CommonUtils
                    .getDimension(R.dimen.snackbar_bottom_margin_no_bottom_bar)
            params.leftMargin = snackBarMetaData.snackBarLayoutParams?.leftMargin ?: CommonUtils
                    .getDimension(R.dimen.snackbar_margin)
            params.rightMargin = snackBarMetaData.snackBarLayoutParams?.rightMargin ?: CommonUtils
                    .getDimension(R.dimen.snackbar_margin)
            snackBarView.layoutParams = params
            val mainTextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as
                    TextView
            val actionTextView = snackBarView.findViewById(com.google.android.material.R.id
                    .snackbar_action) as
                    TextView
            mainTextView.visibility = View.INVISIBLE
            actionTextView.visibility = View.INVISIBLE
            val customView = if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(),
                            Constants.URDU_LANGUAGE_CODE)) {
                LayoutInflater.from(context).inflate(R.layout
                        .layout_custom_snackbar_urdu, null)
            } else {
                LayoutInflater.from(context).inflate(R.layout
                        .layout_custom_snackbar, null)
            }
            val message = customView.findViewById<NHTextView>(R.id.snackbar_message)
            // removed the use of attr for textcolor in xml, was causing crash when same layout
            // file is getting used in webView
            message.setTextColor(ThemeUtils.getThemeColorByAttribute(context,
                    R.attr.snackbar_msg_color))
            message.setSpannableText(htmlString,snackBarMessage)

            val actionMessage = customView.findViewById<NHTextView>(R.id.snackbar_action_message)
            actionMessage.text = actionMessageTxt ?: CommonUtils.getString(string.read_now)

            snackBarMetaData.snackBarActionClickListener?.let { listener ->
                actionMessage.setOnClickListener {
                    snackBar.dismiss()
                    listener.onSnackBarAction(context, snackBarMessage)
                }
            }

            val backgroundDrawable = ThemeUtils.getThemeDrawableByAttribute(context, R.attr
                    .snackbar_background_color, View.NO_ID)
            snackBarView.setBackgroundResource(backgroundDrawable)
            snackBarView.addView(customView, 0)
            return snackBar
        }

        @JvmStatic
        fun showSnackBar(
            view: View, context: Context, snackBarMetaData:
            NHFollowBlockButton.FollowSnackBarMetaData?
        ): Snackbar? {
            snackBarMetaData ?: return null
            val snackBarMessage = CommonUtils.getString(string
                .follow_item_added_snackbar_text, snackBarMetaData.title)
            val htmlString = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(snackBarMessage), HtmlCompat
                .FROM_HTML_MODE_LEGACY) as Spannable

            val duration = if (snackBarMetaData.displayDuration <= 0)
                NHFollowButton.DEFAULT_SNACKBAR_DISPLAY_DURATION.toInt() else snackBarMetaData
                .displayDuration.toInt()

            val actionMessageTxt = snackBarMetaData.actionMessage
                ?: CommonUtils.getString(string.read_now)

            val snackBar = Snackbar.make(view, Constants.EMPTY_STRING, duration)
            snackBar.addCallback(SnackBarCallback)
            val snackBarView = snackBar.view as Snackbar.SnackbarLayout
            val params = snackBarView.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = snackBarMetaData.snackBarLayoutParams?.bottomMargin ?: CommonUtils
                .getDimension(R.dimen.snackbar_bottom_margin_no_bottom_bar)
            params.leftMargin = snackBarMetaData.snackBarLayoutParams?.leftMargin ?: CommonUtils
                .getDimension(R.dimen.snackbar_margin)
            params.rightMargin = snackBarMetaData.snackBarLayoutParams?.rightMargin ?: CommonUtils
                .getDimension(R.dimen.snackbar_margin)
            snackBarView.layoutParams = params
            val mainTextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as
                    TextView
            val actionTextView = snackBarView.findViewById(com.google.android.material.R.id
                .snackbar_action) as
                    TextView
            mainTextView.visibility = View.INVISIBLE
            actionTextView.visibility = View.INVISIBLE
            val customView = if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(),
                    Constants.URDU_LANGUAGE_CODE)) {
                LayoutInflater.from(context).inflate(R.layout
                    .layout_custom_snackbar_urdu, null)
            } else {
                LayoutInflater.from(context).inflate(R.layout
                    .layout_custom_snackbar, null)
            }
            val message = customView.findViewById<NHTextView>(R.id.snackbar_message)
            // removed the use of attr for textcolor in xml, was causing crash when same layout
            // file is getting used in webView
            message.setTextColor(ThemeUtils.getThemeColorByAttribute(context,
                R.attr.snackbar_msg_color))
            message.setSpannableText(htmlString,snackBarMessage)

            val actionMessage = customView.findViewById<NHTextView>(R.id.snackbar_action_message)
            actionMessage.text = actionMessageTxt ?: CommonUtils.getString(string.read_now)

            snackBarMetaData.snackBarActionClickListener?.let { listener ->
                actionMessage.setOnClickListener {
                    snackBar.dismiss()
                    listener.undoFollowOrBlockAction(snackBarMetaData.referrer,FollowActionType.UNFOLLOW) // since this is follow snackbar hence it should UNFOLLOW
                }
            }

            val backgroundDrawable = ThemeUtils.getThemeDrawableByAttribute(context, R.attr
                .snackbar_background_color, View.NO_ID)
            snackBarView.setBackgroundResource(backgroundDrawable)
            snackBarView.addView(customView, 0)
            return snackBar
        }


        @JvmStatic
        fun showUnfollowSnackBar(
            view: View, context: Context,
            snackBarMetaData: FollowSnackBarMetaData?
        ): Snackbar? {
            snackBarMetaData ?: return null

            val snackBarMessage: String = CommonUtils.getString(com.newshunt.common.util.R.string.np_unfollowing_text,
                    snackBarMetaData.title)
            val duration: Int = if (snackBarMetaData.displayDuration.toInt() <= 0) {
                NHFollowButton.DEFAULT_SNACKBAR_DISPLAY_DURATION.toInt()
            } else {
                snackBarMetaData.displayDuration.toInt()
            }

            val snackBar = Snackbar.make(view, Constants.EMPTY_STRING, duration)
            snackBar.addCallback(SnackBarCallback)
            val snackBarView = snackBar.view as Snackbar.SnackbarLayout
            val params = snackBarView.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = snackBarMetaData.snackBarLayoutParams?.bottomMargin ?: CommonUtils
                    .getDimension(R.dimen.snackbar_bottom_margin_no_bottom_bar)
            params.leftMargin = snackBarMetaData.snackBarLayoutParams?.leftMargin ?: CommonUtils
                    .getDimension(R.dimen.snackbar_margin)
            params.rightMargin = snackBarMetaData.snackBarLayoutParams?.rightMargin ?: CommonUtils
                    .getDimension(R.dimen.snackbar_margin)
            snackBarView.layoutParams = params
            val mainTextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as
                    TextView
            val actionTextView = snackBarView.findViewById(com.google.android.material.R.id
                    .snackbar_action) as
                    TextView
            mainTextView.visibility = View.INVISIBLE
            actionTextView.visibility = View.INVISIBLE
            val customView = if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(),
                            Constants.URDU_LANGUAGE_CODE)) {
                LayoutInflater.from(context).inflate(R.layout
                        .layout_custom_snackbar_urdu, null)
            } else {
                LayoutInflater.from(context).inflate(R.layout
                        .layout_custom_snackbar, null)
            }
            val message = customView.findViewById<NHTextView>(R.id.snackbar_message)
            message.setTextColor(ThemeUtils.getThemeColorByAttribute(context,
                    R.attr.snackbar_msg_color))
            message.text = snackBarMessage

            val actionMessage = customView.findViewById<NHTextView>(R.id.snackbar_action_message)
            actionMessage?.visibility = View.GONE

            val backgroundDrawable = ThemeUtils.getThemeDrawableByAttribute(context, R.attr
                    .snackbar_background_color, View.NO_ID)
            snackBarView.setBackgroundResource(backgroundDrawable)
            snackBarView.addView(customView, 0)
            return snackBar
        }

        @JvmStatic
        fun showUnfollowSnackBar(
            view: View, context: Context,
            snackBarMetaData: NHFollowBlockButton.FollowSnackBarMetaData?
        ): Snackbar? {
            snackBarMetaData ?: return null

            val snackBarMessage: String = CommonUtils.getString(com.newshunt.common.util.R.string.np_unblock_text,
                snackBarMetaData.title)
            val duration: Int = if (snackBarMetaData.displayDuration.toInt() <= 0) {
                NHFollowButton.DEFAULT_SNACKBAR_DISPLAY_DURATION.toInt()
            } else {
                snackBarMetaData.displayDuration.toInt()
            }

            val snackBar = Snackbar.make(view, Constants.EMPTY_STRING, duration)
            snackBar.addCallback(SnackBarCallback)
            val snackBarView = snackBar.view as Snackbar.SnackbarLayout
            val params = snackBarView.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = snackBarMetaData.snackBarLayoutParams?.bottomMargin ?: CommonUtils
                .getDimension(R.dimen.snackbar_bottom_margin_no_bottom_bar)
            params.leftMargin = snackBarMetaData.snackBarLayoutParams?.leftMargin ?: CommonUtils
                .getDimension(R.dimen.snackbar_margin)
            params.rightMargin = snackBarMetaData.snackBarLayoutParams?.rightMargin ?: CommonUtils
                .getDimension(R.dimen.snackbar_margin)
            snackBarView.layoutParams = params
            val mainTextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as
                    TextView
            val actionTextView = snackBarView.findViewById(com.google.android.material.R.id
                .snackbar_action) as
                    TextView
            mainTextView.visibility = View.INVISIBLE
            actionTextView.visibility = View.INVISIBLE
            val customView = if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(),
                    Constants.URDU_LANGUAGE_CODE)) {
                LayoutInflater.from(context).inflate(R.layout
                    .layout_custom_snackbar_urdu, null)
            } else {
                LayoutInflater.from(context).inflate(R.layout
                    .layout_custom_snackbar, null)
            }
            val message = customView.findViewById<NHTextView>(R.id.snackbar_message)
            message.setTextColor(ThemeUtils.getThemeColorByAttribute(context,
                R.attr.snackbar_msg_color))
            message.text = snackBarMessage

            val actionMessage = customView.findViewById<NHTextView>(R.id.snackbar_action_message)
            actionMessage.text = snackBarMetaData.actionMessage ?: CommonUtils.getString(com.newshunt.common.util.R.string.read_now)

            snackBarMetaData.snackBarActionClickListener?.let { listener ->
                actionMessage.setOnClickListener {
                    snackBar.dismiss()
                    listener.undoFollowOrBlockAction(snackBarMetaData.referrer,FollowActionType.UNBLOCK) // since this is block snackbar hence it should UNBLOCK
                }
            }

            val backgroundDrawable = ThemeUtils.getThemeDrawableByAttribute(context, R.attr
                .snackbar_background_color, View.NO_ID)
            snackBarView.setBackgroundResource(backgroundDrawable)
            snackBarView.addView(customView, 0)
            return snackBar
        }


    }

}

data class SnackBarLayoutParams(val leftMargin: Int = CommonUtils.getDimension(R.dimen.snackbar_margin),
                                val rightMargin: Int = CommonUtils.getDimension(R.dimen.snackbar_margin),
                                val bottomMargin: Int = CommonUtils.getDimension(R.dimen.snackbar_bottom_margin_no_bottom_bar))

fun getDefaultSnackBarParams(): SnackBarLayoutParams {
    return SnackBarLayoutParams()
}

fun getDefaultSnackBarParamsWithBottomBar(): SnackBarLayoutParams {
    return SnackBarLayoutParams(bottomMargin = CommonUtils.getDimension(R.dimen.snackbar_bottom_margin))
}

class DismissFollowSnackbarEvent

val SnackBarCallback = object : Snackbar.Callback() {

    private var isRegistered = false
    private var snackbarRef: WeakReference<Snackbar>? = null

    @Subscribe
    fun onDismissEventReceived(event: DismissFollowSnackbarEvent) {
        snackbarRef?.let {
            val snackbar = it.get()
            snackbar?.dismiss()
        }
    }

    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
        super.onDismissed(transientBottomBar, event)
        snackbarRef = null
        if (isRegistered) {
            BusProvider.getUIBusInstance().unregister(this)
            isRegistered = false
        }
    }

    override fun onShown(sb: Snackbar?) {
        super.onShown(sb)
        sb?.let {
            snackbarRef = WeakReference(it)
            if (!isRegistered) {
                BusProvider.getUIBusInstance().register(this)
                isRegistered = true
            }
        }
    }
}

/**
 * Utility function to get Default Snackbar action listener
 */
fun getDefaultSnackBarClickListener(): SnackBarActionClickListener {
    return object : SnackBarActionClickListener {
        override fun onSnackBarAction(context: Context, messageDisplayed: String) {

            CommonNavigator.launchFollowingFeed(context, PageReferrer(NhGenericReferrer.FOLLOW_SNACKBAR))
        }
    }
}
/**
 * Utility function to get undo Default Snackbar action listener
 */
fun getUndoSnackBarClickListener(nhFollowBlockButton: NHFollowBlockButton): SnackBarActionClickListener {
    return object : SnackBarActionClickListener {
        override fun onSnackBarAction(context: Context, messageDisplayed: String) {
            nhFollowBlockButton.setState(false,false)

        }
    }
}

/**
 * Utility to return SnackBarActionListener from Activity, if activity is not NewsDetailInterface
 * returns CustomSnackBar.getDefaultSnackBarClickListener()
 */
fun getSnackBarActionListenerFrom(activity: Activity?): SnackBarActionClickListener? {
    if (activity !is SnackBarActionClickListener) {
        return getDefaultSnackBarClickListener()
    }
    return activity
}


class GenericCustomSnackBar {
    companion object {

        @JvmOverloads
        @JvmStatic
        fun showSnackBar(
                view: View,
                context: Context,
                text: String,
                duration: Int,
                actionType:
                ErrorMessageBuilder.ActionType? = null,
                errorMessageClickedListener: ErrorMessageBuilder.ErrorMessageClickedListener? = null,
                action: String? = null,
                customActionClickListener: View.OnClickListener? = null,
                bottomBarVisible: Boolean? = null,
                spannableString: SpannableString? = null,
                originalString: String? = null,
                increaseTouchAreaBy : Int  = 0,
                isThemeSnackbar: Boolean = false
        ): Snackbar {
            val snackBar = Snackbar.make(view, Constants.EMPTY_STRING, duration)
            val snackBarView = snackBar.view as Snackbar.SnackbarLayout
            val params = snackBarView.layoutParams as ViewGroup.MarginLayoutParams
            if(!isThemeSnackbar){
                if (bottomBarVisible == true) {
                    params.bottomMargin = CommonUtils.getDimension(R.dimen.snackbar_bottom_margin_bottom_bar)
                } else {
                    params.bottomMargin = CommonUtils.getDimension(R.dimen.snackbar_bottom_margin_no_bottom_bar)
                }
                params.leftMargin = CommonUtils.getDimension(R.dimen.snackbar_margin)
                params.rightMargin = CommonUtils.getDimension(R.dimen.snackbar_margin)
            }
            snackBarView.layoutParams = params
            val mainTextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as
                    TextView
            val actionTextView = snackBarView.findViewById(com.google.android.material.R.id
                    .snackbar_action) as
                    TextView
            mainTextView.visibility = View.INVISIBLE
            actionTextView.visibility = View.INVISIBLE
            val customView = if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(),
                            Constants.URDU_LANGUAGE_CODE)) {
                LayoutInflater.from(context).inflate(R.layout
                        .layout_custom_snackbar_urdu, null)
            } else {
                LayoutInflater.from(context).inflate(R.layout
                        .layout_custom_snackbar, null)
            }

            val message = customView.findViewById<NHTextView>(R.id.snackbar_message)
            message.setTextColor(CommonUtils.getColor(R.color.snackbar_background_color_night))
            if (spannableString != null && originalString != null) {
                message.setSpannableText(spannableString, originalString)
            } else {
                message.text = text
            }

            val actionTv: TextView = customView.findViewById<NHTextView>(R.id.snackbar_action_message)
            if(increaseTouchAreaBy > 0) actionTv.increaseTouch(increaseTouchAreaBy)
            if (actionType != null && errorMessageClickedListener != null) {
                actionTv.setText(action)
                errorMessageClickedListener.let { errorMessageClickedListener ->
                    actionTv.setOnClickListener {
                        snackBar.dismiss()
                        if (actionType.equals(ErrorMessageBuilder.ActionType.Retry))
                            errorMessageClickedListener.onRetryClicked(view)
                        if (actionType.equals(ErrorMessageBuilder.ActionType.Home))
                            errorMessageClickedListener.onNoContentClicked(view)

                    }
                }
            } else if (!CommonUtils.isEmpty(action) && customActionClickListener != null) {
                actionTv.text = action
                actionTv.setOnClickListener {
                    snackBar.dismiss()
                    customActionClickListener.onClick(it)
                }
            }

            snackBarView.setBackgroundResource(R.drawable.snackbar_rounded_corner)
            snackBarView.addView(customView, 0)
            return snackBar

        }
    }
}