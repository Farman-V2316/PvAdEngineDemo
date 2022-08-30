/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.app.analytics

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.client.NhAnalyticsEventHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.common.model.entity.LifeCycleEvent
import com.newshunt.dataentity.viral.model.entity.UiEvent
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.Usecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList

/**
 * @author shrikant.agrawal
 * Helper file for handling the UI Screens
 */

private const val SCREEN_ID_ALL = -2
private const val PAUSE_WAIT_TIME = 2 * 60000L

object UiEventsPersistentHelper {

	private val uiHandler: Handler
	private val fireUsecase = FireEventsUsecase().toMediator2(scheduler = Schedulers.single())

	init {
		uiHandler = object : Handler(Looper.getMainLooper()) {

			override fun handleMessage(msg: Message) {
				if (msg == null) {
					return
				}
				if (msg.arg1 == LifeCycleEvent.DESTROYED) {
					fireItems(LifeCycleEvent(msg.what, LifeCycleEvent.DESTROYED))
				}
			}
		}
	}

	fun postEvent(uiEvent: UiEvent) {
		InsertUiEventUsecase().toMediator2().execute(uiEvent)
	}

	fun onAppStart() {
		val message = Message()
		message.what = SCREEN_ID_ALL
		message.arg1 = LifeCycleEvent.DESTROYED
		uiHandler.removeMessages(message.what)
		uiHandler.sendMessage(message)
	}

	fun fireItems(event: LifeCycleEvent) {
		fireUsecase.execute(event.screenId)
	}

	fun onActivityPaused(screenId: Int) {
		val message = Message()
		message.what = screenId
		message.arg1 = LifeCycleEvent.DESTROYED
		uiHandler.sendMessageDelayed(message, PAUSE_WAIT_TIME)
	}

	fun onActivityResumed(screenId: Int) {
		val message = Message()
		message.what = screenId
		message.arg1 = LifeCycleEvent.DESTROYED
		uiHandler.removeMessages(message.what)
	}

	fun onActivityDestroyed(screenId: Int) {
		val message = Message()
		message.what = screenId
		message.arg1 = LifeCycleEvent.DESTROYED
		uiHandler.removeMessages(message.what)
		uiHandler.sendMessage(message)
	}
}


class InsertUiEventUsecase : Usecase<UiEvent, Any> {

	override fun invoke(p1: UiEvent): Observable<Any> {
		return Observable.fromCallable {

			val storedEntity = SocialDB.instance().uiEventDao().getStoredEvent(p1.id())
			if (storedEntity != null) {
				val storedUiEvent = storedEntity.toUiEvent(f1 = ::converStringToNhParam, f2 = ::converStringToDynamicParam)
				updateParams(storedUiEvent, p1)
			} else {
				updateTimespentArray(p1)
			}
			val newUiEvent = p1.uiEventEntity(f1 = ::convertNhParamToString, f2 = ::convertDynamicParamToString)
			SocialDB.instance().uiEventDao().insReplace(newUiEvent)
		}
	}

	private fun updateTimespentArray(newEvent: UiEvent) {

		val newNhParams = newEvent.nhParams
		val timeSpentArray =  ArrayList<Long>()
		val timespent = newNhParams[AnalyticsParam.TIMESPENT.getName()] as? Long?:0L
		if (timespent > 0) {
			timeSpentArray.add(timespent)
			newNhParams[AnalyticsParam.TIMESPENT_ARRAY.getName()] = timeSpentArray.toString()
				.replace(Constants.OPENING_BRACKET, Constants.EMPTY_STRING)
				.replace(Constants.CLOSING_BRACKET, Constants.EMPTY_STRING)
		}
	}

	private fun updateParams(oldEvent: UiEvent, newEvent: UiEvent) {
		val newNhParams = newEvent.nhParams
		val oldNhParams = oldEvent.nhParams

		val timespentArrayType = object : TypeToken<List<Long>>() {}.type

		val timespent = newNhParams[AnalyticsParam.TIMESPENT.getName()] as? Long?:0L
		if (timespent > 0) {
			var timeSpentArray: ArrayList<Long>? = null
			val timeSpentArrayObject = oldNhParams[AnalyticsParam.TIMESPENT_ARRAY.getName()]
			if (timeSpentArrayObject != null) {
				timeSpentArray = JsonUtils.fromJson<ArrayList<Long>>(
					Constants.OPENING_BRACKET + timeSpentArrayObject +
						Constants.CLOSING_BRACKET, timespentArrayType)
			}

			if (timeSpentArray == null) {
				timeSpentArray = ArrayList()
				val oldTimeSpent = oldNhParams[AnalyticsParam.TIMESPENT.getName()] as Double
				if (oldTimeSpent.compareTo(0.0) != 0) {
					timeSpentArray.add(oldTimeSpent.toLong())
				}
			}
			timeSpentArray.add(timespent)
			newNhParams[AnalyticsParam.TIMESPENT_ARRAY.getName()] = timeSpentArray.toString()
				.replace(Constants.OPENING_BRACKET, Constants.EMPTY_STRING)
				.replace(Constants.CLOSING_BRACKET, Constants.EMPTY_STRING)
		}

		// pick referrer from old params as referrer should always be picked from first value
		if (oldNhParams[NhAnalyticsAppEventParam.REFERRER.getName()] != null) {
			newNhParams[NhAnalyticsAppEventParam.REFERRER.getName()] = oldNhParams[NhAnalyticsAppEventParam.REFERRER.getName()]
			newNhParams[NhAnalyticsAppEventParam.REFERRER_ID.getName()] = oldNhParams[NhAnalyticsAppEventParam.REFERRER_ID.getName()]
		}

		// pick referrer from old params as referrer should always be picked from first value
		if (oldNhParams[NhAnalyticsAppEventParam.REFERRER_ACTION.getName()] != null) {
			newNhParams[NhAnalyticsAppEventParam.REFERRER_ACTION.getName()] = oldNhParams[NhAnalyticsAppEventParam.REFERRER_ACTION.getName()]
		}

		// pick referrer from old params as referrer raw should always be picked from first value
		if (oldNhParams[AnalyticsParam.REFERRER_RAW.getName()] != null) {
			newNhParams[AnalyticsParam.REFERRER_RAW.getName()] = oldNhParams[AnalyticsParam.REFERRER_RAW.getName()]
		}

		// fill story card seen from old entry
		if (oldNhParams[AnalyticsParam.IS_SCV.getName()] != null) {
			newNhParams[AnalyticsParam.IS_SCV.getName()] = oldNhParams[AnalyticsParam.IS_SCV.getName()]
		}

		// fill story card seen from old entry
		if (oldNhParams[AnalyticsParam.IS_SPV.getName()] != null) {
			newNhParams[AnalyticsParam.IS_SPV.getName()] = oldNhParams[AnalyticsParam.IS_SPV.getName()]
		}

		if (oldNhParams[AnalyticsParam.IS_CLICKED.getName()] != null) {
			newNhParams[AnalyticsParam.IS_CLICKED.getName()] = oldNhParams[AnalyticsParam.IS_CLICKED.getName()]
		}
	}
}

class MarkStoryCardClickUsecase: BundleUsecase<Any> {

	override fun invoke(p1: Bundle): Observable<Any> {
		return Observable.fromCallable {
			val eventId = p1.getString(UIEVENT_EVENTID)?:return@fromCallable
			val uid =  p1.getString(UIEVENT_UID)?:return@fromCallable
			val event = p1.getString(UIEVENT_EVENT)?:return@fromCallable
			val section = p1.getString(NewsConstants.DH_SECTION) ?: return@fromCallable

			val params = mutableMapOf<String, Any?>()
			params[AnalyticsParam.IS_CLICKED.getName()] = true
			val uiEvent = UiEvent(uid = uid, event = event, eventId = eventId, section = section,
				nhParams = params, dynamicParams = null)

			val storedEvent = SocialDB.instance().uiEventDao().getStoredEvent(id = uiEvent.id())
			storedEvent?.let {
				val storedUiEvent = it.toUiEvent(f1 = ::converStringToNhParam, f2 = ::converStringToDynamicParam)
				storedUiEvent.nhParams[AnalyticsParam.IS_CLICKED.getName()] = true
				val newUiEvent = storedUiEvent.uiEventEntity(f1 = ::convertNhParamToString, f2 = ::convertDynamicParamToString)
				SocialDB.instance().uiEventDao().insReplace(newUiEvent)
			}
		}
	}

	companion object {
		const val UIEVENT_EVENTID = "uiEvent_eventId"
		const val UIEVENT_UID = "uiEvent_uid"
		const val UIEVENT_EVENT = "uiEvent_event"
		const val VIDEO_BUFFER_TIME_TAG = "VIDEO_BUFFER_TIME_TAG"
		const val VIDEO_LOAD_TIME_TAG = "VIDEO_LOAD_TIME_TAG"
	}
}

fun convertNhParamToString(map: MutableMap<String, Any?>): String {
	return JsonUtils.toJson(map)
}

fun convertDynamicParamToString(map: MutableMap<String, String>?) : String {
	return JsonUtils.toJson(map)
}

fun converStringToNhParam(value: String): MutableMap<String, Any?> {
	val type = object : TypeToken<MutableMap<String, Any?>>() {}.type
	val data: MutableMap<String, Any?>? =  JsonUtils.fromJson(value, type)
	return data ?: HashMap()
}

fun converStringToDynamicParam(value: String?) : MutableMap<String, String>? {
	val type = object : TypeToken<Map<String, String>>() {}.type
	return JsonUtils.fromJson(value, type)
}

class FireEventsUsecase : Usecase<Int, Any> {

	override fun invoke(p1: Int): Observable<Any> {
		return Observable.fromCallable {
			val eventList =
			if (p1 == SCREEN_ID_ALL) {
				 SocialDB.instance().uiEventDao().getAllEvents()
			} else  {
				SocialDB.instance().uiEventDao().getEventsForScreenId(p1.toString())
			}

			eventList.forEach { it ->
				val event = it.toUiEvent(f1 = ::converStringToNhParam, f2 = ::converStringToDynamicParam)
				val processedStringMap = event.nhParams
				processedStringMap[NhAnalyticsAppEventParam.PAGE_VIEW_EVENT.getName()] = false.toString() // always spv is false
				if (event.dynamicParams != null) {
					processedStringMap.putAll(event.dynamicParams!!)
				}
				val baseEvents = NhAnalyticsEventHelper.getBaseParams(ClientInfoHelper.getClientId())
				baseEvents.forEach { entry ->
					processedStringMap[entry.key.getName()] = entry.value
				}
				val stateEvents = NhAnalyticsAppState.getInstance().getStateParams(true)
				stateEvents.forEach {entry ->
					if(processedStringMap[entry.key.getName()] == null)
						processedStringMap[entry.key.getName()] = entry.value
				}
				AnalyticsClient.logStringParamsBasedEvents(event.event, AnalyticsHelper2.getSection(event.section), processedStringMap)
				SocialDB.instance().uiEventDao().deleteEvent(it.id)
			}
		}
	}

}