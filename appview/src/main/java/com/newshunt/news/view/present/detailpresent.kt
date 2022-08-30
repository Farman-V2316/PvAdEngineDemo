/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.present

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsDialogEvent
import com.newshunt.analytics.entity.NhAnalyticsDialogEventParam
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.LikesResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.common.pages.ReportEntity
import com.newshunt.dataentity.social.entity.AdditionalContents
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dataentity.social.entity.LocalDelete
import com.newshunt.dataentity.social.entity.PhotoChild
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.apis.NewsDetailAPI
import com.newshunt.news.model.apis.NewsDetailAPIProxy
import com.newshunt.news.model.apis.PostDeletionService
import com.newshunt.news.model.apis.PostReportService
import com.newshunt.news.model.daos.AdditionalContentsDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.GeneralFeedDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.internal.rest.NewsCarouselAPI
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.FetchRelatedStoriesUsecase
import com.newshunt.news.model.usecase.FetchRelatedVideosUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.ReadFullPostUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.Usecase
import com.newshunt.socialfeatures.helper.analytics.NHAnalyticsSocialCommentsEventParam
import com.newshunt.socialfeatures.helper.analytics.NHSocialAnalyticsEvent
import com.newshunt.sso.SSO
import io.reactivex.Observable
import java.lang.Exception
import java.util.Collections
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "DetailsViewModel"
private const val COMMENT_TYPE_MAIN = "main"
private const val REPORT_SPAM_YES = "yes"
private const val REPORT_SPAM_COMMENT = "report_spam_comment"

class ReadLikesFirstPageUsecase
@Inject constructor(@Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("listLocation") private val listLocation: String,
                    @Named("level") private val level: String,
                    private val groupFeedDao: GeneralFeedDao,
                    private val fetchDao: FetchDao,
                    @Named("postId") private val postId: String,
                    private val postDao: PostDao,
                    @Named("normalPriorityDetailAPI") private val normalPriorityDetailAPI: NewsDetailAPI) :
        BundleUsecase<LikesResponse> {

    override fun invoke(p1: Bundle): Observable<LikesResponse> {
        val postLevel = p1.getString(Constants.BUNDLE_LEVEL)
        val entityLevel = if (postLevel == null) PostEntityLevel.valueOf(level) else PostEntityLevel.valueOf(postLevel)
        return normalPriorityDetailAPI.getCountsForPost(postId).lift(ApiResponseOperator()).map {
            SocialDB.instance().postDao().updateCount(postId, it.data.counts)
            val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
                    ?: fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)

            uniqueId?.let { uniqueIdParam ->
                val localChildren = SocialDB.instance().fetchDao().getLocalCardForParent(postId)
                val localChildrenPosts = mutableListOf<PostEntity>()
                localChildren?.forEach {
                    localChildrenPosts.add(it.postEntity)
                }

                discussionRespHandle(localChildrenPosts, postId, fetchDao, groupFeedDao,
                        location, section, postDao, entityLevel, uniqueIdParam, true)

                discussionRespHandle(it.data.discussions?.rows, postId, fetchDao, groupFeedDao,
                        location, section, postDao, entityLevel, uniqueIdParam, true)
            }
            it.data
        }
    }
}

class SuggestedFollowUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    private val api: NewsDetailAPI) : BundleUsecase<List<PostSuggestedFollow>> {

    override fun invoke(p1: Bundle): Observable<List<PostSuggestedFollow>> {
        val network: Observable<List<PostSuggestedFollow>> = api.getSuggestedFollowForPostFromId(postId).map {
            it.data
        }

        return network
    }
}

class RelatedStoriesUsecase
@Inject constructor(private val fetchDao: FetchDao, @Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("section") private val section: String,
                    @Named("location") private val location: String,
                    @Named("listLocation") private val listLocation: String) :
        MediatorUsecase<Bundle, List<TopLevelCard>> {

    override fun execute(t: Bundle): Boolean {
        return true
    }

    override fun data(): LiveData<Result0<List<TopLevelCard>>> {
        val loc = fetchDao.related(postId, PostEntityLevel.RELATED_STORIES, entityId, location, section)
        val lloc = fetchDao.related(postId, PostEntityLevel.RELATED_STORIES, entityId, listLocation, section)
        val result = MediatorLiveData<List<TopLevelCard>>()
        result.addSource(loc) {
            if (it.isNotEmpty()) {
                result.value = it
            }
        }
        result.addSource(lloc) {
            if (it.isNotEmpty()) {
                result.value = it
            }
        }

        return Transformations.map(result) {
            Result0.success(it)
        }
    }
}


class RelatedStoriesFrommAddContentUsecase
@Inject constructor(private val fetchDao: FetchDao, @Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("listLocation") private val listLocation: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    private val fetchRelatedStoriesUsecase: FetchRelatedStoriesUsecase) : BundleUsecase<AdditionalContents?> {
    override fun invoke(p1: Bundle): Observable<AdditionalContents?> {

        var fetchRelatedStories = false
        return Observable.fromCallable {
            fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)
                    ?: fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
        }.flatMap {
            fetchDao.fetchAdditionalContents(it, Constants.RELATED_NEWS).map {
                it?.let {

                    if (!fetchRelatedStories) {
                        val bundle = Bundle()
                        bundle.putString(Constants.CONTENT_URL, it.content)
                        bundle.putString(Constants.REQUEST_METHOD, it.contentRequestMethod)
                        bundle.putLong(Constants.CONTENT_ID, it.id)

                        fetchRelatedStoriesUsecase.invoke(bundle).subscribe({
                            // Success
                        }, {
                            Logger.caughtException(it)
                        })
                    }

                    fetchRelatedStories = true

                }

                it
            }
        }

    }
}


class RelatedStoriesForVideoContentUsecase
@Inject constructor(private val fetchDao: FetchDao, @Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("listLocation") private val listLocation: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    private val fetchRelatedVideoUsecase: FetchRelatedVideosUsecase) :
        BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {

        return Observable.fromCallable {
            fetchRelatedVideoUsecase.invoke(p1).subscribe({
                // Success
            }, {
                Logger.caughtException(it)
            })
            true
        }

//        var fetchRelatedStories = false
//        return Observable.fromCallable {
//            fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)
//                    ?: fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
//        }.flatMap {
//            fetchDao.fetchAdditionalContents(it, Constants.RELATED_NEWS).map {
//                it?.let {
//
//                    if (!fetchRelatedStories) {
//                        fetchRelatedStoriesUsecase.invoke(p1).subscribe({
//                            // Success
//                        }, {
//                            Logger.caughtException(it)
//                        })
//                    }
//
//                    fetchRelatedStories = true
//
//                }
//
//                true
//            }
//        }

    }
}

class AdditionalContentFetchUsecase
@Inject constructor(private val fetchDao: FetchDao, @Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("listLocation") private val listLocation: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String) : Usecase<String, AdditionalContents?> {
    override fun invoke(type: String): Observable<AdditionalContents?> {
        return Observable.fromCallable {
            fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)
                    ?: fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
        }.flatMap {
            fetchDao.fetchAdditionalContents(it, type)
        }
    }
}

class ClearAndDownloadFirstDiscussion
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("listLocation") private val listLocation: String,
                    @Named("section") private val section: String,
                    private val discussionNetworkUsecase: DiscussionNetworkUsecase) : BundleUsecase<String?> {
    override fun invoke(p1: Bundle): Observable<String?> {
        return Observable.fromCallable {
            SocialDB.instance().discussionsDao().deleteForParentId(postId, entityId, location, section)
            SocialDB.instance().discussionsDao().deleteForParentId(postId, entityId, listLocation, section)
            p1.putBoolean(Constants.BUNDLE_CLEAR_EXISTING, true)
            discussionNetworkUsecase.invoke(p1).blockingFirst()
        }
    }
}

class AddNewDiscussionUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("level") private val level: String,
                    @Named("listLocation") private val listLocation: String,
                    @Named("section") private val section: String,
                    private val groupFeedDao: GeneralFeedDao,
                    private val fetchDao: FetchDao,
                    private val postDao: PostDao,
                    private val discussionNetworkUsecase: DiscussionNetworkUsecase) : BundleUsecase<Boolean> {
    override fun invoke(p1: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            val mode = p1.getString(Constants.BUNDLE_MODE)
            val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
                    ?: fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)
            val postLevel = p1.getString(Constants.BUNDLE_LEVEL)
            val entityLevel = if (postLevel == null) PostEntityLevel.valueOf(level) else PostEntityLevel.valueOf(postLevel)
            uniqueId?.let { uniqueIdParam ->

                val existingPosts = SocialDB.instance().fetchDao().discussionsListForPost(uniqueId)
                val discussionIds = mutableListOf<String>()
                existingPosts.forEach {
                    discussionIds.add(it.i_id())
                }

                val localChildren = SocialDB.instance().fetchDao().getLocalCardForParent(postId)
                val localChildrenPosts = mutableListOf<PostEntity>()
                localChildren?.forEach {
                    if (!discussionIds.contains(it.id)) {
                        if (mode == CreatePostUiMode.REPOST.name && it.i_type() == AssetType2.REPOST.name) {
                            localChildrenPosts.add(it.postEntity)
                        } else if (mode == CreatePostUiMode.COMMENT.name && it.i_type() == AssetType2.COMMENT.name) {
                            localChildrenPosts.add(it.postEntity)
                        } else if (mode == CreatePostUiMode.ALL.name) {
                            localChildrenPosts.add(it.postEntity)
                        }
                    }
                }

                discussionRespHandle(localChildrenPosts, postId, fetchDao, groupFeedDao,
                        location, section, postDao, entityLevel, uniqueIdParam, true)
            }

            true
        }
    }
}

class DiscussionNetworkUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("listLocation") private val listLocation: String,
                    @Named("level") private val level: String,
                    @Named("section") private val section: String,
                    private val groupFeedDao: GeneralFeedDao,
                    private val fetchDao: FetchDao,
                    private val postDao: PostDao,
                    private val api: NewsDetailAPI) : BundleUsecase<String?> {

    override fun invoke(p1: Bundle): Observable<String?> {
        val url = p1.getString(Constants.CONTENT_URL)
        val mode = p1.getString(Constants.BUNDLE_MODE)
        val postLevel = p1.getString(Constants.BUNDLE_LEVEL)
        val clearExisting = p1.getBoolean(Constants.BUNDLE_CLEAR_EXISTING, false)
        val entityLevel = if (postLevel == null) PostEntityLevel.valueOf(level) else PostEntityLevel.valueOf(postLevel)

        if (CommonUtils.isEmpty(mode)) {
            if (CommonUtils.isEmpty(url)) {
                return Observable.fromCallable {
                    if (clearExisting) {
                        SocialDB.instance().discussionsDao().deleteForParentId(postId, entityId, listLocation, section)
                    }

                    val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
                            ?: fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)
                    uniqueId?.let { uniqueIdParam ->

                        if (clearExisting) {
                            val localChildren = SocialDB.instance().fetchDao().getLocalCardForParent(postId)
                            val localChildrenPosts = mutableListOf<PostEntity>()
                            localChildren?.forEach {
                                localChildrenPosts.add(it.postEntity)
                            }

                            discussionRespHandle(localChildrenPosts, postId, fetchDao, groupFeedDao,
                                    location, section, postDao, entityLevel, uniqueIdParam, true)
                        }
                    }

                    Constants.EMPTY_STRING
                }
            }
            return api.getDiscussions(url).lift(ApiResponseOperator()).map {
                if (clearExisting) {
                    SocialDB.instance().discussionsDao().deleteForParentId(postId, entityId, listLocation, section)
                }

                val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
                        ?: fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)
                uniqueId?.let { uniqueIdParam ->
                    if (clearExisting) {
                        val localChildren = SocialDB.instance().fetchDao().getLocalCardForParent(postId)
                        val localChildrenPosts = mutableListOf<PostEntity>()
                        localChildren?.forEach {
                            localChildrenPosts.add(it.postEntity)
                        }

                        discussionRespHandle(localChildrenPosts, postId, fetchDao, groupFeedDao,
                                location, section, postDao, entityLevel, uniqueIdParam, true)
                    }

                    discussionRespHandle(it.data?.rows, postId, fetchDao, groupFeedDao, location,
                            section, postDao, entityLevel, uniqueIdParam, true)
                }
                it.data.nextPageUrl ?: Constants.EMPTY_STRING
            }.onErrorReturn { t: Throwable ->
                Logger.e(TAG, "Error", t)
                throw t
            }
        } else {
            if (CommonUtils.isEmpty(url)) {
                return Observable.fromCallable {
                    if (clearExisting) {
                        SocialDB.instance().discussionsDao().deleteForParentId(postId, entityId, listLocation, section)
                    }

                    val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
                            ?: fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)

                    uniqueId?.let { uniqueIdParam ->
                        if (clearExisting) {
                            val localChildren = SocialDB.instance().fetchDao().getLocalCardForParent(postId)
                            val localChildrenPosts = mutableListOf<PostEntity>()
                            localChildren?.forEach {
                                if (mode == CreatePostUiMode.REPOST.name && it.i_type() == AssetType2.REPOST.name) {
                                    localChildrenPosts.add(it.postEntity)
                                } else if (mode == CreatePostUiMode.COMMENT.name && it.i_type() == AssetType2.COMMENT.name) {
                                    localChildrenPosts.add(it.postEntity)
                                } else if (mode == CreatePostUiMode.ALL.name) {
                                    localChildrenPosts.add(it.postEntity)
                                }
                            }

                            discussionRespHandle(localChildrenPosts, postId, fetchDao, groupFeedDao,
                                    location, section, postDao, entityLevel, uniqueIdParam, true)
                        }
                    }

                    Constants.EMPTY_STRING
                }
            }

            return api.getDiscussions(url, mode).map {
                if (clearExisting) {
                    SocialDB.instance().discussionsDao().deleteForParentId(postId, entityId, listLocation, section)
                }
                val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
                        ?: fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)
                uniqueId?.let { uniqueIdParam ->
                    if (clearExisting) {
                        val localChildren = SocialDB.instance().fetchDao().getLocalCardForParent(postId)
                        val localChildrenPosts = mutableListOf<PostEntity>()
                        localChildren?.forEach {
                            if (mode == CreatePostUiMode.REPOST.name && it.i_type() == AssetType2.REPOST.name) {
                                localChildrenPosts.add(it.postEntity)
                            } else if (mode == CreatePostUiMode.COMMENT.name && it.i_type() == AssetType2.COMMENT.name) {
                                localChildrenPosts.add(it.postEntity)
                            } else if (mode == CreatePostUiMode.ALL.name) {
                                localChildrenPosts.add(it.postEntity)
                            }
                        }

                        discussionRespHandle(localChildrenPosts, postId, fetchDao, groupFeedDao,
                                location, section, postDao, entityLevel, uniqueIdParam, true)
                    }

                    discussionRespHandle(it.data?.rows, postId, fetchDao, groupFeedDao, location,
                            section, postDao, entityLevel, uniqueIdParam, true)
                }
                it.data.nextPageUrl ?: Constants.EMPTY_STRING
            }.onErrorReturn { t: Throwable ->
                Logger.e(TAG, "Error", t)
                throw t
            }
        }
    }
}

class PhotoChildUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    private val api: NewsDetailAPI) : BundleUsecase<List<PhotoChild>> {

    override fun invoke(p1: Bundle): Observable<List<PhotoChild>> {
        val url: String = p1.getString(Constants.CONTENT_URL)
                ?: return Observable.just(Collections.emptyList())
        return api.getChildPhotos(url).lift(ApiResponseOperator()).map {
            var i = 0
            it.data.rows.forEach {
                it.postId = postId
                it.viewOrder = i++
            }

            SocialDB.instance(CommonUtils.getApplication()).photoChildDao().insReplace(it.data.rows)
            it.data.rows
        }.onErrorReturn {
            throw it
        }
    }
}

class FetchMoreStoriesUsecase
@Inject constructor(private val api: NewsDetailAPI,
                    @Named("postId") private val postId: String) :
        BundleUsecase<MultiValueResponse<CommonAsset>> {

    override fun invoke(p1: Bundle): Observable<MultiValueResponse<CommonAsset>> {
        val path = p1.getString(Constants.CONTENT_URL)?.replace("{postId}", postId)
        val resp = api.getMoreStories2(path, UserPreferenceUtil.getUserLanguages(),
                UserPreferenceUtil.getUserNavigationLanguage(), UserPreferenceUtil.getUserEdition()).map {
            it.data
        }.map {
            /*TODO :: change commonAsset to postEntity if causing issue.*/
            it as MultiValueResponse<CommonAsset>
        }

        return resp
    }
}

class FetchCarouselMoreStoriesUsecase
@Inject constructor(private val api: NewsCarouselAPI,
                    @Named("buildPayloadUsecase")
                    private val buildPayloadUsecase: BundleUsecase<Any>,
                    @Named("postId") private val postId: String) :
        BundleUsecase<MultiValueResponse<CommonAsset>> {

    override fun invoke(p1: Bundle): Observable<MultiValueResponse<CommonAsset>> {
        val path = p1.getString(Constants.CONTENT_URL)?.replace("{postId}", postId)
                ?: throw Throwable("url shouldn't be null")

        return buildPayloadUsecase.invoke(p1).flatMap {

            val resp = api.getViewMoreNews2(path, UserPreferenceUtil.getUserNavigationLanguage(),
                    UserPreferenceUtil.getUserLanguages(), UserPreferenceUtil.getUserEdition(), it)
                    .map {
                        it.data as MultiValueResponse<CommonAsset>
                    }

            resp
        }
    }
}

class DeleteLocalCommentUsecase
@Inject constructor(@Named("postId") private val postId: String) :
        BundleUsecase<Any> {

    override fun invoke(p1: Bundle): Observable<Any> {
        val discussionId = p1.getString(Constants.BUNDLE_POST_ID)
        discussionId?.let {
            SocialDB.instance().discussionsDao().deleteDiscussion(postId, it)
            SocialDB.instance().cpDao().deleteByPostId(it)
            SocialDB.instance().localDeleteDao().insReplace(LocalDelete(it, System.currentTimeMillis()))
        }
        return Observable.just(true)
    }
}

class FetchParentNwUsecase
@Inject constructor(private val api: NewsDetailAPI,
                    @Named("postId") private val postId: String) : BundleUsecase<AllLevelCards?> {

    override fun invoke(p1: Bundle): Observable<AllLevelCards?> {

        val obs = Observable.fromCallable {
            val item = SocialDB.instance().fetchDao().getDiscussionParent(postId)
            if (item != null) {
                val follEntity = SocialDB.instance().followEntityDao().isFollowed(item.source?.id
                        ?: "")
                AllLevelCards(postEntity = item.toCard2(), isFollowin = follEntity != null)
            } else {
                null
            }
        }.onErrorResumeNext { t: Throwable ->
            Logger.d("FetchParentNwUsecase", "ReadDetailCardUsecase Error", t)
            val fromNw = api.getParentForComment(postId).lift(ApiResponseOperator()).map {
                val follEntity = SocialDB.instance().followEntityDao().isFollowed(it.data.source?.id
                        ?: "")
                AllLevelCards(postEntity = it.data.toCard2(), isFollowin = follEntity != null)
            }

            fromNw
        }

        return obs
    }
}

class DeleteCommentUsecase
@Inject constructor(private val postService: PostDeletionService,
                    private val deleteLocalCommentUsecase: DeleteLocalCommentUsecase) : BundleUsecase<Boolean> {

    override fun invoke(p1: Bundle): Observable<Boolean> {
        val commentId = p1.getString(Constants.BUNDLE_POST_ID)!!
        val itemType = p1.getString(Constants.ITEM_TYPE) ?: CreatePostUiMode.COMMENT.name
        val analyticsItemType = p1.getString(Constants.EVENT_ITEM_TYPE) ?: COMMENT_TYPE_MAIN
        val pageReferrer = p1.getSerializable(Constants.REFERRER) as PageReferrer
        val isPrimaryContent = p1.getBoolean(Constants.BUNDLE_IS_PRIMARY_CONTENT, false)
        return postService.deleteComment(commentId, itemType, SSO.getInstance().encryptedSessionData).map {
            logCommentDeleted(NhAnalyticsEventSection.NEWS, null, commentId, pageReferrer, analyticsItemType)
            deleteLocalCommentUsecase.invoke(p1).subscribe()
            isPrimaryContent
        }.doOnError {
            Logger.e(TAG, "error deleting comment", it)
            val baseError = BaseErrorBuilder.getBaseError(it, null, null, null)
            AndroidUtils.getMainThreadHandler().post {
                // TODO: 16/03/21
                FontHelper.showCustomFontToast(CommonUtils.getApplication(),
                        baseError.message, Toast.LENGTH_LONG)
            }
        }
    }


    private fun logCommentDeleted(eventSection: NhAnalyticsEventSection?,
                                  map: MutableMap<NhAnalyticsEventParam, Any>?, commentId: String,
                                  referrer: PageReferrer?, itemType: String) {
        var map = map
        if (eventSection == null || referrer == null) {
            return
        }

        if (map == null) {
            map = HashMap<NhAnalyticsEventParam, Any>()
        }

        map[NHAnalyticsSocialCommentsEventParam.ITEM_ID] = commentId
        map[NHAnalyticsSocialCommentsEventParam.ITEM_TYPE] = itemType
        map[NHAnalyticsSocialCommentsEventParam.COMMENT_ITEM_ID] = commentId
        AnalyticsClient.logDynamic(NHSocialAnalyticsEvent.COMMENT_DELETED, eventSection, map, null,
                referrer,
                false)
    }
}

class ReportCommentUsecase
@Inject constructor(private val postService: PostReportService) : BundleUsecase<String?> {

    override fun invoke(p1: Bundle): Observable<String?> {
        val commentId = p1.getString(Constants.BUNDLE_POST_ID)!!
        val pageReferrer = p1.getSerializable(Constants.REFERRER) as PageReferrer
        logCommentReported(NhAnalyticsEventSection.NEWS, null, commentId, pageReferrer)
        return postService.reportComments(commentId, CreatePostUiMode.COMMENT.name, SSO.getInstance().encryptedSessionData).map {
            SocialDB.instance().reportDao().insert(ReportEntity(entityId = commentId))
            commentId
        }
    }

    private fun logCommentReported(eventSection: NhAnalyticsEventSection?,
                                   map: MutableMap<NhAnalyticsEventParam, Any>?, commentId: String,
                                   referrer: PageReferrer?) {
        var map = map

        if (eventSection == null || referrer == null) {
            return
        }

        if (map == null) {
            map = HashMap()
        }

        map[NHAnalyticsSocialCommentsEventParam.ITEM_ID] = commentId
        map[NHAnalyticsSocialCommentsEventParam.TYPE] = REPORT_SPAM_COMMENT
        map[NhAnalyticsDialogEventParam.ACTION] = REPORT_SPAM_YES
        AnalyticsClient.logDynamic(NhAnalyticsDialogEvent.DIALOGBOX_ACTION, eventSection, map, null,
                referrer,
                false)
    }
}

fun discussionRespHandle(data: List<PostEntity>?, postId: String, fetchDao: FetchDao,
                         groupFeedDao: GeneralFeedDao, location: String, section: String,
                         postDao: PostDao, parentLevel: PostEntityLevel, uniquePostId: String,
                         shoudlReplacePostBody: Boolean) {

    val uniqueId = postId + "_discussion"
    groupFeedDao.insReplace(GeneralFeed(uniqueId, "", "GET", section))
    val fetchEntity = FetchInfoEntity(uniqueId, location, "", 0, section = section)

    val discussionRelationShip = mutableListOf<Discussions>()
    data?.mapIndexed { index, it ->
        val newpost = it.copy(level = PostEntityLevel.DISCUSSION).toCard2().copy(uniqueId = it.id)
        val ids = postDao.insIgnore(newpost)
        if (shoudlReplacePostBody && !ids.isEmpty() && ids.get(0) == -1L) {
            postDao.updatePost(newpost)
        }

        discussionRelationShip.add(Discussions(uniquePostId, it.id, index = index, level = parentLevel))
    }

    if (discussionRelationShip.isNotEmpty()) {
        SocialDB.instance().discussionsDao().insIgnore(discussionRelationShip)
    }

    data?.let {
        fetchDao.insertDiscussionsinFetchDB(fetchEntity, it)
    }
}


class ReadDetailCardUsecase
@Inject constructor(
        @Named("entityId") private val entityId: String,
        @Named("location") private val location: String,
        @Named("section") private val section: String,
        @Named("postId") private val postId: String,
        private val groupFeedDao: GeneralFeedDao,
        private val readFullPostUsecase: ReadFullPostUsecase,
        private val fetchDao: FetchDao) : BundleUsecase<Boolean> {


    override fun invoke(p1: Bundle): Observable<Boolean> {
        val postEntityLevel = p1.getString(Constants.POST_ENTITY_LEVEL)
                ?: PostEntityLevel.TOP_LEVEL.name

        val fromNw = readFullPostUsecase.invoke(p1)

        val obs = Observable.fromCallable {
            val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
                    ?: postId
            fetchDao.detailCardByUniqueIdLevel(uniqueId, postEntityLevel)
        }.map {
            Logger.d("ReadDetailCardUsecase", "ReadDetailCardUsecase" + it)
            true
        }.onErrorResumeNext { t: Throwable ->
            Logger.d("ReadDetailCardUsecase", "ReadDetailCardUsecase Error" + t)
            fromNw.map {
                true
            }
        }

        return obs

    }
}

/**
 * Used to fetch History card in video detail
 */
class ReadFullCardUsecase
@Inject constructor(
        @Named("entityId") private val entityId: String,
        @Named("location") private val location: String,
        @Named("section") private val section: String,
        @Named("postId") private val postId: String,
        private val groupFeedDao: GeneralFeedDao,
        private val readFullPostUsecase: ReadFullPostUsecase,
        private val fetchDao: FetchDao) : BundleUsecase<Boolean> {


    override fun invoke(p1: Bundle): Observable<Boolean> {
        val postEntityLevel = p1.getString(Constants.POST_ENTITY_LEVEL)
                ?: PostEntityLevel.TOP_LEVEL.name

        val fromNw = readFullPostUsecase.invoke(p1)

        val obs = Observable.fromCallable {
            fetchDao.detailCardByPostIdLevel(postId, postEntityLevel)
        }.map {
            Logger.d("ReadFullCardUsecase", "ReadFullCardUsecase" + it)
            true
        }.onErrorResumeNext { t: Throwable ->
            Logger.d("ReadFullCardUsecase", "ReadFullCardUsecase Error" + t)
            fromNw.map {
                true
            }
        }

        return obs

    }
}


class ReadDetailedFromNetworkAndUpdatePostUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("listLocation") private val listLocation: String,
                    @Named("referrerFlow") private val referrerFlow: PageReferrer?,
                    private val postDao: PostDao,
                    private val fetchDao: FetchDao,
                    private val additionalContentsDao: AdditionalContentsDao,
                    private val fetchRelatedStoriesUsecase: FetchRelatedStoriesUsecase,
                    private val api: NewsDetailAPIProxy
) :
    BundleUsecase<PostEntity> {
    override fun invoke(p1: Bundle): Observable<PostEntity> {
        val postEntityLevel =
            p1.getString(Constants.POST_ENTITY_LEVEL) ?: PostEntityLevel.TOP_LEVEL.name
        var referrerFlowName = Constants.EMPTY_STRING
        var referrerFlowId = Constants.EMPTY_STRING
        if (referrerFlow != null) {
            if (referrerFlow.referrer.referrerName != null) {
                referrerFlowName = referrerFlow.referrer.referrerName
            }

            if (referrerFlow.id != null) {
                referrerFlowId = referrerFlow.id
            }
        }

        val network: Observable<PostEntity> = NewsDetailAPIProxy.contentOfPost(
            api, postId,
            entityId, location, section, false, true, referrerFlowName, referrerFlowId, postDao
        )
            .lift(ApiResponseOperator())
            .map {
                if(it.data.content2 == null) {
                    AnalyticsHelper2.logDevCustomErrorEvent(("ReadDetailedFromNetworkAndUpdatePostUsecase from network and content2 null: ${it.data.id}"))
                }
                Logger.d("ReadDetailedFromNetworkAndUpdatePostUsecase", " network ${it}")
                handleMoreContentResponse(it.data, postEntityLevel)
                it.data
            }

        return network
    }

    private fun handleMoreContentResponse(data: PostEntity, postEntityLevel: String) {
        try {
            postDao.updatePost(data)
            val uniqueId = fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)
                ?: fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
            uniqueId?.let { uniqueIdParam ->
                data.additionalContents?.forEach {
                    it.postId = uniqueIdParam
                    it.level = PostEntityLevel.valueOf(postEntityLevel)
                }

                associationRespHandle(
                    data, postId, SocialDB.instance().fetchDao(),
                    SocialDB.instance().groupDao(), location, section, entityId, SocialDB
                        .instance().postDao(), PostEntityLevel.TOP_LEVEL, uniqueIdParam
                )

                if (data.additionalContents != null) {
                    additionalContentsDao.insIgnore(data.additionalContents!!)
                }

                var fetchRelatedStories = false
                data.additionalContents?.filter {
                    it.contentType == Constants.RELATED_NEWS
                }?.forEach {
                    if (!fetchRelatedStories) {
                        val bundle = Bundle()
                        bundle.putString(Constants.CONTENT_URL, it.content)
                        bundle.putString(Constants.REQUEST_METHOD, it.contentRequestMethod)
                        bundle.putLong(Constants.CONTENT_ID, it.id)

                        fetchRelatedStoriesUsecase.invoke(bundle).subscribe({
                            // Success
                        }, {
                            Logger.caughtException(it)
                        })
                    }

                    fetchRelatedStories = true
                }
            }
        } catch (e: Exception) {
            AnalyticsHelper2.logDevCustomErrorEvent(("handleMoreContentResponse failed with exception: ${e}"))
        }
    }

    private fun associationRespHandle(
        data: PostEntity, postId: String, fetchDao: FetchDao,
        groupFeedDao: GeneralFeedDao, location: String, section: String,
        entityId: String, postDao: PostDao, parentLevel: PostEntityLevel,
        uniquePostId: String
    ) {
        val uniqueId = postId + "_association"
        groupFeedDao.insReplace(GeneralFeed(uniqueId, "", "GET", section))

        fetchDao.fullCleanupFetch(uniqueId, location, section)

        val fetchEntity = FetchInfoEntity(uniqueId, location, "", 0, section = section)

        val associationRelationShip = mutableListOf<Associations>()
        data.associations?.mapIndexed { index, it ->
            postDao.insIgnore(
                it.copy(level = PostEntityLevel.ASSOCIATION).toCard2().copy(uniqueId = it.id)
            )
            associationRelationShip.add(
                Associations(
                    uniquePostId,
                    it.id,
                    index = index,
                    level = parentLevel
                )
            )
        }

        if (associationRelationShip.isNotEmpty()) {
            SocialDB.instance().associationsDao().insIgnore(associationRelationShip)
        }

        data.associations?.let {
            fetchDao.insertAssociationsinFetchDB(fetchEntity, it)
        }
    }
}


