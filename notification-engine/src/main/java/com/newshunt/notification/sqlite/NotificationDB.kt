/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.sqlite

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.notification.model.entity.*
import com.newshunt.notification.model.internal.dao.*

/**
 * @author aman.roy
 */

@Database(entities = [NotificationEntity::class,
        NotificationPresentEntity::class,
        NotificationDeleteEntity::class, NotificationPrefetchEntity::class, InAppNotificationEntity::class],
        version = 6)
@TypeConverters(NotificationTypeConv::class)
abstract class NotificationDB : RoomDatabase() {

    abstract fun getNotificationDao() : NotificationDao
    abstract fun getNotificationPrefetchInfoDao(): NotificationPrefetchInfoDao
    abstract fun getInAppNotificationDao() : InAppNotificationDao

    companion object {

        private var INST: NotificationDB? = null

        /**
         * creates a new instance if current instance is already closed
         */
        @JvmStatic
        @JvmOverloads
        fun instance(context: Context = CommonUtils.getApplication(), inMemoryDB: Boolean = false):
                NotificationDB {
            if (INST == null) {
                synchronized(this) {
                    if (INST == null) {
                        Logger.d(TAG, "[${Thread.currentThread().name}] creating new connection. $INST")
                        INST = if (inMemoryDB) {
                            Room.inMemoryDatabaseBuilder(context, NotificationDB::class.java)
                                    .allowMainThreadQueries()
                                    .build()
                        } else Room.databaseBuilder(context, NotificationDB::class.java, "notifications.db")
                                .addCallback(NotificationDBCallback())
                                .addMigrations(DBUpgradeHelper1_2)
                                .addMigrations(DBUpgradeHelper2_3)
                                .addMigrations(DBUpgradeHelper3_4)
                                .addMigrations(DBUpgradeHelper4_5)
                                .addMigrations(DBUpgradeHelper5_6)
                                .build()
                    }
                }
            }
            return INST!!
        }

        /**
         * should be closed on app exit
         */
        @JvmStatic
        fun closeConnection() {
            INST?.close()
        }

        const val TAG = "notifications.db"
    }

    /**
     * No need to call this function.
     * If and when called, we need to make sure companion's INST is set to null, so that a new
     * connection will be created next time.
     */
    override fun close() {
        super.close()
        INST = null
    }
}

/**
*migration helper for db version update from 2 to 1
*/
val DBUpgradeHelper1_2 = object :Migration(1, 2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE 'notification_cache_details' ('pk' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'id' TEXT NOT NULL," +
                " 'post_notification' INTEGER NOT NULL," +
                " 'retry_number' INTEGER NOT NULL," +
                " 'last_retry_time' INTEGER NOT NULL," +
                " 'received_time' INTEGER NOT NULL," +
                " 'notification_cached' INTEGER NOT NULL," +
                " FOREIGN KEY(`id`) REFERENCES `notification_info`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
    }
}

val DBUpgradeHelper2_3 = object :Migration(2, 3){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE 'notification_info' ADD COLUMN 'description' TEXT")
        database.execSQL("ALTER TABLE 'notification_info' ADD COLUMN 'sticky_item_type' TEXT NOT NULL DEFAULT '${NotificationConstants.STICKY_NONE_TYPE}'")
    }
}

val DBUpgradeHelper3_4 = object :Migration(3, 4){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE 'notification_info' ADD COLUMN 'disable_events' INTEGER NOT NULL DEFAULT 0")
    }
}

val DBUpgradeHelper4_5 = object :Migration(4, 5){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE 'notification_info' ADD COLUMN 'displayed_at_timestamp' TEXT")
    }
}

val DBUpgradeHelper5_6 = object :Migration(5, 6){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `in_app_notification` (`pk` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `id` TEXT NOT NULL, `data` BLOB, `time_stamp` INTEGER, `priority` INTEGER, `language` TEXT, `endTime` INTEGER, `inAppNotificationCtaLink` TEXT, `in_app_notification_info` TEXT NOT NULL, `disable_lang_filter` INTEGER NOT NULL DEFAULT 0,`status` TEXT NOT NULL )")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_in_app_notification_id` ON `in_app_notification` (`id`)")
    }
}

class NotificationDBCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CommonUtils.runInBackground {loadPreloadedData()}
    }

    fun loadPreloadedData() {
        try {
            val allNotificationInfoModels = NotificationDaoImpl.getInstance().allNotificationInfoEntries
            val allDeleteEntries = NotificationDaoImpl.getInstance().allNotificationDeleteEntries
            val allPresentEntries = NotificationDaoImpl.getInstance().allNotificationPresentEntries
            Logger.d("NotificationDB", "Sizes ( " + allDeleteEntries.size + " " + allNotificationInfoModels.size + " " + allPresentEntries.size + " )")
            val notificationDao = NotificationDB.instance().getNotificationDao()
            notificationDao.insertNotification(allNotificationInfoModels);
            notificationDao.insertNotificationId(allPresentEntries);
            notificationDao.insertDeleteNotificationWithConflict(allDeleteEntries)
            Logger.d("NotificationDB", "All the DB inserted, now will delete DB")
            CommonUtils.getApplication().deleteDatabase(NotificationSQLIteHelper.SQLITE_DB_NAME)
            Logger.d("NotificationDB", "DB Deleted")
        } catch(e:Exception) {
            Logger.d("NotificationDB","Data Migration from SQLite to Room failed")
            Logger.caughtException(e)
        }
    }
}