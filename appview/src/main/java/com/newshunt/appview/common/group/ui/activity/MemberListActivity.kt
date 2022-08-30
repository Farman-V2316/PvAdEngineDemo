/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.ui.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.analytics.helper.ReferrerProviderHelper
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.group.DaggerGroupMemberListComponent
import com.newshunt.appview.common.group.GroupBaseModule
import com.newshunt.appview.common.group.MemberListModule
import com.newshunt.appview.common.group.buildLocationForGroupDao
import com.newshunt.appview.common.group.viewmodel.MEMBERS_FP_ENDPOINT
import com.newshunt.appview.common.group.viewmodel.MEMBERS_FP_GRPID_QUERY_KEY
import com.newshunt.appview.common.group.viewmodel.MemberListViewModel
import com.newshunt.appview.common.ui.activity.AuthorizationBaseActivity
import com.newshunt.appview.common.ui.fragment.SearchCardsFragment
import com.newshunt.appview.common.ui.helper.ErrorHelperUtils
import com.newshunt.appview.common.viewmodel.ClickDelegate
import com.newshunt.appview.common.viewmodel.ClickDelegateProvider
import com.newshunt.appview.databinding.ActivityMemberListBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.GROUP_INFO_KEY
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.GroupLocations
import com.newshunt.dataentity.model.entity.MEMBER_LIST_TAB_TYPE
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.model.entity.MemberRole
import com.newshunt.dataentity.model.entity.UIResponseWrapper
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.dataentity.search.SearchActionType
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.searchhint.entity.SearchLocation
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.profile.OptionsBottomSheetFragment
import com.newshunt.profile.SimpleOptionItem
import com.newshunt.profile.SimpleOptions
import com.newshunt.profile.UiProperties
import com.newshunt.search.model.service.AUTOCOMPLETE_URL
import javax.inject.Inject

private const val TAG = "MemberListActivity"
private const val TAG_CARDS_FRAGMENT = "TAG_CARDS_FRAGMENT_PARENT"

/**
 * @author raunak.yadav
 */
class MemberListActivity : AuthorizationBaseActivity(), ClickDelegateProvider, ReferrerProviderlistener, ClickDelegate {

    override fun getLogTag(): String {
        return TAG
    }

    override fun showLoginError() {
        //No deeplink to this screen. No need to handle login error
    }

    @Inject
    lateinit var viewModelF: MemberListViewModel.Factory

    private lateinit var viewBinding: ActivityMemberListBinding
    private lateinit var viewModel: MemberListViewModel
    private lateinit var groupInfo: GroupInfo
    private val currentReferrer: PageReferrer = PageReferrer(NhGenericReferrer.MEMBER_LIST)
    private val referrerProviderHelper = ReferrerProviderHelper()
    private var type: DialogBoxType? = null
    private var referrerRaw:String? = null


    private var selectedMember: Member? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_member_list)

        intent?.let {
            (it.getSerializableExtra(GROUP_INFO_KEY) as? GroupInfo?)?.let { info ->
                groupInfo = info
            }
        }

        if (!::groupInfo.isInitialized) {
            finish()
            throw IllegalArgumentException("Can not show members without GroupInfo")
        }

        DaggerGroupMemberListComponent.builder()
                .groupBaseModule(GroupBaseModule(SocialDB.instance()))
                .memberListModule(MemberListModule(groupInfo.id))
                .build().inject(this)
        viewModel = ViewModelProviders.of(this, viewModelF).get(MemberListViewModel::class.java)
        viewModel.setReferrer(currentReferrer)
        pageReferrer = intent.extras?.get(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        referrerRaw = intent.extras?.getString(Constants.REFERRER_RAW)
        referrerProviderHelper.addReferrerByProvider(pageReferrer)
        setupViews()

        observeViewData()
        observeMenuEvents()
    }

    override fun getClickDelegate(): ClickDelegate {
        return this
    }

    override fun onViewClick(view: View, item: Any) {
        onViewClick(view, item, null)
    }

    override fun onViewClick(view: View, item: Any, args: Bundle?) {
        Logger.d(TAG, "view clicked $item")
        if (item !is Member) return

        when (view.id) {
            R.id.member_info_card ->
                CommonNavigator.launchProfileActivity(view.context,
                        UserBaseProfile().apply { userId = item.s_id() }, currentReferrer)

            R.id.member_card_three_dots -> {
                selectedMember = item
                showMemberMenuOptions(selectedMember?.role ?: MemberRole.NONE)
                DialogAnalyticsHelper.logDialogBoxViewedEvent(null, currentReferrer, NhAnalyticsEventSection.GROUP,
                        groupInfo.userRole)
            }
            else ->
                Logger.e(TAG, "unhandled View clicked $view")
        }
    }

    override fun getReferrerEventSection(): NhAnalyticsEventSection {
        return NhAnalyticsEventSection.GROUP
    }

    override fun getProvidedReferrer(): PageReferrer? {
        return referrerProviderHelper.providedPageReferrer
    }

    override fun onBackPressed() {
        handleBack(true,referrerRaw)
    }

    override fun getLatestPageReferrer(): PageReferrer? {
        return referrerProviderHelper.referrerQueue.yongest
    }

    private fun isMenuBlocked(): Boolean {
        return groupInfo.userRole == MemberRole.NONE
    }

    private fun setupViews() {
        viewBinding.setVariable(BR.groupInfo, groupInfo)
        viewBinding.actionbar.toolbarBack.setOnClickListener {
            finish()
        }
        viewBinding.actionbar.inviteOption.setOnClickListener {
            CommonNavigator.launchGroupInvitationActivity(this, groupInfo, currentReferrer)
        }
        val transaction = supportFragmentManager.beginTransaction()
        val dynamicFeed = buildDynamicFeed()
        val searchPayloadContext = SearchPayloadContext(groupId = groupInfo.id,
                action = SearchActionType.GROUP_PARTICIPANT_SEARCH.name,
                section = NhAnalyticsEventSection.GROUP.eventSection)
        val searchUrl = CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getSearchBaseUrl())
                .plus(AUTOCOMPLETE_URL)
        transaction.replace(R.id.search_fragment_holder,
                SearchCardsFragment.create(dynamicFeed = dynamicFeed,
                        pageId = dynamicFeed.id,
                        listType = Format.MEMBER.name,
                        section = PageSection.GROUP.section,
                        searchUrl = searchUrl,
                        queryParam = Constants.SEARCH_QUERY_PARAM_KEY,
                        context = searchPayloadContext,
                        searchLocation = SearchLocation.MemberSearch,
                        groupInfo = groupInfo,
                        tabType = MEMBER_LIST_TAB_TYPE,
                        referrer = currentReferrer),
                TAG_CARDS_FRAGMENT)
        transaction.commit()
    }

    private fun showMemberMenuOptions(memberRole: MemberRole) {
        check(memberRole != MemberRole.NONE) { "Non member present in member list. How?" }

        if (isMenuBlocked()) return

        val menuList = ArrayList<SimpleOptionItem>()
        val uiProperties = UiProperties()
        uiProperties.imageIconSize = CommonUtils.getDimension(R.dimen.square_option_icon_small_size)

        groupInfo.userRole?.let {
            val options = MemberMenuResolver.resolve(it, memberRole)
            options.forEach {
                when (it) {
                    GroupMemberOptions.REMOVE_ADMIN ->
                        menuList.add(SimpleOptionItem(R.drawable.ic_remove_admin,
                                CommonUtils.getString(R.string.remove_admin), GroupMemberOptions.REMOVE_ADMIN, uiProperties = uiProperties))
                    GroupMemberOptions.MAKE_ADMIN ->
                        menuList.add(SimpleOptionItem(R.drawable.ic_add_admin,
                                CommonUtils.getString(R.string.make_admin), GroupMemberOptions.MAKE_ADMIN, uiProperties = uiProperties))
                    GroupMemberOptions.REMOVE_USER ->
                        menuList.add(SimpleOptionItem(R.drawable.ic_remove,
                                CommonUtils.getString(R.string.remove_member), GroupMemberOptions.REMOVE_USER, uiProperties = uiProperties))
                    GroupMemberOptions.REPORT_USER ->
                        menuList.add(SimpleOptionItem(R.drawable.ic_report_icon,
                                CommonUtils.getString(R.string.report_user), GroupMemberOptions.REPORT_USER, uiProperties = uiProperties))
                }
            }
            if (menuList.isNotEmpty()) {
                val menuOptions = SimpleOptions(menuList, activityID)
                supportFragmentManager?.let {
                    OptionsBottomSheetFragment.newInstance(menuOptions).show(it, "MemberOptionsMenu")
                }
            }
        }
    }

    private fun observeMenuEvents() {
        if (isMenuBlocked()) return

        ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
                .fragmentCommunicationLiveData.observe(this, Observer {

            if (it.hostId != activityID || it.anyEnum !is GroupMemberOptions) {
                return@Observer
            }
            selectedMember?.userId ?: return@Observer
            val userId = selectedMember!!.userId

            when (it.anyEnum) {
                GroupMemberOptions.REMOVE_ADMIN -> {
                    viewModel.changeRole(userId, MemberRole.MEMBER)
                    type = DialogBoxType.REMOVE_ADMIN
                }
                GroupMemberOptions.MAKE_ADMIN -> {
                    viewModel.changeRole(userId, MemberRole.ADMIN)
                    type = DialogBoxType.MAKE_ADMIN
                }
                GroupMemberOptions.REMOVE_USER -> {
                    viewModel.removeUser(userId)
                    type = DialogBoxType.REMOVE_USER
                }
                GroupMemberOptions.REPORT_USER -> {
                    viewModel.reportUser(groupInfo)
                    type = DialogBoxType.REPORT_USER
                }
            }
            DialogAnalyticsHelper.logDialogBoxActionEvent(type, currentReferrer,
                    DialogAnalyticsHelper.DIALOG_ACTION_OK, NhAnalyticsEventSection.GROUP, groupInfo.userRole)
        })
    }

    private fun buildDynamicFeed(): GeneralFeed {
        val contentUrl = CommonUtils.formatBaseUrlForRetrofit(
                NewsBaseUrlContainer.getGroupsBaseUrl())
                .plus(MEMBERS_FP_ENDPOINT)
        val formatedUrl = Uri.parse(contentUrl)
                .buildUpon()
                .appendQueryParameter(MEMBERS_FP_GRPID_QUERY_KEY, groupInfo.id)
                .toString()
        return GeneralFeed(buildLocationForGroupDao(GroupLocations.G_M_L, groupInfo.id),
                formatedUrl, Constants.HTTP_GET, PageSection.GROUP.section)
    }

    private fun getCardsFragment(): SearchCardsFragment? {
        return supportFragmentManager?.findFragmentByTag(TAG_CARDS_FRAGMENT) as? SearchCardsFragment
    }

    private fun observeViewData() {
        viewModel.roleChangeLiveData.observe(this, Observer {
            handleOperationFeedback(it)
        })
        viewModel.userRemovalLiveData.observe(this, Observer {
            handleOperationFeedback(it)
        })
    }


    private fun <T> handleOperationFeedback(result: Result<UIResponseWrapper<T>>) {
        if (result.isSuccess) {
            val response = result.getOrNull()
            response?.message?.let {
                FontHelper.showCustomFontToast(this, it, Toast.LENGTH_SHORT)
            }
            getCardsFragment()?.refresh()
        } else {
            ErrorHelperUtils.showErrorSnackbar(result.exceptionOrNull(), viewBinding.root)
        }
    }

    private enum class GroupMemberOptions {
        /**
         * Only Owner can remove an admin
         */
        REMOVE_ADMIN,
        /**
         * Only Owner/Admins can make a member, admin.
         */
        MAKE_ADMIN,
        /**
         * For Owner/Admins (except Admins cannot remove other admins.)
         */
        REMOVE_USER,
        /**
         * All users
         */
        REPORT_USER
    }

    /**
     * Helps resolve menu options for a given member.
     *
     * @author raunak.yadav
     */
    private class MemberMenuResolver {

        companion object {
            private val allowedMenuMap: Map<MemberRole, List<GroupMemberOptions>> =
                    mapOf(MemberRole.OWNER to listOf(
                            GroupMemberOptions.REMOVE_ADMIN,
                            GroupMemberOptions.MAKE_ADMIN,
                            GroupMemberOptions.REMOVE_USER,
                            GroupMemberOptions.REPORT_USER),

                            MemberRole.ADMIN to listOf(
                                    GroupMemberOptions.MAKE_ADMIN,
                                    GroupMemberOptions.REMOVE_USER,
                                    GroupMemberOptions.REPORT_USER),

                            MemberRole.MEMBER to listOf(
                                    GroupMemberOptions.REPORT_USER),

                            MemberRole.NONE to listOf()
                    )

            fun resolve(selfRole: MemberRole, userRole: MemberRole): List<GroupMemberOptions> {
                val options = mutableListOf<GroupMemberOptions>()
                options.addAll(allowedMenuMap.getValue(selfRole))

                when (userRole) {
                    MemberRole.OWNER -> {
                        options.clear()
                        options.add(GroupMemberOptions.REPORT_USER)
                    }
                    MemberRole.ADMIN -> {
                        options.remove(GroupMemberOptions.MAKE_ADMIN)
                        if (selfRole != MemberRole.OWNER) {
                            options.remove(GroupMemberOptions.REMOVE_USER)
                        }
                    }
                    MemberRole.MEMBER -> {
                        options.remove(GroupMemberOptions.REMOVE_ADMIN)
                    }
                    else -> {
                        options.clear()
                    }
                }
                return options
            }
        }
    }
}
