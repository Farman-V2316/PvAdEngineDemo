/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view.customview

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import androidx.core.text.HtmlCompat

import android.util.AttributeSet

import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dhutil.R
import com.newshunt.dhutil.view.listener.ExpandableTVLayoutChangeListener

import java.util.HashSet
import java.util.Stack
import java.util.regex.Pattern

/**
 * Expandable Text View which can be used in Recycler view.
 *
 * Created by karthik.r on 2020-04-12.
 */
class ExpandableRecyclingTextView : NHTextView {
    var layoutChangeListener: ExpandableTVLayoutChangeListener? = null
    private var collapsedMaxLines = DEFAULT_MAX_LINES //Customizable collapsedMaxLines

    private var readMoreTextColor: Int = 0
    private var readMoreTextStyle: Int = 0

    constructor(context: Context) : super(context) {
        buildView(context, null, 0, 0)
    }

    constructor(context: Context,
                attrs: AttributeSet?) : super(context, attrs) {
        buildView(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        buildView(context, attrs, defStyleAttr, 0)
    }

    private fun buildView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        fetchLayoutParameters(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun fetchLayoutParameters(context: Context?, attrs: AttributeSet?, defStyleAttr: Int,
                                      defStyleRes: Int) {
        if (context == null || attrs == null) {
            return
        }
        val array = context.obtainStyledAttributes(attrs, com.newshunt.dhutil.R.styleable.ExpandableText,
                defStyleAttr, defStyleRes)
        val count = array.indexCount
        for (i in 0 until count) {
            val index = array.getIndex(i)

            if (index == com.newshunt.dhutil.R.styleable.ExpandableText_desc_collapsed_max_lines) {
                collapsedMaxLines = array.getInt(index, DEFAULT_MAX_LINES)
            } else if (index == com.newshunt.dhutil.R.styleable.ExpandableText_more_text_color) {
                readMoreTextColor = CommonUtils.getColor(array.getResourceId(index, com.newshunt.dhutil.R.color
                        .white_color))
            } else if (index == R.styleable.ExpandableText_more_text_style) {
                readMoreTextStyle = array.getInt(index, Typeface.NORMAL)
            }
        }
        array.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (layoutChangeListener != null) {
            layoutChangeListener!!.onLayoutChange()
        }

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onPreDraw(): Boolean {
        if (layoutChangeListener?.onLayoutChange() == true) {
            return false
        }

        return super.onPreDraw()
    }

    companion object {
        private val acceptableTagsForExpandableHtml = arrayOf("a")
        private const val ELLIPSIZE_CHARACTER: Char = 0x2026.toChar()
        private const val DEFAULT_MAX_LINES = 5

        fun addLayoutEllipsize(tv: ExpandableRecyclingTextView, maxLine: Int,
                               originalStringWithHtml: String,
                               ellipsizePrefixHtml: String,
                               ellipsizetext: String,
                               ellipsizeSuffixHtml: String) {
            if (tv.tag == null) {
                tv.tag = tv.text
            }

            val htmlTags = getAllHtmlTags(originalStringWithHtml)
            var hasHtmlAndAcceptablesOnly = !htmlTags.isEmpty()
            for (knownTag in acceptableTagsForExpandableHtml) {
                htmlTags.remove(knownTag)
            }

            hasHtmlAndAcceptablesOnly = hasHtmlAndAcceptablesOnly && htmlTags.isEmpty()
            val finalHasHtmlAndAcceptablesOnly = hasHtmlAndAcceptablesOnly
            tv.layoutChangeListener = object: ExpandableTVLayoutChangeListener {
                override fun onLayoutChange() : Boolean {
                    val tagsToClose = Stack<String>()
                    var lineEndIndex = Integer.MIN_VALUE
                    if (maxLine <= 0) {
                        lineEndIndex = tv.layout.getLineEnd(0)
                    } else if (tv.lineCount > maxLine) {
                        lineEndIndex = tv.layout.getLineEnd(maxLine - 1)
                    }

                    if (lineEndIndex != Integer.MIN_VALUE && (lineEndIndex - 1 - ellipsizetext.length) > 0) {
                        val displayedText = tv.text.subSequence(0, lineEndIndex - 1 - ellipsizetext.length).toString()

                        if (!finalHasHtmlAndAcceptablesOnly) {
                            val s = HtmlCompat.fromHtml(displayedText + ELLIPSIZE_CHARACTER + ellipsizePrefixHtml
                                    + ellipsizetext + ellipsizeSuffixHtml,
                                    HtmlCompat.FROM_HTML_MODE_LEGACY)  as Spannable

                            tv.setSpannableText(s, originalStringWithHtml, BufferType.NORMAL)
                            return false
                        }

                        var truncatedHtml = StringBuilder()
                        var truncateIndex = 0
                        var i = 0
                        while (i < originalStringWithHtml.length) {
                            if (originalStringWithHtml[i] == '<') {
                                // Increment till end of tag
                                val tag = StringBuilder()
                                var tagNameEnded = false
                                var isCloseTag = false
                                while (originalStringWithHtml[i] != '>') {
                                    tagNameEnded = tagNameEnded || originalStringWithHtml[i] == ' ' ||
                                            originalStringWithHtml[i] == '\t'
                                    if (!tagNameEnded && originalStringWithHtml[i] != '<' &&
                                            originalStringWithHtml[i] != '/') {
                                        tag.append(originalStringWithHtml[i])
                                    }

                                    if (!tagNameEnded) {
                                        isCloseTag = isCloseTag || originalStringWithHtml[i] == '/'
                                    }

                                    i++
                                }

                                val tagStr = tag.toString()
                                if (isCloseTag) {
                                    if (!tagsToClose.empty() && tagsToClose.peek() == tagStr) {
                                        tagsToClose.pop()
                                    }
                                } else {
                                    tagsToClose.push(tagStr)
                                }

                                i++
                                continue
                            }

                            truncatedHtml = truncatedHtml.append(originalStringWithHtml[i])
                            if (displayedText.length < truncatedHtml.length) {
                                truncateIndex = i - 1
                                break
                            }
                            i++
                        }

                        val closingTags = StringBuilder()
                        while (!tagsToClose.empty()) {
                            val tag = tagsToClose.pop()
                            closingTags.append("</").append(tag).append(">")
                        }

                        val output = (originalStringWithHtml.substring(0, truncateIndex) + ELLIPSIZE_CHARACTER + closingTags
                                + ellipsizePrefixHtml + ellipsizetext + ellipsizeSuffixHtml)
                        val s = HtmlCompat.fromHtml(output, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
                        tv.setSpannableText(s, originalStringWithHtml, BufferType.NORMAL)
                        return true
                    }

                    return false
                }
            }
        }

        private fun getAllHtmlTags(originalStringWithHtml: String): MutableSet<String> {
            val tags = HashSet<String>()
            val p = Pattern.compile("<([^\\s>/]+)")
            val m = p.matcher(originalStringWithHtml)

            while (m.find()) {
                val codeGroup = m.group(1)
                tags.add(codeGroup)
            }

            return tags
        }
    }

}
