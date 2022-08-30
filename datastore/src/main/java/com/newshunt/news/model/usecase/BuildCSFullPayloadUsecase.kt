/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import com.dailyhunt.datastore.BuildConfig
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.PasswordEncryption
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.ContactEntity
import com.newshunt.dataentity.model.entity.ContactFullItem
import com.newshunt.dataentity.model.entity.ContactProviderEvent
import com.newshunt.dataentity.model.entity.ContactsSyncPayload
import com.newshunt.news.model.repo.ContactsRepository
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.min
import kotlin.system.measureTimeMillis

private const val LOG_TAG = "BuildCSFullPayloadUsecase"

/**
 * Usecase implementation to build the payload for full contact sync API
 * <p>
 * Created by srikanth.ramaswamy on 06/18/2020.
 */
class BuildCSFullPayloadUsecase @Inject constructor(private val contentResolver: ContentResolver,
                                                    @Named("payloadBucketSize")
                                                    private val payloadBucketSize: Int) : Usecase<Unit, List<ContactsSyncPayload>> {

    /**
     * This is a map of mime types BE is interested in and the corresponding columns for the mime
     * types. We form the payload by querying the contacts provider for these columns
     */
    private val mimeMapping = mapOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA1),
            ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA1),
            ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA1),
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA1),
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA4, ContactsContract.Contacts.Data.DATA1),
            ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA1),
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA1),
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA1),
            ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA1),
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE to listOf(ContactsContract.Contacts.Data.DATA1, ContactsContract.Contacts.Data.DATA2))

    /**
     * Maintain a mapping of contact group id and its name. This is handy when multiple contacts
     * belong to same group, we do not need to make DB query everytime.
     */
    private val groupNameMapping = HashMap<String, String>()

    /**
     * A map of contactId -> Payload. This is used for calculating the diff to form payload.
     */
    private val contactMap = HashMap<Int, ContactFullItem>()

    override fun invoke(p1: Unit): Observable<List<ContactsSyncPayload>> {
        return Observable.fromCallable {
            var payload: List<ContactsSyncPayload>? = null
            val addedList = ArrayList<ContactEntity>()
            val modifiedList = ArrayList<ContactEntity>()
            val deletedList = ArrayList<String>()
            val timetaken = measureTimeMillis {
                Logger.d(LOG_TAG, "Start building, Bucket size: $payloadBucketSize")
                //First fetch the already sync'd ids and their versions from our ROOM DB
                val localContactsMap = fetchLocalContactsAsMap()
                //populate the contactMap by querying contacts Provider
                populateContactMap()
                //Loop through each item and calculate diff with last sync'd ROOM DB
                contactMap.iterator().forEach { item ->
                    val contactId = item.key.toString()
                    val existingContactEntity = localContactsMap[contactId]
                    val itemVersion = item.value.hashCode()
                    //This contact exists in our ROOM DB
                    if (existingContactEntity != null) {
                        if (existingContactEntity.version != itemVersion) {
                            //If the versions are different, there is an update for this contact
                            logContact(contactId, item.value, ContactUpdateReason.UPDATION)
                            modifiedList.add(ContactEntity(_id = contactId,
                                    payload = gzipAndEncrypt(formJsonString(item.value)),
                                    version = itemVersion))
                        }
                        localContactsMap.remove(contactId)
                    } else {
                        //This contact does not exist in our ROOM DB. Must be a new addition
                        logContact(contactId, item.value, ContactUpdateReason.ADDITION)
                        addedList.add(ContactEntity(_id = contactId,
                                payload = gzipAndEncrypt(formJsonString(item.value)),
                                version = itemVersion))
                    }
                }
                //Items remaining in the localContactMap are the ones deleted from user's contacts
                localContactsMap.keys.forEach { contactId ->
                    deletedList.add(contactId)
                }
                payload = buildPayload(addedList, modifiedList, deletedList)
            }
            Logger.d(LOG_TAG, "additions: ${addedList.size}, updations: ${modifiedList.size}, " + "deletions: ${deletedList.size}, time taken: $timetaken")
            payload ?: listOf(ContactsSyncPayload(addedList, modifiedList, deletedList))
        }
    }

    /**
     * Query the contacts provider and populate the contactMap. This is done by grouping the
     * contacts based on the column contact_id. Each contact_id has multiple rows depending on
     * mime types.
     */
    private fun populateContactMap() {
        //Compute selection string based on the mimetypes we want to query
        var placeHolderStr: String = Constants.EMPTY_STRING
        for (i in mimeMapping.keys.indices) {
            placeHolderStr = if (i < mimeMapping.size - 1) {
                placeHolderStr.plus("?, ")
            } else {
                placeHolderStr.plus("?)")
            }
        }
        var cursor: Cursor? = null
        val contentUri = ContactsContract.Data.CONTENT_URI
        val selectionArgs: Array<String> = mimeMapping.keys.toList().toTypedArray()
        val selection = "${ContactsContract.Data.MIMETYPE} IN (".plus(placeHolderStr)

        try {
            cursor = contentResolver.query(contentUri,
                    null,
                    selection,
                    selectionArgs,
                    null)
            cursor?.apply {
                if (cursor.count > 0) {
                    this.moveToFirst()
                    do {
                        val id = getColumnData(this, ContactsContract.RawContacts.CONTACT_ID)
                        (id as? Int?)?.let { contactId ->
                            //Group by ContactId
                            if (contactMap[contactId] == null) {
                                contactMap[contactId] = ContactFullItem()
                            }
                            contactMap[contactId]?.let { item ->
                                //Photo Uri does not depend on the row's mime type, can fetch from any row
                                if (item.photoExists == null) {
                                    val photoUri = getColumnData(this, ContactsContract.CommonDataKinds.Phone.PHOTO_URI)?.toString()
                                    item.photoExists = mutableSetOf(photoUri.isNullOrEmpty().not())
                                }
                                //Starred does not depend on the row's mime type, can fetch from any row
                                if (item.favourite == null) {
                                    item.favourite = mutableSetOf(getColumnData(this, ContactsContract.CommonDataKinds.StructuredName.STARRED) as? Int?
                                            ?: 0)
                                }
                                (getColumnData(this, ContactsContract.Data.MIMETYPE) as? String?)?.let { mimeType ->
                                    //Rest of the data depends on mimetype, fetch MIMETYPE!
                                    when (mimeType) {
                                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                                            getStringColumnData(this, mimeType)?.let { email ->
                                                item.emails = initSetOfString(item.emails)
                                                item.emails?.add(email)
                                            }
                                        }
                                        ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE -> {
                                            getGroupTitle(this, mimeType)?.let { groupTitle ->
                                                item.grpMembership = initSetOfString(item.grpMembership)
                                                item.grpMembership?.add(groupTitle)
                                            }
                                        }
                                        ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE -> {
                                            getStringColumnData(this, mimeType)?.let { nickName ->
                                                item.nickname = initSetOfString(item.nickname)
                                                item.nickname?.add(nickName)
                                            }
                                        }
                                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE -> {
                                            getStringColumnData(this, mimeType)?.let { org ->
                                                item.org = initSetOfString(item.org)
                                                item.org?.add(org)
                                            }
                                        }
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                                            getPhoneNumber(this, mimeType)?.let { phone ->
                                                item.phones = initSetOfString(item.phones)
                                                item.phones?.add(phone)
                                            }
                                        }
                                        ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE -> {
                                            getStringColumnData(this, mimeType)?.let { relation ->
                                                item.relations = initSetOfString(item.relations)
                                                item.relations?.add(relation)
                                            }
                                        }
                                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                                            getStringColumnData(this, mimeType)?.let { name ->
                                                item.name = initSetOfString(item.name)
                                                item.name?.add(name)
                                            }
                                        }
                                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE -> {
                                            getStringColumnData(this, mimeType)?.let { address ->
                                                item.address = initSetOfString(item.address)
                                                item.address?.add(address)
                                            }
                                        }
                                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE -> {
                                            getStringColumnData(this, mimeType)?.let { website ->
                                                item.website = initSetOfString(item.website)
                                                item.website?.add(website)
                                            }
                                        }
                                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE -> {
                                            if (item.events == null) {
                                                item.events = HashSet()
                                            }
                                            getEvent(this, mimeType)?.let { event ->
                                                item.events?.add(event)
                                            }
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                        }
                    } while (this.moveToNext())
                }
            }
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        } finally {
            cursor?.close()
        }
    }

    /**
     * Read the data per column
     */
    private fun getColumnData(cursor: Cursor, columnName: String?): Any? {
        if (cursor.isClosed) {
            return null
        }
        columnName ?: return null

        try {
            with(cursor) {
                val columnIndex = getColumnIndex(columnName)
                return when (getType(columnIndex)) {
                    Cursor.FIELD_TYPE_FLOAT -> cursor.getFloat(columnIndex)
                    Cursor.FIELD_TYPE_INTEGER -> cursor.getInt(columnIndex)
                    Cursor.FIELD_TYPE_STRING -> cursor.getString(columnIndex)
                    else -> null
                }
            }
        } catch (ex: Exception) {
            Logger.caughtException(ex)
            return null
        }
    }

    /**
     * A convenience function to typecast the column data as String
     */
    private fun getStringColumnData(cursor: Cursor, mimeType: String): String? {
        if (cursor.isClosed) {
            return null
        }
        return (getColumnData(cursor, mimeMapping[mimeType]?.first())) as? String?
    }

    /**
     * A convenience function to check if the field is null and return a new HashSet
     */
    private fun initSetOfString(field: MutableSet<String>?): MutableSet<String> {
        return field ?: HashSet()
    }

    /**
     * Query the contacts provider's group table to fetch the name of the group based on the
     * group id that exists in the DATA table.
     */
    private fun getGroupTitle(cursor: Cursor, mimeType: String): String? {
        if (cursor.isClosed) {
            return null
        }

        (getStringColumnData(cursor, mimeType))?.let { groupId ->
            //First time querying for a groupId
            if (groupNameMapping[groupId] == null) {
                //Group details are stored in a separate table. Need to query the title based on ID
                val groupUri = ContactsContract.Groups.CONTENT_URI
                val grpProjection: Array<String?> = arrayOf(ContactsContract.Groups.TITLE,
                        ContactsContract.Groups._ID)
                val selection = "${ContactsContract.Groups._ID} IN (?)"
                val selectionArgs = arrayOf(groupId)

                var groupCursor: Cursor? = null
                try {
                    groupCursor = contentResolver.query(groupUri,
                            grpProjection,
                            selection,
                            selectionArgs,
                            null)
                    groupCursor?.apply {
                        if (this.count > 0) {
                            this.moveToFirst()
                            do {
                                (getColumnData(groupCursor, ContactsContract.Groups.TITLE) as? String?)?.let { groupTitle ->
                                    groupNameMapping[groupId] = groupTitle
                                    return groupTitle
                                }
                            } while (this.moveToNext())
                        }
                    }
                } catch (ex: Exception) {
                    Logger.caughtException(ex)
                } finally {
                    groupCursor?.close()
                }
            } else {
                //Optimization to not query for the same group id multiple times.
                return groupNameMapping[groupId]
            }
        }
        return null
    }

    /**
     * Query the events detail from Contacts provider.
     */
    private fun getEvent(cursor: Cursor, mimeType: String): ContactProviderEvent? {
        if (cursor.isClosed) {
            return null
        }

        val eventDate = getStringColumnData(cursor, mimeType)
        val eventType = (getColumnData(cursor, mimeMapping[mimeType]?.get(1)))?.toString()
        return ContactProviderEvent(eventDate, eventType)
    }

    /**
     * Fetch the last sync'd contacts data from ROOM DB and convert into a map of
     * _id -> ContactEntity.
     * The version of a contact changes when there is real change in any of the fields in the
     * contact DB row. We rely on this version field to compute the diff of contacts. Also, we do
     * not query the payload column from DB to reduce RAM usage.
     */
    private fun fetchLocalContactsAsMap(): MutableMap<String, ContactEntity> {
        return ContactsRepository.fetchContacts()
                .map { contactEntity ->
                    Pair(contactEntity._id, contactEntity)
                }
                .toMap()
                .toMutableMap()
    }

    /**
     * Logging helper for debugging
     */
    private fun logContact(id: String, item: ContactFullItem, reason: ContactUpdateReason) {
        if (AppConfig.getInstance().isLoggerEnabled && BuildConfig.DEBUG) {
            Logger.d(LOG_TAG, "id: $id, reason: $reason, $item ")
        }
    }

    /**
     * Gzip and encrypt the string
     */
    private fun gzipAndEncrypt(string: String): String {
        return PasswordEncryption.encrypt(CommonUtils.compressString(string))
    }

    private fun formJsonString(item: ContactFullItem): String {
        return JsonUtils.toJson(item)
    }

    /**
     * Helper function to make batched lists
     */
    private fun makePayloadWithList(inputList: List<ContactEntity>,
                                    reason: ContactUpdateReason): MutableList<ContactsSyncPayload> {
        //Split the list to buckets of size payloadBucketSize. The last bucket might have some room to accommodate more items
        val splitLists = inputList.chunked(payloadBucketSize)
        val listOfPayload = ArrayList<ContactsSyncPayload>()
        if (splitLists.isNotEmpty()) {
            splitLists.forEach {
                val payloadItem = if (reason == ContactUpdateReason.ADDITION) {
                    ContactsSyncPayload(it.toMutableList(), mutableListOf(), mutableListOf())
                } else {
                    ContactsSyncPayload(mutableListOf(), it.toMutableList(), mutableListOf())
                }
                listOfPayload.add(payloadItem)
            }
        }
        return listOfPayload
    }

    /**
     * This method builds a List<ContactSyncPayload> with added, modified, deleted items.
     * First creates N ContactSyncPayload items with added items
     * Appends few modified items into (N -1)th bucket basis available slots.
     * Next creates M ContactSyncPayload items with modified items
     * Adds the deleted items into (N+M)th bucket always.
     */
    private fun buildPayload(addedList: MutableList<ContactEntity>,
                             modifiedList: MutableList<ContactEntity>,
                             deletedList: MutableList<String>): List<ContactsSyncPayload> {
        //First make a list of payloads with addition items
        val listOfPayloads = makePayloadWithList(addedList, ContactUpdateReason.ADDITION)
        //Added list is split into multiple buckets. Last bucket can also accommodate some modified items
        var availableSlotsInLastAddedList = payloadBucketSize
        var needMergingModifiedWithLastAddedBucket = true //Can some modified items fit into last added bucket?
        //After fitting some modified items into last added bucket, what is the start index of next modified bucket?
        var modifiedListWindowStart = modifiedList.size


        if (listOfPayloads.isNotEmpty()) {
            //Compute the available slots in the last added bucket
            availableSlotsInLastAddedList = payloadBucketSize - listOfPayloads[listOfPayloads.size - 1].additions.size
        } else {
            //No added items, directly compose the payload with modified list
            listOfPayloads.addAll(makePayloadWithList(modifiedList, ContactUpdateReason.UPDATION))
            //No need to merge modified with added list's bucket
            needMergingModifiedWithLastAddedBucket = false
        }

        if (needMergingModifiedWithLastAddedBucket && modifiedList.isNotEmpty()) {
            //Merge the first few modified items into last added bucket. max availableSlotsInLastAddedList modified items
            val modifiedFirstSubList = modifiedList.subList(0, min(modifiedList.size, availableSlotsInLastAddedList))
            modifiedListWindowStart = modifiedFirstSubList.size //Next bucket start index
            //Add the first few modified items into last added bucket. listOfPayloads can not be empty here!!
            listOfPayloads[listOfPayloads.size - 1].updations.addAll(modifiedFirstSubList)
        }
        //If there are more modified items, compose new list of payloads with modified items
        if (modifiedListWindowStart < modifiedList.size) {
            listOfPayloads.addAll(makePayloadWithList(modifiedList.subList(modifiedListWindowStart, modifiedList.size), ContactUpdateReason.UPDATION))
        }

        //Finally, if there was no added/modified items, listOfPayloads will be empty. Create an empty payload
        if (listOfPayloads.isEmpty()) {
            listOfPayloads.add(ContactsSyncPayload(mutableListOf(), mutableListOf(), mutableListOf()))
        }
        //Deleted items list is just list of ints. Hence add them to the final bucket irrespective.
        if (deletedList.isNotEmpty()) {
            listOfPayloads[listOfPayloads.size - 1].deletions.addAll(deletedList)
        }
        return listOfPayloads
    }

    /**
     * Phone number usually is stored in DATA4. But in some cases when phone number normalization
     * fails, DATA4 has null. But in any case, DATA1 has the number in raw format. Hence, return
     * DATA1 if DATA4 is empty
     */
    private fun getPhoneNumber(cursor: Cursor, mimeType: String): String? {
        if (cursor.isClosed) {
            return null
        }
        val normalizedPhone = getColumnData(cursor, mimeMapping[mimeType]?.first()) as? String?
        return if (normalizedPhone.isNullOrEmpty().not()) {
            normalizedPhone
        } else {
            getColumnData(cursor, mimeMapping[mimeType]?.get(1)) as? String?
        }
    }

    private enum class ContactUpdateReason {
        ADDITION,
        UPDATION,
    }
}