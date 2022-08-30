/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.model.usecase.SearchCardsUsecase
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.news.model.usecase.MediatorUsecase
import javax.inject.Inject
import javax.inject.Named

/**
 * View model implementation to interact with the search cards usecases
 * <p>
 * Created by srikanth.ramaswamy on 11/15/2019.
 */
class SearchCardsViewModel @Inject constructor(private val insertIntoGeneralDaoUsecase: MediatorUsecase<List<GeneralFeed>, List<String>>,
                                               private val searchCardsUsecase: SearchCardsUsecase,
                                               @Named("dynamicFeed")
                                               private val dynamicFeed: GeneralFeed) : ViewModel() {

    val feedSetupLiveData = insertIntoGeneralDaoUsecase.data()

    fun setup(dynamicFeed: GeneralFeed) {
        insertIntoGeneralDaoUsecase.execute(listOf(dynamicFeed))
    }

    fun search(queryStr: String?) {
        searchCardsUsecase.invoke(queryStr)
    }
}

class SearchCardsViewModelF @Inject constructor() : ViewModelProvider.Factory {
    @Inject
    lateinit var viewModel: SearchCardsViewModel

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return viewModel as T
    }
}