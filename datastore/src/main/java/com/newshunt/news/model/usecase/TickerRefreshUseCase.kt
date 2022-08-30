/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import android.os.SystemClock
import com.newshunt.common.helper.common.Logger
import com.newshunt.news.model.apis.TickerApi2
import com.newshunt.news.model.sqlite.SocialDB
import io.reactivex.Observable
import javax.inject.Inject

/**
 * make api call and update the DB
 * @author madhuri.pa
 */

class TickerRefreshUseCase @Inject constructor(val tickerApi: TickerApi2) :
        BundleUsecase<List<Any>> {

    override fun invoke(p1: Bundle): Observable<List<Any>> {

        val url = p1.getSerializable(TICKER_URL) as String?
        val id = p1.getSerializable(TICKER_ID) as String
        if (url != null) {
            Logger.d(LOG_TAG, "calling api for $id = $url")
            return tickerApi.refreshTicker(url).map { multiValueResponse ->
                multiValueResponse.data.rows.forEach {
                    Logger.d(LOG_TAG, "updating $id ${it.tickerRefreshTime}")
                    SocialDB.instance().postDao().updatePost(it.copy(
                            localLastTickerRefreshTime = SystemClock.uptimeMillis(), id = id))
                }
                multiValueResponse.data.rows
            }
        } else {
            return Observable.empty()
        }
    }

    companion object {
        const val TICKER_URL = "ticker_url"
        const val TICKER_ID = "ticker_id"
        const val LOG_TAG = "TickerRefreshUseCase"
    }
}
