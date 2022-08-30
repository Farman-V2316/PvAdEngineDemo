/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.newshunt.appview.common.group.model.usecase.ImageUploadUsecase
import com.newshunt.appview.common.postcreation.view.helper.ImageFilePath
import com.newshunt.appview.common.profile.helper.HandleValidityHelper
import com.newshunt.appview.common.profile.model.internal.service.ProfileService
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.INVALID_SIZE
import com.newshunt.dataentity.model.entity.MyProfile
import com.newshunt.dataentity.model.entity.NOT_FOUND
import com.newshunt.dataentity.model.entity.SUCCESS
import com.newshunt.dataentity.model.entity.UIResponseWrapper
import com.newshunt.dataentity.model.entity.UpdateProfileBody
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.sso.model.entity.LoginResponse
import com.newshunt.sso.model.entity.SSOResult
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

/**
 * ViewModel implementation for Edit Profile Activity.
 * <p>
 * Created by srikanth.ramaswamy on 10/21/2019.
 */

private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 //10MB

class EditProfileViewModel @Inject constructor(private val profileService: ProfileService,
                                               private val imageUploadUsecase: ImageUploadUsecase,
                                               private val handleAvailabilityHelper: HandleValidityHelper) : ViewModel() {
    private val disposables = CompositeDisposable()

    val imageValidStatus = MutableLiveData<UIResponseWrapper<String?>>()

    val myProfileLiveData by lazy {
        MutableLiveData<Result<MyProfile>>()
    }

    val updatedProfileLiveData by lazy {
        MutableLiveData<Result<UIResponseWrapper<MyProfile>>>()
    }


    val profileValidation by lazy {
        MutableLiveData<Boolean>()
    }

    override fun onCleared() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        handleAvailabilityHelper.dispose()
        super.onCleared()
    }

    fun fetchMyProfile(appLang: String) {
        disposables.add(profileService.fetchMyProfile(appLang)
                .subscribeOn(Schedulers.io())
                .map {
                    it.uiLocation = it.location?.let { locStr ->
                        JsonUtils.fromJson(locStr, PostCurrentPlace::class.java)
                    }
                    it
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    myProfileLiveData.postValue(Result.success(it))
                }, {
                    myProfileLiveData.postValue(Result.failure(ApiResponseOperator.getError(it)))
                }))
    }

    private fun updateMyProfile(postBody: UpdateProfileBody, appLang: String) {
        disposables.add(profileService.updateMyProfile(appLang, postBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    //If success, data is not null. So, error code and message are null
                    val uiResponseWrapper: UIResponseWrapper<MyProfile> = if (it.data != null) {
                        UIResponseWrapper(it.data, null, null)
                    } else {
                        //Error case, data is null and error code and message are passed to UI
                        UIResponseWrapper(null, it.status?.code, it.status?.message)
                    }

                    if (it.data != null) {
                        BusProvider.postOnUIBus(LoginResponse(SSOResult.SUCCESS, it.data))
                    }
                    updatedProfileLiveData.postValue(Result.success(uiResponseWrapper))
                }, {
                    updatedProfileLiveData.postValue(Result.failure(ApiResponseOperator.getError(it)))
                }))
    }

    fun updateProfile(imagePath: String?, postBody: UpdateProfileBody, appLang: String) {
        disposables.add(imageUploadUsecase.invoke(imagePath)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    postBody.profileImage = it.paths?.get(0)
                    updateMyProfile(postBody, appLang)
                }, {
                    updatedProfileLiveData.postValue(Result.failure(ApiResponseOperator.getError(it)))
                }))
    }


    fun validateImage(uri: Uri?) {
        disposables.add(Observable.fromCallable {
            if (uri == null) {
                return@fromCallable Constants.EMPTY_STRING
            }
            ImageFilePath.getPath(CommonUtils.getApplication(), uri, false, 0)
        }.subscribe({ path ->
            when {
                path == null || path.isBlank()  -> imageValidStatus.postValue(
                        UIResponseWrapper(null, NOT_FOUND, null))
                File(path).length() > MAX_IMAGE_SIZE -> imageValidStatus.postValue(
                        UIResponseWrapper(null, INVALID_SIZE, null))
                else -> imageValidStatus.postValue(UIResponseWrapper(path, SUCCESS, null))
            }
        }, { imageValidStatus.postValue(null) }))
    }

    fun checkProfileValid(myProfile: UserBaseProfile?) = if (myProfile == null) {
        profileValidation.postValue(false)
    } else if (myProfile.name.isNullOrEmpty()) {
        profileValidation.postValue(false)
    } else {
        profileValidation.postValue(handleAvailabilityHelper.isHandleValid(myProfile.handle))
    }

    fun handleAvailabilityLiveData() = handleAvailabilityHelper.handleValidityLiveData

    fun validateHandle(handle: String): UIResponseWrapper<Int>? {
        return handleAvailabilityHelper.validateHandle(handle)
    }

    fun setApprovedHandle(approvedHandle: String?) {
        handleAvailabilityHelper.approvedHandle = approvedHandle
    }
}
