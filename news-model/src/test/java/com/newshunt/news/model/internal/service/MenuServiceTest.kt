/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.internal.service

import com.newshunt.dhutil.Expirable
import com.newshunt.news.model.entity.MenuEntity
import com.newshunt.news.model.internal.rest.PostDislikeApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner

/**
 * todo(tech debt): fix compile errors
 * @author satosh.dhanymaraju
 */
@Ignore
@RunWith(MockitoJUnitRunner::class)
class MenuServiceTest {
    //@Mock
    private  var runner: (() -> Unit) -> Unit = {it.invoke()}
    @Mock
    private lateinit var api: PostDislikeApi
    @Mock
    private lateinit var saveToPref: (List<Expirable<MenuEntity>>) -> Unit

    val ttl  = 10L
    private lateinit var service: MenuServiceImpl

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val l = listOf(Expirable.fromTTL(10, MenuEntity("i1", "g1"), 1))
        service = MenuServiceImpl(l, runner, api, saveToPref)
    }

    @Test
    fun `takes initial`() {
        val d = service.allDisliked()
        assertEquals(1, d.size)
        d[0].run {
            isExpired(9) shouldBe false
            val (item, group) = value
            item shouldBe "i1"
            group shouldBe "g1"
        }
    }


    private fun <T> T.toExpirable(curTime: Long, ttlL: Long = ttl): Expirable<T> {
        return Expirable.fromTTL(ttlL, this, curTime)
    }
/*
    @Test
    fun `dislike adds entry on post success`() {
        val testobs = TestObserver.create<ApiResponse<Any>>()
        val d2 = MenuEntity("id2","gd2").toExpirable(5)
        Mockito.`when`(api.postDislike(d2.value)).thenReturn(Observable.just(ApiResponse<Any>()))

        service.dislikePost(d2, "").subscribe(testobs)
        testobs.await()

        verify(api, times(1)).postDislike(d2.value, "")
        service.allDisliked().any { it.value.itemId == "id2" } shouldBe true
        verify(saveToPref).invoke(service.allDisliked())
        testobs.assertNoErrors()
    }

    @Test
    fun `dislike does not add  entry on post fail`() {
        val testobs = TestObserver.create<ApiResponse<Any>>()
        val d2 = MenuEntity("id2","gd2").toExpirable(5)
        val err = Exception("testing..")
        Mockito.`when`(api.postDislike(d2.value, "")).thenReturn(Observable.error(err))

        service.dislikePost(d2).subscribe(testobs)
        testobs.await()

        verify(api).postDislike(d2.value)
        service.allDisliked().none { it.value.itemId == "id2" } shouldBe true
        verifyNoMoreInteractions(saveToPref)
        testobs.assertError(err)
    }*/
}