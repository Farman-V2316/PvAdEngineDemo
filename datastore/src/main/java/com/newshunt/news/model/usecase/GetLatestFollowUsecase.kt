/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.news.model.daos.FollowEntityDao
import javax.inject.Inject

/**
 * To show following snackbar
 *
 * @author satosh.dhanyamraju
 */
class GetLatestFollowUsecase @Inject constructor(val followEntityDao: FollowEntityDao) : MediatorUsecase<Bundle, List<FollowSyncEntity>> {
    private val _data = MediatorLiveData<Result0<List<FollowSyncEntity>>>()
    private lateinit var currentSource : LiveData<List<FollowSyncEntity>>

    override fun execute(t: Bundle): Boolean {

        val time = t.getLong(Constants.BUNDLE_CREATION_TIME, -1L)
        if (time == -1L) {
            Logger.e(TAG, "${Constants.BUNDLE_CREATION_TIME} missing")
            return false
        }

        val newSource = followEntityDao.getFollowedNames(time).distinctUntilChanged()
        if (::currentSource.isInitialized) {
            _data.removeSource(currentSource)
        }

        _data.addSource(newSource) {
            val list = it?: emptyList<FollowSyncEntity>()
            _data.value = Result0.success(list)
        }
        currentSource = newSource
        return true
    }

    override fun data(): LiveData<Result0<List<FollowSyncEntity>>> = _data

    companion object {
        private const val TAG = "GetLatestFollowUsecase"
    }
}