/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.onboarding.model.entity.datacollection

import android.graphics.drawable.Drawable
import java.io.Serializable

/**
 * @author kajal.kumari on 27/12/21.
 */
class InstalledAppInfo(val packageName: String,val icon: Drawable, val label: String): Serializable{
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val appInfo: InstalledAppInfo = o as InstalledAppInfo
        return if (packageName != null) packageName == appInfo.packageName else appInfo.packageName == null
    }

    override fun hashCode(): Int {
        return packageName.hashCode() ?: 0
    }
}