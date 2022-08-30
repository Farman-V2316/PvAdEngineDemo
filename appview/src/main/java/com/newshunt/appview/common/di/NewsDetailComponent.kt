package com.newshunt.appview.common.di

import androidx.fragment.app.Fragment
import com.newshunt.appview.common.ui.fragment.NewsDetailFragment2
import com.newshunt.appview.common.ui.fragment.TransitionParent
import com.newshunt.appview.common.ui.fragment.TransitionParentDelegate
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.di.DetailFullPostModule
import com.newshunt.news.di.scopes.PerFragment
import com.newshunt.news.model.apis.NewsDetailAPI
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named

@PerFragment
@Component(modules = [CardsModule::class, DetailLandingItemModule::class, DetailFullPostModule::class])
interface DetailsListComponent2 {
	fun inject(component: NewsDetailFragment2)
}

@Module
class DetailLandingItemModule(private val postId: String,
							  private val entityId: String,
							  private val postEntityLevel: String,
							  private val socialDB: SocialDB,
							  private val referrerFlow: PageReferrer,
							  private val fragment: Fragment,
							  private val fragmentName: String) {

	@Provides
	@PerFragment
	@Named("postEntityLevel")
	fun postEntityLevel(): String = postEntityLevel

	@Provides
	@PerFragment
	fun detailAPI() = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(
			CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
			Priority.PRIORITY_HIGHEST,
			"",
			NewsListErrorResponseInterceptor())
			.create(NewsDetailAPI::class.java)

	@Provides
	@Named("referrerFlow")
	fun referrerFlow(): PageReferrer = referrerFlow

	@Provides
	@PerFragment
	fun historyDao() = socialDB.historyDao()

	@Provides
	fun transitionDelegate(): TransitionParent = TransitionParentDelegate(fragment, fragmentName)
}