/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.newshunt.dataentity.dhutil.analytics.SessionInfo

/**
 * @author shrikant.agrawal
 */

@Dao
abstract class SessionDao {

	@Query("SELECT * FROM `session-info` ORDER BY startTime DESC LIMIT 1")
	abstract fun getCurrentSessionInfo() : SessionInfo?

	@Insert
	abstract fun insert(sessionInfo: SessionInfo)

	@Query("UPDATE `session-info` SET endTime=:endTime WHERE id=:id")
	abstract fun updateEndTime(id:String, endTime: Long)

	@Query("SELECT COUNT(id) FROM `session-info`")
	abstract fun getTotalSessionCounts() : Long

	@Query("DELETE FROM `session-info` WHERE id=:id")
	abstract fun deleteSession(id: String)

	@Query("SELECT * FROM `session-info` ORDER BY startTime DESC LIMIT 1")
	abstract fun getCurrentSessionInfoLivedata(): LiveData<SessionInfo?>
}