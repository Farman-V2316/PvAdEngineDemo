/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.common.view.dbgCode
import com.newshunt.appview.R
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.news.view.entity.ReadMoreStatusType

/**
 * Used to hold and update read-more error layout in news detail.
 * Not using [ErrorMessageBuilder] because there is additional logic around showing 'read-more'
 * button, and a static method will not suffice because we don't want findViewById lookups
 * everytime we want to modify the parent.
 *
 *
 * @author satosh.dhanyamraju
 */
class ReadMoreLayoutViewHolder(val layout: ViewGroup, val listener: View.OnClickListener) {

    internal var lastState: ReadMoreStatusType? = null
    internal val messageView: TextView = layout.findViewById(R.id.refresh_error_msg)
    internal val errorMessageAction: TextView = layout.findViewById(R.id.error_action)
    internal val errorCode : NHTextView = layout.findViewById(R.id.error_code_msg_news_detail)
    internal val newsProgressBar: ProgressBar = layout.findViewById(R.id.progressbar)
    internal val readOrRefreshSubParent: RelativeLayout = layout.findViewById(R.id
            .read_or_Refresh_subparent)
    internal val seperator: View = layout.findViewById(R.id.error_seperator)
    internal val errorMessageIcon: ImageView = layout.findViewById(R.id.news_error_msg_icon)
    internal val openSettingsListener = View.OnClickListener {
        layout.context.startActivity(ErrorMessageBuilder.nwSettingIntent)
    }


    fun show() {
        readOrRefreshSubParent.visibility = View.VISIBLE
        messageView.visibility = View.VISIBLE
        errorMessageIcon.visibility = View.VISIBLE
        errorMessageAction.visibility = View.VISIBLE
        errorCode.visibility = View.VISIBLE
        seperator.visibility = View.VISIBLE
    }

    fun hide() {
        lastState = null
        readOrRefreshSubParent.visibility = View.GONE
        messageView.visibility = View.GONE
        errorMessageIcon.visibility = View.GONE
        errorMessageAction.visibility = View.GONE
        errorCode.visibility = View.GONE
        seperator.visibility = View.GONE
    }

    fun updateState(status: ReadMoreStatusType, baseError: BaseError?) {
        lastState = status;
        if (baseError?.message != null) {
            val iconResourceId = ErrorMessageBuilder.errorIcon(layout.context, baseError.message!!)
            errorMessageIcon.setImageResource(iconResourceId)
            errorMessageIcon.tag = iconResourceId
            messageView.text = baseError.message
            errorCode.text= baseError?.dbgCode().get()
        }

        when (status) {

            ReadMoreStatusType.READ_MORE -> {
                errorMessageAction.visibility = View.VISIBLE
                errorMessageAction.setOnClickListener(listener)
                errorMessageAction.text = CommonUtils.getString(R.string.read_more_botton)
                messageView.visibility = View.VISIBLE
                errorCode.visibility = View.VISIBLE
                newsProgressBar.visibility = View.GONE
                seperator.visibility = View.GONE
                errorMessageIcon.visibility = View.VISIBLE
            }

            ReadMoreStatusType.REFRESH -> {
                errorMessageAction.visibility = View.VISIBLE
                errorMessageAction.setOnClickListener(listener)
                errorMessageAction.text = CommonUtils.getString(R.string.dialog_button_retry)
                messageView.visibility = View.VISIBLE
                seperator.visibility = View.VISIBLE
                errorCode.visibility = View.VISIBLE
                newsProgressBar.visibility = View.GONE
                errorMessageIcon.visibility = View.VISIBLE
            }

            ReadMoreStatusType.NW_SETTING -> {
                errorMessageAction.visibility = View.VISIBLE
                errorMessageAction.setOnClickListener(openSettingsListener)
                errorMessageAction.text = CommonUtils.getString(R.string.action_settings)
                messageView.visibility = View.VISIBLE
                errorCode.visibility = View.VISIBLE
                seperator.visibility = View.VISIBLE
                newsProgressBar.visibility = View.GONE
                errorMessageIcon.visibility = View.VISIBLE
            }

            ReadMoreStatusType.LOADING -> {
                errorMessageAction.visibility = View.GONE
                errorMessageAction.setOnClickListener(null)
                newsProgressBar.visibility = View.VISIBLE
                messageView.visibility = View.GONE
                errorCode.visibility = View.GONE
                seperator.visibility = View.GONE
                errorMessageIcon.visibility = View.GONE
            }
        }
    }

    fun updateCTA(networkConnected: Boolean) {
        if (lastState == ReadMoreStatusType.REFRESH || lastState == ReadMoreStatusType.NW_SETTING) {
            if (networkConnected) {
                errorMessageAction.setOnClickListener(listener)
                errorMessageAction.text = CommonUtils.getString(R.string.dialog_button_retry)
            } else {
                errorMessageAction.setOnClickListener(openSettingsListener)
                errorMessageAction.text = CommonUtils.getString(R.string.action_settings)
            }
        }
    }
}