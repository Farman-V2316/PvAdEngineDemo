/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.helper

import android.net.Uri
import android.os.Bundle
import com.newshunt.appview.common.profile.model.internal.rest.BookmarksAPI
import com.newshunt.appview.common.profile.model.internal.rest.ProfileAPI
import com.newshunt.appview.common.profile.model.internal.rest.SyncBookmarksAPI
import com.newshunt.appview.common.profile.model.internal.service.BookmarkService
import com.newshunt.appview.common.profile.model.internal.service.BookmarkServiceImpl
import com.newshunt.appview.common.profile.model.internal.service.ProfileServiceImpl
import com.newshunt.appview.common.profile.model.internal.service.SyncBookmarksService
import com.newshunt.appview.common.profile.model.internal.service.SyncBookmarksServiceImpl
import com.newshunt.appview.common.profile.view.fragment.CurrentFilter
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.common.helper.cachedapi.WriteToCacheUsecaseController
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.model.interceptor.CachingInterceptor
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.dataentity.model.entity.BookmarkBody
import com.newshunt.dataentity.model.entity.BookmarkList
import com.newshunt.dataentity.model.entity.HISTORY_DATE_PATTERN
import com.newshunt.dataentity.model.entity.HISTORY_TIME_PATTERN
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.dataentity.model.entity.ProfileFilter
import com.newshunt.dataentity.model.entity.ProfileTabType
import com.newshunt.dataentity.model.entity.ProfileTabs
import com.newshunt.dataentity.model.entity.RunTimeProfileFilter
import com.newshunt.dataentity.model.entity.TimeFilter
import com.newshunt.dataentity.news.analytics.ProfileReferrer
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dhutil.CacheProvider
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.sqlite.SocialTypeConv
import com.newshunt.news.util.NewsConstants
import com.newshunt.profile.SimpleOptionItem
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.SignInUIModes
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import com.newshunt.sso.view.fragment.SignOnFragment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * @author santhosh.kc
 */

private const val TABTYPE_RESPONSES = "RESPONSES"
val timeFormat = SimpleDateFormat(HISTORY_TIME_PATTERN, Locale.ENGLISH)
val dateFormat = SimpleDateFormat(HISTORY_DATE_PATTERN, Locale.ENGLISH)

fun constructFilterInfoFrom(anchorViewId : Int, profileFilter: ProfileFilter) : CurrentFilter {
    return CurrentFilter(profileFilter = profileFilter, anchorViewId = anchorViewId,
            currentFilterOption = profileFilter.fromOptionName(profileFilter.name))
}

fun constructRunTimeFiltersFrom(profileFilter : ProfileFilter?, position : Int) :
        List<SimpleOptionItem>? {
    profileFilter ?: return null
    val simpleOptionItems = ArrayList<SimpleOptionItem>()
    profileFilter.options?.filter { it.isValid() }?.forEach {
        simpleOptionItems.add(SimpleOptionItem(displayText = it.displayName ?: Constants
                .EMPTY_STRING, anyEnumerationAsEnum = RunTimeProfileFilter(position, it),
                iconUrl = it.iconUrl))
    }
    return simpleOptionItems
}

private fun appendFilterParametersTo(contentUrl: String, currentFilters : List<CurrentFilter>?):
        String {
    if (CommonUtils.isEmpty(contentUrl) || CommonUtils.isEmpty(currentFilters)) {
        return contentUrl
    }

    val uriBuilder = try {
        Uri.parse(contentUrl).buildUpon()
    } catch (ex: Exception) {
        Logger.caughtException(ex)
        return contentUrl
    } ?: return contentUrl

    currentFilters?.forEach {
        uriBuilder.appendQueryParameter(it.profileFilter.type, it.currentFilterOption?.value)
        if (it.currentFilterOption?.allowLocalPost == true) { // this is for inserting local cards; not needed by BE
            uriBuilder.appendQueryParameter(Constants.URL_PARAM_ALLOW_LOCAL_CARD, Constants.YES)
        }
    }

    return uriBuilder.build().toString()
}

fun createSignOnFragment(uiMode: SignInUIModes,
                         isFPV: Boolean,
                         delayPageView: Boolean,
                         referrer: PageReferrer?): SignOnFragment {
    val fragment = SignOnFragment()
    val args = Bundle()
    args.putBoolean(Constants.BUNDLE_REFERRER_VIEW_IS_FVP, isFPV)
    args.putString(Constants.BUNDLE_SIGN_ON_UI_MODE, uiMode.name)
    args.putBoolean(Constants.BUNDLE_SIGN_IN_DELAY_PAGE_VIEW, delayPageView)
    args.putSerializable(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
    fragment.arguments = args
    return fragment
}

fun createProfileAPI(): ProfileAPI {
    val profileBaseUrl = NewsBaseUrlContainer.getUserServiceSecuredBaseUrl()
    return RestAdapterContainer.getInstance().getRestAdapter(profileBaseUrl,
            Priority.PRIORITY_HIGHEST,
            null,
            NewsListErrorResponseInterceptor(), HTTP401Interceptor())
            .create(ProfileAPI::class.java)
}

fun createGateWayAPIWithCaching(): ProfileAPI {
    val cacheApiCacheRx = CachedApiCacheRx(CacheProvider.getCachedApiCache(NewsConstants.HTTP_FEED_CACHE_DIR))
    val cachingInterceptor = CachingInterceptor(WriteToCacheUsecaseController<String>(cacheApiCacheRx))
    val newsBaseUrl = CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationSecureUrl())
    return RestAdapterContainer.getInstance().getRestAdapter(newsBaseUrl,
            Priority.PRIORITY_HIGHEST,
            null,
            cachingInterceptor, NewsListErrorResponseInterceptor(), HTTP401Interceptor())
            .create(ProfileAPI::class.java)
}

fun createProfileService() = ProfileServiceImpl(createProfileAPI(), createGateWayAPIWithCaching())

fun getPageReferrerForTab(profileTab: ProfileTabs?, id:String?): PageReferrer? {
    profileTab ?: return null
    id ?: return null

    return when (profileTab.tabType.pageType) {
        PageType.PROFILE_SAVED -> PageReferrer(ProfileReferrer.SAVED)
        PageType.PROFILE_SAVED_DETAIL -> PageReferrer(ProfileReferrer.SAVED)
        PageType.PROFILE_ACTIVITY -> PageReferrer(ProfileReferrer.ACTIVITY)
        PageType.PROFILE_MY_POSTS -> PageReferrer(ProfileReferrer.MY_POSTS)
        PageType.PROFILE_TPV_RESPONSES -> return PageReferrer(ProfileReferrer.RESPONSES)
        PageType.PROFILE_TPV_POSTS -> PageReferrer(ProfileReferrer.POSTS)
        else -> null
    }
}

fun appendFilterParameters(contentUrl: String, filterType: String, filterValue: String): String {
    if (CommonUtils.isEmpty(contentUrl)) {
        return contentUrl
    }

    val uriBuilder = try {
        Uri.parse(contentUrl).buildUpon()
    } catch (ex: Exception) {
        Logger.caughtException(ex)
        return contentUrl
    } ?: return contentUrl

    uriBuilder.appendQueryParameter(filterType, filterValue)

    return uriBuilder.build().toString()
}

fun buildLocationForTab(tabId: String, userId: String): String = tabId.plus(Constants.UNDERSCORE_CHARACTER).plus(userId)

fun buildDynamicFeedForTab(tab: ProfileTabs,
                           filters: List<CurrentFilter>,
                           userId: String): GeneralFeed? {
    tab.contentUrl?.let {
        return GeneralFeed((tab.id ?: tab.tabType.name).plus(Constants.UNDERSCORE_CHARACTER).plus(userId),
                appendFilterParametersTo(it, filters),
                Constants.HTTP_POST,
                PageSection.PROFILE.section)
    }
    return null
}

fun makeBookmarkApiPostBody(commonAsset: CommonAsset, action: BookMarkAction): BookmarkList {
    return BookmarkList(listOf(BookmarkBody(action = action,
            itemId = commonAsset.i_id(),
            format = commonAsset.i_format()?.name,
            subFormat = commonAsset.i_subFormat()?.name)))
}

fun buildHistoryEntity(id: String,
                       format: Format,
                       subFormat: SubFormat? = null,
                       uiType: UiType2? = null,
                       thumbnailUrl: ImageDetail? = null,
                       title: String? = null,
                       content: String? = null,
                       duration: String? = null,
                       timestamp: Long = System.currentTimeMillis(),
                       srcImgUrl: String? = null,
                       srcName: String? = null,
                       isNSFW: Boolean = false,
                       hideControl: Boolean? = false): HistoryEntity {
    return HistoryEntity(id = id,
            format = format,
            subFormat = subFormat,
            uiType = uiType,
            imgUrl = thumbnailUrl,
            title = title,
            content = content,
            duration = duration,
            timestamp = SocialTypeConv().fromTimestamp(timestamp),
            srcLogo = srcImgUrl,
            srcName = srcName,
            isDeleted = false,
            isMarkedDeleted = false,
            isNsfw = isNSFW,
            hideControl = hideControl)
}

fun mapFilterToTimeLimit(timeFilter: TimeFilter): Long {
    val currentTime = System.currentTimeMillis()
    return when (timeFilter) {
        TimeFilter.NINETY_DAYS -> {
            currentTime - TimeUnit.DAYS.toMillis(90)
        }
        TimeFilter.THIRTY_DAYS -> {
            currentTime - TimeUnit.DAYS.toMillis(30)
        }
        TimeFilter.SEVEN_DAYS -> {
            currentTime - TimeUnit.DAYS.toMillis(7)
        }
    }
}

fun createSyncBookmarkAPI(): SyncBookmarksAPI {
    val profileBaseUrl = NewsBaseUrlContainer.getUserServiceSecuredBaseUrl()
    return RestAdapterContainer.getInstance().getRestAdapter(profileBaseUrl,
            Priority.PRIORITY_LOW,
            null,
            NewsListErrorResponseInterceptor(), HTTP401Interceptor())
            .create(SyncBookmarksAPI::class.java)
}

fun createBookmarkAPI(): BookmarksAPI {
    val profileBaseUrl = NewsBaseUrlContainer.getUserServiceSecuredBaseUrl()
    return RestAdapterContainer.getInstance().getRestAdapter(profileBaseUrl,
            Priority.PRIORITY_NORMAL,
            null,
            NewsListErrorResponseInterceptor(), HTTP401Interceptor())
            .create(BookmarksAPI::class.java)
}

fun createSyncBookmarksService(): SyncBookmarksService {
    return SyncBookmarksServiceImpl(createSyncBookmarkAPI())
}

fun createBookmarkService(): BookmarkService {
    return BookmarkServiceImpl(createBookmarkAPI())
}