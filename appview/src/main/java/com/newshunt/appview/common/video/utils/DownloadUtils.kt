/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.utils

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import com.dailyhunt.tv.players.analytics.VideoAnalyticsHelper
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

private const val DAILY_HUNT_VIDEO_FOLDER = "Dailyhunt" + Constants.FORWARD_SLASH + "Videos" +
        Constants.FORWARD_SLASH
private const val DAILY_HUNT_VIDEO_FILE_NAME = "dailyhunt_vid_"
private const val DAILY_HUNT_VIDEO_FILE_EXTENSION = ".mp4"

object DownloadUtils {
    val downloadRequestEvent: MutableLiveData<DownloadRequestData> = MutableLiveData()

    @JvmStatic
    fun downloadSingleFile(context: Context, downloadUrl: String, title: String?): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(true)
        request.setTitle("Dailyhunt - $title")
//        request.setDescription("Downloading Video File..")
        request.setVisibleInDownloadsUi(true)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                DAILY_HUNT_VIDEO_FOLDER + DAILY_HUNT_VIDEO_FILE_NAME +
                        System.currentTimeMillis() + DAILY_HUNT_VIDEO_FILE_EXTENSION)

        val downloadRequestId = downloadManager.enqueue(request)

        Logger.e("", "OUT - $downloadRequestId")
        return downloadRequestId
    }

    @JvmStatic
    fun checkDownloadStatus(activity: Activity?, downloadRequestId: Long, section: NhAnalyticsEventSection,
                            card: CommonAsset?, disposables: CompositeDisposable, referrer: PageReferrer) {
        if (activity == null || activity.isFinishing ||  card == null) return
        val downloadQuery = DownloadManager.Query()
        downloadQuery.setFilterById(downloadRequestId)
        val downloadManager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as
                DownloadManager?
        disposables.add(Observable.fromCallable {
            downloadManager?.query(downloadQuery)
        }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it != null && it.moveToFirst()) {
                        val columnIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        when (it.getInt(columnIndex)) {
                            DownloadManager.STATUS_FAILED -> {
                                AnalyticsHelper2.logDownloadEvent(
                                        NhAnalyticsAppEvent.ITEM_DOWNLOAD_FAILED,
                                        section, VideoAnalyticsHelper.getCardParams(HashMap(), card, true),
                                        referrer, card?.i_experiments())
                            }
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                AnalyticsHelper2.logDownloadEvent(NhAnalyticsAppEvent.ITEM_DOWNLOAD_COMPLETE,
                                        section, VideoAnalyticsHelper.getCardParams(HashMap(), card, true),
                                        referrer, card?.i_experiments())
                            }
                        }
                    } else {
                        AnalyticsHelper2.logDownloadEvent(NhAnalyticsAppEvent.ITEM_DOWNLOAD_FAILED,
                                section, VideoAnalyticsHelper.getCardParams(HashMap(), card, true),
                                referrer, card?.i_experiments())
                    }
                    if (it?.isClosed == false) {
                        it.close()
                    }
                })

    }

}


data class DownloadRequestData(val requestId: Long? = null,
                               val asset: CommonAsset? = null)

data class DownloadStatusUpdate(val requestId: Long? = null, val isDownloadStart: Boolean, val id: String?)