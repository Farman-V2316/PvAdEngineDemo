/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.client

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.newshunt.adengine.model.entity.AdTypeDeserializer
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.processor.AdProcessorFactory
import com.newshunt.adengine.util.AdLogger
import com.newshunt.common.helper.common.JsonUtils
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer


/**
 * Created by {Mukesh Yadav} on 15,November,2019
 */
private const val LOG_TAG = "AdProcessorHelper"

class AdProcessorHelper {
    companion object {
        @JvmStatic
        fun parseJson(data: String?): ArrayList<BaseDisplayAdEntity> {
            val baseDisplayAdEntities = ArrayList<BaseDisplayAdEntity>()
            try {
                val obj = JsonParser.parseString(data).asJsonObject
                val asJsonArray = obj.get("ads").asJsonArray
                //todo mukesh rxjava2 + can done on background thread

                // convert to particular type of BaseDisplayAdEntity
                for (item in asJsonArray){
                    val adContentType = AdContentType.fromName((item as JsonObject)
                            .get("type").asString)
                    val clazz = AdProcessorFactory.fromAdContentType(adContentType)
                    val baseDisplayAdEntity: BaseDisplayAdEntity = JsonUtils.fromJson(
                            item,
                            clazz,
                            AdTypeDeserializer(AdContentType::class.java))
                    baseDisplayAdEntities.add(baseDisplayAdEntity)
                }
            } catch (e: Exception) {
                AdLogger.e(LOG_TAG, "${e.message} \n$data")
            }

            return baseDisplayAdEntities
        }

        @JvmStatic
        fun readInputStream(inputStream: InputStream): String {
            val writer: Writer = StringWriter()
            val buffer = CharArray(1024)
            try {
                val reader: Reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                var n: Int
                while (reader.read(buffer).also { n = it } != -1) {
                    writer.write(buffer, 0, n)
                }
            } finally {
                inputStream.close()
            }

            return writer.toString()
        }
    }

}