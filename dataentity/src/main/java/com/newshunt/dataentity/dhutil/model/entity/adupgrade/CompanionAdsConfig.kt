/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.adupgrade

import androidx.annotation.IntDef
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.CompanionAdsConfig.CompanionDisplayType.Companion.COLLAPSE
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.CompanionAdsConfig.CompanionDisplayType.Companion.EXPAND
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.CompanionAdsConfig.CompanionDisplayType.Companion.HIDE
import java.io.Serializable
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @author raunak.yadav
 */
class CompanionAdsConfig : Serializable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = [HIDE, EXPAND, COLLAPSE])
    annotation class CompanionDisplayType {
        companion object {
            const val HIDE = 0
            const val EXPAND = 1
            const val COLLAPSE = 2
        }
    }

    // Do not show companion ad if aspectRatio is less than this.
    val videoAspectRatioLimit: Float = 1.77f

    @CompanionDisplayType
    val showAfterVideoAd: Int = HIDE

    val collapseHeading: String? = null
    val expandHeading: String? = null

}