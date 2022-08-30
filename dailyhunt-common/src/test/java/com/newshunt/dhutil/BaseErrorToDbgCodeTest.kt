/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonSyntaxException
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.common.view.DbgCode
import com.newshunt.common.view.dbgCode
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.model.NoConnectivityException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.io.FileNotFoundException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Tests [BaseError] to [DbgCode] conversion
 *
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
class BaseErrorToDbgCodeTest {
    @Before
    fun setUp() {
        CommonUtils.IS_IN_TEST_MODE = true
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        AppConfig.createInstance(AppConfigBuilder())
    }

    @Test
    fun testBaseError_toDbgCode() {
        assertThat(baseErr<DbgCode.DbgHttpCode>().dbgCode(), instanceOf(DbgCode.DbgHttpCode::class.java))
        assertThat(baseErr<SocketTimeoutException>().dbgCode(), instanceOf(DbgCode.DbgSocketTimeoutCode::class.java))
        assertThat(baseErr<UnknownHostException>().dbgCode(), instanceOf(DbgCode.DbgUnknownHostCode::class.java))
        assertThat(baseErr<NoConnectivityException>().dbgCode(), instanceOf(DbgCode.DbgNoConnectivityCode::class.java))
        assertThat(baseErr<JsonSyntaxException>().dbgCode(), instanceOf(DbgCode.DbgJsonSyntaxCode::class.java))
        assertThat(baseErr<FileNotFoundException>().dbgCode(), instanceOf(DbgCode.DbgUnexpectedCode::class.java))
    }

    private inline fun <reified T: Throwable> baseErr() = BaseError("").apply {
        originalError = Mockito.mock(T::class.java)
    }
}