/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.processor.AdProcessorFactory
import java.lang.reflect.Type

/**
 * @author raunak.yadav
 */
class AdCustomDeserializer : JsonDeserializer<BaseDisplayAdEntity?> {

    override fun deserialize(json: JsonElement?,
                             typeOfT: Type?,
                             context: JsonDeserializationContext?): BaseDisplayAdEntity? {
        if (json == null || context == null) return null
        val ad = runCatching { json.asJsonObject }.getOrNull() ?: return null
        val adType = if (ad.has("type")) {
            json.asJsonObject.get("type").asString
        } else {
            return null
        }
        val type = AdProcessorFactory.fromAdContentType(AdContentType.fromName(adType))
        return type?.let { context.deserialize(json, type) }
    }
}