package com.newshunt.appview.common.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.news.model.usecase.FetchWebDataUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import javax.inject.Inject

class WebFragmentViewModel(val fetchWebData: MediatorUsecase<String, List<PostEntity>>) : ViewModel() {

	val webLiveData : LiveData<Result0<List<PostEntity>>> = fetchWebData.data()

	fun fetchData(url: String) {
		fetchWebData.execute(url)
	}

	class Factory @Inject constructor(val fetchWebData: FetchWebDataUsecase) :
		ViewModelProvider.Factory {

		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			return WebFragmentViewModel(
				fetchWebData.toMediator2(true)) as T
		}
	}
}