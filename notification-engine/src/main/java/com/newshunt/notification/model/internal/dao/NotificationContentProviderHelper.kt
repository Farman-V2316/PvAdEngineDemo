/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.internal.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.dataentity.notification.BaseModelType
import com.newshunt.notification.model.entity.NotificationEntity
import com.newshunt.notification.sqlite.NotificationDB
import com.newshunt.notification.sqlite.NotificationProvider

/**
 * Helper class to interact with the content provider and cursor
 *
 * Created by srikanth.r on 10/22/21.
 */
object NotificationContentProviderHelper {
    private val notificationDao by lazy {
        NotificationDB.instance().getNotificationDao()
    }
    private val columns = arrayOf(NotificationProvider.NOTIFICATION_BASE_MODEL,
        NotificationProvider.NOTIFICATION_BASE_MODEL_TYPE)


    fun mapContentValuesToNotificationEntity(values: ContentValues?): NotificationEntity? {
        values ?: return null
        return (values[NotificationProvider.NOTIFICATION_BASE_MODEL] as? BaseModel?)?.let {
            notificationDao.setNotificationInfo(it)
        }
    }

    fun cursorFromBaseModels(baseModels: List<BaseModel>?): Cursor? {
        baseModels ?: return null
        return MatrixCursor(columns).apply {
            baseModels.forEach { baseModel ->
                val jsonBlob = JsonUtils.toJson(baseModel).toByteArray()
                addRow(arrayOf(jsonBlob, baseModel.baseModelType.name))
            }
        }
    }

    fun baseModelsFromCursor(cursor: Cursor?): List<BaseModel>? {
        cursor ?: return null
        if (cursor.isClosed || cursor.count == 0) {
            return listOf()
        }
        cursor.moveToFirst()
        val baseModels = ArrayList<BaseModel>()
        do {
            getBlobColumnData(cursor, NotificationProvider.NOTIFICATION_BASE_MODEL)?.let { blob ->
                val baseModelType = getStringColumnData(cursor, NotificationProvider.NOTIFICATION_BASE_MODEL_TYPE)
                if (baseModelType.isNotBlank()) {
                    BaseModelType.convertStringToBaseModel(String(blob),
                        BaseModelType.getValue(baseModelType),
                        Constants.EMPTY_STRING)?.let {
                        baseModels.add(it)
                    }
                }
            }
        } while (cursor.moveToNext())
        return baseModels
    }

    private fun getColumnData(cursor: Cursor, columnName: String?): Any? {
        columnName ?: return null
        if (cursor.isClosed) {
            return null
        }
        try {
            with(cursor) {
                val columnIndex = getColumnIndex(columnName)
                return when (getType(columnIndex)) {
                    Cursor.FIELD_TYPE_FLOAT -> cursor.getFloat(columnIndex)
                    Cursor.FIELD_TYPE_INTEGER -> cursor.getInt(columnIndex)
                    Cursor.FIELD_TYPE_STRING -> cursor.getString(columnIndex)
                    Cursor.FIELD_TYPE_BLOB -> cursor.getBlob(columnIndex)
                    else -> null
                }
            }
        } catch (ex: Exception) {
            Logger.caughtException(ex)
            return null
        }
    }

    private fun getStringColumnData(cursor: Cursor, columnName: String?): String {
        return getColumnData(cursor, columnName) as? String? ?: Constants.EMPTY_STRING
    }

    private fun getBlobColumnData(cursor: Cursor, columnName: String?): ByteArray? {
        return getColumnData(cursor, columnName) as? ByteArray?
    }
}