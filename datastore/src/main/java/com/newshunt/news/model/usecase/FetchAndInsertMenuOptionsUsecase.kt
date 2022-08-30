/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.apis.MenuApi
import com.newshunt.news.model.daos.MenuDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import javax.inject.Inject

/**
 * @author satosh.dhanyamraju
 */
class FetchAndInsertMenuOptionsUsecase @Inject constructor(val menuDao: MenuDao,
                                                           private val menuApi: MenuApi) : BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {

        return Observable.fromCallable {
            menuDao.fetchMenuMeta()
        }.flatMap {
            val version = it.getOrNull(0)?.version
            menuApi.getMenuDictionary(version = version)
        }.map {
            menuDao.clearAndinsert1(it.data)
            PreferenceManager.savePreference(AppStatePreference
                    .DISLIKE_CONTENT_AVAILABLE, true)
        }.map {
            true // success
        }
    }

    companion object {
        @JvmStatic
        fun create(): Usecase<Bundle, Boolean> {
            return FetchAndInsertMenuOptionsUsecase(SocialDB.instance().menuDao(),
                    RestAdapterContainer.getInstance()
                            .getDynamicRestAdapterRx(
                                    CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
                                    Priority.PRIORITY_HIGHEST,
                                    ""
                            ).create(MenuApi::class.java))
        }
    }
}