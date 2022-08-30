package com.newshunt.appview.common.postcreation.view.customview

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.newshunt.appview.R

class CreatePostWalkThroughDialog : DialogFragment(), DialogInterface.OnKeyListener, View.OnClickListener {

    companion object {
        @JvmStatic
        fun newInstance(): CreatePostWalkThroughDialog {
            return CreatePostWalkThroughDialog()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val walkThroughDialog = inflater.inflate(R.layout.create_post_walkthrough,
                container, false)
        val cpWalkThroughDialogContainer =
                walkThroughDialog.findViewById<FrameLayout>(R.id.walkthrough_dialog_container)
        cpWalkThroughDialogContainer.setOnClickListener(this)
        dialog?.setOnKeyListener(this)
        return walkThroughDialog
    }

    private fun dismissDialog() {
        try {
            dismiss()
        } catch (e: IllegalStateException) {
            // Do nothing
        }
    }

    override fun onClick(view: View?) {
        if(view == null) return
        if(view.id == R.id.walkthrough_dialog_container){
            dismissDialog()
        }
    }

    override fun onKey(dialogInterface: DialogInterface?, keyCode: Int,
                       keyEvent: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dismissDialog()
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}
