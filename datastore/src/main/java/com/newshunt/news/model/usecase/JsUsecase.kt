package com.newshunt.news.model.usecase

import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.JsFollowAndDislikesRequest
import com.newshunt.dataentity.common.JsFollowAndDislikesResponse
import com.newshunt.dataentity.common.JsResponse
import com.newshunt.dataentity.common.JsResponseLikesDislikes
import com.newshunt.news.model.sqlite.SocialDB
import io.reactivex.Observable

class IsFollowedUsecase: Usecase<String, JsResponse> {
	override fun invoke(p1: String): Observable<JsResponse> {
		return Observable.fromCallable {
			val isFollowed = SocialDB.instance().followEntityDao().isFollowed(p1) != null
			JsResponse(p1, isFollowed)
		}
	}
}

class JsFollowAndDislikeUsecase : Usecase<String, JsFollowAndDislikesResponse?> {
	private val LOG_TAG = "JsFollowAndDislikeUsecase"
	override fun invoke(p1: String): Observable<JsFollowAndDislikesResponse?> {
		return Observable.fromCallable {
			val request = JsonUtils.fromJson(p1, JsFollowAndDislikesRequest::class.java)
			if (request == null) {
				null
			} else {
				Logger.d(LOG_TAG, "running $request")
				val jsFollowList = request.follows?.let {
					val followList = SocialDB.instance().followEntityDao().getFollowsFromList(it)
					it.map { id -> JsResponse(id, followList?.contains(id)?:false) }
				}
				val jsDislikeList = request.posts?.let {
					val dislikeList = SocialDB.instance().dislikeDao().getAllDislikedFrom(it)
					val likeList = SocialDB.instance().interactionsDao().likes(it)
					it.map { id ->
						JsResponseLikesDislikes(id, dislikeList.contains(id),
								likeList.find { it.entityId == id }?.action ?: "")
					}
				}
				JsFollowAndDislikesResponse(follows = jsFollowList, posts = jsDislikeList)
			}
		}
	}
}

class IsDislikeUsecase: Usecase<String, JsResponse> {
	override fun invoke(p1: String): Observable<JsResponse> {
		return Observable.fromCallable {
			val isDisliked = SocialDB.instance().dislikeDao().isDisliked(p1) != null
			JsResponse(p1, isDisliked)
		}
	}
}