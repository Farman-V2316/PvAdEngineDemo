/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable
import javax.inject.Inject

/**
 * @author amit.chaudhary
 * */
class HidePostUsecase @Inject constructor(private val fetchDao: FetchDao) : BundleUsecase<Boolean> {
    override fun invoke(data: Bundle): Observable<Boolean> {


        val menuLocation = data.getSerializable(Constants.BUNDLE_MENU_CLICK_LOCATION) as? MenuLocation

        //If item is in detail view do not hide content
        if (menuLocation != null && menuLocation == MenuLocation.DETAIL) {
            Logger.e(LOG_TAG, "Not hiding item as it is in detail view")
            return Observable.empty()
        }

        val postIds = data.getStringArrayList(Constants.BUNDLE_POST_IDS) ?: run {
                    Logger.e(LOG_TAG, "postId can not be null")
                    return Observable.empty()
                }

        val sectionId = data.getString(NewsConstants.DH_SECTION) ?: run {
                    Logger.e(LOG_TAG, "section id can not be null")
                    return Observable.empty()
                }

        val fetchLocation = data.getString(Constants.BUNDLE_LOCATION_ID) ?: run {
            Logger.e(LOG_TAG, "fetch location id can not be null")
            return Observable.empty()
        }

        val entityId = data.getString(Constants.BUNDLE_ENTITY_ID) ?: run {
                    Logger.e(LOG_TAG, "entityId can not be null")
                    return Observable.empty()
                }

        return Observable.fromCallable {
            try {
                val fetchInfo = fetchDao.fetchInfo(entityId, fetchLocation, sectionId)
                if (fetchInfo != null) {
                    fetchDao.deleteFetchDataOfPostIds(fetchInfo.fetchInfoId, postIds)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

}

private const val LOG_TAG = "HidePostUsecase"