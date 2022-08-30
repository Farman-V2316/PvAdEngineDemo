/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.Intent.EXTRA_MIME_TYPES
import android.database.Cursor
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.newshunt.appview.common.group.model.apis.GroupAPI
import com.newshunt.appview.common.group.model.service.GroupServiceImpl
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.common.asset.ApprovalCounts
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.GroupLocations
import com.newshunt.dataentity.model.entity.ReviewItem
import com.newshunt.dataentity.model.entity.UIResponseWrapper
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.usecase.Usecase
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import io.reactivex.disposables.CompositeDisposable

/**
 * TODO(raunak): class for temp code now. will move to proper utils later.
 */

fun createGroupApi(): GroupAPI {
    val groupsBaseUrl = NewsBaseUrlContainer.getGroupsBaseUrl()
    return RestAdapterContainer.getInstance().getRestAdapter(groupsBaseUrl,
        Priority.PRIORITY_HIGHEST,
        null, NewsListErrorResponseInterceptor(), HTTP401Interceptor())
        .create(GroupAPI::class.java)
}

fun createGatewayApi(): GroupAPI {
    val gatewayUrl = CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationSecureUrl())
    return RestAdapterContainer.getInstance().getRestAdapter(gatewayUrl,
            Priority.PRIORITY_HIGHEST,
            null,
            NewsListErrorResponseInterceptor(), HTTP401Interceptor())
            .create(GroupAPI::class.java)
}

fun createGroupService() = GroupServiceImpl(createGroupApi(), createGatewayApi(), AppUserPreferenceUtils.getUserNavigationLanguage())

fun getImagePickerIntent(): Intent {
    val intent = Intent(ACTION_PICK)
    intent.type = "image/*"
    val mimeTypes = arrayOf("image/jpeg", "image/png")
    intent.putExtra(EXTRA_MIME_TYPES, mimeTypes)
    return intent
}


fun getFilePath(context: Context, uri: Uri): String? {
    if ("content".equals(uri.scheme!!, ignoreCase = true)) {
        return if ("com.google.android.apps.photos.content" == uri.authority) {
            uri.lastPathSegment
        } else getDataColumn(context, uri, null, null)

    } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
        return uri.path
    }
    return ""
}

private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs:
Array<String>?): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

fun buildLocationForGroupDao(location: GroupLocations, groupId: String): String = location.name.plus(Constants.UNDERSCORE_CHARACTER).plus(groupId)

fun mapGroupInfoResponse(apiResponse: ApiResponse<GroupInfo>, requestedUserId: String): GroupInfo {
    ApiResponseUtils.throwErrorIfDataNull(apiResponse)
    apiResponse.data.userId = requestedUserId
    return apiResponse.data
}

fun getPreferredApprovalTab(item: CommonAsset): ReviewItem? {
    val entityConfig = listOf(item.i_counts()?.MEMBER_APPROVALS, item.i_counts()?.POST_APPROVALS, item.i_counts()?.INVITES)
            .filterNotNull()
            .find { it.value != Constants.ZERO_STRING && !CommonUtils.isEmpty(it.value) }
    return when (entityConfig) {
        item.i_counts()?.MEMBER_APPROVALS -> {
            ReviewItem.GROUP_MEMBER
        }
        item.i_counts()?.POST_APPROVALS -> {
            ReviewItem.GROUP_POST
        }
        item.i_counts()?.INVITES -> {
            ReviewItem.GROUP_INVITATION
        }
        else -> {
            null
        }
    }
}

fun getPreferredApprovalTab(item: ApprovalCounts): ReviewItem? {
    val entityConfig = listOf(item.INVITES, item.POST_APPROVALS, item.MEMBER_APPROVALS)
            .filterNotNull()
            .find { it.value != Constants.ZERO_STRING && !CommonUtils.isEmpty(it.value) }
    return when (entityConfig) {
        item.MEMBER_APPROVALS -> {
            ReviewItem.GROUP_MEMBER
        }
        item.POST_APPROVALS -> {
            ReviewItem.GROUP_POST
        }
        item.INVITES -> {
            ReviewItem.GROUP_INVITATION
        }
        else -> {
            null
        }
    }
}