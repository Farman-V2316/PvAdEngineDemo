/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.repo

import com.newshunt.dataentity.model.entity.ContactEntity
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.ResetContactSyncUsecase
import com.newshunt.news.model.usecase.toMediator2

/**
 * Contact repository singleton to talk to the dao
 * <p>
 * Created by srikanth.ramaswamy on 10/04/2019.
 */
object ContactsRepository {

    fun fetchContacts(): List<ContactEntity> {
        return SocialDB.instance().contactsDao().fetchContacts()
    }

    fun insertContacts(contactList: List<ContactEntity>) {
        return SocialDB.instance().contactsDao().insertContacts(contactList)
    }

    fun deleteContacts(contactIds: List<String>) {
        SocialDB.instance().contactsDao().deleteContact(contactIds)
    }

    fun clearContacts() {
        SocialDB.instance().contactsDao().deleteAll()
    }

    fun flush() {
        ResetContactSyncUsecase(SocialDB.instance().contactsDao()).toMediator2().execute(Unit)
    }
}