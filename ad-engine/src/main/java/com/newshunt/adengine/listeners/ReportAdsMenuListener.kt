package com.newshunt.adengine.listeners

import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.common.helper.common.Constants
import org.jetbrains.annotations.NotNull

interface ReportAdsMenuListener {

    fun onReportAdsMenuClick(@NotNull reportedAdEntity: BaseAdEntity,
                             @NotNull reportedAdParentUniqueAdIdIfCarousal: String = Constants.EMPTY_STRING)

}

interface OnAdReportedListener {

    fun onAdReported(reportedAdEntity: BaseAdEntity?,
                     reportedParentAdIdIfCarousel: String? = null)

    fun onAdReportDialogDismissed(reportedAdEntity: BaseAdEntity?,
                                  reportedParentAdIdIfCarousel: String? = null) {}
}