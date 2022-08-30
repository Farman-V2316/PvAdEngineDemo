package com.newshunt.news.view.adapter

import android.view.View
import com.newshunt.dataentity.common.pages.CampaignMeta
import com.newshunt.dataentity.common.pages.PageEntity

/**
 * Created by karthik.r on 18/09/18.
 */
interface SlidingTabLayoutAdapter {

    fun getCampaignMetaItem(position: Int): CampaignMeta?

    fun getPageIconUrl(position: Int): String?

    fun applyCustomStyles(view: View, position: Int)
}