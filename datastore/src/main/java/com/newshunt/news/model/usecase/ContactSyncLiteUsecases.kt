/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.PasswordEncryption
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.model.entity.ContactLiteItem
import com.newshunt.dataentity.model.entity.ContactsSyncLitePayload
import com.newshunt.news.model.daos.FollowEntityDao
import io.reactivex.Observable
import javax.inject.Inject

/**
 * All lite contact sync usecases are to be written in this file
 * <p>
 * Created by srikanth.ramaswamy on 03/24/2020.
 */
private const val LOG_TAG = "LiteContactSync"

/**
 * Usecase implementation to build the payload for Lite contact sync.
 */
class BuildContactSyncLitePayloadUsecase @Inject constructor(private val entityDao: FollowEntityDao) : BundleUsecase<Any> {
    private val contentUri = ContactsContract.Data.CONTENT_URI
    private val projection: Array<String>? = arrayOf(ContactsContract.RawContacts.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1
    )
    private val selection: String? = "${ContactsContract.Data.MIMETYPE} IN (?, ?)"
    private val selectionArgs: Array<String>? = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
    private val sortOrder: String? = ContactsContract.Data.DISPLAY_NAME + " " + "ASC"

    override fun invoke(p1: Bundle): Observable<Any> {
        return if(AppUserPreferenceUtils.isContactLiteSyncDone()) {
            Logger.d(LOG_TAG, "Contact lite sync is already done, returning an empty payload")
            Observable.fromCallable {
                formPayload(emptyList())
            }
        } else {
            return Observable.fromCallable {
                var cursor: Cursor? = null
                //Mapping between contactId and POJO containing name, phones, emails
                val contactMap = HashMap<Int, ContactLiteItem>()
                val processingTimeStart = System.currentTimeMillis()
                try {
                    cursor = CommonUtils.getApplication().contentResolver.query(contentUri, projection, selection, selectionArgs, sortOrder)
                    cursor?.apply {
                        if (count > 0) {
                            this.moveToFirst()
                            //Loop through each contact on the device
                            Logger.d(LOG_TAG, "Cursor count: ${cursor.count}")
                            do {
                                /**
                                 * In this loop, we loop through every row in the cursor and group the
                                 * contacts based on contactId because there will be multiple rows
                                 * for the same contactId in the contacts provider. We pick the name,
                                 * list of phone numbers and emails and put it in the Map: whose key
                                 * is contactId.
                                 */
                                val contactIdIndex = getColumnIndex(ContactsContract.RawContacts.CONTACT_ID)
                                val id = getColumnData(this, contactIdIndex, getType(contactIdIndex))

                                (id as? Int?)?.let { contactId ->
                                    val mimeTypeIndex = getColumnIndex(ContactsContract.Data.MIMETYPE)
                                    val mimeType = getColumnData(this, mimeTypeIndex, getType(mimeTypeIndex))
                                    val nameIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
                                    val name = getColumnData(this, nameIndex, getType(nameIndex)) as? String?

                                    //First row of a contactId, create a new ContactLiteItem
                                    if (contactMap[contactId] == null) {
                                        contactMap[contactId] = ContactLiteItem(name, HashSet(), HashSet())
                                    }
                                    val csItem = contactMap[contactId]

                                    if (mimeType is String) {
                                        val dataIndex = getColumnIndex(ContactsContract.Data.DATA1)
                                        val data = getColumnData(this, dataIndex, getType(dataIndex))
                                        if (data is String) {
                                            when (mimeType) {
                                                //Add phone numbers and emails into their respective sets
                                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                                                    csItem?.phones?.add(data)
                                                }
                                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                                                    csItem?.emails?.add(data)
                                                }
                                            }
                                        }
                                    }
                                }
                            } while (this.moveToNext())
                        } else {
                            Logger.e(LOG_TAG, "Contact cursor is empty")
                        }
                    }
                } catch (ex: Exception) {
                    Logger.caughtException(ex)
                } finally {
                    cursor?.close()
                }

                val payload = if (contactMap.isEmpty()) {
                    Logger.e(LOG_TAG, "Could not form the payload, map is empty")
                    formPayload(emptyList())
                } else {
                    Logger.d(LOG_TAG, "Found ${contactMap.size} unique contacts")
                    formPayload(contactMap.values.toList())
                }
                Logger.d(LOG_TAG, "Returning the Gzipped, encrypted payload. time taken: ${System
                        .currentTimeMillis() - processingTimeStart}ms")
                payload
            }
        }
    }

    private fun getColumnData(cursor: Cursor, columnIndex: Int, columnType: Int): Any? {
        if (cursor.isClosed) {
            return null
        }

        return when (columnType) {
            Cursor.FIELD_TYPE_FLOAT -> cursor.getFloat(columnIndex)
            Cursor.FIELD_TYPE_INTEGER -> cursor.getInt(columnIndex)
            Cursor.FIELD_TYPE_STRING -> cursor.getString(columnIndex)
            else -> null
        }
    }

    private fun formPayload(list: List<ContactLiteItem>): ContactsSyncLitePayload {
        val notOlderTs = System.currentTimeMillis() - BuildPayloadUsecase.dislikeTimeLimit()
        val recentFollows = entityDao.recentActions(notOlderTs)
        val recentUnFollows = entityDao.recentActions(notOlderTs, FollowActionType.UNFOLLOW.name)
        val recentBlocks = entityDao.recentActions(notOlderTs, FollowActionType.BLOCK.name)

        val gzippedPayload = CommonUtils.compressString(JsonUtils.toJson(list))
        val payload = PasswordEncryption.encrypt(gzippedPayload)
        return ContactsSyncLitePayload(payload, recentFollows, recentUnFollows, recentBlocks)
    }
}