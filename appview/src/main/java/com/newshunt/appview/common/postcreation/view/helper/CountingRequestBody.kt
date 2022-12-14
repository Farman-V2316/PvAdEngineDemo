package com.newshunt.appview.common.postcreation.view.helper

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Okio
import okio.Sink
import java.io.IOException


class CountingRequestBody(
        private val delegate: RequestBody?,
        private val imagePath: String,
        private val prog: (String, Long, Long) -> Unit) : RequestBody() {

    override fun contentType(): MediaType? {
        return delegate?.contentType()
    }

    override fun contentLength(): Long {
        try {
            return delegate?.contentLength() ?: -1
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = Okio.buffer(countingSink)

        delegate?.writeTo(bufferedSink)

        bufferedSink.flush()
    }

    internal inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {

        private var bytesWritten: Long = 0

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            prog(imagePath, bytesWritten, contentLength())
        }
    }
}