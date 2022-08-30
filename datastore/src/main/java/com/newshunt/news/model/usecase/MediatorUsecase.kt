/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.UIResponseWrapper
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

// Common framework


/**
 * @author satosh.dhanyamraju
 */
/**
 * Usecase is a function that returns an observable.
 *
 * Compose usecases by composing observables.
 *
 * Usecases affect the model (side effect)
 *
 * @see MediatorUsecase
 * @see SingleUsecase
 *
 */
typealias Usecase<T, U> = (T) -> Observable<U>


/**
 * Usecase having view layer interaction.
 *
 * View calls [execute] to trigger action, expects [Result] in [data]
 * [dispose] is called when view is destroyed
 * @author satosh.dhanyamraju
 */
interface MediatorUsecase<T, U> {
    companion object {
        val _status = MutableLiveData<Boolean>()
    }

    /**
     * would be invoked by view. Results are pushed to [data]
     */
    fun execute(t: T): Boolean

    /**
     * for view
     */
    fun data(): LiveData<Result0<U>>

    /**
     * for view: to show progress bar
     */
    fun status(): LiveData<Boolean> = _status

    /**
     * called when view is destroyed
     */
    fun dispose() {}

    /**
     * [data] livedata wraps both sucess and failure in [Result]. If we get sucess and a failure in
     * a quick succession, it is possible livedata observer might get only failure (which is latest)
     * livedata event. This is reproducible with using [Observable.mergeDelayError] in [BundleUsecase]
     *
     * This live data contains only success events.
     */
    fun successData() : LiveData<U?>? = null

}

/**
 * - Define and use new `Result` type in MediatorUsecase because kotlin built-in Result is getting
 *   auto-unwrapped when used as function argument. @see LiveData.scan
 *
 *  @author satosh.dhanyamraju
 */
class Result0<T> private constructor(private val _data: T?, private val error: Throwable?) {
    val isSuccess: Boolean
        get() {
            return _data != null
        }

    val isFailure: Boolean
        get() {
            return error != null
        }

    fun getOrNull() = _data

    fun exceptionOrNull() = error

    fun getOrThrow() : T {
        return _data!!
    }

    fun getOrDefault(t1: T): T {
        return _data ?: t1
    }
    companion object {
        fun <T> success(t: T?): Result0<T> {
            return Result0(t, null)
        }
        fun <T> failure(e: Throwable): Result0<T> {
            return Result0(null, e)
        }
    }
}


/**
 * Use [MediatorUsecase.successData] if available, fallback to [MediatorUsecase.data] with success-only filter
 */
fun <T, U> MediatorUsecase<T,U>.onlyData() : LiveData<U?> {
    return  successData() ?: Transformations.map(data(), {it.getOrNull()})!!
}


/**
 * All these usecases compose very well, they pass around the same bundle.
 * @author satosh.dhanyamraju
 */
interface BundleUsecase<T> : Usecase<Bundle, T>



/**
 * Converts [Usecase] to [MediatorUsecase]
 */
@Deprecated("use toMediator2")
fun <T, U> Usecase<T, U>.toMediator(): MediatorUsecase<T, U> {
    return object : MediatorUsecase<T, U> {
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val _data = MutableLiveData<Result0<U>>()
        private val _status = MutableLiveData<Boolean>()

        override fun execute(t: T) = disposables.add(invoke(t).subscribe(
                { _data.postValue(Result0.success(it)) },
                {
                    _data.postValue(Result0.failure(ApiResponseUtils.getError(it)))
                },
                { _status.postValue(false) },
                { _status.postValue(true) }
        ))

        override fun data(): LiveData<Result0<U>> = _data
        override fun status(): LiveData<Boolean> = _status
        override fun dispose() = disposables.dispose()
    }
}

/**
 * To be used for observables like API calls - they terminate by themselves, may be allowed to run
 * even when view is not active.
 * Converts [Usecase] to [MediatorUsecase] with options to
 * 1. specify scheduler
 * 2. ignore [MergeUsecase.exceute] call when its observable is not finished emitting
 */
@JvmOverloads
fun <T, U> Usecase<T, U>.toMediator2(
        ignoreIfAnotherReqInProgress: Boolean = false,
        scheduler: Scheduler = Schedulers.io(),
        needSuccessDataSeparately: Boolean = false,
        needAccurateStatusForParallelExecution: Boolean = false
): MediatorUsecase<T, U> {
    val successMediator = if (needSuccessDataSeparately) MutableLiveData<U>() else null
    return if (needAccurateStatusForParallelExecution) {
        MediatorUsecaseParallelImpl(this, ignoreIfAnotherReqInProgress, scheduler, successMediator)
    } else {
        MediatorUsecaseDefaultImpl(this, ignoreIfAnotherReqInProgress, scheduler, successMediator)
    }
}


/**
 * Implementation for [MediatorUsecase] that handles concurrent requests and scheduling.
 */
internal open class  MediatorUsecaseDefaultImpl<T, U>(
        private val usecase : Usecase<T, U>,
        private val ignoreIfAnotherReqInProgress: Boolean,
        private val scheduler: Scheduler,
        private val successData : MutableLiveData<U>? = null
) : MediatorUsecase<T, U> {
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val _data = MutableLiveData<Result0<U>>()
    private val reqInProg = AtomicBoolean(false)
    protected val _status = MutableLiveData<Boolean>()


    override fun execute(t: T): Boolean {
        if (ignoreIfAnotherReqInProgress && !reqInProg.compareAndSet(false, true)) {
            return false
        }
        return disposables.add(usecase.invoke(t)
                .subscribeOn(scheduler)
                .subscribe(
                        {
                            successData?.postValue(it)
                            val data = Result0.success(it)
                            _data.postValue(data)
                        },
                        {
                            Logger.e("Mediator2", "${it.message}", it)
                            _data.postValue(Result0.failure(ApiResponseUtils.getError(it)))
                            inProg(false)
                        },
                        { inProg(false) },
                        { inProg(true) }
                ))
    }

    override fun data(): LiveData<Result0<U>> = _data
    override fun status(): LiveData<Boolean> {
        return _status
    }

    override fun dispose() = disposables.dispose()
    open fun inProg(state: Boolean) {
        _status.postValue(state)
        reqInProg.set(state)
    }

    override fun successData(): LiveData<U?>? = successData
}

/**
 * An implementation of MediatorUsecase which gives out accurate status when multiple API calls
 * are made using the same usecase instance. This waits for all executions to finish before
 * dispatching done status and similarly dispatches start status when it goes from empty to 1 job
 * execution.
 */
internal class MediatorUsecaseParallelImpl<T, U>(
        usecase: Usecase<T, U>,
        ignoreIfAnotherReqInProgress: Boolean,
        scheduler: Scheduler,
        successData: MutableLiveData<U>? = null) : MediatorUsecaseDefaultImpl<T, U>(usecase, ignoreIfAnotherReqInProgress, scheduler, successData) {
    private val inProgressCounter = AtomicInteger(0)
    override fun inProg(state: Boolean) {
        val prevCounter = inProgressCounter.get()
        val newCounter = if (state) {
            inProgressCounter.incrementAndGet()
        } else {
            inProgressCounter.decrementAndGet()
        }
        if(prevCounter == 1 && newCounter == 0) {
            _status.postValue(false)
        } else if(prevCounter == 0 && newCounter == 1) {
            _status.postValue(true)
        }
    }
}

fun <T> getResult(result: Result<T>) : T? {
    return result.getOrNull()
}


fun disposeUsecases(vararg usecases: MediatorUsecase<*, *>) = usecases.forEach { it.dispose() }

/**
 * Usecase having view layer interaction.
 *
 * View calls [execute] to trigger action, expects [Result] in [data]
 * [dispose] is called when view is destroyed
 *
 */
interface UIWrapperUsecase<T, U> : Usecase<T, ApiResponse<U>> {

    /**
     * would be invoked by view. Results are pushed to [data]
     */
    fun execute(t: T): Boolean

    /**
     * for view
     */
    fun data(): LiveData<Result<UIResponseWrapper<U>>>

    /**
     * for view: to show progress bar
     */
    fun status(): LiveData<Boolean>

    /**
     * called when view is destroyed
     */
    fun dispose()
}

fun <T, U> Usecase<T, ApiResponse<U>>.toUIWrapper(): UIWrapperUsecase<T, U> {
    return object : UIWrapperUsecase<T, U> {
        private val disposables: CompositeDisposable = CompositeDisposable()
        private val _data = MutableLiveData<Result<UIResponseWrapper<U>>>()
        private val _status = MutableLiveData<Boolean>()
        override fun invoke(p1: T) = this@toUIWrapper(p1)

        override fun execute(t: T) = disposables.add(invoke(t).subscribe(
                {
                    val uiResponseWrapper: UIResponseWrapper<U> = if (it.data != null) {
                        UIResponseWrapper(it.data, null, null)
                    } else {
                        UIResponseWrapper(null, it.status?.code, it.status?.message)
                    }
                    Logger.d("UIWrapper", "Response $uiResponseWrapper")
                    _data.postValue(Result.success(uiResponseWrapper))
                },
                {
                    Logger.e("UIWrapper", "Error Response ${it.message}")
                    _data.postValue(Result.failure(ApiResponseOperator.getError(it)))
                    _status.postValue(false)
                },
                { _status.postValue(false) },
                { _status.postValue(true) }
        ))

        override fun data(): LiveData<Result<UIResponseWrapper<U>>> = _data
        override fun status(): LiveData<Boolean> = _status
        override fun dispose() = disposables.dispose()
    }
}