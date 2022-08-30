/*
 * Created by Rahul Ravindran at 26/9/19 7:12 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.search.viewmodel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.search.RecentSearchEntity
import com.newshunt.dataentity.search.SearchActionType
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SearchSuggestionType
import com.newshunt.dataentity.search.SuggestionPayload
import com.newshunt.dataentity.search.SuggestionResponse
import com.newshunt.dataentity.search.SuggestionUiResponse
import com.newshunt.dataentity.search.UserData
import com.newshunt.dhutil.LiveEvent
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.makeFunctionCache
import com.newshunt.dhutil.mapfromJson
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.dhutil.scan
import com.newshunt.dhutil.zipWith
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.sdk.network.Priority
import com.newshunt.search.model.rest.SuggestionApi
import com.newshunt.search.model.rest.TrendingApi
import com.newshunt.search.model.service.RecentsDelUseCase
import com.newshunt.search.model.service.RecentsInsertUseCase
import com.newshunt.search.model.service.RecentsReadUseCase
import com.newshunt.search.model.service.SearchUseCase
import com.newshunt.search.model.service.SuggestionUseCase
import com.newshunt.search.model.service.TRENDING_QUERY
import com.newshunt.search.model.service.TrendingHandleUseCase
import com.newshunt.search.model.service.TrendingHashtagUseCase
import com.newshunt.search.model.service.TrendingUnifiedUseCase
import java.util.*
import kotlin.properties.Delegates

/**
 * Scope will be per-activity
 *
 * @author satosh.dhanymaraju
 */
private const val MSG_DEBOUNCE = 2609
private const val DEBOUNCE_DELAY = 100L //Need to profile and fine tune this value
private const val LOG_TAG = "SearchViewModel"

private val GSONExposed = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

class SearchViewModel(
        private val rsinsertusecase: MediatorUsecase<Bundle, Boolean>,
        private val rsdelUseCase: MediatorUsecase<Bundle, Boolean>,
        private val searchusecase: SearchUseCase,
        private val presearchUsecase: PreSearchUseCase,
        private val requestType: SearchRequestType) {

    // will be observed by pre-search screens
    val presearch: LiveData<SuggestionUiResponse?> = Transformations.map(presearchUsecase.preSearchMediator) {
        it.getOrNull()?.getContentIfNotHandled()
    }

    // will be observed by searchfragment
    val searchResults = searchusecase.toMediator2(true)


    /**
     * We send all typed text through the handler, which, within Xmsec -> replaces the message, and
     * after Xmsec processes the message.
     */
    private val debounceHandler = Handler(Looper.getMainLooper(), Handler.Callback {
        if (it.what == MSG_DEBOUNCE) {
            val queryStr = it.obj as? QueryWithId
            if (queryStr != null) _typing(queryStr)
            true
        } else false
    })

    /**
     * to be called on textchanges in the searchbar
     * @param postText only used for create post
     */
    fun typing(query: QueryWithId) {
        debounceHandler.removeMessages(MSG_DEBOUNCE)
        debounceHandler.sendMessageDelayed(Message.obtain(debounceHandler, MSG_DEBOUNCE, query), DEBOUNCE_DELAY)
        return
    }

    /**
     * The query text, after debouncing will be processed here.
     */
    private fun _typing(query: QueryWithId) {
        Logger.d(LOG_TAG, "Accepted input $query")
        presearchUsecase.execute(query)
    }

    fun insertQueryToRecent(query: String, suggestion: SearchSuggestionItem) {
        Logger.d(LOG_TAG, "insertQueryToRecent: $query")

        if (requestType != SearchRequestType.NEWS) return

        //update typeName of hashtag and handle while storing as recents
        val newSuggestion = suggestion.copy(suggestionType = SearchSuggestionType.RECENT,
                typeName = "")

        Logger.d(LOG_TAG, "insertQueryToRecent: $query inserting")
        val json = GSONExposed.toJson(newSuggestion)
        rsinsertusecase.execute(bundleOf(
                RecentsInsertUseCase.RS_ENTITY to RecentSearchEntity(
                        query = query,
                        json = json)
        ))

    }

    fun deleteQueryFromRecent(query: String) {
        rsdelUseCase.execute(bundleOf(
                RecentsInsertUseCase.RS_ENTITY to RecentSearchEntity(query)))
    }

    fun deleteAllFromRecent() {
        rsdelUseCase.execute(bundleOf(RecentsDelUseCase.RS_DEL_ALL to true))
    }

    /**
     * - submit from keyboard
     * - click on recent/trending/suggestion item
     */
    fun submit(query: SearchSuggestionItem) {
        searchResults.execute(bundleOf(SearchUseCase.SEARCH_REQUEST to query))
    }


}



typealias SugItemList = List<RecentSearchEntity>


/**
 * For query(q), Create a livedata by recents + suggestions.
 * If empty query and corresponding section is present, add headers as well.
 * Limit the size, excluding headers.
 */
fun combineSources(
        rec: LiveData<Result0<SugItemList>>,
        sug: LiveData<Result0<SuggestionResponse<List<SearchSuggestionItem>>>>,
        query: String,
        recentToSuggItem: (RecentSearchEntity) -> SearchSuggestionItem,
        recentHeaderString: String,
        trendingHeaderString: String,
        requestType: SearchRequestType): LiveData<SuggestionUiResponse?> {
    return rec.scan(emptyList<RecentSearchEntity>(), { _, result -> result.getOrNull()!!})
            .zipWith(sug.scan(SuggestionResponse<List<SearchSuggestionItem>>(), { _, response -> response.getOrNull()!!})) { r, s ->
                val recentHeader = listOf(SearchSuggestionItem(query, recentHeaderString, suggestionType = SearchSuggestionType.RECENT_HEADER))
                val trendingHeader = listOf(SearchSuggestionItem(query, trendingHeaderString, suggestionType = SearchSuggestionType.TRENDING_HEADER))

                // recents
                val l1 = r.take(s.maxRecentSize).map(recentToSuggItem)
                val h1 = if (query == "" && l1.isNotEmpty()) recentHeader else emptyList()

                // suggestions
                val l2 = if (l1.isEmpty()) {
                    if(requestType != SearchRequestType.CREATE_POST) s.rows
                    //particular to create post
                    else {
                        //for hashtag we need to allow creation of hashtag. Below logic checks for every result
                        // returned if query is same and choose to show create hashtag option
                        if(query.startsWith("#") && query.length > 1) {
                            s.rows = s.rows ?: listOf(SearchSuggestionItem("", query.substring(1),
                                    typeName = SearchSuggestionType.HASHTAG.name))
                            s.rows?.find { it.suggestion.toLowerCase() == query.substring(1).toLowerCase() }?.let {
                                s.rows
                            } ?: s.rows?.toMutableList()?.apply {
                                add(0,SearchSuggestionItem("", query.substring(1),
                                        typeName = SearchSuggestionType.HASHTAG.name))
                            }
                        }else s.rows
                    }
                } else s.rows?.filterNot { aSuggestion ->
                    l1.any { it.suggestion == aSuggestion.suggestion }
                }?.take(if (s.maxListSize == 0) s.maxListSize else s.maxListSize - l1.size)
                val h2 = if (query == "" && query != "@"
                        && query != "#" && l2?.isNotEmpty() == true) trendingHeader else emptyList()

                // for dividers
                if ((query == "" || l2?.isEmpty() == true) && l1.isNotEmpty()) l1.last().isEndItem = true
                if (l2?.isNotEmpty() == true) l2.last().isEndItem = true


                // combine them
                SuggestionUiResponse(query, s.copy(rows = (h1 + l1 + h2 + (l2 ?: emptyList()))))
            }
}


/*
*  alias for search input
* @Type 1 : input query
* @type 2: id for the search item
* @type 3: editable context. NOTE: used only for the case of create post flow
* */
typealias QueryWithId = Triple<String, String, String>


class PreSearchUseCase(
        private val rsreadusecase: MediatorUsecase<Bundle, SugItemList>,
        private val searchContext: String,
        private val recentHeaderString: String,
        private val trendingHeaderString: String,
        private val minCharForAutoComplete: Int = 2,
        private val maxCharForAutoComplete: Int = Int.MAX_VALUE,
        private val isAutoCompleteDisabled: Boolean = false,
        private val requestType: SearchRequestType,
        private val searchPayloadContext: SearchPayloadContext? = null
) : MediatorUsecase<QueryWithId, SuggestionUiResponse> {

    private val TAG = PreSearchUseCase::class.java.simpleName
    val preSearchMediator = MediatorLiveData<Result<LiveEvent<SuggestionUiResponse>>>()
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>>()

    /**
     * Whenever query changes, presearch's sources will change (to reflect the query.)
     * This source is filtered_recents + suggestions, will a size limit.
     * #
     */
    private var prevQuery by Delegates.observable(Triple("", "", "")) { property, oldValue, newValue ->
        Logger.d(TAG, "lookup old value: ${oldValue}")
        if(oldValue.first.isNotEmpty() || requestType != SearchRequestType.CREATE_POST) preSearchMediator.removeSource(lookup(oldValue, false))
        Logger.d(TAG, "lookup new value: ${newValue}")
        preSearchMediator.addSource(if (newValue.first.isEmpty() || ((newValue.first == "#" || newValue.first == "@") &&
                requestType == SearchRequestType.CREATE_POST)) lookup(newValue, true) else lookup(newValue, false)) {
            preSearchMediator.value = Result.success(LiveEvent(it!!))
        }
    }

    private val payload = SuggestionPayload(query = "", appUserData = UserData(
            campaign = "",
            clientTS = System.currentTimeMillis(),
            clientTZ = TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT),
            cid = UserPreferenceUtil.getClientId(),
            context = searchContext,
            cookieInfo = mapfromJson(PreferenceManager.getPreference(GenericAppStatePreference.COOKIE_INFO, "")),
            deviceHeight = CommonUtils.getDeviceScreenHeight(),
            deviceWidth = CommonUtils.getDeviceScreenWidth(),
            contextMap = searchPayloadContext))

    private val recentToSuggItem = { it: RecentSearchEntity ->
        (if (it.json.isNotBlank())
            JsonUtils.fromJson(it.json, SearchSuggestionItem::class.java).copy(

            )
        else SearchSuggestionItem(it.query, it.query))
                .copy(suggestionType = SearchSuggestionType.RECENT, ts = it.timeStamp)
    }


    /**
     * To prevent requesting network again for the same query(a -> ab -> a), we save the query's
     * resulting livedata in a map. Will be kept per fragment instance.
     *
     */

    private val lookup = makeFunctionCache(
                keySelector = { it.first },
                fn = { query: QueryWithId ->
                    val q = query.first
                    // when we pass empty payload, we get empty suggestions
                    val p = when {
                        // Always pass empty query: it will fetch trending not suggestions.
                        q.isNotEmpty() && isAutoCompleteDisabled -> {
                            Logger.d(LOG_TAG, "Autocomplete disable in handshake")
                            null
                        }

                        // Always pass empty query: it will fetch trending not suggestions.
                        // Both values in range are inclusive
                        q.isNotEmpty() && q != "#" && q != "@"
                                && q.length !in minCharForAutoComplete..maxCharForAutoComplete -> {
                            Logger.d(LOG_TAG, "Not auto-completing $query. len=${q.length}, " +
                                    "min=$minCharForAutoComplete, max=$maxCharForAutoComplete")
                            null
                        }
                        requestType == SearchRequestType.LOCATION -> payload.copy(query = query.first,
                                appUserData = payload.appUserData?.copy(
                                        searchRequestId = query.second,
                                        postText = query.third), type = "LOCATION")
                        else -> payload.copy(query = query.first,
                                appUserData = payload.appUserData?.copy(
                                        searchRequestId = query.second,
                                        postText = query.third))
                    }

                combineSources(
                        rsreadusecase.apply {
                            execute(bundleOf(
                                    RecentsReadUseCase.RS_QUERY to query.first
                            ))
                        }.data(),
                        //very hacky way of trigger trending api for hashtag and handle
                        getUsecaseOnQuery(query, p).data(),
                        query.first,
                        recentToSuggItem,
                        recentHeaderString,
                        trendingHeaderString, requestType)
            })

    override fun execute(t: QueryWithId): Boolean {
        prevQuery = t
        Logger.d(TAG, "search term : ${t.first}")
        return true
    }

    override fun data(): LiveData<Result0<SuggestionUiResponse>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun status(): LiveData<Boolean> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dispose() {

    }

    private fun getSuggestionUsecase(): MediatorUsecase<Bundle, SuggestionResponse<List<SearchSuggestionItem>>> {
        val api = RestAdapterContainer.getInstance()
                .getDynamicRestAdapterRx(CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getAutoCompleteBaseUrl()),
                        Priority.PRIORITY_HIGHEST, null).create(SuggestionApi::class.java)
        return SuggestionUseCase(requestType, api).toMediator2(true)
    }

    private fun getUsecaseOnQuery(query: QueryWithId, payload: SuggestionPayload?): MediatorUsecase<Bundle, SuggestionResponse<List<SearchSuggestionItem>>> {
        val api = RestAdapterContainer.getInstance()
                .getDynamicRestAdapterRx(NewsBaseUrlContainer.getSearchBaseUrl(),
                        Priority.PRIORITY_HIGHEST, null)
                .create(TrendingApi::class.java)

        //deeplink case action is null hence call autocomplete api
        payload?.appUserData?.contextMap?.action?.let { action ->
            SearchActionType.values().find { v -> action.equals(v.typeName, true) }
        } ?: return getSuggestionUsecase().apply {
            execute(bundleOf(
                    TRENDING_QUERY to query.first,
                    SuggestionUseCase.SS_PAYLOAD to payload
            ))
        }

        return when {
            // location search flow
            requestType == SearchRequestType.LOCATION -> getSuggestionUsecase()
            //create post flow
            query.first == "#" && requestType == SearchRequestType.CREATE_POST -> TrendingHashtagUseCase(api, payload).toMediator2(true)
            //create post flow
            query.first == "@" && requestType == SearchRequestType.CREATE_POST -> TrendingHandleUseCase(api, payload).toMediator2(true)
            //unified search flow. Eg. # or @ -> hit autocomplete api with q=# or @
            (query.first == "#" && requestType != SearchRequestType.CREATE_POST) ||
                    (query.first == "@" && requestType != SearchRequestType.CREATE_POST) -> getSuggestionUsecase()
            //unified search flow. Trending unified with versioned api interceptor added now
            query.first == "" -> TrendingUnifiedUseCase(versionedApiHelper, RestAdapterContainer.getInstance()
                    .getDynamicRestAdapterRx(NewsBaseUrlContainer.getSearchBaseUrl(),
                            Priority.PRIORITY_HIGHEST, null, VersionedApiInterceptor({ json: String -> validator(json) }))
                    .create(TrendingApi::class.java)
                    , requestType, payload).toMediator2(true)
            // autosuggestion api. query.first length > 1. Eg. #a,@a, ab etc
            else -> getSuggestionUsecase()
        }.apply {
            execute(bundleOf(
                    TRENDING_QUERY to query.first,
                    SuggestionUseCase.SS_PAYLOAD to payload
            ))
        }

    }

    private fun validator(jsonResponse: String): String {
        if (CommonUtils.isEmpty(jsonResponse)) return Constants.EMPTY_STRING
        try {
            val type = object : TypeToken<ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>>() {}.type
            val apiResponse = CommonUtils.GSON.fromJson<ApiResponse<SuggestionResponse<List<SearchSuggestionItem>>>>(jsonResponse, type)
            return if (apiResponse == null || apiResponse.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = TrendingUnifiedUseCase.entity.entityType,
                        langCode = UserPreferenceUtil.getUserLanguages(),
                        version = apiResponse.data.version, data = jsonResponse.toByteArray())
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                apiResponse.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }
}