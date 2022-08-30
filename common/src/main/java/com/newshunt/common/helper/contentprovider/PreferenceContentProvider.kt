/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.PreferenceDataType
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.preference.PreferenceType

/**
 * @author anshul.jain
 * A content provider to access shared preferences across multiple processes.
 */

class PreferenceContentProvider : ContentProvider() {

    private val TAG = "PreferenceContentProvider"

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {

        Logger.d(TAG, "Inside update() method with uri : $uri")

        val pathSegments = uri.pathSegments

        if (pathSegments.size < 3) return 0

        val fileName = pathSegments.get(0)
        val type = pathSegments.get(1)
        val key = pathSegments.get(2)
        val preferenceDataType: PreferenceDataType? = PreferenceDataType.valueOf(type)
        val preferenceType = PreferenceType.getType(fileName)
        val context = CommonUtils.getApplication()

        if (preferenceDataType == null || preferenceType == null || context == null) return 0

        val value = getValueFromArguments(preferenceDataType, selectionArgs)
        PreferenceManager.savePreference(context, preferenceType, key, value, true, null)
        return 1
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        Logger.d(TAG, "Inside query() method with uri : $uri")

        val pathSegments = uri.pathSegments

        if (pathSegments.size < 3) return null

        val fileName = pathSegments[0]
        val type = pathSegments[1]
        val key = pathSegments[2]
        val preferenceDataType: PreferenceDataType? = PreferenceDataType.valueOf(type)
        val preferenceType = PreferenceType.getType(fileName)
        val context = CommonUtils.getApplication()

        if (preferenceDataType == null || preferenceType == null || context == null) return null

        val defaultValue = getValueFromArguments(preferenceDataType, selectionArgs)
        val value = PreferenceManager.getPreference(context, preferenceType, key,
                defaultValue, true)
        return populateCursor(key, value, preferenceDataType)
    }

    private fun getFirstElement(array: Array<String>?): String? {
        array?.isEmpty() ?: return null
        return array[0]
    }

    private fun populateCursor(key: String, value: Any?, type: PreferenceDataType): Cursor {

        val cursor = MatrixCursor(arrayOf(key))

        if (type == PreferenceDataType.SET) {

            val set = value as Set<String>
            for (stringValue in set) {
                val rowBuilder = cursor.newRow()
                rowBuilder.add(stringValue)
            }
            return cursor
        }

        val rowBuilder = cursor.newRow()
        val preferenceValue = if (type == PreferenceDataType.BOOLEAN) {
            if (value == true) 1 else 0
        } else value
        rowBuilder.add(preferenceValue)

        return cursor
    }

    override fun onCreate(): Boolean {
        Logger.d(TAG, "onCreate Called for content Provider")
        return true
    }

    private fun getValueFromArguments(preferenceDataType: PreferenceDataType, selectionArgs: Array<String>?): Any? {
        val value = when (preferenceDataType) {
            PreferenceDataType.STRING -> getFirstElement(selectionArgs)
            PreferenceDataType.INTEGER -> getFirstElement(selectionArgs)?.toInt()
            PreferenceDataType.FLOAT -> getFirstElement(selectionArgs)?.toFloat()
            PreferenceDataType.LONG -> getFirstElement(selectionArgs)?.toLong()
            PreferenceDataType.BOOLEAN -> getFirstElement(selectionArgs)?.toBoolean()
            PreferenceDataType.SET -> selectionArgs?.toSet()
        }
        return value
    }
}