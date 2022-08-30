package com.newshunt.dataentity.common.model.entity

import java.io.Serializable

data class CommunicationEventsResponse(val version: String,
                                       val uniqueRequestId: Int = 0,
                                       val events: List<EventsInfo>?=null) : Serializable

data class EventsInfo(val event: String? = null,
                      val id: Int = 0,
                      val resource: String?= null,
                      val activity: EventsActivity?= null,
                      val precondition: Map<String, String>?=null) : Serializable