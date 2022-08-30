package com.newshunt.appview.common.postcreation.view.customview

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.activity.CreatePostActivity
import com.newshunt.appview.databinding.PostCreationDiscardDialogBinding

class PostCreationDiscardDialog(private val isSystemBackKeyPressed: Boolean = false) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val binding: PostCreationDiscardDialogBinding = DataBindingUtil.inflate(
                inflater, R.layout.post_creation_discard_dialog, null, false
            )
            binding.frag = this
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    fun onDiscard() {
        (activity as? CreatePostActivity?)?.handleBackPress(isSystemBackKeyPressed)
    }

    fun onCancel() {
        dismiss()
    }

}