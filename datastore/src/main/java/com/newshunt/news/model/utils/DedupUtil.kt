/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity

/**
 * Used for deduping cards before they are inserted to DB
 *
 * @author satosh.dhanyamraju
 */
object DedupUtil {
    private const val TAG = "DedupUtil"
    fun removeDuplicates(list: List<PostEntity>?, existingIds: Set<String> = HashSet()): List<PostEntity>? {
        list?:return null
        val result = mutableListOf<PostEntity>()
        val runningSet = HashSet(existingIds)
        list.forEach { pe ->
            if (pe.format == Format.POST_COLLECTION  && pe.collectionAsset?.collectionItem != null) {
                pe.collectionAsset?.collectionItem = removeDuplicates(pe.collectionAsset?.collectionItem, runningSet)
                pe.collectionAsset?.collectionItem?.map { it.id }?.let {
                    runningSet.addAll(it)
                }
            }
            if (pe.id !in runningSet) {
                result.add(pe)
                runningSet.add(pe.id)
            } else {
                Logger.d(TAG, "dropped ${pe.id}")
            }
        }
        return result
    }
}