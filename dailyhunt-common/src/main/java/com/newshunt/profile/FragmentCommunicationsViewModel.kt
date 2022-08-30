/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.profile

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.newshunt.common.helper.common.Constants

/**
 * A view model to host multiple livedata objects to help communicate between different fragments
 *
 * Created by srikanth.ramaswamy on 07/01/2019.
 */
class FragmentCommunicationsViewModel: ViewModel() {
    val fragmentCommunicationLiveData: MutableLiveData<FragmentCommunicationEvent> by lazy {
        MutableLiveData<FragmentCommunicationEvent>()
    }
}

data class FragmentCommunicationEvent(val hostId: Int,
                                      val anyEnum: Any,
                                      val useCase: String? = Constants.EMPTY_STRING,
                                      val arguments: Bundle? = null)

