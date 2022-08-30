/*
 * Created by Rahul Ravindran at 26/9/19 6:58 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.activity.CreatePostActivity
import com.newshunt.appview.databinding.ItemMentionSuggestionBinding
import com.newshunt.appview.databinding.ItemMentionSuggestionUnifiedBinding
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.model.interceptor.HeaderInterceptor
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHEditText
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.follow.entity.FollowEntitySubType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.notification.SearchNavModel
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SearchSuggestionType
import com.newshunt.dataentity.search.SuggestionUiResponse
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.execMax
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.news.view.activity.SearchActvityInterface
import com.newshunt.sdk.network.image.Image
import com.newshunt.search.view.view.DeleteConfirmationDialog
import com.newshunt.search.view.view.DeleteSearchListener
import com.newshunt.search.viewmodel.SearchViewModel


/**
 * @author shrikant.agarwal
 */
class PresearchFragment : BaseSupportFragment(), TextWatcher, SuggestionListener, View.OnTouchListener {
    private var searchActivityInterface: SearchActvityInterface? = null
    private var viewModel: SearchViewModel? = null
    private lateinit var suggestionList: RecyclerView
    private lateinit var searchBarContainer: AppBarLayout
    private lateinit var searchBox: NHEditText
    private lateinit var backButton: ImageView
    private lateinit var crossButton: ImageView
    private lateinit var requestId: String
    private lateinit var adapter: SuggestionAdapter
    private var referrer: PageReferrer? = null
    private var referrerRaw: String? = null
    private var query: String = ""
    private val LOG_TAG = "PresearchFragment"
    private lateinit var fireSearchInitated: () -> Unit
    private var hasUserTyped: Boolean = false
    // for restoring state
    private var lastText: String? = null
    private var showToolbar: Boolean = true
    private var suggestionCallback: SuggestionListener? = null
    private var searchNavModel:SearchNavModel?=null


    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        requestId = arguments?.getString(BUNDLE_REQUEST_ID, "") ?: ""
        referrer = arguments?.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        referrerRaw = arguments?.getString(Constants.REFERRER_RAW)
        searchNavModel = arguments?.getSerializable(Constants.BUNDLE_SEARCH_MODEL) as? SearchNavModel
        query = (arguments?.getSerializable(BUNDLE_QUERY) as? SearchSuggestionItem)?.suggestion
                ?: ""
        fireSearchInitated = {
            SearchAnalyticsHelper.logSearchInitiated(requestId, referrer ?: createReferrer(),
                    hasUserTyped.toString())
        } execMax 1
        AndroidUtils.getMainThreadHandler().post {
            viewModel = searchActivityInterface?.searchViewModel()
            viewModel?.presearch?.observe(this@PresearchFragment, Observer { t ->
                updateUi(t)
            })
        }
    }

    fun setSearchInterface(callback: SearchActvityInterface) {
        this.searchActivityInterface = callback
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SearchActvityInterface) {
            searchActivityInterface = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        searchActivityInterface = null
        viewModel = null
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when (p1?.action) {
            MotionEvent.ACTION_UP -> {
                val clickedOnCross = searchBox.compoundDrawables[2] != null && p1?.rawX!! >= (searchBox.right - searchBox.compoundDrawables[2].bounds.width())
                if (clickedOnCross) {
                    searchBox.text?.clear()
                }
                AndroidUtils.showKeyBoard(activity, searchBox)
                return false
            }
        }
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val hint = arguments?.getString(BUNDLE_HINT) ?: ""
        val view = LayoutInflater.from(context).inflate(R.layout.layout_fragment_presearch, null)
        suggestionList = view.findViewById(R.id.suggestion_list)
        suggestionList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        searchBarContainer = view.findViewById(R.id.search_bar)


        if (arguments?.getBoolean(BUNDLE_TOOLBAR_STATUS, true) == false) {
            showToolbar = false
            searchBarContainer.visibility = View.GONE
        }

        searchBox = view.findViewById(R.id.search_box)
        searchBox.setOnTouchListener(this)


        searchBox.addTextChangedListener(this)
        searchBox.setOnEditorActionListener { textView, i, keyEvent ->
            if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
                val text = textView.text.toString()
                if (text.isBlank()) {
                    Logger.d(LOG_TAG, "ignoring blank query")
                    false
                } else {
                    // first insert to db because submit querr will detach the fragment.
                    insertQueryToRecent(text)
                    searchActivityInterface?.submitQuery(SearchSuggestionItem(text,
                            text, requestId = requestId), "query")
                    true
                }
            } else false
        }
        searchBox.hint = FontHelper.getFontConvertedString(hint)
        searchBox.setText(query)
        searchBox.setSelection(searchBox.text?.length ?: 0)

        backButton = view.findViewById(R.id.toolbar_back_button)
        backButton.setOnClickListener {
            AndroidUtils.hideKeyboard(activity, searchBox)
            if (NewsNavigator.shouldNavigateToHome(activity, referrer, false,referrerRaw)) {
                NewsNavigator.navigateToHomeOnLastExitedTab(activity, PageReferrer(NewsReferrer.SEARCH))
            } else {
                activity?.onBackPressed()
            }
        }

        adapter = SuggestionAdapter(context!!, this)
        suggestionList.adapter = adapter
        adapter.notifyDataSetChanged()
        return view
    }

    override fun handleBackPress(): Boolean {
        if (NewsNavigator.shouldNavigateToHome(activity, referrer, true,referrerRaw)) {
            NewsNavigator.navigateToHomeOnLastExitedTab(activity, PageReferrer(NewsReferrer.SEARCH))
        }
        return false
    }


    fun setCallback(callback: SuggestionListener) {
        this.suggestionCallback = callback
    }

    fun insertQueryToRecent(query: String? = null, suggestion: SearchSuggestionItem? = null) {
        when {
            suggestion != null -> {
                viewModel?.insertQueryToRecent(suggestion.suggestion, suggestion)
            }
            query != null -> {
                val suggestion = SearchSuggestionItem(query, query)
                viewModel?.insertQueryToRecent(query, suggestion)
            }
            else -> {
                Logger.e(LOG_TAG, "insertQueryToRecent: both can not be null")
            }
        }
    }

    fun resetListing() {
        adapter.clear()
    }

    private fun updateUi(uiResponse: SuggestionUiResponse?) {
        if (uiResponse == null) {
            return  // handle the null case
        }

        val presentSearch = searchBox.text.toString()
        if (!CommonUtils.equals(uiResponse.query, presentSearch) && showToolbar) {
            // do not accept the response if the present string and the response string is not same
            return
        }
        if (uiResponse.response.rows?.isEmpty() == true) {
            if (activity is CreatePostActivity) {
                (activity as? CreatePostActivity)?.showSuggestionView(false)
            }
            return
        }

        adapter.updateSuggestion(uiResponse.response.rows ?: emptyList(), presentSearch);

        if (activity is CreatePostActivity) {
            (activity as? CreatePostActivity)?.showSuggestionView(true)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        processTextChanges(s?.toString())
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(BUNDLE_LAST_TEXT, lastText)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lastText = savedInstanceState?.getString(BUNDLE_LAST_TEXT)
        if (viewModel == null)
            viewModel = (activity as? SearchActvityInterface)?.searchViewModel()
        Logger.d(LOG_TAG, "lastText : $lastText")
        processTextChanges(lastText)
    }

    override fun onResume() {
        super.onResume()
        AndroidUtils.showKeyBoard(activity, searchBox)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // do nothing
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // do nothing
    }

    override fun deleteSuggestion(query: String, position: Int) {
        viewModel?.deleteQueryFromRecent(query)
        adapter.deleteItem(position)
    }

    override fun suggestionClicked(pos: Int, item: SearchSuggestionItem) {
        AndroidUtils.hideKeyboard(activity, searchBox)
        if (suggestionCallback != null) {
            suggestionCallback?.suggestionClicked(pos, item)
            return
        }
        val itemType: String = if (!item.deeplinkUrl.isEmpty()) "auto_sources" else item.suggestionType.type
        insertQueryToRecent(suggestion = item)
        if (item.deeplinkUrl.isEmpty()) {
            (activity as? SearchActvityInterface)?.submitQuery(item.copy(requestId = requestId),
                    itemType)
        } else {
            fireSearchInitated()
            SearchAnalyticsHelper.logSearchExecuted(requestId,
                    item.suggestion,
                    referrer ?: createReferrer(),
                    0,
                    itemType,
                    item.experiment,
                    item.itemId
            )
            if (CommonUtils.equals(item.subType, FollowEntitySubType.CREATOR.name)) {
                CommonNavigator.launchProfileActivity(context!!, mapToUserBaseProfile(item),
                        createReferrer())
            }
            // for the deeplink, referrer should be search.
            else {
                CommonNavigator.launchDeeplink(context, item.deeplinkUrl, createReferrer())
            }
            activity?.finish()
        }
    }

    fun mapToUserBaseProfile(item: SearchSuggestionItem): UserBaseProfile {
        return UserBaseProfile().apply {
            this.userId = item.userId
        }
    }

    fun processTextChanges(s: String?) {
        if (s != null) {
            if (::searchBox.isInitialized) {
                if (s.isEmpty()) searchBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0) else {
                }
                searchBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.close_selector, 0)
            }
            viewModel?.typing(Triple(s, requestId, ""))
            hasUserTyped = true
        } else {
            if (::searchBox.isInitialized) {
                searchBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            viewModel?.typing(Triple("", requestId, ""))
        }
        lastText = s
    }

    private fun createReferrer(): PageReferrer {
        return PageReferrer(NewsReferrer.SEARCH)
    }

    override fun getPreFragmentManager(): androidx.fragment.app.FragmentManager {
        return activity?.supportFragmentManager!!
    }

    override fun deleteAllSuggestion() {
        viewModel?.deleteAllFromRecent()
        adapter.clearAllRecents()
    }

    override fun onDestroy() {
        fireSearchInitated()
        super.onDestroy()
    }

    companion object {
        private const val BUNDLE_QUERY = "presearch_bundle_query"
        private const val BUNDLE_HINT = "bundle_hint"
        private const val BUNDLE_REQUEST_ID = "bundle_request_id"
        private val BUNDLE_LAST_TEXT = "lastText"
        private val BUNDLE_TOOLBAR_STATUS = "bundle_toolbar_status"

        fun newInstance(query: SearchSuggestionItem? = null, referrer: PageReferrer, hint: String
        = "", showToolbar: Boolean = true,searchNavModel:SearchNavModel?=null): PresearchFragment {
            val fragment = PresearchFragment()
            val bundle = bundleOf(
                    BUNDLE_QUERY to query,
                    BUNDLE_HINT to hint,
                    BUNDLE_TOOLBAR_STATUS to showToolbar,
                    BUNDLE_REQUEST_ID to HeaderInterceptor.generateRequestId(),
                    Constants.BUNDLE_SEARCH_MODEL to searchNavModel,
                    Constants.BUNDLE_ACTIVITY_REFERRER to referrer
            )
            fragment.arguments = bundle
            return fragment
        }
    }
}

interface SuggestionListener {
    fun deleteSuggestion(query: String, position: Int) {}
    fun deleteAllSuggestion() {}
    fun suggestionClicked(pos: Int, item: SearchSuggestionItem) {}
    fun getPreFragmentManager(): FragmentManager
}

const val HEADER = 1
const val SUGGESTION = 2
const val HASHTAG = 3
const val MENTION = 4
const val HASHTAG_UNIFIED = 5
const val MENTION_UNIFIED = 6

class SuggestionAdapter(val context: Context, val suggestionListener: SuggestionListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var suggestionList: MutableList<SearchSuggestionItem> = mutableListOf()
    private var presentSearch: String = ""

    fun updateSuggestion(suggestions: List<SearchSuggestionItem>, searchKey: String) {
        suggestionList.clear()
        suggestionList.addAll(suggestions)
        presentSearch = searchKey
        notifyDataSetChanged()
    }

    fun clear() {
        suggestionList.clear()
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int)  {
        val range = trendingHeadIndex()
        if(range > 2) {
            suggestionList.removeAt(position)
            notifyItemRemoved(position)
        }else clearAllRecents()
    }

    private fun trendingHeadIndex(): Int {
        return suggestionList.indexOfFirst { it.suggestionType  == SearchSuggestionType.TRENDING_HEADER }
    }

    fun clearAllRecents() {
        val recentRangeEnd = trendingHeadIndex()
        if (recentRangeEnd < 0) return
        suggestionList = suggestionList.drop(recentRangeEnd).toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HEADER) {
            val view: View
            if (UserPreferenceUtil.getUserNavigationLanguage() == Constants.URDU_LANGUAGE_CODE) {
                view = LayoutInflater.from(context).inflate(R.layout
                        .item_suggestion_header_urdu, parent, false)
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.item_suggestion_header, parent,
                        false)
            }
            return CategoryViewHolder(view, suggestionListener)
        } else return when (viewType) {
            HASHTAG -> HashTagViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_hashtag, parent, false), viewType, suggestionListener)
            MENTION -> MentionViewHolder(DataBindingUtil.inflate<ItemMentionSuggestionBinding>(LayoutInflater.from(parent.context),
                    R.layout.item_mention_suggestion, parent, false), suggestionListener)
            HASHTAG_UNIFIED -> HashTagViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_hashtag_unified, parent, false), viewType,  suggestionListener)
            MENTION_UNIFIED -> {
                MentionViewHolder(DataBindingUtil.inflate<ItemMentionSuggestionUnifiedBinding>(LayoutInflater.from(parent.context),
                        R.layout.item_mention_suggestion_unified, parent, false), suggestionListener)

            }
            else -> return when {
                UserPreferenceUtil.getUserNavigationLanguage() == Constants.URDU_LANGUAGE_CODE -> SuggestionViewHolder(LayoutInflater.from(context).inflate(R.layout.item_suggestion_urdu,
                        parent, false), suggestionListener)
                else -> SuggestionViewHolder(LayoutInflater.from(context).inflate(R.layout.item_suggestion, parent, false), suggestionListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BaseViewHolder).updateView(suggestionList[position], presentSearch)
    }

    override fun getItemCount(): Int {
        return suggestionList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (suggestionList[position].suggestionType) {
            SearchSuggestionType.RECENT_HEADER,
            SearchSuggestionType.TRENDING_HEADER -> HEADER
            else ->
                return when (suggestionList[position].typeName) {
                    SearchSuggestionType.HASHTAG.name -> HASHTAG
                    SearchSuggestionType.HANDLE.name -> MENTION
                    SearchSuggestionType.HASHTAG_UNIFIED.name -> HASHTAG_UNIFIED
                    //OGC non taggable should be shown in global search. Hence the inner if condition check
                    SearchSuggestionType.HANDLE_UNIFIED.name -> if(suggestionList[position].creatorType == "OGC") SUGGESTION else MENTION_UNIFIED
                    else -> SUGGESTION
                }

        }
    }


}

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun updateView(suggestionItem: SearchSuggestionItem, searchKey: String)
}

class CategoryViewHolder(itemView: View, val suggestionListener: SuggestionListener) : BaseViewHolder(itemView) {
    private val headerView: NHTextView
    private val crossView: ImageView

    init {
        headerView = itemView.findViewById(R.id.suggestion_header)
        crossView = itemView.findViewById(R.id.header_cross)
    }

    override fun updateView(suggestionItem: SearchSuggestionItem, searchKey: String) {
        headerView.setText(suggestionItem.suggestion)

        when (suggestionItem.suggestionType) {
            SearchSuggestionType.RECENT_HEADER, SearchSuggestionType.RECENT -> {
                crossView.visibility = View.VISIBLE
                crossView.setOnClickListener {
                    if (suggestionListener.getPreFragmentManager() != null) {
                        DeleteConfirmationDialog.createDialog(listener = object : DeleteSearchListener {
                            override fun onDeleteConfirmation() {
                                suggestionListener.deleteAllSuggestion()
                            }
                        }).show(suggestionListener.getPreFragmentManager(), "")
                    }
                }
            }
            else -> crossView.visibility = View.GONE
        }
    }

}

class HashTagViewHolder(itemView: View,
                        private val viewType: Int,
                        private val suggestionListener: SuggestionListener) : BaseViewHolder(itemView) {

    private val hashtagSuggestionTitle: NHTextView? = itemView.findViewById(R.id.title)
    private val hashtagSuggestionSubtitle: NHTextView? = itemView.findViewById(R.id.subtitle)

    override fun updateView(suggestionItem: SearchSuggestionItem, searchKey: String) {
        hashtagSuggestionTitle?.text = getFormattedSubString(viewType, suggestionItem.suggestion)

        if (suggestionItem.id.isEmpty()) {
            hashtagSuggestionSubtitle?.visibility = View.VISIBLE
            hashtagSuggestionSubtitle?.typeface = Typeface.defaultFromStyle(Typeface.BOLD);
            hashtagSuggestionSubtitle?.setTextColor(CommonUtils.getColor(R.color.new_hastag_subtitle_color))
            val typedVal = TypedValue()
            itemView.context.theme.resolveAttribute(R.attr.hashtag_new_highlight_color, typedVal, true)
            itemView.setBackgroundResource(typedVal.resourceId)
            hashtagSuggestionSubtitle?.text = CommonUtils.getString(R.string.post_create_new_hash_tag)
        } else {
            hashtagSuggestionSubtitle?.visibility = View.GONE
            hashtagSuggestionSubtitle?.typeface = Typeface.defaultFromStyle(Typeface.BOLD);
            val typedVal = TypedValue()
            itemView.context.theme.resolveAttribute(R.attr.presearch_list_bg, typedVal, true)
            itemView.setBackgroundResource(typedVal.resourceId)
        }

        itemView.setOnClickListener { suggestionListener.suggestionClicked(position, suggestionItem) }
    }

    fun  getFormattedSubString(viewType: Int, input: String) :String {
        var result: String = if(input.contains("#")) input.substring(1) else input
        return if(viewType == HASHTAG) "#${result}" else result
    }
}

class MentionViewHolder(private val binding: ViewDataBinding,
                        private val suggestionListener: SuggestionListener) : BaseViewHolder(binding.root) {

    override fun updateView(suggestionItem: SearchSuggestionItem, searchKey: String) {
        if(binding is ItemMentionSuggestionBinding){
            binding.sItem = suggestionItem
        } else if(binding is ItemMentionSuggestionUnifiedBinding){
            binding.sItem = suggestionItem
        }
        binding.executePendingBindings()
        binding.root.setOnClickListener { suggestionListener.suggestionClicked(adapterPosition, suggestionItem) }
    }
}

class SuggestionViewHolder(itemView: View, val suggestionListener: SuggestionListener) : BaseViewHolder(itemView) {
    private val suggestionIcon: NHImageView
    private val suggestionText: NHTextView
    private val deleteIcon: ImageView

    init {
        suggestionIcon = itemView.findViewById(R.id.suggestion_icon)
        suggestionText = itemView.findViewById(R.id.suggestion_text)
        deleteIcon = itemView.findViewById(R.id.suggestion_delete)
    }

    override fun updateView(suggestionItem: SearchSuggestionItem, searchKey: String) {
        itemView.setOnClickListener {
            suggestionListener.suggestionClicked(adapterPosition, suggestionItem)
        }
        when (suggestionItem.suggestionType) {
            SearchSuggestionType.RECENT -> {
                val ic = ThemeUtils.getThemeDrawableByAttribute(itemView.context, R.attr.search_recent_icon, R.drawable.vector_recent_search)
                suggestionIcon.setImageResource(ic)
            }
            SearchSuggestionType.TRENDING -> {
                val ic = ThemeUtils.getThemeDrawableByAttribute(itemView.context, R.attr.search_trending_icon, R.drawable.vector_trending_search)
                suggestionIcon.setImageResource(ic)
            }
            else -> {
                if (!suggestionItem.iconUrl.isEmpty()) {
                    Image.load(suggestionItem.iconUrl).into(suggestionIcon)
                } else if (!suggestionItem.deeplinkUrl.isEmpty()) {
                    suggestionIcon.setImageResource(R.drawable.deeplink_search_selector)
                } else {
                    suggestionIcon.setImageResource(R.drawable.search_selector)
                }
            }
        }
        highlightText(query = searchKey, suggestion = suggestionItem.suggestion)
    }

    fun highlightText(query: String, suggestion: String) {
        val theme = itemView.context.getTheme()
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.search_suggestion_text_color, typedValue, true)
        @ColorInt val color = typedValue.data
        if (query.isEmpty()) {
            suggestionText.setTextColor(color)
            suggestionText.setText(suggestion)
        } else {
            suggestionText.setTextColor(itemView.context.resources.getColor(R.color.search_suggestion_text_highlight))
            if (suggestion.startsWith(query)) {
                val span = ForegroundColorSpan(color)
                val convertedStr = FontHelper.getFontConvertedString(suggestion)
                val spannableString = SpannableString(convertedStr)
                val spanLen = query.length.coerceAtMost(convertedStr.length - 1)
                spannableString.setSpan(span, 0, spanLen, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                suggestionText.setSpannableText(spannableString, suggestion)
            } else {
                suggestionText.setText(suggestion)
            }
        }
    }
}