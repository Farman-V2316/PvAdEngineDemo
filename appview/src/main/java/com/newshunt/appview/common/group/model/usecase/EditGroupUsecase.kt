/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.model.usecase

import android.media.ExifInterface
import android.os.Bundle
import com.newshunt.appview.common.group.mapGroupInfoResponse
import com.newshunt.appview.common.group.model.service.GroupService
import com.newshunt.appview.common.group.model.service.ImageUploadService
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.ApprovalTab
import com.newshunt.dataentity.model.entity.ChangeRolePostBody
import com.newshunt.dataentity.model.entity.EditMode
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.GroupLocations
import com.newshunt.dataentity.model.entity.ImageResponseBody
import com.newshunt.dataentity.model.entity.PendingApprovalsEntity
import com.newshunt.dataentity.model.entity.ReviewActionBody
import com.newshunt.dataentity.model.entity.ReviewItem
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dhutil.model.internal.service.ApprovalTabsInfoService
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.Usecase
import com.newshunt.sso.SSO
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import javax.inject.Inject
import javax.inject.Named

/**
 * @author raunak.yadav
 */
class EditGroupUsecase @Inject constructor(private val service: GroupService,
                                           private val insertGroupInfoUsecase: InsertGroupInfoUsecase) : BundleUsecase<ApiResponse<GroupInfo>> {

    override fun invoke(bundle: Bundle): Observable<ApiResponse<GroupInfo>> {
        val info = bundle.getSerializable(B_GROUP_INFO) as? GroupBaseInfo

        info ?: throw IllegalStateException("No group info passed for saving.")

        return when (bundle.getSerializable(B_EDIT_MODE)) {
            EditMode.CREATE -> chainInsertUsecase(info, service.create(info))
            EditMode.UPDATE -> chainInsertUsecase(info, service.update(info))
            else -> throw IllegalStateException("Edit mode missing")
        }
    }

    private fun chainInsertUsecase(info: GroupBaseInfo, apiResponseObservable: Observable<ApiResponse<GroupInfo>>)
            : Observable<ApiResponse<GroupInfo>> {
        return apiResponseObservable.flatMap { apiResponse ->
            if (apiResponse.data != null) {
                insertGroupInfoUsecase.invoke(mapGroupInfoResponse(apiResponse, info.userId))
                        .map { apiResponse }
            } else {
                Observable.just(apiResponse)
            }
        }
    }

    companion object {
        const val B_GROUP_INFO = "group_info"
        const val B_EDIT_MODE = "edit_mode"
    }
}

//Upload image
class ImageUploadUsecase @Inject constructor(val service: ImageUploadService) :
        Usecase<String?, ImageResponseBody> {

    override fun invoke(filePath: String?): Observable<ImageResponseBody> {
        if (filePath.isNullOrBlank()) {
            return Observable.just(ImageResponseBody(HTTP_NO_CONTENT, null, null))
        }
        val file = File(filePath)
        val exifI = ExifInterface(filePath)

        val orientation: Int = exifI.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        val rotation =  when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_90 -> 270
            else -> 0
        }

        val rotate = "${file.name}=${rotation}"
        val requestBody1 = RequestBody.create(MediaType.parse("multipart/form-data"), rotate)
        val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
        val part = MultipartBody.Part.createFormData("upload", file.name, requestBody)
        return service.uploadImage(requestBody1,part)
    }
}

/**
 * Usecase implementation to approve/decline an open item. Once the Review is successful,
 * refreshes the list by chaining fp usecase
 */
class ApprovalActionUsecase @Inject constructor(val service: GroupService,
                                                private val insertIntoApprovalsUsecase: InsertIntoApprovalsUsecase,
                                                @Named("userId")
                                                private val userId: String) : Usecase<ReviewActionBody, Boolean> {

    override fun invoke(postBody: ReviewActionBody): Observable<Boolean> {
        return service.reviewItem(postBody)
                .map {
                    ApiResponseUtils.throwErrorIfDataNull(it)
                    it.data
                }
                .flatMap {
                    insertIntoApprovalsUsecase.invoke(PendingApprovalsEntity(userId, it))
                }
                .map {
                    true
                }
    }
}

/**
 * Fetch Group Approval Tabs config from cache/Network.
 * Save the tab fetch info in group_feed table.
 *
 * @author raunak.yadav
 */
class GetApprovalTabsInfoUseCase @Inject constructor(private var service: ApprovalTabsInfoService,
                                                     private var insertIntoGroupDaoUsecase: InsertIntoGroupDaoUsecase)
    : Usecase<Unit, List<ApprovalTab>> {

    override fun invoke(input: Unit): Observable<List<ApprovalTab>> {
        return service.getTabsConfig()
                .flatMap {
                    val feeds = ArrayList<GeneralFeed>()
                    val finalTabs = ArrayList<ApprovalTab>()

                    it.tabs?.forEach { tab ->
                        tab.contentUrl ?: return@forEach
                        tab.tabType ?: return@forEach
                        tab.entityId = getPageIdFor(tab.tabType!!)
                        finalTabs.add(tab)
                        feeds.add(GeneralFeed(tab.entityId!!, tab.contentUrl!!, Constants.HTTP_GET, PageSection.GROUP.section))
                    }
                    insertIntoGroupDaoUsecase.invoke(feeds).map {
                        finalTabs
                    }
                }
    }

    private fun getPageIdFor(tabType: ReviewItem): String {
        return when (tabType) {
            ReviewItem.GROUP_INVITATION -> GroupLocations.G_A_I.name
            ReviewItem.GROUP_MEMBER -> GroupLocations.G_A_M.name
            ReviewItem.GROUP_POST -> GroupLocations.G_A_P.name
        }.plus(Constants.UNDERSCORE_CHARACTER).plus(SSO.getLoginResponse()?.userId ?: Constants.EMPTY_STRING)
    }
}

/**
 * Remove a user from a group
 *
 * @author raunak.yadav
 */
class RemoveUserUsecase @Inject constructor(var service: GroupService,
                                            private val insertGroupInfoUsecase: InsertGroupInfoUsecase)
    : Usecase<ChangeRolePostBody, ApiResponse<GroupInfo>> {

    override fun invoke(postBody: ChangeRolePostBody): Observable<ApiResponse<GroupInfo>> {
        return service.removeUser(postBody)
                .flatMap { info ->
                    val groupInfo = mapGroupInfoResponse(info, postBody.myUserId)
                    insertGroupInfoUsecase.invoke(groupInfo)
                            .map {
                                info
                            }
                }
    }
}


/**
 * Change role admin <-> member
 *
 * @author raunak.yadav
 */
class ChangeMemberRoleUsecase @Inject constructor(var service: GroupService)
    : Usecase<ChangeRolePostBody, ApiResponse<Any?>> {

    override fun invoke(postBody: ChangeRolePostBody): Observable<ApiResponse<Any?>> {
        return service.changeRole(postBody)
    }
}