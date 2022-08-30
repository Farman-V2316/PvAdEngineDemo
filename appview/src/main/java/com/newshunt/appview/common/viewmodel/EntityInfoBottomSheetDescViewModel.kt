/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.viewmodel

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.dataentity.common.model.EntityNERBottomSheetWebResponse
import com.newshunt.news.model.usecase.MediatorUsecase
import javax.inject.Inject
import javax.inject.Named

/**
 * View Model for bottomsheet description webview for NERs and TPV profiles.
 * <p>
 * Created by aman.roy on 05/30/2022.
 */
class EntityInfoBottomSheetDescViewModel @Inject constructor(@Named("entityNERBottomSheetWebUsecase")
                                                            private val nerBottomSheetWebUsecase:MediatorUsecase<String, EntityNERBottomSheetWebResponse?>):ViewModel() {

    val webResData = Transformations.map(nerBottomSheetWebUsecase.data()) {
        if (it.isSuccess) {
            it.getOrNull()?.content
        } else {
            null
        }
    }

    fun fetchEntityBottomSheetWebInfo(url:String?) {
        url ?: return
        nerBottomSheetWebUsecase.execute(url)
    }

    override fun onCleared() {
        nerBottomSheetWebUsecase.dispose()
        super.onCleared()
    }

    class EntityInfoBottomSheetDescViewModelF @Inject constructor() : ViewModelProvider.Factory {
        @Inject
        lateinit var vm: EntityInfoBottomSheetDescViewModel

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return vm as T
        }
    }
}