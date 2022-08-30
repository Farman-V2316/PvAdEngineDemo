/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.di

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.newshunt.appview.common.di.CardsModule
import com.newshunt.appview.common.ui.activity.CreateDummyPostPojo
import com.newshunt.appview.common.ui.activity.ViewAllCommentsFragment
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.di.scopes.PerFragment
import com.newshunt.news.model.apis.NewsDetailAPI
import com.newshunt.news.model.apis.NewsDetailAPIProxy
import com.newshunt.news.model.apis.PostCreationService
import com.newshunt.news.model.apis.PostDeletionService
import com.newshunt.news.model.apis.PostReportService
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.GeneralFeedDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.view.present.discussionRespHandle
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import java.util.Collections
import javax.inject.Inject
import javax.inject.Named

/**
 * Activity to display complete list of all discussions.
 *
 * Created by karthik.r on 2020-02-12.
 */
private const val TAG = "ViewAllComModule"

@Module
class ViewAllCommentsModule(private val app: Application,
                            private val postId: String,
                            private val section: String,
                            private val lifecycleOwner: LifecycleOwner,
                            private val pageReferrer: PageReferrer?) {

    @Provides
    @PerFragment
    @Named("pageReferrer")
    fun pageReferrer(): PageReferrer? = pageReferrer

    @Provides
    @PerFragment
    fun detailAPI() = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(CommonUtils
            .formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
            Priority.PRIORITY_HIGHEST, "").create(NewsDetailAPI::class.java)

    @Provides
    @PerFragment
    fun postService(): PostCreationService = RestAdapterContainer.getInstance().getRestAdapter(
            NewsBaseUrlContainer.getPostCreationBaseUrl(), Priority.PRIORITY_HIGH, null, true, HTTP401Interceptor())
            .create(PostCreationService::class.java)

    @Provides
    fun postDeletionService(): PostDeletionService =
            RestAdapterContainer.getInstance().getRestAdapter(
                    NewsBaseUrlContainer.getPostDeletionBaseUrl(),
                    Priority.PRIORITY_HIGHEST, null, true, HTTP401Interceptor()
            ).create(PostDeletionService::class.java)

    @Provides
    fun postReportService(): PostReportService =
            RestAdapterContainer.getInstance().getRestAdapter(
                    NewsBaseUrlContainer.getPostReportBaseUrl(),
                    Priority.PRIORITY_HIGHEST, null, true, HTTP401Interceptor()
            ).create(PostReportService::class.java)
}

@PerFragment
@Component(modules = [CardsModule::class, ViewAllCommentsModule::class])
interface ViewAllCommentsComponent {
    fun inject(component: ViewAllCommentsFragment)
}

class ReadNetworkCommentsUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("entityId") private val entityId: String,
                    private val api: NewsDetailAPI,
                    private val fetchDao: FetchDao,
                    private val groupFeedDao: GeneralFeedDao) : BundleUsecase<Pair<String, Integer>> {

    override fun invoke(p1: Bundle): Observable<Pair<String, Integer>> {
        val filter :CreatePostUiMode = p1.getSerializable(Constants.BUNDLE_FILTER) as? CreatePostUiMode? ?: CreatePostUiMode.ALL
        return api.getDiscussionsForPost(postId, filter.name).map { it ->
            if (it.body() == null) {
                Pair(Constants.EMPTY_STRING, Integer(0))
            } else {
                val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
                        ?: postId
                discussionRespHandle(it.body().data?.rows, postId, fetchDao, groupFeedDao, location,
                        section, SocialDB.instance().postDao(), PostEntityLevel.TOP_LEVEL, uniqueId,
                        true)
                Pair(it.body().data?.nextPageUrl ?: "", Integer(it.body().data.count ?: 0))
            }
        }.onErrorReturn { t: Throwable ->
            Logger.e(TAG, "Error fetching next discussion", t)
            throw t
        }
    }
}

class ClearAndReadNetworkCommentsUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("entityId") private val entityId: String,
                    private val readNetworkCommentsUsecase: ReadNetworkCommentsUsecase,
                    private val api: NewsDetailAPI,
                    private val fetchDao: FetchDao,
                    private val groupFeedDao: GeneralFeedDao) : BundleUsecase<Pair<String, Integer>> {

    override fun invoke(p1: Bundle): Observable<Pair<String, Integer>> {
        return Observable.fromCallable {
            SocialDB.instance().discussionsDao().deleteForParentId(postId, entityId, location, section)
            readNetworkCommentsUsecase.invoke(p1).blockingFirst()
        }
    }
}

class CreateDummyPostUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    private val api: NewsDetailAPI,
                    private val fetchDao: FetchDao,
                    private val groupFeedDao: GeneralFeedDao,
                    private val followEntityDao: FollowEntityDao,
                    private val postDao : PostDao) : BundleUsecase<CreateDummyPostPojo> {

    override fun invoke(p1: Bundle): Observable<CreateDummyPostPojo> {
        val title = p1.getString(Constants.BUNDLE_ACTIVITY_TITLE)

        val local: Observable<CreateDummyPostPojo> = Observable.fromCallable {
            var post = fetchDao.lookupById(postId)
            if (post == null) {
                post = PostEntity(id = postId, title = title)
            }

            post.level = PostEntityLevel.TOP_LEVEL
            groupFeedDao.insReplace(GeneralFeed(entityId, "", "POST", section))
            val fie = FetchInfoEntity(entityId, location, null, 0, null, null, 0, section)
            val posts = Collections.singletonList(post)
            val fetchId = fetchDao.insertPostBothChunks(fie, posts, followEntityDao)
            CreateDummyPostPojo(post, fetchId, true)
        }

        val network: Observable<CreateDummyPostPojo> = NewsDetailAPIProxy.contentOfPost(api, postId,
                entityId, location, section, false, true, null, null, postDao)
                .lift(ApiResponseOperator()).map {
            ApiResponseUtils.throwErrorIfDataNull(it)
            val post = it.data
            post.level = PostEntityLevel.TOP_LEVEL
            groupFeedDao.insReplace(GeneralFeed(entityId, "", "POST", section))
            val fie = FetchInfoEntity(entityId, location, null, 0, null, null, 0, section)
            val posts = Collections.singletonList(post)
            val fetchId = fetchDao.insertPostBothChunks(fie, posts, followEntityDao)
            CreateDummyPostPojo(post, fetchId, false)
        }.onErrorResumeNext { it : Throwable ->
            local.map {
                it
            }
        }

        return network
    }
}

class ReadDiscussionsForViewCommentsUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("entityId") private val entityId: String,
                    private val api: NewsDetailAPI,
                    private val fetchDao: FetchDao,
                    private val groupFeedDao: GeneralFeedDao,
                    private val followEntityDao: FollowEntityDao) :
        BundleUsecase<String> {

    override fun invoke(p1: Bundle): Observable<String> {
        val contentUrl = p1.getString(Constants.BUNDLE_CONTENT_URL)
        return api.getDiscussions(contentUrl).lift(ApiResponseOperator()).map { it ->
            val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
                    ?: postId
            discussionRespHandle(it.data?.rows, postId, fetchDao, groupFeedDao, location,
                    section, SocialDB.instance().postDao(), PostEntityLevel.TOP_LEVEL, uniqueId,
                    true)
            it.data.nextPageUrl!!
        }.onErrorResumeNext { t: Throwable ->
            Logger.e(TAG, "Error fetching next discussion", t)
            Observable.just("")
        }
    }
}