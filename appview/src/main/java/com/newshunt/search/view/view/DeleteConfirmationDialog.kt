/*
 * Created by Rahul Ravindran at 26/9/19 7:12 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.search.view.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.newshunt.appview.R
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils


/**
 * @author shrikant.agrawal
 */

interface DeleteSearchListener {
    fun onDeleteConfirmation()
}

class DeleteConfirmationDialog : androidx.fragment.app.DialogFragment() {

    private var type = TYPE_ALL
    private var text = ""
    private var listener: DeleteSearchListener?= null

    companion object {

        const val TYPE_ALL = 1
        const val TYPE_SINGLE = 2

        fun createDialog(text: String = "", type: Int = TYPE_ALL, listener: DeleteSearchListener) : DeleteConfirmationDialog {
            val dialog = DeleteConfirmationDialog()
            dialog.type = type
            dialog.text = text
            dialog.listener = listener
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity as Context)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_profile_clear_activity)
        dialog.setCanceledOnTouchOutside(true)
        
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)

        val titleView = dialog.findViewById<NHTextView>(R.id.profile_dialog_title)
        titleView.visibility = View.GONE
        val desc = dialog.findViewById<NHTextView>(R.id.profile_dialog_subtitle)
        val clearView = dialog.findViewById<NHTextView>(R.id.dialog_positive_button)
        clearView.text = CommonUtils.getString(R.string.clear_text)
        val cancelView = dialog.findViewById<NHTextView>(R.id.dialog_negative_button)
        cancelView.text = CommonUtils.getString(R.string.cancel_text)

        setTitle(desc)
        setClearView(clearView)
        setCancelView(cancelView)
        return dialog
    }

    private fun setTitle(titleView: NHTextView) {
        if (type == TYPE_ALL) {
            titleView.setText(R.string.all_clear_title_text)
        } else {
            titleView.setText(CommonUtils.getString(R.string.single_clear_title_text, text))
        }
    }

    private fun setClearView(clearView:NHTextView) {
        clearView.setOnClickListener{
            listener?.onDeleteConfirmation()
            dismiss()
        }

    }

    private fun setCancelView(cancelView: NHTextView) {
        cancelView.setOnClickListener { dialog!!.dismiss() }
    }

}