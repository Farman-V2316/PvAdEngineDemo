/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity

import com.newshunt.dataentity.common.asset.BaseDetailList
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntityLevel

/**
 * data class for pgi ads in news details page
 *
 * @author Mukesh Yadav
 */
data class NativePgiAdAsset(var id: String,
                            var video_assetId: String?,
                            var mm_includeCollectionInSwipe: Boolean?,
                            var format: Format?, var baseAdEntity: BaseAdEntity?) : BaseDetailList{
    override fun i_id(): String {
        return id
    }

    override fun i_video_assetId(): String? {
        return video_assetId
    }

    override fun i_mm_includeCollectionInSwipe(): Boolean? {
        return mm_includeCollectionInSwipe
    }

    override fun i_format(): Format? {
        return format
    }

    override fun i_level(): PostEntityLevel? {
        return PostEntityLevel.TOP_LEVEL
    }

    override fun i_adId(): String? {
        return baseAdEntity?.i_id()
    }

}
