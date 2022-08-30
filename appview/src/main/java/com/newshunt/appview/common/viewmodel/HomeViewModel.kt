package com.newshunt.appview.common.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.model.usecase.MediatorFirstAddPageUsecase
import com.newshunt.dataentity.common.model.entity.CommunicationEventsResponse
import com.newshunt.dataentity.common.pages.AddPageEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageResponse
import com.newshunt.news.model.repo.PageSyncRepo
import com.newshunt.news.model.usecase.CommunicationEventUsecase
import com.newshunt.news.model.usecase.MediatorHomePageUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.StoreHomePagesUsecase
import com.newshunt.news.model.usecase.toMediator2

class HomeViewModel(val section: String) : ViewModel() {

  private var mediatorUsecase : MediatorUsecase<String, List<PageEntity>> = MediatorHomePageUsecase().toMediator2()
  private var firstPageAdded : MediatorUsecase<Any, AddPageEntity> = MediatorFirstAddPageUsecase()
  private val storeHomePagesUsecase  = StoreHomePagesUsecase(PageSyncRepo(section)).toMediator2()
  val pageLiveData : LiveData<Result0<List<PageEntity>>>
  val nwLiveData: LiveData<Result0<PageResponse>>
  val firstAddPageLiveData : LiveData<Result0<AddPageEntity>>

  private val communicationEventUsecase : MediatorUsecase<Any, CommunicationEventsResponse>
  val communicationLiveData : LiveData<Result0<CommunicationEventsResponse>>

  private var communicationProcessed = false

  init {
    pageLiveData = mediatorUsecase.data()
    nwLiveData = storeHomePagesUsecase.data()
    mediatorUsecase.execute(section)

    firstAddPageLiveData = firstPageAdded.data()
    firstPageAdded.execute(Any())

    communicationEventUsecase = CommunicationEventUsecase().toMediator2()
    communicationLiveData = communicationEventUsecase.data()
  }

  fun viewStarted() {
    syncPage()

    if (!communicationProcessed) {
      communicationEventUsecase.execute(Any())
      communicationProcessed = true
    }
  }

  fun syncPage() {
    // get the updated data from database
    mediatorUsecase.execute(section)
    storeHomePagesUsecase.execute(section)
  }
}

class HomeViewModelFactory(val section: String) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return HomeViewModel(section) as T
  }

}