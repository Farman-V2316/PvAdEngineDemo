package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dataentity.social.entity.RelatedList
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.apis.NewsDetailAPI
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.GeneralFeedDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.daos.RelatedListDao
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

class FetchRelatedStoriesUsecase
@Inject constructor(

	@Named("entityId") private val entityId: String,
	@Named("location") private val location: String,
	@Named("section") private val section: String,
	@Named("postId") private val postId: String,
	@Named("listLocation") private val listLocation: String,
	private val groupFeedDao: GeneralFeedDao,
	private val fetchDao: FetchDao,
	@Named("normalPriorityDetailAPI") private val normalPriorityDetailAPI: NewsDetailAPI,
	private val relatedListDao: RelatedListDao,
	private val postDao: PostDao,
	@Named("buildPayloadUsecase")
	private val buildPayloadUsecase: BundleUsecase<Any>) :
	BundleUsecase<MultiValueResponse<CommonAsset>> {

	override fun invoke(p1: Bundle): Observable<MultiValueResponse<CommonAsset>> {
		val path = p1.getString(Constants.CONTENT_URL)?.replace("{postId}", postId)
		val requestMethod = p1.getString(Constants.REQUEST_METHOD) ?: Constants.HTTP_GET
		val contentId = p1.getLong(Constants.CONTENT_ID)

		val apiRequest: Observable<ApiResponse<MultiValueResponse<PostEntity>>> =
			if (Constants.HTTP_POST.equals(requestMethod, ignoreCase = true)) {
				normalPriorityDetailAPI.postRelatedStories(path,
					buildPayloadUsecase.invoke(p1).blockingSingle(),
					UserPreferenceUtil.getUserLanguages(),
						UserPreferenceUtil.getUserNavigationLanguage(),
					UserPreferenceUtil.getUserEdition()).lift(ApiResponseOperator())
			} else {
				normalPriorityDetailAPI.getRelatedStories(path,
					UserPreferenceUtil.getUserLanguages(),
						UserPreferenceUtil.getUserNavigationLanguage(),
					UserPreferenceUtil.getUserEdition()).lift(ApiResponseOperator())
			}

		return apiRequest.map {

			var locationUsed = location
			var postUniqueId = fetchDao.getUniqueIdFromFetch(entityId, location, section, postId)
			if (postUniqueId == null) {
				postUniqueId =  fetchDao.getUniqueIdFromFetch(entityId, listLocation, section, postId)
				locationUsed = listLocation
			}
			postUniqueId?.let { id ->
				if (it.data.rows.isNotEmpty()) {
					relatedListDao.deleteForPostId(id)
				}
			}
			val uniqueId = postId + "_related"
			groupFeedDao.insReplace(GeneralFeed(uniqueId, "", "GET",section))
			val fetchEntity = FetchInfoEntity(uniqueId, locationUsed, "", 0, section = section)
			fetchDao.insIgnore(fetchEntity)
			fetchDao.insertStoriesinFetchDB(fetchEntity, it.data.rows)

			it.data.rows.forEach { post ->
				//  relatedListDao.insReplace(RelatedList(postId, it.id))
				postDao.insIgnore(post.copy(level = PostEntityLevel.RELATED_STORIES).toCard2().copy(uniqueId = post.id))
			}
			val fetchId = fetchDao.fetchInfo(entityId, location, section = section)?.fetchInfoId ?: 0L
			postUniqueId?.let { postUniqueId ->
				relatedListDao.insIgnore(it.data.rows.mapIndexed { index, postEntity ->
					RelatedList(postUniqueId, fetchId, index, contentId, postEntity.id)
				})
			}

			it.data
		}.map {
			it as MultiValueResponse<CommonAsset>
		}
	}
}