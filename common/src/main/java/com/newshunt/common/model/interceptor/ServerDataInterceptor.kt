/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.interceptor

import android.content.Context
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import okio.ByteString
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * Used like a cookie manager. For each path, extracts and stores 'serverData' from the response
 * Subsequent request , if post, adds the stored 'serverData' for that path, if any.
 *
 * @author satosh.dhanymaraju
 */

object ServerDataInterceptor : Interceptor {

    private const val LOG_TAG = "ServerDataInterceptor"
    private const val SERVER_DATA = "serverData"// TODO (satosh.dhanyamraju): rename as per protocol

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url().url().path

        val modifiedRequest = read(path)?.let {
            append(request, it)
        } ?: request

        val response = chain.proceed(modifiedRequest)
        parse(response)?.let {
            store(path, it)
        }
        return response
    }

    /**
     * @param response body will be parsed to read server_data
     * @return server_data as string; null on failure
     */
    private fun parse(response: Response): String? {
        val source = response.body()?.source()
        source ?: return null
        try {
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
        } catch (e: Exception) {
            return null
        }
        val buffer = source.buffer()
        val responseString = buffer.clone().readString(Charset.forName("UTF-8"))
        responseString ?: return null
        return try {
            JSONObject(responseString).getString(SERVER_DATA)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Write data to file
     * @param path is used to generate filename
     */
    private fun store(path: String, data: String) {
        // As url path may contain special chars like '/', we cannot use it as filename. Encode it.
        val fileName = md5Hex(path)
        CommonUtils.getApplication().openFileOutput(fileName, Context.MODE_PRIVATE)?.use {
            Logger.d(LOG_TAG, "storing $data for $path")
            it.write(data.toByteArray())
        }
    }

    /**
     * @param path will be used to locate the file
     * @return contents of the file as string or null if error
     */
    private fun read(path: String): String? {
        return try {
            CommonUtils.getApplication().openFileInput(md5Hex(path))?.use {
                it.bufferedReader().readText()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * @param data  will be added to post body of returned request
     * @return  new request object with modified body
     */
    private fun append(request: Request, data: String): Request {

        if (request.method() != Constants.HTTP_POST || request.body() == null) return request

        val buffer = Buffer()
        request.body()?.writeTo(buffer)
        if (buffer.size() == 0L) return request

        val newJson = JSONObject(buffer.readUtf8()).apply {
            put(SERVER_DATA, data)
        }
        Logger.d(LOG_TAG, "appending $data to ${request.url().url().path}")
        return request.newBuilder()
                .post(RequestBody.create(request.body()?.contentType(), newJson.toString()))
                .build()
    }

    /***
     * Returns a 32 character string containing an MD5 hash of `s`, or alphanumeric str on failure.
     */
    private fun md5Hex(s: String): String {
        return try {
            val messageDigest = MessageDigest.getInstance("MD5")
            val md5bytes = messageDigest.digest(s.toByteArray(charset("UTF-8")))
            ByteString.of(*md5bytes).hex()
        } catch (e: Exception) {
            Logger.e(LOG_TAG, "md5hex", e)
            s.filter { it.isLetterOrDigit() }
        }
    }
}