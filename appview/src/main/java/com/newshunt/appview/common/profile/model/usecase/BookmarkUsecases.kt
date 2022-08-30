/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.appview.common.profile.model.internal.service.BookmarkService
import com.newshunt.appview.common.profile.model.internal.service.SyncBookmarksService
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Constants.CONTACT_SYNC_FREQ_DEFAULT
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.dataentity.model.entity.BookmarkBody
import com.newshunt.dataentity.model.entity.BookmarkEntity
import com.newshunt.dataentity.model.entity.BookmarkList
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.daos.BookmarksDao
import com.newshunt.news.model.service.legacy.OfflineStoriesProvider
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import javax.inject.Inject

/**
 * All Usecases related to Bookmarks should be implemented here
 * Created by srikanth.ramaswamy on 12/06/2019
 */

/**
 * Usecase implementation to sync bookmarks
 */
class SyncBookmarksUsecase @Inject constructor(private val syncBookmarksService: SyncBookmarksService,
                                               private val bookmarksDao: BookmarksDao) : Usecase<Boolean, Boolean> {
    override fun invoke(isForced: Boolean): Observable<Boolean> {
        val lastBookmarkSync = PreferenceManager.getPreference(AppStatePreference.BOOKMARK_SYNC_LAST_DONE, 0L)
        val minimumGap = PreferenceManager.getPreference(GenericAppStatePreference.BOOKMARK_SYNC_MINIMUM_GAP, CONTACT_SYNC_FREQ_DEFAULT)
        if (System.currentTimeMillis() - lastBookmarkSync < minimumGap && !isForced) {
            return Observable.empty()
        } else return syncBookmarksService.syncBookmarks()
                .map {
                    val bookmarks = it.rows
                    if (!bookmarks.isNullOrEmpty()) {
                        val filteredBookmarks = bookmarks.map { bookmarkItem ->
                            BookmarkEntity(id = bookmarkItem.itemId,
                                    format = bookmarkItem.format,
                                    subFormat = bookmarkItem.subFormat,
                                    syncStatus = SyncStatus.SYNCED,
                                    timestamp = bookmarkItem.timestamp ?: System.currentTimeMillis(),
                                    action = BookMarkAction.ADD)
                        }
                        bookmarksDao.insertIgnore(filteredBookmarks)
                    }
                    PreferenceManager.savePreference(AppStatePreference.BOOKMARK_SYNC_LAST_DONE, System.currentTimeMillis())
                    true
                }
    }
}

/**
 * Usecase implementation to add/delete bookmarks
 */
class PostBookmarksUsecase @Inject constructor(private val bookmarkService: BookmarkService,
                                               private val bookmarksDao: BookmarksDao) : Usecase<BookmarkList, Boolean> {
    private val LOG_TAG = "PostBookmarksUsecase"

    override fun invoke(bookmarkList: BookmarkList): Observable<Boolean> {
        return Observable.fromCallable {
            //First add the items to Bookmarks DB
            bookmarksDao.insReplace(bookmarkList.items.map {
                BookmarkEntity(id = it.itemId,
                        action = it.action,
                        timestamp = it.timestamp ?: System.currentTimeMillis(),
                        format = it.format,
                        subFormat = it.subFormat,
                        syncStatus = SyncStatus.UN_SYNCED)
            })
            //Set the status of all UNSYNCED bookmarks to IN PROGRESS
            bookmarksDao.setStatus(SYNC_TO_SERVER_STATUS, SyncStatus.IN_PROGRESS)
            //Query all IN PROGRESS bookmarks
            val unsyncedBookmarks = bookmarksDao.getBookmarksByStatus(listOf(SyncStatus.IN_PROGRESS))

            //Form a new list of bookmarks with all previous unsynced bookmarks + new ones input to this method
            val unsyncedBody = unsyncedBookmarks.map { bookmarkEntity ->
                BookmarkBody(itemId = bookmarkEntity.id,
                        action = bookmarkEntity.action,
                        subFormat = bookmarkEntity.subFormat,
                        format = bookmarkEntity.format,
                        timestamp = bookmarkEntity.timestamp)
            }
            Logger.d(LOG_TAG, "Updated status of synced items to IN PROGRESS")
            unsyncedBody
        }.flatMap {
            if (it.isEmpty()) {
                Observable.just(Constants.HTTP_SUCCESS)
            } else {
                Logger.d(LOG_TAG, "Making bookmark API call for ${it.size} items")
                bookmarkService.bookmark(BookmarkList(it))
            }
        }.map {
            bookmarksDao.setStatus(PROGRESS_STATUS, SyncStatus.SYNCED)
            bookmarksDao.deleteBookmarksByActionAndStatus(SyncStatus.SYNCED, BookMarkAction.DELETE)
            Logger.d(LOG_TAG, "Bookmarks success, marked added items SYNCED and deleted the DELETE bookmarks")
            true
        }.onErrorResumeNext { it: Throwable? ->
            bookmarksDao.setStatus(PROGRESS_STATUS, SyncStatus.UN_SYNCED)
            Logger.e(LOG_TAG, "onErrorResumeNext ${it?.message}")
            Observable.error(it)
        }
    }
}

/**
 * Usecase implementation to count number of bookmarks of a specific format
 * */
class CountBookmarksUsecase @Inject constructor(private val bookmarksDao: BookmarksDao) : MediatorUsecase<List<String>, Int> {

    private val liveData = MediatorLiveData<Result0<Int>>()
    override fun execute(formats: List<String>): Boolean {
        liveData.addSource(bookmarksDao.countByFormatLiveData(BookMarkAction.ADD, formats)) {
            liveData.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<Int>> = liveData
}

/**
 * Usecase implementation to clear all bookmarks in the DB
 * */
class ClearBookmarksUsecase @Inject constructor(private val bookmarksDao: BookmarksDao): Usecase<Unit, Boolean> {
    override fun invoke(unit: Unit): Observable<Boolean> {
        return Observable.fromCallable {
            bookmarksDao.clearBookmarks()
        }.map {
            true
        }
    }
}

/**
 * Usecase implementation to chain clear bookmarks and sync bookmarks:
 * First clear the bookmarks table and then resync from server.
 */
class ResetBookmarksUsecase @Inject constructor(private val clearBookmarksUsecase: ClearBookmarksUsecase,
                                                private val syncBookmarksUsecase: SyncBookmarksUsecase) : Usecase<Unit, Boolean> {
    override fun invoke(p1: Unit): Observable<Boolean> {
        return clearBookmarksUsecase.invoke(Unit)
                .flatMap {
                    PreferenceManager.savePreference(AppStatePreference.BOOKMARK_SYNC_LAST_DONE, 0L)
                    syncBookmarksUsecase.invoke(isForced = false)
                }
    }
}

/**
 * Usecase implementation to query livedata of added bookmarks
 * */
class QueryBookmarksUsecase @Inject constructor(private val bookmarksDao: BookmarksDao): MediatorUsecase<BookMarkAction, List<String>> {
    private val liveData = MediatorLiveData<Result0<List<String>>>()
    override fun execute(t: BookMarkAction): Boolean {
        liveData.addSource(bookmarksDao.queryBookmarkIdsLiveData(t).distinctUntilChanged()) {
            liveData.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<List<String>>> = liveData
}

/**
 * Usecase implementation to sync legacy saved stories. This will be used to sync pre social
 * saved DB stories
 */
class SyncLegacyBookmarksUsecase @Inject constructor(private val bookmarkService: BookmarkService,
                                                     private val syncBookmarksUsecase: SyncBookmarksUsecase) : Usecase<BookmarkList, Boolean> {
    override fun invoke(p1: BookmarkList): Observable<Boolean> {

        val legacySyncDone = PreferenceManager.getPreference(AppStatePreference.LEGACY_ARTICLES_SYNC_DONE, false)
        return if (legacySyncDone) {
            Observable.empty()
        } else Observable.fromCallable {
            OfflineStoriesProvider.getOfflineStoriesProvider().savedArticles
        }.flatMap { offlineArticles ->
            //Compose a BookmarkBody out of old ids. Hardcode Format and SubFormat since we are  not sure
            val bookmarkList = offlineArticles.map { offlineArticle ->
                val ts =
                        if (offlineArticle.interactionTime > 0)
                            offlineArticle.interactionTime
                        else
                            System.currentTimeMillis()
                BookmarkBody(offlineArticle.id,
                        Format.HTML.name,
                        SubFormat.STORY.name,
                        BookMarkAction.ADD,
                        offlineArticle.groupType,
                        ts)
            }
            if (bookmarkList.isNullOrEmpty()) {
                Observable.empty()
            } else {
                bookmarkService.bookmarkLegacy(BookmarkList(bookmarkList))
            }
        }.map {
            OfflineStoriesProvider.getOfflineStoriesProvider().clearAll()
            PreferenceManager.savePreference(AppStatePreference.LEGACY_ARTICLES_SYNC_DONE, true)
            it
        }.flatMap {
            syncBookmarksUsecase.invoke(isForced = true)
        }
    }
}