/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.service

import com.newshunt.dataentity.model.entity.ContactsSyncPayload
import com.newshunt.news.model.apis.ContactSyncAPI
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Service interface and implementation for syncing contacts
 * <p>
 * Created by srikanth.ramaswamy on 10/04/2019.
 */
interface ContactSyncService {
    fun syncContacts(payload: ContactsSyncPayload): Observable<Int>
}

class ContactSyncServiceImpl @Inject constructor(private val contactSyncAPI: ContactSyncAPI) : ContactSyncService {
    override fun syncContacts(payload: ContactsSyncPayload): Observable<Int> {
        return contactSyncAPI.syncContacts(payload).map {
            it.code
        }
    }
}