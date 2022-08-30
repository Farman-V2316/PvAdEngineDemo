/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.usecase

import android.os.Bundle
import com.newshunt.appview.common.profile.model.internal.service.ProfileService
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.model.entity.DeletedInteractionsEntity
import com.newshunt.dhutil.toArrayList
import com.newshunt.news.model.daos.DeletedInteractionsDao
import com.newshunt.news.model.usecase.DislikeUsecase
import com.newshunt.news.model.usecase.UndoDislikeUsecase
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import javax.inject.Inject

/**
 * All Usecases related to userinteraction activities must be implemented in this file
 *
 * Created by srikanth.ramaswamy on 12/06/2019
 */

/**
 * Usecase implementation to delete an interaction
 */
class DeleteInteractionUsecase @Inject constructor(private val deletedInteractionsDao: DeletedInteractionsDao,
                                                   private val profileService: ProfileService) : Usecase<Boolean, Int> {
    private val LOG_TAG = "DeleteInteractionUsecase"
    override fun invoke(clearAll: Boolean): Observable<Int> {
        return Observable.fromCallable {
            //First mark UNSYNCD and MARKED items to IN PROGRESS
            deletedInteractionsDao.setStatus(fromStatus = SYNC_TO_SERVER_STATUS, toStatus = SyncStatus.IN_PROGRESS)
            //Fetch all IN PROGRESS ITEMS and this will be input to API
            Logger.d(LOG_TAG, "Set all unsynced items as IN PROGRESS")
            deletedInteractionsDao.getDeletedInteractionsByStatus(PROGRESS_STATUS)

        }.flatMap {
            val activities = it.map {
                it.id
            }
            Logger.d(LOG_TAG, "Making API call to delete user interactions")
            //API Call to delete activities
            profileService.deleteUserInteractions(activities, clearAll)
        }.map {
            Logger.d(LOG_TAG, "API call success, delete the IN PROGRESS items")
            //API Success, delete the items from DB. Not needed anymore.
            deletedInteractionsDao.deleteInteractionsByStatus(SyncStatus.IN_PROGRESS)
            it
        }.onErrorResumeNext { it: Throwable? ->
            //API Failure, update the status as UN SYNCED
            deletedInteractionsDao.setStatus(fromStatus = PROGRESS_STATUS, toStatus = SyncStatus.UN_SYNCED)
            Logger.e(LOG_TAG, "onErrorResumeNext, setting status to unsynced ${it?.message}")
            Observable.error(it)
        }
    }
}

/**
 * Usecase implementation to mark user interactions deleted
 */
class MarkInteractionDeletedUsecase @Inject constructor(private val deletedInteractionsDao: DeletedInteractionsDao,
                                                        private val dislikeUsecase: DislikeUsecase) : Usecase<String, Boolean> {
    override fun invoke(id: String): Observable<Boolean> {
        return Observable.fromCallable {
            deletedInteractionsDao.insReplace(DeletedInteractionsEntity(id, SyncStatus.MARKED))
        }.flatMap {
            val args = Bundle()
            args.putStringArrayList(Constants.BUNDLE_POST_IDS, listOf(id).toArrayList())
            dislikeUsecase.invoke(args)
        }
    }
}

/**
 * Usecase implementation to remove the marked interactions from the table
 */
class UndoInteractionDeleteUsecase @Inject constructor(private val deletedInteractionsDao: DeletedInteractionsDao,
                                                       private val undoDislikeUsecase: UndoDislikeUsecase) : Usecase<Unit, Boolean> {
    override fun invoke(unit: Unit): Observable<Boolean> {
        return Observable.fromCallable {
            // Fetch all items marked MARKED and delete from them from the DB
            val postIds = deletedInteractionsDao.getDeletedInteractionsByStatus(listOf(SyncStatus.MARKED))
            deletedInteractionsDao.deleteInteractionsByStatus(SyncStatus.MARKED)
            postIds
        }.flatMap { postIds ->
            //Remove the posts from the dislike DB as well
            val args = Bundle()
            args.putStringArrayList(Constants.BUNDLE_POST_IDS, postIds.map { it.id }.toArrayList())
            undoDislikeUsecase.invoke(args)
        }
    }
}

/**
 * Usecase implementation to clear the deleted interactions from the DB
 */
class ClearDeletedInteractionsUsecase @Inject constructor(private val deletedInteractionsDao: DeletedInteractionsDao) : Usecase<Unit, Boolean> {
    override fun invoke(p1: Unit): Observable<Boolean> {
        return Observable.fromCallable {
            deletedInteractionsDao.clearDeletedInteractions()
            true
        }
    }
}