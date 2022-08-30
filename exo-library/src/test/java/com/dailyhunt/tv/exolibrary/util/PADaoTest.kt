/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.dailyhunt.tv.exolibrary.util

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * For testing the queries written in [PADao].
 *
 * [PATest] also covers some functionality of [PADao]
 *
 * For each test, creates an in-memory db, and runs test on it.
 *
 * @author satosh.dhanyamraju
 */
@RunWith(AndroidJUnit4::class)
class PADaoTest {
  lateinit var dao : PADao
  val uid = 1L
  val uri: String = "abc.m3u8"


  @Before
  fun setUp() {
    dao = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PADB::class
        .java)
        .allowMainThreadQueries().build().dao()
  }

  @Test
  fun testStates() {
    val state = PlayerState(uid, 1, 1)
    val state2 = PlayerState(uid, 2, 1)
    dao.add(state)
    dao.add(state2)
    assertThat(dao.allStates(uid)).containsExactly(state, state2)
  }

  @Test
  fun testLoadEntryInsertion() {
    val le = LoadEntry(uid, uri, 5L)
    assertThat(dao.allLoadEntries()).isEmpty()

    dao.add(le)

    assertThat(dao.allLoadEntries()).isNotEmpty()
    assertThat(dao.allLoadEntries(uid)).containsExactly(le)
  }

  @Test
  fun testLoadEntryDeletion() {
    val le = LoadEntry(uid, uri, 5L)
    dao.add(le)
    assertThat(dao.allLoadEntries()).contains(le)

    dao.deleteLoadEntries(uid)

    assertThat(dao.allLoadEntries(uid)).doesNotContain(le)
  }



  @Test
  fun testLoadEntryUpdation() {
    val le = LoadEntry(uid, uri, 5L)
    dao.add(le)

    dao.updateLoadEntry(uri, 10, 2)

    val allLoadEntries = dao.allLoadEntries(uid)
    assertThat(allLoadEntries).hasSize(1)
    assertThat(allLoadEntries?.first()?.timeFinish).isEqualTo(10)
    assertThat(allLoadEntries?.first()?.finishType).isEqualTo(2)
  }

  @Test
  fun testLoadEntryUpdationDoesNotUpdateEndTimeIfExists() {
    val le = LoadEntry(uid, uri, 5L)
    val le1 = LoadEntry(uid, uri, 6L, 7, 4)
    dao.add(le, le1)

    dao.updateLoadEntry(uri, 10, 2)

    val allLoadEntries = dao.allLoadEntries(uid)
    assertThat(allLoadEntries).hasSize(2)
    val itemToBeUpdated = allLoadEntries?.find {
      it?.timeStart?: -1 == 5L
    }!!
    val itemShouldNotBeUpdated = allLoadEntries.find {
      it?.timeStart?: -1 == 6L
    }!!

    assertThat(itemToBeUpdated.timeFinish).isEqualTo(10)
    assertThat(itemToBeUpdated.finishType).isEqualTo(2)
    assertThat(itemShouldNotBeUpdated.timeFinish).isEqualTo(7)
    assertThat(itemShouldNotBeUpdated.finishType).isEqualTo(4)
  }

  @Test
  fun testLoadEventSummary() {
    dao.add(LoadEntry(uid, uri, 5, 15,1),
    LoadEntry(uid, uri, 35,40,2),
    LoadEntry(uid, uri, 45,100,3),
    LoadEntry(uid, "d", 46, 48, 2),
    LoadEntry(uid, "d1", 47,48, 3),
    LoadEntry(uid, "d", 48, 55,3),
    LoadEntry(uid, "d2", 200),
    LoadEntry(uid, uri, 201, 231, 1),
    LoadEntry(uid+1, uri, 202, 232, 1))

    assertThat(dao.loadEntrySummary(uid)).containsExactlyElementsIn(listOf(
        LoadEntryAgg(uri = uri, attempts = 4, totalTime = 40, ends = 2, errors = 1, cancells = 1),
        LoadEntryAgg(uri = "d", attempts = 2, errors = 1, cancells = 1),
        LoadEntryAgg(uri = "d1", attempts = 1, cancells = 1),
        LoadEntryAgg(uri = "d2", attempts = 1, incompletes = 1)
    ))
  }


  @Test
  fun testDownStreamFormatChanges() {
    val f1  = FormatChange(uid, 1,1,20, "video/h24")
    val f2  = FormatChange(uid, 11,1,25, "video/h24")
    val f3  = FormatChange(uid, 15,1,23, "video/h24")
    val f4 = f3.copy(uniqueId = 2)

    dao.add(f1, f2, f3, f4)
    assertThat(dao.formatChangeCount(uniqueId = uid)).isEqualTo(2)
    assertThat(dao.timeTakenForFirstFormatChange(uid)).isEqualTo(10)

  }

  @Test
  fun testDownstreamFormatChangeReturnsNullIfNoMatchingRows() {
    val f1  = FormatChange(uid, 1,1,20, "video/h24")
    dao.add(f1)

    assertThat(dao.formatChangeCount(1)).isEqualTo(0)
    assertThat(dao.formatChangeCount(2)).isEqualTo(-1)

    assertThat(dao.timeTakenForFirstFormatChange(1)).isNull()
    assertThat(dao.timeTakenForFirstFormatChange(2)).isNull()
  }

  @Test
  fun testNonUniqueInserts() {
    dao.add(LoadEntry(uid, uri, 5))
    dao.add(LoadEntry(uid, uri, 5))
    // does not throw exception - resolution strategy REPLACE
  }
}