@file:Suppress("SameParameterValue")

package com.newshunt.appview.common.postcreation.view.helper

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.newshunt.dataentity.social.entity.DEFAULT_IMAGE_COMPRESS_QUALITY
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.KITKAT)
object ImageFilePath {

    fun getPath(
        context: Context, uri: Uri, compressImage: Boolean = false,
        compressQuality: Int = DEFAULT_IMAGE_COMPRESS_QUALITY): String? {
        if (compressImage) {
            return compressImage(context, uri, compressQuality)
        } else {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return copyFile(context, uri)
            }
            if ("content".equals(uri.scheme!!, ignoreCase = true)) {
                if (isGoogleOldPhotosUri(uri)) {
                    // return http path, then download file.
                    return uri.lastPathSegment
                } else if (isGoogleNewPhotosUri(uri) || isPicassoPhotoUri(uri)) {
                    return copyFile(context, uri)
                } else {
                    return getDataColumn(context, uri, null, null) ?: return copyFile(context, uri)
                }
            } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
                return uri.path
            }
        }
        return null
    }

    private fun isGoogleOldPhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun isGoogleNewPhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.contentprovider" == uri.authority
    }

    private fun isPicassoPhotoUri(uri: Uri?): Boolean {

        return (uri != null
                && !TextUtils.isEmpty(uri.authority)
                && (uri.authority!!.startsWith("com.android.gallery3d") || uri.authority!!.startsWith(
            "com.google.android.gallery3d"
        )))
    }

    private fun getDataColumn(
        context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?
    ): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(
                uri, projection, selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (e: Exception){
        }
        finally {
            cursor?.close()
        }
        return null
    }

    @SuppressLint("SimpleDateFormat")
    private fun copyFile(context: Context, uri: Uri): String {

        var filePath: String
        var inputStream: InputStream? = null
        var outStream: BufferedOutputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)

            val extDir = context.getExternalFilesDir(null)
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(Date())
            filePath = extDir?.absolutePath + "/${PostConstants.POST_CREATE_IMAGE_FILE_NAME}${timeStamp}" + ".jpg"
            outStream = BufferedOutputStream(FileOutputStream(filePath))

            val buf = ByteArray(2048)
            var len: Int
            do {
                len = inputStream!!.read(buf)
                if (len <= 0) break
                outStream.write(buf, 0, len)
            } while (len > 0)
        } catch (e: IOException) {
            e.printStackTrace()
            filePath = ""
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                outStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return filePath
    }

    @SuppressLint("SimpleDateFormat")
    private fun compressImage(context: Context, uri: Uri, compressQuality: Int): String {
        var filePath: String
        var inputStream: InputStream? = null
        val quality = if (compressQuality <= 0 || compressQuality > 100) {
            DEFAULT_IMAGE_COMPRESS_QUALITY
        } else {
            compressQuality
        }
        try {
            inputStream = context.contentResolver.openInputStream(uri)

            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

            val extDir = context.getExternalFilesDir(null)
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(Date())
            filePath =
                extDir?.absolutePath + "/${PostConstants.POST_CREATE_IMAGE_FILE_NAME}${timeStamp}" + ".jpg"

            val out = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            bitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
            filePath = ""
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return filePath
    }
}