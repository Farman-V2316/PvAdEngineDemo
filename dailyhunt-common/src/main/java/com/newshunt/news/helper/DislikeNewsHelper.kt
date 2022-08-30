/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

/**
 * author madhuri.pa
 *
 * Re-using functionality from MenuService due to module dependency, to be used in notification-
 * dedup
 */

package com.newshunt.news.helper

import androidx.core.util.Pair
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.news.model.entity.MenuEntity
import com.newshunt.dhutil.Expirable

private abstract class G(any: Any) : List<Expirable<MenuEntity>>

class DislikeNewsHelper {
    companion object {
        private val dislikePref = GenericAppStatePreference.DISLIKED_LIST
        @JvmStatic
        fun dislikeNewsIds(): List<Pair<String, String>>? {
            val initialjson = PreferenceManager.getPreference(dislikePref, "")
            val initialList = CommonUtils.GSON.fromJson<List<Expirable<MenuEntity>>>(initialjson, G::class.java)
            val returnValue = initialList?.map { Pair(it.value.itemId, it.value.groupType) }
            return returnValue ?: emptyList()

        }
    }
}