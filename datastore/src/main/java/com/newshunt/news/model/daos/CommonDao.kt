/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Transaction
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.sqlite.SocialDB

/**
 * For executing methods of mulitple daos in 1 transaction
 *
 * @author satosh.dhanymraju
 */
@Dao
abstract class CommonDao {

    @Transaction
    open fun cleanupOnAppStart(socialDB: SocialDB) {
        with(socialDB) {
            with(fetchDao()) {
                resetViewDestroyTime()
                deleteAllAds()
                deleteOldAdSpecs(50)
                deleteAllFetchInfo()
                deleteExpiredLocalCards(System.currentTimeMillis() -
                        PreferenceManager.getPreference(GenericAppStatePreference.LOCAL_CARD_TTL, Constants.DEFAULT_LOCAL_CARD_TTL))
                deleteProxyAdCards()
            }
            /* disliking astrocard is limited to current session. Can be shown from next session*/
            dislikeDao().removeAstroCards()
            groupInfoDao().deleteAll()
            memberDao().deleteAll()
            historyDao().flushOld()
            entityInfoDao().cleanUpEntityInfo()
            groupDao().clearRowsNotInFetchInfo()
            nudgeDao().markAllActive()
            inAppUpdatesDao().cleanupOlderUpdateInfo()
            PreferenceManager.savePreference(AppStatePreference.NUDGE_SHOWN_IN_CURRENT_LAUNCH, false)
        }
    }

    @Transaction
    open fun cleanUpOnUserLogout(socialDB: SocialDB) {
        with(socialDB) {
            dislikeDao().deleteAll()
            followEntityDao().deleteAll()
            pullDao().deleteAllPullInfo()
            pullDao().deleteAllRecentTabs()
            interactionsDao().deleteAll()
            fetchDao().deleteFetchInfoAndLocalCards()
            cssDao().markAllUnknownAsDiscarded()
            deletedInteractionsDao().clearDeletedInteractions()
            bookmarkDao().clearBookmarks()
            historyDao().clearForever()
            pendingApprovalsDao().delete()
            groupInfoDao().deleteAll()
            memberDao().deleteAll()
            voteDao().deleteAll()
        }
    }

    @Transaction
    open fun cleanUpOnLanguageChange(socialDB: SocialDB) {
        with(socialDB) {
            fetchDao().deleteFetchInfoAndLocalCards()
            cssDao().markAllUnknownAsDiscarded()
            locationsDao().deleteAllLocations()
        }
    }

    @Transaction
    open fun cleanUpOnAppLanguageChange(socialDB: SocialDB) {
        with(socialDB) {
            locationsDao().deleteAllLocations()
        }
    }

    @Transaction
    open fun cleanupOnAppUpgrade(fetchDao: FetchDao, discussionsDao: DiscussionsDao,
                                 postDao: PostDao, adsDao: AdsDao, inAppUpdatesDao: InAppUpdatesDao,
                                 cssDao: CSSDao) {
        fetchDao.deleteFetchInfoAndLocalCards()
        cssDao.markAllUnknownAsDiscarded()
        discussionsDao.deleteAllDiscussion()
        postDao.deleteAllPost()
        adsDao.deleteAll()
        inAppUpdatesDao.deleteAll()
    }

    @Transaction
    open fun cleanUpOnCardStyleChange(socialDB: SocialDB) {
        with(socialDB) {
            fetchDao().deleteFetchInfoAndLocalCards()
        }
    }
}