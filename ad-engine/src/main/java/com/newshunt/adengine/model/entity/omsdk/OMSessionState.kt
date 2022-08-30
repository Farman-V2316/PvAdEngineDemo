/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity.omsdk

import com.iab.omid.library.versein.adsession.AdSession
import com.iab.omid.library.versein.adsession.Owner

import java.io.Serializable

/**
 * @author raunak.yadav
 */
class OMSessionState(var adSession: AdSession?,
                     val impressionOwner: Owner) : Serializable {

    /**
     * Finish @[AdSession] to stop tracking.
     */
    fun finish() {
        adSession?.finish()
        adSession = null
    }

    companion object {
        private const val serialVersionUID = 131609157356680781L
    }
}