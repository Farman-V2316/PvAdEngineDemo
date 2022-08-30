/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.helper

import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.util.AdsUtil

/**
 * A store for ads actively being used in views.
 * Will be cleared when ad is destroyed.
 *
 * @author raunak.yadav
 */
object AdBinderRepo {

    private var ads = HashMap<String, BaseAdEntity>()

    fun getAdById(adId: String): BaseAdEntity? {
        return ads[adId]
    }

    fun add(adEntity: BaseAdEntity?) {
        adEntity ?: return
        ads[adEntity.uniqueAdIdentifier] = adEntity
    }

    fun destroyAds(adIds: List<String>, parentId: Int, viewDestroyed: Boolean = true) {
        adIds.forEach { id ->
            destroyAd(id, parentId, viewDestroyed)
        }
    }

    fun destroyAd(adId: String, parentId: Int, viewDestroyed: Boolean = true) {
        ads[adId]?.let { ad ->
            AdsUtil.destroyAd(ad, parentId, viewDestroyed)
            // Remove only if this is not attached anywhere else.
            if (ad.parentIds.isEmpty()) {
                ads.remove(adId)
            }
        }
    }

    fun clear() {
        ads.clear()
    }
}