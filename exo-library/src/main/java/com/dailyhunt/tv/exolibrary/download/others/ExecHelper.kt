/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.download.others

import com.newshunt.common.helper.common.Logger
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * Helper functions to run code for IO, UI
 * @author satosh.dhanyamraju
 */
class ExecHelper(
        private val schedulerProvider: SchedulerProvider = DefaultSchedulerProvider /*for passing different schedulers in tests*/) {

    /**
     * used to run some IO code and use the value in UI
     */
    fun <T> runIOThenUi(ioF: () -> T, uiF: (T) -> Unit): Disposable? {
        return Observable.fromCallable(ioF)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(uiF, Logger::caughtException)
    }

    fun <T> runIO(f: () -> T): Disposable? {
        return Observable.fromCallable(f)
                .subscribeOn(schedulerProvider.io())
                .subscribe({}, Logger::caughtException)
    }

    fun <T> runUI(f: () -> T): Disposable? {
        return Observable.fromCallable(f)
                .subscribeOn(schedulerProvider.ui())
                .subscribe({}, Logger::caughtException)
    }

}