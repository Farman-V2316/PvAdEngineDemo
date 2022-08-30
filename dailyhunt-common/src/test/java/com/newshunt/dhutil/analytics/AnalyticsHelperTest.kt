/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newshunt.common.helper.analytics.NhAnalyticsUtility.ErrorResponseCode.CONTENT_ERROR
import com.newshunt.common.helper.analytics.NhAnalyticsUtility.ErrorResponseCode.NETWORK_ERROR
import com.newshunt.common.helper.analytics.NhAnalyticsUtility.ErrorResponseCode.NO_INTERNET
import com.newshunt.common.helper.analytics.NhAnalyticsUtility.ErrorResponseCode.SERVER_ERROR
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R
import com.newshunt.dhutil.analytics.AnalyticsHelper.getErrorResponseCode
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test cases for [AnalyticsHelper]
 *
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
class AnalyticsHelperTest {
    init {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
    }
    private val errorConnectivity = CommonUtils.getString(R.string.error_connectivity)
    private val errorNoConnection = CommonUtils.getString(R.string.error_no_connection)
    private val errorNoConnectionSnackbar = CommonUtils.getString(R.string.error_no_connection_snackbar)
    private val noContentFound = CommonUtils.getString(R.string.no_content_found)
    private val errorNoContentMsg = CommonUtils.getString(R.string.error_no_content_msg)
    private val errorNoContentSnakbar = CommonUtils.getString(R.string.error_no_content_msg_snackbar)
    private val errorServerIssue = CommonUtils.getString(R.string.error_server_issue)
    private val errorGeneric = CommonUtils.getString(R.string.error_generic)
    private val noConnectionError = CommonUtils.getString(R.string.no_connection_error)


    @Test
    fun testErrorResponseCode() {
        //content
        assertThat(getErrorResponseCode(errorNoContentMsg), equalTo(CONTENT_ERROR))
        assertThat(getErrorResponseCode(errorNoContentSnakbar), equalTo(CONTENT_ERROR))
        assertThat(getErrorResponseCode(noContentFound), equalTo(CONTENT_ERROR))
        //no internet
        assertThat(getErrorResponseCode(errorNoConnection), equalTo(NO_INTERNET))
        assertThat(getErrorResponseCode(errorNoConnectionSnackbar), equalTo(NO_INTERNET))
        //server
        assertThat(getErrorResponseCode(errorServerIssue), equalTo(SERVER_ERROR))
        //network
        assertThat(getErrorResponseCode(errorGeneric), equalTo(NETWORK_ERROR))
        assertThat(getErrorResponseCode(noConnectionError), equalTo(NETWORK_ERROR))
        assertThat(getErrorResponseCode(errorConnectivity), equalTo(NETWORK_ERROR))
    }
}