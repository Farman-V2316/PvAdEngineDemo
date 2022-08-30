/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.newshunt.appview.common.model.usecase.InsertFollowFeedPageUsecase
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants

/**
 * @author shrikant.agrawal
 */
class ImportFollowViewModel : ViewModel() {

	private fun getUrl(referrer: String): String {
		return Uri.Builder().encodedPath(NewsBaseUrlContainer.getContactSyncBaseUrl())
				.appendEncodedPath("cs/lite")
				.appendQueryParameter(Constants.REFERRER, referrer)
				.toString()
	}


	fun insertPage(referrer: String) {
		InsertFollowFeedPageUsecase().toMediator2().execute(bundleOf(InsertFollowFeedPageUsecase
				.BUNDLE_ID to Constants.IMPORT_FOLLOW_PAGE_ID,
				InsertFollowFeedPageUsecase.BUNDLE_CONTENT_URL to getUrl(referrer),
				InsertFollowFeedPageUsecase.BUNDLE_CONTENT_POST to "POST",
				NewsConstants.DH_SECTION to NhAnalyticsEventSection.APP.eventSection))
	}
}