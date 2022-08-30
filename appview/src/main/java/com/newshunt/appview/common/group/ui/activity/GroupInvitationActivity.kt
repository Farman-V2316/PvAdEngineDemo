/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.group.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eterno.BGSyncService
import com.newshunt.analytics.entity.NhAnalyticsDialogEventParam
import com.newshunt.appview.R
import com.newshunt.appview.common.group.DaggerGroupInviteComponent
import com.newshunt.appview.common.group.GroupBaseModule
import com.newshunt.appview.common.group.GroupInvitationModule
import com.newshunt.appview.common.group.buildLocationForGroupDao
import com.newshunt.appview.common.group.ui.adapter.InviteItemDecoration
import com.newshunt.appview.common.group.ui.adapter.InviteOptionsAdapter
import com.newshunt.appview.common.group.viewmodel.GroupInvitationVMFactory
import com.newshunt.appview.common.group.viewmodel.GroupInvitationViewModel
import com.newshunt.appview.common.ui.activity.AuthorizationBaseActivity
import com.newshunt.appview.common.ui.fragment.SearchCardsFragment
import com.newshunt.appview.common.ui.fragment.SearchCardsFragmentUIMode
import com.newshunt.appview.common.ui.helper.ErrorHelperUtils
import com.newshunt.appview.common.viewmodel.ClickDelegate
import com.newshunt.appview.common.viewmodel.ClickDelegateProvider
import com.newshunt.appview.databinding.ActivityGrpInvitationBinding
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.PermissionEvent
import com.newshunt.dataentity.common.model.entity.PermissionResult
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.GROUP_ID_KEY
import com.newshunt.dataentity.model.entity.GROUP_ID_QUERY_PARAM_KEY
import com.newshunt.dataentity.model.entity.GROUP_INFO_KEY
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.GroupInviteConfig
import com.newshunt.dataentity.model.entity.GroupLocations
import com.newshunt.dataentity.model.entity.INVITATION_TAB_TYPE
import com.newshunt.dataentity.model.entity.InviteConfigWithGroupInfo
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.model.entity.MembershipStatus
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.dataentity.onboarding.RegistrationState
import com.newshunt.dataentity.onboarding.RegistrationUpdate
import com.newshunt.dataentity.search.SearchActionType
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.searchhint.entity.SearchLocation
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.APIUtils
import com.newshunt.dhutil.helper.common.DefaultRationaleProvider
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.onboarding.presenter.AppRegistrationHandler
import com.newshunt.permissionhelper.PermissionAdapter
import com.newshunt.permissionhelper.PermissionHelper
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.permissionhelper.utilities.PermissionUtils
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.search.model.service.AUTOCOMPLETE_URL
import com.newshunt.sso.SSO
import com.squareup.otto.Subscribe
import javax.inject.Inject

/**
 * Activity to show various invitation options
 * <p>
 * Created by srikanth.ramaswamy on 09/27/2019.
 */
private const val GROUP_SUGGESTED_MEMBERS = "GRP_SUGGESTED_MEMBERS"
private const val LOG_TAG = "GrpInvitationActivity"
private const val CONTACT_PERM_REQ_CODE = 1568

class GroupInvitationActivity : AuthorizationBaseActivity(), ClickDelegateProvider, ClickDelegate, ContactsFlow, View.OnClickListener {
    private lateinit var requestedGroup: GroupBaseInfo
    private var inviteConfigWithGroupInfo: InviteConfigWithGroupInfo?= null
    private var permissionHelper: PermissionHelper? = null

    @Inject
    lateinit var groupInvitationViewModelF: GroupInvitationVMFactory
    private lateinit var viewBinding: ActivityGrpInvitationBinding
    private lateinit var groupInvitationViewModel: GroupInvitationViewModel
    private lateinit var fragmentCommunicationsViewModel: FragmentCommunicationsViewModel
    private var inviteOptAdapter: InviteOptionsAdapter? = null
    private var eventParams = HashMap<NhAnalyticsEventParam, Any?>()
    private var pendingSearchQueryString = Constants.EMPTY_STRING
    private var referrerRaw:String? = null


    private val permissionAdapter = object : PermissionAdapter(CONTACT_PERM_REQ_CODE, this, DefaultRationaleProvider()) {
        override fun getPermissions(): MutableList<Permission> {
            return mutableListOf(Permission.READ_CONTACTS)
        }

        override fun onPermissionResult(grantedPermissions: MutableList<Permission>, deniedPermissions: MutableList<Permission>, blockedPermissions: MutableList<Permission>) {
            val contactPermissionGranted = PermissionUtils.hasPermission(CommonUtils.getApplication(), Permission.READ_CONTACTS.permission)
            val permissionEvent = PermissionEvent(Permission.READ_CONTACTS.permission, contactPermissionGranted)
            val fragmentCommunicationEvent = FragmentCommunicationEvent(activityId, permissionEvent)
            fragmentCommunicationsViewModel.fragmentCommunicationLiveData.value = fragmentCommunicationEvent
            if (deniedPermissions.isNotEmpty() || blockedPermissions.isNotEmpty()) {
                Logger.d(LOG_TAG, "Contacts Permission was denied")
                return
            }
            if (pendingSearchQueryString.isEmpty().not()) {
                openPhonebook(pendingSearchQueryString)
            }
            BGSyncService.startBGSyncService()
        }

        override fun shouldShowRationale(): Boolean {
            return true
        }

        override fun getExtraParamsMap(): MutableMap<NhAnalyticsEventParam, Any?> {
            eventParams[NhAnalyticsDialogEventParam.USER_PROFILE] =
                    inviteConfigWithGroupInfo?.groupInfo?.userRole?.name
            return eventParams
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_grp_invitation)
        readGroupInfoFromIntent(intent)
        DaggerGroupInviteComponent
                .builder()
                .groupBaseModule(GroupBaseModule(SocialDB.instance()))
                .groupInvitationModule(GroupInvitationModule(requestedGroup, SocialDB.instance()))
                .build()
                .inject(this)
        groupInvitationViewModel = ViewModelProviders.of(this, groupInvitationViewModelF)
                .get(GroupInvitationViewModel::class.java)
        fragmentCommunicationsViewModel = ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
        if(!SSO.getInstance().isLoggedIn(false)) {
            showError(APIUtils.getError(IllegalStateException("User is not logged in")))
        }
        viewBinding.vm = groupInvitationViewModel
        viewBinding.executePendingBindings()
        viewBinding.actionbarBackButton.setOnClickListener(this)
        observeInvitations()
        observeInvitationConfig()
    }

    override fun getLogTag(): String = LOG_TAG

    override fun showLoginError() {
        hideError()
        hideShimmer()
        Logger.e(LOG_TAG, "NO GUEST SESSION, SHOWING ERROR TO RETRY GUEST LOGIN")
        viewBinding.errorParent.root.visibility = View.VISIBLE
        viewBinding.errorParent.vm = groupInvitationViewModel
        viewBinding.errorParent.baseError = BaseError(CommonUtils.getString(com.newshunt.dhutil.R.string.error_generic))
    }

    @Subscribe
    fun onRegistrationUpdate(registrationUpdate: RegistrationUpdate) {
        if (RegistrationState.REGISTERED
                        .equals(registrationUpdate.getRegistrationState())) {
            if (!SSO.getInstance().isLoggedIn(false) || CommonUtils.isEmpty(SSO.getInstance()
                            .userDetails?.userLoginResponse?.userId)) {
                groupInvitationViewModel.retryGuestLogin(this)
            }
        }else{
            mandateGuestLogin()
        }
    }
    override fun getClickDelegate(): ClickDelegate {
        return this
    }

    override fun onDestroy() {
        try {
            BusProvider.getUIBusInstance().unregister(this)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
        super.onDestroy()
    }

    @Subscribe
    fun onPermissionResult(result: PermissionResult) {
        permissionHelper?.handlePermissionCallback(this, result.permissions)
        BusProvider.getUIBusInstance().unregister(this)
    }

    override fun onViewClick(view: View, item: Any) {
        onViewClick(view, item, null)
    }

    override fun onViewClick(view: View, item: Any, args: Bundle?) {
        val position = args?.getInt(Constants.STORY_POSITION) ?: -1

        when (view.id) {
            R.id.invite_member_btn -> {
                inviteConfigWithGroupInfo?.groupInfo?.let { info ->
                    (item as? Member?)?.let { member ->
                        groupInvitationViewModel.invite(member)
                        AnalyticsHelper2.logInviteMemberClick(position, member.userId)
                        return
                    }
                }
            }
            R.id.member_info_card ->
                (item as? Member?)?.let { member ->
                    CommonNavigator.launchProfileActivity(view.context,
                            UserBaseProfile().apply { userId = member.userId }, PageReferrer(NhGenericReferrer.INVITE_SCREEN))
                }
        }
    }

    override fun onResume() {
        super.onResume()
        inviteOptAdapter?.filterInstalledApps()
    }

    override fun requestContactsPermission() {
        if (PermissionUtils.isPermissionBlocked(this, Permission.READ_CONTACTS.permission)) {
            PermissionUtils.openAppSettingActivity(this)
        } else {
            BusProvider.getUIBusInstance().register(this)
            permissionHelper = PermissionHelper(permissionAdapter).apply {
                requestPermissions()
            }
        }
    }

    override fun requestContactsPermissionAndSearchPhonebook(queryString: String?) {
        pendingSearchQueryString = queryString ?: Constants.EMPTY_STRING
        requestContactsPermission()
    }

    override fun getActivityId(): Int {
        return activityID
    }

    override fun openPhonebook(queryString: String?) {
        inviteConfigWithGroupInfo?.groupInfo?.let { groupInfo ->
            groupInvitationViewModel.openPhonebook(this, groupInfo, queryString)
        }
    }

    override fun onClick(view: View?) {
        view ?: return
        if (view.id == R.id.actionbar_back_button) {
            handleBack(false)
        }
    }

    override fun onBackPressed() {
        handleBack(true,referrerRaw)
    }

    private fun readGroupInfoFromIntent(intent: Intent?) {
        intent?.let {
            requestedGroup = (it.getSerializableExtra(GROUP_INFO_KEY) as? GroupInfo?)?.let { grpInfo ->
                grpInfo
            } ?: GroupBaseInfo().apply {
                this.id = it.getStringExtra(GROUP_ID_KEY)
                        ?: throw IllegalArgumentException("Can not display invitation screen without atleast group id")
            }
            pageReferrer = it.getSerializableExtra(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
            referrerRaw = intent.extras?.getString(Constants.REFERRER_RAW)
        }
    }

    private fun setupViews(info: GroupInfo, inviteConfig: GroupInviteConfig) {
        inviteOptAdapter = InviteOptionsAdapter(this, inviteConfig.inviteMediums, this, groupInvitationViewModel)
        viewBinding.inviteOptions.adapter = inviteOptAdapter
        viewBinding.inviteOptions.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        viewBinding.inviteOptions.addItemDecoration(InviteItemDecoration())
        groupInvitationViewModel.setReferrer(pageReferrer)
        val dynamicFeed = buildDynamicFeed(inviteConfig, info)
        val searchPayloadContext = SearchPayloadContext(groupId = info.id,
                action = SearchActionType.GROUP_ADD_PARTICIPANT.name,
                section = NhAnalyticsEventSection.GROUP.eventSection)
        val searchUrl = CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getSearchBaseUrl())
                .plus(AUTOCOMPLETE_URL)
        val fragment = SearchCardsFragment.create(dynamicFeed = dynamicFeed,
                pageId = dynamicFeed.id,
                listType = Format.MEMBER.name,
                section = PageSection.GROUP.section,
                searchUrl = searchUrl,
                queryParam = Constants.SEARCH_QUERY_PARAM_KEY,
                context = searchPayloadContext,
                searchLocation = SearchLocation.PeapleSearch,
                groupInfo = inviteConfigWithGroupInfo?.groupInfo,
                tabType = INVITATION_TAB_TYPE,
                uiMode = SearchCardsFragmentUIMode.INVITES_PEOPLE_SEARCH,
                referrer = PageReferrer(NhGenericReferrer.INVITE_SCREEN),
                noContentLayoutId = R.layout.layout_no_invite_suggestions)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.suggestedContactsFragmentHolder, fragment, GROUP_SUGGESTED_MEMBERS)
        fragmentTransaction.commit()
        fragment.userVisibleHint = true
        viewBinding.executePendingBindings()
        AnalyticsHelper2.logInviteScreenShown(pageReferrer, info.id)
    }

    private fun observeInvitations() {
        groupInvitationViewModel.invitationResultLiveData.observe(this, Observer {
            if (it.isSuccess) {
                Logger.d(LOG_TAG, "Invitation sent out")
            } else {
                ErrorHelperUtils.showErrorSnackbar(it.exceptionOrNull(), viewBinding.root)
            }
        })
    }

    private fun observeInvitationConfig() {
        groupInvitationViewModel.inviteConfigWithGroupInfoLD.observe(this, Observer {
            if (it.isSuccess) {
                inviteConfigWithGroupInfo = it.getOrNull()
                inviteConfigWithGroupInfo?.also { invConfigWithGrpInfo ->
                    if(invConfigWithGrpInfo.groupInfo.membership == MembershipStatus.NONE) {
                        showError(APIUtils.getError(IllegalStateException("User is not a member of the group")))
                    }
                    else {
                        hideError()
                        setupViews(invConfigWithGrpInfo.groupInfo, invConfigWithGrpInfo.inviteConfig)
                    }
                } ?: showError(BaseError(CommonUtils.getString(com.newshunt.dhutil.R.string.error_generic)))
            } else {
                showError(it.exceptionOrNull())
            }
        })
        groupInvitationViewModel.inviteConfigStatusLD.observe(this, Observer {
            if (it) {
                showShimmer()
            } else {
                hideShimmer()
            }
        })
        groupInvitationViewModel.showContactPermission.observe(this, Observer {
            if(it) {
                if(permissionAdapter.shouldShowRationale()){
                    requestContactsPermission()
                }
            }
        })
    }

    private fun buildDynamicFeed(inviteConfig: GroupInviteConfig, info: GroupInfo): GeneralFeed {
        val contentUrl = Uri.parse(inviteConfig.contentUrl).buildUpon()
                .appendQueryParameter(GROUP_ID_QUERY_PARAM_KEY, info.id)
                .build()
                .toString()
        return GeneralFeed(buildLocationForGroupDao(GroupLocations.G_M_L_I, info.id), contentUrl,
                Constants.HTTP_GET, PageSection.GROUP.section)
    }

    private fun showError(throwable: Throwable?) {
        if (throwable is BaseError) {
            hideShimmer()
            Logger.d(LOG_TAG, "Showing error for ${throwable.message}")
            viewBinding.errorParent.vm = groupInvitationViewModel
            viewBinding.errorParent.baseError = throwable
            viewBinding.errorParent.root.visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        viewBinding.errorParent.root.visibility = View.GONE
    }

    private fun showShimmer() {
        viewBinding.inviteShimmer.profileShimmerContainer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        viewBinding.inviteShimmer.profileShimmerContainer.visibility = View.GONE
    }
}

interface ContactsFlow {
    fun requestContactsPermission()
    fun requestContactsPermissionAndSearchPhonebook(queryString: String?)
    fun getActivityId(): Int
    fun openPhonebook(queryString: String?)
}