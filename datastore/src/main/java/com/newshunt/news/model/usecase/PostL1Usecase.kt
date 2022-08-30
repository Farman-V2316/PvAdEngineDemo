/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.dataentity.social.entity.MenuPayload
import com.newshunt.news.model.apis.MenuApi
import com.newshunt.news.model.daos.MenuDao
import io.reactivex.Observable
import javax.inject.Inject

/**
 * To be called when a L1 with clickAction 'POST' is clicked
 *
 * @author satosh.dhanyamraju
 */
class PostL1Usecase @Inject constructor(private val menuApi: MenuApi,
                                        private val menuDao: MenuDao) : BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        val payload = (p1.getSerializable(B_PAYLOAD) as? MenuPayload)
                ?: return Observable.error(Throwable("PostL1Usecase missing payload "))
        return Observable.fromCallable { menuDao.postUrl()?:"" }.flatMap {
            if(it.isEmpty()) Observable.error(Throwable("missing postUrl"))
            else menuApi.postL1(it, payload).map { true }
        }
    }

    companion object {
        private const val B_PAYLOAD = "postl1-payload"
        fun create(payload: MenuPayload,
                   bundle: Bundle = Bundle()) =
                bundle.also {
                    it.putSerializable(B_PAYLOAD, payload)
                }
    }
}