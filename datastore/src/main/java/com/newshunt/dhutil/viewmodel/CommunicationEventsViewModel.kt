/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.news.model.usecase.CommunicationEventUsecase
import com.newshunt.news.model.usecase.toMediator2

/**
 * ViewModel implementation to read the communication API response from versioned DB
 * Created by srikanth.r on 12/28/21.
 */
class CommunicationEventsViewModel(application: Application) : AndroidViewModel(application) {
    private val communicationEventUsecase = CommunicationEventUsecase().toMediator2()
    val communicationLiveData = communicationEventUsecase.data().distinctUntilChanged()

    init {
        communicationEventUsecase.execute(Any())
    }
}