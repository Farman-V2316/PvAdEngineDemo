/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.view.view

import android.app.Activity
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.version.AdPosition

/**
 * Interface to handle callbacks from DHTVAdsHelper and handle incoming ads
 * <p>
 * Created by srikanth.ramaswamy on 10/18/18.
 */

interface AdHostView {
    fun insertAd(adEntity: BaseAdEntity, adPosition: AdPosition, index: Int = 0) : Int
    fun getActivityContext() : Activity?
}