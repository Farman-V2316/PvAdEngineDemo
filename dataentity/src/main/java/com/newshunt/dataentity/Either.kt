/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity

import java.io.Serializable

/**
 * Wrapper for only one of any 2 types. Like [Result] class with 2 generic types.
 * @author satosh.dhanyamraju
 */
sealed class Either<out A, out B> : Serializable {
    class Left<out A>(val value: A) : Either<A, Nothing>()
    class Right<out B>(val value: B) : Either<Nothing, B>()

    fun <C, D> bimap(f1: (A) -> C, f2: (B) -> D): Either<C, D> = when (this) {
        is Left -> Left(f1(value))
        is Right -> Right(f2(value))
    }
}