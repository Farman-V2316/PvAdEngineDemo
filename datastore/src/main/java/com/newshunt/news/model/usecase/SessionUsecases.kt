package com.newshunt.news.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.dataentity.dhutil.analytics.SessionInfo
import com.newshunt.news.model.daos.SessionDao
import javax.inject.Inject

/**
 * Usecase implementation to count the total number of entries in history table from specified time
 * */
class QueryCurrentSessionUsecase @Inject constructor(private val sessionDao: SessionDao): MediatorUsecase<Unit, SessionInfo?> {

    private val liveData = MediatorLiveData<Result0<SessionInfo?>>()
    override fun execute(t: Unit): Boolean {
        liveData.addSource(sessionDao.getCurrentSessionInfoLivedata()) {
            liveData.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<SessionInfo?>> = liveData
}