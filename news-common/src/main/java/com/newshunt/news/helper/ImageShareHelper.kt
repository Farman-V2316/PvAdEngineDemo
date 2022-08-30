/*
 *  Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.widget.Toast
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.FileUtil
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.share.ImageSaveCallback
import com.newshunt.common.helper.share.ImageSaveTask
import com.newshunt.common.helper.share.ShareContent
import com.newshunt.dataentity.common.model.entity.ShareContentType
import com.newshunt.news.common.R
import com.newshunt.viral.helper.VHShareHelper
import java.io.FileOutputStream
import java.lang.ref.SoftReference

/**
 * @author amitkumar.chaudhary
 */
class ImageShareHelper(private val imageShareHelperCallback: ImageShareHelperCallback,
                       val shareContent: ShareContent) :
        ImageSaveCallback {
    private var progressDialog: ProgressDialog? = null

    fun loadImageAndShare() {
        if (CommonUtils.isEmpty(shareContent.imageUrl)) {
            imageShareHelperCallback.onFinish(shareContent)
            return
        }
        when (shareContent.shareContentType) {
            ShareContentType.IMAGE, ShareContentType.VIRAL_GIF, ShareContentType.VIRAL_IMAGE -> {
                downloadShareImage(shareContent, imageShareHelperCallback.getActivity())
            }
            else -> imageShareHelperCallback.onFinish(shareContent)
        }
    }

    private var imageSaveTask: ImageSaveTask? = null

    private fun downloadShareImage(shareContent: ShareContent,
                                   activity: Activity) {
        val downloadPath = getShareImageDownloadPath(shareContent.imageUrl)
        imageSaveTask = ImageSaveTask(SoftReference(activity), this@ImageShareHelper);
        imageSaveTask?.execute(shareContent.imageUrl, downloadPath)
    }

    override fun onStart() {
        progressDialog = ProgressDialog(imageShareHelperCallback.getActivity())
        progressDialog?.let {
            it.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            it.isIndeterminate = true
            it.setCancelable(true)
            it.setCanceledOnTouchOutside(false)
            it.setProgressNumberFormat(null)
            it.setProgressPercentFormat(null)
            it.setTitle("Downloading")
            it.setOnCancelListener {
                imageSaveTask?.clear()
            }
            it.show()
        }
    }

    override fun onDownloaded(filePath: String, failed: Boolean) {
        if (failed || CommonUtils.isEmpty(filePath)) {
            progressDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
            if (!CommonUtils.isNetworkAvailable(imageShareHelperCallback.getActivity())) {
                FontHelper.showCustomFontToast(imageShareHelperCallback.getActivity(),
                        CommonUtils.getString(com.newshunt.common.util.R.string.error_no_connection), Toast.LENGTH_SHORT)
            }
            return
        }
        var overlappedBannerImage = Constants.EMPTY_STRING
        when (shareContent.shareContentType) {
            ShareContentType.VIRAL_IMAGE -> {
                var outStream: FileOutputStream? = null
                try {
                    overlappedBannerImage = CommonUtils.getCacheDir(Constants.DH_SHARE_HIDDEN_DIR).absolutePath +
                            Constants.FORWARD_SLASH + shareContent.imageUrl.hashCode() +
                            "_O" + Constants.DOT + FileUtil.getExtension(FileUtil
                            .getFileNameFromUrl(shareContent.imageUrl))
                    val options = Options()
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                    val bitmap = BitmapFactory.decodeFile(filePath, options)
                    val vhShareHelper = VHShareHelper.getInstance()
                    val newBitmap = vhShareHelper.overlayBitmap(bitmap, vhShareHelper.getShareBannerBitmap(emptyArray()))
                    outStream = FileOutputStream(overlappedBannerImage, false)
                    newBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                } catch (e: Exception) {
                    imageShareHelperCallback.onFinish(shareContent)
                } finally {
                    outStream?.close()
                }
            }
            else -> {

            }
        }
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }

        imageShareHelperCallback.onFinish(shareContent.apply {
            fileUri = FileUtil.getFileUri(imageShareHelperCallback.getActivity(), if (shareContent.shareContentType == ShareContentType.VIRAL_IMAGE)
                overlappedBannerImage else filePath)
        })
        imageSaveTask?.clear()
        return
    }

    override fun onError(e: Throwable?) {
        imageShareHelperCallback.onFinish(shareContent)
        imageSaveTask?.clear()
    }

    companion object {
        @JvmStatic
        fun getShareImageDownloadPath(url: String): String {
            return CommonUtils.getCacheDir(Constants.DH_SHARE_HIDDEN_DIR).absolutePath +
                    Constants.FORWARD_SLASH +
                    FileUtil.getHashCodeBasedFileName(url)
        }
    }
}

interface ImageShareHelperCallback {
    fun onFinish(shareContent: ShareContent)
    fun getActivity(): Activity
}