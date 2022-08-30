package com.newshunt.appview.common.postcreation.view.customview

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.dataentity.model.entity.KEY_DIALOG_OPTIONS
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel

/**
 * @author amitkumar.chaudhary
 * */
class PostDeleteDialog() : DialogFragment() {
    private var hostId = 0
    private var useCase: String? = Constants.EMPTY_STRING
    private var resultArgument: Bundle? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = arguments?.getSerializable(KEY_DIALOG_OPTIONS) as? CommonMessageDialogOptions
        options?.let {
            hostId = it.hostId
            useCase = it.useCase ?: Constants.EMPTY_STRING
            resultArgument = it.arguments
        }
        return activity?.let { activity ->
            val builder = AlertDialog.Builder(activity)
            val inflater = LayoutInflater.from(activity)

            val binding: ViewDataBinding = DataBindingUtil.inflate(
                    inflater, R.layout.post_delete_dialog, null, false
            )
            binding.setVariable(BR.frag, this)
            binding.setVariable(BR.headingText, options?.title)
            binding.setVariable(BR.messageText, options?.message)
            binding.setVariable(BR.negativeText, options?.negativeButtonText)
            binding.setVariable(BR.positiveText, options?.positiveButtonText)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    fun onNegativeClick() {
        activity?.run {
            val viewModel = ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
            viewModel.fragmentCommunicationLiveData.postValue(FragmentCommunicationEvent(hostId,
                    CommonMessageEvents.NEGATIVE_CLICK, useCase, resultArgument))
        }
        dismiss()
    }

    fun onPositiveClick() {
        activity?.run {
            val viewModel = ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
            viewModel.fragmentCommunicationLiveData.postValue(FragmentCommunicationEvent(hostId,
                    CommonMessageEvents.POSITIVE_CLICK, useCase, resultArgument))
        }
        dismiss()
    }

    companion object {
        @JvmStatic
        fun newInstance(options: CommonMessageDialogOptions): PostDeleteDialog {
            val fragment = PostDeleteDialog()
            val bundle = Bundle()
            bundle.putSerializable(KEY_DIALOG_OPTIONS, options)
            fragment.arguments = bundle
            return fragment
        }
    }
}