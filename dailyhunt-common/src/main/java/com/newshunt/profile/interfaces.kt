package com.newshunt.profile

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam

interface OptionItemClickListener {
    fun onOptionItemClicked(optionItem: SimpleOptionItem,pos:Int)
}

interface ExtraAnalyticsParameterProvider {
    fun getExtraAnalyticsParams(): Map<NhAnalyticsEventParam, Any>? {
        return null
    }
}