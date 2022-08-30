/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import android.os.Bundle
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.model.entity.InAppUpdatesEntity
import com.newshunt.news.model.daos.InAppUpdatesDao
import io.reactivex.Observable
import javax.inject.Inject

/**
 * All usecases related to in app updates can be implemented in this file
 *
 * Created by srikanth.ramaswamy on 03/01/2021.
 */

/**
 * Usecase implementation to query the in app updates prompt table
 */
class QueryInAppUpdatePromptsUsecase @Inject constructor(private val inAppUpdatesDao: InAppUpdatesDao) : MediatorUsecase<Unit, List<InAppUpdatesEntity>> {

    private val liveData = MediatorLiveData<Result0<List<InAppUpdatesEntity>>>()
    override fun execute(t: Unit): Boolean {
        liveData.addSource(inAppUpdatesDao.queryUpdatePromptsData()) {
            liveData.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<List<InAppUpdatesEntity>>> = liveData
}

/**
 * Usecase implementation to update the in app update prompt in the DB
 */
const val BUNDLE_IN_APP_CONFIG_ENTITIES = "dbEntities"
const val BUNDLE_AVAILABLE_APP_VERSION = "availableAppVersion"

class IncrementUpdatePromptUsecase @Inject constructor(private val inAppUpdatesDao: InAppUpdatesDao) : BundleUsecase<Unit> {
    private val logTag = "IncrementUpdatePromptUsecase"
    override fun invoke(p1: Bundle): Observable<Unit> {
        return Observable.fromCallable {
            (p1.getSerializable(BUNDLE_IN_APP_CONFIG_ENTITIES) as? List<InAppUpdatesEntity>)?.let { dbEntities ->
                val availableAppVersion = p1.getInt(BUNDLE_AVAILABLE_APP_VERSION)

                //If an entry exists in the DB for this version, update it else insert a new entry
                val dbEntity = dbEntities.firstOrNull {
                    it.availableVersion == availableAppVersion
                }?.let {
                    it.copy(it.availableVersion,
                            it.promptShownCount + 1,
                            SystemClock.elapsedRealtime())
                } ?: InAppUpdatesEntity(availableAppVersion,
                        1,
                        SystemClock.elapsedRealtime())
                inAppUpdatesDao.insReplace(dbEntity)
                Logger.d(logTag, "inserted item $dbEntity to DB")
            } ?: Logger.e(logTag, "List of DB entities is mandatory")
        }
    }
}