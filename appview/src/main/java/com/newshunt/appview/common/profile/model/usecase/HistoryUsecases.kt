/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.usecase

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.newshunt.appview.common.profile.helper.dateFormat
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.news.model.daos.HistoryDao
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Named

/**
 * All History feature related usecases to be implemented in this file
 * Created by srikanth.ramaswamy on 12/06/2019
 */


/**
 * Usecase implementation to add an item to History table
 */
class AddToHistoryUsecase @Inject constructor(private val historyDao: HistoryDao) : Usecase<HistoryEntity, Unit> {
    private val LOG_TAG = "AddToHistoryUsecase"
    override fun invoke(historyEntity: HistoryEntity): Observable<Unit> {
        return Observable.fromCallable {
            val existingEntry = historyDao.getItem(historyEntity.id)
            if (existingEntry?.isDeleted == true) {
                //If entry exists but marked permanently deleted, replace the entry
                historyDao.insReplace(historyEntity)
            } else {
                //Entry not deleted, just insert ignore
                historyDao.insertIgnore(historyEntity)
            }
            Logger.d(LOG_TAG, "inserted into history: ${historyEntity.id}, ${historyEntity.format}")
        }
    }
}

/**
 * Usecase implementation to query the history table for display. Handles inserting the rows to
 * show dates in between history rows.
 */
class QueryHistoryForDisplayUsecase @Inject constructor(private val historyDao: HistoryDao) : MediatorUsecase<Long, List<Any>> {

    val mediatorLiveData = MediatorLiveData<Result0<List<Any>>>()
    private var sourceLD: LiveData<List<Any>>? = null


    override fun execute(t: Long): Boolean {
        sourceLD?.let {
            mediatorLiveData.removeSource(it)
        }

        sourceLD = Transformations.map(historyDao.queryLiveData(t),
                Function { historyList ->
                    val adapterList = ArrayList<Any>()
                    var runningDate = Constants.EMPTY_STRING
                    historyList.forEach { history ->
                        val dateString = dateFormat.format(history.timestamp)
                        if (runningDate != dateString) {
                            adapterList.add(dateString)
                            runningDate = dateString
                        }
                        adapterList.add(history)
                    }

                    if (adapterList.isNotEmpty()) {
                        //Dummy item to show Footer view
                        adapterList.add(Constants.EMPTY_STRING)
                    }
                    return@Function adapterList
                })

        sourceLD?.let {
            mediatorLiveData.addSource(it) {
                mediatorLiveData.value = Result0.success(it)
            }
        }
        return true
    }

    override fun data(): LiveData<Result0<List<Any>>> = mediatorLiveData
}

/**
 * Usecase implementation to count the total number of items in the history table
 */
class CountHistoryUsecase @Inject constructor(private val historyDao: HistoryDao):
        MediatorUsecase<Unit, Int> {

    private val liveData = MediatorLiveData<Result0<Int>>()
    override fun execute(t: Unit): Boolean {
        liveData.addSource(historyDao.count()) {
            liveData.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<Int>> = liveData
}

/**
 * Usecase implementation to query history table sice a time. Can be used to swipe through the
 * list of history items
 */
class QueryHistoryUsecase @Inject constructor(private val historyDao: HistoryDao) : MediatorUsecase<Long, List<HistoryEntity>> {

    private val liveData = MediatorLiveData<Result0<List<HistoryEntity>>>()
    private var sourceLD: LiveData<List<HistoryEntity>>? = null

    override fun execute(fromTime: Long): Boolean {

        sourceLD?.let {
            liveData.removeSource(it)
        }

        sourceLD = historyDao.queryLiveData(fromTime)

        sourceLD?.let {
            liveData.addSource(it) {
                liveData.value = Result0.success(it)
            }

        }
        return true
    }

    override fun data(): LiveData<Result0<List<HistoryEntity>>> = liveData
}

/**
 * Usecase implementation to mark an item deleted from the history table.
 */
class MarkHistoryDeletedUsecase @Inject constructor(private val historyDao: HistoryDao) : Usecase<String, Unit> {
    override fun invoke(storyId: String): Observable<Unit> {
        return Observable.fromCallable {
            historyDao.markDeleted(storyId)
        }
    }
}

/**
 * Usecase implementation to change the status of items from marked deleted to deleted in the
 * history table
 */
class DeleteHistoryUsecase @Inject constructor(private val historyDao: HistoryDao): Usecase<Unit, Unit> {
    override fun invoke(p1: Unit): Observable<Unit> {
        return Observable.fromCallable {
            historyDao.deleteMarkedItems()
        }
    }
}

/**
 * Usecase implementation to reset the status of items marked deleted in the history table
 */
class UndoMarkDeleteUsecase @Inject constructor(private val historyDao: HistoryDao): Usecase<Unit, Unit> {
    override fun invoke(p1: Unit): Observable<Unit> {
        return Observable.fromCallable {
            historyDao.undoDelete()
        }
    }
}

/**
 * Usecase implementation to mark all items deleted in the history table
 */
class ClearHistoryUsecase @Inject constructor(private val historyDao: HistoryDao): Usecase<Unit, Unit> {
    override fun invoke(p1: Unit): Observable<Unit> {
        return Observable.fromCallable {
            historyDao.clear()
        }
    }
}

/**
 * Usecase implementation to permanently clear the history table
 */
class ClearAllHistoryUsecase @Inject constructor(private val historyDao: HistoryDao): Usecase<Unit, Unit> {
    override fun invoke(p1: Unit): Observable<Unit> {
        return Observable.fromCallable {
            historyDao.clearForever()
        }
    }
}

/**
 * Usecase implementation to count the total number of entries in history table from specified time
 * */
class CountFilteredHistoryUsecase @Inject constructor(private val historyDao: HistoryDao,
                                                      @Named("clearSourcesOnExecute")
                                                      private val clearSourcesOnExecute: Boolean = false) : MediatorUsecase<Long, Int> {

    private val liveData = MediatorLiveData<Result0<Int>>()
    private val sourceLiveData = ArrayList<LiveData<Int>>()

    override fun execute(fromTime: Long): Boolean {
        if (clearSourcesOnExecute) {
            sourceLiveData.forEach {
                liveData.removeSource(it)
            }
            sourceLiveData.clear()
        }
        val source = historyDao.countSinceTime(fromTime)
        sourceLiveData.add(source)
        liveData.addSource(source) {
            liveData.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<Int>> = liveData
}