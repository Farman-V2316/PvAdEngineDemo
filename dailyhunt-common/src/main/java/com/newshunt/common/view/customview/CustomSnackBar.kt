/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview

import android.content.Context
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.increaseTouch
import com.newshunt.dhutil.view.ErrorMessageBuilder

/**
 * Custom snack bar to be shown on follow
 * @author Madhuri.pa
 */

private const val TAG = "FollowSnackBar"

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