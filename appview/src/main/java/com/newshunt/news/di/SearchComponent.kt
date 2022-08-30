/*
 * Created by Rahul Ravindran at 26/9/19 7:04 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.di

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.appview.common.di.FollowSnackbarModule
import com.newshunt.appview.common.ui.activity.LocationSelectionActivity
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.ListTransformType
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SuggestionResponse
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.analytics.DefaultInvalidCardsLogger
import com.newshunt.news.model.daos.SearchFeedDao
import com.newshunt.news.model.daos.SearchServiceDao
import com.newshunt.news.model.helper.TotalServedPageTracker
import com.newshunt.news.model.sqlite.SearchDatabase
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.model.utils.CardDeserializer
import com.newshunt.news.model.utils.FilteroutUnknownCards
import com.newshunt.news.view.activity.SearchActivity
import com.newshunt.sdk.network.Priority
import com.newshunt.search.model.rest.SearchApi
import com.newshunt.search.model.service.RecentsDelUseCase
import com.newshunt.search.model.service.RecentsInsertUseCase
import com.newshunt.search.model.service.RecentsReadUseCase
import com.newshunt.search.model.service.SearchUseCase
import com.newshunt.search.model.service.TrendingUnifiedUseCase
import com.newshunt.search.viewmodel.PreSearchUseCase
import com.newshunt.search.viewmodel.SearchViewModel
import dagger.Component
import dagger.Module
import dagger.Provides

/**
 * Contains dagger classes to build searchviewmodel
 *
 * @author satosh.dhanymaraju
 */

@Component(modules = arrayOf(SearchModule::class, FollowSnackbarModule::class))
interface SearchComponent {
    fun inject(a: SearchActivity)
    fun inject(a: LocationSelectionActivity)
}

const val DHTV_URL = "api/v2/dhtv/search/query"
const val NEWS_URL = "api/v2/search/query"

@Module
class SearchModule @JvmOverloads constructor(private val searchContext: String,
                   private val recentHeaderString: String,
                   private val trendingHeaderString: String,
                   private val searchDb: SearchDatabase,
                   private val requestType: SearchRequestType,
                   private val searchPayloadContext: SearchPayloadContext?,
                   private val listType: String? = null) {
    @Provides
    fun viewmodel(
            rsinsertusecase: RecentsInsertUseCase,
            rsdelusecase: RecentsDelUseCase,
            searchUseCase: SearchUseCase,
            PreSearchUseCase: PreSearchUseCase
    ): SearchViewModel {
        return SearchViewModel(
                rsinsertusecase.toMediator2(true),
                rsdelusecase.toMediator2(true),
                searchUseCase,
                PreSearchUseCase,
                requestType)
    }

    @Provides
    fun searchFeedDao() = SocialDB.instance().searchFeedDao()

    @Provides
    fun searchApi(): SearchApi {
        return RestAdapterContainer.getInstance().getDynamicRestAdapterRx(
                when(requestType){
                    SearchRequestType.GROUP -> CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getSearchBaseUrl())
                    SearchRequestType.NEWS ->  CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getSearchBaseUrl())
                    SearchRequestType.CREATE_POST -> CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getSearchBaseUrl())
                    SearchRequestType.LOCATION -> CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getSearchBaseUrl())
                },
                Priority.PRIORITY_HIGHEST, null,
                CardDeserializer.gson(listType, DefaultInvalidCardsLogger),
                NewsListErrorResponseInterceptor(),
                TotalServedPageTracker).create(SearchApi::class.java)
    }

    @Provides
    fun searchDao(): SearchServiceDao = searchDb.searchServiceDao()

    @Provides
    fun searchUsecase(
            api: SearchApi,
            searchFeedDao: SearchFeedDao,
            filteroutUnknownCards: FilteroutUnknownCards
    ): SearchUseCase =  SearchUseCase(api, requestType, searchFeedDao, SocialDB.instance().fetchDao(), SocialDB.instance().followEntityDao(), SocialDB.instance().locationsDao(), filteroutUnknownCards)

    @Provides
    fun versionedApiHelperTrending(): VersionedApiHelper<ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>> = VersionedApiHelper()

    @Provides
    fun rsinsertUseCase(ssdao: SearchServiceDao): RecentsInsertUseCase = RecentsInsertUseCase(ssdao)

    @Provides
    fun rsreadUseCase(ssdao: SearchServiceDao): RecentsReadUseCase = RecentsReadUseCase(ssdao,requestType)

    @Provides
    fun rsdelUseCase(ssdao: SearchServiceDao): RecentsDelUseCase = RecentsDelUseCase(ssdao)

    @Provides
    fun presearchUseCase(
            rsreadusecase: RecentsReadUseCase): PreSearchUseCase =
            PreSearchUseCase(
            rsreadusecase = rsreadusecase.toMediator2(false),
            isAutoCompleteDisabled = PreferenceManager.getPreference(GenericAppStatePreference.IS_AUTO_COMPLETE_DISABLE, false),
            minCharForAutoComplete = PreferenceManager.getPreference(GenericAppStatePreference.MIN_CHAR_FOR_AUTO_COMPLETE, 2),
            maxCharForAutoComplete = PreferenceManager.getPreference(GenericAppStatePreference.MAX_CHAR_FOR_AUTO_COMPLETE, Int.MAX_VALUE),
            searchContext = searchContext,
            recentHeaderString = recentHeaderString,
            trendingHeaderString = trendingHeaderString,
            requestType = requestType,
            searchPayloadContext = searchPayloadContext)


    private fun <T> validator(helper: VersionedApiHelper<ApiResponse<SuggestionResponse<T>>>, jsonResponse: String): String {
        if (CommonUtils.isEmpty(jsonResponse)) return Constants.EMPTY_STRING
        try {
            val type = object : TypeToken<ApiResponse<SuggestionResponse<T>>>() {}.type
            val apiResponse = Gson().fromJson<ApiResponse<SuggestionResponse<T>>>(jsonResponse, type)
            return if (apiResponse == null || apiResponse.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = TrendingUnifiedUseCase.entity.entityType,
                        langCode = UserPreferenceUtil.getUserLanguages(),
                        version = apiResponse.data.version,
                        data = jsonResponse.toByteArray())
                helper.insertVersionDbEntity(versionDbEntity)
                apiResponse.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    @Provides
    fun filterUnknown(): FilteroutUnknownCards {
        return FilteroutUnknownCards(null, ListTransformType.DEFAULT, DefaultInvalidCardsLogger, SocialDB.instance().cardDao())
    }

}