/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.newshunt.appview.common.profile.model.usecase.ValidateHandleUsecase
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.model.entity.UIResponseWrapper
import javax.inject.Inject

/**
 * Helper class to cache the responses from checkhandle API and manage the livedata for the
 * viewmodel
 * <p>
 * Created by srikanth.ramaswamy on 01/13/2020.
 */
private const val LOG_TAG = "HandleAvailabilityHelper"
class HandleValidityHelper @Inject constructor(private val validateHandleUsecase: ValidateHandleUsecase) {
    //Hold the handle and the corresponding response in a map to be re-used later
    val handleMap: MutableMap<String, UIResponseWrapper<Int>> = HashMap()
    var approvedHandle: String?= null

    val handleValidityLiveData: LiveData<Map<String, UIResponseWrapper<Int>>> = Transformations.map(validateHandleUsecase.data()) {
        if (it.isSuccess) {
            it.getOrNull()?.let { handleAvailabilityUiResponseWrapper ->
                handleMap[handleAvailabilityUiResponseWrapper.handle] = handleAvailabilityUiResponseWrapper.uiResponseWrapper
            }
        }
        handleMap
    }

    /**
     * Checks if the response is already available in the cache else calls upon the use case
     */
    fun validateHandle(handle: String): UIResponseWrapper<Int>? {
        handleMap[handle]?.let {
            Logger.d(LOG_TAG, "Returning cached response for $handle")
            return it
        }
        validateHandleUsecase.execute(handle)
        return null
    }

    fun dispose() {
        validateHandleUsecase.dispose()
    }

    fun isHandleValid(handle: String?): Boolean {
        return handle?.let {
            handle == approvedHandle || it.isNotEmpty() && handleMap[it]?.response != null
        } ?: false
    }
}