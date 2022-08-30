package com.newshunt.appview.common.helper

import androidx.fragment.app.Fragment
import com.newshunt.adengine.listeners.OnAdReportedListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.appview.common.ui.fragment.ReportAdsMenuFragment
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider

/**
 *
 *
 * @author shashikiran.nr
 */
class ReportAdsMenuFeedbackHelper(private val fragment: Fragment,
                                  private val onAdReportedListener: OnAdReportedListener?) : ReportAdsMenuListener {

    override fun onReportAdsMenuClick(reportedAdEntity: BaseAdEntity,
                                      reportedAdParentUniqueAdIdIfCarousal: String) {
        AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.reportAdsMenuEntity?.let {
            ReportAdsMenuFragment(fragment, it, reportedAdEntity,
                reportedAdParentUniqueAdIdIfCarousal, onAdReportedListener).show()
        }
    }
}