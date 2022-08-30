/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.news.model.usecase

import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.apis.NewsApi
import com.newshunt.news.model.utils.CardDeserializer
import com.newshunt.news.model.utils.InvalidCardsLogger
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

/**
 * @author raunak.yadav
 */
class FetchContentForAdUsecase(listType : String?) : Usecase<Any, List<PostEntity>> {

    private var api = RestAdapterContainer.getInstance()
            .getDynamicRestAdapterRx(CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer
                    .getApplicationUrl()), Priority.PRIORITY_NORMAL, null,
                    CardDeserializer.gson(listType, object : InvalidCardsLogger {
                        override fun log(message: String, pojo: Any) {
                            Logger.e(TAG, "Ad Card fetch failed : $message")
                        }
                    }))
            .create(NewsApi::class.java)

    override fun invoke(payload: Any): Observable<List<PostEntity>> {
        return api.contentByIds(if (payload is List<*>) payload else listOf(payload))
                .lift(ApiResponseOperator())
                .map {
                    ApiResponseUtils.throwErrorIfDataNull(it)
                    val posts = it.data.rows.filterIsInstance<PostEntity>()
                    if (posts.isNullOrEmpty()) {
                        return@map emptyList<PostEntity>()
                    }
                    posts
                }
    }

    companion object {
        private const val TAG = "FetchContentForAdUsecase"
    }

}