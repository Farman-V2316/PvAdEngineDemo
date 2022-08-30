/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newshunt.common.helper.common.SetLocaleUtil
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.actionSettings
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.btnHome
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.dialogButtonRetry
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorConnectivity
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorGeneric
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorNoConnection
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorNoConnectionSnackbar
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorNoContentMsg
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorNoContentMsgSnackbar
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.errorServerIssue
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.noConnectionError
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.noContentFound
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.offlineSavingFailed
import com.newshunt.dhutil.view.ErrorMessageBuilder.Companion.savedArticleEmptyList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [ErrorMessageBuilder] uses string(error-message) comparison to setup appropriate ui.
 * So, all messages should be different to prevent ambiguity. This test class is for the same
 *
 * This test can be deleted once error-codes are used for comparison rather than messages
 *
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
class ErrorMessageBuilderStringTest {
    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun test_as() = checkUniqueness("as")
    @Test
    fun test_bh() = checkUniqueness("bh")
    @Test
    fun test_bn() = checkUniqueness("bn")
    @Test
    fun test_en() = checkUniqueness("en")
    @Test
    fun test_gu() = checkUniqueness("gu")
    @Test
    fun test_hi() = checkUniqueness("hi")
    @Test
    fun test_kn() = checkUniqueness("kn")
    @Test
    fun test_ml() = checkUniqueness("ml")
    @Test
    fun test_mr() = checkUniqueness("mr")
    @Test
    fun test_ne() = checkUniqueness("ne")
    @Test
    fun test_or() = checkUniqueness("or")
    @Test
    fun test_pa() = checkUniqueness("pa")
    @Test
    fun test_sd() = checkUniqueness("sd")
    @Test
    fun test_ta() = checkUniqueness("ta")
    @Test
    fun test_te() = checkUniqueness("te")
    @Test
    fun test_ur() = checkUniqueness("ur")


    private fun checkUniqueness(langCode: String) {
        SetLocaleUtil.updateLanguage(langCode)
        val strings = listOf(
                "errorConnectivity" to errorConnectivity,
                "errorNoConnection" to errorNoConnection,
                "errorNoConnectionSnackbar" to errorNoConnectionSnackbar,
                "errorServerIssue" to errorServerIssue,
                "savedArticleEmptyList" to savedArticleEmptyList,
                "noContentFound" to noContentFound,
                "errorGeneric" to errorGeneric,
                "noConnectionError" to noConnectionError,
                "errorNoContentMsg" to errorNoContentMsg,
                "errorNoContentMsgSnackbar" to errorNoContentMsgSnackbar,
                "offlineSavingFailed" to offlineSavingFailed,
                "dialogButtonRetry" to dialogButtonRetry,
                "btnHome" to btnHome,
                "actionSettings" to actionSettings
        )
        val occ = strings.groupBy { it.second }.filterValues { it.size>1 }.mapValues {
            it.value.map {
                it.first
            }
        }
        assertThat(occ, equalTo(emptyMap()))
    }
}