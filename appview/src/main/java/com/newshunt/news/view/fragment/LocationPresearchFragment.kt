/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.fragment


import android.content.Context
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.activity.CreatePostActivity
import com.newshunt.appview.common.ui.listeners.AddLocationListener
import com.newshunt.appview.common.ui.viewholder.StateInfoViewHolder
import com.newshunt.appview.common.viewmodel.LocationsViewModel
import com.newshunt.appview.common.viewmodel.LocationsViewModelFactory
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.model.interceptor.HeaderInterceptor
import com.newshunt.common.view.customview.fontview.NHEditText
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.DEFAULT_SEARCH_TYPE_SELECTOR
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.Locations
import com.newshunt.dataentity.common.asset.SEARCH_ITEMVIEW_TYPE_SUGGESTION
import com.newshunt.dataentity.common.asset.SearchUIVisitor
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.notification.SearchNavModel
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SuggestionUiResponse
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.combineWith
import com.newshunt.dhutil.execMax
import com.newshunt.dhutil.zipWith
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.view.activity.SearchActvityInterface
import com.newshunt.recolocations.DaggerRecommendedLocationsComponent
import com.newshunt.recolocations.RecommendedLocationsModule
import com.newshunt.search.viewmodel.SearchViewModel
import java.util.ArrayList
import javax.inject.Inject


/**
 * @author aman.roy
 */
class LocationPresearchFragment : BaseSupportFragment(), TextWatcher, SuggestionListener, View.OnTouchListener, AddLocationListener {
    private var searchActivityInterface: SearchActvityInterface? = null
    private var viewModel: SearchViewModel? = null
    private lateinit var suggestionList: RecyclerView
    private lateinit var searchBarContainer: AppBarLayout
    private lateinit var searchBox: NHEditText
    private lateinit var backButton: ImageView
    private lateinit var crossButton: ImageView
    private lateinit var requestId: String
    private lateinit var adapter: LocationSuggestionAdapter
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
    private lateinit var locationsViewModel: LocationsViewModel
    private val section: String = com.newshunt.dataentity.common.pages.PageSection.NEWS.section
    @Inject
    lateinit var locationsViewModelF: LocationsViewModelFactory
    private val userTyping = MutableLiveData<Boolean>().apply { value = true }


    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        requestId = arguments?.getString(BUNDLE_REQUEST_ID, "") ?: ""
        referrer = arguments?.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        referrerRaw = arguments?.getString(Constants.REFERRER_RAW)
        searchNavModel = arguments?.getSerializable(Constants.BUNDLE_SEARCH_MODEL) as? SearchNavModel
        query = (arguments?.getSerializable(BUNDLE_QUERY) as? SearchSuggestionItem)?.suggestion
                ?: ""

        DaggerRecommendedLocationsComponent.builder()
                .recommendedLocationsModule(RecommendedLocationsModule(section, SocialDB.instance
                ())).build().inject(this)

        locationsViewModel = ViewModelProviders.of(this, locationsViewModelF).get(LocationsViewModel::class
                .java)

        fireSearchInitated = {
            SearchAnalyticsHelper.logSearchInitiated(requestId, referrer ?: createReferrer(),
                    hasUserTyped.toString())
        } execMax 1
        AndroidUtils.getMainThreadHandler().post {
            viewModel = searchActivityInterface?.searchViewModel()
            val vm = viewModel ?: return@post
            vm.presearch.zipWith(userTyping) { a, b -> a to b }
                    .observe(this@LocationPresearchFragment, Observer { (t, typing) ->
                        if (typing) updateUi(t)
                        else Logger.d(LOG_TAG, "ignored presearch: $t, $typing")
                    })
            vm.searchResults.data().combineWith(userTyping,locationsViewModel.getAllFollowedLocations()) { a, b,c -> Triple(a,b,c)}
                    .observe(this@LocationPresearchFragment, Observer { (t, typing,followedLocations) ->
                        if (t.isSuccess && (!typing)) {
                            t.getOrNull()?.response?.rows?.filterIsInstance<Locations>()?.let {
                                updateFollowState(it,followedLocations)
                                updateResults(it) }
                        } else Logger.d(LOG_TAG, "ignored searchResults: $t, $typing")
                    })
        }
    }

    fun updateFollowState(locations: List<Locations>, followedLocations: List<FollowSyncEntity>) {
        val followedLocationsId = followedLocations.map { it.actionableEntity.entityId }
        for (location in locations) {
            location.kids?.let { kids ->
                for (childLocation in kids) {
                    childLocation.isFollowed = childLocation.id in followedLocationsId
                }
            }
        }
    }

    fun updateResults(it: List<Locations>): Unit {
        searchBox.isCursorVisible=false
        adapter.updateItems(it)
        adapter.notifyDataSetChanged()
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
                searchBox.isCursorVisible = true
                AndroidUtils.showKeyBoard(activity, searchBox)
                return false
            }
        }
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val hint = arguments?.getString(BUNDLE_HINT) ?: ""
        val view = LayoutInflater.from(context).inflate(R.layout.layout_location_fragment_presearch, null)

//      When I use this one it does not even show anything on fragment
//        val view = LayoutInflater.from(context).inflate(R.layout.layout_location_fragment_presearch, null)

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
                   // updateResults()

//                    should log the search exectuted query if user presses the enter key
                    SearchAnalyticsHelper.logSearchExecuted(requestId,text,referrer ?: createReferrer(),0,
                            Constants.LOCATION,null,Constants.BUNDLE_LOCATION_ID)
                    userTyping.value = false
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
        adapter = LocationSuggestionAdapter(context!!, this,referrer,this)
        suggestionList.adapter = adapter
        adapter.notifyDataSetChanged()
        return view
    }

    override fun onLocationAdded(isAdded: Boolean, location: Location) {
        locationsViewModel.onLocationFollowed(location)
        if (isAdded) {
            AnalyticsHelper2.logExploreButtonLocationClickEvent(referrer, "check", PageSection.NEWS)
        }
        else {
            AnalyticsHelper2.logExploreButtonLocationClickEvent(referrer, "uncheck", PageSection.NEWS)
        }
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
        // no recents for location search
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

        adapter.updateItems(uiResponse.response.rows ?: emptyList(), presentSearch)
        adapter.notifyDataSetChanged()
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
        // no recents for location search
    }

    override fun suggestionClicked(pos: Int, item: SearchSuggestionItem) {
        AndroidUtils.hideKeyboard(activity, searchBox)
        searchBox.setText(item.suggestion)

//        Log the search executed when user clicks on suggested item
        SearchAnalyticsHelper.logSearchExecuted(requestId,item.suggestion,referrer ?: createReferrer(),0,
                Constants.LOCATION,null,Constants.BUNDLE_LOCATION_ID)

        if (suggestionCallback != null) {
            suggestionCallback?.suggestionClicked(pos, item)
            return
        }
        val itemType: String = if (!item.deeplinkUrl.isEmpty()) "auto_sources" else item.suggestionType.type
        userTyping.value = false
        (activity as? SearchActvityInterface)?.submitQuery(item.copy(requestId = requestId),
                itemType)
    }

    fun mapToUserBaseProfile(item: SearchSuggestionItem): UserBaseProfile {
        return UserBaseProfile().apply {
            this.userId = item.userId
        }
    }

    fun processTextChanges(s: String?) {
        userTyping.value = true
        if (s != null) {
            if (::searchBox.isInitialized) {
                if (s.isEmpty()) searchBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0) else {
                }
                searchBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.close_selector, 0)
            }
            viewModel?.typing(Triple(s, requestId, ""))
            if (s != Constants.EMPTY_STRING) {
                hasUserTyped = true
            }
        } else {
            if (::searchBox.isInitialized) {
                searchBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            viewModel?.typing(Triple("", requestId, ""))
        }
        lastText = s
    }

    private fun createReferrer(): PageReferrer {
        return PageReferrer(NewsReferrer.LOCATION_SELECTION_PAGE)
    }

    override fun getPreFragmentManager(): androidx.fragment.app.FragmentManager {
        return activity?.supportFragmentManager!!
    }

    override fun deleteAllSuggestion() {
        // no recents for location search
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
        = "", showToolbar: Boolean = true, searchNavModel: SearchNavModel? = null): LocationPresearchFragment {
            val fragment = LocationPresearchFragment()
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

class LocationSuggestionAdapter(val context: Context, val suggestionListener: SuggestionListener,val pageReferrer: PageReferrer?,val addLocationListener : AddLocationListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items : MutableList<SearchUIVisitor> = mutableListOf()
    private var presentSearch: String = "Del"

    fun updateItems(l: List<SearchUIVisitor>, searchKey: String? = null) {
        items.clear()
        items.addAll(l)
        if(searchKey != null) presentSearch = searchKey
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        if (viewType == SEARCH_ITEMVIEW_TYPE_SUGGESTION) {
            val view: View = inflater.inflate(R.layout.location_item_suggestion, parent, false)
            return LocationSuggestionViewHolder(view, suggestionListener)
        } else {
            val view: View = inflater.inflate(R.layout.location_list_parent_view, parent,
                    false)
            return StateInfoViewHolder(view, null, null, pageReferrer, addLocationListener, true, null, ArrayList<String>())
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == SEARCH_ITEMVIEW_TYPE_SUGGESTION) {
            (holder as BaseViewHolder).updateView(items[position] as SearchSuggestionItem, presentSearch)
        } else {
            val location : Locations = items[position] as Locations
            location.areChildrenVisible = true
            (holder as StateInfoViewHolder).updateStateItem(location)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
      return items[position].viewType(DEFAULT_SEARCH_TYPE_SELECTOR)
    }

}

class LocationSuggestionViewHolder(itemView: View, val suggestionListener: SuggestionListener) : BaseViewHolder(itemView) {
    private val suggestionText: NHTextView
    private val itemDivider: View

    init {
        suggestionText = itemView.findViewById(R.id.suggestion_text)
        itemDivider = itemView.findViewById(R.id.suggestion_divider)
    }

    override fun updateView(suggestionItem: SearchSuggestionItem, searchKey: String) {
        itemView.setOnClickListener {
            suggestionListener.suggestionClicked(adapterPosition, suggestionItem)
        }
        highlightText(query = searchKey, suggestion = suggestionItem.suggestion)
        itemDivider.visibility = View.VISIBLE
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
