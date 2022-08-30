/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * Returns livedata that emits atmost 1 value.
 * Similar to : https://rxmarbles.com/#take
 */
fun <T> LiveData<T>.take1(): LiveData<T> {
    val mediator = MediatorLiveData<T>()
    mediator.addSource(this) {
        mediator.value = it
        mediator.removeSource(this)
    }
    return mediator
}

/**
 * For accumulating result. Mimics Rxjava scan operator.
 * https://rxmarbles.com/#scan
 */
fun <T, R> LiveData<T>.scan(initial: R, acc: (R, T) -> R) : LiveData<R> {
  val op = MediatorLiveData<R>()
  var cur = initial
  op.addSource(this) {
    val tmp   = acc(cur, it)
    op.value = tmp
    cur = tmp
  }
  return op
}

/**
 * Mimics behavior of rxjava operator
 * https://rxmarbles.com/#distinctUntilChanged
 */
fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> {
    val mediator = MediatorLiveData<T>()
    var initDone : Boolean = false
    mediator.addSource(this) {
        if (!initDone) {
            initDone = true
            mediator.value = it
        } else if (mediator.value != it) {
            mediator.value = it
        }

    }
    return mediator
}