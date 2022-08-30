/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator

import android.content.Intent
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.notification.TVNavModel
import com.newshunt.news.model.usecase.GetHomePageUsecase
import io.reactivex.schedulers.Schedulers

/**
 * @author shrikant.agrawal
 */

class TvHomeRouter(private val callback: NewsHomeRouter.Callback, private val tvNavModel: TVNavModel, private val pageReferrer: PageReferrer) {

	fun startTvHomeRouting() {
		val usecase = GetHomePageUsecase()
		usecase.invoke(PageSection.TV.section)
			.subscribeOn(Schedulers.io())
			.doOnNext { this.handlePageResponse(it) }
			.subscribe()
	}

	private fun handlePageResponse(pageList: List<PageEntity>) {
		var intent: Intent? = null
		pageList.forEach {
			if (it.id == tvNavModel.groupId) {
				intent = TvNavigationHelper.getIntentForTVHome(tvNavModel, it)
			}
		}

		if (intent == null) {
			intent = TvNavigationHelper.getTvChannelOrGroupIntent(tvNavModel, pageReferrer)
		}
		callback.onRoutingSuccess(intent, tvNavModel)
	}

}