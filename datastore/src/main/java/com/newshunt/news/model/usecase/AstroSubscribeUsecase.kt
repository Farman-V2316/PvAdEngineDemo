package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.model.entity.server.AstroSubscriptionRequest
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.news.model.apis.AstroSubscribeAPI
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

class AstroSubscribeUsecase : BundleUsecase<PageEntity> {
	override fun invoke(p1: Bundle): Observable<PageEntity> {
		val api = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null).create(AstroSubscribeAPI::class.java)
		val gender = p1.getString(BUNDLE_GENDER)?: Constants.EMPTY_STRING
		val dob = p1.getString(BUNDLE_DOB) ?: Constants.EMPTY_STRING
		val entityId = p1.getString(BUNDLE_ENTITY_ID) ?: Constants.EMPTY_STRING
		return api.subscribeAstro(AstroSubscriptionRequest(gender = gender, dob = dob, entityId = entityId)).map {
			it.data
		}
	}

	companion object {
		const val BUNDLE_DOB = "bundle_dob"
		const val BUNDLE_GENDER = "bundle_gender"
		const val BUNDLE_ENTITY_ID = "bundle_entityId"
	}
}