/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil
import com.newshunt.common.view.DbgCode
import com.newshunt.common.view.DbgCode.DbgHttpCode
import com.newshunt.common.view.DbgCode.DbgJsonSyntaxCode
import com.newshunt.common.view.DbgCode.DbgNoConnectivityCode
import com.newshunt.common.view.DbgCode.DbgSocketTimeoutCode
import com.newshunt.common.view.DbgCode.DbgUnexpectedCode
import com.newshunt.common.view.DbgCode.DbgUnknownHostCode
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.core.StringStartsWith
import org.junit.Test

/**
 *
 * Tests for [DbgCode]
 * @author satosh.dhanyamraju
 */
class DbgCodeTest {
    // ListNoContentException : should never be shown, ideally
    //

    @Test
    fun testUniqeId() {
        assertThat(DbgHttpCode(200).id, not(equalTo(DbgHttpCode(200).id)))
    }

    @Test
    fun testHttpCodes() {
        //100
        assertThat(DbgHttpCode(1).get(), equalTo("BU00"))
        assertThat(DbgHttpCode(1).get(), equalTo("BU00"))
        assertThat(DbgHttpCode(12).get(), equalTo("BU00"))
        assertThat(DbgHttpCode(123).get(), equalTo("BU00"))

        //200
        assertThat(DbgHttpCode(2).get(), equalTo("BB"))
        assertThat(DbgHttpCode(204).get(), equalTo("BB04"))

        //300
        assertThat(DbgHttpCode(3).get(), equalTo("BC"))
        assertThat(DbgHttpCode(30).get(), equalTo("BC0"))
        assertThat(DbgHttpCode(304).get(), equalTo("BC04"))

        //400
        assertThat(DbgHttpCode(4).get(), equalTo("BD"))
        assertThat(DbgHttpCode(40).get(), equalTo("BD0"))
        assertThat(DbgHttpCode(404).get(), equalTo("BD04"))

        //500
        assertThat(DbgHttpCode(5).get(), equalTo("BE"))
        assertThat(DbgHttpCode(50).get(), equalTo("BE0"))
        assertThat(DbgHttpCode(504).get(), equalTo("BE04"))

        // 600
        assertThat(DbgHttpCode(604).get(), equalTo("BU00"))
        assertThat(DbgHttpCode(999904).get(), equalTo("BU00"))
    }


    @Test
    fun testSocketTimeoutCode() {
        assertThat(DbgSocketTimeoutCode().get(), StringStartsWith("AS"))
    }

    @Test
    fun testUnknownHostCode() {
        assertThat(DbgUnknownHostCode().get(), StringStartsWith("AD"))
    }

    @Test
    fun testNoConnectivityCode() {
        assertThat(DbgNoConnectivityCode().get(), StringStartsWith("AN"))
    }

    @Test
    fun testJsonSyntaxCode() {
        assertThat(DbgJsonSyntaxCode().get(), StringStartsWith("CJ"))
    }

    @Test
    fun testErrorUnexpectedCode() {
        assertThat(DbgUnexpectedCode("ab").get(), equalTo("ZU"))
        assertThat(DbgUnexpectedCode("ab").message, equalTo("ab"))
        assertThat(DbgUnexpectedCode("bc").get(), equalTo("ZU"))
        assertThat(DbgUnexpectedCode("").get(), equalTo("ZU"))
    }
}

