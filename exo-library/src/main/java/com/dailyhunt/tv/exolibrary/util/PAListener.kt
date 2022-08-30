/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.dailyhunt.tv.exolibrary.util

import android.net.Uri
import android.os.SystemClock
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.io.IOException


/**
 * Inserts player analytics data into DB using [dao]
 * @author satosh.dhanyamraju
 */
class PAListener(private val id: Any/*used only for analytics*/,
                 private val dao: PADao = PA_DB.dao()/*for capturing player events*/,
                 private val time: () -> Long = { SystemClock.elapsedRealtime() } /*to enable test-fake*/,
                 private val bg: (Runnable) -> Unit = com.newshunt.dataentity.common.helper.common.CommonUtils::runInBackground /*current thread in tests*/,
                 private val cleanupOnRelease: Boolean = true/*false in tests*/,
                 private val shouldTrackLoad: (Uri?) -> Boolean = {
                   (it?.lastPathSegment?.toString()?.endsWith("m3u8")) ?: false
                 }/*currently, only manifests*/,
                 private val onComplete: (PA) -> Unit = {
                   Logger.d("PAListener", it.formattedStr())
                 }/*one-time callback when periodReleased; after generating the PA object data could be cleared*/) :
    AnalyticsListener {
  val TAG = "PAListener"
  var uid = time()

  override fun onPlayerStateChanged(eventTime: AnalyticsListener.EventTime, playWhenReady: Boolean, playbackState: Int) {
    bg(Runnable {
      runCatching {
        dao.add(PlayerState(uid, eventTime.time(), playbackState))
      }.onFailure {
        Logger.e(TAG, "onPlayerStateChanged: ", it)
      }
    })
  }

  override fun onBandwidthEstimate(eventTime: AnalyticsListener.EventTime, totalLoadTimeMs: Int, totalBytesLoaded: Long, bitrateEstimate: Long) {
    bg(Runnable {
      runCatching {
        dao.add(BitrateEntry(uid, eventTime.time(), bitrateEstimate))
      }.onFailure {
        Logger.e(TAG, "onBandwidthEstimate: ", it)
      }
    })
  }

  override fun onLoadStarted(eventTime: AnalyticsListener.EventTime, loadEventInfo: MediaSourceEventListener.LoadEventInfo, mediaLoadData: MediaSourceEventListener.MediaLoadData) {
    if (shouldTrackLoad(loadEventInfo?.uri).not()) return
    bg(Runnable {
      runCatching {
        dao.add(LoadEntry(uid, loadEventInfo.uri(), eventTime.time()))
      }.onFailure {
        Logger.e(TAG, "onLoadStarted: ", it)
      }
    })
  }

  override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime, loadEventInfo: MediaSourceEventListener.LoadEventInfo, mediaLoadData: MediaSourceEventListener.MediaLoadData) {
    if (shouldTrackLoad(loadEventInfo?.uri).not()) return
    bg(Runnable {
      dao.updateLoadEntry(loadEventInfo.uri(), eventTime.time(), 1)
    })
  }

  override fun onLoadError(eventTime: AnalyticsListener.EventTime, loadEventInfo: MediaSourceEventListener.LoadEventInfo, mediaLoadData: MediaSourceEventListener.MediaLoadData, error: IOException, wasCanceled: Boolean) {
    if (shouldTrackLoad(loadEventInfo?.uri).not()) return
    bg(Runnable {
      dao.updateLoadEntry(loadEventInfo.uri(), eventTime.time(), 2)
    })
  }

  override fun onLoadCanceled(eventTime: AnalyticsListener.EventTime, loadEventInfo: MediaSourceEventListener.LoadEventInfo, mediaLoadData: MediaSourceEventListener.MediaLoadData) {
    if (shouldTrackLoad(loadEventInfo?.uri).not()) return
    bg(Runnable {
      dao.updateLoadEntry(loadEventInfo.uri(), eventTime.time(), 3)
    })
  }

  override fun onDownstreamFormatChanged(eventTime: AnalyticsListener.EventTime, mediaLoadData: MediaSourceEventListener.MediaLoadData) {
    bg(Runnable {
      runCatching {
        val format = mediaLoadData?.trackFormat?:return@runCatching
        dao.add(FormatChange(uid, eventTime.time(), format.id?.toIntOrNull()?:-1,
            format.bitrate, format.sampleMimeType))
      }.onFailure {
        Logger.e(TAG, "onDownstreamFormatChanged: ", it)
      }
    })
  }

  /**
   * maybe used in multi-threaded way.
   * - save the current-id and change it immediately (subsequent dao ops will not affect current
   * computation - to cover reprepare cases)
   * - add 'end' rows to tables
   * - generate [PA] and call [onComplete]
   * - cleanup if required
   */
  override fun onMediaPeriodReleased(eventTime: AnalyticsListener.EventTime) {
    val lUid = uid
    if (cleanupOnRelease) uid = time()
    bg(Runnable {
      // add a DEFAULT rows to mark the end.
      runCatching {
        dao.add(PlayerState(lUid, eventTime.time(), -1))
        dao.add(BitrateEntry(lUid, eventTime.time(), -1))
      }.onFailure {
        Logger.e(TAG, "onMediaPeriodReleased: ", it)
      }

      onComplete(generatePA(lUid))

      if (cleanupOnRelease) cleanup(lUid)
    })
  }

  fun generatePA(lUid: Long = uid) = PA(
      id,
      dao.totalBufferTime(lUid) ?: 0,
      dao.totalPlaybackTime(lUid) ?: C.TIME_UNSET,
      dao.allStates(lUid)?.map { it?.state ?: -1 } ?: emptyList(),
      dao.bandwidthEstimateAtStateChanges(lUid)?.map { it ?: -1L } ?: emptyList(),
      dao.bandWidthChangeSummary(lUid) ?: emptyList(),
      dao.loadEntrySummary(lUid)?.filterNotNull() ?: emptyList(),
      dao.timeTakenForFirstFormatChange(lUid)?: -1L,
      dao.formatChangeCount(lUid)?:-1
  )

  fun cleanup(luid: Long = uid) {
    bg(Runnable {
      runCatching {
        dao.deleteBitrateEntries(luid)
        dao.deleteLoadEntries(luid)
        dao.deletePlayerStates(luid)
        dao.deleteFormatChanges(luid)
      }.onFailure {
        Logger.e(TAG, "cleanup $luid", it)
      }
    })
  }

  private fun AnalyticsListener.EventTime?.time(): Long {
    return (this?.realtimeMs) ?: -1L
  }

  private fun MediaSourceEventListener.LoadEventInfo?.uri(): String {
    this ?: return "UNKNOWN"
    return this.uri.toString()
  }
}

