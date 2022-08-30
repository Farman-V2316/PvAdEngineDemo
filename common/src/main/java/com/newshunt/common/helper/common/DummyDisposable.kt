/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common

import io.reactivex.observers.DisposableObserver

/**
 * @author shrikant.agrawal
 * This class provides the dummy disposable observer
 */
class DummyDisposable<T> : DisposableObserver<T>() {

    override fun onNext(t: T) {
        //do nothing
    }

    override fun onError(e: Throwable) {
        Logger.caughtException(e)
    }

    override fun onComplete() {
    }
}