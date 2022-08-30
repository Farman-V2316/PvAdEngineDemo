/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper

import android.os.Bundle
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.share.ShareContent
import com.newshunt.common.model.usecase.ShareUsecase
import com.newshunt.common.model.usecase.SyncShareUsecase
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.FollowBlockUpdateUsecase
import com.newshunt.news.model.usecase.toMediator2

/**
 * When share-icon on a card in web-tab is clicked, this class used to make interactions-api call.
 * @author satosh.dhanyamraju
 */
class WebShareAPIHelper {
    private val LOG_TAG = "WebShareAPIHelper"
    private val usecase = ShareUsecase(SocialDB.instance().interactionsDao(), SyncShareUsecase(),
        FollowBlockUpdateUsecase(SocialDB.instance().followBlockRecoDao())).toMediator2()
    fun onShared(shareContent: ShareContent) {
        Logger.d(LOG_TAG, "onShared: ${shareContent.shareAPIParams}")
        toUsecaseBundle(shareContent)?.let {
            usecase.execute(it)
        }
    }
    private fun toUsecaseBundle(shareContent: ShareContent): Bundle? {
        val itemId = shareContent.shareAPIParams?.itemId
        val entityType = shareContent.shareAPIParams?.entityType?:"POST"
        return if (itemId != null) {
            ShareUsecase.args(itemId, entityType, postSourceAsset = PostSourceAsset(id = shareContent.sourceId,
            displayName = shareContent.sourceName, imageUrl = shareContent.sourceImageUrl), sourceLang = shareContent.sourceLang )
        } else null
    }
}
