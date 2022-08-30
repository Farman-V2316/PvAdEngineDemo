/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.model.entity.ContactEntity
import com.newshunt.dataentity.model.entity.ContactSyncResetException
import com.newshunt.dataentity.model.entity.ContactsSyncPayload
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.daos.ContactsDao
import com.newshunt.news.model.repo.ContactsRepository
import com.newshunt.news.model.service.ContactSyncService
import io.reactivex.Observable
import java.net.HttpURLConnection.HTTP_RESET
import javax.inject.Inject

/**
 * All contact sync related use cases to be written here
 * <p>
 * Created by srikanth.ramaswamy on 10/04/2019.
 */
/**
 * Usecase implementation to update contacts data in the ROOM table
 */
class UpdateContactsDBUsecase @Inject constructor() : Usecase<ContactsSyncPayload, Boolean> {
    override fun invoke(payload: ContactsSyncPayload): Observable<Boolean> {
        return Observable.fromCallable {
            val addList = ArrayList<ContactEntity>()

            if (payload.additions.isNotEmpty()) {
                addList.addAll(payload.additions)
            }
            if (payload.updations.isNotEmpty()) {
                addList.addAll(payload.updations)
            }
            if (addList.isNotEmpty()) {
                ContactsRepository.insertContacts(addList)
            }
            if (payload.deletions.isNotEmpty()) {
                ContactsRepository.deleteContacts(payload.deletions)
            }
            true
        }
    }
}

/**
 * Usecase implementation to hit the contact sync API
 */
class SyncContactUsecase @Inject constructor(private val contactService: ContactSyncService) : Usecase<ContactsSyncPayload, ContactsSyncPayload> {
    override fun invoke(payload: ContactsSyncPayload): Observable<ContactsSyncPayload> {
        return contactService.syncContacts(payload).map {
            if (it == HTTP_RESET) {
                throw ContactSyncResetException("Need to Perform full sync")
            }
            payload
        }
    }
}

/**
 * Usecase implementation to flush the lite and full contact sync status and timestamps
 */
class ResetContactSyncUsecase @Inject constructor(private val contactsDao: ContactsDao): Usecase<Unit, Unit> {
    override fun invoke(p1: Unit): Observable<Unit> {
        return Observable.fromCallable {
            AppUserPreferenceUtils.flushContactLiteSyncStatus()
            PreferenceManager.savePreference(AppStatePreference.CONTACT_SYNC_LATEST_TIMESTAMP, 0L)
            contactsDao.deleteAll()
            Unit
        }
    }
}