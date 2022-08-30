/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.newshunt.dataentity.common.asset.CountType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.Interaction
import com.newshunt.dataentity.social.entity.InteractionPayload
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.news.model.daos.InteractionDao
import com.newshunt.news.model.sqlite.SocialDB
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [27])
class InteractionDaoTest {

    private val unsyncDelLike1 = Interaction("KHsnthusnahHTUDnHSenthknsqth", "OGC", "LIKE", false, false)

    private val syncAddLike3 = Interaction("KHsnthusnuahHTUeDnHSenthknsqth", "OGC", "LIKE", true, true)

    private val unsyncAddLike4 = Interaction("KHsnthusnahHTUeDnHSenthknsqth", "OGC", "LIKE", true, false)

    private val unsyncDelLike2 = Interaction("KHsnthusnOOahHTUDnHSenthknsqth", "UGC", "LIKE", false, false)

    val unsyncShare = Interaction("KHsnthusnOOahHTUDnHSenthknsqth", "UGC", CountType.SHARE.name, true, false)
    val syncShare = Interaction("KHsnthusnOOahHTUDnHSenthknsqth", "UGC", CountType.SHARE.name, true, true)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    lateinit var dao: InteractionDao
    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        dao = instance.interactionsDao()
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    @Test
    fun testIns() {
        val test = dao._allInteractions().test()
        dao._insIgnore(unsyncDelLike1)
        dao._insIgnore(unsyncDelLike2)

        test.assertValues(listOf(), listOf(unsyncDelLike1), listOf(unsyncDelLike1, unsyncDelLike2))
    }

    @Test
    fun testNoDuplicates() {
        val test = dao._allInteractions().test()
        dao._insIgnore(unsyncDelLike1)
        dao._insIgnore(unsyncDelLike1.copy(actionToggle = true)) // this should be ignored

        test.assertValues(listOf(), listOf(unsyncDelLike1))
    }

    @Test
    fun toggleLikeOnNonexistentRowInsertsCorrectly() {

        dao.toggleLike(unsyncDelLike1.entityId, unsyncDelLike1.entityType, unsyncDelLike1.action)

        val interaction = dao._allInteractions().test().values().get(0).get(0).copy(ts = 0)

        Truth.assertThat(interaction).isEqualTo(
                Interaction(unsyncDelLike1.entityId, unsyncDelLike1.entityType, unsyncDelLike1.action, true, false, 0)
        )
    }

    @Test
    fun toggleLikeOnExsitingRowWillToggleAndMarkUnsynced() {

        dao._insIgnore(unsyncDelLike1)
        dao.toggleLike(unsyncDelLike1.entityId, unsyncDelLike1.entityType, unsyncDelLike1.action)

        val interaction = dao._allInteractions().test().values().get(0).get(0).copy(ts = 0)
        Truth.assertThat(interaction).isEqualTo(
                Interaction(unsyncDelLike1.entityId, unsyncDelLike1.entityType, unsyncDelLike1.action, true, false, 0)
        )
    }

    @Test
    fun toggleLikeDouble() {
        dao.toggleLike(unsyncDelLike1.entityId, unsyncDelLike1.entityType, unsyncDelLike1.action)
        dao.toggleLike(unsyncDelLike1.entityId, unsyncDelLike1.entityType, unsyncDelLike1.action)

        val interaction = dao._allInteractions().test().values().last().get(0).copy(ts = 0)
        Truth.assertThat(interaction).isEqualTo(
                Interaction(unsyncDelLike1.entityId, unsyncDelLike1.entityType, unsyncDelLike1.action, false, false, 0)
        )
    }

    @Test
    fun toggle2DifferentLikeTypesWillOnlyRemoves1stOneAndAdds2ndOne() {
        val syncAddAngry = syncAddLike3.copy(action = LikeType.ANGRY.name)
        dao.toggleLike(syncAddLike3.entityId, syncAddLike3.entityType, syncAddLike3.action)
        dao.toggleLike(syncAddAngry.entityId, syncAddAngry.entityType, syncAddAngry.action)

        val interactions = dao._allInteractions().test().values().last().map { it.copy(ts = 0) }
        Truth.assertThat(interactions)
                .containsExactly(syncAddLike3.copy(ts = 0, actionToggle = true, isSynced = false, action = LikeType.ANGRY.name))
    }

    @Test
    fun toggleLikeOnWouldChangeEmoji() {
        val likeOff = Interaction("K", "OGC", "LIKE", false, false)
        val angryOff = Interaction("K", "OGC", "ANGRY", false, false)
        dao.toggleLike(likeOff.entityId, likeOff.entityType, likeOff.action) // ON
        dao.toggleLike(likeOff.entityId, likeOff.entityType, likeOff.action) // OFF
        dao.toggleLike(angryOff.entityId, angryOff.entityType, angryOff.action) // ON

        val interactions = dao._allInteractions().test().values().last().map { it.copy(ts = 0) }
        Truth.assertThat(interactions)
                .containsExactly(angryOff.copy(ts = 0, actionToggle = true, isSynced = false))


    }

    @Test
    fun toggleOffWouldToggleDifferentEmoji() {
        val likeOff = Interaction("K", "OGC", "LIKE", false, false)
        val angryOff = Interaction("K", "OGC", "ANGRY", false, false)
        dao.toggleLike(likeOff.entityId, likeOff.entityType, likeOff.action) // ON
        dao.toggleLike(angryOff.entityId, angryOff.entityType, angryOff.action) // OFF

        val interactions = dao._allInteractions().test().values().last().map { it.copy(ts = 0) }

        Truth.assertThat(interactions)
                .containsExactly(likeOff.copy(ts = 0, actionToggle = true, isSynced = false, action = angryOff.action))
    }

    @Test
    fun _toggleWithoutRowIsNoop() {
        dao._toggleLike(syncAddLike3.entityId, syncAddLike3.entityType, syncAddLike3.action)

        Truth.assertThat(dao._allInteractions().test().values().last())
                .isEmpty()
    }

    @Test
    fun testShareInsertsMultiple() {

        val id = "unsyncDelLike1"
        val type = "ogc"
        dao.share(id, type)
        dao.share(id, type)

        val list = dao._allInteractions().test().values().last().filter{
            it.action == CountType.SHARE.name
        }?.map { it.copy(ts = 0, shareTs = 0) }

        Truth.assertThat(list).containsExactly(
                Interaction(id, type, CountType.SHARE.name, true, false, 0, 0),
                Interaction(id, type, CountType.SHARE.name, true, false, 0, 0)
        )

    }

    @Test
    fun testmarkSyncedItems() {
        dao._insIgnore(unsyncDelLike1.copy(ts = 999))
        dao._insIgnore(unsyncDelLike2.copy(ts = 1001, action = "like"))
        dao._insIgnore(unsyncShare.copy(ts = 999))

        dao.markSyncedItems(1000, listOf("like", CountType.SHARE.name))

        val last = dao._allInteractions().test().values().last()

        Truth.assertThat(last.map { it.isSynced }).containsExactly(false/*unsyncDelLike1 not toggled, its type is 'Like'*/, false/*unsyncDelLike2 not toggled. sync time*/, true)
    }


    @Test
    fun test_unsyncedAddedLikes() {
        dao._insIgnore(unsyncDelLike1, syncAddLike3, unsyncAddLike4)

        val last = dao.unsyncedAddedLikes()

        Truth.assertThat(last).containsExactly(unsyncAddLike4.toPayloadEntry())
    }

    @Test
    fun test_unsyncedDeletedLikes() {
        dao._insIgnore(unsyncDelLike1, syncAddLike3, unsyncAddLike4, unsyncDelLike2)

        val last = dao.unsyncedDeletedLikes()

        Truth.assertThat(last).containsExactly(unsyncDelLike1.toPayloadEntry(), unsyncDelLike2.toPayloadEntry()).inOrder()
    }

    @Test
    fun test_unsyncedShares() {
        dao._insIgnore(unsyncShare, syncShare, unsyncDelLike2)

        val last = dao.unsyncedShares()

        Truth.assertThat(last).containsExactly(unsyncShare.toPayloadEntry()).inOrder()
    }


    private fun Interaction.toPayloadEntry() = InteractionPayload.InteractionPayloadItem(
            entityId, entityType, action, ts ?: 0)

}