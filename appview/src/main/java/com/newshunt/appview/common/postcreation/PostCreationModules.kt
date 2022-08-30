package com.newshunt.appview.common.postcreation

import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.newshunt.appview.common.postcreation.model.usecase.PostCurrentCityUseCase
import com.newshunt.appview.common.postcreation.model.usecase.PostCurrentPlacesUseCase
import com.newshunt.appview.common.postcreation.view.activity.CreatePostActivity
import com.newshunt.appview.common.postcreation.view.activity.PostLocationActivity
import com.newshunt.appview.common.postcreation.view.service.UploadJobService
import com.newshunt.appview.common.postcreation.viewmodel.CreatePostViewModel
import com.newshunt.appview.common.postcreation.viewmodel.HashTagViewModel
import com.newshunt.appview.common.postcreation.viewmodel.PostAutoCompleteLocationVM
import com.newshunt.appview.common.postcreation.viewmodel.PostCreationViewModelFactory
import com.newshunt.appview.common.postcreation.viewmodel.PostCurrentPlaceVM
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.di.SearchModule
import com.newshunt.news.di.scopes.PerActivity
import com.newshunt.news.model.apis.ImageUploadService
import com.newshunt.news.model.apis.PostCreationService
import com.newshunt.news.model.apis.PostDeletionService
import com.newshunt.news.model.apis.PostReportService
import com.newshunt.news.model.daos.CreatePostDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.ImageDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.CpCreationUseCase
import com.newshunt.news.model.usecase.CpImageInsertUseCase
import com.newshunt.news.model.usecase.CpImageReadUseCase
import com.newshunt.news.model.usecase.CpLocationInsertUseCase
import com.newshunt.news.model.usecase.CpPollInsertUseCase
import com.newshunt.news.model.usecase.CpPrivacyUseCase
import com.newshunt.news.model.usecase.CpReadUseCase
import com.newshunt.news.model.usecase.CpRepostInsertUseCase
import com.newshunt.news.model.usecase.OEmbedAPI
import com.newshunt.news.model.usecase.OEmbedUsecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.view.fragment.FetchParentUsecase
import com.newshunt.news.view.fragment.PhotosModule
import com.newshunt.sdk.network.NetworkSDK
import com.newshunt.sdk.network.Priority
import com.newshunt.search.viewmodel.SearchViewModel
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@Component(modules = [CreatePostModule::class, SearchModule::class, PhotosModule::class])
@PerActivity
interface CreatePostViewComponent {
    fun inject(i: CreatePostActivity)
}

@Module
class CreatePostModule(
    val application: Application = CommonUtils.getApplication(),
    val postId: String,
    val socialDb: SocialDB) {

    @Provides
    @Named("postId")
    fun postId(): String = postId

    @Provides
    @PerActivity
    fun app(): Application = application

    @Provides
    fun oembedApi() = Retrofit.Builder()
        .baseUrl(NewsBaseUrlContainer.getOgServiceBaseUrl())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .client(NetworkSDK.newClient(Priority.PRIORITY_HIGH, null))
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(OEmbedAPI::class.java)

    @Provides
    @PerActivity
    fun vf(
        embedUseCase: OEmbedUsecase,
        cpUseCase: CpCreationUseCase,
        locUseCase: CpLocationInsertUseCase,
        pollUseCase: CpPollInsertUseCase,
        privacyUseCase: CpPrivacyUseCase,
        htvm: HashTagViewModel,
        cpReadUseCase: CpReadUseCase,
        cpRepostUseCase: CpRepostInsertUseCase,
        detailsAPIUsecase: FetchParentUsecase):
            CreatePostViewModel.Factory = CreatePostViewModel.Factory(
            application, postId,
            embedUseCase.toMediator2(true),
            cpUseCase, locUseCase, pollUseCase, privacyUseCase,
            htvm, cpReadUseCase, cpRepostUseCase, detailsAPIUsecase)

    @Provides
    @PerActivity
    fun embedVM(api: OEmbedAPI, cpdao: CreatePostDao): OEmbedUsecase = OEmbedUsecase(api, cpdao)

    @Provides
    fun htvm(scVM: SearchViewModel): HashTagViewModel = HashTagViewModel(scVM)

    @Provides
    fun cpDao(): CreatePostDao = socialDb.cpDao()

    @Provides
    fun imgDao(): ImageDao = socialDb.imgDao()

    @Provides
    fun cpCreationUseCase(cpdao: CreatePostDao): CpCreationUseCase = CpCreationUseCase(cpdao)

    @Provides
    fun cpImgInsertionUseCase(imgDao: ImageDao): CpImageInsertUseCase = CpImageInsertUseCase(imgDao)

    @Provides
    fun cpLocInsertionUseCase(cpDao: CreatePostDao): CpLocationInsertUseCase =
        CpLocationInsertUseCase(cpDao)

    @Provides
    fun pollInsertionUseCase(cpDao: CreatePostDao): CpPollInsertUseCase = CpPollInsertUseCase(cpDao)

    @Provides
    fun privacyInsertionUseCase(cpDao: CreatePostDao): CpPrivacyUseCase = CpPrivacyUseCase(cpDao)

    @Provides
    fun imageReadUseCase(imgDao: ImageDao): CpImageReadUseCase = CpImageReadUseCase(imgDao)

    @Provides
    fun cpReadUseCase(cpdao: CreatePostDao): CpReadUseCase = CpReadUseCase(cpdao)

    @Provides
    fun fetchDao(): FetchDao = socialDb.fetchDao()

    @Provides
    fun followDao(): FollowEntityDao = socialDb.followEntityDao()

    @Provides
    fun repostInsertUseCase(cpDao: CreatePostDao): CpRepostInsertUseCase =
        CpRepostInsertUseCase(cpDao)

}

@Component(modules = [UploadJobServiceModule::class, CreatePostModule::class])
interface UploadJobServiceComponent {
    fun inject(service: UploadJobService)
}

@Module
class UploadJobServiceModule(private val context: Context = CommonUtils.getApplication()) {

    @Provides
    fun imageUploadService(): ImageUploadService =
        RestAdapterContainer.getInstance().getRestAdapter(
            NewsBaseUrlContainer.getImageBaseUrl(), Priority.PRIORITY_HIGHEST, null, false
        ).create(ImageUploadService::class.java)

    @Provides
    fun postCreationService(): PostCreationService =
        RestAdapterContainer.getInstance().getRestAdapter(
            NewsBaseUrlContainer.getPostCreationBaseUrl(),
            Priority.PRIORITY_HIGHEST, null, true, HTTP401Interceptor()
        ).create(PostCreationService::class.java)

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

@Component(modules = [PostCreationLocationModule::class])
@PerActivity
interface PostCreationLocationComponent {
    fun inject(i: PostLocationActivity)
}

@Module
class PostCreationLocationModule() {

    @Provides
    fun getPlaceClient(): PlacesClient = Places.createClient(CommonUtils.getApplication())

    @Provides
    fun getFusedLocationClient(): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(CommonUtils.getApplication())

    @Provides
    fun getPostCurrentPlacesUseCase(placesClient: PlacesClient):
            Observable<List<PostCurrentPlace>> =
        PostCurrentPlacesUseCase(placesClient).invoke(Bundle())

    @Provides
    fun getPostCurrentCityUseCase(fusedLocationProviderClient: FusedLocationProviderClient):
            Observable<PostCurrentPlace> =
        PostCurrentCityUseCase(fusedLocationProviderClient).invoke(Bundle())

    @Provides
    @PerActivity
    fun getPostCreationViewModelFactory(
        postCurrentPlaceVM: PostCurrentPlaceVM,
        postAutoCompleteLocationVM: PostAutoCompleteLocationVM):
            PostCreationViewModelFactory =
        PostCreationViewModelFactory(postCurrentPlaceVM, postAutoCompleteLocationVM)

}