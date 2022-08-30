/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.entities

enum class PlayerState {
    IDLE,
    PREPARING,
    READY,
    BUFFERING,
    PLAYING,
    PAUSED,
    COMPLETE,
    RELEASED
}