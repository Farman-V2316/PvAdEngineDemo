/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * @author shrikant.agrawal
 */
object NonLinearStore {

    /**
     * Map for storing non linear post for a parent post
     */
    @JvmStatic
    private val nonLinearMap = mutableMapOf<String,NonLinearParent>()


    @JvmStatic fun insertStories(parentId:String, stories: List<PostEntity>) {
        var nonLinearParent = nonLinearMap[parentId]

        if (nonLinearParent == null) {
            nonLinearParent = NonLinearParent(parentId)
        }

        if (!CommonUtils.isEmpty(stories)) {
            stories.forEach { t ->  nonLinearParent.cardsList.add(t) }
        }
    }

    @JvmStatic fun deleteStories() {
        nonLinearMap.clear()
    }

    @JvmStatic fun updateTimespent(parentId:String, timespent:Long) {
        val collection = nonLinearMap.values
        collection.forEach {
            t: NonLinearParent? -> if (t?.parentId == parentId) {
                t.timeSpent = timespent
            }
        }
    }

    @JvmStatic fun getParentId(postId:String) : NonLinearParent? {
        val collection = nonLinearMap.values
        collection.forEach {
                t: NonLinearParent? -> t?.cardsList?.forEach {
                if (it.id == postId) {
                    return t
                }
            }
        }
        return null
    }

}

class NonLinearParent(val parentId: String) {
    var timeSpent: Long = 0L
    val cardsList: MutableList<PostEntity> = mutableListOf()
}