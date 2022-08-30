package com.newshunt.appview.common.viewmodel

import android.content.Intent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.R
import com.newshunt.appview.common.model.usecase.DeleteAllAddPageUsecase
import com.newshunt.appview.common.model.usecase.MediatorAddPageUsecase
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.pages.AddPageEntity
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants

class AddPageViewModel(val section: String): ViewModel(), ClickHandlingViewModel {

  private val observableAddPage = MediatorAddPageUsecase()
  val addPageLiveData : LiveData<Result0<List<AddPageEntity>>>

  init {
    DeleteAllAddPageUsecase().toMediator2().execute(Any())
    addPageLiveData = observableAddPage.data()
    observableAddPage.execute(Any())
  }

  override fun onViewClick(view: View) {
    // create the navigation intent to open reorder activity
    if (view.id == R.id.toolbar_settings_button) {
      val intent = Intent(Constants.REORDER_PAGE_OPEN_ACTION)
      intent.putExtra(NewsConstants.DH_SECTION, section)
      NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
    }
  }
}

class AddPageViewModelFactory(val section: String) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return AddPageViewModel(section) as T
  }
}