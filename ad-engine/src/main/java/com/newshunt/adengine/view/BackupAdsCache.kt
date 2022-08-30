/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view

import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.version.AdRequest

/**
 * @author raunak.yadav
 */
interface BackupAdsCache {
    fun getBackupAd(adRequest: AdRequest?): BaseAdEntity?
}