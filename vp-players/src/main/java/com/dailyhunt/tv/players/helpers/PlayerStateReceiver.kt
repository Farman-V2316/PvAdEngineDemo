package com.dailyhunt.tv.players.helpers

import com.dailyhunt.tv.players.entity.PLAYER_STATE

data class PlayerEvent(val playerState: PLAYER_STATE, val id: String?, val msg: String? = null)
