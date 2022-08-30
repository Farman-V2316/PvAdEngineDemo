/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.newshunt.appview.common.group.model.service.HandleValidatorService
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.model.entity.HandleAvailabilityUIResponseWrapper
import com.newshunt.dataentity.model.entity.UIResponseWrapper
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named


/**
 * All Profile related usecases should be implemented here
 * <p>
 * Created by srikanth.ramaswamy on 11/08/2019.
 */

val SYNC_TO_SERVER_STATUS = listOf(SyncStatus.MARKED, SyncStatus.UN_SYNCED)
val PROGRESS_STATUS = listOf(SyncStatus.IN_PROGRESS)

/**
 * Usecase implementation to debounce the input string and hit the checkhandle usecase to verify if
 * handle is valid
 */
class ValidateHandleUsecase @Inject constructor(private val service: HandleValidatorService,
                                                @Named("debounceDelay") private val debounceDelayMillis: Long) : MediatorUsecase<String, HandleAvailabilityUIResponseWrapper> {
    private val queryPublisher = PublishSubject.create<String>()
    private val _data = MutableLiveData<Result0<HandleAvailabilityUIResponseWrapper>>()
    private val disposables: CompositeDisposable = CompositeDisposable()

    init {
        disposables.add(queryPublisher
                .debounce(debounceDelayMillis, TimeUnit.MILLISECONDS)
                .flatMap {
                    service.checkHandle(it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.response.code == Constants.HTTP_SUCCESS) {
                        val uiResponseWrapper: UIResponseWrapper<Int> = if (it.response.status != null) {
                            UIResponseWrapper(null, it.response.status?.code, it.response.status?.message)
                        } else {
                            UIResponseWrapper(Constants.HTTP_SUCCESS, null, null)
                        }
                        _data.value = Result0.success(HandleAvailabilityUIResponseWrapper(it.handle, uiResponseWrapper))
                    }
                },
                {
                    //Silently consume the error for this API. No need to inform the view
                })
        )
    }

    override fun execute(t: String): Boolean {
        queryPublisher.onNext(t)
        return true
    }

    override fun data(): LiveData<Result0<HandleAvailabilityUIResponseWrapper>> {
        return _data
    }

    override fun dispose() = disposables.dispose()
}