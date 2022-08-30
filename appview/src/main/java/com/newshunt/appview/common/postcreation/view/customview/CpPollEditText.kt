package com.newshunt.appview.common.postcreation.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import com.newshunt.appview.R
import com.newshunt.common.view.customview.fontview.NHEditText
import com.newshunt.dataentity.common.helper.common.CommonUtils

class CpPollEditText : NHEditText {

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
    }

    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection? {
        val ic = super.onCreateInputConnection(editorInfo)
        EditorInfoCompat.setContentMimeTypes(editorInfo, arrayOf("image/*"))
        val callback = InputConnectionCompat.OnCommitContentListener { _, _, _ ->
            Toast.makeText(
                context, CommonUtils.getString(R.string.cp_no_gif_support), Toast.LENGTH_SHORT
            ).show()
            true
        }
        return if (ic != null) InputConnectionCompat.createWrapper(ic, editorInfo, callback) else null
    }
}