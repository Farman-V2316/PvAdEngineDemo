/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.SyncStatus
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.dataentity.model.entity.BookmarkEntity
import com.newshunt.dataentity.news.model.entity.MenuL1Filter
import com.newshunt.dataentity.social.entity.MenuDictionaryEntity1
import com.newshunt.dataentity.social.entity.MenuL1
import com.newshunt.dataentity.social.entity.MenuL2
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.Mp
import com.newshunt.news.model.daos.BookmarksDao
import com.newshunt.news.model.daos.FollowEntityDao
import com.newshunt.news.model.daos.MenuDao
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
class MenuDaoTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    lateinit var dao: MenuDao
    lateinit var followEntityDao: FollowEntityDao
    lateinit var bookmarksDao: BookmarksDao
    val eid = "oe"
    val eType = "oe"
    val F = Format.HTML
    val SF = SubFormat.STORY
    val UT = UiType2.NORMAL
    val L = MenuLocation.LIST

    val A = 1
    val B = 2L
    val al1 = MenuL1(id = "ida", clickAction = null, filter = null, postAction = null, browserUrl = null, isDislikeL2 = false)
    val al2 = MenuL2(id = "ida", content = "ahoeush")
    val aMD = MenuDictionaryEntity1(
            masterOptionsL1 = listOf(al1),
            masterOptionsL2 = listOf(al2),
            listL1Options = mapOf("$F/$SF/$UT" to listOf(Mp(al1.id, al2.id))),
            detailL1Options = mapOf("$F/$SF/$UT" to listOf(Mp(al1.id, al2.id)))
    )


    val bl1 = MenuL1(id = "idb", clickAction = null, filter = null, postAction = null, browserUrl = null, isDislikeL2 = false, dictionaryIdL1 = B)
    val bl2 = MenuL2(id = "idb", content = "ahoeush", dictionaryIdL2 = B)
    val bMD = MenuDictionaryEntity1(
            0,
            masterOptionsL1 = listOf(bl1),
            masterOptionsL2 = listOf(bl2),
            listL1Options = mapOf("$F/$SF/$UT" to listOf(Mp(bl1.id, bl2.id))),
            detailL1Options = mapOf("$F/$SF/$UT" to listOf(Mp(bl1.id, bl2.id)))
    )

    val cl1_1 = MenuL1(id = "idc1", clickAction = null, filter = null, postAction = null, browserUrl = null, isDislikeL2 = false, dictionaryIdL1 = B)
    val cl1_canblock = MenuL1(id = "idc2", clickAction = null, filter = MenuL1Filter.CAN_BLOCK.name, postAction = null, browserUrl = null, isDislikeL2 = false, dictionaryIdL1 = B)
    val cl1_canUnblock = MenuL1(id = "idc6", clickAction = null, filter = MenuL1Filter.CAN_UNBLOCK.name, postAction = null, browserUrl = null, isDislikeL2 = false, dictionaryIdL1 = B)
    val cl1_canUnfollow = MenuL1(id = "idc7", clickAction = null, filter = MenuL1Filter.CAN_UNFOLLOW
            .name, postAction = null, browserUrl = null, isDislikeL2 = false, dictionaryIdL1 = B)
    val cl1_canfollow = MenuL1(id = "idc3", clickAction = null, filter = MenuL1Filter.CAN_FOLLOW.name, postAction = null, browserUrl = null, isDislikeL2 = false, dictionaryIdL1 = B)
    val cl2 = MenuL2(id = "idc", content = "ahoeush", dictionaryIdL2 = B)
    val cMD = MenuDictionaryEntity1(
            0,
            masterOptionsL1 = listOf(cl1_1, cl1_canfollow, cl1_canblock, cl1_canUnblock, cl1_canUnfollow),
            masterOptionsL2 = listOf(cl2),
            listL1Options = mapOf("$F/$SF/$UT" to listOf(
                    Mp(cl1_1.id, cl2.id),
                    Mp(cl1_canblock.id, cl2.id),
                    Mp(cl1_canUnblock.id, cl2.id),
                    Mp(cl1_canUnfollow.id, cl2.id),
                    Mp(cl1_canfollow.id, cl2.id))),
            detailL1Options = mapOf("$F/$SF/$UT" to listOf(Mp(cl1_1.id, cl2.id)))
    )


    val dl1_canSave = MenuL1(id = "idc4", clickAction = null, filter = MenuL1Filter.CAN_SAVE.name,
            postAction = null, browserUrl = null, isDislikeL2 = false, dictionaryIdL1 = B)
    val dl1_canUnsave = MenuL1(id = "idc5", clickAction = null, filter = MenuL1Filter.CAN_UNSAVE
            .name,
            postAction = null, browserUrl = null, isDislikeL2 = false, dictionaryIdL1 = B)

    val dMD = MenuDictionaryEntity1(
            0,
            masterOptionsL1 = listOf(cl1_1, cl1_canfollow, cl1_canblock, dl1_canSave,
                    dl1_canUnsave),
            masterOptionsL2 = listOf(cl2),
            listL1Options = mapOf("$F/$SF/$UT" to listOf(
                    Mp(cl1_1.id, cl2.id),
                    Mp(cl1_canblock.id, cl2.id),
                    Mp(cl1_canfollow.id, cl2.id),
                    Mp(dl1_canSave.id, cl2.id),
                    Mp(dl1_canUnsave.id, cl2.id))),
            detailL1Options = mapOf("$F/$SF/$UT" to listOf(Mp(cl1_1.id, cl2.id)))
    )

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        dao = instance.menuDao()
        followEntityDao = instance.followEntityDao()
        bookmarksDao = instance.bookmarkDao()
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }


    @Test
    fun testEmptyResult() {
        assertThat(dao.optionsMatching1(Format.HTML, SubFormat.STORY, UiType2.NORMAL, "list",
                eid, eType, null, "", emptyList()).test().values().first()).isEmpty()
    }


    @Test
    fun testInsertAndFetch1Row() {
        val dA = dao.clearAndinsert1(aMD)
        val options = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, "", emptyList()).test().values().last()
        assertThat(options).hasSize(1)
        assertThat(options.first().menuL1).isEqualTo(al1.copy(dictionaryIdL1 = dA))
        assertThat(options.first().menuL2).isEqualTo(al2.copy(dictionaryIdL2 = dA))
    }

    @Test
    fun testDeleteAndInsertRow() {
        val observer = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, "", emptyList()).test()
        val dA = dao.clearAndinsert1(aMD)
        val dB = dao.clearAndinsert1(bMD)

        val v = observer.values()

        assertThat(v).hasSize(3)
        assertThat(v[0]).isEmpty()
        assertThat(v[1]).hasSize(1)
        assertThat(v[2]).hasSize(1)


        val menuOptionA = v[1].get(0)
        val menuOptionB = v[2].get(0)

        assertThat(menuOptionA.menuL1).isEqualTo(al1.copy(dictionaryIdL1 = dA))
        assertThat(menuOptionB.menuL1).isEqualTo(bl1.copy(dictionaryIdL1 = dB))
        assertThat(menuOptionA.menuL2).isEqualTo(al2.copy(dictionaryIdL2 = dA))
        assertThat(menuOptionB.menuL2).isEqualTo(bl2.copy(dictionaryIdL2 = dB))
    }

    @Test
    fun testcanfollowFilter() {
        dao.clearAndinsert1(cMD)
        followEntityDao.insReplace(FollowSyncEntity(ActionableEntity(eid, eType, null), FollowActionType.FOLLOW))
        val last = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, "", emptyList()).test().values().last()
        assertThat(last).hasSize(3)
        assertThat(last.map { it.menuL1.id }).containsExactly(cl1_1.id, cl1_canblock.id, cl1_canUnfollow.id)
    }

    @Test
    fun testcanBlockFilter() {
        dao.clearAndinsert1(cMD)
        followEntityDao.insReplace(FollowSyncEntity(ActionableEntity(eid, eType, null), FollowActionType.BLOCK))
        val last = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, "", emptyList()).test().values().last()
        assertThat(last).hasSize(3)
        assertThat(last.map { it.menuL1.id }).containsExactly(cl1_1.id, cl1_canfollow.id, cl1_canUnblock.id)
    }

    @Test
    fun testcanUnBlockFilter() {
        dao.clearAndinsert1(cMD)
        followEntityDao.insReplace(FollowSyncEntity(ActionableEntity(eid, eType, null), FollowActionType.UNBLOCK))
        val last = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, "", emptyList()).test().values().last()
        assertThat(last).hasSize(3)
        assertThat(last.map { it.menuL1.id }).containsExactly(cl1_1.id, cl1_canfollow.id, cl1_canblock.id)
    }

    @Test
    fun testcanUnFollowFilter() {
        dao.clearAndinsert1(cMD)
        followEntityDao.insReplace(FollowSyncEntity(ActionableEntity(eid, eType, null), FollowActionType.UNFOLLOW))
        val last = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, "", emptyList()).test().values().last()
        assertThat(last).hasSize(3)
        assertThat(last.map { it.menuL1.id }).containsExactly(cl1_1.id, cl1_canfollow.id, cl1_canblock.id)
    }

    @Test
    fun testIfFollowsIsEmptyAllRowsAreReturned() {
        dao.clearAndinsert1(cMD)
        val last = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, "", emptyList()).test().values().last()
        assertThat(last).hasSize(3)
        assertThat(last.map { it.menuL1.id }).containsExactly(cl1_1.id, cl1_canfollow.id, cl1_canblock.id)
    }

    @Test
    fun testToGetListOfL1AsPerKeysGiven() {
        val dA = dao.clearAndinsert1(cMD)
        val options = dao.optionMatchingKeys(listOf(cl1_1.id, cl1_canblock.id), null, null, null)
                .test().values().last()
        assertThat(options.size).isEqualTo(2)
        assertThat(options).contains(cl1_1.copy(dictionaryIdL1 = dA))
        assertThat(options).contains(cl1_canblock.copy(dictionaryIdL1 = dA))
    }

    @Test
    fun testIgnoreNotPresentKey() {
        val dA = dao.clearAndinsert1(cMD)
        val options = dao.optionMatchingKeys(listOf(cl1_1.id, "NOT_PRESENT_KEY"), null, null, null).test().values().last()
        assertThat(options.size).isEqualTo(1)
    }

    @Test
    fun testcanUnSaveFilter() {
        val postId = "p1"
        dao.clearAndinsert1(dMD)
        bookmarksDao.insReplace(
                BookmarkEntity(id = postId,
                        action = BookMarkAction.ADD,
                        timestamp = System.currentTimeMillis(),
                        format = F.name,
                        subFormat = SF.name,
                        syncStatus = SyncStatus.UN_SYNCED)
        )
        val last = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, postId, emptyList()).test().values().last()
        assertThat(last).hasSize(4)
        assertThat(last.map { it.menuL1.id }).containsExactly(
                cl1_1.id,
                cl1_canfollow.id,
                cl1_canblock.id,
                dl1_canUnsave.id)
    }

    @Test
    fun testcanSaveFilter() {
        val postId = "p1"
        dao.clearAndinsert1(dMD)
        bookmarksDao.insReplace(
                BookmarkEntity(id = postId,
                        action = BookMarkAction.DELETE,
                        timestamp = System.currentTimeMillis(),
                        format = F.name,
                        subFormat = SF.name,
                        syncStatus = SyncStatus.UN_SYNCED)
        )
        val last = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, postId, emptyList()).test().values().last()
        assertThat(last).hasSize(4)
        assertThat(last.map { it.menuL1.id }).containsExactly(
                cl1_1.id,
                cl1_canfollow.id,
                cl1_canblock.id,
                dl1_canSave.id)
    }

    @Test
    fun testcanSaveFilterNotMarkedItem() {
        /*
        * New item which is not marked in bookMark table
        * */
        val postId = "new_item"
        dao.clearAndinsert1(dMD)
        val last = dao.optionsMatching1(F, SF, UT, L.name, eid, eType, null, postId, emptyList()).test().values().last()
        assertThat(last).hasSize(4)
        assertThat(last.map { it.menuL1.id }).containsExactly(
                cl1_1.id,
                cl1_canfollow.id,
                cl1_canblock.id,
                dl1_canSave.id)
    }
}