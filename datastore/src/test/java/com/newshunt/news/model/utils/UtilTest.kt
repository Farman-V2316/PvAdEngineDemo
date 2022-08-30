/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import com.google.common.truth.Expect
import com.google.common.truth.Truth.assertThat
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.EntityConfig2
import com.newshunt.dataentity.common.helper.common.CommonUtils.isAtleastNextXCalDay
import com.newshunt.dataentity.common.helper.common.CommonUtils.isNextCalDay
import com.newshunt.dhutil.helper.preference.langsFirst
import com.newshunt.dhutil.helper.preference.langsRest
import com.newshunt.dhutil.runOnce
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


class UtilTest {
    @Rule
    @JvmField
    val expect = Expect.create()

    @Test
    fun testRunOnce() {
        var count = 0
        val c = runOnce<Unit> {
            count += 1
            println("called ")
            if (count > 1) Assert.fail("called more than once")
            true
        }

        c(Unit)
        c(Unit)
        c(Unit)
        c(Unit)
    }

    @Test
    fun `test increment count null becomes 1`() {
        val c1 = Counts2(COMMENTS = null)
        val c2 = Counts2(COMMENTS = EntityConfig2(""))

        assertThat(c1.incrementCommentCount().COMMENTS?.value).isEqualTo("1")
        assertThat(c2.incrementCommentCount().COMMENTS?.value).isEqualTo("1")
    }

    @Test
    fun `test increment count 1 becomes 2`() {
        assertThat(Counts2(COMMENTS = EntityConfig2("1")).incrementCommentCount().COMMENTS?.value)
                .isEqualTo("2")
    }

    @Test
    fun `test increment count 1k remains 1k`() {
        assertThat(Counts2(COMMENTS = EntityConfig2("1k")).incrementCommentCount().COMMENTS?.value)
                .isEqualTo("1k")
    }

    @Test
    fun `test decrement count null becomes 0 and empty remains empty`() {
        val c1 = Counts2(COMMENTS = null)
        val c2 = Counts2(COMMENTS = EntityConfig2(""))

        assertThat(c1.decrementCommentCount().COMMENTS?.value).isEqualTo("0")
        assertThat(c2.decrementCommentCount().COMMENTS?.value).isEmpty()
    }

    @Test
    fun `test decrement count 1 becomes 0`() {
        assertThat(Counts2(COMMENTS = EntityConfig2("1")).decrementCommentCount().COMMENTS?.value).isEqualTo("0")
    }

    @Test
    fun `test decrement count 0 remains 0`() {
        assertThat(Counts2(COMMENTS = EntityConfig2("0")).decrementCommentCount().COMMENTS?.value).isEqualTo("0")
    }


    @Test
    fun `test decrement count 1k remains 1k`() {
        assertThat(Counts2(COMMENTS = EntityConfig2("1k")).decrementCommentCount().COMMENTS?.value).isEqualTo("1k")
    }

    @Test
    fun `test primsect`() {
        val EN = "en"
        val TE = "te"
        val EN_TE = "en, te"

        expect.that(langsFirst(null)).isEqualTo(null)
        expect.that(langsFirst("")).isEqualTo("")
        expect.that(langsFirst(EN)).isEqualTo(EN)
        expect.that(langsFirst(EN_TE)).isEqualTo(EN)

        expect.that(langsRest(null)).isEqualTo(null)
        expect.that(langsRest("")).isEqualTo("")
        expect.that(langsRest(EN)).isEqualTo("")
        expect.that(langsRest(EN_TE)).isEqualTo(TE)

        //langs have more spaces and extra commas
        val langs = "$EN_TE,, hi  ,,    "
        expect.that(langsFirst(langs)).isEqualTo(EN)
        expect.that(langsRest(langs)).isEqualTo("te,hi")
    }

    @Test
    fun testUrlUtilQueryParam_replacesExistingParams() {
        val url = "http://qa-news.newshunt.com/api/v2/posts/article/content/dhfc2873b69414440eb42499fe7a1dadef_088044a641bd4eeb892ed8f95a12b219?useWidgetPosition=false&feedConfigKey=USER_POST"
        val finalUrl = "http://qa-news.newshunt.com/api/v2/posts/article/content/dhfc2873b69414440eb42499fe7a1dadef_088044a641bd4eeb892ed8f95a12b219?useWidgetPosition=true&feedConfigKey=USER_POST&a=b"
        assertThat(UrlUtil.getUrlWithQueryParamns(url, hashMapOf("a" to "b", "useWidgetPosition" to "true"))).isEqualTo(finalUrl)
    }


    @Test
    fun testNextCalDay() {
        // msec taken from https://currentmillis.com/
        val t2020_03_31 = 1585596601000
        val t2020_04_01 = 1585683001000
        val t2020_12_30 = 1609270201000
        val t2020_12_31 = 1609356601000
        val t2021_01_01 = 1609443001000
        val t2021_01_02 = 1609529401000
        val t2021_01_03 = 1609615801000
        val t2021_12_30 = 1640806201000
        val t2021_12_31 = 1640892601000
        val t2022_12_31 = 1672428601000
        Assert.assertTrue(isNextCalDay(t2021_01_01, t2021_01_02))
        Assert.assertTrue(isNextCalDay(t2020_12_31, t2021_01_01))
        Assert.assertTrue(isNextCalDay(t2020_12_30, t2020_12_31))
        Assert.assertTrue(isNextCalDay(t2020_03_31, t2020_04_01))
        Assert.assertFalse(isNextCalDay(t2021_01_01, t2021_01_03))
        Assert.assertFalse(isNextCalDay(t2020_12_30, t2020_12_30))
        Assert.assertFalse(isNextCalDay(t2020_12_30, t2021_12_31))
        Assert.assertFalse(isNextCalDay(t2021_01_02, t2021_01_01))
        Assert.assertFalse(isNextCalDay(t2021_12_30, t2022_12_31))
    }

    @Test
    fun testNextXCalDay() {
        // msec taken from https://currentmillis.com/
        val t2020_03_31 = 1585596601000
        val t2020_04_01 = 1585683001000
        val t2020_12_30 = 1609270201000
        val t2020_12_31 = 1609356601000
        val t2021_01_01 = 1609443001000
        val t2021_01_02 = 1609529401000
        val t2021_01_03 = 1609615801000
        val t2021_12_30 = 1640806201000
        val t2021_12_31 = 1640892601000
        val t2022_12_31 = 1672428601212
        val t2020_02_29 = 1582914600000
        val t2020_03_02 = 1583087400000
        val t2020_06_30 = 1593455400000
        val t2020_07_02 = 1593628200000
        val t2020_07_05 = 1593973799999
        Assert.assertTrue(isAtleastNextXCalDay(t2021_01_01, t2021_01_02,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_12_31, t2021_01_01,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_12_30, t2020_12_31,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_03_31, t2020_04_01,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2021_01_01, t2021_01_03,1))
        Assert.assertFalse(isAtleastNextXCalDay(t2020_12_30, t2020_12_30,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_12_30, t2021_12_31,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2021_01_01, t2021_01_02,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2021_12_30, t2022_12_31,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_12_31,t2021_01_02,2))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_12_31,t2021_01_02,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_02_29,t2020_03_02,2))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_02_29,t2020_03_02,1))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_06_30,t2020_07_02,2))
        Assert.assertFalse(isAtleastNextXCalDay(t2020_06_30,t2020_07_02,3))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_12_30, t2021_12_31,366))
        Assert.assertTrue(isAtleastNextXCalDay(t2020_07_02, t2020_07_05,3))
    }
}