/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.viewmodel

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.EitherList
import androidx.paging.PagedList
import com.newshunt.adengine.util.AdConstants
import com.newshunt.appview.R
import com.newshunt.appview.common.entity.CardsPojoPagedList
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.GroupBaseInfo
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.ReviewActionBody
import com.newshunt.dataentity.notification.FollowModel
import com.newshunt.dataentity.social.entity.CardsPayload
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.dhutil.helper.LiveSharedPreference
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.scan
import com.newshunt.dhutil.zipWith
import com.newshunt.news.helper.NewsDetailTimespentHelper
import com.newshunt.news.helper.NonLinearFeedHelper
import com.newshunt.news.model.daos.CardDao
import com.newshunt.news.model.daos.DislikeDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.CFDestroyUsecase
import com.newshunt.news.model.usecase.CleanUpFetchUsecase
import com.newshunt.news.model.usecase.CleanUpNonlinearUsecase
import com.newshunt.news.model.usecase.CurrentPageInfoUsecase
import com.newshunt.news.model.usecase.FPFetchUseCase
import com.newshunt.news.model.usecase.FPInserttoDBUsecase
import com.newshunt.news.model.usecase.InsertLanguageSelectionCard
import com.newshunt.news.model.usecase.InsertNonLinearFeedUsecase
import com.newshunt.news.model.usecase.ListDataSourceFactory
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.NLResp
import com.newshunt.news.model.usecase.NLResponseWrapper
import com.newshunt.news.model.usecase.NonLinearListFeedUsecase
import com.newshunt.news.model.usecase.disposeUsecases
import com.newshunt.news.model.usecase.onlyData
import com.newshunt.news.model.usecase.readCardPageConfig
import com.newshunt.news.model.usecase.toMediator
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Named

/**
 * Handles
 * 1, Building multiple live-data for view
 * 2. Request to load more stories as per data and scroll position.
 * 3. pull to refresh
 * 4. ads and non-linear insertion
 * 5. delegates clicks
 * @author satosh.dhanyamraju
 */
class CardsViewModel(context: Application,
                     private val entityId: String,
                     private val postId: String,
                     private val timeSpentEventId: Long,
                     private val isDetail: Boolean,
                     private val fpUsecase: MediatorUsecase<Bundle, NLResponseWrapper>, //FPUsecase,
                     private val insertFPtoDBUsecase: MediatorUsecase<Bundle, NLResponseWrapper>, //NPUsecase,
                     val npUsecase: MediatorUsecase<Bundle, NLResp>,
                     private val readCardsUsecase: MediatorUsecase<Bundle, EitherList<Any>>,
                     val cardClickDelegate: ClickDelegate,
                     private val cleanupUsecase: MediatorUsecase<Bundle, Boolean>,
                     private val currentPageInfoUsecase: CurrentPageInfoUsecase,
                     private val nonLinearListFeedUsecase: MediatorUsecase<Any, List<NLFCItem>>,
                     private val nonLinearInsertUsecase: MediatorUsecase<Bundle, Any>,
                     private val cleanUpNonlinearUsecase: MediatorUsecase<Any, Any>,
                     private val nonLinearFeedHelper: NonLinearFeedHelper,
                     private val insertOffLineCardUsecase: MediatorUsecase<Bundle, Any>,
                     private val approvalActionMediatorUC: MediatorUsecase<ReviewActionBody, Boolean>,
                     private val readNudgesUsecase: MediatorUsecase<List<CardInfo>, Map<String, CardNudge?>>,
                     private val markNudgeUsecase: MediatorUsecase<Int, Boolean>,
                     private val listType: String,
                     cardDao: CardDao,
                     private val joinGroupMediatorUC: MediatorUsecase<GroupBaseInfo, GroupInfo>,
                     dislikeDao: DislikeDao,
                     private val location: String,
                     val section: String,
                     private val cfCountTracker: CFCountTracker,
                     cfDestroyUC: CFDestroyUsecase
) : AndroidViewModel(context), ClickDelegate by cardClickDelegate {

    val mediatorCardsLiveData = MediatorLiveData<CardsPojoPagedList>()
    val nonLinearFeedLiveData = nonLinearListFeedUsecase.data()
    var pageReferrer: PageReferrer? = null
    private var cardsPojo = CardsPojoPagedList()
    private var isLiked: Boolean = false
    private var isShared: Boolean = false
    var uniqueScreenId: Int = 0
    private val extraAdCardIds = ArrayList<String>()

    val followList: LiveData<List<CardDao.Follow>> = cardDao.allFollowsAndBlocks()
    val likeList: LiveData<List<CardDao.Interaction>> = cardDao.allLikeTypes()
    val voteList: LiveData<List<CardDao.Vote>> = cardDao.allVotes()
    val readIds:LiveData<List<String>> = cardDao.allReadIds()
    val membership: LiveData<List<CardDao.MemberShip>> = cardDao.allMemberShip()
    val dislikeList: LiveData<List<String>> = dislikeDao.allIds()
    val cardsAdapterSize:MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    private val extraCardsIds = MutableLiveData<List<String>>()
    val extraCards = Transformations.switchMap(extraCardsIds){cardDao.cardsById(it)}
    var fragmentBundle: Bundle? = null
     set(value) {
         field = value
         if (cardClickDelegate is CardClickDelegate) {
             cardClickDelegate.fragmentBundle = value
         }
     }
    val npStatus = npUsecase.status() // for loading footer
    private val fpStatus2 : LiveData<Pair<Boolean?, CardsPojoPagedList?>> =
            fpUsecase.status().zipWith(mediatorCardsLiveData) {a, b -> a to b }

    val fpStatus = Transformations.map(fpStatus2) {
        Logger.d(TAG, "fpstatus: ${it.first}, ${it.second}")

        if (it.first == true && it.second?.error == null && it.second?.data?.getList().isNullOrEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val fpRequestStatus = fpUsecase.status()
    val fpData = Transformations.map(fpUsecase.data()) { it.getOrNull() }

    var lastFetchedFpResp: NLResponseWrapper? = null
    val firstpageData = Transformations.map(fpUsecase.onlyData()) {
        it?.also {
            lastFetchedFpResp = it
        }
    }

    val npData = Transformations.map(npUsecase.data()) { it.getOrNull() }

    val nsfwLiveData = LiveSharedPreference.pref(GenericAppStatePreference.SHOW_NSFW_FILTER, context, true)

    val cards: LiveData<CardsPojoPagedList> = readCardsUsecase.data().scan(CardsPojoPagedList()) { acc, t ->
        if (t.isSuccess) {
            // view wants stories; not posts
            acc.copy(data = t.getOrNull(), tsData = System.currentTimeMillis())

        } else {
            acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
        }
    }
    val approvalLiveData by lazy {
        approvalActionMediatorUC.data()
    }

    val joinGroupUseCaseLD by lazy {
        joinGroupMediatorUC.data()
    }
    val currentPageInfoLiveData = currentPageInfoUsecase.data()
    var enableNpUsecase = true
    var started = false

    val nudges: LiveData<Map<String, CardNudge?>?> = readNudgesUsecase.onlyData().distinctUntilChanged()
    val cfDestroyUsecase = cfDestroyUC.toMediator2()
    var callCFDestroy = false
    init {
        if (!isDetail) {
            readCardsUsecase.execute(Bundle())
        }
        // for swiperfresh
        mediatorCardsLiveData.addSource(cards) {
            try{
                cardsPojo = cardsPojo.copy(data = it.data, tsData = it.tsData)
                mediatorCardsLiveData.value = cardsPojo
                CommonUtils.runInBackground(object :Runnable{
                    override fun run() {
                        Logger.v(TAG, "updating from DB")
                        val c = getUnblockedItemsCountFromDb()
                        val size = cardsPojo.data?.getList()?.size?:0
                        if(c > 0 && size == 0){
                            val msg = CommonUtils.getString(R.string.unblock_source)?:"Please unblock some sources for content to load"
                            val error = BaseErrorBuilder.getBaseError(msg, Constants.ERROR_NO_FEED_ITEMS_BECAUSE_OF_BLOCKED_SOURCES)
                            cardsPojo = cardsPojo.copy(data = it.data, tsData = it.tsData, error = error, tsError = System.currentTimeMillis())
                        }else{
                            cardsPojo = cardsPojo.copy(data = it.data, tsData = it.tsData)
                        }
                        mediatorCardsLiveData.postValue(cardsPojo)
                    }

                })
            }catch(ex: Exception){
                Logger.e(TAG, ex.message)
            }
        }
        mediatorCardsLiveData.addSource(fpUsecase.data()) {
            if (it.isFailure) {
                cardsPojo = cardsPojo.copy(error = it.exceptionOrNull(), tsError = System.currentTimeMillis())
                mediatorCardsLiveData.value = cardsPojo
            } else if (ENABLE_IN_MEMORY_FP_INSERTION && listType != Format.ENTITY.name) {
                it.getOrNull()?.nlResp?.rows?.let { cardsInResponse ->
                    if (cardsPojo.data?.getList().isNullOrEmpty()) {
                        Logger.d(TAG, "inserting from memory")
                        val memoryDataSource = ListDataSourceFactory<Any>(cardsInResponse).create()
                        val pageListBuilder=PagedList.Builder(memoryDataSource, readCardPageConfig())
                        pageListBuilder.setNotifyExecutor(Executors.newSingleThreadExecutor())
                        pageListBuilder.setFetchExecutor(Executors.newSingleThreadExecutor())
                        cardsPojo = cardsPojo.copy(data = EitherList(pagedList = pageListBuilder.build()), tsData = System
                                .currentTimeMillis())
                        mediatorCardsLiveData.value = cardsPojo
                    }
                }
            }
        }
        mediatorCardsLiveData.addSource(npUsecase.data()) {
            if (it.isFailure) {
                cardsPojo = cardsPojo.copy(error = it.exceptionOrNull(), tsError = System.currentTimeMillis())
                mediatorCardsLiveData.value = (cardsPojo)
            }
        }

        nonLinearListFeedUsecase.execute(Any())
        setIsDetail()
    }

    fun start() {
        if (started.not() || cards.value?.data == null) {
            started = true
            fpUsecase.execute(createBundleForFp())
            currentPageInfoUsecase.execute(Unit)
        }
    }

    fun pullToRefresh() {
        if (lastFetchedFpResp != null) {
            replaceFP()
            return
        }
        fpUsecase.execute(createBundleForFp(true))
    }

    fun updateCurrentCardLocation(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int) {
        /*usecase would also prevent concurrent req. This is additional check*/
        if (npStatus.value == true || !enableNpUsecase) return
        Logger.v(TAG, "$entityId: nVis=$visibleItemCount, 1st=$firstVisibleItem, nTot=$totalItemCount")
        if (totalItemCount > 0) {
            if (totalItemCount - visibleItemCount <= firstVisibleItem + PREFETCH_ITEM_THRESHOLD) {
                npUsecase.execute(Bundle())
            }
        }
    }

    override fun onEmojiClick(view: View, item: Any, parentItem: Any?, likeType: LikeType, isComment: Boolean?, commentType: String?) {
        cardClickDelegate.onEmojiClick(view, item, parentItem, likeType, isComment, commentType)

        if (!isLiked && isDetail) {
            isLiked = true
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                    Constants.IS_LIKED, java.lang.Boolean.toString(isLiked))
        }
    }


    override fun onShareClick(view: View, item: Any, args: Bundle?) {
        super.onShareClick(view, item, args)
        cardClickDelegate.onShareClick(view, item,args)
        if (!isShared && isDetail) {
            isShared = true
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                    Constants.IS_SHARED, java.lang.Boolean.toString(isShared))
        }
    }

    override fun isDetail(): Boolean {
        return isDetail
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared $entityId, $location, $section, hc=${hashCode()}, detail=$isDetail")
        if((!isDetail && !location.contains(Constants.FETCH_LOCATION_DETAIL)) || callCFDestroy) cfDestroyUsecase.execute(Bundle.EMPTY)
        val c = cfCountTracker.count(entityId, location, section) ?: 0
        if (!isDetail && c == 1/*VM.onCleared() is called before fragment.onDestroy().
        So this 1 is in the process of becoming 0 when this function completes and call goes to fragment. */) {
            cleanupUsecase.execute(bundleOf())
        }
        cardClickDelegate.dispose()
        disposeUsecases(fpUsecase, insertFPtoDBUsecase, npUsecase, readCardsUsecase,
                currentPageInfoUsecase, nonLinearListFeedUsecase, nonLinearInsertUsecase,
                cleanUpNonlinearUsecase, insertOffLineCardUsecase, approvalActionMediatorUC)
        super.onCleared()
    }

    fun insertNonLinear(nlfcItem: NLFCItem, id: String) {
        nonLinearInsertUsecase.execute(bundleOf(InsertNonLinearFeedUsecase.BUNDLE_NLFC_ITEM to nlfcItem,
                InsertNonLinearFeedUsecase.BUNDLE_PREV_POST_ID to id))
    }

    fun insertNonLinearAt(nlfcItem: NLFCItem, id: String, position: Int, url: String?) {
        nonLinearInsertUsecase.execute(bundleOf(InsertNonLinearFeedUsecase.BUNDLE_NLFC_ITEM to nlfcItem,
                InsertNonLinearFeedUsecase.BUNDLE_PREV_POST_ID to id,
                InsertNonLinearFeedUsecase.BUNDLE_FORCE_POSITION to position,
                InsertNonLinearFeedUsecase.BUNDLE_URL to url))
    }

    fun insertLanguageSelectionCard(offLineCard: PostEntity, prevPostId: String? = null) {
        val args = Bundle()
        args.putSerializable(InsertLanguageSelectionCard.BUNDLE_LANGUAGE_SELECTION_ITEM, offLineCard)
        args.putSerializable(InsertLanguageSelectionCard.BUNDLE_PREV_POST_ID, prevPostId)
        insertOffLineCardUsecase.execute(args)
    }

    fun cleanUpNonLinear() {
        cleanUpNonlinearUsecase.execute(Any())
    }

    fun getNonLinearFeedCard(url: String, id: String) {
        nonLinearFeedHelper.getNLFC(url, id)
    }
    fun getNonLinearFeedCardForExplicitSignals(url: String, id: String,recommenedFollowBlcorequest:CardsPayload.FollowBlockRequest?) {
        nonLinearFeedHelper.getNLFC(url, id,recommenedFollowBlcorequest)
    }

    fun replaceFP() {
        lastFetchedFpResp?.let {
            Logger.d(TAG, "replacing ")
            insertFPtoDBUsecase.execute(bundleOf(FPInserttoDBUsecase.B_RESP to lastFetchedFpResp))
            lastFetchedFpResp = null
            CommonUtils.runInBackground(object :Runnable {
                override fun run() {
                    SocialDB.instance().nccImpressionDao().deleteSyncedImpressionData()
                }

            })
        }
    }

    fun getUnblockedItemsCountFromDb(): Int{
        return SocialDB.instance().fetchDao().itemsMatchingUnblockedCount(entityId, location, section)
    }

    fun getPostEntityById(postID: String): PostEntity? {
        return SocialDB.instance().fetchDao().lookupById(postID)
    }

    class Factory @Inject constructor(private val app: Application,
                                      @Named("entityId") val entityId: String,
                                      @Named("postId") val postId: String,
                                      @Named("timeSpentEventId")
                                      private val timeSpentEventId: Long,
                                      @Named("isDetail") val isDetail: Boolean,
                                      @Named("fpUsecase")
                                      private val fpUsecase: MediatorUsecase<Bundle, NLResponseWrapper>,
                                      @Named("npUsecase")
                                      private val npUsecase: BundleUsecase<NLResp>,
                                      @Named("readUsecase")
                                      private val readCardsUsecase: MediatorUsecase<Bundle, EitherList<Any>>,
                                      private val cardClickDelegate: CardClickDelegate.Factory,
                                      val cleanupUsecase: CleanUpFetchUsecase,
                                      @Named("currentPageInfoUsecase")
                                      private val currentPageInfoUsecase: CurrentPageInfoUsecase,
                                      private val nonLinearListFeedUsecase: NonLinearListFeedUsecase,
                                      private val insertNonLinearUsecase: InsertNonLinearFeedUsecase,
                                      private val cleanUpNonlinearUsecase: CleanUpNonlinearUsecase,
                                      private val nonLinearFeedHelper: NonLinearFeedHelper,
                                      @Named("FPInserttoDBUsecase")
                                      private val FPInserttoDBUsecase: BundleUsecase<NLResponseWrapper>,
                                      private val insertLanguageSelectionCardUsecase: InsertLanguageSelectionCard,
                                      @Named("approvalActionMediatorUC")
                                      private val approvalActionMediatorUC: MediatorUsecase<ReviewActionBody, Boolean>,
                                      @Named("listType")
                                      private val listType: String,
                                      private val cardDao: CardDao,
                                      @Named("joinGroupMediatorUC")
                                      private val joinGroupMediatorUC:
                                      MediatorUsecase<GroupBaseInfo, GroupInfo>,
                                      private val dislikeDao: DislikeDao,
                                      @Named("location")
                                      private val location: String,
                                      @Named("section")
                                      private val section: String,
                                      private val cfCountTracker: CFCountTracker,
                                      @Named("readNudgesUc")
                                      private val readNudgesUsecase: MediatorUsecase<List<CardInfo>, Map<String, CardNudge?>>,
                                      @Named("markNudgeUc")
                                      private val markNudgeUsecase: MediatorUsecase<Int, Boolean>,
                                      private val cfDestroyUC : CFDestroyUsecase
    ) : ViewModelProvider
    .AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CardsViewModel(
                    app,
                    entityId,
                    postId,
                    timeSpentEventId,
                    isDetail,
                    fpUsecase,
                    FPInserttoDBUsecase.toMediator2(),
                    npUsecase.toMediator2(true),
                    readCardsUsecase,
                    cardClickDelegate.get(),
                    cleanupUsecase.toMediator(),
                    currentPageInfoUsecase,
                    nonLinearListFeedUsecase,
                    insertNonLinearUsecase.toMediator2(),
                    cleanUpNonlinearUsecase.toMediator2(),
                    nonLinearFeedHelper,
                    insertLanguageSelectionCardUsecase.toMediator2(),
                    approvalActionMediatorUC,
                    readNudgesUsecase,
                    markNudgeUsecase,
                    listType,
                    cardDao,
                    joinGroupMediatorUC,
                    dislikeDao,
                    location,
                    section,
                    cfCountTracker,
                    cfDestroyUC
            ) as T
        }
    }

    companion object {
        private const val PREFETCH_ITEM_THRESHOLD = 3
        private const val TAG = "CardsViewModel"
        /**
         * This is an optimization to prevent displaying temporary blank screen( or error screen)
         * which is shown for duration of time between writing response to DB and getting query output.
         */
        private val ENABLE_IN_MEMORY_FP_INSERTION = false
    }

    fun getFollowModel(): FollowModel? {
        val model = fragmentBundle?.getString(Constants.BUNDLE_FOLLOW_MODEL)
        if (model == null) {
            return null
        } else {
            return FollowModel.valueOf(model)
        }
    }

    fun setCurrentPageReferrer(referrer: PageReferrer?, referrerFlow:PageReferrer?,
                               referrerProviderListner: ReferrerProviderlistener?) {
        if (cardClickDelegate is CardClickDelegate) {
            cardClickDelegate.referrer = referrer
            cardClickDelegate.referrerProviderListener = referrerProviderListner
            cardClickDelegate.referrerFlow = referrerFlow
            cardClickDelegate.fragmentBundle = fragmentBundle
        }

        this.pageReferrer = referrer
    }

    fun setIsDetail() {
        if (cardClickDelegate is CardClickDelegate) {
            cardClickDelegate.isInDetail = isDetail
        }
    }

    fun setUniqueId(uniqueIdentifierId: Int) {
        if (cardClickDelegate is CardClickDelegate) {
            cardClickDelegate.uniqueId = uniqueIdentifierId
            cardClickDelegate.fragmentBundle = fragmentBundle
        }
    }

    fun updateNLFCAsset(commonAsset: CommonAsset?, position: Int, landingStoryId: String? = null) {
        nonLinearFeedHelper.asset = commonAsset?.rootPostEntity()
        nonLinearFeedHelper.position = position
        nonLinearFeedHelper.parentId = landingStoryId ?: commonAsset?.i_id()
    }

    fun fetchNudges(currentList: List<Any?>?) {
        val list = currentList?:return
        val cardInfoList = list.mapNotNull {
            it as? CommonAsset
        }.mapNotNull {
            val format = it.i_format()
            val subFormat = it.i_subFormat()
            val uiType2 = it.i_uiType()
            if(format!= null && subFormat !=null && uiType2 != null)
            CardInfo(it.i_id(), it.i_level(), format, subFormat, uiType2, it.i_commentRepostItems()?.isNotEmpty() == true)
            else null
        }
        readNudgesUsecase.execute(cardInfoList)
    }

    fun markNudgeShown(cardNudge: CardNudge) {
        markNudgeUsecase.execute(cardNudge.id)
    }

    private fun createBundleForFp(isPullToRefresh: Boolean = false): Bundle {
        val cacheDelay = PreferenceManager.getPreference(AppStatePreference.PREF_FEED_CACHE_DELAY,
                Constants.PREF_FEED_CACHE_DELAY_DEFAULT)
        return if (isPullToRefresh) {
            bundleOf(
                    NewsConstants.FEED_CACHE_DELAY to cacheDelay,
                    FPFetchUseCase.B_PULL_TO_RFRSH to true,
                    Constants.REQUEST_WITH_CACHE to false
            )
        } else {
            bundleOf( NewsConstants.FEED_CACHE_DELAY to cacheDelay)
        }
    }

    fun addExtraAdId(id: String?) {
        id ?: return
        if (extraAdCardIds.contains(id)) return
        extraAdCardIds.add(AdConstants.AD_PROXY_FETCH_ID.plus(Constants.UNDERSCORE_CHARACTER).plus(id))
        extraCardsIds.value = extraAdCardIds
    }
}