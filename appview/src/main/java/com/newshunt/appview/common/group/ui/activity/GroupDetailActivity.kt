/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.group.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.analytics.helper.ReferrerProviderHelper
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsFragment
import com.newshunt.appview.common.group.DaggerGroupDetailComponent
import com.newshunt.appview.common.group.GroupBaseModule
import com.newshunt.appview.common.group.buildLocationForGroupDao
import com.newshunt.appview.common.group.viewmodel.GroupDetailVMFactory
import com.newshunt.appview.common.group.viewmodel.GroupViewModel
import com.newshunt.appview.common.postcreation.analytics.helper.CreatePostAnalyticsHelper
import com.newshunt.appview.common.ui.activity.AuthorizationBaseActivity
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.helper.ErrorHelperUtils
import com.newshunt.appview.common.ui.helper.LiveDataEventHelper
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.helper.NewGroupEvent
import com.newshunt.appview.common.ui.helper.SnackbarViewModel
import com.newshunt.appview.databinding.ActivityGroupDetailBinding
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.CommonMessageDialog
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.LocalInfo
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.view.customview.FIT_TYPE
import com.newshunt.dataentity.model.entity.GROUP_INFO_KEY
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.GroupLocations
import com.newshunt.dataentity.model.entity.LEAVE_GROUP
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.model.entity.MemberRole
import com.newshunt.dataentity.model.entity.MembershipStatus
import com.newshunt.dataentity.model.entity.SettingState
import com.newshunt.dataentity.onboarding.RegistrationState
import com.newshunt.dataentity.onboarding.RegistrationUpdate
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SearchSuggestionType
import com.newshunt.dataentity.social.entity.MenuL1Id
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.MenuPayLoad2
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NhBrowserNavigator
import com.newshunt.deeplink.navigator.SSONavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.helper.NewsListCardLayoutUtil
import com.newshunt.news.helper.ProfileToolTipWrapper
import com.newshunt.news.helper.StoryShareUtil
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.profile.OptionsBottomSheetFragment
import com.newshunt.profile.SimpleOptionItem
import com.newshunt.profile.SimpleOptions
import com.newshunt.sdk.network.image.Image
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType
import com.newshunt.sso.view.adapters.DHProfilesAdapter
import com.newshunt.sso.view.adapters.ProfileListItemDecorator
import com.squareup.otto.Subscribe
import java.io.Serializable
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.forEach
import kotlin.collections.get
import kotlin.collections.isNotEmpty
import kotlin.collections.listOf
import kotlin.collections.mapOf
import kotlin.collections.set
import kotlin.collections.take
import kotlin.math.abs

/**
 * Activity to show detailed information of groups
 * <p>
 * Created by srikanth.ramaswamy on 09/18/2019.
 */
private const val LOG_TAG = "GroupsDetailsActivity"
private const val BUNDLE_JOIN_PENDING = "JOIN_PENDING"
private const val BUNDLE_LAUNCH_MEMBERS = "LAUNCH_MEMBERS"
private const val GROUP_DETAIL_FRAGMENT_TAG = "GROUP_POSTS"
private const val MEMBER_PHOTOS_MAX_COUNT = 4

class GroupDetailActivity : AuthorizationBaseActivity(), ReferrerProviderlistener, View.OnClickListener {

    private var requestedGroup: GroupBaseInfo? = null
    private var renderedGroup: GroupInfo? = null
    private var myRole: MemberRole? = MemberRole.NONE

    @Inject
    lateinit var groupViewModelF: GroupDetailVMFactory

    @Inject
    lateinit var snackbarViewModelFactory: SnackbarViewModel.Factory

    private lateinit var groupViewModel: GroupViewModel
    private lateinit var viewBinding: ActivityGroupDetailBinding
    private lateinit var editProfileToolTip: ProfileToolTipWrapper
    private lateinit var videoRequester: VideoRequester

    private var isJoinPending = false
    private var launchMemberList = false
    private val menuOptionsMap = mapOf(Pair(MemberRole.NONE, listOf(GroupsOptions.SHARE, GroupsOptions.CREATE_NEW_GROUP, GroupsOptions.REPORT)),
            Pair(MemberRole.MEMBER, listOf(GroupsOptions.LEAVE, GroupsOptions.INVITE,
                    GroupsOptions.SHARE, GroupsOptions.CREATE_NEW_GROUP, GroupsOptions.REPORT)),
            Pair(MemberRole.ADMIN, listOf(GroupsOptions.SETTINGS, GroupsOptions.INVITE,
                    GroupsOptions.SHARE, GroupsOptions.CREATE_NEW_GROUP, GroupsOptions.REPORT)),
            Pair(MemberRole.OWNER, listOf(GroupsOptions.SETTINGS, GroupsOptions.INVITE,
                    GroupsOptions.SHARE, GroupsOptions.CREATE_NEW_GROUP, GroupsOptions.REPORT)))

    private var type: DialogBoxType? = null
    private val currentReferrer: PageReferrer = PageReferrer(NhGenericReferrer.GROUP_HOME)
    private val section: NhAnalyticsEventSection = NhAnalyticsEventSection.GROUP
    private var referrerRaw:String? = null
    private val referrerProviderHelper = ReferrerProviderHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_group_detail)
        NewsListCardLayoutUtil.manageLayoutDirection(findViewById(R.id.group_detail_rootview))
        DaggerGroupDetailComponent.builder()
                .groupBaseModule(GroupBaseModule(SocialDB.instance()))
                .build()
                .inject(this)
        groupViewModel = ViewModelProviders.of(this, groupViewModelF).get(GroupViewModel::class.java)
        viewBinding.actionbarBackWhite.setOnClickListener(this)
        viewBinding.actionbarBackBlack.setOnClickListener(this)
        isJoinPending = needGroupAutoJoining(savedInstanceState)
        launchMemberList = savedInstanceState?.getBoolean(BUNDLE_LAUNCH_MEMBERS, false) ?: false
        pageReferrer = intent.extras?.get(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        referrerRaw = intent.extras?.getString(Constants.REFERRER_RAW)
        referrerProviderHelper.addReferrerByProvider(pageReferrer)
        videoRequester = VideoRequester(activityID)
        viewBinding.grpPostsOverlay.setOnTouchListener { _, _ ->
            true
        }
        observeGroupInfo()
        observeFragmentCommunications()
        ViewModelProviders.of(this, snackbarViewModelFactory).get(SnackbarViewModel::class.java)
                .also {
                    it.followChanges.observe(this, Observer {
                        SnackbarViewModel.onFollowChangeEvent(it, viewBinding.root)
                    })
                    it.newPostChanges.observe(this, Observer { res ->
                        SnackbarViewModel.onPostUploaded(res, viewBinding.root, true,
                                snackbarStringId = if (renderedGroup?.postApproval == SettingState.REQUIRED)
                                    R.string.group_post_approval
                                else
                                    null
                        )
                    })
                    it.start()
                }
    }

    override fun getLogTag() = LOG_TAG

    override fun onStart() {
        super.onStart()
        BusProvider.getUIBusInstance().register(this)
    }

    override fun onStop() {
        super.onStop()
        BusProvider.getUIBusInstance().unregister(this)
    }

    override fun showLoginError() {
        hideError()
        hideShimmer()
        Logger.e(LOG_TAG, "NO GUEST SESSION, SHOWING ERROR TO RETRY GUEST LOGIN")
        viewBinding.errorParent.root.visibility = View.VISIBLE
        viewBinding.errorParent.baseError = BaseError(CommonUtils.getString(com.newshunt.dhutil.R.string.error_generic))
        viewBinding.errorParent.vm = groupViewModel
        enableScrolling(false)
    }

    @Subscribe
    fun onRegistrationUpdate(registrationUpdate: RegistrationUpdate) {
        if (RegistrationState.REGISTERED
                        .equals(registrationUpdate.getRegistrationState())) {
            if (!SSO.getInstance().isLoggedIn(false) || CommonUtils.isEmpty(SSO.getInstance()
                            .userDetails?.userLoginResponse?.userId)) {
                groupViewModel.retryGuestLogin(this)
            }
        } else {
            mandateGuestLogin()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.joinGroup -> {
                joinGroup()
                AnalyticsHelper2.logJoinGroupClickEVent(currentReferrer,
                        NewsExploreButtonType.JOIN_GROUP, renderedGroup)
            }
            R.id.groups_detail_3dots_black, R.id.groups_detail_3dots_white -> {
                showGroupMenuOptions()
                DialogAnalyticsHelper.logDialogBoxViewedEvent(null, currentReferrer, section,
                        myRole)
            }
            R.id.member_clickZone -> {
                launchMemberList()
            }
            R.id.invite_people_btn -> {
                launchGroupInvitationActivity()
            }
            R.id.actionbar_back_black, R.id.actionbar_back_white -> {
                handleBack(false)
            }
            R.id.group_detail_create_post -> {
                val pageReferrer = PageReferrer(NhGenericReferrer.GROUP_HOME, renderedGroup?.id)
                CreatePostAnalyticsHelper.logCreatePostClickEvent(pageReferrer)
                val intent = CommonNavigator.getPostCreationIntent(
                        null, null, SearchSuggestionItem(
                        itemId = renderedGroup?.id ?: "",
                        suggestion = renderedGroup?.handle ?: "",
                        name = renderedGroup?.name ?: renderedGroup?.handle,
                        typeName = SearchSuggestionType.HANDLE.type
                ), pageReferrer,
                        LocalInfo(
                                pageId = buildLocationForGroupDao(GroupLocations.G_D, renderedGroup?.id
                                        ?: ""),
                                section = PageSection.GROUP.section,
                                creationDate = System.currentTimeMillis(),
                                isCreatedFromOpenGroup = true
                        ), null, null, null, renderedGroup)
                startActivity(intent)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BUNDLE_JOIN_PENDING, isJoinPending)
        outState.putBoolean(BUNDLE_LAUNCH_MEMBERS, launchMemberList)
        super.onSaveInstanceState(outState)
    }

    override fun onUserIdAvailable() {
        if (requestedGroup == null) {
            readGroupInfoFromIntent(intent)
        }

        requestedGroup?.apply {
            hideError()
            fetchGroupInfo()
            return
        }
        showError(BaseErrorBuilder.getBaseError(null))
    }

    override fun getDetailFragmentHostId(): Int {
        return R.id.detail_fragment_holder
    }

    override fun getReferrerEventSection(): NhAnalyticsEventSection {
        return NhAnalyticsEventSection.GROUP
    }

    override fun getProvidedReferrer(): PageReferrer? {
        return referrerProviderHelper.providedPageReferrer
    }

    override fun getLatestPageReferrer(): PageReferrer? {
        return referrerProviderHelper.referrerQueue.yongest
    }

    override fun onBackPressed() {
        handleBack(true,referrerRaw)
    }

    private fun observeGroupInfo() {
        //Always observe the live data coming from DB
        groupViewModel.readGroupMediatorLD.observe(this, Observer { result ->
            if (result.isSuccess) {
                showGroupInfo(result.getOrNull())
            } else {
                showError(result.exceptionOrNull())
            }
        })

        groupViewModel.fetchGroupLiveData.observe(this, Observer { result ->
            //If network call fails and we already have data from DB, dont show error. Else show error
            if (result.isFailure) {
                renderedGroup?.let {
                    Logger.e(LOG_TAG, "Network call failed, showing group info from cache. Might be stale?")
                } ?: showError(result.exceptionOrNull())
            }
        })

        groupViewModel.readPendingApprovalsLD.observe(this, Observer { result ->
            if (result.isSuccess) {
                result.getOrNull()?.let { pendingApprovalsEntity ->
                    viewBinding.commonGroupInfoCard.approvalCard.approvalCounts = pendingApprovalsEntity.approvalCounts
                    viewBinding.commonGroupInfoCard.approvalCard.vm = groupViewModel
                    viewBinding.commonGroupInfoCard.approvalCard.groupInfo = renderedGroup
                    viewBinding.commonGroupInfoCard.approvalCard.executePendingBindings()
                }
            }
        })

        groupViewModel.fetchStatusLD.observe(this, Observer {
            if (it && renderedGroup == null) {
                showShimmer()
            }
        })
    }

    private fun fetchGroupInfo() {
        if (!::groupViewModel.isInitialized) return
        requestedGroup?.let { grp ->
            myUserId.userId?.let { userId ->
                if (grp.id.isNotEmpty()) {
                    Logger.d(LOG_TAG, "fetching group info with id: ${grp.id}")
                    groupViewModel.fetchGroupInfo(grp.id, userId)
                    return
                }
                grp.handle?.let { handle ->
                    Logger.d(LOG_TAG, "fetching group info with handle: ${grp.handle}")
                    groupViewModel.fetchGroupInfoWithHandle(handle, userId)
                }
            }
        }
    }

    private fun readGroupInfoFromIntent(intent: Intent?) {
        intent?.let {
            val groupInfoExtra = it.getSerializableExtra(GROUP_INFO_KEY) as? GroupBaseInfo?
            groupInfoExtra?.apply {
                requestedGroup = this
            }
        }
    }

    private fun setupGroupInfo() {
        renderedGroup?.let { groupInfo ->
            groupInfo.coverImage?.let {
                viewBinding.root.findViewById<NHImageView>(R.id.group_detail_image).setFitType(FIT_TYPE.TOP_CROP)
                Image.load(ImageUrlReplacer.getQualifiedImageUrl(it,
                        CommonUtils.getDeviceScreenWidthInDp(), Constants.IMAGE_ASPECT_RATIO_16_9))
                        .into(viewBinding.commonGroupInfoCard.groupDetailImage)
            }

            if (groupInfo.coverImage.isNullOrEmpty()) {
                viewBinding.commonGroupInfoCard.groupDetailImage.setImageResource(R.drawable.ic_group_image)
            }

            viewBinding.setVariable(BR.item, groupInfo)
            viewBinding.setVariable(BR.vm, groupViewModel)
            viewBinding.invalidateAll()

            viewBinding.commonGroupInfoCard.joinGroup.setOnClickListener(this)
            viewBinding.commonGroupInfoCard.invitePeopleBtn.setOnClickListener(this)
            viewBinding.groupsDetail3dotsBlack.setOnClickListener(this)
            viewBinding.groupsDetail3dotsWhite.setOnClickListener(this)
            viewBinding.groupDetailCreatePost.setOnClickListener(this)
            viewBinding.commonGroupInfoCard.groupDetailDescription.setText(groupInfo.description, false, null)

            setSupportActionBar(viewBinding.groupDetailToolbar)
            viewBinding.appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

                override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                    if (abs(verticalOffset) > ((0.8) * appBarLayout.totalScrollRange)) {
                        //when toolbar is in collapsed mode
                        viewBinding.actionbarTitle.text = renderedGroup?.name.toString()
                        viewBinding.actionbarTitle.animate()
                                .alpha(1f)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        if (!ThemeUtils.isNightMode()) {
                                            groupViewModel.showActionBar.postValue(true)
                                        }
                                    }
                                })
                    } else {
                        // toolbar is in expanded mode
                        viewBinding.actionbarTitle.animate()
                                .alpha(0f)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        groupViewModel.showActionBar.postValue(false)
                                    }
                                })
                    }
                }
            })
            viewBinding.lifecycleOwner = this
            enableScrolling(!CardsBindUtils.canShowGroupFeedOverlap(renderedGroup))
            if (groupInfo.membership == MembershipStatus.BLOCKED) {
                GenericCustomSnackBar.showSnackBar(viewBinding.root,
                        this@GroupDetailActivity,
                        CommonUtils.getString(R.string.group_user_blocked),
                        Snackbar.LENGTH_LONG).show()
            }
            viewBinding.executePendingBindings()
        }
    }

    private fun showGroupInfo(groupInfo: GroupInfo?) {
        groupInfo?.apply {
            if (validateResponse(groupInfo)) {
                Logger.d(LOG_TAG, "Rendering valid group response, role: ${groupInfo.userRole}")
                hideError()
                hideShimmer()
                renderedGroup = this
                myRole = this.userRole
                if (myRole?.isOwnerOrAdmin() == true) {
                    myUserId.userId?.let {
                        if (it.isNotEmpty()) {
                            groupViewModel.syncApprovalCounts(it)
                        }
                    }
                }
                showSettingsTooltip()
                setupGroupInfo()
                loadPostsFragment()
                setupMemberList()
                if (isJoinPending) {
                    Logger.d(LOG_TAG, "Automatically trying to Join this group")
                    joinGroup()
                }
                //If User had not logged and hit on member list, auto launch member list after login
                if (launchMemberList && SSO.getInstance().isLoggedIn(false)) {
                    if (renderedGroup?.userRole?.isMember() == true) {
                        launchMemberList()
                    } else {
                        GenericCustomSnackBar.showSnackBar(viewBinding.root,
                                this@GroupDetailActivity,
                                CommonUtils.getString(R.string.member_list_condition),
                                Snackbar.LENGTH_LONG).show()
                    }
                }
                AnalyticsHelper2.logGroupHomeEvent(pageReferrer, this)
            }
        } ?: Logger.e(LOG_TAG, "Error, no group info to show")
    }

    private fun showSettingsTooltip() {
        if (PreferenceManager.getPreference(AppStatePreference.GROUP_SETTINGS_TOOLTIP_SHOWN, false)
                || myRole == MemberRole.NONE
                || myRole == MemberRole.MEMBER) {
            return
        }
        editProfileToolTip = ProfileToolTipWrapper(this, R.layout.group_settings_tooltip)
        editProfileToolTip.showProfileTooltip(CommonUtils.getString(R.string.group_tooltip_text),
                null, 10, viewBinding.groupsDetail3dotsWhite, null)
        PreferenceManager.savePreference(AppStatePreference.GROUP_SETTINGS_TOOLTIP_SHOWN, true)
    }

    private fun showError(throwable: Throwable?) {
        if (throwable is BaseError) {
            hideShimmer()
            Logger.d(LOG_TAG, "Showing error for ${throwable.message}")
            viewBinding.errorParent.vm = groupViewModel
            viewBinding.errorParent.baseError = throwable
            viewBinding.errorParent.root.visibility = View.VISIBLE
            enableScrolling(false)
        }
    }

    private fun hideError() {
        viewBinding.errorParent.root.visibility = View.GONE
        enableScrolling(true)
    }

    private fun showShimmer() {
        viewBinding.grpDetailShimmer.profileShimmerContainer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        viewBinding.grpDetailShimmer.profileShimmerContainer.visibility = View.GONE
    }

    private fun validateResponse(groupBaseInfo: GroupBaseInfo?): Boolean {
        groupBaseInfo ?: return false
        requestedGroup ?: return false

        var retValue = true
        requestedGroup?.let {
            if (it.id.isNotEmpty()) {
                retValue = (it.id == groupBaseInfo.id && myUserId.userId == groupBaseInfo.userId)
                Logger.d(LOG_TAG, "validateResponse: request and response groupId, userid match: $retValue")
            }
        }
        return retValue
    }

    private fun loadPostsFragment() {
        renderedGroup?.let {
            val layoutId = if (myRole != MemberRole.NONE) {
                R.layout.layout_group_detail_card_invite_people
            } else {
                -1
            }
            val fragment = CardsFragment.create(bundleOf(Constants.PAGE_ID to
                    buildLocationForGroupDao(GroupLocations.G_D, it.id),
                    NewsConstants.DH_SECTION to PageSection.GROUP.section,
                    Constants.BUNDLE_MENU_CLICK_LOCATION to MenuLocation.GROUP_LIST,
                    Constants.BUNDLE_ACTIVITY_REFERRER_FLOW to groupViewModel.referrerFlow,
                    Constants.BUNDLE_ACTIVITY_REFERRER_FLOW_PARENT to intent?.getSerializableExtra(Constants.BUNDLE_ACTIVITY_REFERRER_FLOW),
                    NewsConstants.DH_SECTION to Constants.GROUP,
                    Constants.BUNDLE_GROUP_INFO to it, Constants.BUNDLE_ERROR_LAYOUT_ID to layoutId),
                    null)
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.grp_posts_fragment_holder, fragment, GROUP_DETAIL_FRAGMENT_TAG)
            fragmentTransaction.commit()
            fragment.userVisibleHint = true
        }
    }

    private fun joinGroup() {
        renderedGroup?.let { group ->
            if (!isSocialLogin) {
                isJoinPending = true
                SSONavigator.launchSignInActivity(this, LoginType.NONE, currentReferrer)
                return
            }
            if (myRole == MemberRole.NONE) {
                groupViewModel.joinGroupLiveData.observe(this, Observer {
                    if (it.isFailure) {
                        ErrorHelperUtils.showErrorSnackbar(it.exceptionOrNull(), viewBinding.grpDetailRoot)
                    } else {
                        Logger.d(LOG_TAG, "Successfully joined the group")
                        if (group.memberApproval == SettingState.NOT_REQUIRED) {
                            GenericCustomSnackBar.showSnackBar(viewBinding.root, this, CommonUtils.getString(R.string.group_joined, group.name), Snackbar.LENGTH_LONG).show()
                            LiveDataEventHelper.newGroupLiveData.postValue(NewGroupEvent(System.currentTimeMillis(), group.id))
                        } else {
                            GenericCustomSnackBar.showSnackBar(viewBinding.root, this, CommonUtils.getString(R.string.request_sent, group.name), Snackbar.LENGTH_LONG).show()
                        }
                    }
                })

                myUserId.userId?.let { userid ->
                    groupViewModel.joinGroup(group.id, userid)
                }
            } else {
                Logger.e(LOG_TAG, "Can not join this group, myRole: $myRole")
            }
            isJoinPending = false
        }
    }

    private fun showGroupMenuOptions() {
        renderedGroup?.let { groupInfo ->
            val menuList = ArrayList<SimpleOptionItem>()
            menuOptionsMap[myRole]?.forEach { option ->
                val simpleOption = when (option) {
                    GroupsOptions.SETTINGS -> {
                        SimpleOptionItem(R.drawable.ic_grp_settings_icon,
                                CommonUtils.getString(R.string.action_settings), GroupsOptions.SETTINGS)
                    }
                    GroupsOptions.INVITE -> {
                        SimpleOptionItem(R.drawable.ic_invite_icon, CommonUtils.getString(R
                                .string.invite_btn_text), GroupsOptions.INVITE)
                    }
                    GroupsOptions.SHARE -> {
                        if (groupInfo.shareUrl.isNullOrEmpty()) {
                            null
                        } else {
                            SimpleOptionItem(R.drawable.ic_grp_share_icon, CommonUtils.getString(R
                                    .string.fab_share_text), GroupsOptions.SHARE)
                        }
                    }
                    GroupsOptions.REPORT -> {
                        SimpleOptionItem(R.drawable.ic_report_icon, CommonUtils.getString(R
                                .string.report), GroupsOptions.REPORT)
                    }
                    GroupsOptions.LEAVE -> {
                        SimpleOptionItem(R.drawable.ic_leave_group, CommonUtils.getString(R
                                .string.leave_group), GroupsOptions.LEAVE)
                    }
                    GroupsOptions.CREATE_NEW_GROUP -> {
                        SimpleOptionItem(R.drawable.ic_create_group, CommonUtils.getString(R.string
                                .create_new_group), GroupsOptions.CREATE_NEW_GROUP)
                    }
                }
                simpleOption?.also {
                    menuList.add(it)
                }
            }
            if (menuList.isNotEmpty()) {
                val menuOptions = SimpleOptions(menuList, activityID)
                supportFragmentManager?.let {
                    OptionsBottomSheetFragment.newInstance(menuOptions).show(it, "GroupOptions")
                }
            }
        }
    }

    private fun observeFragmentCommunications() {
        ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java).fragmentCommunicationLiveData.observe(this, Observer {
            if (it.hostId != activityID || (it.anyEnum !is GroupsOptions && it.anyEnum !is CommonMessageEvents)) {
                return@Observer
            }

            var action = DialogAnalyticsHelper.DIALOG_ACTION_OK
            when (it.anyEnum) {
                GroupsOptions.SETTINGS -> {
                    launchGroupSettings()
                    type = DialogBoxType.GROUP_SETTINGS
                }
                GroupsOptions.INVITE -> {
                    launchInvite()
                    type = DialogBoxType.GROUP_INVITE
                }
                GroupsOptions.SHARE -> {
                    shareGroup()
                    type = DialogBoxType.GROUP_SHARE
                }
                GroupsOptions.REPORT -> {
                    reportGroup()
                    type = DialogBoxType.REPORT_GROUP
                }
                GroupsOptions.LEAVE -> {
                    supportFragmentManager?.let {
                        val commonMessageDialogOptions = CommonMessageDialogOptions(
                            this.activityID,
                            CommonUtils.getString(R.string.leave_group),
                            CommonUtils.getString(R.string.leave_group_msg),
                            CommonUtils.getString(R.string.leave_text),
                            CommonUtils.getString(R.string.not_now_text),
                            useCase = LEAVE_GROUP
                        )

                        CommonMessageDialog.newInstance(commonMessageDialogOptions).show(it, "CommonMessageDialog")
                    }
                    type = DialogBoxType.LEAVE_GROUP
                }
                CommonMessageEvents.POSITIVE_CLICK -> {
                    if (it.useCase.equals(LEAVE_GROUP))
                        leaveGroup()
                }
                GroupsOptions.CREATE_NEW_GROUP -> {
                    createGroup()
                    type = DialogBoxType.CREATE_GROUP
                }
                CommonMessageEvents.NEGATIVE_CLICK -> {
                    action = DialogAnalyticsHelper.DIALOG_ACTION_CANCEL
                }
                CommonMessageEvents.DISMISS -> {
                    action = DialogAnalyticsHelper.DIALOG_ACTION_CANCEL
                    type = DialogBoxType.UNQUALIFIED_FEEDBACK
                }
            }
            DialogAnalyticsHelper.logDialogBoxActionEvent(type, currentReferrer, action, section,
                    myRole)
        })
    }

    private fun createGroup() {
        CommonNavigator.launchEditGroupActivity(this, null, SSO.getInstance().isLoggedIn(false), PageReferrer(NhGenericReferrer.DIALOGBOX))
    }

    private fun launchGroupSettings() {
        val intent = Intent(DHConstants.GROUP_SETTINGS_ACTION)
        intent.setPackage(AppConfig.getInstance().packageName)
        intent.putExtra(GROUP_INFO_KEY, renderedGroup)
        startActivity(intent)
    }

    private fun launchInvite() {
        renderedGroup?.let {
            CommonNavigator.launchGroupInvitationActivity(this, it, currentReferrer)
        }
    }

    private fun shareGroup() {
        val title = CommonUtils.getString(R.string.share_group_text, renderedGroup?.name)
        startActivity(StoryShareUtil.getShareIntent(renderedGroup?.shareUrl, title))
    }

    private fun reportGroup() {
        val menuPayload = renderedGroup.toMenuPayload(renderedGroup)
        val intent = NhBrowserNavigator.getTargetIntent().apply {
            putExtra(DailyhuntConstants.MENU_PAYLOAD, menuPayload)
            putExtra(DailyhuntConstants.URL_STR, NewsBaseUrlContainer.getReportGroupUrl())
            putExtra(Constants.VALIDATE_DEEPLINK, false)
        }
        NavigationHelper.navigationLiveData.value = NavigationEvent(intent)
    }

    private fun GroupInfo?.toMenuPayload(groupInfo: GroupInfo?): MenuPayLoad2? {
        val map = HashMap<String, Any?>()
        try {
            map[NhAnalyticsAppEventParam.CLIENT_ID.getName()] = ClientInfoHelper.getClientId()
            map[AnalyticsParam.ITEM_ID.getName()] = groupInfo?.id
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
        return MenuPayLoad2(groupId = groupInfo?.id, option = MenuL1Id.L1_REPORT.name, eventParam = params)
    }

    private fun leaveGroup() {
        renderedGroup?.id?.let { groupId ->
            if (myRole != MemberRole.NONE) {
                groupViewModel.leaveGroupLiveData.observe(this, Observer { result ->
                    if (result.isSuccess) {
                        finishAffinity()
                        CommonNavigator.launchFollowHome(this,
                                false,
                                null,
                                null,
                                currentReferrer)
                    } else if (result.isFailure) {
                        ErrorHelperUtils.showErrorSnackbar(result.exceptionOrNull(), viewBinding.grpDetailRoot)
                    }
                    myRole?.let {
                        AnalyticsHelper2.logGroupSettingsClickEvent(PageReferrer(currentReferrer),
                                renderedGroup, type = Constants.LEAVE_GROUP_CONFIRM, userProfile = it)
                    }
                })
                myUserId.userId?.let { userid ->
                    groupViewModel.leaveGroup(groupId, userid)
                }
            }
        }
    }

    private fun setupMemberList() {
        renderedGroup?.topMembersPhotos?.let { memberList ->
            if (memberList.isNotEmpty()) {
                viewBinding.commonGroupInfoCard.memberProfilesList.layoutManager =
                        LinearLayoutManager(this).apply {
                            this.orientation = RecyclerView.HORIZONTAL
                        }
                viewBinding.commonGroupInfoCard.memberProfilesList.adapter = DHProfilesAdapter(memberList.take(MEMBER_PHOTOS_MAX_COUNT), this)
                viewBinding.commonGroupInfoCard.memberProfilesList.addItemDecoration(ProfileListItemDecorator())
                if (!SSO.getInstance().isLoggedIn(false) || renderedGroup?.userRole?.isMember() == true) {
                    viewBinding.commonGroupInfoCard.memberClickZone.setOnClickListener(this)
                }
            }
        }
    }

    private fun launchMemberList() {
        renderedGroup?.let {
            if (SSO.getInstance().isLoggedIn(false)) {
                CommonNavigator.launchGroupMemberActivity(this, it, currentReferrer)
            } else {
                SSO.getInstance().login(this,
                        LoginMode.USER_EXPLICIT,
                        SSOLoginSourceType.GROUP_SCREENS,
                        this)
                launchMemberList = true
            }
        }
    }

    private fun launchGroupInvitationActivity() {
        renderedGroup?.let {
            CommonNavigator.launchGroupInvitationActivity(this, it, currentReferrer)
        }
    }

    private fun enableScrolling(enable: Boolean) {
        (viewBinding.groupDetailToolbarLayout.layoutParams as? AppBarLayout.LayoutParams?)?.let {
            it.scrollFlags = if (enable) {
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
            } else {
                0
            }
            viewBinding.groupDetailToolbarLayout.layoutParams = it
        }
    }

    private fun needGroupAutoJoining(savedInstanceState: Bundle?): Boolean {
        return (savedInstanceState?.getBoolean(BUNDLE_JOIN_PENDING, false) == true) ||
                intent?.getBooleanExtra(Constants.BUNDLE_AUTO_JOIN_FROM_NOTIFICATION, false)
                ?: false
    }
}

enum class GroupsOptions : Serializable {
    SETTINGS,
    INVITE,
    SHARE,
    REPORT,
    LEAVE,
    CREATE_NEW_GROUP
}