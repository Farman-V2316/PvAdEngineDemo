/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.di

import com.newshunt.appview.common.group.model.usecase.InsertIntoGroupDaoUsecase
import com.newshunt.appview.common.ui.fragment.SearchCardsFragment
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.toMediator2
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * DI modules for SearchCardsFragment and its related classes
 * <p>
 * Created by srikanth.ramaswamy on 11/15/2019.
 */
@Component(modules = [SearchCardsModule::class])
interface SearchCardsComponent {
    fun inject(component: SearchCardsFragment)
}

@Module
class SearchCardsModule(private val searchUrl: String,
                        private val queryParam: String,
                        private val dynamicFeed: GeneralFeed) {
    @Provides
    fun insertIntoDynamicFeedUsecase(insertIntoGroupDaoUsecase: InsertIntoGroupDaoUsecase): MediatorUsecase<List<GeneralFeed>, List<String>> {
        return insertIntoGroupDaoUsecase.toMediator2()
    }

    @Provides
    @Named("searchUrl")
    fun searchUrl(): String {
        return searchUrl
    }

    @Provides
    @Named("queryParam")
    fun queryParam(): String {
        return queryParam
    }

    @Provides
    @Named("debounceDelayMs")
    fun delay(): Long {
        return 300
    }

    @Provides
    @Named("dynamicFeed")
    fun dynamicFeed(): GeneralFeed {
        return dynamicFeed
    }

    @Provides
    @Named("requestMethod")
    fun requestMethod(): String {
        return Constants.HTTP_POST
    }
}