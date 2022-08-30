/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.news.model.apis.NewsDetailAPIProxy
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.GeneralFeedDao
import com.newshunt.news.model.daos.PageEntityDao
import com.newshunt.news.model.daos.PostDao
import io.reactivex.Observable
import io.reactivex.functions.Function
import java.util.Collections
import javax.inject.Inject
import javax.inject.Named

/**
 * Will be invoked in notification and flows other than card-click.
 *
 * @author satosh.dhanyamraju
 */
class ReadFullPostUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("referrerFlow") private val referrerFlow: PageReferrer?,
                    private val fetchDao: FetchDao,
                    private val groupFeedDao: GeneralFeedDao,
                    private val followEntityDao: FollowEntityDao,
                    private val pageEntityDao: PageEntityDao,
                    @Named("isViralPost")
                    private val isViralPost: Function<PostEntity?, Boolean>,
                    val newsdetailCachedAPI: NewsDetailAPIProxy,
                    private val postDao: PostDao)
    : BundleUsecase<PostEntity?> {
    private val LOG_TAG = "ReadFullPostUsecase"
    override fun invoke(p1: Bundle): Observable<PostEntity?> {

        var referrerFlowName = Constants.EMPTY_STRING
        var referrerFlowId = Constants.EMPTY_STRING
        if (referrerFlow != null) {
            if (referrerFlow.referrer.referrerName != null) {
                referrerFlowName = referrerFlow.referrer.referrerName
            }

            if (referrerFlow.id!= null) {
                referrerFlowId = referrerFlow.id
            }
        }

        return NewsDetailAPIProxy.contentOfPost(newsdetailCachedAPI, postId,
                entityId, location, section, false, true, referrerFlowName, referrerFlowId, postDao)
                .map { apiResp ->
                    val pageId = entityId //"$entityId-$postId-${System.currentTimeMillis()}"
                    Logger.d(LOG_TAG, "Got response $pageId, $postId")
                    var url = p1.getString(Constants.BUNDLE_CONTENT_URL)
                    val contentUrlOptional = p1.getBoolean(Constants.BUNDLE_CONTENT_URL_OPTIONAL, false)
                    val data = apiResp.body().data
                    if (contentUrlOptional && isViralPost.apply(data)) {
                        val contentUrl = pageEntityDao.getAllPages(AppSection.NEWS.name.toLowerCase())
                                ?.filter {
                                    it.pageEntity.contentUrl?.contains(PageType.VIRAL.pageType) == true
                                }
                                ?.getOrNull(0)
                                ?.pageEntity
                                ?.contentUrl
                        if (!CommonUtils.isEmpty(contentUrl)) {
                            url = contentUrl
                        }
                    }
                    if (fetchDao.lookupPage(pageId, section) == null) {
                        groupFeedDao.insReplace(GeneralFeed(pageId, "", "POST", section))
                    }
                    val fie = FetchInfoEntity(pageId, location, url, 0, null, null, 0, section)
                    val posts = Collections.singletonList(data)
                    fetchDao.insertPostBothChunks(fie, posts, followEntityDao, p1.getString(Constants.BUNDLE_AD_ID))
                    data
                }
    }
}