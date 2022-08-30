/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.dataentity.common.asset.CardLabel2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LinkAsset
import com.newshunt.dataentity.common.asset.PostDisplayType
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.PostSourceType
import com.newshunt.dataentity.common.asset.VideoAsset
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.TopLevelCard
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
/**
*
* @author satosh.dhanyamraju
*/
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class CardsBindingUtilsTest {

    @Test
    fun liveTagVisibilityTest() {
        val card: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(id = "2", videoAsset = VideoAsset(liveStream = true)).toCard2(1)))
        Assert.assertTrue(CardsBindUtils.canShowLiveTag(card))
        val card2: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(id = "2", videoAsset = VideoAsset(liveStream = false)).toCard2(1)))
        Assert.assertFalse(CardsBindUtils.canShowLiveTag(card2))
        val card3: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(id = "2", videoAsset = VideoAsset(liveStream = true), showVideoIcon = true).toCard2(1)))
        Assert.assertFalse(CardsBindUtils.canShowLiveTag(card3))
    }

    @Test
    fun `creator badge visibility test`() {
        Assert.assertTrue(CardsBindUtils.canShowCreatorBadge(PostSourceAsset(type = PostSourceType.ICC.name)))
        Assert.assertTrue(CardsBindUtils.canShowCreatorBadge(PostSourceAsset(type = PostSourceType
                .OGC.name)))
        Assert.assertTrue(CardsBindUtils.canShowCreatorBadge(PostSourceAsset(type = PostSourceType
                .PGC.name)))
        Assert.assertFalse(CardsBindUtils.canShowCreatorBadge(PostSourceAsset(type = PostSourceType
                .UGC.name)))
    }

    @Test
    fun `big source image visibility`() {
        Assert.assertTrue(CardsBindUtils.showBigSourceImage(TopLevelCard(postEntity =
        AllLevelCards(PostEntity(source = PostSourceAsset(entityImageUrl = "www.img.url"))
                .toCard2(1)))))
        Assert.assertFalse(CardsBindUtils.showBigSourceImage(TopLevelCard(postEntity = AllLevelCards(PostEntity(source = PostSourceAsset(icon = "www.img.url")).toCard2(1)))))
    }

    @Test
    fun `repost og visibility`() {
        Assert.assertTrue(CardsBindUtils.showRepostOg(PostDisplayType.REPOST_OG.index))
        Assert.assertFalse(CardsBindUtils.showRepostOg(PostDisplayType.SIMPLE_POST.index))
    }


    @Test
    fun `source header visibility`() {
        Assert.assertTrue(CardsBindUtils.showBigSourceImage(TopLevelCard(postEntity =
        AllLevelCards(PostEntity(source = PostSourceAsset(entityImageUrl = "www.img.url"))
                .toCard2(1)))))
        Assert.assertFalse(CardsBindUtils.showBigSourceImage(TopLevelCard(postEntity =
        AllLevelCards(PostEntity().toCard2(1)))))
    }

    @Test
    fun `breaking news tag visibility`() {
        val card: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(id = "2",
                publishTime = System.currentTimeMillis() - 1000, cardLabel = CardLabel2(ttl = 10000))
                .toCard2(1)))
        Assert.assertTrue(CardsBindUtils.canShowBreakingNewsTag(card))
        val card2: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(id = "2")
                .toCard2(1)))
        Assert.assertFalse(CardsBindUtils.canShowBreakingNewsTag(card2))
    }

    @Test
    fun `play icon visibility`() {
        val card: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(id = "2",
                showVideoIcon = true).toCard2(1)))
        Assert.assertTrue(CardsBindUtils.canShowPlayIcon(card))
        val card1: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(id = "2",
                videoAsset = VideoAsset(liveStream = false), showVideoIcon = false).toCard2(1)))
        Assert.assertTrue(CardsBindUtils.canShowPlayIcon(card1))
        val card2: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(id = "2", videoAsset = VideoAsset(liveStream = true), showVideoIcon = false).toCard2(1)))
        Assert.assertFalse(CardsBindUtils.canShowPlayIcon(card2))
        val card3 = LinkAsset(type = "VIDEO")
        Assert.assertTrue(CardsBindUtils.canShowPlayIcon(card3))
    }


    @Test
    fun `repost poll visibility`() {
        val card: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(format =
        Format.POLL).toCard2(1)))
        Assert.assertTrue(CardsBindUtils.showRepostPoll(card))
        val card2: CommonAsset = TopLevelCard(postEntity = AllLevelCards(PostEntity(format =
        Format.HTML).toCard2(1)))
        Assert.assertFalse(CardsBindUtils.showRepostPoll(card2))
    }
}