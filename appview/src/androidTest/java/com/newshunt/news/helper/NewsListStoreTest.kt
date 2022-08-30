/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper

import com.newshunt.helper.NewsListStore
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.Thread.sleep

/**
 * @author satosh.dhanymaraju
 */
class NewsListStoreTest {
    lateinit var requester : NewsListStore<Int>
    val obs1 = {Observable.just(1)}
    val obs2 = {Observable.just(2)}
    val obsErr = {
        Observable.error<Int>(Exception("execp")).subscribeOn(Schedulers.io())
    }
    val uniqueId  = 42
    val obsNotInCache = {
        Observable.error<Int>(Exception("Not found in cache")).subscribeOn(Schedulers.io())
    }


    @Before
    fun setUp() {
        requester = NewsListStore()
    }

    @Test
    fun can_have_multiple_keys() {
        requester.run {
            request(1, obs1, uniqueId)
            request(2, obs2, uniqueId)
            size() shouldBe 2
        }
    }

    @Test
    fun auto_remove_expriy() {
        requester.run {
            size() shouldBe 0
            request(1, obs1, uniqueId)
            size() shouldBe 1
            request(2, obs2, uniqueId)
            size() shouldBe 2
            removeLater(expiry = 300, uniqueId = uniqueId)
            sleep(100)
            size() shouldBe 2
            sleep(300)
            size() shouldBe 0
        }
    }

    @Test
    fun on_error_immediate_removal() {
        requester.run {
            val exp = 500L
            val liveData = request(1, obsErr, uniqueId) // will be removed
            val liveData2 = request(2, obsNotInCache, uniqueId) // will be kept
            removeLater(expiry = exp, uniqueId = uniqueId)
            sleep(100)
            size() shouldBe 1
            sleep(exp)
            size() shouldBe 0
        }
    }

    @Test
    fun invalidate_cache_entry() {
        var count = 0
        requester.run {
            val f = {
                count++
                Observable.just(1)
            }
            val exp = 500L
            request(1, f, uniqueId)
            size() shouldBe  1
            count shouldBe 1
            request(1, f, uniqueId)
            size() shouldBe  1
            count shouldBe 1
            request(1, f, uniqueId, invalidateEntry = true)
            size() shouldBe  1
            count shouldBe 2
        }
    }

    @Test
    fun clear() {
        requester.run {
            request(1, obs1, uniqueId)
            size() shouldBe 1
            clear()
            size() shouldBe 0
        }
    }

    @Test
    fun expiry_cancelled_on_recreation() {
        requester.request(1, obs1, uniqueId)
        requester.run {
            request(1, obs1, uniqueId)
            size() shouldBe 1
            removeLater(uniqueId, 400)
            sleep(200)
            Thread.sleep(700)
            size() shouldBe 1
            removeLater(uniqueId, 100)
            sleep(200)
            size() shouldBe 10
        }
    }

    private infix fun Any.shouldBe(that: Any) {
        assertEquals(that, this)
    }
    private fun <T> NewsListStore<T>.size() = allItems().size
}