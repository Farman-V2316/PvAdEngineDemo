/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.viral.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam

/**
 * @author shrikant.agrawal
 */
@Entity(tableName = "UiEvent")
data class UiEventEntity(@PrimaryKey val id: Int,
                         val uid: String,
                         val eventId: String,
                         val section: String,
                         val event: String,
                         val nhParams: String,
                         val dynamicParams: String?) {

	 fun toUiEvent(f1: (String) -> MutableMap<String, Any?>, f2: (String?) -> MutableMap<String, String>?) : UiEvent {
		 return UiEvent(uid = uid,
			            eventId = eventId,
			            section = section,
			            event = event,
			            nhParams = f1.invoke(nhParams),
			            dynamicParams = f2.invoke(dynamicParams))
	 }
}

data class UiEvent(val uid: String,
                   val eventId: String,
                   val section: String,
                   val event: String,
                   val nhParams: MutableMap<String, Any?>,
                   val dynamicParams: MutableMap<String, String>?) {

	fun uiEventEntity(f1: (MutableMap<String, Any?>) -> String, f2: (MutableMap<String, String>?) -> String?) : UiEventEntity {
		return UiEventEntity(
			id = id(),
			uid = uid,
			eventId = eventId,
			event =  event,
			section = section,
			nhParams =  f1.invoke(nhParams),
			dynamicParams = f2.invoke(dynamicParams))
	}

	fun id() = (uid + eventId + event).hashCode()
}