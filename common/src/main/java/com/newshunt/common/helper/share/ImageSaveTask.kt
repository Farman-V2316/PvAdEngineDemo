package com.newshunt.common.helper.share

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.SoftReference
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutionException


class ImageSaveTask(private val contextRef: SoftReference<Context>,
                    private val fileDownloadCallback: ImageSaveCallback) {
    val compositDisposable: CompositeDisposable = CompositeDisposable()
    fun execute(urlToDownload: String?, filePathToSave: String?) {
        if (CommonUtils.isEmpty(urlToDownload) || CommonUtils.isEmpty(filePathToSave)) {
            throw IllegalArgumentException("Empty url or filepath not supported")
        }
        fileDownloadCallback.onStart()
        compositDisposable.add(
                Observable.fromCallable {
                    downloadFile(urlToDownload!!, filePathToSave!!)
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ filePath: String? ->
                            fileDownloadCallback.onDownloaded(filePath
                                    ?: Constants.EMPTY_STRING, CommonUtils.isEmpty(filePath))
                        }, {
                            fileDownloadCallback.onError(it)
                            Logger.caughtException(it)
                        })
        )
    }

    fun clear() {
        compositDisposable.dispose()
    }

    private fun downloadFile(vararg params: String): String? {
        if (params.size < 2) {
            return Constants.EMPTY_STRING;
        }

        val src = params[0]
        val fileSavePath = params[1];

        try {
            val context = contextRef.get() ?: return Constants.EMPTY_STRING
            val file = Glide.with(context)
                    .load(src)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get()

            moveFile(file.absolutePath, fileSavePath)
            return fileSavePath
        } catch (e: InterruptedException) {
            Logger.e(LOG_TAG, "Task interrupted")
        } catch (e: ExecutionException) {
            Logger.e(LOG_TAG, "execution excption")
        } catch (e: IOException) {
            Logger.e(LOG_TAG, "IO excption")
        }

        return null
    }

    /*
    * Move file to different location
    * */
    @Throws(IOException::class)
    private fun moveFile(currentPath: String, newPath: String) {
        val newFile = File(newPath)
        if (newFile.exists()) {
            newFile.delete()
        }
        val currentFile = File(currentPath)
        if (!currentFile.exists()) {
            return
        }
        var outputChannel: FileChannel? = null
        var inputChannel: FileChannel? = null
        try {
            outputChannel = FileOutputStream(newFile).getChannel()
            inputChannel = FileInputStream(currentFile).getChannel()
            inputChannel.transferTo(0, inputChannel.size(), outputChannel)
            inputChannel.close()
        } finally {
            if (inputChannel != null) inputChannel.close()
            if (outputChannel != null) outputChannel.close()
        }
    }

    companion object {
        private const val LOG_TAG = "ImageSaveTask"
    }
}

interface ImageSaveCallback {
    fun onStart()
    fun onDownloaded(filePath: String, failed: Boolean)
    fun onError(e: Throwable?)
}