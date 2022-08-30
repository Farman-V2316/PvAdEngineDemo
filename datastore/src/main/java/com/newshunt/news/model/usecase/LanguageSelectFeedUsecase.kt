/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.daos.FetchDao
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * @author amit.chaudhary
 */

class InsertLanguageSelectionCard @Inject constructor(@Named("entityId") private val entityId: String,
                                                      @Named("location") private val location: String,
                                                      @Named("section") private val section: String,
                                                      val fetchDao: FetchDao) : BundleUsecase<Any> {
    override fun invoke(p1: Bundle): Observable<Any> {
        return Observable.fromCallable {
            val prevPostId = p1.getString(BUNDLE_PREV_POST_ID) ?: return@fromCallable
            val languageSelectionCard = p1.getSerializable(BUNDLE_LANGUAGE_SELECTION_ITEM) as? PostEntity
                    ?: return@fromCallable
            val foryouId = PreferenceManager.getPreference(AppStatePreference.ID_OF_FORYOU_PAGE, Constants.EMPTY_STRING)
            if (foryouId == entityId && section == PageSection.NEWS.section) {
                Logger.d(LOG_TAG, "insert language select card")
                fetchDao.insertLanguageSelectionCard(languageSelectionCard, prevPostId, entityId, location, section)
            }
        }
    }

    companion object {
        const val BUNDLE_LANGUAGE_SELECTION_ITEM = "BUNDLE_LANGUAGE_SELECTION_ITEM"
        const val BUNDLE_PREV_POST_ID = "BUNDLE_PREV_POST_ID"
    }
}

class ClearLanguageSelectionCard @Inject constructor(
        @Named("entityId") private val entityId: String,
        @Named("location") private val location: String,
        @Named("section") private val section: String,
        val fetchDao: FetchDao) :
        BundleUsecase<Any> {
    override fun invoke(p1: Bundle): Observable<Any> {
        val languageCardId = p1.getString(Constants.BUNDLE_POST_ID)
                ?: return Observable.error(Throwable("Id not passed"))
        return Observable.fromCallable {
            val fetchInfo = fetchDao.fetchInfo(entityId, location, section)
            if (fetchInfo != null) {
                Logger.d(LOG_TAG, "Clear language select card")
                fetchDao.deleteFetchDataForPostMatching(fetchInfo.fetchInfoId,
                        listOf(PostEntity.joinFetchIdAndPostId(fetchInfo.fetchInfoId.toString(), languageCardId)))
                true
            } else {
                false
            }
        }
    }
}

private const val LOG_TAG = "LanguageSelectCardUsecase"