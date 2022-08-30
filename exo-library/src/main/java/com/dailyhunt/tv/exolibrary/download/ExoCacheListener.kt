/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.download

import com.dailyhunt.tv.exolibrary.entities.MediaItem
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset
import java.util.concurrent.ConcurrentHashMap

/**
 * @author vinod.bc
 */
interface ExoCacheListener {
    fun deleteFromCache(assetList: MediaItem?)
    fun deleteFromCache(assetList: ConcurrentHashMap<String, MediaItem>)
    fun isMediaPresentInCacheDirectory(asset: MediaItem?): Boolean
}