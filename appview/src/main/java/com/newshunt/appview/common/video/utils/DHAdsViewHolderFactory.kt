/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dhtvapp.common.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.adengine.BR
import com.newshunt.adengine.databinding.EmptyAdLayoutBinding
import com.newshunt.adengine.databinding.NewsItemTypeHtmlAdBinding
import com.newshunt.adengine.databinding.NewsItemTypeImageLinkAdBinding
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.view.viewholder.EmptyAdsViewHolder
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.adengine.view.viewholder.NativeAdImageLinkViewHolder
import com.newshunt.appview.R
import com.newshunt.dataentity.common.asset.AdDisplayType
import com.newshunt.dhutil.helper.AppSettingsProvider

/**
 * Created on 11/27/2019.
 *
 * @author umesh.isran
 */
object DHAdsViewHolderFactory {

    fun getViewBinding(cardType: Int, layoutInflater: LayoutInflater, parent: ViewGroup):
            ViewDataBinding? {
        return when (cardType) {
            AdDisplayType.HTML_AD.index -> {
                DataBindingUtil.inflate<NewsItemTypeHtmlAdBinding>(layoutInflater,
                        R.layout.news_item_type_html_ad, parent, false)
            }
            AdDisplayType.IMAGE_LINK.index -> {
                DataBindingUtil.inflate<NewsItemTypeImageLinkAdBinding>(layoutInflater,
                        R.layout.news_item_type_image_link_ad, parent, false)
            }
            AdDisplayType.EMPTY_AD.index -> {
                DataBindingUtil.inflate<EmptyAdLayoutBinding>(layoutInflater, com.newshunt.adengine.R.layout
                        .empty_ad_layout, parent, false)
            }
            else -> null
        }
    }

    fun getViewHolder(cardType: Int, viewDataBinding: ViewDataBinding, uniqueRequestId: Int,
                      parentLifecycleOwner: LifecycleOwner? = null,
                      reportAdsMenuListener: ReportAdsMenuListener? = null):
            RecyclerView.ViewHolder? {
        viewDataBinding.setVariable(BR.adReportListener, reportAdsMenuListener)
        viewDataBinding.setVariable(BR.appSettingsProvider,AppSettingsProvider)
        return when (cardType) {
            AdDisplayType.HTML_AD.index -> NativeAdHtmlViewHolder(viewDataBinding as
                    NewsItemTypeHtmlAdBinding, uniqueRequestId, parentLifecycleOwner)
            AdDisplayType.IMAGE_LINK.index -> NativeAdImageLinkViewHolder(viewDataBinding as
                    NewsItemTypeImageLinkAdBinding, uniqueRequestId, parentLifecycleOwner)
            AdDisplayType.EMPTY_AD.index ->
                EmptyAdsViewHolder(viewDataBinding as EmptyAdLayoutBinding, uniqueRequestId)
            else -> null
        }
    }
}
