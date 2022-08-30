/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.di

import android.os.Handler
import android.os.Looper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.news.model.entity.MenuEntity
import com.newshunt.dhutil.Expirable
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.internal.rest.PostDislikeApi
import com.newshunt.news.model.internal.service.MenuService
import com.newshunt.news.model.internal.service.MenuServiceImpl
import com.newshunt.sdk.network.Priority
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author satosh.dhanymaraju
 */
@Module
class MenuModule(){

    private val ttlPref = GenericAppStatePreference.RECENT_ACTIVITY_TTL

    private val dislikePref = GenericAppStatePreference.DISLIKED_LIST

    // for json deserialzation
    private abstract class G(any: Any) : List<Expirable<MenuEntity>>

    @Provides
    @Singleton
    fun dislikeService(): MenuService {
        val initialjson = PreferenceManager.getPreference(dislikePref, "")
        val initialList = CommonUtils.GSON.fromJson<List<Expirable<MenuEntity>>>(initialjson, G::class.java)
        val runner: (() -> Unit) -> Unit = { Handler(Looper.getMainLooper()).post(it) }
        val baseUrl = CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl())
        val api = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(baseUrl, Priority
                .PRIORITY_HIGHEST, null).create(PostDislikeApi::class.java)

        val save: (List<Expirable<MenuEntity>>) -> Unit = {
            val fornextsession = it.filterNot { it.value.isPerSession }
            PreferenceManager.savePreference(dislikePref, CommonUtils.GSON.toJson(fornextsession))
        }
        return MenuServiceImpl(initialList?: emptyList(), runner, api, save)
    }
}

