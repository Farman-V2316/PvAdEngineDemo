/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.news.model.daos.FetchDao
import javax.inject.Inject
import javax.inject.Named


/**
 * use case implementation to read the FeedPage View and fetch the PaqeEntity
 * <p>
 * Created by srikanth.ramaswamy on 11/04/2019.
 */
class CurrentPageInfoUsecase @Inject constructor(@Named("entityId") private val entityId: String,
                                                 @Named("section") private val section: String,
                                                 private val fetchDao: FetchDao) : MediatorUsecase<Unit, FeedPage?> {
    private val liveData = MediatorLiveData<Result0<FeedPage?>>()
    override fun execute(t: Unit): Boolean {
        liveData.addSource(fetchDao.LiveDataLookupPage(entityId, section)) {
            liveData.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<FeedPage?>> = liveData
}