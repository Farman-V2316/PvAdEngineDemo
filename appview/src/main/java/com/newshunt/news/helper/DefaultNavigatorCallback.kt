package com.newshunt.news.helper

import com.newshunt.dataentity.notification.FollowNavModel
import com.newshunt.dataentity.notification.NewsNavModel
import com.newshunt.dataentity.notification.TVNavModel
import com.newshunt.deeplink.navigator.NavigatorCallback
import com.newshunt.sso.SSO

class DefaultNavigatorCallback: NavigatorCallback {

	override fun onDeeplinkedToFollowingTab(followNavModel: FollowNavModel) {
		// do nothing
	}

	override fun startNewsHomeRouting(newsNavModel: NewsNavModel) {
		// do nothing
	}

	override fun startTvHomeRouting(tvNavModel: TVNavModel) {
		// do nothing
	}

	override fun getUserId(): String? {
		return SSO.getInstance().userDetails?.userID
	}

	override fun isLoggedIn(): Boolean {
		return SSO.getInstance().isLoggedIn(false)
	}
}