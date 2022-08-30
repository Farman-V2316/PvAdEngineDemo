/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.model.usecase

import android.net.Uri
import com.newshunt.appview.common.group.model.usecase.InsertIntoGroupDaoUsecase
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

/**
 *  Search cards related use cases
 * <p>
 * Created by srikanth.ramaswamy on 11/15/2019.
 */

/**
 * Usecase implementation to debounce the search queries, compose the search url and then update
 * the feed table with new content url
 */
class SearchCardsUsecase @Inject constructor(private val composeSearchUrlUsecase: ComposeSearchUrlUsecase,
                                             @Named("debounceDelayMs")
                                             private val debounceDelayMs: Long,
                                             @Named("dynamicFeed")
                                             private val dynamicFeed: GeneralFeed,
                                             private val insertIntoGroupDaoUsecase: InsertIntoGroupDaoUsecase,
                                             @Named("requestMethod")
                                             private val requestMethod: String = Constants.HTTP_POST) : Usecase<String?, Unit> {
    private val queryPublisher = PublishSubject.create<String?>()

    init {
        queryPublisher
                .debounce(debounceDelayMs, TimeUnit.MILLISECONDS)
                .flatMap { query ->
                    if (query.isNullOrEmpty().not()) {
                        composeSearchUrlUsecase.invoke(query)
                                .flatMap {
                                    val feedLocal = dynamicFeed.copy(contentUrl = it, contentRequestMethod = requestMethod)
                                    insertIntoGroupDaoUsecase.invoke(listOf(feedLocal))
                                }
                    } else {
                        insertIntoGroupDaoUsecase.invoke(listOf(dynamicFeed))
                    }
                }.subscribe()
    }

    override fun invoke(queryString: String?): Observable<Unit> {
        if (queryString != null) {
            queryPublisher.onNext(queryString)
        }
        return Observable.empty()
    }
}

/**
 * Usecase implementation to compose search url with query parameter.
 */
class ComposeSearchUrlUsecase @Inject constructor(@Named("searchUrl") private val searchUrl: String,
                                                  @Named("queryParam") private val queryParam: String) : Usecase<String?, String> {
    private val LOG_TAG = "ComposeSearchUrlUsecase"
    private val url = Uri.parse(searchUrl)
    override fun invoke(queryString: String?): Observable<String> {
        return Observable.fromCallable {
            val urlFormed = url.buildUpon().appendQueryParameter(queryParam, queryString).toString()
            Logger.d(LOG_TAG, "query url is: $urlFormed")
            urlFormed
        }
    }
}