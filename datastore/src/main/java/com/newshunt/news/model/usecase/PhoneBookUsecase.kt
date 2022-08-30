/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.news.model.usecase

import android.database.Cursor
import android.provider.ContactsContract
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.Contact
import io.reactivex.Observable
import java.util.TreeSet
import javax.inject.Inject

/**
 * use case for contact
 * @author Mukesh Yadav
 * */
private const val ORDER_BY = " ASC"
private const val LOG_TAG = "PhoneBookUsecase"

class PhoneBookUsecase @Inject constructor() : Usecase<Unit, List<Contact>> {
    override fun invoke(p1: Unit): Observable<List<Contact>> {
        return Observable.fromCallable {
            val contacts = TreeSet<Contact>(kotlin.Comparator { contact1, contact2 ->
                val compareValue = contact1.name.compareTo(contact2.name)
                return@Comparator if (compareValue == 0) {
                    contact1.phoneNumber.compareTo(contact2.phoneNumber)
                } else {
                    compareValue
                }
            })

            /**
             * creating the project to avoid fetching raw data
             * this will ensure that only name,number and profile thumbnails will be fetched
             * */
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            var cursor : Cursor ? = null
            try {
                cursor = CommonUtils.getApplication().contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        projection,
                        null,
                        null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ORDER_BY)

                cursor?.apply {
                    if (this.count > 0) {
                        while (cursor.moveToNext()) {
                            val contact = Contact(
                                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
                                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)))
                            contacts.add(contact)
                        }
                    }
                }
            }catch (e : Exception ){
                e.printStackTrace()
            }finally {
                cursor?.close()
            }
            contacts.toList()
        }
    }
}

