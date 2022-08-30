package com.newshunt.appview.common.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.adengine.ClearAdsDataUsecase
import com.newshunt.appview.common.profile.model.usecase.QueryHistoryUsecase
import com.newshunt.appview.common.ui.fragment.FullPagePojo
import com.newshunt.appview.common.ui.fragment.InsertHistoryPosts
import com.newshunt.appview.common.ui.fragment.InsertNotificationPosts
import com.newshunt.appview.common.ui.fragment.ReadFirstCardUsecase
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.DetailListCard
import com.newshunt.dataentity.common.asset.NLFCItem
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.dataentity.news.model.entity.server.asset.PlaceHolderAsset
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.scan
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.InsertNonLinearFeedUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.NonLinearFeedUsecase
import com.newshunt.news.model.usecase.ReadFullPostUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants
import javax.inject.Inject
import javax.inject.Named

class DetailLandingItemViewModel(context: Application,
                                 private val postId: String,
                                 private val entityId: String,
                                 private val location: String,
                                 private val section: String,
                                 private val postEntityLevel: String,
                                 private val readFirstCardUsecase: MediatorUsecase<Bundle, Boolean>,
                                 private val readFullPostUsecase: MediatorUsecase<Bundle, PostEntity?>,
                                 private val nlfcUsecase: MediatorUsecase<Any, NLFCItem?>,
								 private val nlfcInsertUseCase: MediatorUsecase<Bundle, Any>,
								 private val insertNotificationPostsUsecase: MediatorUsecase<Bundle, Boolean>,
								 private val insertHistoryPostsUsecase: MediatorUsecase<Bundle, Boolean>,
                                 private val queryHistoryMediatorUC: MediatorUsecase<Long, List<HistoryEntity>>,
								 private val clearAdsDataUsecase: MediatorUsecase<Bundle, Unit>) : AndroidViewModel(context) {

	init {
		nlfcUsecase.execute(Any())
	}

	fun fetchFirstCard(contentURL: String?, useAlternativeContentUrlIfAvailable: Boolean) {
		readFirstCardUsecase.execute(bundleOf(
				Constants.BUNDLE_CONTENT_URL to contentURL,
				Constants.BUNDLE_CONTENT_URL_OPTIONAL to useAlternativeContentUrlIfAvailable))
	}

	val firstCard: LiveData<Result0<Boolean>> = readFirstCardUsecase.data()

	val fullPost: LiveData<FullPagePojo> = readFullPostUsecase.data().scan(FullPagePojo()) { acc, t ->
		if (t.isSuccess) {
			acc.copy(data = t.getOrNull(), tsData = System.currentTimeMillis(), error = null, tsError = null)
		} else {
			acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
		}
	}

	val nlfcLiveData: LiveData<Result0<NLFCItem?>> = nlfcUsecase.data()

	val historyLiveData = queryHistoryMediatorUC.data()

	fun fetchFullPost(postId: String, contentURL: String?) {
		readFullPostUsecase.execute(bundleOf(Constants.BUNDLE_POST_ID to postId, Constants.BUNDLE_CONTENT_URL to
			contentURL))
	}

	fun insertNonLinearCard(nlfcItem: NLFCItem, prevPostId: String) {
		nlfcInsertUseCase.execute(bundleOf(InsertNonLinearFeedUsecase.BUNDLE_NLFC_ITEM to nlfcItem,
			InsertNonLinearFeedUsecase.BUNDLE_PREV_POST_ID to prevPostId))
	}

	var detailLists: LiveData<List<DetailListCard>> =
		SocialDB.instance().fetchDao().detailList(entityId, location, section)

	fun queryHistory(fromTime: Long) {
		queryHistoryMediatorUC.execute(fromTime)
	}

	fun prepareNotificationStories(storyList : ArrayList<PlaceHolderAsset>) {
		insertNotificationPostsUsecase.execute(bundleOf(NewsConstants.STORIES_EXTRA to CommonUtils.bigBundlePut(storyList)))
	}

	fun prepareHistoryStories(sinceTime : Long) {
		insertHistoryPostsUsecase.execute(bundleOf(Constants.BUNDLE_HISTORY_SINCE_TIME to sinceTime))
	}

	override fun onCleared() {
		clearAdsDataUsecase.execute(bundleOf(ClearAdsDataUsecase.B_IS_DETAIL_PAGE to true))
		super.onCleared()
	}

	class Factory @Inject constructor(private val app: Application,
									  @Named("postId") val postId: String,
									  @Named("entityId") private val entityId: String,
									  @Named("location") private val location: String,
									  @Named("section") private val section: String,
									  @Named("postEntityLevel") private val postEntityLevel: String,
									  private val readCardsUsecase: ReadFirstCardUsecase,
									  private val insertNotificationPostsUsecase: InsertNotificationPosts,
									  private val insertHistoryPostsUsecase: InsertHistoryPosts,
									  private val readFullPostUsecase: ReadFullPostUsecase,
									  private val nlfcUsecase: NonLinearFeedUsecase,
									  private val insertNonLinearFeedUsecase: InsertNonLinearFeedUsecase,
									  private val queryHistoryUsecase: QueryHistoryUsecase,
									  private val clearAdsDataUsecase: ClearAdsDataUsecase) :
		ViewModelProvider.AndroidViewModelFactory(app) {
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			return DetailLandingItemViewModel(
					app,
					postId, entityId, location, section, postEntityLevel,
					readCardsUsecase.toMediator2(true),
					readFullPostUsecase.toMediator2(true),
					nlfcUsecase,
					insertNonLinearFeedUsecase.toMediator2(),
					insertNotificationPostsUsecase.toMediator2(),
					insertHistoryPostsUsecase.toMediator2(),
					queryHistoryUsecase,
					clearAdsDataUsecase.toMediator2()) as T
		}
	}
}