/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dhutil.CacheProvider
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable

/**
 * To run db cleanup tasks on app events like logout, language change etc.
 * @author satosh.dhanyamraju
 */
class CleanupDBOnAppEventUsecase(private val socialDB: SocialDB) : BundleUsecase<Unit> {

    override fun invoke(p1: Bundle): Observable<Unit> {
        return when (p1.getSerializable(BUNDLE_EVENT_TYPE) as? CleanupType) {
            CleanupType.LOGOUT -> Observable.mergeDelayError(
                    Observable.fromCallable {
                        socialDB.commonDao().cleanUpOnUserLogout(socialDB)
                        CommonUtils.clearOkHttpCache()
                    },
                    CachedApiCacheRx(CacheProvider.getCachedApiCache(NewsConstants
                            .HTTP_FEED_CACHE_DIR)).clear().map { Unit }
            )

            CleanupType.APP_START -> Observable.fromCallable {
                socialDB.commonDao().cleanupOnAppStart(socialDB)
            }

            CleanupType.LANG_CHANGE -> Observable.fromCallable {
                socialDB.commonDao().cleanUpOnLanguageChange(socialDB)
                VersionedApiHelper.cleanUpVersionedData(listOf(VersionEntity.NEWS_STICKY_OPTIN.name))
                CommonUtils.clearOkHttpCache()
            }
            CleanupType.APP_LANG_CHANGE -> Observable.fromCallable {
                socialDB.commonDao().cleanUpOnAppLanguageChange(socialDB)
                VersionedApiHelper.cleanUpVersionedData(listOf(VersionEntity.NEWS_STICKY_OPTIN.name))
                CommonUtils.clearOkHttpCache()
            }

            CleanupType.CARD_STYLE -> Observable.mergeDelayError(
                    Observable.fromCallable {
                        socialDB.commonDao().cleanUpOnCardStyleChange(socialDB)
                        CommonUtils.clearOkHttpCache()
                    },
                    CachedApiCacheRx(CacheProvider.getCachedApiCache(NewsConstants
                            .HTTP_FEED_CACHE_DIR)).clear().map { Unit }
            )

            else -> Observable.error<Unit>(Throwable("CleanupDBOnAppEventUsecase: unknown type"))
        }
    }

    enum class CleanupType {
        LOGOUT, APP_START, LANG_CHANGE, CARD_STYLE,APP_LANG_CHANGE
    }

    companion object {
        private const val BUNDLE_EVENT_TYPE = "dbCleanupEventType"
        @JvmStatic val LOGOUT_BUNDLE = bundleOf(BUNDLE_EVENT_TYPE to CleanupType.LOGOUT)
        @JvmStatic val APPSTART_BUNDLE = bundleOf(BUNDLE_EVENT_TYPE to CleanupType.APP_START)
        @JvmStatic val LANGCHANGE_BUNDLE = bundleOf(BUNDLE_EVENT_TYPE to CleanupType.LANG_CHANGE)
        @JvmStatic val APPLANGCHANGE_BUNDLE = bundleOf(BUNDLE_EVENT_TYPE to CleanupType.APP_LANG_CHANGE)
        @JvmStatic val CARDSTYLE_BUNDLE = bundleOf(BUNDLE_EVENT_TYPE to CleanupType.CARD_STYLE)
    }

}