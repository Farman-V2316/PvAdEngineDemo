/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.listener

/**
 * @author anshul.jain
 * Listener for updating counts.
 */
interface AssetCountListener {
    fun setAssetUpdateCountListener(listener: AssetCountsUpdateListener)
}