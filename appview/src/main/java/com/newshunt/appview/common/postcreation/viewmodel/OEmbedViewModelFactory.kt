package com.newshunt.appview.common.postcreation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.news.model.usecase.OEmbedUsecase
import com.newshunt.news.model.usecase.toMediator2
import javax.inject.Inject

class OEmbedViewModelFactory @Inject constructor(private val oembedUseCase: OEmbedUsecase): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return OEmbedViewModel(oembedUseCase.toMediator2(true)) as T
    }
}