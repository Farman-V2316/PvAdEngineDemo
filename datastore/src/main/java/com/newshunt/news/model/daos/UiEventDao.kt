/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.viral.model.entity.UiEventEntity

/**
 * @author shrikant.agrawal
 */
@Dao
abstract class UiEventDao: BaseDao<UiEventEntity> {

	@Query("SELECT * FROM UiEvent WHERE id=:id")
	abstract fun getStoredEvent(id: Int) : UiEventEntity?

	@Query("SELECT * FROM UiEvent")
	abstract fun getAllEvents() : List<UiEventEntity>

	@Query("SELECT * FROM UiEvent WHERE uid=:uid")
	abstract fun getEventsForScreenId(uid:String): List<UiEventEntity>

	@Query("DELETE FROM UiEvent WHERE id=:id")
	abstract fun deleteEvent(id: Int)
}