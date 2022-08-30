/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.group.ui.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.group.DaggerGroupSettingsUpdateComponent
import com.newshunt.appview.common.group.GroupBaseModule
import com.newshunt.appview.common.group.viewmodel.GroupSettingsViewModel
import com.newshunt.appview.common.ui.activity.AuthorizationBaseActivity
import com.newshunt.appview.common.ui.helper.ErrorHelperUtils
import com.newshunt.appview.databinding.ActivityGroupSettingsBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.view.customview.CommonMessageDialog
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.ChangedSettingsName
import com.newshunt.dataentity.model.entity.DELETE_GROUP
import com.newshunt.dataentity.model.entity.GROUP_INFO_KEY
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.LEAVE_GROUP
import com.newshunt.dataentity.model.entity.MemberRole
import com.newshunt.dataentity.model.entity.SettingState
import com.newshunt.dataentity.model.entity.SettingsPostBody
import com.newshunt.dataentity.model.entity.SocialPrivacy
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.helper.NewsListCardLayoutUtil
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.sso.SSO
import javax.inject.Inject

/**
 * Activity to show group settings
 * <p>
 * Created by helly.patel on 09/23/2019.
 */
private const val LOG_TAG = "GroupSettingsActivity"
private const val YES = "Yes"
private const val NO = "No"

class GroupSettingsActivity : AuthorizationBaseActivity(), View.OnClickListener {

    @Inject
    lateinit var groupSettingsModelF: GroupSettingsViewModel.Factory

    private lateinit var viewModel: GroupSettingsViewModel
    private lateinit var viewBinding: ActivityGroupSettingsBinding
    private lateinit var updateInfo: SettingsPostBody
    private lateinit var groupInfo: GroupInfo
    private var currentReferrer: NhGenericReferrer = NhGenericReferrer.GROUP_SETTINGS
    private lateinit var oldValue: String
    private lateinit var newValue: String
    private var myRole: MemberRole = MemberRole.ADMIN
    private var progressDialog: ProgressDialog? = null
    private var referrerRaw:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_group_settings)
        NewsListCardLayoutUtil.manageLayoutDirection(findViewById(R.id.group_settings_view))

        DaggerGroupSettingsUpdateComponent
                .builder()
                .groupBaseModule(GroupBaseModule(SocialDB.instance()))
                .build()
                .inject(this)
        viewModel = ViewModelProviders.of(this, groupSettingsModelF).get(GroupSettingsViewModel::class.java)
        (intent?.getSerializableExtra(GROUP_INFO_KEY) as? GroupInfo?)?.let {
            groupInfo = it
        }
        referrerRaw = intent.extras?.getString(Constants.REFERRER_RAW)
        if (!::groupInfo.isInitialized) {
            finish()
            throw IllegalArgumentException("Group info not passed in bundle, cant show the setting screen")
        }

        updateInfo = SettingsPostBody()
        updateInfo.id = groupInfo.id
        updateInfo.userId = groupInfo.userId
        groupInfo.userRole?.let {
            myRole = it
        }
        setUpViews()
        viewModel.getUpdatedGroup(groupInfo.id, groupInfo.userId)
        observeFragmentCommunications()
        viewBinding.setVariable(BR.item, groupInfo)
        viewBinding.executePendingBindings()
    }

    private fun observeFragmentCommunications() {
        ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java).fragmentCommunicationLiveData.observe(this, Observer {
            if (it.hostId != activityID) {
                return@Observer
            }

            when (it.anyEnum) {
                is CommonMessageEvents -> {
                    if (it.anyEnum == CommonMessageEvents.POSITIVE_CLICK) {
                        if(it.useCase.equals(LEAVE_GROUP))
                            leaveGroup()
                        else if(it.useCase.equals(DELETE_GROUP))
                            deleteGroup()
                    }
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.delete_group_option -> {
                supportFragmentManager?.let {
                    val commonMessageDialogOptions = CommonMessageDialogOptions(
                        this.activityID,
                        CommonUtils.getString(R.string.delete_group),
                        CommonUtils.getString(R.string.delete_group_msg),
                        CommonUtils.getString(R.string.dialog_delete),
                        CommonUtils.getString(R.string.cancel_text),
                        useCase = DELETE_GROUP
                    )

                    CommonMessageDialog.newInstance(commonMessageDialogOptions).show(it, "CommonMessageDialog")
                    AnalyticsHelper2.logGroupSettingsClickEvent(PageReferrer(currentReferrer),
                            groupInfo, type = Constants.DELETE_GROUP_CLICK, userProfile = myRole)
                }
            }
            R.id.leave_group_option -> {
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
                    AnalyticsHelper2.logGroupSettingsClickEvent(PageReferrer(currentReferrer),
                            groupInfo, type = Constants.LEAVE_GROUP_CLICK, userProfile = myRole)
                }
            }
            R.id.can_join_info, R.id.can_join_checkbox -> {
                updateInfo.name = ChangedSettingsName.MEMBER_APPROVAL.name.toLowerCase()
                if (groupInfo.memberApproval == SettingState.REQUIRED) {
                    updateInfo.value = SettingState.NOT_REQUIRED.name
                    newValue = YES
                    oldValue = NO
                } else {
                    updateInfo.value = SettingState.REQUIRED.name
                    newValue = NO
                    oldValue = YES
                }
                viewModel.updateSettings(updateInfo)
                if (view.id == R.id.can_join_info) {
                    viewBinding.canJoinCheckbox.toggle()
                }
                AnalyticsHelper2.logGroupSettingsClickEvent(PageReferrer(currentReferrer),
                        groupInfo, newValue, oldValue, Constants.ANYONE_CAN_JOIN, userProfile = myRole)
            }
            R.id.can_discover_info, R.id.can_discover_checkbox -> {
                updateInfo.name = ChangedSettingsName.PRIVACY.name.toLowerCase()
                if (groupInfo.privacy == SocialPrivacy.PRIVATE) {
                    updateInfo.value = SocialPrivacy.PUBLIC.name
                    newValue = YES
                    oldValue = NO
                } else {
                    updateInfo.value = SocialPrivacy.PRIVATE.name
                    newValue = NO
                    oldValue = YES
                }
                viewModel.updateSettings(updateInfo)
                if (view.id == R.id.can_discover_info) {
                    viewBinding.canDiscoverCheckbox.toggle()
                }
                AnalyticsHelper2.logGroupSettingsClickEvent(PageReferrer(currentReferrer),
                        groupInfo, newValue, oldValue, Constants.ANYONE_CAN_DISCOVER, userProfile = myRole)
            }
            R.id.can_post_info, R.id.can_post_checkbox -> {
                updateInfo.name = ChangedSettingsName.POST_APPROVAL.name.toLowerCase()
                if (groupInfo.postApproval == SettingState.REQUIRED) {
                    updateInfo.value = SettingState.NOT_REQUIRED.name
                    newValue = YES
                    oldValue = NO
                } else {
                    updateInfo.value = SettingState.REQUIRED.name
                    newValue = NO
                    oldValue = YES
                }
                viewModel.updateSettings(updateInfo)
                if (view.id == R.id.can_post_info) {
                    viewBinding.canPostCheckbox.toggle()
                }
                AnalyticsHelper2.logGroupSettingsClickEvent(PageReferrer(currentReferrer),
                        groupInfo, newValue, oldValue, Constants.ANYONE_CAN_POST, userProfile = myRole)
            }
            R.id.toolbar_back_button_container -> {
                handleBack()
            }
        }
    }

    override fun showLoginError() {
        //No deeplink to this screen. No need to handle login error
    }

    override fun onBackPressed() {
        handleBack()
    }

    override fun getLogTag(): String = LOG_TAG

    private fun setUpViews() {
        viewBinding.editGroupInfo.setOnClickListener {
            CommonNavigator.launchEditGroupActivity(this, groupInfo, SSO.getInstance().isLoggedIn
            (false), PageReferrer(currentReferrer))
        }
        viewBinding.canJoinInfo.setOnClickListener(this)
        viewBinding.canJoinCheckbox.setOnClickListener(this)
        viewBinding.canDiscoverInfo.setOnClickListener(this)
        viewBinding.canDiscoverCheckbox.setOnClickListener(this)
        viewBinding.canPostInfo.setOnClickListener(this)
        viewBinding.canPostCheckbox.setOnClickListener(this)
        viewBinding.actionbar.toolbarBackButtonContainer.setOnClickListener(this)
        viewModel.updateSettingsLiveData.observe(this, Observer {
            if (it.isSuccess) {
                groupInfo = it.getOrThrow()
                FontHelper.showCustomFontToast(this, CommonUtils.getString(R.string.group_setting_updated), Constants.TOAST_LENGTH_SHORT)
            } else {
                viewBinding.canJoinCheckbox.isChecked = (groupInfo.memberApproval == SettingState.NOT_REQUIRED)
                viewBinding.canDiscoverCheckbox.isChecked = (groupInfo.privacy == SocialPrivacy.PUBLIC)
                viewBinding.canPostCheckbox.isChecked = (groupInfo.postApproval == SettingState.NOT_REQUIRED)
                ErrorHelperUtils.showErrorSnackbar(it.exceptionOrNull(), viewBinding.root)
            }
        })

        viewModel.updateSettingStatusLiveData.observe(this, Observer { inProgress ->
            if (!inProgress) {
                //If a progress dialog is shown, it means a request was in progress and user pressed back
                progressDialog?.let { dialog ->
                    dialog.dismiss()
                    progressDialog = null
                    handleBack()
                }
            }
        })
        viewBinding.deleteGroupOption.setOnClickListener(this)
        viewBinding.leaveGroupOption.setOnClickListener(this)
        viewModel.readGroupInfoLiveData.observe(this, Observer {
            if (it.isSuccess) {
                it.getOrNull()?.let {
                    groupInfo = it
                }
            }
        })
    }

    private fun deleteGroup() {
        myUserId.userId?.let {
            viewModel.deleteGroupLiveData.observe(this, Observer { result ->
                if (result.isSuccess) {
                    finishAffinity()
                    CommonNavigator.launchFollowHome(this,
                            false,
                            null,
                            null,
                            PageReferrer(currentReferrer))
                } else {
                    ErrorHelperUtils.showErrorSnackbar(result.exceptionOrNull(), viewBinding.groupSettingsView)
                }
            })
            viewModel.deleteGroup(groupInfo.id, it)
            AnalyticsHelper2.logGroupSettingsClickEvent(PageReferrer(currentReferrer),
                    groupInfo, type = Constants.DELETE_GROUP_CONFIRM, userProfile = myRole)
        }
    }

    private fun leaveGroup() {
        myUserId.userId?.let {
            viewModel.leaveGroupLiveData.observe(this, Observer { result ->
                if (result.isSuccess) {
                    finishAffinity()
                    CommonNavigator.launchFollowHome(this,
                            false,
                            null,
                            null,
                            PageReferrer(currentReferrer))
                } else {
                    ErrorHelperUtils.showErrorSnackbar(result.exceptionOrNull(), viewBinding.groupSettingsView)
                }
            })
            viewModel.leaveGroup(groupInfo.id, it)
            AnalyticsHelper2.logGroupSettingsClickEvent(PageReferrer(currentReferrer),
                    groupInfo, type = Constants.LEAVE_GROUP_CONFIRM, userProfile = myRole)
        }
    }

    private fun handleBack() {
        if (viewModel.updateSettingStatusLiveData.value == true) {
            Logger.d(LOG_TAG, "Some update request in progress, wait with a dialog")
            if (progressDialog == null) {
                progressDialog = ProgressDialog(this)
            }
            progressDialog?.apply {
                isIndeterminate = true
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCanceledOnTouchOutside(false)
                setMessage(CommonUtils.getString(R.string.saving_group))
                show()
            }
            return
        } else {
            handleBack(true,referrerRaw)
        }
    }
}
