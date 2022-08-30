/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.group.viewmodel

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.R
import com.newshunt.appview.common.group.ui.activity.PhoneBookActivity
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.viewmodel.ClickDelegate
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.share.ShareContent
import com.newshunt.common.helper.share.ShareFactory
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.InvitationAppData
import com.newshunt.dataentity.model.entity.InvitationMedium
import com.newshunt.dataentity.model.entity.InvitationOption
import com.newshunt.dataentity.model.entity.InviteConfigWithGroupInfo
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.model.entity.PHONEBOOK_SEARCH_QUERY
import com.newshunt.dataentity.model.entity.SMS_BODY
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.helper.StoryShareUtil
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.sso.SSO
import javax.inject.Inject
import javax.inject.Named

/**
 * View model implementation for the invitation activity
 * <p>
 * Created by srikanth.ramaswamy on 09/27/2019.
 */
class GroupInvitationViewModel @Inject constructor(@Named("inviteMediatorUc")
                                                   private val inviteMediatorUC: MediatorUsecase<List<Member>, Int>,
                                                   @Named("readInviteConfigMediatorUC")
                                                   private val readInviteConfigMediatorUC: MediatorUsecase<GroupBaseInfo, InviteConfigWithGroupInfo>,
                                                   private val requestedGroupInfo: GroupBaseInfo) : ViewModel(), ClickDelegate {

    private var inviteConfigWithGroupInfo: InviteConfigWithGroupInfo? = null
    private val errorClickDelegate = ErrorClickDelegate(::fetchInviteConfig)
    private var pageReferrer: PageReferrer? = null

    val inviteConfigWithGroupInfoLD: LiveData<Result0<InviteConfigWithGroupInfo>> = Transformations.map(readInviteConfigMediatorUC.data()) {
        if (it.isSuccess) {
            inviteConfigWithGroupInfo = it.getOrNull()
        }
        it
    }

    fun setReferrer(referrer: PageReferrer?) {
        pageReferrer = referrer
    }

    val inviteConfigStatusLD by lazy {
        readInviteConfigMediatorUC.status()
    }

    val showContactPermission : MutableLiveData<Boolean> = MutableLiveData()

    init {
        fetchInviteConfig()
    }

    val invitationResultLiveData by lazy {
        inviteMediatorUC.data()
    }

    override fun onViewClick(view: View) {
        if (view.id == R.id.actionbar_back_button) {
            (view.context as? AppCompatActivity?)?.let {
                NavigationHelper.onBackPressed(it, -1)
            }
        }
    }

    override fun onViewClick(view: View, item: Any) {
        onViewClick(view, item, null)
    }

    override fun onViewClick(view: View, item: Any, args: Bundle?) {
        if (item is BaseError) {
            errorClickDelegate.onViewClick(view)
            return
        }

        (item as? InvitationMedium?)?.let { invitationMedium ->
            (view.context as? Activity?)?.let { activity ->
                inviteConfigWithGroupInfo?.groupInfo?.let { info ->
                    var type = Constants.EMPTY_STRING
                    when (invitationMedium.invitationOption) {
                        InvitationOption.GENERIC_SHARE -> {
                            handleGenericShare(info)
                        }
                        InvitationOption.COPY_LINK -> {
                            handleCopyLink(activity, info)
                            type = Constants.LINK
                        }
                        InvitationOption.PHONE_BOOK -> {

                            if (ContextCompat.checkSelfPermission(view.context, Manifest.permission
                                            .READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                                showContactPermission.postValue(true)
                            } else {
                                type = Constants.PHONEBOOK_CONTACTS
                                openPhonebook(view.context, info, null)
                            }
                        }
                        else -> {
                            type = invitationMedium.invitationAppData.name.toLowerCase()
                            handleAppShare(activity, info, invitationMedium.invitationAppData)
                        }
                    }
                    AnalyticsHelper2.logInviteOptionClicked(type)
                }
            }
        }
    }

    fun retryGuestLogin(context: Context) {
        errorClickDelegate.retryLogin(context)
    }


    fun fetchInviteConfig() {
        readInviteConfigMediatorUC.execute(requestedGroupInfo)
    }

    override fun onCleared() {
        readInviteConfigMediatorUC.dispose()
        inviteMediatorUC.dispose()
        super.onCleared()
    }

    fun invite(member: Member) {
        inviteMediatorUC.execute(listOf(member))
    }

    fun openPhonebook(context: Context, info: GroupInfo, queryString: String?) {
        Intent(context, PhoneBookActivity::class.java).apply {
            putExtra(SMS_BODY, StoryShareUtil.getShareableString(info.shareUrl, formatInviteMsg(), null, false, null))
            putExtra(PHONEBOOK_SEARCH_QUERY, queryString)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(this))
        }
    }

    private fun formatInviteMsg(): String? {
        return inviteConfigWithGroupInfo?.inviteConfig?.invitationMsg?.let {
            String.format(it, SSO.getUserName(), inviteConfigWithGroupInfo?.groupInfo?.name)
        }
    }

    private fun handleGenericShare(groupInfo: GroupInfo) {
        formatInviteMsg()?.let { inviteMsg ->
            val intent = StoryShareUtil.getShareIntent(groupInfo.shareUrl, inviteMsg)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
        }
    }

    private fun handleCopyLink(activity: Activity, groupInfo: GroupInfo) {
        (activity.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?)?.let { clipboardMgr ->
            clipboardMgr.setPrimaryClip(ClipData.newPlainText(InvitationOption.COPY_LINK.name, groupInfo.shareUrl))
            FontHelper.showCustomFontToast(activity, CommonUtils.getString(R.string.copy_to_clipboard), Toast.LENGTH_LONG)
        }
    }

    private fun handleAppShare(activity: Activity, groupInfo: GroupInfo, appData: InvitationAppData) {
        formatInviteMsg()?.let {
            val shareIntent = StoryShareUtil.buildBasicShareIntent()
            val shareContent = ShareContent().apply {
                this.title = it
                this.shareUrl = groupInfo.shareUrl
            }
            ShareFactory.getShareHelper(appData.pkgName, activity, shareIntent,
                    shareContent, false).share()
        }
    }
}

class GroupInvitationVMFactory @Inject constructor() : ViewModelProvider.Factory {
    @Inject
    lateinit var groupInvitationModel: GroupInvitationViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return groupInvitationModel as T
    }
}