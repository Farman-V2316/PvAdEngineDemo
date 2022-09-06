/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper

import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.model.usecase.SyncLikeUsecase
import com.newshunt.common.model.usecase.ToggleLikeUsecase
import com.newshunt.dataentity.common.JsFollowAndDislikesResponse
import com.newshunt.dataentity.common.JsResponse
import com.newshunt.dataentity.common.JsUpdateLikeRequest
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.repo.FollowRepo
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.IsDislikeUsecase
import com.newshunt.news.model.usecase.IsFollowedUsecase
import com.newshunt.news.model.usecase.JsFollowAndDislikeUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import com.newshunt.news.model.usecase.toMediator2

/**
 * Helper class to be used in jscallbacks.
 *
 * @author satosh.dhanyamraju
 */
object JsCallbackHelper {
    private val toggleLikeUsecase by lazy {
        ToggleLikeUsecase(SocialDB.instance().interactionsDao(),
                SocialDB.instance().cpDao(), SyncLikeUsecase()).toMediator2()
    }
    private val LOG_TAG = "JsCallbackHelper"

    fun <T> transform(f: LiveData<Result0<T>>) = Transformations.map(f) {
        it.getOrNull()
    }

    @JvmStatic
    fun isFollowed(id: String): LiveData<JsResponse?> {
        val usecase = IsFollowedUsecase().toMediator2()
        usecase.execute(id)
        return transform(usecase.data())
    }


    @JvmStatic
    fun getFollowsAndDislikes(json: String): LiveData<JsFollowAndDislikesResponse?> {
        val usecase = JsFollowAndDislikeUsecase().toMediator2()
        usecase.execute(json)
        return transform(usecase.data())
    }

    @JvmStatic
    fun isDisliked(id: String): LiveData<JsResponse?> {
        val usecase = IsDislikeUsecase().toMediator2()
        usecase.execute(id)
        return transform(usecase.data())
    }

    @JvmStatic
    fun updateFollow(actionableEntity: ActionableEntity) {
        val usecase = ToggleFollowUseCase(FollowRepo(SocialDB.instance().followEntityDao())).toMediator2()
        usecase.execute(bundleOf(ToggleFollowUseCase.B_ACTION to FollowActionType.FOLLOW.name, ToggleFollowUseCase.B_FOLLOW_ENTITY to actionableEntity))
    }

    @JvmStatic
    fun updateLikeRequestFrom(json: String): Boolean {
        // validations
        val req = runCatching {
            requireNotNull(JsonUtils.fromJson(json, JsUpdateLikeRequest::class.java)).apply {
                requireNotNull(entityId)
                requireNotNull(entityType)
                requireNotNull(action)
                requireNotNull(actionToggle)
            }
        }.getOrNull() ?: run {
            Logger.e(LOG_TAG, "updateLikeRequestFrom : failed to parse $json")
            return false
        }
        val bundle = ToggleLikeUsecase.args(req.entityId, req.entityType, req.action)
        toggleLikeUsecase.execute(bundle)
        return true
    }

    @JvmStatic
    fun getPermissionStatus(permission: String): Boolean {
        //TODO: PANDA removed
//        var requestPermission: Permission? = null
//        try {
//            requestPermission = Permission.valueOf(permission)
//        } catch (rte: RuntimeException) {
//            Logger.e(LOG_TAG, rte.message)
//        }
//        if (requestPermission == Permission.INVALID) {
//            return false
//        }
//        requestPermission?.let {
//            try {
//                return CommonUtils.getApplication().checkSelfPermission(it.permission) == PackageManager.PERMISSION_GRANTED
//            } catch (rte: RuntimeException) {
//                Logger.e(LOG_TAG, rte.message)
//            }
//
//        }
//        return false
        return false
    }
}