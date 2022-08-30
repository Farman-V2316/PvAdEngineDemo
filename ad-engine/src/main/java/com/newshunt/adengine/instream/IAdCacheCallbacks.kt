/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.instream

interface IAdCacheCallbacks {

    val timeSinceLastAdPlayer: Long

    fun onAdReponseReceived(inStreamAdsHelper: IAdHelper)

    fun onAdResponseError(inStreamAdsHelper: IAdHelper)

}
