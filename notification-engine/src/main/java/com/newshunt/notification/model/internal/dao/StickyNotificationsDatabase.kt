/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.internal.dao

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.OptReason
import com.newshunt.notification.model.manager.START_STICKY_SERVICE_EXECUTOR
import com.newshunt.notification.model.manager.StickyNotificationsManager
import java.io.Serializable

/**
 * @author santhosh.kc
 * Database for StickyNotifications.
 */

private const val CURRENT_DB_VERSION = 2
private const val STICKY_NOTIFICATIONS_DB_NAME = "sticky_notifications.db"
private const val STICKY_NOTIFICATIONS_TABLE_NAME = "table_sticky_notifications"

private const val COL_ID = "id"
private const val COL_META_API = "metaUrl"
private const val COL_TYPE = "type"
private const val COL_PRIORITY = "priority"
private const val COL_START_TIME = "startTime"
private const val COL_EXPIRY_TIME = "expiryTime"
private const val COL_CHANNEL ="channel"
private const val COL_DATA = "data"
private const val COL_OPT_STATE = "optState"
private const val COL_OPT_REASON = "optReason"
private const val COL_LIVE_OPT_IN = "liveOptIn"
private const val COL_JOB_STATUS = "jobStatus"
private const val COL_META_URL_ATTEMPTS = "metaUrlAttempts"
private const val COL_CHANNEL_ID = "channelId"

@Entity(tableName = STICKY_NOTIFICATIONS_TABLE_NAME, primaryKeys = [COL_ID, COL_TYPE])
data class StickyNotificationEntity(
	@ColumnInfo(name = COL_ID) val id: String,
	@ColumnInfo(name = COL_META_API) val metaUrl: String? = null,
	@ColumnInfo(name = COL_TYPE) val type: String,
	@ColumnInfo(name = COL_PRIORITY, typeAffinity = ColumnInfo.INTEGER) val priority: Int? =
                Integer.MIN_VALUE,
	@ColumnInfo(name = COL_START_TIME, typeAffinity = ColumnInfo.INTEGER) val startTime:
        Long? = 0,
	@ColumnInfo(name = COL_EXPIRY_TIME, typeAffinity = ColumnInfo.INTEGER) val expiryTime: Long? = 0,
	@ColumnInfo(name = COL_CHANNEL) val channel : String? = null,
	@ColumnInfo(name = COL_DATA, typeAffinity = ColumnInfo.BLOB) val data: ByteArray? = null,
	@ColumnInfo(name = COL_OPT_STATE) val optState : StickyOptState? = null,
	@ColumnInfo(name = COL_OPT_REASON) val optReason : OptReason? = OptReason.SERVER,
	@ColumnInfo(name = COL_LIVE_OPT_IN, typeAffinity = ColumnInfo.INTEGER) val isLiveOptIn :
        Boolean? = false,
	@ColumnInfo(name = COL_JOB_STATUS) val jobStatus: StickyNotificationStatus? =
                StickyNotificationStatus.UNSCHEDULED,
	@ColumnInfo(name = COL_META_URL_ATTEMPTS, typeAffinity = ColumnInfo.INTEGER) val
        metaUrlAttempts : Int = 0,
    @ColumnInfo(name = COL_CHANNEL_ID) val channelId: String? = null)
    : Serializable {

    override fun toString(): String {
        return "(id : $id, metaUrl : " + (metaUrl ?: Constants.EMPTY_STRING) + " ,type : " + type +
                " ,priority : " + (priority ?: Int.MIN_VALUE) + " " +
                ",startTime : " + (startTime ?: 0) + " ,expiryTime : " + (expiryTime ?: 0) + " ," +
                "channel : " + (channel ?: Constants.EMPTY_STRING) + " ,data present?" + (data !=
                null) + " ,optState : " + (optState ?: StickyOptState.OPT_OUT) + " ,optReason : " +
                ""+ (optReason ?: OptReason.SERVER) + " ,isLiveOptIn : " +
                (isLiveOptIn ?: false) + " , jobStatus : " + (jobStatus ?:
        StickyNotificationStatus.UNSCHEDULED) + " , metaUrlAttemptsCount : $metaUrlAttempts" + " ," + "channelId : " + (channelId ?: Constants.EMPTY_STRING) + ")"
    }
}

enum class StickyNotificationStatus : Serializable {
    UNSCHEDULED, SCHEDULED, ONGOING, COMPLETED;

    companion object {

        fun from(statusStr: String?): StickyNotificationStatus {
            StickyNotificationStatus.values().forEach { if (CommonUtils.equalsIgnoreCase(statusStr, it.name)) return it }
            return UNSCHEDULED
        }
    }
}

enum class StickyOptState : Serializable {
    OPT_IN, OPT_OUT;

    companion object {
        fun from(optStateStr: String?): StickyOptState {
            StickyOptState.values().forEach {
                if (CommonUtils.equalsIgnoreCase(optStateStr, it.name)) return it
            }
            return OPT_OUT
        }
    }
}

private class StickyDBTypeConverter {

    @TypeConverter
    fun toStatus(statusStr: String?) : StickyNotificationStatus{
        return StickyNotificationStatus.from(statusStr)
    }

    @TypeConverter
    fun toStatusListString(statusList : List<StickyNotificationStatus>) : List<String> {
        val stringList = ArrayList<String>()
        statusList.forEach {
            stringList.add(toStatusString(it))
        }
        return stringList
    }

    @TypeConverter
    fun toStatusString(status: StickyNotificationStatus?) : String {
        return status?.name ?: StickyNotificationStatus.UNSCHEDULED.name
    }

    @TypeConverter
    fun toOptState(optStateStr: String?) : StickyOptState {
        return StickyOptState.from(optStateStr)
    }

    @TypeConverter
    fun toOptStateString(optState: StickyOptState?) : String {
        return optState?.name ?: StickyOptState.OPT_OUT.name
    }

    @TypeConverter
    fun toOptReason(optStateStr : String?) : OptReason {
        return OptReason.from(optStateStr)
    }

    @TypeConverter
    fun toOptReasonString(optReason: OptReason?) : String {
        return optReason?.name ?: OptReason.SERVER.name
    }
}

@Database(entities = [StickyNotificationEntity::class], version = CURRENT_DB_VERSION)
@TypeConverters(StickyDBTypeConverter::class)
abstract class StickyNotificationsDatabase : RoomDatabase() {
    abstract fun stickyNotificationDao(): StickyNotificationDao
}

val StickyNotificationsDBInstance by lazy {
    Room.databaseBuilder(CommonUtils.getApplication(), StickyNotificationsDatabase::class.java,
            STICKY_NOTIFICATIONS_DB_NAME)
            .addMigrations(MIGRATION_1_2)
            .addCallback(DatabaseCreateCallback())
            .allowMainThreadQueries().build()
}

private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${STICKY_NOTIFICATIONS_TABLE_NAME} ADD COLUMN ${COL_CHANNEL_ID} TEXT")
    }
}

class DatabaseCreateCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        START_STICKY_SERVICE_EXECUTOR.execute {
            cleanUpExpiredAndInValidNotifications(db)
        }
    }

    private fun cleanUpExpiredAndInValidNotifications(db: SupportSQLiteDatabase) {
        val currentTime = System.currentTimeMillis()
        db.execSQL("DELETE FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_EXPIRY_TIME < " +
                "$currentTime OR $COL_EXPIRY_TIME < $COL_START_TIME OR $COL_META_URL_ATTEMPTS >= " +
                "${StickyNotificationsManager.MAX_META_RETRY_ATTEMPTS}")
    }
}

@Dao
interface StickyNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotification(stickyNotificationEntity: StickyNotificationEntity)

    @Query("SELECT * FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_ID = :id AND $COL_TYPE = :type")
    fun getNotificationByIdAndType(id : String, type: String) : StickyNotificationEntity?

    @Query("SELECT * FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_ID = :id AND $COL_TYPE = :type")
    fun getNotificationByIdAndTypeLivedata(id : String, type: String) : LiveData<StickyNotificationEntity?>

    @Query("SELECT * FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_JOB_STATUS = :status AND $COL_TYPE = :type AND $COL_OPT_STATE = 'OPT_IN' ORDER BY $COL_START_TIME ASC")
    fun getNotificationsByStatus(status: StickyNotificationStatus, type: String): List<StickyNotificationEntity>?

    @Query("SELECT * FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_OPT_REASON = :optReason ORDER BY $COL_START_TIME ASC")
    fun getNotificationsByOptReasonIncludingAllOptState(optReason: OptReason) : List<StickyNotificationEntity>?

    @Query("SELECT * FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_JOB_STATUS = :status AND $COL_TYPE = :type ORDER BY $COL_START_TIME ASC")
    fun getNotificationsByStatusIncludingAllOptState(status: StickyNotificationStatus, type: String): List<StickyNotificationEntity>?

    @Query("SELECT * FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_JOB_STATUS = :status ORDER BY $COL_START_TIME ASC")
    fun getNotificationsByStatusIncludingAllOptState(status: StickyNotificationStatus): List<StickyNotificationEntity>?

    @Query("UPDATE $STICKY_NOTIFICATIONS_TABLE_NAME SET $COL_JOB_STATUS = :jobStatus WHERE $COL_ID = :id AND $COL_TYPE = :type")
    fun markNotificationStatus(id: String, type : String, jobStatus: StickyNotificationStatus)

    @Query("UPDATE $STICKY_NOTIFICATIONS_TABLE_NAME SET $COL_JOB_STATUS = :toStatus WHERE $COL_JOB_STATUS = :fromStatus AND $COL_TYPE = :type")
    fun changeNotificationsJobStatus(fromStatus : StickyNotificationStatus, toStatus : StickyNotificationStatus, type: String)

    @Query("UPDATE $STICKY_NOTIFICATIONS_TABLE_NAME SET $COL_LIVE_OPT_IN = '0' WHERE $COL_LIVE_OPT_IN = '1'")
    fun markPrevLiveNotificationsAsNonLive()

    @Query("UPDATE $STICKY_NOTIFICATIONS_TABLE_NAME SET $COL_OPT_STATE = 'OPT_OUT', " +
            "$COL_JOB_STATUS = :status, $COL_OPT_REASON = :optReason WHERE $COL_ID = :id AND $COL_TYPE = :type")
    fun markNotificationOptOut(id : String, type: String, optReason: OptReason, status: StickyNotificationStatus)

    @Query("UPDATE $STICKY_NOTIFICATIONS_TABLE_NAME SET $COL_CHANNEL_ID = :channelId, $COL_META_API = :metaUrl WHERE $COL_ID = :id AND $COL_TYPE = :type")
    fun updateNotificationDataWithFetchedConfigData(id: String, type: String, channelId: String, metaUrl: String)

    @Query("UPDATE $STICKY_NOTIFICATIONS_TABLE_NAME SET $COL_START_TIME = :startTime, $COL_EXPIRY_TIME = :expiryTime WHERE $COL_ID = :id AND $COL_TYPE = :type")
    fun updateNotificationDataWith(id: String, type: String, startTime: Long, expiryTime: Long)

    @Query("DELETE FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_ID = :id AND type = :type")
    fun deleteNotification(id : String, type: String)

    @Query("SELECT $COL_ID FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_OPT_STATE = 'OPT_IN' AND $COL_TYPE = :type")
    fun getOptInNotificationIds(type : String) : List<String>?

    @Query("SELECT $COL_OPT_STATE FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_ID = :id AND $COL_TYPE = :type")
    fun getOptInState(id : String, type : String) : StickyOptState?

    @Query("SELECT $COL_ID FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_ID IN (:idsToCheck) AND $COL_TYPE = :type AND $COL_OPT_STATE = 'OPT_IN'")
    fun getOptInSeriesFrom(idsToCheck: List<String>, type: String): List<String>?

    @Query("SELECT * FROM $STICKY_NOTIFICATIONS_TABLE_NAME WHERE $COL_JOB_STATUS = 'UNSCHEDULED' AND $COL_TYPE= :type AND $COL_EXPIRY_TIME < :expiryTime AND $COL_EXPIRY_TIME > :currentTime AND $COL_PRIORITY >= :priority AND $COL_OPT_STATE = 'OPT_IN' AND $COL_META_URL_ATTEMPTS < ${StickyNotificationsManager.MAX_META_RETRY_ATTEMPTS} ORDER BY $COL_START_TIME ASC")
    fun getMatchingUnscheduledNotifications(expiryTime : Long, priority : Int,
                                            currentTime : Long = System.currentTimeMillis(), type: String) : List<StickyNotificationEntity>?
}