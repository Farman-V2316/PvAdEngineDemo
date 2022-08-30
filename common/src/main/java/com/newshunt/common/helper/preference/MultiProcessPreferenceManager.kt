/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.preference

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.lang.Exception
import java.util.HashSet

/**
 * @author anshul.jain
 * Preference manager to access preferences across multiple processes.
 */

class MultiProcessPreferenceManager {

    private val BASE_URI = Uri.parse("content://" + AppConfig.getInstance().packageName + ".preferenceProvider")

    fun savePreference(fileName: String?, type: PreferenceDataType?, key: String?,
                       value: Any?,
                       alternativeContext: Context?) {

        val context = CommonUtils.getApplication()
        if (type == null || fileName == null || key == null || context == null) {
            return
        }

        val contentUri = getContentUri(fileName, type.name, key)
        val selectionArgs = getSelectionArgs(type, value)
        try{
            context.contentResolver.update(contentUri, ContentValues(), null, selectionArgs)
        }catch(ex: Exception){
            try{
                alternativeContext?.let {
                    it.contentResolver.update(contentUri, ContentValues(), null, selectionArgs)
                }
            }catch(exception: Exception){
                Logger.caughtException(exception)
            }
        }
    }

    fun <T> getPreference(fileName: String?, type: PreferenceDataType?, key: String?, defValue: T?)
            : T? {

        val context = CommonUtils.getApplication()
        if (type == null || fileName == null || key == null || context == null) {
            return defValue
        }

        val contentUri = getContentUri(fileName, type.name, key)
        val selectionArgs = getSelectionArgs(type, defValue)

        val cursor = context.contentResolver
                .query(contentUri, null, null, selectionArgs, null)

        var result: T? = defValue
        when (type) {
            PreferenceDataType.STRING -> {result = getStringValue(cursor, defValue)}
            PreferenceDataType.INTEGER -> {result = getIntegerValue(cursor, defValue)}
            PreferenceDataType.FLOAT -> {result = getFloatValue(cursor, defValue)}
            PreferenceDataType.LONG -> {result = getLongValue(cursor, defValue)}
            PreferenceDataType.BOOLEAN -> {result = getBooleanValue(cursor, defValue)}
            PreferenceDataType.SET -> {result = getSetValue(cursor, defValue)}
        }

        cursor?.close()
        return result
    }

    private fun <T> getSelectionArgs(type: PreferenceDataType, defValue: T?): Array<String> {
        return when (type) {
            PreferenceDataType.STRING -> arrayOf(defValue as String)
            PreferenceDataType.INTEGER -> arrayOf((defValue as Int).toString())
            PreferenceDataType.FLOAT -> arrayOf((defValue as Float).toString())
            PreferenceDataType.LONG -> arrayOf((defValue as Long).toString())
            PreferenceDataType.BOOLEAN -> arrayOf((defValue as Boolean).toString())
            PreferenceDataType.SET -> (defValue as Set<String>).toTypedArray()
        }
    }

    private fun <T> getStringValue(cursor: Cursor?, defValue: T): T {

        cursor ?: return defValue

        val value = if (cursor.moveToFirst()) {
            cursor.getString(0) as T
        } else defValue

        cursor.close()
        return value
    }

    private fun <T> getIntegerValue(cursor: Cursor?, defValue: T): T {

        cursor ?: return defValue

        val value = if (cursor.moveToFirst()) {
            cursor.getInt(0) as T
        } else defValue

        cursor.close()
        return value
    }

    private fun <T> getLongValue(cursor: Cursor?, defValue: T): T {

        cursor ?: return defValue

        val value = if (cursor.moveToFirst()) {
            cursor.getLong(0) as T
        } else defValue

        cursor.close()
        return value
    }

    private fun <T> getBooleanValue(cursor: Cursor?, defValue: T): T {

        cursor ?: return defValue

        val value = if (cursor.moveToFirst()) {
            (cursor.getInt(0) > 0) as T
        } else defValue

        cursor.close()
        return value
    }

    private fun <T> getFloatValue(cursor: Cursor?, defValue: T): T {

        cursor ?: return defValue

        val value = if (cursor.moveToFirst()) {
            cursor.getFloat(0) as T
        } else defValue

        cursor.close()
        return value
    }

    private fun <T> getSetValue(cursor: Cursor?, defValue: T): T {

        cursor ?: return defValue

        if (!cursor.moveToFirst()) {
            return defValue
        }

        val set = HashSet<String>()
        do {
            set.add(cursor.getString(0))
        } while (cursor.moveToNext())
        cursor.close()
        return set as T
    }

    private fun getContentUri(fileName: String, type: String, key: String): Uri {
        return BASE_URI.buildUpon()
                .appendPath(fileName)
                .appendPath(type)
                .appendPath(key)
                .build()
    }
}