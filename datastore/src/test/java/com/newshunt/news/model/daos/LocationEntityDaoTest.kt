/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.LocationIdChild
import com.newshunt.dataentity.common.asset.LocationIdParent
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.utils.LiveDataTestUtil
import com.newshunt.news.model.utils.samplePageEntity
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
class LocationEntityDaoTest {
    private lateinit var locationEntityDao: LocationEntityDao
    private lateinit var followEntityDao: FollowEntityDao
    private val AP = Location(buildPE("ap"), null)
    private val UP = Location(buildPE("up"), null)
    private val VTZ = Location(buildPE("vtz"), "ap")
    private val VZM = Location(buildPE("vzm"), "ap")


    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
        val instance = SocialDB.instance(ApplicationProvider.getApplicationContext(), true)
        locationEntityDao = instance.locationsDao()
        followEntityDao = instance.followEntityDao()
    }

    @After
    fun tearDown() {
        SocialDB.closeConnection()
    }

    @Test
    fun testNested() {
        fun locs() = LiveDataTestUtil.getValue(locationEntityDao.getLocationsNested())
        assertThat(locs()).isEmpty()
        insertSomeLocs()
        assertThat(locs()).hasSize(2)
        assertThat(locs()?.get(0)?.parent?.id).isEqualTo(AP.id)
        assertThat(locs()?.get(0)?.kids?.map { it.id }).containsExactly(VTZ.id, VZM.id)
        assertThat(locs()?.get(1)?.parent?.id).isEqualTo(UP.id)
        assertThat(locs()?.get(1)?.kids?.map { it.id }).isEmpty()
    }

    @Test
    fun testToggle() {
        fun locs() = LiveDataTestUtil.getValue(locationEntityDao.getLocationsNested())
        insertSomeLocs()

        fun parent() = locs()?.get(0)?.parent
        fun _0th_kid() = locs()?.get(0)?.kids?.get(0)


        assertThat(parent()?.id).isEqualTo(AP.id)
        assertThat(parent()?.isFollowed).isFalse()
        assertThat(_0th_kid()?.id).isEqualTo(VTZ.id)
        assertThat(_0th_kid()?.isFollowed).isFalse()

        // toggle the parent
        followEntityDao.toggleFollowItems(FollowSyncEntity(ActionableEntity(AP.id,""), action = FollowActionType.FOLLOW))
        assertThat(parent()?.id).isEqualTo(AP.id)
        assertThat(parent()?.isFollowed).isTrue()
        assertThat(_0th_kid()?.id).isEqualTo(VTZ.id)
        assertThat(_0th_kid()?.isFollowed).isFalse()

        //toggle the kid
        followEntityDao.toggleFollowItems(FollowSyncEntity(ActionableEntity(VTZ.id,""), action = FollowActionType.FOLLOW))
        assertThat(parent()?.id).isEqualTo(AP.id)
        assertThat(parent()?.isFollowed).isTrue()
        assertThat(_0th_kid()?.id).isEqualTo(VTZ.id)
        assertThat(_0th_kid()?.isFollowed).isTrue()

        // again toggle the parent
        followEntityDao.toggleFollowItems(FollowSyncEntity(ActionableEntity(AP.id,""), action = FollowActionType.FOLLOW))
        assertThat(parent()?.id).isEqualTo(AP.id)
        assertThat(parent()?.isFollowed).isFalse()
        assertThat(_0th_kid()?.id).isEqualTo(VTZ.id)
        assertThat(_0th_kid()?.isFollowed).isTrue()

        //again toggle the kid
        followEntityDao.toggleFollowItems(FollowSyncEntity(ActionableEntity(VTZ.id,""), action = FollowActionType.FOLLOW))
        assertThat(parent()?.id).isEqualTo(AP.id)
        assertThat(parent()?.isFollowed).isFalse()
        assertThat(_0th_kid()?.id).isEqualTo(VTZ.id)
        assertThat(_0th_kid()?.isFollowed).isFalse()
    }

    @Test
    fun testlookup() {
        insertSomeLocs()
        val readFromIds = locationEntityDao.readFromIds(listOf(LocationIdParent(AP.id, listOf(LocationIdChild(VTZ.id)))))
        assertThat(readFromIds).hasSize(1)
        assertThat(readFromIds.get(0).parent?.id).isEqualTo(AP.id)
        assertThat(readFromIds.get(0).kids).hasSize(1)
        assertThat(readFromIds.get(0).kids?.get(0)?.id).isEqualTo(VTZ.id)

    }

    private fun insertSomeLocs() {
        locationEntityDao.insIgnore(AP, UP, VTZ, VZM)
    }

    private fun buildPE(name: String): PageEntity {
       return samplePageEntity.pageEntity.copy(id = name, name = name)
    }
}