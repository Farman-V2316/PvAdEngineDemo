/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.gson.reflect.TypeToken
import com.newshunt.appview.R
import com.newshunt.common.helper.cachedapi.CacheApiKeyBuilder
import com.newshunt.common.helper.cachedapi.CachedApiHandler
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.cachedapi.CacheType
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiEntity
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.news.model.apis.NewsDetailAPI
import com.newshunt.news.model.apis.NewsDetailAPIProxy
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.view.PrefetchAdRequestCallback
import com.newshunt.sdk.network.Priority
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * Placeholder fragment that shows progress while the post is downloaded in the background. This
 * works as temp holder before inflating action fragment.
 *
 * This is used by notification flow, where only information available is postid.
 *
 * Created by karthik.r on 2020-03-25.
 */
class PlaceholderFragment : BaseSupportFragment() {

    @Inject
    lateinit var placeholderViewModelF: PlaceholderViewModel.Factory

    private lateinit var vm: PlaceholderViewModel
    var prefetchAdRequestCallback: PrefetchAdRequestCallback? = null
    private lateinit var placeHolder: FrameLayout
    private var errorMessageBuilder: ErrorMessageBuilder? = null
    private lateinit var progressBar: View
    private lateinit var errorParent: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.placeholder_fragment, container, false)
        val postId = arguments?.getString(Constants.STORY_ID)
        placeHolder = view.findViewById(R.id.placeholder)
        progressBar = view.findViewById(R.id.progressbar)
        errorParent = view.findViewById(R.id.error_parent)
        val section = arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section
        var referrerLead = arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
        if (referrerLead == null) {
            referrerLead = PageReferrer()
        }

        val referrerFlow = PageReferrer(referrerLead)
        referrerLead.referrerAction = NhAnalyticsUserAction.CLICK

        if (postId != null) {
            DaggerPlaceholderComponent.builder().placeholderModule(PlaceholderModule(
                    CommonUtils.getApplication(),
                    SocialDB.instance(),
                    arguments?.getString(Constants.PAGE_ID) ?: postId + System.currentTimeMillis().toString(),
                    postId,
                    Constants.FETCH_LOCATION_DETAIL,
                    section = section
            )).build().inject(this)
        } else {
            // TODO : Show non-recoverable error screen and return
            return view
        }

        vm = ViewModelProviders.of(this, placeholderViewModelF)[PlaceholderViewModel::class.java]
        vm.readAndReplaceFullPostUsecase.data().observe(viewLifecycleOwner, Observer {
           val data = it.getOrNull()
            if (data != null) {
                if(data.content2 == null) {
                    Logger.d("PlaceholderFragment", "readAndReplaceFullPostUsecase  from network ")
                    AnalyticsHelper2.logDevCustomErrorEvent(("ReadAndReplaceFullPostUsecase from network and content2 null: ${data.id}"))
                }
                vm.readAndReplaceFullPostUsecase.data().removeObservers(viewLifecycleOwner)
            }
            else if (it.exceptionOrNull() != null) {
                val error = it.exceptionOrNull()
                if (error is BaseError) {
                    showError(error)
                }
                else {
                    val baseError = BaseErrorBuilder.getBaseError(error, null, null, null)
                    showError(baseError)
                }
            }
        })

        vm.readAndReplaceFullPostUsecase.execute(Bundle.EMPTY)
        return view
    }

    private fun showError(throwable: Throwable?) {
        progressBar.visibility = View.GONE
        val contextParam = context
        if (throwable is BaseError && contextParam != null) {
            errorParent.visibility = View.VISIBLE
            errorMessageBuilder = ErrorMessageBuilder(errorParent, contextParam,
                    object : ErrorMessageBuilder.ErrorMessageClickedListener {
                override fun onRetryClicked(view: View?) {
                    vm.readAndReplaceFullPostUsecase.execute(Bundle.EMPTY)
                }

                override fun onNoContentClicked(view: View?) {
                    launchNewsHome()
                }
            })
            errorMessageBuilder!!.showError(throwable)
        }
    }

    private fun launchNewsHome() {
        val prevNewsAppSection = AppSectionsProvider.getAnyUserAppSectionOfType(AppSection.NEWS)
        prevNewsAppSection ?: return
        handleBackPress()
        CommonNavigator.launchNewsHome(activity, false, prevNewsAppSection.id, prevNewsAppSection.appSectionEntityKey)
    }
}

/**
 * Usecase that will fetch the entire post and replace the placeholder item in DB.
 */
class ReadAndReplaceFullPostUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    @Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    private val api: NewsDetailAPI,
                    private val fetchDao: FetchDao,
                    private val postDao : PostDao) : BundleUsecase<PostEntity> {

    override fun invoke(p1: Bundle): Observable<PostEntity> {
        val cache: Observable<PostEntity> = Observable.fromCallable {
            val cacheApiKeyBuilder = CacheApiKeyBuilder()
            cacheApiKeyBuilder.addParam("class", "newsDetailAPI")
            cacheApiKeyBuilder.addParam("storyId", postId)

            val cachedApiEntity = CachedApiEntity()
            cachedApiEntity.key = cacheApiKeyBuilder.build()
            cachedApiEntity.cacheType = CacheType.USE_NETWORK_IF_NO_CACHE

            val type = object : TypeToken<ApiResponse<PostEntity>>() {}.type
            val cachedApiHandler =
                    CachedApiHandler<ApiResponse<PostEntity>>(cachedApiEntity, null, type)
            val apiResp: ApiResponse<PostEntity>? = cachedApiHandler.dataFromCache
            if (apiResp != null) {
                val fetchInfoId = fetchDao.fetchInfo(entityId, location, section)?.fetchInfoId
                apiResp.data.id = postId
                val card = apiResp.data.toCard2(fetchInfoId.toString(), idParam = postId)
                fetchDao.updatePosts(listOf(card))
                fetchDao.updateFetchDataFormat(card.uniqueId, card.format)
            }

            apiResp?.data
        }

        val network: Observable<PostEntity> = NewsDetailAPIProxy.contentOfPost(api, postId,
                entityId, location, section, false, true, postDao = postDao)
                .map {
            ApiResponseUtils.throwErrorIfResponseNull(it)
            val fetchInfoId = fetchDao.fetchInfo(entityId, location, section)?.fetchInfoId
            it.body().data.id = postId
            val card = it.body().data.toCard2(fetchInfoId.toString(), idParam = postId)
            fetchDao.updatePosts(listOf(card))
            fetchDao.updateFetchDataFormat(card.uniqueId, card.format)
            it.body().data
        }.onErrorReturn {
            throw it
        }

        return cache.onErrorResumeNext(network) //Observable.merge(cache, network)
    }
}

class PlaceholderViewModel(val context: Application,
                           val readAndReplaceFullPostUsecase: MediatorUsecase<Bundle, PostEntity>)
    : AndroidViewModel(context) {

    class Factory @Inject constructor(private val app: Application,
                                      private val readAndReplaceFullPostUsecase: ReadAndReplaceFullPostUsecase) :
            ViewModelProvider.AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlaceholderViewModel(app,
                    readAndReplaceFullPostUsecase.toMediator2(true)) as T
        }
    }
}

@Component(modules = [PlaceholderModule::class])
interface PlaceholderComponent {
    fun inject(placeholderFragment: PlaceholderFragment)
}

@Module
class PlaceholderModule(private val app: Application, // should be inherited
                        private val socialDB: SocialDB,  // should be inherited
                        private val entityId: String,
                        private val postId: String,
                        private val location: String,
                        private val section: String) {
    @Provides
    fun app() = app

    @Provides
    @Named("postId")
    fun postId(): String = postId

    @Provides
    @Named("entityId")
    fun entityId(): String = entityId

    @Provides
    @Named("location")
    fun loc(): String = location

    @Provides
    @Named("section")
    fun section() = section

    @Provides
    fun fetchDao() = socialDB.fetchDao()

    @Provides
    fun postDao() = socialDB.postDao()

    @Provides
    fun detailAPI() = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(CommonUtils
            .formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
            Priority.PRIORITY_HIGHEST, "").create(NewsDetailAPI::class.java)

}