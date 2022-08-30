/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.news.model.utils.TransformNewsList
import javax.inject.Inject
import javax.inject.Named

/**
 * @author amit.chaudhary
 * */
class FetchCacheUsecase @Inject constructor(@Named("apiCacheProvider")
                                            apiCacheProvider: ApiCacheProvider,
                                            @Named("fetchUsecase")
                                            fetchUsecase: BundleUsecase<NLResponseWrapper>,
                                            private val f: TransformNewsList) : CacheUsecase<NLResponseWrapper>(fetchUsecase, apiCacheProvider) {
    private val LOG_TAG = "FetchCacheUsecase"
    override fun afterDecompress(data: NLResponseWrapper): NLResponseWrapper {
        data.nlResp.rows = removeTickers(f.transf(data.nlResp.rows))
        data.nlResp.isFromNetwork = false
        data.nlResp.timeTakenToFetch = 0 // should be set only for network response
        return data
    }

    private fun removeTickers(cards: List<AnyCard>): List<AnyCard> {
        val cList = cards.filterNot {
            it is CommonAsset && it.i_format() == Format.TICKER
        }
        Logger.v(LOG_TAG, "removeTickers: ${cards.size} => ${cList.size}")
        return cList
    }
}
