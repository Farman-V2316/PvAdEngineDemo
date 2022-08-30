/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *
 */

package com.newshunt.appview.common.ui.helper

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Expect
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appconfig.AppConfigBuilder
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.EntityConfig2
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
*
* @author satosh.dhanyamraju
*/
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class LikeEmojiBindingUtilsTest {

    @Rule
    @JvmField
    val expect = Expect.create()
    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        AppConfig.createInstance(AppConfigBuilder())
    }

    @Test
    fun test_likeLayoutSmileSadAndAngryCounts() {
        val cnt = Counts2(
                LIKE = EntityConfig2("24", 0),
                ANGRY = EntityConfig2("2", 0)
        )
        expect.that(LikeEmojiBindingUtils.likeLayoutSmileSadAndAngryCounts(PostEntity(counts = cnt)))
                .asList().containsExactly(24, 2 , 0) // null is 0

        val strVal = EntityConfig2(value = "24K")
        //if any of them is not parseable to string, empty array is returned
        expect.that(LikeEmojiBindingUtils.likeLayoutSmileSadAndAngryCounts(PostEntity(counts = Counts2(LIKE = strVal)))).isEmpty()
        expect.that(LikeEmojiBindingUtils.likeLayoutSmileSadAndAngryCounts(PostEntity(counts = Counts2(LOVE = strVal)))).isEmpty()
        expect.that(LikeEmojiBindingUtils.likeLayoutSmileSadAndAngryCounts(PostEntity(counts = Counts2(HAPPY = strVal)))).isEmpty()
        expect.that(LikeEmojiBindingUtils.likeLayoutSmileSadAndAngryCounts(PostEntity(counts = Counts2(WOW = strVal)))).isEmpty()
        expect.that(LikeEmojiBindingUtils.likeLayoutSmileSadAndAngryCounts(PostEntity(counts = Counts2(SAD = strVal)))).isEmpty()
        expect.that(LikeEmojiBindingUtils.likeLayoutSmileSadAndAngryCounts(PostEntity(counts = Counts2(ANGRY = strVal)))).isEmpty()
    }
}