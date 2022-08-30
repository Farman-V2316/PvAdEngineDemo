/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newshunt.news.model.utils.test
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class LiveDataExtnsKtTest {

    @Test
    fun testTake1() {
        val source = MutableLiveData<Int>()
        val testObserver = source.take1().test()
        testObserver.assertEmpty()

        source.value = 1
        testObserver.assertValues(1)

        source.value = 2
        testObserver.assertValues(1)

        source.value = 3
        testObserver.assertValues(1)
    }

    @Test
    fun testScan() {
        val source = MutableLiveData<Int>()
        val testObserver = source.scan(0, { x , y -> x + y
        }).test()
        testObserver.assertEmpty()

        source.value = 1
        testObserver.assertValues(1)

        source.value = 2
        testObserver.assertValues(1, 3)

        source.value = 3
        testObserver.assertValues(1, 3, 6)

        source.value = 4
        testObserver.assertValues(1, 3, 6, 10)

        source.value = 5
        testObserver.assertValues(1, 3, 6, 10, 15)
    }

    @Test
    fun testDistinctUntilChanged() {
        val source = MutableLiveData<Int>()
        val testObserver = source.distinctUntilChanged().test()
        testObserver.assertEmpty()

        source.value = 1
        testObserver.assertValues(1)

        source.value = 2
        testObserver.assertValues(1, 2)

        source.value = 2
        testObserver.assertValues(1, 2) // prev value is same => so it is ignored

        source.value = 1 // value changed
        testObserver.assertValues(1, 2, 1)
        source.value = 3
        testObserver.assertValues(1, 2, 1, 3)
    }

    @Test
    fun testCombineWith() {
        val x = MutableLiveData<String>()
        val y = MutableLiveData<Int>()
        val z = MutableLiveData<Double>()
        val a = x.combineWith(y, z, { a, b, c -> Triple(a, b, c) })
        val obs = a.test()
        obs.assertEmpty()
        x.value = "ac"
        obs.assertEmpty()
        y.value = 42
        obs.assertEmpty()
        z.value = 3.0
        obs.assertValues(Triple("ac", 42, 3.0))
        z.value = 4.0
        obs.assertValues(Triple("ac", 42, 3.0), Triple("ac", 42, 4.0))
        y.value = null
        obs.assertValues(Triple("ac", 42, 3.0), Triple("ac", 42, 4.0))

    }
}