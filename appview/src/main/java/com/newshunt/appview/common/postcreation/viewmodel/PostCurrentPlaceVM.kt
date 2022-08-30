package com.newshunt.appview.common.postcreation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PostCurrentPlaceVM @Inject constructor(
    private val currentPlacesUseCase: Observable<List<PostCurrentPlace>>,
    private val currentCityUseCase: Observable<PostCurrentPlace>
) : ViewModel() {

    private val disposables = CompositeDisposable()

    val currentPlaceLiveData = MutableLiveData<Result<List<PostCurrentPlace>>>()

    fun findCurrentPlaces() {
        disposables.add(Observable.zip(
            currentPlacesUseCase.onErrorReturn { emptyList() },
            currentCityUseCase.onErrorReturn { PostCurrentPlace() },
            BiFunction<List<PostCurrentPlace>, PostCurrentPlace, List<PostCurrentPlace>>
            { list: List<PostCurrentPlace>, currentPlace: PostCurrentPlace ->
                val data = mutableListOf<PostCurrentPlace>()
                if (currentPlace.name.isNullOrEmpty().not()) {
                    data.add(0, currentPlace)
                }
                data.addAll(list)
                return@BiFunction data
            }).subscribeOn(Schedulers.io())
            .doOnError { currentPlaceLiveData.postValue(Result.failure(Exception())) }
            .subscribe {
                if (it.isEmpty()) {
                    currentPlaceLiveData.postValue(Result.failure(Exception()))
                } else {
                    currentPlaceLiveData.postValue(Result.success(it))
                }
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        if (disposables.isDisposed.not()) {
            disposables.dispose()
        }
    }
}