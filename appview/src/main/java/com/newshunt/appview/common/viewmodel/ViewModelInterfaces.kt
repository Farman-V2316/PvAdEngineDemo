/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.viewmodel

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.appview.common.ui.viewholder.PerspectiveState
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.EntityItem
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.MenuOption
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.viewmodel.EmojiClickHandlingViewModel
import com.newshunt.news.viewmodel.SocialInteractionViewModel
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent

interface ClickHandlingViewModel : SocialInteractionViewModel {
    fun onViewClick(view: View) {}
    fun onViewClick(view: View, item: Any) {}
    fun onViewClick(view: View, item: Any, args: Bundle?) {}
    fun onViewClick(view: View, item: Any, args: Bundle?, contentAdDelegate: ContentAdDelegate?) {}

    fun onDialogDiscardLocal(arguments: Bundle?) {}
    fun onAutoPlayVideoClick(view: View, item: CommonAsset?, parent: CommonAsset?,
                             videoPlayerProvider: VideoPlayerProvider? = null,
                             contentAdDelegate: ContentAdDelegate? = null) {}

    fun onClickPerspective(view: View, item: Any, state: PerspectiveState) {}
    fun onOpenPerspective(view: View, item: Any, parentId: String, childId: String,
                          section: String, pageReferrer: PageReferrer?) {}

    fun onFollowEntityClick(view: View,
                            parent: CommonAsset?,
                            item: EntityItem?, position: Int) {
    }
    fun onFollowEntities(view: View, entityList: List<EntityItem>?, args: Bundle, asset: CommonAsset) {
    }

    fun onUnFollowLocations(view: View, actionableEntities: List<ActionableEntity>) {
    }

    fun canClickOnSource(item:Any?):Boolean{
        return false
    }

    fun onCollectionItemClick(view: View,
                              item: CommonAsset?, parent: CommonAsset?, parentCardPosition: Int = 0) {
    }

    fun onCollectionItemClick(view: View,
                              item: CommonAsset?, parent: CommonAsset?, parentCardPosition: Int = 0,collectionId:String?=null) {
    }

    fun onThreeDotMenuClick(view: View, item: Any?) {
        Logger.d(LOG_TAG, "Menu button clicked")
    }

    fun onThreeDotMenuClick(view: View, item: Any?,location: MenuLocation?) {
        Logger.d(LOG_TAG, "Menu button clicked")
    }

    fun onThreeDotMenuClick(view: View, item: Any?, adDelegate: ContentAdDelegate?,
                            adsMenuListener: ReportAdsMenuListener?) {
        if (adDelegate?.adEntity != null && adsMenuListener != null) {
            Logger.d(LOG_TAG, "Ads Menu clicked")
            adsMenuListener.onReportAdsMenuClick(adDelegate.adEntity!!)
            return
        }
        Logger.d(LOG_TAG, "Menu button clicked")
        onThreeDotMenuClick(view, item)
    }

    fun onInternalUrlClick(view: View, url: String) {

    }

    fun getConLiveData(): LiveData<ConnectionSpeedEvent>? {
        return AndroidUtils.connectionSpeedLiveData
    }

    fun getShareUsecase() : MediatorUsecase<Bundle, Boolean>? = null
}

/**
 * interface for all card clicks
 *
 * @author satosh.dhanyamraju
 */
interface ClickDelegate : ClickHandlingViewModel,
        EmojiClickHandlingViewModel,
        MultiMediaCollectionClickHandlingViewModel {
    fun dispose() {}
}

interface ClickDelegateProvider {
    fun getClickDelegate(): ClickDelegate
}

interface MultiMediaCollectionClickHandlingViewModel {
    fun onMultiMediaCollectionViewClick(view: View, parentItem: Any?, item: Any?) {
        Logger.e(LOG_TAG, "Multimedia card click not handled")
    }
}

interface MenuClickHandlingViewModel {
    fun onMenuL1OptionClick(
            view: View,
            menuOption: MenuOption,
            asset: CommonAsset?,
            pageEntity:
            PageEntity?,
            activity: Activity?
    ) {
        Logger.d(LOG_TAG, "Menu l1 option clicked")
    }

    fun onDialogConformDelete(arguments: Bundle?) {
        Logger.d(LOG_TAG, "Communication dialog conform delete")
    }
}

private const val LOG_TAG = "ClickInterface"