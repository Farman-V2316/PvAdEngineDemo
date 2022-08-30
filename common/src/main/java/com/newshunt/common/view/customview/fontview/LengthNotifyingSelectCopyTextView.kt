/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview.fontview

import android.content.Context
import android.os.Parcelable
import android.text.Spannable
import android.util.AttributeSet
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FontHelper

/**
 * @author anshul.jain
 * A class which extends [LengthNotifyingTextView] and enables select and copy functionality in
 * textview.
 */
class LengthNotifyingSelectCopyTextView @JvmOverloads constructor(val viewContext: Context, val
attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) : LengthNotifyingTextView(viewContext, attributeSet, defStyleAttr) {

    override fun setText(text: CharSequence?, type: BufferType?) {

        var convertedText = if (text.isNullOrEmpty()) Constants.EMPTY_STRING else text

        var isIndic = false
        if (text !is Spannable && convertedText?.isNotEmpty() == true) {
            val fontEngineOutput = FontHelper.convertToFontIndices(text.toString())
            convertedText = fontEngineOutput.fontIndicesString.toString()
            isIndic = fontEngineOutput.isSupportedLanguageFound
        }

        if (convertedText?.isNotEmpty() == true) {
            setTextIsSelectable(!isIndic && isTextSelectable)
        }
        super.setText(convertedText, type, isIndic)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(null)
    }
}