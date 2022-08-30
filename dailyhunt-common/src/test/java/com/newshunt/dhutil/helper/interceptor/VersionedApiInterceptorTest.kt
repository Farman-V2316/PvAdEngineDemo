/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.interceptor

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations

/**
 * For testing retry scenarios of [VersionedApiInterceptor]
 *
 * @author satosh.dhanymaraju
 */
@RunWith(AndroidJUnit4::class)
class VersionedApiInterceptorTest {
    lateinit var interceptor: VersionedApiInterceptor
    val someReq = Request.Builder().url("http://www.newshunt.com/").build()
    val somedataResp = ResponseBody.create(MediaType.parse("text/plain"), "some-data")
    val errorBody = ResponseBody.create(MediaType.parse("text/plain"), "error")

    var parsever: (String) -> String = { if (it.contains("error")) "" else "some" }

    val entity = VersionedApiEntity(VersionEntity.APP_LAUNCH_CONFIG)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        interceptor = VersionedApiInterceptor(parsever, {})
    }

    @Test
    fun `304 just forwards the response`() {
        val chain = chain(resp(304))
        interceptor.intercept(chain)
        verify(chain).request()
        verify(chain).proceed(someReq)
        verifyNoMoreInteractions(chain)
    }

    @Test
    fun `200 store, no retry`() {
        val resp = resp(200)
        val chain = chain(resp)
        assertEquals(resp, interceptor.intercept(chain))
        verify(chain).request()
        verify(chain).proceed(someReq)
        verifyNoMoreInteractions(chain)
    }

    @Test
    fun `400 retry 200 store`() {
        val resp400 = resp(400)
        val resp200 = resp(200)
        val chain = chain(resp400, resp200)
        assertEquals(resp200, interceptor.intercept(chain))
        verify(chain).request()
        verify(chain, times(2)).proceed(someReq)
        verifyNoMoreInteractions(chain)
    }

    @Test
    fun `400 retry 400 no-store`() {
        val resp400 = resp(400)
        val resp401 = resp(401)
        val chain = chain(resp400, resp401)
        assertEquals(resp401, interceptor.intercept(chain))
        verify(chain).request()
        verify(chain, times(2)).proceed(someReq)
        verifyNoMoreInteractions(chain)
    }

    @Test
    fun `reset ver 0 when retry-parse fails`() {
        val resp400 = resp(400)
        val resp401 = resp(200, body = errorBody)
        val chain = chain(resp400, resp401)
        assertEquals(resp401, interceptor.intercept(chain))
        verify(chain).request()
        verify(chain, times(2)).proceed(someReq)
        verifyNoMoreInteractions(chain)
    }

    private fun resp(code: Int, req: Request = someReq, body: ResponseBody = somedataResp): Response {
        return Response.Builder().request(someReq).protocol(Protocol.HTTP_1_0)
                .message("Intercepted!").code(code).body(body).build()
    }

    private fun chain(response1: Response, response2: Response = response1): Interceptor.Chain {
        val chain = mock(Interceptor.Chain::class.java)
        `when`(chain.request()).thenReturn(someReq)
        `when`(chain.proceed(someReq)).thenReturn(response1, response2)
        return chain
    }
}