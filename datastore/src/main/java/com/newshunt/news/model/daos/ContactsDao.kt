/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.newshunt.dataentity.model.entity.CONTACTS_ID
import com.newshunt.dataentity.model.entity.CONTACTS_TABLE_NAME
import com.newshunt.dataentity.model.entity.CONTACT_VERSION
import com.newshunt.dataentity.model.entity.ContactEntity


/**
 * Contacts Dao
 * <p>
 * Created by srikanth.ramaswamy on 10/04/2019.
 */
@Dao
interface ContactsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContacts(contacts: List<ContactEntity>)

    @Query("SELECT $CONTACTS_ID, $CONTACT_VERSION FROM $CONTACTS_TABLE_NAME")
    fun fetchContacts(): List<ContactEntity>

    @Query("DELETE FROM $CONTACTS_TABLE_NAME WHERE $CONTACTS_ID IN (:ids)")
    fun deleteContact(ids: List<String>)

    @Query("DELETE FROM $CONTACTS_TABLE_NAME")
    fun deleteAll()
}