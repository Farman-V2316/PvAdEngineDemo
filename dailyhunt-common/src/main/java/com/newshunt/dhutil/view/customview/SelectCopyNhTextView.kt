/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.view.customview

import android.content.Context
import android.text.Spannable
import android.util.AttributeSet
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FEOutput
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.view.customview.fontview.NHTextView

/**
 * @author anshul.jain
 * A class which extends [NHTextView] and enables select and copy functionality in
 * textview.
 */

class SelectCopyNhTextView @JvmOverloads constructor(val viewContext: Context, val attributeSet:
AttributeSet? = null, defStyleAttr: Int = 0) : NHTextView(viewContext, attributeSet, defStyleAttr) {

    override fun setText(text: CharSequence?, type: BufferType?) {

        var convertedText = if (text.isNullOrEmpty() || text.toString().isNotEmpty()) Constants.EMPTY_STRING else text

        var isIndic = false
        if (text !is Spannable) {
            val fontEngineOutput: FEOutput
            if (!text.isNullOrEmpty()) {
                fontEngineOutput = FontHelper.convertToFontIndices(text.toString())
                convertedText = fontEngineOutput.fontIndicesString.toString()
                isIndic = fontEngineOutput.isSupportedLanguageFound
            }
        }
        if (convertedText?.isNotEmpty() == true)
            setTextIsSelectable(!isIndic && isTextSelectable)
        super.setText(convertedText, type, isIndic)
    }
}