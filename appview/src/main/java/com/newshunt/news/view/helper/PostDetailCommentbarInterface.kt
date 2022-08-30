/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *  
 */

package com.newshunt.news.view.helper

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.model.entity.GroupInfo
/**
*
* @author satosh.dhanyamraju
*/
interface PostDetailCommentbarInterface{
    fun inflateCommentsBar(isInBottomSheet: Boolean, context: Activity?, card: CommonAsset?,
                           commentBarHolder: ViewGroup, layoutInflater: LayoutInflater,
                           cvm: CardsViewModel, parentStoryId: String?, postId: String?,
                           currentPageReferrer: PageReferrer?, groupInfo: GroupInfo?)
}