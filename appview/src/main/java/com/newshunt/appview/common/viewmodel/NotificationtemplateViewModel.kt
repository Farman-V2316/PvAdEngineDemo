/*
 *  * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.utils.TemplateUsecase
import com.newshunt.dataentity.notification.InAppTemplateResponse
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.notification.model.service.NotificationTemplateServiceImpl

/**
 * Created by kajal.kumari on 22/04/22.
 */
class NotificationtemplateViewModel (private val templateUsecase: MediatorUsecase<Unit, InAppTemplateResponse>) : ViewModel() {
    val templateLiveData: LiveData<Result0<InAppTemplateResponse>>

    init {
        templateLiveData = templateUsecase.data()
    }

    fun getTemplateInfo() {
        templateUsecase.execute(Unit)
    }

    override fun onCleared() {
        templateUsecase.dispose()
        super.onCleared()
    }

    class NotificationTemplateViewModelF : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NotificationtemplateViewModel(TemplateUsecase(NotificationTemplateServiceImpl()).toMediator2()) as T
        }
    }
}
