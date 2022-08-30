/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.group.model.usecase.ChangeMemberRoleUsecase
import com.newshunt.appview.common.group.model.usecase.InsertIntoGroupDaoUsecase
import com.newshunt.appview.common.group.model.usecase.RemoveUserUsecase
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.model.entity.ChangeRolePostBody
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.MemberRole
import com.newshunt.dataentity.social.entity.MenuL1Id
import com.newshunt.dataentity.social.entity.MenuPayLoad2
import com.newshunt.deeplink.navigator.NhBrowserNavigator
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.model.usecase.UIWrapperUsecase
import com.newshunt.news.model.usecase.toUIWrapper
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import com.newshunt.sso.SSO
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Named

const val MEMBERS_FP_ENDPOINT = "groups/members/list"
const val MEMBERS_FP_GRPID_QUERY_KEY = "groupId"
private const val TAG = "MemberListViewModel"

/**
 * Handles
 * - Saving fetch url in db for member list
 * - Member 3-dot menu options
 *
 * @author raunak.yadav
 */
class MemberListViewModel @Inject constructor(private val groupId: String,
                                              private val removeUserUsecase: UIWrapperUsecase<ChangeRolePostBody, GroupInfo>,
                                              private val changeMemberRoleUsecase: UIWrapperUsecase<ChangeRolePostBody, Any?>) : ViewModel() {

    val roleChangeLiveData = changeMemberRoleUsecase.data()
    val userRemovalLiveData = removeUserUsecase.data()
    var pageReferrer: PageReferrer? = null

    fun removeUser(memberId: String) {
        SSO.getInstance().userDetails.userID?.let {
            removeUserUsecase.execute(ChangeRolePostBody(memberId, groupId, MemberRole.NONE, it))
        }
    }

    fun changeRole(userId: String, newRole: MemberRole) {
        changeMemberRoleUsecase.execute(ChangeRolePostBody(userId, groupId, newRole))
    }

    fun setReferrer(referrer: PageReferrer?) {
        pageReferrer = referrer
    }

    override fun onCleared() {
        removeUserUsecase.dispose()
        changeMemberRoleUsecase.dispose()
        super.onCleared()
    }

    fun reportUser(groupInfo: GroupInfo) {
        val menuPayLoad = ToMenuPayLoad2(groupInfo)
        val intent = NhBrowserNavigator.getTargetIntent().apply {
            putExtra(DailyhuntConstants.MENU_PAYLOAD, menuPayLoad)
            putExtra(DailyhuntConstants.URL_STR, NewsBaseUrlContainer.getReportGroupUrl())
            putExtra(Constants.VALIDATE_DEEPLINK, false)
        }
        NavigationHelper.navigationLiveData.value = NavigationEvent(intent)
    }

    fun ToMenuPayLoad2(groupInfo: GroupInfo) : MenuPayLoad2? {
        val map = HashMap<String, Any?>()
        try {
            map[NhAnalyticsAppEventParam.CLIENT_ID.getName()] = ClientInfoHelper.getClientId()
            map[AnalyticsParam.ITEM_ID.getName()] = groupInfo.id
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
        return MenuPayLoad2(groupId = groupInfo.id, option = MenuL1Id.L1_REPORT.name, eventParam = params)
    }

    class Factory @Inject constructor(@Named("groupId") private val groupId: String,
                                      private val insertIntoGroupDaoUsecase:
                                      InsertIntoGroupDaoUsecase,
                                      private val removeUserUsecase: RemoveUserUsecase,
                                      private val changeMemberRoleUsecase: ChangeMemberRoleUsecase)
        : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MemberListViewModel(groupId, removeUserUsecase.toUIWrapper(), changeMemberRoleUsecase.toUIWrapper()) as T
        }
    }
}

private const val LOG_TAG = "MemberListViewModel"