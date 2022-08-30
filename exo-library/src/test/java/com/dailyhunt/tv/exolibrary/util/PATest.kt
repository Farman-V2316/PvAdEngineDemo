/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.dailyhunt.tv.exolibrary.util

import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.Player.STATE_IDLE
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.common.truth.Truth.assertThat
import com.newshunt.dataentity.common.helper.common.CommonUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Integration Tests PA and builder
 *
 * @author satosh.dhanyamraju
 */
@Config(sdk = [21], manifest = Config.NONE)
@RunWith(AndroidJUnit4::class)
class PATest {
  lateinit var db: PADB

  @Before
  fun setUp() {
    CommonUtils.setApplication(ApplicationProvider.getApplicationContext())
    db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PADB::class
        .java).allowMainThreadQueries().build()
  }

  @Test
  fun testTotalBuffer() {
    val pa = listenerDB().apply {
      onPlayerStateChanged(time(0), true, STATE_BUFFERING)
      onPlayerStateChanged(time(12), true, STATE_READY)
      onPlayerStateChanged(time(13), true, STATE_BUFFERING)
      onPlayerStateChanged(time(14), true, STATE_IDLE)
      onMediaPeriodReleased(time(20))
    }
    assertThat(pa.generatePA().totalBufferTime).isEqualTo(19)
  }

  @Test
  fun testTotalBuffer_NoBuffering() {
    val pa = listenerDB().apply {
      onPlayerStateChanged(time(12), true, STATE_READY)
      onPlayerStateChanged(time(15), true, STATE_READY)
      onMediaPeriodReleased(time(20))
    }
    assertThat(pa.generatePA().totalBufferTime).isEqualTo(0)
  }

  @Test
  fun testTotalPlaybackTime() {
    val pa = listenerDB().apply {
      onPlayerStateChanged(time(0), true, STATE_BUFFERING)
      onPlayerStateChanged(time(12), true, STATE_READY)
      onPlayerStateChanged(time(13), true, STATE_BUFFERING)
      onPlayerStateChanged(time(14), true, STATE_IDLE)
      onMediaPeriodReleased(time(25))
    }

    assertThat(pa.generatePA().totalPlaybackTime).isEqualTo(25)
    assertThat(listenerDB().generatePA().totalPlaybackTime).isEqualTo(C.TIME_UNSET) // edge-case
    pa.cleanup()
  }

  @Test
  fun testBitrateEstimateAtStateChange() {
    val pa = listenerDB().apply {
      onPlayerStateChanged(time(2), true, STATE_BUFFERING) //edge case
      onPlayerStateChanged(time(4), true, STATE_READY) //edge case
      onPlayerStateChanged(time(21), true, STATE_BUFFERING)
      onPlayerStateChanged(time(25), true, STATE_IDLE)
      onPlayerStateChanged(time(40), true, STATE_IDLE)

      onBandwidthEstimate(time(5), 1, 1L, 1005)
      onBandwidthEstimate(time(10), 1, 1L, 1010)
      onBandwidthEstimate(time(15), 1, 1L, 1015)
      onBandwidthEstimate(time(20), 1, 1L, 1020)
      onBandwidthEstimate(time(25), 1, 1L, 1025)
      onBandwidthEstimate(time(30), 1, 1L, 1030)
      onBandwidthEstimate(time(35), 1, 1L, 1035) // edge case
      //onBandwidthEstimate(time(55), 1, 1L, 1055)
      onMediaPeriodReleased(time(55)) // check this
    }

    assertThat(pa.generatePA().bitratesAtStateChanges).containsExactly(1005L, 1005L,
        1020L, 1025L, 1035L)
  }

  @Test
  fun testEmptyBwEstimates_FillWithDefaults() {
    val pa = listenerDB().apply {
      onPlayerStateChanged(time(100L), true, 2)
      onPlayerStateChanged(time(105L), true, 3)
    }
    assertThat(pa.generatePA().bitratesAtStateChanges).containsExactly(-1L, -1L)
  }

  @Test
  fun test_insertingDuplicatesDoesnotCrash() {
    listenerDB().apply {
      onPlayerStateChanged(time(100L), true, 2)
      onPlayerStateChanged(time(100L), true, 2)
    }
  }

  @Test
  fun testLoadingBasicFlow() {
    val info = loadInfo("abc.m3u8")
    val pa = listenerDB().apply {
      onLoadStarted(time(5), info, null)
      onLoadCompleted(time(15), info, null)
      onLoadStarted(time(35), info, null)
      onLoadCanceled(time(40), info, null)
      onLoadStarted(time(45), info, null)
      onLoadError(time(100), info, null, null, false)
      onMediaPeriodReleased(time(105))
    }

    val summary = pa.generatePA().loadSummary.first()
    assertThat(summary.attempts).isEqualTo(3)
    assertThat(summary.errors).isEqualTo(1)
    assertThat(summary.cancells).isEqualTo(1)
    assertThat(summary.ends).isEqualTo(1)
    assertThat(summary.totalTime).isEqualTo(10)
  }

  @Test
  fun test_periodReleaseClearsDataFromDBAfterCallingPassedFunction() {
    val pa = PAListener("tests", db.dao(), {
      System
          .currentTimeMillis()
    }, { it.run() }, true)

    pa.apply {
      onPlayerStateChanged(time(5),true, 1)
      onBandwidthEstimate(time(5),1, 1, 320000)
      onBandwidthEstimate(time(10),1, 1, 760000)
      onLoadStarted(time(5), loadInfo("abc.m3u8"), null)
      onLoadCompleted(time(15), loadInfo("abc.m3u8"), null)
      onLoadStarted(time(20), loadInfo("ab1.m3u8"), null)
      assertThat(generatePA().allStates).isNotEmpty()
      assertThat(generatePA().bitrateSummary).isNotEmpty()
      assertThat(generatePA().loadSummary).isNotEmpty()
      onMediaPeriodReleased(time(100))
    }

    assertThat(pa.generatePA().allStates).isEmpty()
    assertThat(pa.generatePA().bitrateSummary).isEmpty()
    assertThat(pa.generatePA().loadSummary).isEmpty()
  }

  @Test
  fun test_bwChangeAccumlation() {
    val (LOAD_TIME, BYTES) =  0 to 0L
    val (LOW, MED, HIGH) = arrayOf(320_000L, 760_000L, 1200_000L)
    val pa = listenerDB().apply {
      onBandwidthEstimate(time(5), LOAD_TIME, BYTES, LOW)
      onBandwidthEstimate(time(7), LOAD_TIME, BYTES, LOW)
      onBandwidthEstimate(time(10), LOAD_TIME, BYTES, LOW)
      onBandwidthEstimate(time(14), LOAD_TIME, BYTES, LOW)
      onBandwidthEstimate(time(15), LOAD_TIME, BYTES, MED) // change
      onBandwidthEstimate(time(25), LOAD_TIME, BYTES, LOW) // change
      onBandwidthEstimate(time(34), LOAD_TIME, BYTES, LOW)
      onBandwidthEstimate(time(35), LOAD_TIME, BYTES, HIGH) // change
      onBandwidthEstimate(time(36), LOAD_TIME, BYTES, HIGH)
      onBandwidthEstimate(time(45), LOAD_TIME, BYTES, HIGH)
      onMediaPeriodReleased(time(50))
    }

    assertThat(pa.generatePA().bitrateSummary).containsExactly(
        BwDuration(LOW, 10L),
        BwDuration(MED, 10L),
        BwDuration(LOW, 10L),
        BwDuration(HIGH, 15L))
  }

  @Test
  fun test_bwChangeAccumlation_Ordering() {
    val (LOAD_TIME, BYTES) =  0 to 0L
    val pa = listenerDB().apply {

      onBandwidthEstimate(time(768452233), LOAD_TIME, BYTES, 31167940)
      onBandwidthEstimate(time(768453060), LOAD_TIME, BYTES, 26479800)
      onBandwidthEstimate(time(768453657), LOAD_TIME, BYTES, 34639968)
      onMediaPeriodReleased(time(60))
    }

    assertThat(pa.generatePA().bitrateSummary).containsExactly(
        BwDuration(31167940, 827),
        BwDuration(26479800, 597))
  }

  @Test
  fun test_bwChangeAccumlation_Empty() {
    val pa = listenerDB().apply {
      // only player event
      onPlayerStateChanged(time(5), true, 3)
      onMediaPeriodReleased(time(10))

    }
    val pa1 = pa.generatePA()
    assertThat(pa1.bitrateSummary).isEmpty()
    assertThat(pa1.bitratesAtStateChanges).containsExactly(-1L)
  }

  @Test
  fun test_bwChangeAccumlation_SingleResult() {
    val pa = listenerDB().apply {
      // only bandwidth event
      onBandwidthEstimate(time(5), 0, 0, 320000)
      onMediaPeriodReleased(time(10))
    }
    val pa1 = pa.generatePA()
    assertThat(pa1.bitrateSummary).containsExactly(
      BwDuration(320000, 5)
    )
  }

  @Test
  fun testDownstreamformatChanges() {
    val pa = listenerDB().apply {
      onDownstreamFormatChanged(time(10), MediaSourceEventListener.MediaLoadData(
          0,
          0,
          Format.createSampleFormat("1", "video/h24", "codec", 320, null),
          1,
          null,
          0,
          10)
      )
      onDownstreamFormatChanged(time(24), MediaSourceEventListener.MediaLoadData(
          0,
          0,
          Format.createSampleFormat("1", "video/h24", "codec", 640, null),
          1,
          null,
          0,
          10)
      )

      onMediaPeriodReleased(time(42))
    }

    val pa1 = pa.generatePA()
    assertThat(pa1.timeTakenForFirstFormatChange).isEqualTo(14)
    assertThat(pa1.formatChangeCount).isEqualTo(1)
  }

  // Builders
  private fun time(time: Long) = AnalyticsListener.EventTime(
      time,
      null,
      0,
      null,
      0,
      0,
      0
  )

  private fun loadInfo(file: String) =
      MediaSourceEventListener.LoadEventInfo(null, Uri.parse("http://versedomain/someid/${file}"), mapOf(), 1, 1, 1)

  private fun listenerDB(): PAListener = PAListener("tests", db.dao(), {System
      .currentTimeMillis()}, {it.run()}, false)
}
