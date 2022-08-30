/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.social.entity.CardsPayload
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.repo.FollowRepo
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
class FollowDaoTest {


    @get:Rule
    val rule = InstantTaskExecutorRule()

    lateinit var dao: FollowEntityDao
    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        dao = instance.followEntityDao()
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    val fe1 = FollowSyncEntity(ActionableEntity("id", "et", null, displayName = "n", entityImageUrl = "", handle = "", deeplinkUrl = "", iconUrl = ""), FollowActionType.FOLLOW)
    val fe2 = FollowSyncEntity(ActionableEntity("id2", "et", null, displayName = "n", entityImageUrl = "", handle = "", deeplinkUrl = "", iconUrl = ""), FollowActionType.FOLLOW)
    val fe3 = FollowSyncEntity(ActionableEntity("id3", "et", null, displayName = "n", entityImageUrl = "", handle = "", deeplinkUrl = "", iconUrl = ""), FollowActionType.FOLLOW)
    val fe4 = FollowSyncEntity(ActionableEntity("id4", "et", null, displayName = "n", entityImageUrl = "", handle = "", deeplinkUrl = "", iconUrl = ""), FollowActionType.FOLLOW)
    val fe5 = FollowSyncEntity(ActionableEntity("id4", "et", null, displayName = "n", entityImageUrl = "", handle = "", deeplinkUrl = "", iconUrl = ""), FollowActionType.BLOCK)


    @Test
    fun testIns() {

        dao.insReplace(fe1)
        val follows = dao.getFollowList()
        Truth.assertThat(follows)
                .containsExactly(fe1)
    }

    @Test
    fun testToggleInsertsIfNotInDB() {
        val followRepo = FollowRepo(followEntityDao = dao)
        followRepo.toggleFollow(fe1.actionableEntity, FollowActionType.FOLLOW.name).test()
//        val enti = dao.lookup(fe1.id, fe1.followType.name) ?: throw Exception("expected not null")
        val followEntityFromDB = dao.getFollowEntity(fe1.actionableEntity.entityId)
        Truth.assertThat(followEntityFromDB).isEqualTo(fe1.copy(actionTime = followEntityFromDB!!.actionTime))
    }

    @Test
    fun testToggleChangeFlagIfPresentInDB() {
        dao.insReplace(fe1)
        /*      assertThat(dao.getFollowEntity(fe1.id, fe1.followType.name)?.isFollowin).isTrue()
              dao.toggle(fe1)
              assertThat(dao.lookup(fe1.id, fe1.followType.name)?.isFollowin).isFalse()
              dao.toggle(fe1)
              assertThat(dao.lookup(fe1.id, fe1.followType.name)?.isFollowin).isTrue()
      */
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.FOLLOW)
        dao.toggleFollowEntity(fe1.actionableEntity.entityId)
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.UNFOLLOW)
        dao.toggleFollowEntity(fe1.actionableEntity.entityId)
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.FOLLOW)
    }

    @Test
    fun testToggle_EmptyToFollow() {
        dao.toggleFollowItems(fe1)
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.FOLLOW)
    }
    @Test
    fun testToggle_EmptyToBlock() {
        dao.toggleFollowItems(fe1.copy(action = FollowActionType.BLOCK))
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.BLOCK)
    }
    @Test
    fun testToggle_FollowBecomesUnFollowForBlock() {
        dao.insReplace(fe1)
        dao.toggleFollowItems(fe1.copy(action = FollowActionType.BLOCK))
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.UNFOLLOW)
    }
    @Test
    fun testToggle_BlockToFollow() {
        dao.insReplace(fe1.copy(action = FollowActionType.BLOCK))
        dao.toggleFollowItems(fe1.copy(action = FollowActionType.FOLLOW))
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.FOLLOW)
    }
    @Test
    fun testToggle_FollowToUnFollow() {
        dao.insReplace(fe1)
        dao.toggleFollowItems(fe1.copy(action = FollowActionType.FOLLOW))
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.UNFOLLOW)
    }

    @Test
    fun testToggle_UnFollowToFollow() {
        dao.insReplace(fe1.copy(action = FollowActionType.UNFOLLOW))
        dao.toggleFollowItems(fe1.copy(action = FollowActionType.FOLLOW))
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.FOLLOW)
    }
    @Test
    fun testToggle_BlockToUnblock() {
        dao.insReplace(fe1.copy(action = FollowActionType.BLOCK))
        dao.toggleFollowItems(fe1.copy(action = FollowActionType.BLOCK))
        Truth.assertThat(dao.getFollowEntity(fe1.actionableEntity.entityId)?.action).isEqualTo(FollowActionType.UNBLOCK)
    }


    @Test
    fun testRecentFollowsReturnsUnion() {
        fun FollowSyncEntity.e_id() = actionableEntity.entityId
        fun List<CardsPayload.P_Follow>.ids() = map{it.id}
        dao.insReplace(
                fe1.copy(actionTime =  5),
                fe2.copy(actionTime = 6),
                fe3.copy(actionTime = 2),
                fe4.copy(actionTime = 1),
                fe5.copy(actionTime = 6) // not follow
        )

        // only by time
        assertThat(dao.recentActions(5).ids())
                .containsExactly(fe2.e_id(), fe1.e_id()).inOrder()
    }


    @Test
    fun testFollowNamesGivesEmpty() {
        dao.getFollowedNames(Long.MIN_VALUE).test().assertValueAt(0) {
            it.isEmpty()
        }
    }


    @Test
    fun testFollowNamesToggleFollowBehavior() {
        val obs1 = dao.getFollowedNames(Long.MIN_VALUE).test()

        obs1.assertValueAt(0) {it.isEmpty()}

        dao.toggleFollowItems(fe1)
        obs1.assertValueAt(1) {it.map { it.actionableEntity.displayName } == listOf(fe1.actionableEntity.displayName)}

        dao.toggleFollowItems(fe2)
        obs1.assertValueAt(2) {it.map { it.actionableEntity.displayName } == listOf(fe2.actionableEntity.displayName)}

        dao.toggleFollowItems(fe1)
        obs1.assertValueAt(3) {it.map { it.actionableEntity.displayName } == listOf(fe1.actionableEntity.displayName)}
    }
}