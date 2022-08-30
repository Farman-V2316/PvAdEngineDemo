package com.newshunt.dataentity.analytics.entity

class DynamicEventParam(val value: String) : NhAnalyticsEventParam {

	override fun getName(): String {
		return value
	}
}