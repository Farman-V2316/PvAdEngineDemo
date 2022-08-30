/*
 * Created by Rahul Ravindran at 17/9/19 5:51 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.postcreation.view.customview

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.linkedin.android.spyglass.mentions.MentionSpan
import com.linkedin.android.spyglass.mentions.Mentionable
import com.linkedin.android.spyglass.mentions.MentionsEditable
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver
import com.linkedin.android.spyglass.ui.MentionsEditText
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.helper.PostConstants
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FEOutput
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.font.NHCommonTextViewUtil
import com.newshunt.common.view.customview.fontview.NHEditText
import com.newshunt.common.view.customview.fontview.NHFontView
import com.newshunt.dataentity.common.asset.Added
import com.newshunt.dataentity.common.asset.Delete
import com.newshunt.dataentity.common.asset.PartialDelete
import com.newshunt.dataentity.common.asset.Phrase
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SearchSuggestionType

class NHCreatePostEditText : MentionsEditText, NHFontView {

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
            context,
            attrs,
            defStyle
    ) {
        init(context, attrs)
    }


    private val TAG = NHCreatePostEditText::class.java.simpleName
    private var nhCommonTextUtil: NHCommonTextViewUtil? = null
    private var callback: NHEditText.Callback? = null
    private var style: Int = 0
    private var customFontWeight = -1;
    private val textWatcherLiveData: MutableLiveData<Phrase> by lazy {
        MutableLiveData<Phrase>()
    }
    private val suggestionDisplay: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    var bsVisibility: MutableLiveData<Boolean>? = null


    private val DEFAULT_MAX_SPACE_WORDS = 10
    private val DEFAULT_EXPLICIT_CHAR = "@#"
    private val DEFAULT_TOKENIZER_HASH_AND_MENTION_CONFIG = WordTokenizerConfig.Builder().apply {
        setExplicitChars(DEFAULT_EXPLICIT_CHAR)
        setWordBreakChars(Constants.SPACE_STRING + System.getProperty("line.separator")) // word break between #tag
        setThreshold(2)
        setMaxNumKeywords(DEFAULT_MAX_SPACE_WORDS)
    }.build()


    private val queryReceiver = QueryTokenReceiver {
        if (it.isExplicit && textWatcherLiveData.hasActiveObservers()) {
            textWatcherLiveData.value = Added(it,
                    editable = text.toString(),
                    start = 0,
                    end = 0)
        } else if (!it.isExplicit) {
            suggestionDisplay.value = false
        }
        emptyList()
    }

    fun visibility(): MutableLiveData<Boolean> = suggestionDisplay

    fun addKeyBoardCallback(callback: NHEditText.Callback) {
        this.callback = callback
    }

    //for share explicit intent to EditText
    fun setText(intent: Intent) {
        val shareText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (shareText.isNullOrEmpty()) return
        text = Editable.Factory.getInstance().newEditable(shareText)
        post {
            text = text?.append(Constants.SPACE_STRING)
            setSelection(length())
        }
    }

    fun getFormattedText(): String {
        val postText = text as MentionsEditable
        var postSpannable = SpannableStringBuilder(postText)
        //using trimEnd() as we are appending space for displayString
        var startIndex = postText.nextSpanTransition(-1 , postText.length, MentionSpan::class.java)
        var endIndex = 0
        val spanRange  = mutableListOf<IntRange>()
        while(startIndex < postText.length) {
             endIndex = postText.nextSpanTransition(startIndex , postText.length, MentionSpan::class.java)
             spanRange.add(IntRange(startIndex, endIndex))
             startIndex = endIndex
        }
        spanRange.reversed().forEach {  range: IntRange ->
            val mentionSpan = postText.getMentionSpanStartingAt(range.first)
            val mention = mentionSpan?.mention
            if(mentionSpan != null && mention != null) {
                postSpannable = postSpannable.replace(range.first, range.last - 1, when ((mention as NHCreatePostMention).type()) {
                    SearchSuggestionType.HANDLE ->
                        "${PostConstants.POST_HANDLE_START}@${mention.getId()}${PostConstants
                                .POST_HANDLE_END}"
                    SearchSuggestionType.HASHTAG -> "${PostConstants
                            .POST_TAG_START}${mentionSpan.displayString.trim()}${PostConstants
                            .POST_TAG_END}"
                    else -> mentionSpan.displayString
                })
            }
        }
        return postSpannable.toString()
    }

    private val watcher = object : MentionWatcher {
        override fun onMentionDeleted(mention: Mentionable, text: String, start: Int, end: Int) {
            if (textWatcherLiveData.hasActiveObservers()) {
                textWatcherLiveData.value =
                        Delete(editable = mention.suggestiblePrimaryText, start = start, end = end)
            }
        }

        override fun onMentionPartiallyDeleted(
                mention: Mentionable,
                text: String,
                start: Int,
                end: Int
        ) {
            if (textWatcherLiveData.hasActiveObservers()) {
                textWatcherLiveData.value = PartialDelete(
                        editable = mention.suggestiblePrimaryText,
                        start = start,
                        end = end
                )
            }
        }

        override fun onMentionAdded(mention: Mentionable, text: String, start: Int, end: Int) {
            if (textWatcherLiveData.hasActiveObservers()) {
                textWatcherLiveData.value =
                        Added(editable = mention.suggestiblePrimaryText, start = start, end = end)
            }
        }
    }


    fun replaceText(selectedText: String) {
        val index = indexOfPrevNonLetterDigit(text.toString(), 0, length() - 1)
        setText(text?.replaceRange(index + 1, length(), selectedText + Constants.SPACE_STRING))
        setSelection(length())
    }

    fun appendText(appendText: String) {
        if (appendText.isNotEmpty()) {
            text?.insert(selectionStart, appendText)
        }
    }

    private fun indexOfPrevNonLetterDigit(text: CharSequence, start: Int, end: Int): Int {
        for (i in end downTo start) {
            if (!Character.isLetterOrDigit(text[i])) return i
        }
        return text.length
    }


    private fun init(context: Context, attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.NHCreatePostEditText)
        customFontWeight = typedArray.getInt(R.styleable.NHCreatePostEditText_dh_custom_font_weight, -1)
        typedArray.recycle()

        FontHelper.initTextView(this, context, attributeSet, customFontWeight)
        if(customFontWeight != -1){
            //re-set text to apply custom_font_weight based font
            setText(this.text.subSequence(0, this.text.length), nhCommonTextUtil?.bufferType)
        }

        tokenizer = NHCreatePostEditTextTokenizer(DEFAULT_TOKENIZER_HASH_AND_MENTION_CONFIG)
        addMentionWatcher(watcher)
        setQueryTokenReceiver(queryReceiver)
        setBackgroundColor(Color.TRANSPARENT)
        requestFocus()
        setSelection(0)

        setSuggestionsVisibilityManager(object : SuggestionsVisibilityManager {
            var oldStateSug: Boolean = false
            override fun displaySuggestions(display: Boolean) {
                suggestionDisplay.value = display
                oldStateSug = display
            }

            override fun isDisplayingSuggestions(): Boolean = oldStateSug
        })
    }

    private fun initCommonTextUtil() {
        if (null == nhCommonTextUtil) {
            nhCommonTextUtil = NHCommonTextViewUtil()
        }

    }

    /*
    * Subscribe to edit events from the view
    * */
    fun subscribe(lifecyleOwner: LifecycleOwner, observe: Observer<Phrase>) {
        textWatcherLiveData.observe(lifecyleOwner, observe)
    }


    override fun setText(text: CharSequence?, type: BufferType?) {
        var text = text
        if (text == null) {
            text = Constants.EMPTY_STRING
        }
        var isIndic = false
        if (text !is Spannable) {
            if (text != null && text.isNotEmpty()) {
                val fontEngineOutput: FEOutput
                fontEngineOutput = FontHelper.convertToFontIndices(text.toString())
                text = fontEngineOutput.fontIndicesString.toString()
                isIndic = fontEngineOutput.isSupportedLanguageFound
            }
        }
        initCommonTextUtil()
        setPadding(isIndic)
        if (nhCommonTextUtil?.setTextRequired(text, type) == true) {
            val s = nhCommonTextUtil?.getSpannableString(text, isIndic, style, customFontWeight)
            if (style == Typeface.BOLD) {
                val boldSpan = StyleSpan(Typeface.BOLD)
                s?.setSpan(boldSpan, 0, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            super.setText(s, type)
        }
    }

    override fun onDetachedFromWindow() {
        callback = null
        super.onDetachedFromWindow()
    }

    override fun setTypeface(tf: Typeface?, style: Int) {
        FontHelper.setStyle(this, style)
        this.style = style
    }


    override fun setCurrentTypeface(currentTypeface: Typeface?) {
        initCommonTextUtil()
        nhCommonTextUtil?.setCurrentTypeface(currentTypeface)
    }

    override fun setPadding(isIndic: Boolean) {
        initCommonTextUtil()
        nhCommonTextUtil?.setPadding(this, isIndic)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (callback == null) {
            return super.onKeyPreIme(keyCode, event)
        }
        when (event?.action) {
            KeyEvent.ACTION_UP -> if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (bsVisibility?.value == true) {
                    callback?.onKeyboardHide()
                    return true
                }
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }

    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {
        val ic = super.onCreateInputConnection(editorInfo)
        EditorInfoCompat.setContentMimeTypes(editorInfo, arrayOf("image/*"))
        val callback = InputConnectionCompat.OnCommitContentListener { _, _, _ ->
            Toast.makeText(
                    context, CommonUtils.getString(R.string.cp_no_gif_support), Toast.LENGTH_SHORT
            ).show()
            true
        }
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return try {
            super.onTextContextMenuItem(id)
        } catch (e: Exception) {
            false
        }
    }
}


interface NHCreatePostMention : Mentionable {
    fun getId(): String
    fun type(): SearchSuggestionType
}


class NHCPMention : NHCreatePostMention {
    private var itemName: String? = ""
    private var itemType: String? = ""
    private var itemSuggestion: String? = ""
    private var itemId:String? = ""


    constructor( input: Parcel?) {
        this.itemName = input?.readString()
        this.itemType = input?.readString()
        this.itemSuggestion = input?.readString()
        this.itemId = input?.readString()
    }

    constructor(item: SearchSuggestionItem) {
        this.itemName = item.name ?: ""
        this.itemType = item.typeName
        this.itemSuggestion = item.suggestion
        this.itemId  = item.itemId
    }

    override fun getId(): String  = itemSuggestion ?: ""

    override fun type(): SearchSuggestionType = if (SearchSuggestionType.HANDLE.type.equals(itemType, ignoreCase = true))
        SearchSuggestionType.HANDLE else SearchSuggestionType.HASHTAG

    override fun getSuggestibleId(): Int = itemId.hashCode()

    override fun getSuggestiblePrimaryText(): String  = when {
        SearchSuggestionType.HANDLE.name.equals(itemType, ignoreCase = true) ->
            Constants.AT_SYMBOL + itemName
        SearchSuggestionType.HASHTAG.name.equals(itemType, ignoreCase = true) ->
            Constants.HASH_CHARACTER + itemSuggestion
        else -> Constants.EMPTY_STRING
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeString(this.itemName)
        p0?.writeString(this.itemSuggestion)
        p0?.writeString(this.itemId)
        p0?.writeString(this.itemType)
    }

    override fun getDeleteStyle(): Mentionable.MentionDeleteStyle  = Mentionable.MentionDeleteStyle.FULL_DELETE

    override fun describeContents(): Int  = 0

    override fun getTextForDisplayMode(mode: Mentionable.MentionDisplayMode?): String  = when {
        SearchSuggestionType.HANDLE.name.equals(itemType, ignoreCase = true) ->
            itemName + Constants.SPACE_STRING
        SearchSuggestionType.HASHTAG.name.equals(itemType, ignoreCase = true) ->
            Constants.HASH_CHARACTER + itemSuggestion + Constants.SPACE_STRING
        else -> Constants.EMPTY_STRING
    }

    companion object {
        @JvmStatic
        val CREATOR = object : Parcelable.Creator<NHCPMention> {
            override fun createFromParcel(p0: Parcel?): NHCPMention  = NHCPMention(p0)

            override fun newArray(p0: Int): Array<NHCPMention> = emptyArray()
        }
    }
}

internal class NHCreatePostEditTextTokenizer(config: WordTokenizerConfig) : WordTokenizer(config) {
    override fun isValidMention(text: Spanned, start: Int, end: Int): Boolean {
        val subsequence = text.subSequence(start, end)
        return if(subsequence.startsWith("@") && subsequence.split(" ").count() > 2) false
        else super.isValidMention(text, start, end)
    }
}

