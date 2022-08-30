package com.newshunt.appview.common.di

import com.newshunt.appview.common.ui.fragment.WebFragment
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.analytics.DefaultInvalidCardsLogger
import com.newshunt.news.model.apis.NewsApi
import com.newshunt.news.model.daos.RecentArticleTrackerDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BuildPayloadUsecase
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.FetchCardListFromUrlUsecase
import com.newshunt.news.model.usecase.NLResponseWrapper
import com.newshunt.news.model.utils.CardDeserializer
import com.newshunt.news.model.utils.TransformNewsList
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class WebModule(private val entityId:String,
                private val contentRequestMethod: String,
				private val section:String) {

	@Provides
	@Named("entityId")
	fun entityId() : String = entityId

	@Provides
	@Named("contentRequestMethod")
	fun contentRequestMethod() : String = contentRequestMethod

	@Provides
	@Named("fetchUsecase")
	fun fetchUc(fetchCardListFromUrlUsecase: FetchCardListFromUrlUsecase): BundleUsecase<NLResponseWrapper> =
		fetchCardListFromUrlUsecase

	@Provides
	fun api() = RestAdapterContainer.getInstance()
		.getDynamicRestAdapterRx(
			CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
			Priority.PRIORITY_HIGHEST,
			"",
				CardDeserializer.gson(null, DefaultInvalidCardsLogger),
			HTTP401Interceptor(),
			NewsListErrorResponseInterceptor()
		).create(NewsApi::class.java)

	@Provides
	fun transf(): TransformNewsList {
		return object : TransformNewsList {
			override fun transf(list: List<AnyCard>) = list
		}
	}

	@Provides
	fun followDao() = SocialDB.instance().followEntityDao()

	@Provides
	fun pullDao() = SocialDB.instance().pullDao()

	@Provides
	fun fetchDao() = SocialDB.instance().fetchDao()

	@Provides
	fun dislikeDao() = SocialDB.instance().dislikeDao()

	@Provides
	fun cookieDao() =  SocialDB.instance().cookieDao()

	@Provides
	fun pageEntityDao() = SocialDB.instance().pageEntityDao()

	@Provides
	fun provideRecentArticleTrackerDao(): RecentArticleTrackerDao = SocialDB.instance().recentArticleTrackerDao()

	@Provides
	@Named("buildPayloadUsecase")
	fun provideBuildPayloadUsecase(buildPayloadUsecase: BuildPayloadUsecase): BundleUsecase<Any> {
		return buildPayloadUsecase
	}

	@Provides
	@Named("section")
	fun section() = section

}

@Component(modules = [WebModule::class])
interface WebComponent {
	fun inject(webFragment: WebFragment)
}