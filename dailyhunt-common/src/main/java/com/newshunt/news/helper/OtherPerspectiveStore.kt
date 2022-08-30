/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

/**
 * @author karthik.r
 */
object OtherPerspectiveStore {

    @JvmStatic
    private val expandableStoriesSet = mutableSetOf<String>()

    @JvmStatic
    fun insertExpandableStoryId(id: String) {
        OtherPerspectiveStore.expandableStoriesSet.add(id)
    }

    @JvmStatic
    fun getExpandableStoryId(): MutableSet<String> {
        return OtherPerspectiveStore.expandableStoriesSet
    }

    @JvmStatic
    fun removeExpandableStoryId(id: String) {
        OtherPerspectiveStore.expandableStoriesSet.remove(id)
    }
}