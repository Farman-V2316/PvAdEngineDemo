/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.view.activity

import android.app.Activity
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.appbar.AppBarLayout
import com.newshunt.analytics.helper.ReferrerProviderHelper
import com.newshunt.appview.R
import com.newshunt.appview.common.group.getPreferredApprovalTab
import com.newshunt.appview.common.postcreation.analytics.helper.CreatePostAnalyticsHelper
import com.newshunt.appview.common.profile.DaggerProfileComponent
import com.newshunt.appview.common.profile.ProfileModule
import com.newshunt.appview.common.profile.helper.analytics.getCommonEventParamsForEntity
import com.newshunt.appview.common.profile.helper.analytics.logProfile3DotsMenuActionEvent
import com.newshunt.appview.common.profile.helper.analytics.logProfile3DotsMenuViewedEvent
import com.newshunt.appview.common.profile.helper.analytics.logProfileViewedEvent
import com.newshunt.appview.common.profile.view.adapter.ProfileInteractionsAdapter
import com.newshunt.appview.common.profile.view.interfaces.ProfileFlow
import com.newshunt.appview.common.profile.viewmodel.ProfileViewModel
import com.newshunt.appview.common.profile.viewmodel.ProfileViewModelFactory
import com.newshunt.appview.common.ui.activity.AuthorizationBaseActivity
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.helper.SnackbarViewModel
import com.newshunt.appview.common.viewmodel.FollowNudgeViewModel
import com.newshunt.appview.databinding.ProfileActivityBinding
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.track.DailyhuntUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.ApprovalCounts
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.EventsInfo
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.*
import com.newshunt.dataentity.news.analytics.ProfileReferrer
import com.newshunt.dataentity.news.analytics.ProfileReferrerSource
import com.newshunt.dataentity.notification.FollowModel
import com.newshunt.dataentity.onboarding.RegistrationState
import com.newshunt.dataentity.onboarding.RegistrationUpdate
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SearchSuggestionType
import com.newshunt.dataentity.social.entity.MenuL1Id
import com.newshunt.dataentity.social.entity.MenuPayLoad2
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NhBrowserNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.CustomTabsUtil
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.dhutil.view.listener.TextDescriptionSizeChangeListener
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.navigation.helper.settingsClick
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.helper.DefaultNavigatorCallback
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.helper.NewsListCardLayoutUtil
import com.newshunt.news.helper.ProfileToolTipWrapper
import com.newshunt.news.helper.StoryShareUtil
import com.newshunt.news.helper.handler.NudgeTooltipWrapper
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.util.NewsConstants.BUNDLE_ACTIVITY_REFERRER
import com.newshunt.news.view.activity.UserFollowActivity
import com.newshunt.news.view.fragment.NERDescriptionBottomSheetFragment
import com.newshunt.news.view.listener.FragmentScrollListener
import com.newshunt.news.view.listener.MenuListenerProvider
import com.newshunt.news.view.listener.MenuOptionClickListener
import com.newshunt.notification.sqlite.NotificationDB
import com.newshunt.onboarding.presenter.AppRegistrationHandler
import com.newshunt.profile.ExtraAnalyticsParameterProvider
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.profile.OptionsBottomSheetFragment
import com.newshunt.profile.SimpleOptionItem
import com.newshunt.profile.SimpleOptions
import com.newshunt.sdk.network.image.Image
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import com.newshunt.sso.SSO
import com.newshunt.sso.SignInUIModes
import com.newshunt.sso.helper.OVERLAY_SIGN_ON_FRAGMENT_TAG
import com.newshunt.sso.helper.SSOSignInPopup
import com.newshunt.sso.helper.ShowSignInPopup
import com.newshunt.sso.helper.TPV_SIGNIN_FRAGMENT_TAG
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType
import com.newshunt.sso.view.fragment.SignOnFlow
import com.squareup.otto.Subscribe
import java.io.Serializable
import javax.inject.Inject
import kotlin.collections.set
import kotlinx.android.synthetic.main.profile_appbar.nh_notification_icon

/**
 * User Profile activity. Handles FPV and TPV of User profile
 * <p>
 * Created by srikanth.ramaswamy on 04/17/2019.
 */
private const val LOG_TAG = "UserProfileActivity"
private const val REQ_CODE_BLOCKED_ACTIVITY = 1250
private const val REQ_CODE_FOLLOWING_ACTIVITY = 1251
private const val REQ_CODE_FOLLOWERS_ACTIVITY = 1252
private const val REQ_CODE_EDIT_PROFILE_ACTIVITY = 1253

class ProfileActivity : AuthorizationBaseActivity(), ProfileFlow, View.OnClickListener,
        FragmentScrollListener, ShowSignInPopup, SignOnFlow,
        ReferrerProviderlistener, ViewPager.OnPageChangeListener, MenuListenerProvider,
    TextDescriptionSizeChangeListener {

    private lateinit var interactionsAdapter: ProfileInteractionsAdapter
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var editProfileToolTip: ProfileToolTipWrapper
    private lateinit var nudgeTooltipWrapper: NudgeTooltipWrapper
    private lateinit var viewBinding: ProfileActivityBinding

    private var viewState = ProfileViewState.NONE
    private var renderedUserProfile: UserProfile? = null //The user being rendered by this instance
    private var approvalCounts: ApprovalCounts? = null
    private var errorMessageBuilder: ErrorMessageBuilder? = null
    private var requestedProfile: UserBaseProfile? = null //The user being requested in bundle
    private var preferredProfileLandingType: ProfileTabType? = null
    private var preferredProfileDefaultTabId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var signInFragmentTag = Constants.EMPTY_STRING
    private val referrerProviderHelper = ReferrerProviderHelper()
    private var referrer_raw: String? = null
    private var isWaitingFPVLogin = false
    private var isInternalDeeplink: Boolean = false
    private lateinit var nudgeVM: FollowNudgeViewModel
    private var blockUnblockText = CommonUtils.getString(R.string.block_text)

    @Inject
    lateinit var profileViewModelF: ProfileViewModelFactory
    @Inject
    lateinit var snackbarViewModelFactory: SnackbarViewModel.Factory

    private var autoFollowFromNotification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerProfileComponent
                .builder()
                .profileModule(ProfileModule(SocialDB.instance()))
                .build()
                .inject(this)

        viewBinding = DataBindingUtil.setContentView(this, R.layout.profile_activity)
        pageReferrer = extractReferrerFromBundle(intent.extras)
        isInternalDeeplink = intent.extras?.getBoolean(Constants.IS_INTERNAL_DEEPLINK, false) ?: false
        referrerProviderHelper.addReferrerByProvider(pageReferrer)
	    if (!CommonUtils.isEmpty(intent.getStringExtra(Constants.REFERRER_RAW))) {
		    referrer_raw = intent.getStringExtra(Constants.REFERRER_RAW)
	    }

        autoFollowFromNotification = intent?.getBooleanExtra(Constants.BUNDLE_AUTO_FOLLOW_FROM_NOTIFICATION, false)?:false

        NewsListCardLayoutUtil.manageLayoutDirection(viewBinding.profileRootView)
        setUpActionBar()
        if (PreferenceManager.getPreference(AppStatePreference.PROFILE_TOOL_TIP_LAUNCH, -1) < 0) {
            PreferenceManager.savePreference(AppStatePreference.PROFILE_TOOL_TIP_LAUNCH,
                    AppUserPreferenceUtils.getAppLaunchCount())
        }
        profileViewModel = ViewModelProviders.of(this, profileViewModelF).get(ProfileViewModel::class.java)

        nudgeVM = ViewModelProviders.of(this).get(FollowNudgeViewModel::class.java)
        observeFragmentCommunications()
        ViewModelProviders.of(this, snackbarViewModelFactory).get(SnackbarViewModel::class.java)
                .also {
                    it.followChanges.observe(this, Observer {
                        SnackbarViewModel.onFollowChangeEvent(it, viewBinding.root)
                    })
                    it.newPostChanges.observe(this, Observer {res ->
                        Logger.d(LOG_TAG, "isInMyPosts ${isInMyPosts()}")
                        SnackbarViewModel.onPostUploaded(res, viewBinding.root, !isInMyPosts(),
                                null, R.string.view_photo_in_lite_mode_message)
                    })
                    it.start()
                }
        observeApprovalCard()
        observeProfileLivedata()
        observeNotificationLiveData()
    }

    private fun observeNudges() {
        if(PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED,false)) {
            pageReferrer?.let {
                renderedUserProfile?.isFollowing?.let { isFollowing ->
                    nudgeVM.nudges(it, isFollowing, intent.extras)
                        .observe(this, Observer { events ->
                            Logger.d(LOG_TAG, "loadData: $events")
                            events.firstOrNull()?.let { followTooltip(it) }
                        })
                }

            }
        }
    }

    private fun observeNotificationLiveData() {
        AppSettingsProvider.getNotificationLiveData().observe(this, Observer {
            viewBinding.profileAppBar.nhNotificationIcon.onNotificationEventChanged(it)
            viewBinding.profileNerHeader.profileAppBar.nhNotificationIcon.onNotificationEventChanged(it)
        })
    }

    override fun onResume() {
        super.onResume()
        CommonUtils.runInBackground{
            val count = NotificationDB.instance().getNotificationDao().getUnseenNotificationCount()
            AppSettingsProvider.getNotificationLiveData().postValue(count > 0)
        }
    }

    override fun launchActivity(intent: Intent) {
        startActivity(intent)
    }

    override fun launchNewsHome() {
        val prevNewsAppSection = AppSectionsProvider.getAnyUserAppSectionOfType(AppSection.NEWS)
        prevNewsAppSection ?: return
        CommonNavigator.launchNewsHome(this, false, prevNewsAppSection.id, prevNewsAppSection.appSectionEntityKey)
        finish()
    }

    override fun onClick(view: View?) {
        view ?: return
        when (view.id) {
            R.id.user_fullName,
            R.id.user_handle,
            R.id.user_profilePic,R.id.login_btn -> {
                if (!isSocialLogin) {
                    triggerFPVLogin()
                }
            }
            R.id.profile_three_dots -> {
                showProfileOptionsMenu()
            }
            R.id.bio_expand_icon -> {
                onDescriptionExpanded(true,null)
            }
            R.id.profile_follow_view1,
            R.id.profile_follow_view2,
            R.id.followers_layout,
            R.id.following_layout -> {
                (view.tag as? FollowClickables?)?.let {
                    handleFollowViewClick(it)
                }
            }
            R.id.actionbar_back_button -> {
                handleBack(false)
            }
            R.id.settings_icon -> {
                handleSettingsClick()
            }
            R.id.profile_detail_create_post -> {
                val referrer = PageReferrer(ProfileReferrer.PROFILE, renderedUserProfile?.userId)
                CreatePostAnalyticsHelper.logCreatePostClickEvent(referrer)
                val suggestion = if ( viewState.isTPV() && renderedUserProfile?.taggingPermission == AccountPermission.ALLOWED) {
                    SearchSuggestionItem(itemId = renderedUserProfile?.userId ?: "",
                        suggestion = renderedUserProfile?.handle ?: "",
                        name = renderedUserProfile?.name ?: renderedUserProfile?.handle,
                        typeName = SearchSuggestionType.HANDLE.type)
                } else {
                    null
                }
                val intent = CommonNavigator.getPostCreationIntent(null, null, suggestion, referrer)
                startActivity(intent)
            }
            R.id.follow_button,
            R.id.follow_profile -> {
                renderedUserProfile?.let {
                    profileViewModel.toggleFollow(it)
                }
            }
            R.id.approval_card -> {
                approvalCounts?.let {
                    CommonNavigator.launchApprovalsActivity(view.context, getPreferredApprovalTab(it), PageReferrer(ProfileReferrer.PROFILE))
                    AnalyticsHelper2.logApprovalCardClickEvent(PageReferrer(ProfileReferrer.PROFILE),
                            NewsExploreButtonType.APPROVAL_CARD, renderedUserProfile?.userId!!, it.TOTAL_PENDING_APPROVALS?.value)
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        BusProvider.getUIBusInstance().register(this)
    }

    override fun onStop() {
        super.onStop()
        BusProvider.getUIBusInstance().unregister(this)
    }


    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::editProfileToolTip.isInitialized) {
            editProfileToolTip.hideToolTip()
        }
        super.onDestroy()
    }

    override fun smoothScrollToolBar() {

    }

    override fun onBackPressed() {
        handleBack(true,referrer_raw)
    }

    override fun onPageScrollStateChanged(state: Int) {
        //DO NOTHING
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        //DO NOTHING
    }

    override fun onPageSelected(position: Int) {
        renderedUserProfile?.tabs?.let {
            if (CommonUtils.isEmpty(it)) {
                return@let
            }

            referrerProviderHelper.addReferrerByProvider(PageReferrer(it[position].tabType
                    .referrer, it[position].tabType.deeplinkValue))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_CODE_BLOCKED_ACTIVITY,
                REQ_CODE_FOLLOWERS_ACTIVITY,
                REQ_CODE_FOLLOWING_ACTIVITY -> {
                    if (viewState.isFPV()) {
                        Logger.d(LOG_TAG, "onActivityResult, requestCode: $requestCode, resultCode: $resultCode, recreating the activity")
                        recreate()
                    }
                }
            }
        } else {
            Logger.d(LOG_TAG, "onActivityResult, requestCode: $requestCode, resultCode: $resultCode")
        }
    }

    override fun showSignInPopup() {
        viewBinding.signInFragmentBackground.visibility = View.VISIBLE
        signInFragmentTag = OVERLAY_SIGN_ON_FRAGMENT_TAG
        viewBinding.overlaySignInFragmentHolder.visibility = View.VISIBLE
        SSOSignInPopup.showSignInPopup(this, R.id.overlay_sign_in_fragment_holder, referrer = pageReferrer)
    }

    override fun onSignOnDismissed() {
        viewBinding.signInFragmentBackground.visibility = View.GONE
        viewBinding.overlaySignInFragmentHolder.visibility = View.GONE
        SSOSignInPopup.dismissSignInPopup(this, signInFragmentTag)
    }

    override fun onLoginSuccess(pendingIntent: PendingIntent?) {
        onSignOnDismissed()
    }

    override fun getReferrerEventSection(): NhAnalyticsEventSection {
        return NhAnalyticsEventSection.PROFILE
    }

    override fun getProvidedReferrer(): PageReferrer {
        return referrerProviderHelper.providedPageReferrer
    }

    override fun getLatestPageReferrer(): PageReferrer {
        return referrerProviderHelper.referrerQueue.yongest
    }

    override fun getExtraAnalyticsParams(): Map<NhAnalyticsEventParam, Any?>? {
        if (!this::interactionsAdapter.isInitialized) {
            return null
        }

        val returnMap = getCommonEventParamsForEntity(CommonUtils.equals(myUserId.userId, renderedUserProfile?.userId))
        (interactionsAdapter.currentFragment as? ExtraAnalyticsParameterProvider?)?.getExtraAnalyticsParams()?.let {
            returnMap.putAll(it)
        }
        return returnMap
    }

    override fun menuOptionClickListener(): MenuOptionClickListener? {
        if (!::interactionsAdapter.isInitialized) {
            return null
        }

        interactionsAdapter.currentFragment?.let {
            if (it is MenuOptionClickListener) {
                return it
            }
        }
        return null
    }

    override fun getLogTag(): String {
        return LOG_TAG
    }

    private fun showNERHeaderLayout():Boolean {
        return viewState.isTPV() || SSO.getInstance().isLoggedIn(false)
    }

    private fun setUpActionBar() {
        if(showNERHeaderLayout() ) {
            setUpActionBar(viewBinding.profileNerHeader.profileAppBar.profileActionBar)
            viewBinding.profileNerHeader.profileAppBar.settingsIcon.setOnClickListener(this)
            viewBinding.profileNerHeader.profileAppBar.actionbarBackButton.setOnClickListener(this)
            viewBinding.profileNerHeader.profileAppBar.profileThreeDots.setOnClickListener(this)
            return
        }
        setUpActionBar(viewBinding.profileAppBar.profileActionBar)
        viewBinding.profileAppBar.settingsIcon.setOnClickListener(this)
        viewBinding.profileAppBar.actionbarBackButton.setOnClickListener(this)
        viewBinding.profileAppBar.profileThreeDots.setOnClickListener(this)

    }

    private fun setupTabs() {
        supportFragmentManager ?: return
        renderedUserProfile?.tabs ?: return
        renderedUserProfile?.let { renderedProfile ->
            //If in TPV AND we are either seeing a private profile
            if (viewState.isTPV() && renderedProfile.isPrivateProfile()) {
                Logger.d(LOG_TAG, "Not showing TPV view, viewState: $viewState, isPrivate: ${renderedUserProfile?.isPrivateProfile()}, socialLogin: $isSocialLogin")
                return
            }

            viewBinding.profileTabs.interactionViewPager.addOnPageChangeListener(this)
            viewBinding.profileTabs.interactionViewPager.pagingEnabled = CustomTabsUtil.tabsSwipeEnabled()

            val activeColor = ThemeUtils.getThemeColorByAttribute(this, R.attr.tab_title_select_color)
            val inactiveColor = ThemeUtils.getThemeColorByAttribute(this, R.attr.tab_title_color)
            viewBinding.profileTabs.interactionTabLayout.setTabTextColor(activeColor, inactiveColor)
            viewBinding.profileTabs.interactionTabLayout.setDrawBottomLine(false)
            viewBinding.profileTabs.interactionTabLayout.setCustomTabView(R.layout.profile_tab_view, R.id.profile_tab_textview, View.NO_ID)
            viewBinding.profileTabs.interactionTabLayout.setTabSelectionLineHeight(CommonUtils.getDimension(R.dimen.profile_tabs_selection_indicator_height))

            if (!::interactionsAdapter.isInitialized) {
                interactionsAdapter = ProfileInteractionsAdapter(supportFragmentManager, this, viewState)
                viewBinding.profileTabs.interactionViewPager.adapter = interactionsAdapter
            }
            interactionsAdapter.interactionTabList = renderedProfile.tabs
            interactionsAdapter.notifyDataSetChanged()
            viewBinding.profileTabs.interactionTabLayout.setViewPager(viewBinding.profileTabs.interactionViewPager)
            setDefaultTab(preferredProfileDefaultTabId ?: renderedProfile.defaultTabId, preferredProfileLandingType ?: renderedProfile.defaultTabType)
        }
    }

    private fun setupProfileDetails() {
        //Before calling this method, ensure userProfileInfo is initialized.

        // To handle NER UI and all other UI states except fpv logged out. (NER + FPV/TPV)
        if(viewState.isTPV() || SSO.getInstance().isLoggedIn(false) ) {
            handleProfilePageUI()
            return
        }

        // To handle default UI FPV - Logged out state.
        renderedUserProfile?.let { renderedProfile ->
            viewBinding.profile = renderedProfile
            viewBinding.profilePersonalDetails.root.visibility = View.VISIBLE
            viewBinding.profileAppBar.root.visibility = View.VISIBLE
            viewBinding.profilePersonalDetails.profile = renderedProfile
            var needThreeDots = true
            val followingView1 = viewBinding.profilePersonalDetails.profileFollowView1
            val followingView2 = viewBinding.profilePersonalDetails.profileFollowView2
            val followingView2Grp = viewBinding.profilePersonalDetails.followingView2Grp
            val dividerBelowPhoto = viewBinding.profilePersonalDetails.dividerBelowPhoto
            val profileBio = viewBinding.profilePersonalDetails.profileBio

            val followingOriginalString = "<b>${renderedProfile.followingCount}</b> ${CommonUtils.getString(R.string.following)}"
            val followingString = Html.fromHtml(FontHelper.getFontConvertedString(followingOriginalString)) as Spannable
            val followersOriginalString = "<b>${renderedProfile.followersCount}</b> ${CommonUtils.getString(R.string.followers)}"
            val followersString = Html.fromHtml(FontHelper.getFontConvertedString(followersOriginalString)) as Spannable
            followingView1.visibility = View.VISIBLE
            followingView2Grp.visibility = View.VISIBLE

            viewBinding.profilePersonalDetails.approvalCard.approvalCardRootview.setOnClickListener(this)

            viewBinding.profilePersonalDetails.profileViewState = viewState

            if (isSocialLogin || viewState.isTPV()) {
                viewBinding.profilePersonalDetails.userFullName.text = renderedProfile.name
                viewBinding.profilePersonalDetails.userHandle.text = CommonUtils.formatHandleForDisplay(renderedProfile.handle)
                viewBinding.profilePersonalDetails.userFullName.setTextColor(CommonUtils.getColor(CommonUtils.getResourceIdFromAttribute(this, R.attr.user_fullname_color)))
                viewBinding.profilePersonalDetails.userHandle.setTextColor(CommonUtils.getColor(CommonUtils.getResourceIdFromAttribute(this, R.attr.user_fullname_color)))
            }

            renderedProfile.profileImage?.let {
                val profilePicSize = CommonUtils.getDimension(R.dimen.profile_pic_width)
                Image.load(ImageUrlReplacer.getQualifiedImageUrl(it, profilePicSize, profilePicSize))
                    .placeHolder(ContextCompat.getDrawable(this, R.drawable.default_user_avatar))
                    .transform(CircleCrop())
                    .into(viewBinding.profilePersonalDetails.userProfilePic)
            }

            when (viewState) {
                ProfileViewState.NONE -> {
                    return
                }
                ProfileViewState.FPV_USER,
                ProfileViewState.FPV_CREATOR -> {
                    needThreeDots = isSocialLogin
                    if (!isSocialLogin) {
                        setupFollowingView(followingView1, followingString, followingOriginalString, FollowClickables.FOLLOWING)
                        setupFollowingView(followingView2Grp, null, null, null)
                        viewBinding.profilePersonalDetails.userProfilePic.setOnClickListener(this)
                        viewBinding.profilePersonalDetails.userFullName.setOnClickListener(this)
                        viewBinding.profilePersonalDetails.userHandle.setOnClickListener(this)
                    } else {
                        setupFollowingView(followingView1, followersString, followersOriginalString, FollowClickables.FOLLOWERS)
                        setupFollowingView(followingView2, followingString, followingOriginalString, FollowClickables.FOLLOWING)
                    }
                }
                ProfileViewState.TPV_USER,
                ProfileViewState.TPV_CREATOR -> {
                    if (renderedProfile.isPrivateProfile()) {
                        //We are viewing a private profile. Don't show following
                        setupFollowingView(followingView1, null, null, null)
                        viewBinding.profilePersonalDetails.followingView2Grp.visibility = View.GONE
                        viewBinding.profilePersonalDetails.profilePersDetailsDivider.visibility = View.GONE
                        viewBinding.profileTabs.profileTabsRootView.visibility = View.GONE
                        if (isSocialLogin) {
                            viewBinding.privateProfileIndicator.visibility = View.VISIBLE
                            enableScrolling(false)
                        }
                    } else {
                        //public TPV, all details are shown
                        setupFollowingView(followingView1, followersString, followersOriginalString, FollowClickables.FOLLOWERS)
                        setupFollowingView(followingView2, followingString, followingOriginalString, FollowClickables.FOLLOWING)
                        setupFollowView(renderedProfile)
                    }

                    if (CommonUtils.isEmpty(renderedProfile.profileShareUrl)) {
                        needThreeDots = false
                    }
                    if(renderedProfile.taggingPermission == AccountPermission.ALLOWED) {
                        viewBinding.profileDetailCreatePost.show()
                        viewBinding.profileDetailCreatePost.setOnClickListener(this)
                    }
                }
            }

            if (!renderedProfile.bio.isNullOrEmpty() && !(viewState.isTPV() && renderedProfile.isPrivateProfile())) {
                profileBio.visibility = View.VISIBLE
                profileBio.setText(renderedProfile.bio, false, null)
            } else {
                profileBio.visibility = View.GONE
            }

            viewBinding.profilePersonalDetails.locationInfo.visibility = if (renderedProfile.uiLocation?.name?.isNotEmpty() == true && !(viewState.isTPV() && renderedProfile.isPrivateProfile())) {
                View.VISIBLE
            } else {
                View.GONE
            }

            dividerBelowPhoto.visibility = if (isDividerBelowPhotoNeeded()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (!needThreeDots) {
                viewBinding.profilePersonalDetails.profileThreeDots.visibility = View.GONE
                viewBinding.profileNerHeader.profileAppBar.profileThreeDots.visibility = View.GONE
                viewBinding.profileAppBar.profileThreeDots.visibility = View.GONE
            } else {
                viewBinding.profilePersonalDetails.profileThreeDots.setOnClickListener(this)
            }

            followingView1.setOnClickListener(this)
            followingView2.setOnClickListener(this)
            viewBinding.executePendingBindings()
        }
    }
/**
 * Handles NER UI.
 * FPV Logged in state.
 * TPV state.
 */
    private fun handleProfilePageUI() {
        val profileNerHeader = viewBinding.profileNerHeader
        profileNerHeader.root.visibility = View.VISIBLE
        renderedUserProfile?.let {
            viewBinding.profile = it
            var needThreeDots = true
            it.bio?.let { bio ->
                if (bio.isNotEmpty()) {
                    profileNerHeader.profileBio.let { textView ->
                        textView.visibility = View.VISIBLE
                        textView.text = bio
                    }
                    profileNerHeader.bioExpandIcon.visibility = View.VISIBLE
                    profileNerHeader.bioExpandIcon.setOnClickListener(this)
                }
            }
            if (isSocialLogin || viewState.isTPV()) {
                it.name?.let { name ->
                    profileNerHeader.userFullName.visibility = View.VISIBLE
                    profileNerHeader.userFullName.text  = name
                    updateTextForView(profileNerHeader.userFullName,name,null,profileNerHeader.userFullName)
                }
            }

            it.profileImage?.let { imageUrl ->
                val profilePicSize = CommonUtils.getDimension(R.dimen.profile_pic_width)
                Image.load(ImageUrlReplacer.getQualifiedImageUrl(imageUrl, profilePicSize, profilePicSize))
                    .placeHolder(ContextCompat.getDrawable(this, R.drawable.default_user_avatar))
                    .transform(CircleCrop())
                    .into(profileNerHeader.userProfilePic)
            }

            it.bannerImageUrl?.let { imageUrl ->
                val profilBannerSize = CommonUtils.getDimension(R.dimen.profile_banner_image_height)
                Image.load(ImageUrlReplacer.getQualifiedImageUrl(imageUrl, CommonUtils.getDeviceScreenWidthInDp
                    (), profilBannerSize))
                    .placeHolder(ContextCompat.getDrawable(this, R.drawable.default_user_avatar))
                    .into(profileNerHeader.backgroundImage)

            }
            val divStr = "<vertical_div>"
            val subTitleText = arrayListOf<String>()
            it.handle?.let { handle ->
                subTitleText.add(handle)
            }
            it.subTitle?.let { subTitle ->
                if(subTitleText.isNotEmpty()) {
                    subTitleText.add("  $divStr  ")
                }
                subTitleText.add(subTitle)
            }
            it.uiLocation?.name?.let { location ->
                if(location.isNotEmpty()) {
                    if (subTitleText.isNotEmpty()) {
                        subTitleText.add("  $divStr  ")
                    }
                    subTitleText.add(location)
                }
            }
            if(subTitleText.isNotEmpty()) {
                profileNerHeader.locationInfo.visibility = View.VISIBLE
                profileNerHeader.userSubtitle.text = subTitleText.joinToString(separator="")
                addImageDrawable(profileNerHeader.userSubtitle,divStr)
            } else {
                profileNerHeader.locationInfo.visibility = View.GONE
            }

            when(viewState) {
                ProfileViewState.NONE -> {
                    return
                }
                ProfileViewState.FPV_USER,
                ProfileViewState.FPV_CREATOR -> {
                    needThreeDots = isSocialLogin
                    if (!isSocialLogin) {
                        it.followingCount.let { followingCount ->
                            updateTextForView(profileNerHeader.followingCount,followingCount,FollowClickables.FOLLOWING,profileNerHeader.followingLayout)
                        }
                        viewBinding.profileNerHeader.userProfilePic.setOnClickListener(this)
                        viewBinding.profileNerHeader.userFullName.setOnClickListener(this)
                    } else {
                        profileNerHeader.followersLayout.visibility = View.VISIBLE
                        it.followingCount.let { followingCount ->
                            updateTextForView(profileNerHeader.followingCount,followingCount,FollowClickables.FOLLOWING,profileNerHeader.followingLayout)
                        }
                        it.followersCount.let { followersCount ->
                            profileNerHeader.followersLayout.visibility = View.VISIBLE
                            updateTextForView(profileNerHeader.followersCount,followersCount,FollowClickables.FOLLOWERS,profileNerHeader.followersLayout)
                        }
                    }
                }
                ProfileViewState.TPV_USER,
                ProfileViewState.TPV_CREATOR -> {
                    if (it.isPrivateProfile()) {
                        //We are viewing a private profile. Don't show following
                        viewBinding.profileTabs.profileTabsRootView.visibility = View.GONE
                        if (isSocialLogin) {
                            viewBinding.privateProfileIndicator.visibility = View.VISIBLE
                            enableScrolling(false)
                        }
                    } else {
                        updateTextForView(profileNerHeader.followersCount, it.followersCount, FollowClickables.FOLLOWERS,profileNerHeader.followersLayout)
                        updateTextForView(profileNerHeader.followingCount, it.followingCount, FollowClickables.FOLLOWING,profileNerHeader.followingLayout)
                        setupFollowView(it)
                    }
                    if (CommonUtils.isEmpty(it.profileShareUrl)) {
                        needThreeDots = false
                    }
                }
            }

            if (!needThreeDots) {
                viewBinding.profilePersonalDetails.profileThreeDots.visibility = View.GONE
                viewBinding.profileNerHeader.profileAppBar.profileThreeDots.visibility = View.GONE
                viewBinding.profileAppBar.profileThreeDots.visibility = View.GONE
            } else {
                viewBinding.profilePersonalDetails.profileThreeDots.setOnClickListener(this)
            }
            viewBinding.profileDetailCreatePost.show()
            viewBinding.profileDetailCreatePost.setOnClickListener(this)

            profileNerHeader.followingLayout.setOnClickListener(this)
            profileNerHeader.followersLayout.setOnClickListener(this)
            viewBinding.executePendingBindings()
        }
    }

    private fun addImageDrawable(textView:NHTextView,text:String) {

        val ssb = SpannableStringBuilder(textView.text)

        val drawable = ContextCompat.getDrawable(this, R.drawable.profile_vertical_break) ?: return
        drawable.mutate()
        drawable.setBounds(0, 0,
            drawable.intrinsicWidth,
            drawable.intrinsicHeight)
        var index: Int = textView.text.indexOf(text)
        while (index >= 0) {
            ssb.setSpan(ImageSpan(drawable), index, index+text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            textView.setText(ssb, TextView.BufferType.SPANNABLE)
            index = textView.text.indexOf(text, index + 1)
        }
    }

    private fun updateTextForView(view:View, text:String?, clickable:FollowClickables?, parentLayout:View?) {
        text ?: return
        if(clickable == FollowClickables.FOLLOWING && renderedUserProfile?.isNerProfile==true) {
            parentLayout?.visibility = View.GONE
            return
        }
        parentLayout?.let {
            it.visibility = View.VISIBLE
            clickable?.let { cl ->
                it.tag = cl
            }
        }
        (view as? NHTextView)?.let {
            it.text = text
        }
    }

    private fun handleFollowViewClick(clickable: FollowClickables) {
        when (clickable) {
            FollowClickables.FOLLOWING -> {
                launchFollowersActivity(false)
            }
            FollowClickables.FOLLOWERS -> {
                launchFollowersActivity(true)
            }
        }
    }

    private fun showProfile(profile: UserProfile?) {
        profile?.apply {
            if (validateResponse(this)) {
                hideError()
                hideShimmer()
                if (renderedUserProfile != this) {
                    Logger.d(LOG_TAG, "Received valid UserProfile response")
                    renderedUserProfile = this
                    computeViewState(this)
                    if (viewState.isFPV()) {
                        profileViewModel.syncApprovalCounts(profile.userId)
                    }
                    showOptionalTopbarIcons()
                    setupProfileDetails()
                    setupTabs()
                    handler.postDelayed({
                        editProfileTooltip()
                    }, NewsConstants.TOOL_TIP_DELAY)
                    showSignInPopupIfNeeded()
                    logProfileViewedEvent(this, isSocialLogin, myUserId.userId, pageReferrer,
                            NhAnalyticsEventSection.PROFILE, viewState.isFPV(), referrer_raw, autoFollowFromNotification)
                } else {
                    Logger.d(LOG_TAG, "No need to update the UI since this profile is already rendered")
                }
            } else {
                Logger.e(LOG_TAG, "Ignored stale UserProfile response")
            }
        }
    }

    private fun showError(throwable: Throwable?) {
        hideShimmer()
        if (throwable is BaseError) {
            Logger.d(LOG_TAG, "showing error for ${throwable.message}")
            viewBinding.errorParent.visibility = View.VISIBLE
            errorMessageBuilder = ErrorMessageBuilder(viewBinding.errorParent, this, object : ErrorMessageBuilder.ErrorMessageClickedListener {
                override fun onRetryClicked(view: View?) {
                    Logger.d(LOG_TAG, "Retrying to fetch userProfile")
                    fetchUserProfile()
                }

                override fun onNoContentClicked(view: View?) {
                    Logger.d(LOG_TAG, "Navigating back to news home")
                    launchNewsHome()
                }
            })
            errorMessageBuilder!!.showError(throwable)
            enableScrolling(false)
        }
    }

    private fun readUserProfileFromIntent(intent: Intent?) {
        intent?.let {
            val userProfileExtra = it.getSerializableExtra(PROFILE_USER_DATA_KEY) as? UserBaseProfile?
            userProfileExtra?.apply {
                computeViewState(this)
                requestedProfile = this
            }
            preferredProfileLandingType = it.getSerializableExtra(BUNDLE_PROFILE_PREFERRED_TAB_TYPE)
                    as? ProfileTabType
            preferredProfileDefaultTabId = it.getStringExtra(BUNDLE_DEFAULT_PREFERRED_TAB_ID)
        }
    }

    private fun fetchUserProfile() {
        if (!::profileViewModel.isInitialized) return
        requestedProfile?.let { reqProfile ->
            Logger.d(LOG_TAG, "fetching userProfile")
            showShimmer()
            showOptionalTopbarIcons()

            profileViewModel.fetchProfile(reqProfile.userId,
                    reqProfile.handle,
                    appLanguage,
                    reqProfile.profileImage,
                    reqProfile.name,
                    myUserId)
        }
    }

    /**
     * First check if default tab info is present from deeplink
     * If not then check if default tab info is present from profile response.
     * If not then will default to 0th tab position.
     */
    private fun setDefaultTab(defaultProfileId:String?, profileTabType: ProfileTabType?) {
        var defaultTabIndex = 0
        defaultProfileId?.let {
            defaultTabIndex = interactionsAdapter.getIndexForTabId(it)
        }
        if (profileTabType != null && defaultTabIndex == 0) {
            defaultTabIndex = interactionsAdapter.getIndexForTabType(profileTabType)
        }
        if (defaultTabIndex >= 0) {
            viewBinding.profileTabs.interactionViewPager.setCurrentItem(defaultTabIndex, false)
        }
        if (defaultTabIndex >= 0 && viewBinding.profileTabs.interactionViewPager.currentItem == defaultTabIndex) {
            /*If page index is same than onPageSelected will not be called for
            setCurrentItem(int,boolean)*/
            onPageSelected(defaultTabIndex)
        }

    }

    private fun showProfileOptionsMenu() {
        renderedUserProfile?.let { renderedProfile ->
            val menuList = ArrayList<SimpleOptionItem>()
            if (viewState.isFPV()) {
                menuList.add(SimpleOptionItem(R.drawable.ic_edit_profile, CommonUtils.getString(R.string.edit_profile), ProfileMenuOptions.EDIT_PROFILE))
            }

            renderedProfile.profileShareUrl?.let {
                menuList.add(SimpleOptionItem(R.drawable.ic_share_profile, CommonUtils.getString(R.string.share_profile), ProfileMenuOptions.SHARE_PROFILE))
                menuList.add(SimpleOptionItem(R.drawable.ic_copy_link, CommonUtils.getString(R.string.copy_profile), ProfileMenuOptions.COPY_PROFILE_LINK))
            }

            if(viewState.isTPV()) {
                menuList.add(SimpleOptionItem(R.drawable.ic_report_icon, CommonUtils.getString(R.string.report_profile), ProfileMenuOptions.REPORT_PROFILE))
                if (blockUnblockText == CommonUtils.getString(R.string.block_text))
                    menuList.add(SimpleOptionItem(R.drawable.profile_block_icon, blockUnblockText, ProfileMenuOptions.BLOCK_PROFILE))
                else
                    menuList.add(SimpleOptionItem(R.drawable.profile_unblock_icon, blockUnblockText, ProfileMenuOptions.BLOCK_PROFILE))
            }

            if (menuList.isNotEmpty()) {
                val menuOptions = SimpleOptions(menuList, activityID)
                supportFragmentManager?.let {
                    OptionsBottomSheetFragment.newInstance(menuOptions).show(it, "ProfileOptionsMenu")
                    logProfile3DotsMenuViewedEvent(viewState.isFPV(), PageReferrer(ProfileReferrer.PROFILE))
                }
            }
        }
    }

    private fun computeViewState(profile: UserBaseProfile) {
        viewState = if (CommonUtils.isEmpty(myUserId.userId) || (CommonUtils.isEmpty(profile.userId) &&
                        CommonUtils.isEmpty(profile.handle))) {
            ProfileViewState.FPV_USER
        } else if (CommonUtils.equals(profile.userId, myUserId.userId)) {
            if (profile.isCreator()) ProfileViewState.FPV_CREATOR else ProfileViewState.FPV_USER
        } else {
            if (profile.isCreator()) ProfileViewState.TPV_CREATOR else ProfileViewState.TPV_USER
        }
        Logger.d(LOG_TAG, "Switching to viewState: $viewState")
    }

    private fun hideError() {
        errorMessageBuilder?.hideError()
        viewBinding.errorParent.visibility = View.GONE
        errorMessageBuilder = null
        enableScrolling(true)
    }

    override fun observeSessionChanges() {
        SSO.getInstance().userDetailsLiveData.observe(this, Observer {
            if (mandateGuestLogin()) {
                if (!CommonUtils.isEmpty(myUserId.userId) && myUserId.userId != it.userID) {
                    if (!isSocialLogin && viewState.isFPV() && isWaitingFPVLogin) {
                        Logger.d(LOG_TAG, "FPV login, finish since a new instance will be fired")
                        finish()
                        return@Observer
                    }
                    onSignOnDismissed()
                    recreate()
                    Logger.d(LOG_TAG, "userId changed, restarting the activity")
                    return@Observer
                }
                myUserId = ProfileUserIdInfo(it.userLoginResponse?.userId ?: Constants.EMPTY_STRING,
                        it.userLoginResponse?.handle ?: Constants.EMPTY_STRING)
                isSocialLogin = (it.loginType != LoginType.NONE && it.loginType != LoginType.GUEST)
                if (requestedProfile == null) {
                    readUserProfileFromIntent(intent)
                }
                when (viewState) {
                    ProfileViewState.NONE,
                    ProfileViewState.FPV_USER,
                    ProfileViewState.FPV_CREATOR -> {
                        (SSO.getInstance().userDetails?.userLoginResponse)?.apply {
                            requestedProfile = this
                        }
                    }
                    else -> {
                    }
                }

                requestedProfile?.apply {
                    computeViewState(this)
                    hideError()
                    fetchUserProfile()
                    return@Observer
                }
                showError(BaseErrorBuilder.getBaseError(null))
            }
        })
    }

    override fun showLoginError() {
        val isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false)
        hideShimmer()
        viewBinding.errorParent.visibility = View.VISIBLE
        if (!isRegistered) {
            showRegErrorScreen()
        } else {
            Logger.e(LOG_TAG, "NO GUEST SESSION, SHOWING ERROR TO RETRY GUEST LOGIN")
            errorMessageBuilder = ErrorMessageBuilder(viewBinding.errorParent, this, object : ErrorMessageBuilder.ErrorMessageClickedListener {
                override fun onRetryClicked(view: View?) {
                    if (DailyhuntUtils.isRegisterOrFirstHandshakeDoneInThisVersion()) {
                        retryGuestLogin()
                    }
                }
                override fun onNoContentClicked(view: View?) {
                }
            })
            errorMessageBuilder!!.showError(BaseError(CommonUtils.getString(com.newshunt.dhutil.R.string
                    .error_generic)), true)
            enableScrolling(false)
        }
    }

    fun showRegErrorScreen() {
        Logger.e(LOG_TAG, "REGISTRATION FAILED SHOW FULL PAGE ERROR   ")
        errorMessageBuilder = ErrorMessageBuilder(viewBinding.errorParent, this, object :
                ErrorMessageBuilder.ErrorMessageClickedListener {
            override fun onRetryClicked(view: View?) {
                //retry register
                Logger.e(LOG_TAG, "RETRY REGISTRATION")
                AppRegistrationHandler.getInstance().performRegistration(true)
            }

            override fun onNoContentClicked(view: View?) {

            }

        })
        val resId = ThemeUtils.getThemeDrawableByAttribute(this, R.attr.connection_error, View.NO_ID)
        errorMessageBuilder!!.showCustomError(BaseError(CommonUtils.getString(com.newshunt.dhutil.R.string
                .error_syncing)), true,resId)
        enableScrolling(false)

    }

    @Subscribe
    fun onRegistrationUpdate(registrationUpdate: RegistrationUpdate) {
        if (RegistrationState.REGISTERED
                        .equals(registrationUpdate.getRegistrationState())) {
            if (!SSO.getInstance().isLoggedIn(false) || CommonUtils.isEmpty(SSO.getInstance()
                            .userDetails?.userLoginResponse?.userId)) {
                Logger.e(LOG_TAG, "REGISTRATION SUCCESS")
                retryGuestLogin()
            }
        } else {
            Logger.e(LOG_TAG, "REGISTRATION FAILED")
            mandateGuestLogin()
        }
    }


    fun retryGuestLogin() {
        Logger.d(LOG_TAG, "Retry guest login")
        SSO.getInstance().login(this@ProfileActivity, LoginMode.BACKGROUND_ONLY, SSOLoginSourceType.PROFILE_HOME)
        hideError()
        showShimmer()
    }

    fun getRenderedProfile(): UserProfile? {
        return renderedUserProfile
    }

    override fun getDetailFragmentHostId(): Int {
        return R.id.detail_fragment_holder
    }

    private fun observeFragmentCommunications() {
        ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java).fragmentCommunicationLiveData.observe(this, Observer {
            if (it.hostId != activityID || it.anyEnum !is ProfileMenuOptions) {
                return@Observer
            }
            when (it.anyEnum) {
                ProfileMenuOptions.EDIT_PROFILE -> {
                    launchEditProfileActivity()
                }
                ProfileMenuOptions.COPY_PROFILE_LINK -> {
                    copyProfileToClipboard()
                }
                ProfileMenuOptions.SHARE_PROFILE -> {
                    shareProfile()
                }
                ProfileMenuOptions.REPORT_PROFILE -> {
                    reportProfile()
                }
                ProfileMenuOptions.BLOCK_PROFILE ->{
                   renderedUserProfile?.let {it1->
                       profileViewModel.toggleBlock(it1)
                   }
                }
            }
        })
    }

    private fun launchFollowersActivity(followers: Boolean) {
        renderedUserProfile?.userId ?: return
        val count = if (followers) renderedUserProfile?.followersCount else renderedUserProfile?.followingCount
        val userId = if (!followers && viewState.isFPV()) null else renderedUserProfile?.userId
        val followIntent = Intent(this, UserFollowActivity::class.java)
        followIntent.putExtra(Constants.BUNDLE_USER_ID, renderedUserProfile?.userId)
        followIntent.putExtra(Constants.BUNDLE_USER_NAME, renderedUserProfile?.name)
        followIntent.putExtra(Constants.BUNDLE_IS_FPV, viewState.isFPV())
        followIntent.putExtra(Constants.BUNDLE_FOLLOW_MODEL, if (followers) FollowModel.FOLLOWERS.name else FollowModel.FOLLOWING.name)
        followIntent.putExtra(Constants.BUNDLE_SHOW_COUNT, count)
        followIntent.putExtra(BUNDLE_ACTIVITY_REFERRER, PageReferrer(ProfileReferrer.PROFILE))
        followIntent.putExtra(NewsConstants.DH_SECTION, PageSection.PROFILE.section)
        val reqCode = if (followers) REQ_CODE_FOLLOWERS_ACTIVITY else REQ_CODE_FOLLOWING_ACTIVITY
        startActivityForResult(followIntent, reqCode)
    }


    private fun launchEditProfileActivity() {
        val intent = Intent(DHConstants.PROFILE_EDIT_ACTION)
        intent.setPackage(AppConfig.getInstance()!!.packageName)
        intent.putExtra(Constants.BUNDLE_MY_PROFILE, renderedUserProfile)
        intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, PageReferrer(ProfileReferrer.PROFILE))
        this.startActivityForResult(intent, REQ_CODE_EDIT_PROFILE_ACTIVITY)
        logProfile3DotsMenuActionEvent(viewState.isFPV(), PageReferrer(ProfileReferrer.PROFILE), ProfileMenuOptions.EDIT_PROFILE.toString())
    }

    private fun showShimmer() {
        viewBinding.profileShimmer.profileShimmerContainer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        viewBinding.profileShimmer.profileShimmerContainer.visibility = View.GONE
    }

    private fun validateResponse(response: UserProfile?): Boolean {
        response ?: return false
        requestedProfile ?: return false
        var retValue = true
        requestedProfile?.let { reqProfile ->
            if (reqProfile.userId.isNotEmpty()) {
                retValue = reqProfile.userId == response.userId
                Logger.d(LOG_TAG, "validateResponse: request and response userIds match: $retValue")
                return retValue
            }
        }
        return retValue
    }

    private fun followTooltip(eventsInfo: EventsInfo) {
        if (viewState.isTPV() && !renderedUserProfile?.isPrivateProfile()!!) {
            nudgeTooltipWrapper = NudgeTooltipWrapper()
            val text = eventsInfo.activity?.attributes?.get("text") ?: Constants.EMPTY_STRING
            val time = eventsInfo.activity?.attributes?.get("tooltipDurationSec") ?: "10"
            val view = viewBinding.root.findViewById<ConstraintLayout>(R.id.follow_profile)
            nudgeTooltipWrapper.showFollowTooltip(this, R.layout.nudge_tooltip_follow_middle_arrow,
                    text, time.toLong(), view)
            nudgeVM.nudgeShown(eventsInfo.id)
        }
    }

    private fun editProfileTooltip() {
        if (PreferenceManager.getPreference(AppStatePreference.EDIT_PROFILE_TOOL_TIP, false) ||
                !viewState.isFPV() || !isSocialLogin) {
            return
        }
        if (UserPreferenceUtil.isUserNaviLangRtl()) {
            editProfileToolTip = ProfileToolTipWrapper(this, R.layout.view_edit_profile_tooltip_urdu)
        } else {
            editProfileToolTip = ProfileToolTipWrapper(this, R.layout.view_edit_profile_tooltip)
        }

        val title = CommonUtils.getString(R.string.edit_profile)
        val message = CommonUtils.getString(R.string.edit_profile_tooltip_message)

        if (title != null && message != null) {
            editProfileToolTip.showProfileTooltip(title.toString(), message.toString(), 10, viewBinding.profilePersonalDetails
                    .profileThreeDots, viewBinding.profilePersonalDetails.threeDotsBackground)
            PreferenceManager.savePreference(AppStatePreference.EDIT_PROFILE_TOOL_TIP, true)
        }

    }

    private fun reportProfile() {
        val menuPayload = renderedUserProfile.toMenuPayload(renderedUserProfile)
        val intent = NhBrowserNavigator.getTargetIntent().apply {
            putExtra(DailyhuntConstants.MENU_PAYLOAD, menuPayload)
            putExtra(DailyhuntConstants.URL_STR, NewsBaseUrlContainer.getReportProfileUrl())
            putExtra(Constants.VALIDATE_DEEPLINK, false)
        }
        NavigationHelper.navigationLiveData.value = NavigationEvent(intent)
        logProfile3DotsMenuActionEvent(viewState.isFPV(), PageReferrer(ProfileReferrer.PROFILE), ProfileMenuOptions.REPORT_PROFILE.toString())
    }

    private fun UserProfile?.toMenuPayload(userInfo: UserProfile?): MenuPayLoad2? {
        val map = HashMap<String, Any?>()

        try {
            map[NhAnalyticsAppEventParam.CLIENT_ID.getName()] = ClientInfoHelper.getClientId()
            map[AnalyticsParam.ITEM_ID.getName()] = userInfo?.userId
            map[com.dailyhunt.huntlytics.sdk.Constants.ATTR_PROPERTY_USER_APP_VERSION] = ClientInfoHelper.getClientInfo().appVersion
            map[NhAnalyticsAppEventParam.USER_CONNECTION.getName()] = NetworkSDKUtils
                    .getLastKnownConnectionType()
            map[NhAnalyticsAppEventParam.EVENT_ATTRIBUTION.getName()] = NhAnalyticsAppState.getInstance().eventAttribution.referrerName
            map[NhAnalyticsAppEventParam.SESSION_SOURCE.getName()] = NhAnalyticsAppState.getInstance().sessionSource.referrerName
            map[NhAnalyticsAppEventParam.REFERRER.getName()] = pageReferrer?.referrer?.referrerName
            map[NhAnalyticsAppEventParam.REFERRER_ID.getName()] = pageReferrer?.id
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }

        val params = JsonUtils.toJson(map)
        return MenuPayLoad2(userId = userInfo?.userId, option = MenuL1Id.L1_REPORT.name, eventParam = params)
    }


    private fun shareProfile() {
        renderedUserProfile?.let { renderedProfile ->
            val title = when (viewState) {
                ProfileViewState.NONE -> {
                    Constants.EMPTY_STRING
                }
                ProfileViewState.FPV_USER,
                ProfileViewState.FPV_CREATOR -> {
                    CommonUtils.getString(R.string.share_my_profile)
                }
                ProfileViewState.TPV_USER,
                ProfileViewState.TPV_CREATOR -> {
                    String.format(CommonUtils.getString(R.string.share_tpv_profile, renderedProfile.name))
                }
            }
            startActivity(StoryShareUtil.getShareIntent(renderedProfile.profileShareUrl, title))
            logProfile3DotsMenuActionEvent(viewState.isFPV(), PageReferrer(ProfileReferrer.PROFILE), ProfileMenuOptions.SHARE_PROFILE.toString())
        }
    }

    private fun copyProfileToClipboard() {
        renderedUserProfile?.let { renderedProfile ->
            (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?)?.let {
                it.setPrimaryClip(ClipData.newPlainText(ProfileMenuOptions.SHARE_PROFILE.toString(), renderedProfile.profileShareUrl))
                FontHelper.showCustomFontToast(this, CommonUtils.getString(R.string.copy_to_clipboard), Toast.LENGTH_LONG)
                logProfile3DotsMenuActionEvent(viewState.isFPV(), PageReferrer(ProfileReferrer.PROFILE), ProfileMenuOptions.COPY_PROFILE_LINK.toString())
            }
        }
    }

    private fun enableScrolling(enable: Boolean) {
        //While showing the error parent, block scrolling of the toolbar. Else it will scroll behind the error view
        (viewBinding.profileToolbarLayout.layoutParams as? AppBarLayout.LayoutParams?)?.let {
            it.scrollFlags = if (enable) {
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
            } else {
                0
            }
            viewBinding.profileToolbarLayout.layoutParams = it
        }
    }

    private fun showOptionalTopbarIcons() {
        val visibility = if (viewState.isFPV()) View.VISIBLE else View.GONE
        if(viewState.isTPV() || SSO.getInstance().isLoggedIn(false) ) {
            viewBinding.profileNerHeader.profileAppBar.settingsIcon.visibility = visibility
            viewBinding.profileNerHeader.profileAppBar.nhNotificationIcon.visibility = visibility
            return
        }
        viewBinding.profileAppBar.settingsIcon.visibility = visibility
        viewBinding.profileAppBar.nhNotificationIcon.visibility = visibility
    }

    private fun handleSettingsClick() {
        settingsClick()
        if (!CommonNavigator.launchSplashIfFirstLaunch(this)) {
            CommonNavigator.launchSettingsActivity(this)
        }
    }

    private fun extractReferrerFromBundle(bundle: Bundle?): PageReferrer? {
        return bundle?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
                ?: PageReferrer(NhGenericReferrer.ORGANIC, null, null, null,
                        ProfileReferrerSource.PROFILE_HOME_VIEW)
    }

    private fun setupFollowingView(followView: View,
                                   spannableStr: Spannable?,
                                   originalStr: String?,
                                   clickableTag: FollowClickables?) {
        clickableTag?.let {
            followView.tag = it
            (followView as NHTextView).setSpannableText(spannableStr, originalStr)
            return
        }
        followView.visibility = View.GONE
    }

    private fun isDividerBelowPhotoNeeded(): Boolean {
        return renderedUserProfile?.let { userProfile ->
            (!userProfile.bio.isNullOrEmpty() ||
                    !userProfile.uiLocation?.name.isNullOrEmpty() ||
                    shouldShowApprovalCard()) &&
                    !(userProfile.isPrivateProfile() && viewState.isTPV())
        } ?: false
    }

    private fun setupFollowView(renderedProfile: UserProfile) {

        viewBinding.profileNerHeader.followContainer.visibility = View.VISIBLE
        viewBinding.profileNerHeader.followButton.setOnClickListener (this)

        profileViewModel.fetchFollowState(renderedProfile)
        profileViewModel.fetchBlockState(renderedProfile)
        profileViewModel.profileFollowLiveData.observe(this, Observer {
            if (it.isSuccess) {
                val actionableEntity = it.getOrNull()
                if (actionableEntity.isNullOrEmpty()) {
                    updateFollowState(FollowActionType.UNFOLLOW)
                    return@Observer
                } else {
                    updateFollowState(actionableEntity[0]?.action)
                }
            }
        })

        renderedUserProfile?.let {it1->
            profileViewModel.blockLiveData.observe(this){
                if (it.isSuccess){
                    if (it.getOrNull() != null && it.getOrNull() == it1.userId) {
                        blockUnblockText = CommonUtils.getString(R.string.action_unblock)
                    }
                }
                else {
                    blockUnblockText = CommonUtils.getString(R.string.block_text)
                }
            }
        }
    }

    private fun updateFollowState(followAction: FollowActionType?) {
        renderedUserProfile?.let { renderedProfile ->
            renderedProfile.isFollowing = followAction == FollowActionType.FOLLOW
            viewBinding.profilePersonalDetails.followProfile.profile = renderedProfile
            viewBinding.profileNerHeader.profile = renderedProfile
            Logger.d(LOG_TAG, "updateFollowState for ${renderedProfile.name}, isFollowing = ${renderedProfile.isFollowing}")
            if (autoFollowFromNotification && !renderedProfile.isFollowing) {
                profileViewModel.toggleFollow(renderedProfile)
                autoFollowFromNotification = false
                Logger.d(LOG_TAG, "Triggering auto follow for ${renderedProfile.name}")
            }
            viewBinding.executePendingBindings()
            observeNudges()
        }
    }

    private fun shouldShowApprovalCard(): Boolean {
        return CardsBindUtils.getApprovalCardVisibility(viewState, approvalCounts) == View.VISIBLE
    }

    private fun observeApprovalCard() {
        profileViewModel.readPendingApprovalLD.observe(this, Observer {
            if (it.isSuccess) {
                it.getOrNull()?.let { pendingApprovalsEntity ->
                    approvalCounts = pendingApprovalsEntity.approvalCounts

                    if(viewState.isTPV()) {
                        viewBinding.profileNerHeader.approvalCounts = approvalCounts
                        viewBinding.profileNerHeader.profileViewState = viewState
                        viewBinding.profileNerHeader.executePendingBindings()
                    } else {
                        viewBinding.profilePersonalDetails.approvalCounts = approvalCounts
                        viewBinding.profilePersonalDetails.profileViewState = viewState
                        viewBinding.profilePersonalDetails.executePendingBindings()
                    }
                }
            }
        })
    }

    private fun isInMyPosts() : Boolean {
        if (!viewState.isFPV() || !::interactionsAdapter.isInitialized) {
            return false
        }
        val indexFpvPosts = interactionsAdapter.getIndexForTabType(ProfileTabType.FPV_POSTS)
        val currentTabIndex = viewBinding.profileTabs.interactionViewPager.currentItem
        return indexFpvPosts == currentTabIndex
    }

    private fun observeProfileLivedata() {
        profileViewModel.userProfileLiveData.observe(this, Observer {
            if (it.isSuccess) {
                showProfile(it.getOrNull())
            } else {
                showError(it.exceptionOrNull())
            }
        })
    }

    private fun showSignInPopupIfNeeded() {
        if (viewState.isTPV() && !isSocialLogin && renderedUserProfile?.isPrivateProfile() == true) {
            viewBinding.signInScrollView.visibility = View.VISIBLE
            if (supportFragmentManager?.findFragmentByTag(TPV_SIGNIN_FRAGMENT_TAG) == null) {
                signInFragmentTag = TPV_SIGNIN_FRAGMENT_TAG
                SSOSignInPopup.showSignInPopup(this, R.id.signinFragment_Parent,
                        signInFragmentTag, SignInUIModes.SIGN_IN_FOR_TPV,
                        renderedUserProfile!!.name, referrer = pageReferrer)
            }
        }
    }
    private fun triggerFPVLogin(referrer: PageReferrer? = PageReferrer(NhGenericReferrer.PROFILE_FPV)) {
        isWaitingFPVLogin = true
        CommonNavigator.launchMyProfileAfterLoginAndImportContacts(this, referrer, DefaultNavigatorCallback())
    }

    override fun onDescriptionExpanded(expanded: Boolean, photoId: String?) {
        val fragment = NERDescriptionBottomSheetFragment.instance(renderedUserProfile?.bioUrl)
        fragment.show(supportFragmentManager,"NERDescriptionBottomSheetFragment")
    }

    override fun isStoryExpanded(photoId: String?): Boolean {
        return false
    }
}

//Enum to differentiate the variations of profile being shown by this Activity instance
enum class ProfileViewState : Serializable {
    NONE,
    FPV_USER,
    FPV_CREATOR,
    TPV_USER,
    TPV_CREATOR;

    fun isFPV() = (this == FPV_CREATOR || this == FPV_USER)

    fun isTPV() = (this == TPV_USER || this == TPV_CREATOR)
}

private enum class FollowClickables {
    FOLLOWING,
    FOLLOWERS,
    BLOCKED
}

private enum class ProfileMenuOptions {
    SHARE_PROFILE,
    COPY_PROFILE_LINK,
    EDIT_PROFILE,
    REPORT_PROFILE,
    BLOCK_PROFILE
}