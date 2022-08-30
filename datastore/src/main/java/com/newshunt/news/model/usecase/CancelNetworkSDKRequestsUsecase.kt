/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.sdk.network.NetworkSDK
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

/**
 *
 * Can be used to cancel network SDK requests
 *
 * @author satosh.dhanyamraju
 */
class CancelNetworkSDKRequestsUsecase @Inject constructor(@Named("apiTag") val tag : String) : SingleUsecase<Bundle,Boolean> {
    override fun invoke(p1: Bundle): Single<Boolean> {
        return Single.fromCallable {
            NetworkSDK.cancel(tag)
            true
        }
    }

}