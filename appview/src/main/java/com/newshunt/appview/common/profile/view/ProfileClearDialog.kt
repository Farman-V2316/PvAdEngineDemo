/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.newshunt.appview.R
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.CommonMessageDialog
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.model.entity.KEY_DIALOG_OPTIONS
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel

/**
 * Dialog helper for clear button in profile section
 *
 * @author by aman.roy on 09-08-2022
 */
class ProfileClearDialog: DialogFragment() {
    private var hostId = -1
    private var useCase: String? = Constants.EMPTY_STRING
    private var resultArgument: Bundle? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = arguments?.getSerializable(KEY_DIALOG_OPTIONS) as? CommonMessageDialogOptions?
        val dialog = Dialog(activity as Context)
        options?.let {
            hostId = it.hostId
            useCase = it.useCase
            resultArgument = it.arguments

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.layout_profile_clear_activity)
            dialog.setCanceledOnTouchOutside(true)

            val window = dialog.window
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setGravity(Gravity.CENTER)

            val positiveBtn = dialog.findViewById<NHTextView>(R.id.dialog_positive_button)
            val negativeBtn = dialog.findViewById<NHTextView>(R.id.dialog_negative_button)

            positiveBtn.setOnClickListener {
                notifyViewModel(CommonMessageEvents.POSITIVE_CLICK)
                dismiss()
            }
            negativeBtn.setOnClickListener {
                dismiss()
            }
        }
        return dialog
    }

    private fun notifyViewModel(event: CommonMessageEvents) {
        activity?.run {
            val viewModel = ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
            viewModel.fragmentCommunicationLiveData.postValue(
                FragmentCommunicationEvent(hostId,
                    event, useCase, resultArgument)
            )
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(options: CommonMessageDialogOptions): ProfileClearDialog {
            val fragment = ProfileClearDialog()
            val bundle = Bundle()
            bundle.putSerializable(KEY_DIALOG_OPTIONS, options)
            fragment.arguments = bundle
            return fragment
        }
    }
}