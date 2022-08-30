/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.ui.fragment

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsFragment
import com.newshunt.appview.common.di.DaggerSearchCardsComponent
import com.newshunt.appview.common.di.SearchCardsModule
import com.newshunt.appview.common.group.ui.activity.ContactsFlow
import com.newshunt.appview.common.viewmodel.SearchCardsViewModel
import com.newshunt.appview.common.viewmodel.SearchCardsViewModelF
import com.newshunt.appview.databinding.SearchCardsFragmentBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.PermissionEvent
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dataentity.searchhint.entity.SearchLocation
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.helper.SearchHintUtils
import com.newshunt.news.util.NewsConstants
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.permissionhelper.utilities.PermissionUtils
import com.newshunt.profile.FragmentCommunicationsViewModel
import java.io.Serializable
import javax.inject.Inject

/**
 *  A wrapper around CardsFragment which hosts an Edit text and sends query to its view model
 *  when text changes and triggers updates to CardsFrament
 * <p>
 * Created by srikanth.ramaswamy on 11/15/2019.
 */
private const val BUNDLE_DYNAMIC_FEED = "BUNDLE_DYNAMIC_FEED"
private const val BUNDLE_SEARCH_URL = "BUNDLE_SEARCH_URL"
private const val BUNDLE_SEARCH_QUERY_PARAM = "BUNDLE_SEARCH_QUERY_PARAM"
private const val BUNDLE_CARDS_FRAGMENT = "BUNDLE_CARDS_FRAGMENT"
private const val TAG_SEARCH_CARDS_FRAGMENT = "TAG_SEARCH_CARDS_FRAGMENT"
private const val BUNDLE_UI_MODE = "BUNDLE_UI_MODE"

class SearchCardsFragment: BaseSupportFragment(), TextWatcher, View.OnClickListener {
    private lateinit var viewBinding: SearchCardsFragmentBinding
    private var dynamicFeed: GeneralFeed? = null
    private var pageId: String? = null
    private var uiMode = SearchCardsFragmentUIMode.DEFAULT
    private var isContactspermissionGranted = false
    private var activityId = -1

    @Inject
    lateinit var searchCardsViewModelF: SearchCardsViewModelF
    private lateinit var searchCardsViewModel: SearchCardsViewModel
    private var searchLocation: SearchLocation? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        arguments?.let { args ->
            dynamicFeed = args.getSerializable(BUNDLE_DYNAMIC_FEED) as? GeneralFeed?
            searchLocation = args.getSerializable(Constants.BUNDLE_SEARCH_LOCATION) as? SearchLocation
            dynamicFeed?.let {
                DaggerSearchCardsComponent
                        .builder()
                        .searchCardsModule(SearchCardsModule(args.getString(BUNDLE_SEARCH_URL) as String,
                                args.getString(BUNDLE_SEARCH_QUERY_PARAM) as String,
                                it))
                        .build()
                        .inject(this)

                searchCardsViewModel = ViewModelProviders.of(this, searchCardsViewModelF).get(SearchCardsViewModel::class.java)
                searchCardsViewModel.setup(it)
                searchCardsViewModel.feedSetupLiveData.observe(this, Observer {
                    if (it.isSuccess) {
                        //setup the cards fragment once when the dynamic feed is updated for the first time
                        if (it.getOrNull()?.isNotEmpty() == true && pageId == null) {
                            pageId = it.getOrNull()?.get(0)
                            setupCardsFragment()
                        }
                    }
                })
            } ?: throw IllegalArgumentException("Cant show feed without GeneralFeed object")
        }
        activity?.let {
            activityId = (it as? ContactsFlow?)?.getActivityId() ?: -1
            ViewModelProviders.of(it)
                    .get(FragmentCommunicationsViewModel::class.java).fragmentCommunicationLiveData
                    .observe(this, Observer {
                        if(it.hostId != activityId || it.anyEnum !is PermissionEvent) {
                            return@Observer
                        }
                        handleSearchCTA()
                    })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.search_cards_fragment, container, false)
        viewBinding.searchCards.addTextChangedListener(this)
        uiMode = arguments?.getSerializable(BUNDLE_UI_MODE) as? SearchCardsFragmentUIMode? ?: SearchCardsFragmentUIMode.DEFAULT
        if (uiMode == SearchCardsFragmentUIMode.INVITES_PEOPLE_SEARCH) {
            viewBinding.searchHintsContainer.visibility = View.VISIBLE
            isContactspermissionGranted = checkContactPermission()
            viewBinding.searchAllowCta.setOnClickListener(this)
        }
        searchLocation?.let {
            SearchHintUtils(viewBinding.searchCards, this).updateHint(SearchLocation.PeapleSearch)
        }
        pageId?.let {
            setupCardsFragment()
        }
        return viewBinding.root
    }

    override fun afterTextChanged(editable: Editable?) {
        searchCardsViewModel.search(editable?.toString())
        handleSuggestionHints(editable?.toString())
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onClick(view: View?) {
        view ?: return
        if (view.id == R.id.search_allow_cta) {
            (activity as? ContactsFlow?)?.let { contactsFlow ->
                if (checkContactPermission()) {
                    AnalyticsHelper2.logExploreButtonClickEvent(PageReferrer(PageReferrer(NhGenericReferrer.INVITE_SCREEN)),
                            NewsExploreButtonType.GROUP_INVITE_SEARCH,
                            NhAnalyticsEventSection.GROUP.eventSection)
                    contactsFlow.openPhonebook(viewBinding.searchCards.text?.toString())
                } else {
                    AnalyticsHelper2.logExploreButtonClickEvent(PageReferrer(NhGenericReferrer.INVITE_SCREEN),
                            NewsExploreButtonType.GROUP_INVITE_ALLOW,
                            NhAnalyticsEventSection.GROUP.eventSection)
                    contactsFlow.requestContactsPermissionAndSearchPhonebook(viewBinding.searchCards.text?.toString())
                }
            }
        }
    }

    fun refresh() {
        getCardsFragment()?.refresh()
    }

    private fun setupCardsFragment() {
        view ?: return

        arguments?.let {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.search_cards_fragment_holder,
                    CardsFragment.create(it.getBundle(BUNDLE_CARDS_FRAGMENT) as Bundle, null),
                    TAG_SEARCH_CARDS_FRAGMENT)
            transaction.commit()
        }
    }

    private fun getCardsFragment(): CardsFragment? {
        return childFragmentManager.findFragmentByTag(TAG_SEARCH_CARDS_FRAGMENT) as? CardsFragment
    }

    private fun handleSuggestionHints(editableString: String?) {
        if (uiMode != SearchCardsFragmentUIMode.INVITES_PEOPLE_SEARCH) {
            return
        }
        if (editableString.isNullOrEmpty().not()) {
            if (viewBinding.suggestionText.visibility == View.VISIBLE) {
                viewBinding.suggestionText.visibility = View.GONE
                viewBinding.searchLocal.visibility = View.VISIBLE
                handleSearchCTA()
            }
        } else {
            if (viewBinding.suggestionText.visibility != View.VISIBLE) {
                viewBinding.suggestionText.visibility = View.VISIBLE
                viewBinding.searchLocal.visibility = View.GONE
            }
        }
    }

    private fun handleSearchCTA() {
        if (uiMode != SearchCardsFragmentUIMode.INVITES_PEOPLE_SEARCH) {
            return
        }
        var ctaText = CommonUtils.getString(R.string.permission_btn_allow)
        if (checkContactPermission()) {
            ctaText = CommonUtils.getString(R.string.search)
            viewBinding.searchAllowCta.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            viewBinding.searchCardsHintText.text = CommonUtils.getString(R.string.search_local_contacts)
        } else {
            activity?.let {
                if (PermissionUtils.isPermissionBlocked(it, Permission.READ_CONTACTS.permission)) {
                    ctaText = CommonUtils.getString(R.string.action_settings)
                    viewBinding.searchAllowCta.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                } else {
                    viewBinding.searchAllowCta.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_contact_small, 0, 0, 0)
                }
                viewBinding.searchCardsHintText.text = CommonUtils.getString(R.string.search_local_contacts_allow)
            }
        }
        viewBinding.searchAllowCta.text = ctaText
    }

    private fun checkContactPermission(): Boolean {
        return PermissionUtils.hasPermission(CommonUtils.getApplication(), Permission.READ_CONTACTS.permission)
    }

    companion object {
        @JvmStatic
        fun create(dynamicFeed: GeneralFeed,
                   pageId: String,
                   location: String? = null,
                   listType: String,
                   section: String,
                   searchUrl: String,
                   queryParam: String,
                   context: SearchPayloadContext,
                   groupInfo: GroupInfo ?= null,
                   searchLocation: SearchLocation,
                   tabType: String,
                   uiMode: SearchCardsFragmentUIMode? = SearchCardsFragmentUIMode.DEFAULT,
                   referrer: PageReferrer,
                   noContentLayoutId: Int = -1):
                SearchCardsFragment {
            val cardsFragmentBundle = bundleOf(
                    Constants.PAGE_ID to pageId,
                    Constants.LOCATION to location,
                    Constants.LIST_TYPE to listType,
                    NewsConstants.DH_SECTION to section,
                    Constants.BUNDLE_SEARCH_CONTEXT_PAYLOAD to context,
                    Constants.BUNDLE_SEARCH_QUERY to SearchQuery(searchPayloadContext = context),
                    Constants.BUNDLE_CLEAR_ON_NO_CONTENT to true,
                    Constants.BUNDLE_GROUP_INFO to groupInfo,
                    Constants.LIST_TAB_TYPE to tabType,
                    Constants.CARDS_FRAG_DISABLE_PULL_TO_REFRESH to true,
                    CardsFragment.DISABLE_MORE_NEWS_TOOLITP to true,
                    Constants.REFERRER to referrer,
                    Constants.BUNDLE_ERROR_LAYOUT_ID to noContentLayoutId)
            val bundle = bundleOf(
                    BUNDLE_CARDS_FRAGMENT to cardsFragmentBundle,
                    BUNDLE_DYNAMIC_FEED to dynamicFeed,
                    BUNDLE_SEARCH_URL to searchUrl,
                    BUNDLE_SEARCH_QUERY_PARAM to queryParam,
                    BUNDLE_UI_MODE to uiMode)
            return SearchCardsFragment().apply {
                this.arguments = bundle
            }
        }
    }
}

enum class SearchCardsFragmentUIMode: Serializable {
    INVITES_PEOPLE_SEARCH,
    DEFAULT
}