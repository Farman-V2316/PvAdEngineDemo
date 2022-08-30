/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.helper

import android.view.ViewGroup
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.EmptyAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.view.view.InstreamAdWrapper
import com.newshunt.adengine.view.viewholder.EmptyAdWrapper

/**
 * Class that handles instream ads from SDKs
 * which provide a self-sufficient view instead of adURL.
 *
 * @author raunak.yadav
 */
class InstreamAdViewFactory {

    companion object {
        private const val TAG: String = "InstreamAdViewFactory"
        fun create(adContainer: ViewGroup?, adEntity: BaseAdEntity?): InstreamAdWrapper? {
            adEntity ?: return null
            adContainer ?: return null

            if (adEntity.adPosition != AdPosition.INSTREAM_VIDEO) {
                AdLogger.d(TAG, "Invalid Instream ad. Adposition is ${adEntity.adPosition}")
                return null
            }

            if (adEntity is EmptyAd) {
                return EmptyAdWrapper(adEntity)
            }
            return null
        }
    }
}