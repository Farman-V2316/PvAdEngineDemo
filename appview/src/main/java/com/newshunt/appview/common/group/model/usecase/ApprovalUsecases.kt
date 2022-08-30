/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.group.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.appview.common.group.model.service.GroupService
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.dataentity.model.entity.PendingApprovalsEntity
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.news.model.daos.PendingApprovalsDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import javax.inject.Inject

/**
 * All Approval usecases to be defined here
 *
 * <p>
 * Created by srikanth.ramaswamy on 12/07/2019.
 */

/**
 * Usecase implementation to insert into approvals table
 */
class InsertIntoApprovalsUsecase @Inject constructor(private val approvalsDao: PendingApprovalsDao,
                                                     private val postDao: PostDao) : Usecase<PendingApprovalsEntity, Boolean> {
    override fun invoke(pendingApprovals: PendingApprovalsEntity): Observable<Boolean> {
        return Observable.fromCallable {
            approvalsDao.insert(pendingApprovals, postDao)
        }.map {
            true
        }
    }
}

/**
 * Usecase implementation to read the pending approvals from approvals table
 */
class ReadPendingApprovalCountsUsecase @Inject constructor(private val approvalsDao:
                                                           PendingApprovalsDao) : MediatorUsecase<String, PendingApprovalsEntity?> {
    private val liveData = MediatorLiveData<Result0<PendingApprovalsEntity?>>()
    private var sourceLD : LiveData<PendingApprovalsEntity?> ?= null

    override fun execute(userId: String): Boolean {

        sourceLD?.let {
            liveData.removeSource(it)
        }

        sourceLD = approvalsDao.queryPendingApprovalsLiveData(userId).distinctUntilChanged()

        sourceLD?.let {
            liveData.addSource(it) {
                liveData.value = Result0.success(it)
            }
        }

        return true
    }

    override fun data(): LiveData<Result0<PendingApprovalsEntity?>> = liveData
}

/**
 * Usecase implementation to sync the pending approvals from N/W and update the DB
 */
class SyncPendingApprovalsUsecase @Inject constructor(private val groupService: GroupService,
                                                      private val insertIntoApprovalsUsecase: InsertIntoApprovalsUsecase) : Usecase<String, Boolean> {
    override fun invoke(userId: String): Observable<Boolean> {
        return groupService.syncPendingApprovals()
                .map {
                    ApiResponseUtils.throwErrorIfDataNull(it)
                    it.data
                }.flatMap {
                    insertIntoApprovalsUsecase.invoke(PendingApprovalsEntity(userId, it))
                }
                .map {
                    true
                }
    }
}

class ClearPendingApprovalsUsecase @Inject constructor(private val approvalsDao: PendingApprovalsDao) : Usecase<Unit, Boolean> {
    override fun invoke(p1: Unit): Observable<Boolean> {
        return Observable.fromCallable {
            approvalsDao.delete()
        }.map {
            true
        }
    }
}