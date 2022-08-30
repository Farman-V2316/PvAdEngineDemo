/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.news.model.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.Logger
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

/**
 * To be used for computations that return a single deferred value.
 *
 * @see MediatorUsecase
 * @see Usecase
 */
typealias SingleUsecase<T, U> = (T) -> Single<U>

/**
 * Converts [SingleUsecase] to [MediatorUsecase]
 */
@JvmOverloads
fun <T, U> SingleUsecase<T, U>.toMediator(
        ignoreIfAnotherReqInProgress: Boolean = false,
        scheduler: Scheduler = Schedulers.io()
): MediatorUsecase<T, U> {
    return object : MediatorUsecase<T, U> {
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val data = MutableLiveData<Result0<U>>()
        private val _status = MutableLiveData<Boolean>()
        private val reqInProg = AtomicBoolean(false)

        override fun execute(t: T): Boolean {
            if (ignoreIfAnotherReqInProgress && !reqInProg.compareAndSet(false, true)) {
                return false
            }
            return disposables.add(invoke(t)
                    .subscribeOn(scheduler)
                    .doOnSubscribe {
                        _status.postValue(true)
                    }
                    .subscribe(
                            {
                                data.postValue(Result0.success(it))
                                _status.postValue(false)
                            },
                            {
                                Logger.e("Mediator", "${it.message}")
                                data.postValue(Result0.failure(ApiResponseUtils.getError(it)))
                                _status.postValue(false)
                            }
                    ))
        }

        override fun data(): LiveData<Result0<U>> = data
        override fun status(): LiveData<Boolean> {
            return _status
        }
        override fun dispose() = disposables.dispose()
    }
}
