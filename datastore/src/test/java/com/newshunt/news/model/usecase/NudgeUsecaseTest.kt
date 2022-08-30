/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.dataentity.common.asset.CardInfo
import com.newshunt.dataentity.common.asset.CardNudge
import com.newshunt.dataentity.common.asset.CardNudgeTerminateType
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.news.model.daos.NudgeDao
import com.newshunt.sdk.network.NetworkSDK
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Tests for [ReadNudgesUsecase], [MarkNudgeShownUsecase], [TerminateCardNudgeUsecase]
 *
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class NudgeUsecaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    @io.mockk.impl.annotations.MockK
    lateinit var nudgeDao: NudgeDao

    @Before
    fun setUp() {
        CommonUtils.IS_IN_TEST_MODE = true
        MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks
        AppConfig.createInstance(AppConfigBuilder())
        NetworkSDK.init(ApplicationProvider.getApplicationContext(), null, false)
    }

    @Test
    fun testTerimateUc() {
        every {
            nudgeDao.terminate(any())
        } returns 1

        val observer = TerminateCardNudgeUsecase(nudgeDao).invoke(CardNudgeTerminateType.share).test()

        verify {
            nudgeDao.terminate(CardNudgeTerminateType.share.name)
        }
        observer.assertResult(true)
    }

    @Test
    fun testMarkShownUc() {
        every {
            nudgeDao.markShown(any())
        } returns Unit

        val observer = MarkNudgeShownUsecase(nudgeDao).invoke(21).test()

        verify {
            nudgeDao.markShown(21)
        }
        observer.assertResult(true)
    }
    @Test
    fun testReadUc() {
        val c1 = CardInfo("id", PostEntityLevel.TOP_LEVEL, Format.HTML, SubFormat.STORY, UiType2.NORMAL, true)
        val c2 = CardInfo("id", PostEntityLevel.TOP_LEVEL, Format.HTML, SubFormat.STORY, UiType2.NORMAL, true)
        val cn1 = CardNudge(22, terminationType = "share", type = "u", text = "", tooltipDurationSec = 20)
        val cn2 = CardNudge(23, terminationType = "share", type = "u", text = "", tooltipDurationSec = 20)

        val l = listOf(c1, c2)
        val resList = listOf(c1 to cn1, c2 to cn2)
        every {
            nudgeDao.readCardNudges(l)
        } returns resList

        val observer = ReadNudgesUsecase(nudgeDao).invoke(l).test()
        verify {
            nudgeDao.readCardNudges(l)
        }

        val map = observer.assertValueCount(1)
                .assertNoErrors()
                .values().first()
        assertThat(map).containsExactlyEntriesIn(mutableMapOf(
                c1.id to cn1,
                c2.id to cn2
        ))
    }
}