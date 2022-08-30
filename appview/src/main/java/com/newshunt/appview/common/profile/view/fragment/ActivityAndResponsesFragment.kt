/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.google.gson.reflect.TypeToken
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsExternalListener
import com.newshunt.appview.common.CardsFragment
import com.newshunt.appview.common.profile.DaggerActivityResponsesComponent
import com.newshunt.appview.common.profile.ProfileModule
import com.newshunt.appview.common.profile.helper.buildDynamicFeedForTab
import com.newshunt.appview.common.profile.helper.buildLocationForTab
import com.newshunt.appview.common.profile.helper.constructFilterInfoFrom
import com.newshunt.appview.common.profile.helper.constructRunTimeFiltersFrom
import com.newshunt.appview.common.profile.helper.createSignOnFragment
import com.newshunt.appview.common.profile.helper.getPageReferrerForTab
import com.newshunt.appview.common.profile.model.repo.ProfileRepo
import com.newshunt.appview.common.profile.view.ProfileClearDialog
import com.newshunt.appview.common.profile.view.activity.ProfileActivity
import com.newshunt.appview.common.profile.view.activity.ProfileViewState
import com.newshunt.appview.common.profile.viewmodel.ProfileViewModel
import com.newshunt.appview.common.profile.viewmodel.ProfileViewModelFactory
import com.newshunt.appview.common.ui.fragment.WebFragment
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.ListTransformType
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.*
import com.newshunt.dataentity.news.analytics.NHProfileAnalyticsEventParam
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.listener.FragmentScrollListener
import com.newshunt.profile.ExtraAnalyticsParameterProvider
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.profile.OptionsBottomSheetFragment
import com.newshunt.profile.SimpleOptions
import com.newshunt.sso.SSO
import com.newshunt.sso.SignInUIModes
import com.newshunt.sso.helper.ShowSignInPopup
import com.newshunt.sso.view.fragment.SignOnFragment
import javax.inject.Inject

/**
 * @author santhosh.kc
 */

private const val CARDS_FRAGMENT_TAG = "cards_fragment_tag"
private const val IN_PLACE_SIGN_ON_FRAGMENT_TAG = "in_place_sign_on_fragment_tag"
private const val CLEAR_ALL_ACTIVITIES_FRAGMENT_TAG = "clear_all_activities_tag"
private val NLI_SIGN_ON_FRAGMENT_SHOW_POSITION =
        PreferenceManager.getPreference(AppStatePreference.PROFILE_MAX_CARDS_GUEST, DEFAULT_MAX_CARDS_GUEST)

fun getCardsFragmentTag(profileTabs: ProfileTabs?): String {
    return CARDS_FRAGMENT_TAG + profileTabs?.tabType?.name
}

data class CurrentFilter(val profileFilter: ProfileFilter, val anchorViewId: Int,
                         var currentFilterOption: ProfileFilterOption?)

class ActivityAndResponsesFragment : BaseSupportFragment(), View.OnClickListener, View.OnKeyListener, ExtraAnalyticsParameterProvider, CardsExternalListener {

    private var profileTabs: ProfileTabs? = null
    private var viewState: ProfileViewState = ProfileViewState.TPV_USER
    private var tabPosition: Int = 0
    private var fragmentScrollListener: FragmentScrollListener? = null

    private lateinit var interactionsEditView: NHTextView
    private lateinit var clearAllLayout: ConstraintLayout
    private lateinit var filterLayout: ConstraintLayout
    private lateinit var signinFragmentHolder: View

    private var isInEditMode: Boolean = false

    private var signOnFragmentShownIfApplicable = false
    private var showSignInPopup: ShowSignInPopup? = null
    private var currentFilters: List<CurrentFilter>? = null
    private var currrentFilter:CurrentFilter? = null
    private var savedList: HashSet<String>? = null
    private var pageReferrer: PageReferrer? = null
    private var myUserId: String = Constants.EMPTY_STRING

    private lateinit var profileViewModel: ProfileViewModel
    @Inject
    lateinit var profileViewModelF: ProfileViewModelFactory
    private var renderedProfile: UserProfile? = null
    private var bookmarksList: List<String>? = null
    private lateinit var videoRequester: VideoRequester

    companion object {

        @JvmStatic
        fun newInstance(position: Int, profileTabs: ProfileTabs, fragmentScrollListener:
        FragmentScrollListener?, viewState: ProfileViewState): ActivityAndResponsesFragment {
            val fragment = ActivityAndResponsesFragment()
            val args = Bundle()
            args.putInt(NewsConstants.BUNDLE_ADAPTER_POSITION, position)
            args.putSerializable(BUNDLE_PROFILE_TAB, profileTabs)
            args.putSerializable(BUNDLE_PROFILE_VIEW_STATE, viewState)
            fragment.arguments = args
            fragment.fragmentScrollListener = fragmentScrollListener

            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            ViewModelProviders.of(it).get(FragmentCommunicationsViewModel::class.java)
                    .fragmentCommunicationLiveData.observe(this, Observer {
                if (it.hostId != uniqueScreenId) {
                    return@Observer
                }

                when (it.anyEnum) {
                    is RunTimeProfileFilter -> onFilterChanged(it.anyEnum as? RunTimeProfileFilter)
                    is CommonMessageEvents -> {
                        if (it.anyEnum == CommonMessageEvents.POSITIVE_CLICK) {
                            editModeDone(true)
                        }
                    }
                }

            })
        }
        showSignInPopup = context as? ShowSignInPopup
    }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        DaggerActivityResponsesComponent
                .builder()
                .profileModule(ProfileModule(SocialDB.instance()))
                .build()
                .inject(this)

        myUserId = SSO.getInstance().userDetails.userLoginResponse?.userId ?: Constants.EMPTY_STRING
        profileTabs = arguments?.getSerializable(BUNDLE_PROFILE_TAB) as? ProfileTabs
        removePreviousCardsFragment(profileTabs)
        viewState = arguments?.getSerializable(BUNDLE_PROFILE_VIEW_STATE) as? ProfileViewState
                ?: ProfileViewState.TPV_USER
        tabPosition = arguments?.getInt(NewsConstants.BUNDLE_ADAPTER_POSITION) ?: 0

        savedList = HashSet<String>();

        activity?.let {
            profileViewModel = ViewModelProviders.of(it, profileViewModelF).get(ProfileViewModel::class.java)
            renderedProfile = (it as ProfileActivity?)?.getRenderedProfile()
            if (ProfileTabType.SAVED == profileTabs?.tabType) {
                profileViewModel.fetchBookmarks()
                ProfileRepo.syncBookmarks(isForced = true)
            }
        }
        videoRequester = VideoRequester(fragmentId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_profile_activity_and_responses, container, false)
        setUpContentView(view, profileTabs)
        reloadCardsFragment()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (ProfileTabType.SAVED == profileTabs?.tabType) {
            profileViewModel.bookmarksLD.observe(viewLifecycleOwner, Observer {
                if (it.isSuccess) {
                    val result = it.getOrNull()
                    if (bookmarksList == null) {
                        bookmarksList = result
                        return@Observer
                    }
                    bookmarksList = result
                    getCardsFragment()?.refresh()
                }
            })
        }
    }

    private fun setUpContentView(view: View, profileTabs: ProfileTabs?) {
        profileTabs ?: return

        val editParent = view.findViewById<View>(R.id.activity_responses_edit_tab)
        filterLayout = view.findViewById(R.id.history_edit_row)
        signinFragmentHolder = view.findViewById(R.id.sign_in_fragment_holder)

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(this)

        if (shouldLoadSignInFragment(profileTabs)) {
            editParent.visibility = View.GONE
            return
        }

        setUpFilters(view, profileTabs)
        setUpEditLayout(view, profileTabs)
    }

    private fun setUpFilters(view: View, profileTabs: ProfileTabs) {
        val views = arrayOf<TextView>(view.findViewById(R.id.interaction_filter1),
                view.findViewById(R.id.interaction_filter2))
        views.forEach { it.visibility = View.GONE }
        var count = 0
        currentFilters = profileTabs.getValidFilters()?.map {
            val filterView = views[count]
            filterView.visibility = View.VISIBLE
            filterView.tag = count++
            enableFilterViewIfNeeded(filterView)
            val filter = constructFilterInfoFrom(filterView.id, it)
            setFilterTextView(filterView, filter)
            filter
        }
    }

    private fun enableFilterViewIfNeeded(view: View) {
        if (!SSO.getInstance().isLoggedIn(false) && profileTabs?.tabType != ProfileTabType.TPV_POSTS
                && profileTabs?.tabType != ProfileTabType.FPV_ACTIVITY) {
            view.isEnabled = false
            view.alpha = DISABLED_FILTER_ALPHA
        } else {
            view.isEnabled = true
            view.alpha = ENABLED_FILTER_ALPHA
            view.setOnClickListener(this)
        }
    }

    private fun setFilterTextView(view: TextView, currentFilter: CurrentFilter) {
        view.text = currentFilter.currentFilterOption?.displayName ?: Constants.EMPTY_STRING
    }

    private fun setUpEditLayout(view: View, profileTabs: ProfileTabs) {

        signinFragmentHolder = view.findViewById(R.id.sign_in_fragment_holder)
        interactionsEditView = view.findViewById(R.id.interaction_delete)
        clearAllLayout = view.findViewById(R.id.clearAll_layout)

        if (profileTabs.tabType != ProfileTabType.FPV_ACTIVITY) {
            if (CommonUtils.isEmpty(currentFilters)) {
                filterLayout.visibility = View.GONE
            }
            clearAllLayout.visibility = View.GONE
            interactionsEditView.visibility = View.GONE
            return
        }

        interactionsEditView.setOnClickListener(this)

        view.findViewById<NHTextView>(R.id.history_clear_all).setOnClickListener(this)
        view.findViewById<NHTextView>(R.id.history_delete_done).setOnClickListener(this)
        showOrHideEditIcon(false)
    }

    private fun showOrHideEditIcon(show: Boolean) {
        if (!::interactionsEditView.isInitialized) {
            return
        }

        profileTabs?.let {
            if (it.tabType == ProfileTabType.FPV_ACTIVITY) {
                interactionsEditView.visibility = if (show) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setClearAllLayoutMessage(clearAllMessageView: NHTextView?, profileTabs: ProfileTabs) {
        clearAllMessageView ?: return
        clearAllMessageView.text = when (profileTabs.tabType) {
            ProfileTabType.FPV_ACTIVITY -> CommonUtils.getString(R.string.clear_activity_log)
            else -> Constants.EMPTY_STRING
        }
    }

    override fun onStart() {
        super.onStart()
        loadContentFragment(profileTabs)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (!isAdded) {
            return
        }

        val fragment = getCardsFragment()

        if (fragment != null) {
            fragment.userVisibleHint = isVisibleToUser
            return
        }
        getSigninFragment()?.userVisibleHint = isVisibleToUser
    }

    private fun loadContentFragment(profileTabs: ProfileTabs?) {
        profileTabs ?: return

        pageReferrer = getPageReferrerForTab(profileTabs, profileTabs.tabType.name)
        if (shouldLoadSignInFragment(profileTabs)) {
            loadNonLoggedInTPVResponsesFragment()
        } else {
            loadCardsFragment(profileTabs)
        }
    }

    private fun shouldLoadSignInFragment(profileTabs: ProfileTabs): Boolean {
        return !SSO.getInstance().isLoggedIn(false) &&
                profileTabs.tabType == ProfileTabType.TPV_ACTIVITY &&
                viewState.isTPV()
    }

    private fun loadNonLoggedInTPVResponsesFragment() {
        val signOnFragment = childFragmentManager.findFragmentByTag(IN_PLACE_SIGN_ON_FRAGMENT_TAG) as? SignOnFragment

        if (signOnFragment != null || !::signinFragmentHolder.isInitialized) {
            return
        }

        signinFragmentHolder.visibility = View.VISIBLE

        val fragmentTransaction = childFragmentManager.beginTransaction()
        val fragment = createSignOnFragment(SignInUIModes.SIGN_IN, isFPV = false, delayPageView = !userVisibleHint, referrer = pageReferrer)
        fragmentTransaction.add(R.id.sign_in_fragment_holder, fragment, IN_PLACE_SIGN_ON_FRAGMENT_TAG)
        fragmentTransaction.commit()
    }

    private fun loadCardsFragment(profileTabs: ProfileTabs) {
        if (CommonUtils.isEmpty(profileTabs.contentUrl) || getCardsFragment() != null) {
            return
        }

        var layoutId = -1
        var enableDivider = false
        var listHorzPadding = 0
        var itemLocation: String? = null
        when (profileTabs.tabType) {
            ProfileTabType.FPV_POSTS -> {
                layoutId = R.layout.layout_no_my_posts
                enableDivider = false
            }
            ProfileTabType.SAVED -> {
                layoutId = R.layout.layout_no_saved
                itemLocation = Constants.LIST_TYPE_BOOKMARKS
                listHorzPadding = CommonUtils.getDimension(R.dimen.story_card_padding_left)
            }
            ProfileTabType.TPV_POSTS -> {
                layoutId = R.layout.layout_no_tpv_posts
                enableDivider = false
            }
            ProfileTabType.FPV_ACTIVITY -> {
                layoutId = R.layout.layout_no_activity
                listHorzPadding = CommonUtils.getDimension(R.dimen.story_card_padding_left)
            }
            ProfileTabType.TPV_ACTIVITY -> {
                layoutId = R.layout.layout_no_tpv_responses
                listHorzPadding = CommonUtils.getDimension(R.dimen.story_card_padding_left)
            }
            else -> {}
        }

        val menuListType: MenuLocation? = if (profileTabs.tabType == ProfileTabType.FPV_POSTS ||
                profileTabs.tabType == ProfileTabType.TPV_POSTS) {
            MenuLocation.PROFILE_POST_LIST
        } else {
            null
        }

        val listType = when (profileTabs.tabType) {
            ProfileTabType.FPV_ACTIVITY,
            ProfileTabType.TPV_ACTIVITY -> UiType2.USER_INTERACTION.name
            ProfileTabType.SAVED -> Constants.LIST_TYPE_BOOKMARKS
            else -> null
        }
        val cardsLimit = if (!SSO.getInstance().isLoggedIn(false) && ProfileTabType.SAVED == profileTabs.tabType) {
            PreferenceManager.getPreference(AppStatePreference.PROFILE_MAX_CARDS_GUEST, DEFAULT_MAX_CARDS_GUEST)
        } else {
            Integer.MAX_VALUE
        }

        val listTransformType = when (profileTabs.tabType) {
            ProfileTabType.SAVED -> ListTransformType.PROFILE_SAVED
            ProfileTabType.FPV_ACTIVITY,
            ProfileTabType.TPV_ACTIVITY -> ListTransformType.PROFILE_ACTIVITIES
            else -> ListTransformType.DEFAULT
        }
        val fragment = if(ProfileTabType.GENERIC_WEB == profileTabs.tabType) {
            val profile = renderedProfile
            profile ?: return
            val fragment = WebFragment()
            val type = object : TypeToken<PageEntity>(){}.type
            val page = JsonUtils.fromJson<PageEntity?>(profileTabs.tabData, type)
            page ?: return
            val args = bundleOf(NewsConstants.NEWS_PAGE_ENTITY to page,
                "adapter_position" to tabPosition)
            fragment.arguments = args
            fragment
        } else {
            val bundle = bundleOf(
                Constants.PAGE_ID to buildLocationForTab(
                    profileTabs.id ?: profileTabs.tabType.name, renderedProfile?.userId ?: Constants.EMPTY_STRING),
                NewsConstants.DH_SECTION to PageSection.PROFILE.section,
                Constants.BUNDLE_CLEAR_ON_NO_CONTENT to true,
                Constants.BUNDLE_MENU_CLICK_LOCATION to menuListType,
                Constants.BUNDLE_ERROR_LAYOUT_ID to layoutId,
                Constants.LIST_TYPE to listType,
                Constants.ITEM_LOCATION to itemLocation,
                Constants.CAN_SHOW_ITEM_DECORATION to enableDivider,
                Constants.BUNDLE_CARDS_LIMIT to cardsLimit,
                Constants.LIST_TRANSFORM_TYPE to listTransformType,
                Constants.LIST_HORZ_PADDING to listHorzPadding,
                Constants.REFERRER to pageReferrer,
                Constants.BUNDLE_IS_MY_POSTS_PAGE to (profileTabs.tabType == ProfileTabType.FPV_POSTS),
                Constants.BUNDLE_DELAY_SHOWING_FPE to true
            )
            val fragment = CardsFragment.create(bundle, null)
            fragment
        }
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.cards_fragment_holder, fragment, getCardsFragmentTag(profileTabs))
        fragmentTransaction.commit()
        fragment.userVisibleHint = userVisibleHint
    }

    private fun getCardsFragment(): CardsFragment? {
        return childFragmentManager.findFragmentByTag(getCardsFragmentTag(profileTabs)) as? CardsFragment
    }

    private fun onFilterChanged(filter: RunTimeProfileFilter?) {
        filter ?: return
        val filterClicked = currentFilters?.get(filter.filterPosition) ?: return
        val filterOptionClicked = filter.filterOption as? ProfileFilterOption ?: return
        val viewClicked = view?.findViewById<TextView>(filterClicked.anchorViewId) ?: return
        filterClicked.currentFilterOption = filterOptionClicked
        currrentFilter = filterClicked
        setFilterTextView(viewClicked, filterClicked)
        showOrHideEditIcon(false)
        reloadCardsFragment()
    }

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (isInEditMode) {
                    cancelEditMode()
                    return true
                }
            }
        }
        return false
    }

    override fun onClick(viewClicked: View?) {
        when (viewClicked?.id) {
            R.id.interaction_filter1, R.id.interaction_filter2 -> {
                val tag = viewClicked.tag as? Int ?: 0
                val simpleOptionItems =
                        constructRunTimeFiltersFrom(currentFilters?.get(tag)?.profileFilter, tag)
                                ?: return
                val options = SimpleOptions(simpleOptionItems, uniqueScreenId)
                fragmentManager?.let {
                    OptionsBottomSheetFragment.newInstance(options,false,currrentFilter?.currentFilterOption).show(it, uniqueScreenId.toString())
                }
            }
            R.id.interaction_delete -> goToEditMode()
            R.id.history_delete_done -> editModeDone(false)
            R.id.history_clear_all -> {
                fragmentManager?.let {
                    val commonMessageDialogOptions = CommonMessageDialogOptions(
                        uniqueScreenId,
                        CommonUtils.getString(R.string.clear_all_history),
                        CommonUtils.getString(R.string.clear_all_activity_msg),
                        CommonUtils.getString(R.string.clear_all),
                        CommonUtils.getString(R.string.cancel_text),
                        drawable = CommonUtils.getDrawable(CommonUtils.getResourceIdFromAttribute(activity,R.attr.profile_dialog_delete_icon))
                    )

                    ProfileClearDialog.newInstance(commonMessageDialogOptions).show(it, CLEAR_ALL_ACTIVITIES_FRAGMENT_TAG)
                }
            }
        }
    }

    private fun goToEditMode() {
        if (!::clearAllLayout.isInitialized || !::interactionsEditView.isInitialized) {
            return
        }
        isInEditMode = true
        clearAllLayout.visibility = View.VISIBLE
        interactionsEditView.isEnabled = false
        interactionsEditView.alpha = DISABLED_FILTER_ALPHA
        getCardsFragment()?.goToEditMode()
    }

    private fun editModeDone(allCleared: Boolean) {
        if (!::clearAllLayout.isInitialized || !::interactionsEditView.isInitialized) {
            return
        }
        profileViewModel.deleteActivitiesLiveData.removeObservers(this)
        profileViewModel.deleteActivitiesLiveData.observe(this, Observer {
            if (it.isSuccess || !allCleared) {
                //even if it is failure and not all cleared case, we will sync the failed items
                // later, but exit the edit mode
                val cardsFragment = getCardsFragment()
                isInEditMode = false
                clearAllLayout.visibility = View.GONE
                interactionsEditView.isEnabled = true
                interactionsEditView.alpha = 1f
                if (allCleared) {
                    cardsFragment?.clearAllInList()
                }
                cardsFragment?.onEditModeDone()
            } else {
                FontHelper.showCustomFontToast(activity,
                        CommonUtils.getString(R.string.error_connectivity), Toast.LENGTH_SHORT)
            }
        })
        profileViewModel.deleteUserActivities(allCleared)
    }

    private fun cancelEditMode() {
        if (!::clearAllLayout.isInitialized || !::interactionsEditView.isInitialized) {
            return
        }
        isInEditMode = false
        clearAllLayout.visibility = View.GONE
        interactionsEditView.isEnabled = true
        interactionsEditView.alpha = 1f
        getCardsFragment()?.onEditModeCancel()
        profileViewModel.undoDeleteUserActivities()
    }

    private fun reloadCardsFragment() {
        renderedProfile?.let { profile ->
            profileTabs?.let {
                currentFilters?.let { filters ->
                    buildDynamicFeedForTab(it, filters, profile.userId)?.let { dynamicFeed ->
                        profileViewModel.updateContentUrl(dynamicFeed)
                    }
                }
            }
        }
    }

    private fun removePreviousCardsFragment(profileTabs: ProfileTabs?) {
        childFragmentManager.findFragmentByTag(getCardsFragmentTag(profileTabs))?.let {
            val transaction = childFragmentManager.beginTransaction()
            transaction.remove(it)
            transaction.commit()
        }
    }

    /**
     * This method adds tab specific params to the map for events generated in this tab
     */
    override fun getExtraAnalyticsParams(): Map<NhAnalyticsEventParam, Any>? {
        if (CommonUtils.isEmpty(currentFilters)) return null

        val paramMap = HashMap<NhAnalyticsEventParam, Any>()
        val filterTypeParam = when (profileTabs?.tabType) {
            ProfileTabType.TPV_ACTIVITY,
            ProfileTabType.FPV_ACTIVITY -> {
                NHProfileAnalyticsEventParam.ACTIVITY_FILTER_TYPE
            }
            ProfileTabType.FPV_POSTS,
            ProfileTabType.TPV_POSTS -> {
                NHProfileAnalyticsEventParam.POST_FILTER_TYPE
            }
            else -> {
                null
            }
        }
        filterTypeParam?.let {
            paramMap[it] = (currentFilters!!.map { currentFilter -> currentFilter.currentFilterOption?.value }).toString()
        }
        profileTabs?.tabType?.let {
            paramMap[NhAnalyticsNewsEventParam.TABTYPE] = it.deeplinkValue
        }
        return paramMap
    }

    override fun updateCurrentCardPosition(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int) {
        onVisibleWindowChanged(firstVisibleItem, visibleItemCount, totalItemCount)
    }

    override fun onCardsLoadError(fullPageError: Boolean) {
        showOrHideEditIcon(false)
    }

    override fun onCardsLoaded() {
        showOrHideEditIcon(true)
    }

    private fun getSigninFragment(): SignOnFragment? {
        return childFragmentManager.findFragmentByTag(IN_PLACE_SIGN_ON_FRAGMENT_TAG) as? SignOnFragment?
    }

    private fun onVisibleWindowChanged(firstVisibleItem: Int, visibleItemCount: Int,
                                       totalItemCount: Int) {
        showSignInPopup?.apply {
            if (profileTabs?.tabType == ProfileTabType.TPV_POSTS && !SSO.getInstance().isLoggedIn(false)
                    && !signOnFragmentShownIfApplicable
                    && firstVisibleItem + visibleItemCount > NLI_SIGN_ON_FRAGMENT_SHOW_POSITION) {
                this.showSignInPopup()
                signOnFragmentShownIfApplicable = true
            }
        }
    }
}