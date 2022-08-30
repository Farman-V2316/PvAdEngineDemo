package com.newshunt.dataentity.news.analytics

/**
 * Enum to mark various locations for supplementary section
 *
 * Created by karthik.r on 2020-01-22.
 */
enum class StorySupplementSectionPosition constructor(val value: String) {

    ENDOFSTORY("endofstory");

    companion object {
        @JvmStatic
        fun fromValue(value: String?): StorySupplementSectionPosition {
            for (type in values()) {
                if (type.value.equals(value, ignoreCase = true)) {
                    return type
                }
            }

            return ENDOFSTORY
        }
    }

    fun getName() : String {
        return value
    }
}