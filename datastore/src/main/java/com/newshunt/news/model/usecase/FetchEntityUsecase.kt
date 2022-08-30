/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.news.model.repo.FollowRepo
import javax.inject.Inject

/**
 * use case implementation to get a followable entity from Follow repo
 * <p>
 * Created by srikanth.ramaswamy on 10/31/2019.
 */
class FetchEntityUsecase @Inject constructor(private val followRepo: FollowRepo) : MediatorUsecase<String, List<FollowSyncEntity?>> {

    private val _data = MediatorLiveData<Result0<List<FollowSyncEntity?>>>()

    override fun execute(t: String): Boolean {
        _data.addSource(followRepo.fetchEntity(t)) {
            _data.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<List<FollowSyncEntity?>>> {
        return _data
    }
}