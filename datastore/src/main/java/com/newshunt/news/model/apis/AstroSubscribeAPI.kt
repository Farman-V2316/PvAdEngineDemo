package com.newshunt.news.model.apis

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.server.AstroSubscriptionRequest
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AstroSubscribeAPI {

	@POST("/api/v2/subscription/user/mdm")
	fun subscribeAstro(@Body astroSubscriptionRequest: AstroSubscriptionRequest,
	                   @Query("langCode") langCode: String = UserPreferenceUtil.getUserLanguages(),
	                   @Query("appLanguage") appLanguage: String = UserPreferenceUtil.getUserNavigationLanguage(),
	                   @Query("section") section: String = PageSection.NEWS.section) : Observable<ApiResponse<PageEntity>>
}
