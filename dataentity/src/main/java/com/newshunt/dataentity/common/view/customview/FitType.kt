package com.newshunt.dataentity.common.view.customview

/**
 * Created by karthik.r on 2019-08-26.
 */
enum class FIT_TYPE {
    TOP_CROP, FIT_DISP_WID, FIT_DISP_HEI, FIT_ASTRO, FIT_CENTER, FIT_XY, CENTER_CROP;

    companion object {
        fun fromName(type: String): FIT_TYPE? {
            for (fitType in FIT_TYPE.values()) {
                if (fitType.name.equals(type, ignoreCase = true)) {
                    return fitType
                }
            }
            return null
        }
    }
}

