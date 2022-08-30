/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview.fontview

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.AttributeSet
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FEOutput
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.font.NHCommonTextViewUtil
import com.newshunt.common.util.R

/**
 * Listener to apply NH Fonts.
 *
 * @author priya.gupta
 */
class NHRadioButton : androidx.appcompat.widget.AppCompatRadioButton, NHFontView {

    private var nhCommonTextUtil: NHCommonTextViewUtil? = null
    private var style: Int = 0
    private var customFontWeight = -1;

    constructor(context: Context) : super(context, null) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.NHRadioButton)
        customFontWeight = typedArray.getInt(R.styleable.NHRadioButton_dh_custom_font_weight, -1)
        typedArray.recycle()
        FontHelper.initTextView(this, context, attributeSet, customFontWeight)

        if(customFontWeight != -1){
            //re-set text to apply custom_font_weight based font
            setText(this.text, nhCommonTextUtil?.bufferType)
        }
    }

    private fun initCommonTextUtil() {
        if (null == nhCommonTextUtil) {
            nhCommonTextUtil = NHCommonTextViewUtil()
        }
    }


    override fun setText(text: CharSequence?, type: BufferType?) {
        var text = text
        if (text == null) {
            text = Constants.EMPTY_STRING
        }
        var isIndic = false
        if (text !is Spannable) {
            if (text != null && text.length > 0) {
                val fontEngineOutput: FEOutput
                fontEngineOutput = FontHelper.convertToFontIndices(text.toString())
                text = fontEngineOutput.fontIndicesString.toString()
                isIndic = fontEngineOutput.isSupportedLanguageFound

            }
        }
        initCommonTextUtil()
        setPadding(isIndic)
        if (nhCommonTextUtil!!.setTextRequired(text, type)) {
            val s = nhCommonTextUtil!!.getSpannableString(text, isIndic, style, customFontWeight)
            if (style == Typeface.BOLD) {
                val boldSpan = StyleSpan(Typeface.BOLD)
                s.setSpan(boldSpan, 0, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            super.setText(s, type)
        }
    }

    override fun setTypeface(tf: Typeface?) {
        // do nothing
    }

    override fun setTypeface(tf: Typeface?, style: Int) {
        FontHelper.setStyle(this, style)
        this.style = style
    }

    override fun setCurrentTypeface(currentTypeface: Typeface) {
        initCommonTextUtil()
        nhCommonTextUtil!!.setCurrentTypeface(currentTypeface)
    }

    override fun setPadding(isIndic: Boolean) {
        initCommonTextUtil()
        nhCommonTextUtil!!.setPadding(this, isIndic)
    }
}
