/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.recolocations

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.appview.common.di.FollowSnackbarModule
import com.newshunt.appview.common.ui.fragment.LocationExpansionFragment
import com.newshunt.appview.common.ui.fragment.PageableTopicFragment
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.LocationEntityLevel
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.ListTransformType
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.analytics.DefaultInvalidCardsLogger
import com.newshunt.news.di.scopes.PerFragment
import com.newshunt.news.model.apis.NewsApi
import com.newshunt.news.model.daos.PageEntityDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BuildPayloadUsecase
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.FetchCardListFromUrlUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.NLResponseWrapper
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.model.utils.CardDeserializer
import com.newshunt.news.model.utils.FilteroutUnknownCards
import com.newshunt.news.model.utils.TransformNewsList
import com.newshunt.news.view.fragment.LocationPresearchFragment
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * @author satosh.dhanyamraju
 */

@PerFragment
@Component(modules = [RecommendedLocationsModule::class, FollowSnackbarModule::class])
interface RecommendedLocationsComponent {
    fun inject(locationExpansionFragment: LocationExpansionFragment)
    fun inject(loc: LocationPresearchFragment)
}

@PerFragment
@Component(modules = [RecommendedLocationsModule::class])
interface TopicsRecommendedLocationsComponent {
    fun inject(pageableTopicFragment: PageableTopicFragment)
}

@Module
class RecommendedLocationsModule(private val section: String, val socialDB: SocialDB = SocialDB
        .instance()) {
    @Provides
    @PerFragment
    fun followEntityDao() = socialDB.followEntityDao()

    @Provides
    @PerFragment
    fun recentArticleDao() = socialDB.recentArticleTrackerDao()

    @Provides
    @PerFragment
    fun pullDao() = socialDB.pullDao()

    @Provides
    @PerFragment
    fun cookieDao() = socialDB.cookieDao()

    @Provides
    @PerFragment
    fun dislikeDao() = socialDB.dislikeDao()

    @Provides
    @PerFragment
    fun providePageEntityDao(): PageEntityDao = SocialDB.instance().pageEntityDao()

    @Provides
    @PerFragment
    @Named("buildPayloadUsecase")
    fun buildPayloadUC(uc: BuildPayloadUsecase): BundleUsecase<Any> = uc

    private val LIST_TYPE = Format.ENTITY.name

    @Provides
    @PerFragment
    fun tranformFunction(): TransformNewsList =
            FilteroutUnknownCards(LIST_TYPE, ListTransformType.DEFAULT, DefaultInvalidCardsLogger, socialDB.cardDao())

    @Provides
    @Named("section")
    fun section(): String = section

    @Provides
    @PerFragment
    fun api() = RestAdapterContainer.getInstance()
            .getDynamicRestAdapterRx(
                    CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
                    Priority.PRIORITY_HIGHEST,
                    "apiTag",
                    CardDeserializer.gson(LIST_TYPE, DefaultInvalidCardsLogger),
                    HTTP401Interceptor(),
                    NewsListErrorResponseInterceptor()
            ).create(NewsApi::class.java)

    @Provides
    @Named("fetchRecommendedLocationsUsecase")
    fun fetchRLU(fetchRecommendedLocationsUsecase: FetchRecommendedLocationsUsecase):
            MediatorUsecase<Bundle, NLResponseWrapper> = fetchRecommendedLocationsUsecase.toMediator2()


    @Named("toggleFollowMediatorUC")
    @PerFragment
    @Provides
    fun toggleFollowMediatorUC(toggleFollowUseCase: ToggleFollowUseCase) = toggleFollowUseCase.toMediator2()



    @Named("fetchCardListFromUrlUsecase")
    @PerFragment
    @Provides
    fun fetchUc(fetchCardListFromUrlUsecase: FetchCardListFromUrlUsecase):MediatorUsecase<Bundle, NLResponseWrapper> =
            fetchCardListFromUrlUsecase.toMediator2()
}


class FetchRecommendedLocationsUsecase @Inject constructor( private val fetchCardListFromUrlUsecase: FetchCardListFromUrlUsecase) : BundleUsecase<NLResponseWrapper> {
    private val URL = "api/v2/entity/recommendation/locations"
    override fun invoke(p1: Bundle): Observable<NLResponseWrapper> {
        return fetchCardListFromUrlUsecase.invoke(FetchCardListFromUrlUsecase.bundle(URL,
                Constants.HTTP_POST)).map {
            it?.let {
                val nlResp = it.nlResp
                val items = nlResp.rows.filterIsInstance<ActionableEntity>()
                val recommendations = items.map { entity -> entity.toLocationItem() }
                SocialDB.instance().locationsDao().replaceRecommendations(recommendations)
            }
            it
        }
    }
}


class MediatorRecommendedLocationsUsecase : MediatorUsecase<String, List<Location>> {

    private val _data = MediatorLiveData<Result0<List<Location>>>()

    override fun execute(t: String): Boolean {
        _data.addSource(SocialDB.instance().locationsDao().getLocationsRecommended(LocationEntityLevel
                .RECOMMENDATION.name)) {
            _data.value = Result0.success(it)
        }
        return true
    }

    override fun data(): LiveData<Result0<List<Location>>> {
        return _data
    }
}
