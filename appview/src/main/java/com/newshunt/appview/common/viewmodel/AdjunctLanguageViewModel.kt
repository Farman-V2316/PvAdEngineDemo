/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.model.usecase.AdjunctLanguageUsecase
import com.newshunt.dataentity.common.model.AdjunctLangResponse
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.onboarding.model.internal.service.AdjunctLangServiceImpl
/**
 * Adjunct language viewmodel which fetches static config object.
 * @author aman.roy
 */
class AdjunctLanguageViewModel (private val adjunctLanguageUsecase: MediatorUsecase<Unit,AdjunctLangResponse>) : ViewModel() {
    val adjunctResponseLiveData: LiveData<Result0<AdjunctLangResponse>>

    init {
        adjunctResponseLiveData = adjunctLanguageUsecase.data()
    }

    fun getAdjunctLanguageInfo() {
        adjunctLanguageUsecase.execute(Unit)
    }

    private fun getAdjunctLanguageLiveData():LiveData<Result0<AdjunctLangResponse>> {
        return adjunctResponseLiveData
    }

    override fun onCleared() {
        adjunctLanguageUsecase.dispose()
        super.onCleared()
    }

    class AdjunctLanguageViewModelF : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AdjunctLanguageViewModel(AdjunctLanguageUsecase(AdjunctLangServiceImpl()).toMediator2()) as T
        }
    }
}
