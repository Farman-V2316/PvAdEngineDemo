package com.newshunt.common.view.customview.fontview

import android.graphics.Paint
import android.text.SpannableStringBuilder
import com.newshunt.common.helper.font.FontHelper
import android.text.TextUtils
import com.newshunt.common.helper.font.FontWeight
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.util.*

object TextMeasurementUtils {
    /**
     * Split text into lines using specified parameters
     *
     *
     * parameters include:-
     *
     * text whose number of lines is to calculated
     * startMarginInDp from screen start,
     * endMarginInDp from screen end,
     * weight of the textview
     * langCode if applicable, if not passed application language will be assumed
     *
     *
     * Based on weight & langCode typeface is chosen and applied to paint which helps in better calculation of no of lines
     *
     */
    fun getTextLines(text: CharSequence?, startMarginInDp: Int, endMarginInDp: Int, weight: FontWeight, langCode: String?, fontSizeInDps: Int?): Int {
        if (text.isNullOrEmpty()) {
            return 0
        }
        val width = CommonUtils.getDeviceScreenWidth()
        val currentSize = width - CommonUtils.getPixelFromDP(startMarginInDp, CommonUtils.getApplication()) - CommonUtils.getPixelFromDP(endMarginInDp, CommonUtils.getApplication())
        val paint = Paint()
        paint.typeface = FontHelper.getTypeFaceFor(langCode, weight.weightEnumValue)
        if(fontSizeInDps != null){
            paint.textSize = CommonUtils.getPixelFromDP(fontSizeInDps, CommonUtils.getApplication()).toFloat()
        }else{
            paint.textSize = CommonUtils.getPixelFromDP(21, CommonUtils.getApplication()).toFloat()
        }
        return splitWordsIntoStringsThatFit(text, currentSize.toFloat(), paint, weight, langCode).size
    }

    private fun splitWordsIntoStringsThatFit(source: CharSequence, maxWidthPx: Float, paint: Paint, weight: FontWeight, langCode: String?): List<CharSequence> {
        val result = ArrayList<CharSequence>()
        val currentLine = ArrayList<CharSequence?>()
        val sources = source.split(" ").toTypedArray()
        for (chunk in sources) {
            if (paint.measureText(chunk, 0, chunk.length) < maxWidthPx) {
                processFitChunk(maxWidthPx, paint, result, currentLine, chunk)
            } else {
                //the chunk is too big, split it.
                val splitChunk = splitIntoStringsThatFit(chunk, maxWidthPx, paint)
                for (chunkChunk in splitChunk) {
                    processFitChunk(maxWidthPx, paint, result, currentLine, chunkChunk)
                }
            }
        }
        if (!currentLine.isEmpty()) {
            result.add(TextUtils.join(" ", currentLine))
        }
        return result
    }

    /**
     * Splits a string to multiple strings each of which does not exceed the width
     * of maxWidthPx.
     */
    private fun splitIntoStringsThatFit(source: CharSequence, maxWidthPx: Float, paint: Paint): List<CharSequence> {
        if (TextUtils.isEmpty(source) || paint.measureText(source, 0, source.length) <= maxWidthPx) {
            return Arrays.asList(source)
        }
        val result = ArrayList<CharSequence>()
        var start = 0
        for (i in 1..source.length) {
            val substr = source.subSequence(start, i)
            if (paint.measureText(substr, 0, substr.length) >= maxWidthPx) {
                //this one doesn't fit, take the previous one which fits
                val fits = source.substring(start, i - 1)
                result.add(fits)
                start = i - 1
            }
            if (i == source.length) {
                val fits = source.subSequence(start, i)
                result.add(fits)
            }
        }
        return result
    }

    /**
     * Processes the chunk which does not exceed maxWidth.
     */
    private fun processFitChunk(maxWidth: Float, paint: Paint, result: ArrayList<CharSequence>, currentLine: ArrayList<CharSequence?>, chunk: CharSequence) {
        currentLine.add(chunk)
        var currentLineStr: SpannableStringBuilder = SpannableStringBuilder()
        for(sequence in currentLine){
            if(currentLineStr.length != 0){
                currentLineStr.append(" ")
            }
            currentLineStr.append(sequence)
        }

        if (paint.measureText(currentLineStr, 0, currentLineStr.length) >= maxWidth) {
            //remove chunk
            currentLine.removeAt(currentLine.size - 1)
            result.add(TextUtils.join(" ", currentLine))
            currentLine.clear()
            //ok because chunk fits
            currentLine.add(chunk)
        }
    }
}