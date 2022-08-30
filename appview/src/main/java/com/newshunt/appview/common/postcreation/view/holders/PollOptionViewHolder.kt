package com.newshunt.appview.common.postcreation.view.holders

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.adapter.PollOptionEditTextCallback
import com.newshunt.appview.common.postcreation.view.customview.PollOptionRelativeLayout
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils

class PollOptionViewHolder(itemView: View,
                           private val callback: PollOptionEditTextCallback,
                           private val maxPollOptionLength: Int) :
        RecyclerView.ViewHolder(itemView) {

    private var pollOptionEdittextView: EditText = itemView.findViewById(R.id.poll_option_etv)
    private var pollOptionETVCharaterCountTextView: NHTextView = itemView.findViewById(
            R.id.poll_option_etv_counter_tv)
    private var pollOptionEtvParentLayout: PollOptionRelativeLayout = itemView.findViewById(
            R.id.poll_option_etv_parent_layout)
    private var pollOptionEditTextCharCount: Int= 0


    fun updateView(position: Int) {
        val pollOptionHintString = CommonUtils.getString(R.string.post_poll_option_hint, position + 1)
        pollOptionEdittextView.hint = pollOptionHintString

        if(position != 1){
            pollOptionEdittextView.requestFocus()
            pollOptionETVCharaterCountTextView.text = maxPollOptionLength.toString()
            updatePollOptionUI(true, isSelected = true, isActivated = true,
                    isError = false, counterTextVisibility = View.VISIBLE,
                    counterTextColor = ContextCompat.getColor(itemView.context,
                            CommonUtils.getResourceIdFromAttribute(itemView.context,
                                    R.attr.cp_poll_text_color)))
        }

        pollOptionEdittextView.onFocusChangeListener = View.OnFocusChangeListener { view, hasfocus ->
            if(hasfocus){
                pollOptionEdittextView.requestFocus()
                if(pollOptionEditTextCharCount == 0){
                    pollOptionETVCharaterCountTextView.text = maxPollOptionLength.toString()
                } else {
                    pollOptionETVCharaterCountTextView.text = pollOptionEditTextCharCount.toString()
                }
                if(pollOptionEditTextCharCount < 0){
                    updatePollOptionUI(true, isSelected = false, isActivated = false,
                            isError = true, counterTextVisibility = View.VISIBLE,
                            counterTextColor = CommonUtils.getColor(
                                    R.color.cp_poll_option_edittext_char_exceed_limit_color))
                } else {
                    updatePollOptionUI(true, isSelected = true, isActivated = true,
                            isError = false, counterTextVisibility = View.VISIBLE,
                            counterTextColor = ContextCompat.getColor(itemView.context,
                                    CommonUtils.getResourceIdFromAttribute(itemView.context,
                                            R.attr.cp_poll_text_color)))
                }
            } else {
                pollOptionEdittextView.clearFocus()
                if(pollOptionEditTextCharCount < 0){
                    updatePollOptionUI(true, isSelected = false, isActivated = false,
                            isError = true, counterTextVisibility = View.VISIBLE,
                            counterTextColor = CommonUtils.getColor(
                                    R.color.cp_poll_option_edittext_char_exceed_limit_color))
                    pollOptionETVCharaterCountTextView.text = pollOptionEditTextCharCount.toString()
                } else {
                    updatePollOptionUI(false, isSelected = false, isActivated = false,
                            isError = false, counterTextVisibility = View.GONE,
                            counterTextColor = ContextCompat.getColor(itemView.context,
                                    CommonUtils.getResourceIdFromAttribute(itemView.context,
                                            R.attr.cp_poll_text_color)))
                }
            }
        }

        val mTextEditorWatcher = object : TextWatcher {
            var changed = true
            val foregroundColorSpan = ForegroundColorSpan(CommonUtils.getColor(
                    R.color.cp_poll_option_edittext_char_exceed_limit_color))
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val stringBuilder = SpannableStringBuilder(s.toString())
                if(s.toString().length > maxPollOptionLength && changed){
                    stringBuilder.setSpan(foregroundColorSpan, maxPollOptionLength,
                            s.toString().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    changed = false
                    pollOptionEdittextView.text = stringBuilder
                }
                pollOptionEdittextView.setSelection(s.toString().length)
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                changed = true
                updatePollOptionEditTextCharCounter(s)
            }
        }
        pollOptionEdittextView.addTextChangedListener(mTextEditorWatcher)
    }

    private fun updatePollOptionEditTextCharCounter(s: Editable) {
        pollOptionEditTextCharCount = maxPollOptionLength - s.length
        pollOptionETVCharaterCountTextView.text = (pollOptionEditTextCharCount).toString()
        if(pollOptionEditTextCharCount < 0){
            // make the background red here
            updatePollOptionUI(isClickable = true, isSelected = false, isActivated = false,
                    isError = true, counterTextVisibility = View.VISIBLE,
                    counterTextColor = CommonUtils.getColor(
                            R.color.cp_poll_option_edittext_char_exceed_limit_color))
        } else {
            updatePollOptionUI(isClickable = true, isSelected = true, isActivated = true,
                    isError = false, counterTextVisibility = View.VISIBLE,
                    counterTextColor = ContextCompat.getColor(itemView.context,
                            CommonUtils.getResourceIdFromAttribute(itemView.context,
                                    R.attr.cp_poll_text_color)))
        }
        callback.getPollOptionEditText(position, s)
        if (s.isEmpty() || s.length > maxPollOptionLength) {
            callback.onTextChanged(false)
        } else {
            callback.onTextChanged(true)
        }
    }

    private fun updatePollOptionUI(isClickable: Boolean, isSelected: Boolean,
                                   isActivated: Boolean, isError: Boolean,
                                   counterTextVisibility: Int, counterTextColor: Int){
        pollOptionEtvParentLayout.isClickable = isClickable
        pollOptionEtvParentLayout.isSelected = isSelected
        pollOptionEtvParentLayout.isActivated = isActivated
        pollOptionEtvParentLayout.isError(isError)
        pollOptionETVCharaterCountTextView.visibility = counterTextVisibility
        pollOptionETVCharaterCountTextView.setTextColor(counterTextColor)
    }

}
