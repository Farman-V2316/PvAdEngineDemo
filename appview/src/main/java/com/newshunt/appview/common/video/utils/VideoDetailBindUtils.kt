/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.video.utils

import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.HastTagAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils

class VideoDetailBindUtils {
    companion object {
        fun getHashTags(asset: CommonAsset?): List<HastTagAsset>? {
            var tagList = arrayListOf<HastTagAsset>()
            if(!CommonUtils.isEmpty(asset?.i_cardLocation())) {
                val locationHashtag = HastTagAsset(asset?.i_cardLocation()!!, asset?.i_cardLocation()!!, Constants.LOCATION)
                tagList.add(locationHashtag)
            }

            if(!CommonUtils.isEmpty(asset?.i_hashtags())) {
                tagList.addAll(asset?.i_hashtags()!!)
            }
            return  tagList
        }
    }
}