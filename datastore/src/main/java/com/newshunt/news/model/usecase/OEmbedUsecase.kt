/*
 * Created by Rahul Ravindran at 8/1/20 5:05 PM
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.dataentity.common.asset.OEmbedResponse
import com.newshunt.news.model.daos.CreatePostDao
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.Serializable
import javax.inject.Inject

class OEmbedUsecase @Inject constructor(private val api: OEmbedAPI,
                                        private val cpdao: CreatePostDao) : BundleUsecase<OEmbedResponse> {
    companion object {
        @JvmStatic
        val OEMBED_URL = "OEMBED_URL"
        const val ACTION_TYPE = "ombed_action_type"

        enum class OEMBED_ACTION_TYPE : Serializable {
            REMOVE, UPDATE
        }
    }

    override fun invoke(bundle: Bundle): Observable<OEmbedResponse> {
        val oEmbedUrl: String = bundle.getString(OEMBED_URL, "")
        val cpId = bundle.getLong(CpImageInsertUseCase.POST_ID)
        val actionType = bundle.getSerializable(ACTION_TYPE) as? OEMBED_ACTION_TYPE
        check(cpId > 0) { "cpID not passed in bundle" }
        return when (actionType) {
            OEMBED_ACTION_TYPE.REMOVE -> {
                Observable.fromCallable {
                    val entity = cpdao.cpentityByID(cpId.toInt())
                    val response: OEmbedResponse? = null
                    entity?.let { e ->
                        cpdao.update(e.copy(oemb = response))
                    }
                }.subscribeOn(Schedulers.io()).subscribe()
                Observable.empty()
            }
            OEMBED_ACTION_TYPE.UPDATE -> {
                api.getOEmbedData(oEmbedUrl).map {
                    val entity = cpdao.cpentityByID(cpId.toInt())
                    entity?.let { e ->
                        cpdao.update(e.copy(oemb = it))
                    }
                    it
                }
            }
            else -> Observable.empty()
        }
    }
}

    interface OEmbedAPI {
        @GET("/oembed")
        fun getOEmbedData(@Query("url") url: String): Observable<OEmbedResponse>
    }