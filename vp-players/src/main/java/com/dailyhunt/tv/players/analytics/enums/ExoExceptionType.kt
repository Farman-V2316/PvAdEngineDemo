/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics.enums

/**
 * Enum defining exception types fot Exo Player
 */
enum class ExoExceptionType private constructor(val value: Int) {
    HTTP_DATA_SOURCE(1), PLAYLIST_RESET(2), PLAYLIST_STUCK(3),
    BEHIND_LIVE_WINDOW(4), GENERIC(5)
}
