/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.ui.activity

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.analytics.helper.ReferrerProviderHelper
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsFragment
import com.newshunt.appview.common.group.ApprovalModule
import com.newshunt.appview.common.group.DaggerGroupApprovalComponent
import com.newshunt.appview.common.group.viewmodel.ApprovalViewModelFactory
import com.newshunt.appview.common.group.viewmodel.ApprovalsViewModel
import com.newshunt.appview.common.ui.activity.AuthorizationBaseActivity
import com.newshunt.appview.common.ui.helper.LiveDataEventHelper
import com.newshunt.appview.databinding.ActivityApprovalsBinding
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.ApprovalTab
import com.newshunt.dataentity.model.entity.BUNDLE_APPROVAL_PREFERRED_TAB_TYPE
import com.newshunt.dataentity.model.entity.INVITATION_APPROVAL_TAB_TYPE
import com.newshunt.dataentity.model.entity.MEMBER_APPROVAL_TAB_TYPE
import com.newshunt.dataentity.model.entity.POST_APPROVAL_TAB_TYPE
import com.newshunt.dataentity.model.entity.PendingApprovalsEntity
import com.newshunt.dataentity.model.entity.ReviewItem
import com.newshunt.dataentity.onboarding.RegistrationState
import com.newshunt.dataentity.onboarding.RegistrationUpdate
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.customview.SlidingTabLayout
import com.newshunt.sso.SSO
import com.squareup.otto.Subscribe
import javax.inject.Inject

/**
 * Shows aggregated list of members/post/invitations pending across groups.
 *
 * @author raunak.yadav
 */
private const val LOG_TAG = "ApprovalActivity"
class ApprovalsActivity : AuthorizationBaseActivity(), ReferrerProviderlistener {
    @Inject
    lateinit var approvalsViewModelF: ApprovalViewModelFactory

    private lateinit var viewBinding: ActivityApprovalsBinding
    private lateinit var tabsLayout: SlidingTabLayout
    private lateinit var adapter: ApprovalsAdapter
    private lateinit var viewModel: ApprovalsViewModel
    private var preferredTabLandingType: ReviewItem? = null
    private val referrerProviderHelper = ReferrerProviderHelper()
    private var acceptableTimeStamp = System.currentTimeMillis()
    private var pendingApprovals: PendingApprovalsEntity? = null
    private var referrerRaw:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ThemeUtils.preferredTheme.themeId)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_approvals)

        DaggerGroupApprovalComponent.builder()
                .approvalModule(ApprovalModule())
                .build().inject(this)

        viewModel = ViewModelProviders.of(this, approvalsViewModelF).get(ApprovalsViewModel::class.java)

        setupViews()
        preferredTabLandingType = intent.getSerializableExtra(BUNDLE_APPROVAL_PREFERRED_TAB_TYPE)
                as? ReviewItem
        pageReferrer = intent.extras?.get(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        referrerRaw = intent.extras?.getString(Constants.REFERRER_RAW)
        referrerProviderHelper.addReferrerByProvider(pageReferrer)
        referrerProviderHelper.addReferrerByProvider(PageReferrer(NhGenericReferrer.APPROVALS))
        acceptableTimeStamp = System.currentTimeMillis()
        observeApprovalsConfig()
        observeNewGroup()
        observeApprovals()
    }

    override fun getDetailFragmentHostId(): Int {
        return R.id.detail_fragment_holder
    }

    override fun getLogTag(): String {
        return LOG_TAG
    }

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
        viewBinding.errorParent.vm = viewModel
    }

    @Subscribe
    fun onRegistrationUpdate(registrationUpdate: RegistrationUpdate) {
        if (RegistrationState.REGISTERED
                        .equals(registrationUpdate.getRegistrationState())) {
            if (!SSO.getInstance().isLoggedIn(false) || CommonUtils.isEmpty(SSO.getInstance()
                            .userDetails?.userLoginResponse?.userId)) {
                viewModel.retryGuestLogin(this)
            }
        }else{
            mandateGuestLogin()
        }
    }


    private fun setupViews() {
        val activeColor = CommonUtils.getColor(R.color.text_red)
        val inactiveColor = ThemeUtils.getThemeColorByAttribute(this, R.attr.inactive_tab_text_color)
        tabsLayout = viewBinding.approvalTabs
        tabsLayout.setTabTextColor(activeColor, inactiveColor)
        tabsLayout.setDrawBottomLine(true)
        tabsLayout.setCustomTabView(R.layout.tab_item, R.id.tab_item_title, View.NO_ID)
        tabsLayout.setTabSelectionLineHeight(CommonUtils.getDimension(R.dimen.divider_height))

        if (!::adapter.isInitialized) {
            adapter = ApprovalsAdapter(supportFragmentManager)
            viewBinding.approvalsViewPager.adapter = adapter
        }
        tabsLayout.setViewPager(viewBinding.approvalsViewPager)
        viewBinding.actionbar.toolbarBackButtonContainer.setOnClickListener{
            handleBack(false)
        }
        viewBinding.executePendingBindings()
    }

    override fun onBackPressed() {
        handleBack(true,referrerRaw)
    }

    private fun addTabs(tabs: List<ApprovalTab>) {
        adapter.updateTabs(tabs)
        tabsLayout.setViewPager(viewBinding.approvalsViewPager)
        var defaultTabIndex = 0
        if (preferredTabLandingType != null) {
            defaultTabIndex = adapter.getIndexForTabType(preferredTabLandingType, tabs)
            if (defaultTabIndex >= 0) {
                viewBinding.approvalsViewPager.setCurrentItem(defaultTabIndex, false)
            }
        }
        tabsLayout.setViewPager(viewBinding.approvalsViewPager)
    }

    private fun observeApprovalsConfig() {
        viewModel.approvalTabsLiveData.observe(this, Observer { result ->
            if (result.isSuccess) {
                val adapterList = result.getOrNull()
                adapterList?.let {
                    addTabs(it)
                } ?: showError(BaseError(CommonUtils.getString(com.newshunt.dhutil.R.string.error_generic)))
            } else {
                showError(result.exceptionOrNull())
            }
        })
        viewModel.approvalStatusLD.observe(this, Observer {
            if (it) {
                showShimmer()
            } else {
                hideShimmer()
            }
        })
    }

    private fun showError(throwable: Throwable?) {
        if (throwable is BaseError) {
            hideShimmer()
            Logger.d(LOG_TAG, "Showing error for ${throwable.message}")
            viewBinding.errorParent.vm = viewModel
            viewBinding.errorParent.baseError = throwable
            viewBinding.errorParent.root.visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        viewBinding.errorParent.root.visibility = View.GONE
    }

    private fun showShimmer() {
        viewBinding.approvalsShimmer.profileShimmerContainer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        viewBinding.approvalsShimmer.profileShimmerContainer.visibility = View.GONE
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

    private fun observeNewGroup() {
        LiveDataEventHelper.newGroupLiveData.observe(this, Observer {
            if (it.timestamp < acceptableTimeStamp) {
                return@Observer
            }
            //On new group creation or join, refresh the cards fragment to show the new group
            if (adapter.getCurrentTabType() == ReviewItem.GROUP_INVITATION) {
                Logger.d(LOG_TAG, "We might have an approval done, refresh cards fragment")
                (adapter.currentFragment as? CardsFragment?)?.refresh()
            }
        })
    }

    private fun observeApprovals() {
        SSO.getInstance().userDetails?.userID?.let {
            viewModel.fetchPendingApprovalCounts(it)
            viewModel.pendingApprovalLiveData.observe(this, Observer {
                if (it.isSuccess) {
                    val result = it.getOrNull()
                    if (pendingApprovals == null) {
                        pendingApprovals = result
                        return@Observer
                    }
                    pendingApprovals = result
                    //Approval status changed, refresh. Applicable to invitations and all approvals
                    (adapter.currentFragment as? CardsFragment?)?.refresh()
                }
            })
        }
    }
}

/**
 * Adapter for approval viewPager.
 */
class ApprovalsAdapter(fragmentManager: FragmentManager)
    : FragmentStatePagerAdapter(fragmentManager) {

    var currentFragment: Fragment? = null
    var currentItemPosition = -1
    private var tabList = ArrayList<ApprovalTab>()

    fun updateTabs(tabs: List<ApprovalTab>) {
        tabList.clear()
        tabList.addAll(tabs)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Fragment {
        val tab = tabList[position]
        val layoutId = if(tab.tabType == ReviewItem.GROUP_POST)
        {
            R.layout.layout_no_post_approvals
        }else if(tab.tabType == ReviewItem.GROUP_INVITATION){
            R.layout.layout_no_invitions
        }else{
            R.layout.layout_no_member_approvals
        }
        val tabType = when(tab.tabType) {
            ReviewItem.GROUP_INVITATION -> INVITATION_APPROVAL_TAB_TYPE
            ReviewItem.GROUP_MEMBER -> MEMBER_APPROVAL_TAB_TYPE
            else -> POST_APPROVAL_TAB_TYPE
        }

        return CardsFragment.create(bundleOf(Constants.PAGE_ID to tab.entityId!!,
                Constants.LIST_TYPE to getListType(tab.tabType),
                NewsConstants.DH_SECTION to PageSection.GROUP.section,
                Constants.BUNDLE_CLEAR_ON_NO_CONTENT to true,
                Constants.BUNDLE_ERROR_LAYOUT_ID to layoutId,
                Constants.HIDE_NO_CONTENT_SNACKBAR to true,
                Constants.LIST_TAB_TYPE to tabType,
                CardsFragment.DISABLE_MORE_NEWS_TOOLITP to true,
                Constants.REFERRER to PageReferrer(NhGenericReferrer.APPROVALS)))
    }

    override fun getItemPosition(obj: Any): Int {
        return androidx.viewpager.widget.PagerAdapter.POSITION_NONE
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun getCount(): Int {
        return tabList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabList[position].name
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)
        if (obj is Fragment) {
            currentFragment = obj
            currentItemPosition = position
        }
    }

    //TODO: change return type to enum declared by list
    private fun getListType(tabType: ReviewItem?): String? {
        return when (tabType) {
            ReviewItem.GROUP_MEMBER -> Format.MEMBER.name
            ReviewItem.GROUP_INVITATION -> Format.GROUP_INVITE.name
            else -> null
        }
    }

    fun getIndexForTabType(tabType: ReviewItem?, tabs: List<ApprovalTab>?): Int {
        if (CommonUtils.isEmpty(tabs)) {
            return -1
        }
        tabs!!.forEachIndexed { index, approvalTabs ->
            if (approvalTabs.tabType == tabType) {
                return index
            }
        }
        return 0
    }

    fun getCurrentTabType(): ReviewItem? {
        return if (currentItemPosition != -1) {
            tabList[currentItemPosition].tabType
        } else {
            null
        }
    }
}
