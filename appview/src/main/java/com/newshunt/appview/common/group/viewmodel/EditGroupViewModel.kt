/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.group.viewmodel

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.group.model.usecase.EditGroupUsecase
import com.newshunt.appview.common.group.model.usecase.ImageUploadUsecase
import com.newshunt.appview.common.postcreation.view.helper.ImageFilePath
import com.newshunt.appview.common.profile.helper.HandleValidityHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.EditMode
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.INVALID_SIZE
import com.newshunt.dataentity.model.entity.NOT_FOUND
import com.newshunt.dataentity.model.entity.SUCCESS
import com.newshunt.dataentity.model.entity.UIResponseWrapper
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.usecase.UIWrapperUsecase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import javax.inject.Named

private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 //10MB
private const val TAG = "EditGroupViewModel"

/**
 * Handles :
 * - uploading group image
 * - validate text fields against length limits, chars
 * - validate group handle while create
 * - create/update group
 *
 * @author raunak.yadav
 */
class EditGroupViewModel @Inject constructor(private val imageUploadUsecase: ImageUploadUsecase,
                                             @Named("editGroupUsecase")
                                             private val editGroupUsecase: UIWrapperUsecase<Bundle, GroupInfo>,
                                             private val handleValidityHelper: HandleValidityHelper) : ViewModel() {

    private val disposables = CompositeDisposable()

    val groupInfoValidStatus = MutableLiveData<Boolean>()
    val imageValidStatus = MutableLiveData<UIResponseWrapper<String?>>()
    val editGroupLiveData = editGroupUsecase.data()
    val editGroupStatus = editGroupUsecase.status()

    fun saveGroup(imagePath:String?, info: GroupBaseInfo, mode: EditMode) {
        if(imagePath != null) {
            disposables.add(imageUploadUsecase.invoke(imagePath)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        info.coverImage = it.paths?.get(0)
                        Logger.d(TAG, "Save group $info")
                        editGroupUsecase.execute(bundleOf(EditGroupUsecase.B_GROUP_INFO to info,
                                EditGroupUsecase.B_EDIT_MODE to mode))
                    }, {
                        Logger.d(TAG, "Image upload failed ${it.message}")
                        //Should save group anyway?
                    }))
        } else {
            Logger.d(TAG, "Save group $info")
            editGroupUsecase.execute(bundleOf(EditGroupUsecase.B_GROUP_INFO to info,
                    EditGroupUsecase.B_EDIT_MODE to mode))
        }
    }

    fun validateHandle(handle: String): UIResponseWrapper<Int>? {
        return handleValidityHelper.validateHandle(handle)
    }

    /**
     * Validate an image against restrictions like size
     */
    fun validateImage(uri: Uri?) {
        disposables.add(Observable.fromCallable {
            if (uri == null) {
                return@fromCallable Constants.EMPTY_STRING
            }
            ImageFilePath.getPath(CommonUtils.getApplication(), uri, false, 0)
        }.subscribe({ path ->
            when {
                path == null || path.isBlank()-> imageValidStatus.postValue(
                    UIResponseWrapper(null, NOT_FOUND, null))
                File(path).length() > MAX_IMAGE_SIZE -> imageValidStatus.postValue(
                    UIResponseWrapper(null, INVALID_SIZE, null))
                else -> imageValidStatus.postValue(UIResponseWrapper(path, SUCCESS, null))
            }
        }, { imageValidStatus.postValue(null) }))
    }

    fun checkGroupInfoValid(groupBaseInfo: GroupBaseInfo?) = if (groupBaseInfo == null) {
        groupInfoValidStatus.postValue(false)
    } else if (groupBaseInfo.name.isNullOrBlank()) {
        groupInfoValidStatus.postValue(false)
    } else {
        groupInfoValidStatus.postValue(handleValidityHelper.isHandleValid(groupBaseInfo.handle))
    }

    fun handleAvailabilityLiveData() = handleValidityHelper.handleValidityLiveData

    override fun onCleared() {
        editGroupUsecase.dispose()
        handleValidityHelper.dispose()
        disposables.clear()
        super.onCleared()
    }

    fun setApprovedHandle(handle: String?) {
        handleValidityHelper.approvedHandle = handle
    }
}

class EditGroupVMFactory @Inject constructor() : ViewModelProvider.Factory {
    @Inject lateinit var editGroupViewModel: EditGroupViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return editGroupViewModel as T
    }
}