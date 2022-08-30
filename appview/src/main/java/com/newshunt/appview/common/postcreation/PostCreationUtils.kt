package com.newshunt.appview.common.postcreation

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
@Throws(IOException::class)
fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        val storageDir: File? =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        File.createTempFile(
            "DH_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    } else {
        createImageFileForQ(context, "DH_${timeStamp}")
    }
}

private fun createImageFileForQ(context: Context, fileName: String): File {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val storageDir: File? =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            fileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
    throw Exception("Not version Q")
}

private fun createImageFileQ(context: Context, fileName: String): Uri? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Audio.Media.IS_PENDING, 0)
        }

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        return context.contentResolver.insert(collection, values)
    }
    return null
}

fun addImageToGallery(context: Context, filepath: String?, success: Boolean = false) {
    filepath ?: return
    val uri = filepath.toUri()
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, uri.lastPathSegment)
            put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Images.Media.DATA, uri.toString())
        }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
//    else {
//        if (success) {
//            val values = ContentValues().apply {
//                put(MediaStore.Audio.Media.IS_PENDING, 0)
//            }
//            context.contentResolver.update(uri, values, null, null)
//        } else {
//            context.contentResolver.delete(uri, null, null)
//        }
//    }
}