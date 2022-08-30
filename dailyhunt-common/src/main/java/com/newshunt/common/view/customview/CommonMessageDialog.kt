/*
 * Copyright (c) 2019 . All rights reserved.
 */
package com.newshunt.common.view.customview

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProviders
import com.newshunt.dhutil.R
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.KEY_DIALOG_OPTIONS
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.util.NewsConstants
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel
import java.io.Serializable

/**
 * A common dialog fragment implementation to show a title, message, positive and negative
 * buttons. Communicates the positive and negative button clicks via LiveData hosted in
 * FragmentCommunicationsViewModel
 * <p>
 * Created by srikanth.ramaswamy on 04/25/2019.
 */
class CommonMessageDialog : androidx.fragment.app.DialogFragment(), View.OnClickListener {
    private var hostId = 0
    private var useCase: String? = Constants.EMPTY_STRING
    private var resultArgument: Bundle? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity as Context)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.common_msg_dialog)
        if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(), NewsConstants.URDU_LANGUAGE_CODE)) {
            ViewCompat.setLayoutDirection(dialog.findViewById(R.id.history_dialog_rootview), ViewCompat
                    .LAYOUT_DIRECTION_RTL)
        }
        dialog.setCanceledOnTouchOutside(true)

        val window = dialog.window
        window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.CENTER)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val options = arguments?.getSerializable(KEY_DIALOG_OPTIONS) as? CommonMessageDialogOptions?
        options?.let {
            hostId = it.hostId
            useCase = it.useCase ?: Constants.EMPTY_STRING
            resultArgument = it.arguments
            val titleView = dialog.findViewById<NHTextView>(R.id.clearall_dlg_title)
            val msgView = dialog.findViewById<NHTextView>(R.id.clearall_dlg_msg)
            val positiveView = dialog.findViewById<NHTextView>(R.id.clear_all_positive)
            val negativeView = dialog.findViewById<NHTextView>(R.id.clear_all_negative)
            val errorIconView = dialog.findViewById<ImageView>(R.id.error_icon)
            val iconView = dialog.findViewById<ImageView>(R.id.icon)
            if (CommonUtils.isEmpty(it.title)) {
                titleView.visibility = View.GONE
            } else {
                titleView.text = it.title
            }
            if (CommonUtils.isEmpty(it.message)) {
                msgView.visibility = View.GONE
            } else {
                msgView.text = it.message
            }

            if (CommonUtils.isEmpty(it.positiveButtonText)) {
                positiveView.visibility = View.GONE
            } else {
                positiveView.text = it.positiveButtonText
                positiveView.setOnClickListener(this)
            }

            if (CommonUtils.isEmpty(it.negativeButtonText)) {
                negativeView.visibility = View.GONE
            } else {
                negativeView.text = it.negativeButtonText
                negativeView.setOnClickListener(this)
            }

            if (it.drawable == null) {
                errorIconView.visibility = View.GONE
            } else {
                errorIconView.visibility = View.VISIBLE
                errorIconView.setImageDrawable(it.drawable)
            }
        }
        return dialog
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.clear_all_negative -> {
                notifyViewModel(CommonMessageEvents.NEGATIVE_CLICK)
                dismiss()
            }
            R.id.clear_all_positive -> {
                notifyViewModel(CommonMessageEvents.POSITIVE_CLICK)
                dismiss()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        notifyViewModel(CommonMessageEvents.DISMISS)
    }

    private fun notifyViewModel(event: CommonMessageEvents) {
        activity?.run {
            val viewModel = ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
            viewModel.fragmentCommunicationLiveData.postValue(FragmentCommunicationEvent(hostId,
                    event, useCase, resultArgument))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(options: CommonMessageDialogOptions): CommonMessageDialog {
            val fragment = CommonMessageDialog()
            val bundle = Bundle()
            bundle.putSerializable(KEY_DIALOG_OPTIONS, options)
            fragment.arguments = bundle
            return fragment
        }
    }
}

data class CommonMessageDialogOptions(
    val hostId: Int,
    val title: String?,
    val message: String?,
    val positiveButtonText: String?,
    val negativeButtonText: String?,
    val drawable: Drawable? = null,
    val useCase: String? = Constants.EMPTY_STRING,
    val arguments: Bundle? = null) : Serializable

enum class CommonMessageEvents : Serializable {
    POSITIVE_CLICK,
    NEGATIVE_CLICK,
    DISMISS
}
