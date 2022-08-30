/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.news.model.daos.FetchDao
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

/**
 * To be called when fragment is destroyed.
 * @author satosh.dhanyamraju
 */
class CleanUpFetchUsecase
@Inject constructor(@Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    private val fetchDao: FetchDao,
                    private val cancelNetworkSDKRequestsUsecase: CancelNetworkSDKRequestsUsecase) : SingleUsecase<Bundle,  Boolean> {

    override fun invoke(p1: Bundle): Single<Boolean> {
        return Single.fromCallable {
            /*if (location != Constants.FETCH_LOCATION_LIST) */
            fetchDao.fullCleanupFetch(entityId, location, section)
            /*else fetchDao.cleanUpFetch(entityId, location, section = section)*/
            true
        }.flatMap {
            cancelNetworkSDKRequestsUsecase(p1)
        }
    }

}