package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.cachedapi.CachedApiCacheRx
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.CacheProvider
import com.newshunt.news.model.daos.AdsDao
import com.newshunt.news.model.daos.CSSDao
import com.newshunt.news.model.daos.CommonDao
import com.newshunt.news.model.daos.DiscussionsDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.InAppUpdatesDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.schedulers.Schedulers

class CleanupDataOnUpgradeUsecase(private val commonDao: CommonDao,
                                  private val fetchDao: FetchDao,
                                  private val discussionDao: DiscussionsDao,
                                  private val postDao: PostDao,
                                  private val adsDao: AdsDao,
                                  private val inAppUpdatesDao: InAppUpdatesDao,
                                  private val cssDao: CSSDao
                                  ) : BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        Logger.i(LOG_TAG, "Clearing cache and db data on app upgrade")
        val observable = Observable.merge(
                clearCache(),
                clearDatabase()
        )
        return observable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun clearDatabase(): ObservableSource<Boolean> {
        return Observable.fromCallable {
            commonDao.cleanupOnAppUpgrade(fetchDao, discussionDao, postDao, adsDao, inAppUpdatesDao, cssDao)
            true
        }
    }

    private fun clearCache(): Observable<Boolean> {
        val cache = CachedApiCacheRx(CacheProvider.getCachedApiCache(NewsConstants.HTTP_FEED_CACHE_DIR))
        return cache.clear()
    }

    companion object {
        private const val LOG_TAG = "CleanupDataOnUpgradeUsecase"
    }
}