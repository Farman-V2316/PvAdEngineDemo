/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.ServedButNotPlayedHelper
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.interceptor.HeaderInterceptor
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.social.entity.CardsPayload
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.dataentity.social.entity.RecentTabEntity
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.daos.CookieDao
import com.newshunt.news.model.daos.DislikeDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.PageEntityDao
import com.newshunt.news.model.daos.PullDao
import com.newshunt.news.model.daos.RecentArticleTrackerDao
import com.newshunt.news.model.helper.AutoplayHelper
import com.newshunt.news.model.helper.VideoPlayedCache
import com.newshunt.news.model.repo.CardSeenStatusRepo
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

/**
 * reads db and builds payload pojo for feed API
 * @author satosh.dhanyamraju
 */
class BuildPayloadUsecase @Inject constructor(private val followEntityDao: FollowEntityDao,
                                              private val pullDao: PullDao,
                                              private val cookieDao: CookieDao,
                                              private val recentArticleTrackerDao: RecentArticleTrackerDao,
                                              val dislikeDao: DislikeDao,
                                              val pageEntityDao:PageEntityDao) : BundleUsecase<Any> {

    override fun invoke(p1: Bundle): Observable<Any> {
        val feedPage: FeedPage? = (p1.getSerializable(B_FEEDPAGE_ENTITY) as? FeedPage)
        val recommendFollowBlockRequestPayload: CardsPayload.FollowBlockRequest? = (p1.getSerializable(RECOMMENDED_FOLLOW_REQUEST) as? CardsPayload.FollowBlockRequest)
        val impressionsData = (p1.get(BuildPayloadUsecase.B_IMPRESSIONS_DATA) as? List<String>)

        return Observable.fromCallable {
            val curTime = System.currentTimeMillis()
            val cssId = "${UniqueIdHelper.getInstance().generateUniqueId()}"
            CardsPayload(
                    edition = UserPreferenceUtil.getUserEdition(),
                    currentTab = if (feedPage == null) null else RecentTabEntity(feedPage.id, feedPage.entityType, feedPage.section, 0),
                    deviceWidth = DeviceInfoHelper.getDeviceInfo().width.toInt(),
                    deviceHeight = DeviceInfoHelper.getDeviceInfo().height.toInt(),
                    follows = followEntityDao.recentActions(curTime - dislikeTimeLimit()),
                    connectionInfo = null,
                    autoplayPlayerTypes = AutoplayHelper.getAutoplayPlayerTypes(),
                    langs = AppUserPreferenceUtils.getUserNavigationLanguage(),
                    isUserSelectedLang = UserPreferenceUtil.getUserLanguages().isNullOrEmpty().not(),
                    successfulPrevFeedLoadSessions =
                    if(feedPage != null && feedPage.id == pageEntityDao.getFirstPageId(PageSection.NEWS
                                    .section) && feedPage.section == PageSection.NEWS.section) {
                        PreferenceManager.getInt (GenericAppStatePreference.SUCCESSFUL_PREV_FEED_LOAD_SESSION_COUNT.name,0)
                    }
                    else {
                        null
                    },
                    languageCardShownCount =
                    if(feedPage != null && feedPage.id == pageEntityDao.getFirstPageId(PageSection.NEWS.section) && feedPage.section == PageSection.NEWS.section) {
                        PreferenceManager.getPreference(GenericAppStatePreference.LANG_CARD_TIMES_SHOWN_TO_USER,0)
                    } else {
                        null
                    },
                    clientTZ = TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT),
                    clientTS = curTime.toString(),
                    cid = ClientInfoHelper.getClientId(),
                    requestId = HeaderInterceptor.generateRequestId(),
                    recentArticlesV2 = com.newshunt.dataentity.social.entity.CardsPayload.P_RecentArticles(
                            news = recentArticleTrackerDao.recentViewedArticles(curTime - dislikeTimeLimit())
                    ),
                    requestSessionContext = CardsPayload.P_RequestSessionContext(1,
                            if (feedPage == null) Collections.emptyList() else
                                pullDao.pullInfoWithLimitsAndCleanup(curTime - pullTimeLimit(), feedPage.id, feedPage.section)),
                    currentVideoSessionInfo = CardsPayload.P_VideoSessionInfo(VideoPlayedCache.entries()),
                    recentTabs = pullDao.recentTabsWithLimitsAndCleanup(curTime - pullTimeLimit()).groupBy { it.section },
                    dislikesV2 = dislikeDao.recentDislikes(curTime - dislikeTimeLimit()).map {
                        it.copy(eventParam = null)
                    },
                    userHasAnyFollows = followEntityDao.userHasAnyFollows(),
                    enableSmallCards = PreferenceManager.getPreference(AppStatePreference.ENABLE_SMALL_CARD, false),
                    localCookie = if (feedPage == null) null else cookieDao.getLocalCookie(feedPage.id),
                    globalCookie = cookieDao.getGlobalCookie(),
                    recommendFollowBlockRequest= recommendFollowBlockRequestPayload,
                     isNotificationEnabled = NotificationManagerCompat.from(CommonUtils
                            .getApplication()).areNotificationsEnabled(),
                    videosServedNotViewed = if (feedPage?.section == Constants.SECTION_LOCAL)
                        ServedButNotPlayedHelper.getlocalZoneServedList() else
                        (if (feedPage?.id?.endsWith("_related") == true)
                            ServedButNotPlayedHelper.getRelatedVideoSBNP() else null),
                    selectedLocation = if (feedPage?.section == Constants.SECTION_LOCAL)
                        PreferenceManager.getString(NewsConstants.LOCAL_SELECTED_LOCATION_KEY, Constants.EMPTY_STRING)
                    else
                        Constants.EMPTY_STRING,
                    cardsDiscarded = CardSeenStatusRepo.DEFAULT.tagDiscardedWith(cssId),
                    cardsViewed = CardSeenStatusRepo.DEFAULT.tagSeenWith(cssId),
                    cssBatchId = cssId,
                    impressionsData = impressionsData
                    )

        }
    }

    companion object {
        const val B_FEEDPAGE_ENTITY = "entityId"
        const val B_LOCALZONE_LOCATION = "locationZone"
        const val EXPLICIT_SIGNAL = "explicit_signal"
        const val RECOMMENDED_FOLLOW_REQUEST = "recommended_follow_block_request"
        const val B_IMPRESSIONS_DATA = "impressions_data"
        val _10M = 20_000L
        fun pullTimeLimit(): Long {
            return Math.max(_10M, PreferenceManager.getPreference(AppStatePreference.PAYLOAD_RECENT_PULLS_TIME_LIMIT, _10M))
        }
        fun dislikeTimeLimit() = Math.max(_10M, PreferenceManager.getPreference(AppStatePreference.PAYLOAD_RECENT_DISLIKES_TIME_LIMIT, _10M))
    }

}