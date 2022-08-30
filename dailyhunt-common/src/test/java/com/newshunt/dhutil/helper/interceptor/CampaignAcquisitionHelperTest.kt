/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.interceptor

import com.newshunt.common.helper.common.Constants
import com.newshunt.dhutil.helper.launch.CampaignAcquisitionHelper
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class CampaignAcquisitionHelperTest {

    @Test
    fun testLanguageExtractionFromMultiLanguageCampaign() {
        val campaign = "n:AppInstalls_Karnataka_Elections#lang:en,hi,kn;action:lookup;type:np"
        val expectedLanguages = ArrayList<String>()
        expectedLanguages.add("en")
        expectedLanguages.add("hi")
        expectedLanguages.add("kn")

        val actualLanguages = CampaignAcquisitionHelper.fetchLanguagesFromCampaign(campaign)
        assertEquals(expectedLanguages.size.toLong(), actualLanguages!!.size.toLong())
        for (i in actualLanguages.indices) {
            assertEquals(expectedLanguages[i], actualLanguages[i])
        }
    }

    @Test
    fun testLanguageExtractionFromSingleLanguageCampaign() {
        val campaign = "n:AppInstalls_Karnataka_Elections#lang:en;action:lookup;type:np"
        val expectedLanguages = ArrayList<String>()
        expectedLanguages.add("en")

        val actualLanguages = CampaignAcquisitionHelper.fetchLanguagesFromCampaign(campaign)
        assertEquals(expectedLanguages.size.toLong(), actualLanguages!!.size.toLong())
        for (i in actualLanguages.indices) {
            assertEquals(expectedLanguages[i], actualLanguages[i])
        }
    }

    @Test
    fun testLanguageExtractionFromMultiLanguageAtEndCampaign() {
        val campaign = "n:AppInstalls_Karnataka_Elections#action:lookup;type:np;lang:en,hi,kn"
        val expectedLanguages = ArrayList<String>()
        expectedLanguages.add("en")
        expectedLanguages.add("hi")
        expectedLanguages.add("kn")

        val actualLanguages = CampaignAcquisitionHelper.fetchLanguagesFromCampaign(campaign)
        assertEquals(expectedLanguages.size.toLong(), actualLanguages!!.size.toLong())
        for (i in actualLanguages.indices) {
            assertEquals(expectedLanguages[i], actualLanguages[i])
        }
    }

    @Test
    fun testLanguageExtractionFromCampaignWithoutLanguage() {
        val campaign = "n:AppInstalls_Karnataka_Elections"
        assertEquals(null, CampaignAcquisitionHelper.fetchLanguagesFromCampaign(campaign))
    }

    @Test
    fun testLanguageExtractionFromInvalidLangCampaign() {
        val campaign = "n:AppInstalls_Karnataka_Elections#lang:"
        assertEquals(null, CampaignAcquisitionHelper.fetchLanguagesFromCampaign(campaign))
    }

    @Test
    fun testLanguageExtractionFromEmptyCampaign() {
        val campaign = ""
        assertEquals(null, CampaignAcquisitionHelper.fetchLanguagesFromCampaign(campaign));
    }

    @Test
    fun testInvalidCampaign() {
        val invalidCampaign = "AppInstalls_Karnataka_Elections"
        assertFalse(CampaignAcquisitionHelper.isRecoParamsValid(invalidCampaign))
    }

    @Test
    fun testValidCampaign() {
        val campaign = "n:AppInstalls_Karnataka_Elections#lang:"
        assertTrue(CampaignAcquisitionHelper.isRecoParamsValid(campaign))
    }

    @Test
    fun testValidCampaignWithAcquisitionType() {
        val campaign = "n:AppInstalls_Karnataka_Elections;lang:en,hi,kn;action:lookup;type:np;nsfw-adt:non_nsfw;nsfwok-rac:highly_nsfw;includeig:hi_x_256_12, hi_x_64_9, hi_x_8_2,en_x_256_72, en_x_64_3, en_x_8_7;acq:DH_COOLFIE"
        assertTrue(CampaignAcquisitionHelper.isAcquisitionTypeCampaign(campaign))
    }

    @Test
    fun testValidAcquisitionTypeWithoutAcqType() {
        val campaign = "n:AppInstalls_Karnataka_Elections;lang:en,hi,kn;action:lookup;type:np;nsfw-adt:non_nsfw;nsfwok-rac:highly_nsfw;includeig:hi_x_256_12, hi_x_64_9, hi_x_8_2,en_x_256_72, en_x_64_3, en_x_8_7"
        assertFalse(CampaignAcquisitionHelper.isAcquisitionTypeCampaign(campaign))
    }

    @Test
    fun testInvalidAcquisitionTypeWithAcqType() {
        val campaign = "AppInstalls_Karnataka_Elections;lang:en,hi,kn;action:lookup;type:np;nsfw-adt:non_nsfw;nsfwok-rac:highly_nsfw;includeig:hi_x_256_12, hi_x_64_9, hi_x_8_2,en_x_256_72, en_x_64_3, en_x_8_7;acq:DH_COOLFIE"
        assertFalse(CampaignAcquisitionHelper.isAcquisitionTypeCampaign(campaign))
    }

    @Test
    fun testNewsAcquisitionType() {
        val campaign = "n:AppInstalls_Karnataka_Elections;lang:en,hi,kn;action:lookup;type:np;nsfw-adt:non_nsfw;nsfwok-rac:highly_nsfw;includeig:hi_x_256_12, hi_x_64_9, hi_x_8_2,en_x_256_72, en_x_64_3, en_x_8_7;acq:DH"
        assertEquals("DH", CampaignAcquisitionHelper.fetchAcquisitionTypeFromCampaign(campaign))
    }

    @Test
    fun testDailyTVAcquisitionType() {
        val campaign = "n:AppInstalls_Karnataka_Elections;lang:en,hi,kn;action:lookup;type:np;nsfw-adt:non_nsfw;nsfwok-rac:highly_nsfw;includeig:hi_x_256_12, hi_x_64_9, hi_x_8_2,en_x_256_72, en_x_64_3, en_x_8_7;acq:DAILY_TV"
        assertEquals("DAILY_TV", CampaignAcquisitionHelper.fetchAcquisitionTypeFromCampaign(campaign))
    }

    @Test
    fun testBuzzAcquisitionType() {
        val campaign = "n:AppInstalls_Karnataka_Elections;lang:en,hi,kn;action:lookup;type:np;nsfw-adt:non_nsfw;nsfwok-rac:highly_nsfw;includeig:hi_x_256_12, hi_x_64_9, hi_x_8_2,en_x_256_72, en_x_64_3, en_x_8_7;acq:TV"
        assertEquals("TV", CampaignAcquisitionHelper.fetchAcquisitionTypeFromCampaign(campaign))
    }

    @Test
    fun testFollowAcquisitionType() {
        val campaign = "n:AppInstalls_Karnataka_Elections;lang:en,hi,kn;action:lookup;type:np;nsfw-adt:non_nsfw;nsfwok-rac:highly_nsfw;includeig:hi_x_256_12, hi_x_64_9, hi_x_8_2,en_x_256_72, en_x_64_3, en_x_8_7;acq:FOLLOW"
        assertEquals("FOLLOW", CampaignAcquisitionHelper.fetchAcquisitionTypeFromCampaign(campaign))
    }

    @Test
    fun testAcquisitionTypeWithNullCampaign() {
        assertNull(CampaignAcquisitionHelper.fetchAcquisitionTypeFromCampaign(null))
    }

    @Test
    fun testAcquisitionTypeWithEmptyCampaign() {
        assertNull(CampaignAcquisitionHelper.fetchAcquisitionTypeFromCampaign(Constants.EMPTY_STRING))
    }
}
