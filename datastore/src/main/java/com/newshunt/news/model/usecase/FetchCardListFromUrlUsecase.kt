/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.net.Uri
import android.os.Bundle
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.ServedButNotPlayedHelper
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.util.R
import com.newshunt.common.view.DbgCode
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CookieEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.social.entity.CardsPayload
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.apis.NewsApi
import com.newshunt.news.model.daos.CookieDao
import com.newshunt.news.model.utils.TransformNewsList
import io.reactivex.Observable
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Named

/**
 * Can do both get and post
 */
class FetchCardListFromUrlUsecase @Inject constructor(private val api: NewsApi,
                                                      @Named("buildPayloadUsecase")
                                                      private val buildPayloadUsecase: BundleUsecase<Any>,
                                                      private val cookieDao: CookieDao,
                                                      private val f: TransformNewsList) : BundleUsecase<NLResponseWrapper> {

    override fun invoke(p1: Bundle): Observable<NLResponseWrapper> {
        val url = p1.getString(B_URL, null) ?: throw Throwable("url shouldn't be null")
        val type = p1.getString(B_REQ_TYPE, Constants.HTTP_GET)
        val feedPage: FeedPage? = (p1.getSerializable(BuildPayloadUsecase.B_FEEDPAGE_ENTITY) as? FeedPage)
        val map = mutableMapOf<String, String>()
        val userLanguages = UserPreferenceUtil.getUserLanguages()
        if (userLanguages.isNullOrEmpty().not()) {
            map["langCode"] = userLanguages
        } else if (Uri.parse(url).getQueryParameter("langCode").isNullOrEmpty()) {
            map["langCode"] = Constants.ENGLISH_LANGUAGE_CODE
        }

        val section = p1.getString(B_SECTION, Constants.SECTION1)

        map["appLanguage"] = UserPreferenceUtil.getUserNavigationLanguage()

        val finalUrl = UrlUtil.getUrlWithQueryParamns(url, map)
        val key = p1.getString(B_KEY)
        if (type == Constants.HTTP_GET) {
            return api.getNews2(finalUrl).lift(ApiResponseOperator()).map {
                ApiResponseUtils.throwErrorIfDataNull(it)
                if (it.data.rows == null) {
                    val error = BaseError(DbgCode.DbgHttpCode(HttpURLConnection.HTTP_NO_CONTENT), CommonUtils.getString(R.string.no_content_found), it.code, it.url)
                    throw ListNoContentException(error)
                }
                val onlyPosts = f.transf(it.data.rows)
                it.data.rows = onlyPosts
                val localCookie = it.data.localCookie?:""
                if (feedPage != null && localCookie.isNotEmpty())
                    cookieDao.insReplace(CookieEntity(feedPage.id, localCookie))
                val globalCookie = it.data.globalCookie?:""
                if (globalCookie.isNotEmpty())
                    cookieDao.insReplace(CookieEntity("global", globalCookie))
                it.data
                NLResponseWrapper(url, it.data, key ?: url)
            }
        }
        return Observable.fromCallable {
            System.currentTimeMillis()
        }.flatMap { start ->
            buildPayloadUsecase(p1).map { it to start }
        }.flatMap { (payload, startTime )->
            val batchId = (payload as? CardsPayload)?.cssBatchId
            api.postNews2(finalUrl, payload, batchId).lift(ApiResponseOperator())
                    .map { it to startTime }

        }.map {(resp , startTime) ->
            ApiResponseUtils.throwErrorIfDataNull(resp)
            val onlyPosts = f.transf(resp.data.rows)
            resp.data.rows = onlyPosts
            resp.data.isFromNetwork = true
            resp.data.timeTakenToFetch = System.currentTimeMillis() - startTime
            if(section == Constants.SECTION_LOCAL && null != onlyPosts) {
                // Adding fetched item ID's to Local zone served list
                for (item in onlyPosts) {
                    val commonAsset = item as? CommonAsset
                    if (null != commonAsset) {
                        ServedButNotPlayedHelper.addTOlocalZoneServedList(commonAsset.i_id())
                    }
                }
            }
            val localCookie = resp.data.localCookie?:""
            if (feedPage != null && !CommonUtils.isEmpty(localCookie))
                cookieDao.insReplace(CookieEntity(feedPage.id, localCookie))
            val globalCookie = resp.data.globalCookie?:""
            if (!CommonUtils.isEmpty(globalCookie))
                cookieDao.insReplace(CookieEntity("global", globalCookie))
            NLResponseWrapper(url, resp.data, key ?: url)
        }
    }

    companion object {
        private const val B_URL = "url"
        private const val B_KEY = "b_fd_key"
        private const val B_REQ_TYPE = "b_req_type"
        private const val B_SECTION = "b_section"
        fun bundle(page: FeedPage, bundle: Bundle = Bundle(), key: String? = null) =
                bundle.apply {
                    putAll(bundleOf(
                            B_URL to page.contentUrl,
                            B_REQ_TYPE to page.contentRequestMethod,
                            BuildPayloadUsecase.B_FEEDPAGE_ENTITY to page,
                            B_KEY to key,
                            B_SECTION to page.section
                    ))
                }

        fun bundle(url: String, requestType: String, id: String) : Bundle =
           bundleOf(
                B_URL to url,
                B_REQ_TYPE to requestType,
                BuildPayloadUsecase.B_FEEDPAGE_ENTITY to FeedPage(id, url, requestType)
            )
        fun bundle(url: String, requestType: String, id: String, impressionsData : List<String?>?) : Bundle =
           bundleOf(
                B_URL to url,
                B_REQ_TYPE to requestType,
                BuildPayloadUsecase.B_FEEDPAGE_ENTITY to FeedPage(id, url, requestType),
                BuildPayloadUsecase.B_IMPRESSIONS_DATA to impressionsData
            )

        fun bundle(url: String, requestType: String, id: String,recommendFollowBlock :CardsPayload.FollowBlockRequest?) : Bundle =
            bundleOf(
                B_URL to url,
                B_REQ_TYPE to requestType,
                BuildPayloadUsecase.B_FEEDPAGE_ENTITY to FeedPage(id, url, requestType),
                BuildPayloadUsecase.RECOMMENDED_FOLLOW_REQUEST to recommendFollowBlock

            )

        fun bundle(url: String, requestType: String, id: String, location: String) : Bundle =
                bundleOf(
                        B_URL to url,
                        B_REQ_TYPE to requestType,
                        BuildPayloadUsecase.B_LOCALZONE_LOCATION to location,
                        BuildPayloadUsecase.B_FEEDPAGE_ENTITY to FeedPage(id, url, requestType)
                )

        fun bundle(url: String, requestType: String): Bundle =
                bundleOf(
                        B_URL to url,
                        B_REQ_TYPE to requestType)
    }
}