package com.newshunt.adengine.listeners

import com.newshunt.adengine.model.AdInteraction
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity

/**
 * To handle app exit via ad actions.
 *
 * @author raunak.yadav
 */
interface AdExitListener {

    fun cancelExitApp(adInteraction: AdInteraction? = null)
    fun closeToExitApp(adEntity: BaseDisplayAdEntity?, adInteraction: AdInteraction)

    fun closeToExitAppwithDelay(adEntity: BaseDisplayAdEntity?,
                                adInteraction: AdInteraction,
                                delay: Long,
                                onAdCloseCallback: (() -> Unit)? = null)
}