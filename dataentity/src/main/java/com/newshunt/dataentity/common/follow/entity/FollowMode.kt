/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.follow.entity

/**
 * @author anshul.jain
 */
enum class FollowMode(val mode: String) {
    FOLLOWED("followed"),
    UNFOLLOWED("unfollowed"),
    UNBLOCKED("unblocked");

    companion object {
        @JvmStatic
        fun getValue(mode: String?): FollowMode {
            mode ?: return FOLLOWED
            return (values().find { it.mode == mode }).orDefault()

        }
    }

}

enum class FollowUnFollowReason(val reason: String) {
    USER("USER"),
    BLOCK("BLOCK"),
    UNBLOCK("UNBLOCK"),
    SERVER("SERVER");


    companion object {
        @JvmStatic
        fun getValue(mode: String?): FollowUnFollowReason {
            mode ?: return USER
            return (values().find { it.reason == mode }).orDefault()

        }
    }
}

fun FollowUnFollowReason?.orDefault() = (this ?: FollowUnFollowReason.USER)

fun FollowMode?.orDefault() = (this ?: FollowMode.FOLLOWED)