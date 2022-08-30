/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.interfaces

/**
 * @author shrikant.agrawal
 */
interface AutoplayPlayerCallbacks: PlayerCallbacks {

    fun showLoader()

    fun hideLoader()
}