/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.appview.common.group.model.usecase.ReadPendingApprovalCountsUsecase
import com.newshunt.appview.common.profile.model.internal.service.ProfileService
import com.newshunt.appview.common.profile.model.usecase.QueryBookmarksUsecase
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiResponseSource
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.EntityType
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.model.entity.*
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dataentity.social.entity.MenuL1
import com.newshunt.dataentity.social.entity.MenuL1Id
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.BaseErrorUtils
import com.newshunt.news.model.usecase.BlockedUserByUserIdUseCase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

/**
 * ViewModel implementation for Profile Activity.
 * <p>
 * Created by srikanth.ramaswamy on 06/26/2019.
 */

private const val LOG_TAG = "ProfileViewModel"

class ProfileViewModel @Inject constructor(private val profileService: ProfileService,
                                           @Named("insertIntoGroupMediatorUC")
                                           private val insertIntoGroupMediatorUC: MediatorUsecase<List<GeneralFeed>, List<String>>,
                                           @Named("fetchEntityMediatorUC")
                                           private val fetchEntityUsecase: MediatorUsecase<String, List<FollowSyncEntity?>>,
                                           @Named("toggleFollowMediatorUC")
                                           private val toggleFollowMediatorUC: MediatorUsecase<Bundle, Boolean>,
                                           private val deleteInteractionMediatorUC: MediatorUsecase<Boolean, Int>,
                                           private val undoDeleteInteractionMediatorUC: MediatorUsecase<Unit,Boolean>,
                                           @Named("syncPendingApprovalsMediatorUC")
                                           private val syncPendingApprovalsMediatorUC: MediatorUsecase<String, Boolean>,
                                           private val readPendingApprovalCountsMediatorUC: ReadPendingApprovalCountsUsecase,
                                           private val queryBookmarksMediatorUC: QueryBookmarksUsecase,
                                           private val blockedUserByUserIdUseCase: BlockedUserByUserIdUseCase) : ViewModel() {
    private val disposables = CompositeDisposable()

    val userProfileLiveData by lazy {
        MutableLiveData<Result<UserProfile>>()
    }

    val deleteActivitiesLiveData by lazy {
        deleteInteractionMediatorUC.data()
    }

    val profileFollowLiveData by lazy {
        fetchEntityUsecase.data()
    }

    val readPendingApprovalLD by lazy {
        readPendingApprovalCountsMediatorUC.data()
    }

    val bookmarksLD by lazy {
        queryBookmarksMediatorUC.data()
    }

    val blockLiveData by lazy {
        blockedUserByUserIdUseCase.data()
    }

    private var fetchProfileStatus: CachedApiResponseSource? = null

    fun fetchProfile(userId: String?,
                     userHandle: String?,
                     appLang: String,
                     profileImg: String?,
                     name: String?,
                     myUserId: ProfileUserIdInfo) {
        val postBody = ProfileBaseAPIBody(userId, appLang, name, profileImg, userHandle)
        disposables.add(profileService.fetchUserProfile(postBody, myUserId)
                .subscribeOn(Schedulers.io())
                .map { apiResponse ->
                    consumeResponse(apiResponse)
                }
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribe({
                    handleProfileBaseAPIResponse(it)
                }, {
                    Logger.e(LOG_TAG, "fetchProfile onError: ${it.message}, fetchProfileStatus: $fetchProfileStatus")
                    if (it is BaseError && it.message == Constants.NOT_FOUND_IN_CACHE) {
                        return@subscribe
                    }
                    if (fetchProfileStatus == null) {
                        userProfileLiveData.value = Result.failure(ApiResponseOperator.getError(BaseErrorUtils.extractNetworkError(it)))
                    }
                }))
    }

    fun deleteUserActivities(clearAll: Boolean) {
        deleteInteractionMediatorUC.execute(clearAll)
    }

    fun undoDeleteUserActivities() {
        undoDeleteInteractionMediatorUC.execute(Unit)
    }

    fun fetchFollowState(renderedProfile: UserProfile) {
        fetchEntityUsecase.execute(renderedProfile.userId)
    }

    fun updateContentUrl(dynamicFeed: GeneralFeed) {
        insertIntoGroupMediatorUC.execute(listOf(dynamicFeed))
    }

    fun fetchBlockState(renderedProfile: UserProfile){
        blockedUserByUserIdUseCase.execute(renderedProfile.userId)
    }

    fun toggleFollow(renderedProfile: UserProfile) {
        ActionableEntity(entityId = renderedProfile.userId,
                entityType = renderedProfile.entityType ?: EntityType.SOURCE.name,
                entityImageUrl = renderedProfile.profileImage,
                iconUrl = renderedProfile.profileImage,
                entitySubType = renderedProfile.entitySubType,
                handle = renderedProfile.handle,
                displayName = renderedProfile.name,
                deeplinkUrl = renderedProfile.profileShareUrl
        ).apply {
            AnalyticsHelper2.logFollowButtonClickEvent(this, PageReferrer(NhGenericReferrer.PROFILE), renderedProfile.isFollowing.not(), PageSection.PROFILE.section)
            toggleFollowMediatorUC.execute(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to this))
        }
    }

    fun toggleBlock(renderedProfile: UserProfile) {
        ActionableEntity(entityId = renderedProfile.userId,
            entityType = renderedProfile.entityType ?: EntityType.SOURCE.name,
            entityImageUrl = renderedProfile.profileImage,
            iconUrl = renderedProfile.profileImage,
            entitySubType = renderedProfile.entitySubType,
            handle = renderedProfile.handle,
            displayName = renderedProfile.name,
            deeplinkUrl = renderedProfile.profileShareUrl
        ).apply {
            DialogAnalyticsHelper.logDialogBoxActionEvent(MenuL1Id.L1_BLOCK_SOURCE.name, PageReferrer(NhGenericReferrer.PROFILE), DialogAnalyticsHelper.DIALOG_ACTION_OK, NhAnalyticsEventSection.PROFILE, MemberRole.NONE)
            toggleFollowMediatorUC.execute(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to this, ToggleFollowUseCase.B_ACTION to FollowActionType.BLOCK.name))
        }
    }

    fun syncApprovalCounts(userId: String) {
        readPendingApprovalCountsMediatorUC.execute(userId)
        syncPendingApprovalsMediatorUC.execute(userId)
    }

    fun fetchBookmarks() {
        queryBookmarksMediatorUC.execute(BookMarkAction.ADD)
    }

    override fun onCleared() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        insertIntoGroupMediatorUC.dispose()
        fetchEntityUsecase.dispose()
        toggleFollowMediatorUC.dispose()
        deleteInteractionMediatorUC.dispose()
        undoDeleteInteractionMediatorUC.dispose()
        syncPendingApprovalsMediatorUC.dispose()
        readPendingApprovalCountsMediatorUC.dispose()
        blockedUserByUserIdUseCase.dispose()
        super.onCleared()
    }

    private fun handleProfileBaseAPIResponse(apiResponse: ApiResponse<UserProfile>) {
        apiResponse.cachedApiResponseSource ?: return

        var changeFetchStatus = true
        when (fetchProfileStatus) {
            null -> {
                //Getting this response for first time
                Logger.d(LOG_TAG, "Fresh response from ${apiResponse.cachedApiResponseSource}")
                userProfileLiveData.value = Result.success(apiResponse.data)
            }
            CachedApiResponseSource.DISK_CACHE -> {
                if (isProfileResponseChanged(apiResponse.data)) {
                    //Update the live data and UI only if response changed
                    Logger.d(LOG_TAG, "Received response from ${apiResponse.cachedApiResponseSource}")
                } else {
                    changeFetchStatus = false
                    Logger.d(LOG_TAG, "Same response cachedApiResponseSource: ${apiResponse.cachedApiResponseSource}, fetchProfileStatus: $fetchProfileStatus")
                }
                userProfileLiveData.value = Result.success(apiResponse.data)
            }
            CachedApiResponseSource.NETWORK -> {
                if (apiResponse.cachedApiResponseSource == CachedApiResponseSource.NETWORK && isProfileResponseChanged(apiResponse.data)) {
                    //Update the live data and UI only if response changed
                    Logger.d(LOG_TAG, "Received response from ${apiResponse.cachedApiResponseSource}")
                } else {
                    changeFetchStatus = false
                    Logger.d(LOG_TAG, "Same response cachedApiResponseSource: ${apiResponse.cachedApiResponseSource}, fetchProfileStatus: $fetchProfileStatus")
                }
                userProfileLiveData.value = Result.success(apiResponse.data)
            }
        }
        if (changeFetchStatus) {
            fetchProfileStatus = apiResponse.cachedApiResponseSource
        }
    }

    private fun isProfileResponseChanged(response: UserProfile): Boolean {
        if (userProfileLiveData.value == null) {
            return true
        }
        if ((userProfileLiveData.value as Result<UserProfile>).isSuccess) {
            val prevResponse = (userProfileLiveData.value as Result<UserProfile>).getOrNull()
            return prevResponse != response

        }
        return true
    }

    private fun consumeResponse(apiResponse: ApiResponse<UserProfile>): ApiResponse<UserProfile> {
        apiResponse.data?.uiLocation = apiResponse.data?.location?.let {
            JsonUtils.fromJson(it, PostCurrentPlace::class.java)
        }
        return apiResponse
    }
}