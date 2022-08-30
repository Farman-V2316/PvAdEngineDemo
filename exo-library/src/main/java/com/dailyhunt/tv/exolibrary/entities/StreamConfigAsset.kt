/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.entities

import com.google.android.exoplayer2.DefaultLoadControl


//TODO: more config var to be added
data class StreamConfigAsset(val MAX_BUFFER_SIZE: Int,
                             val MIN_BUFFER_SIZE: Int,
                             val islive: Boolean = false,
                             val mute: Boolean = false) {

    companion object {
        @JvmStatic
        val DEFAULT = StreamConfigAsset(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS)
    }
}