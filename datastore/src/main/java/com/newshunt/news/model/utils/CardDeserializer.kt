/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.InvalidCard
import com.newshunt.dataentity.common.asset.LocationIdParent
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.Member
import java.lang.reflect.Type

/**
 * Gson deserializer for any API returning cards
 *
 * @author satosh.dhanyamraju
 */
class CardDeserializer(
        private val listType: String?,
        private val invalidCardsLogger: InvalidCardsLogger
): JsonDeserializer<AnyCard?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): AnyCard? {
        if (json == null || context == null) return null
        logDebug(json)
        val type = when {
            listType == Format.MEMBER.name -> object : TypeToken<Member>() {}.type
            listType == Format.GROUP_INVITE.name -> object : TypeToken<GroupInfo>() {}.type
            listType == Format.ENTITY.name -> object : TypeToken<ActionableEntity>() {}.type
            listType == Constants.ITEM_TYPE_LOCATION_SEARCH -> object : TypeToken<LocationIdParent>() {}.type
            //Validation rules not to apply to proxy posts for ads.
            listType == Constants.ADS -> object :TypeToken<PostEntity>(){}.type
            /*all other listTypes*/ isValidPostEntity(json) -> object :  TypeToken<PostEntity>() {}.type
            else -> null
        }
        return if(type!=null) context.deserialize(json, type)
        else InvalidCard
    }

    private fun isValidPostEntity(json: JsonElement?): Boolean {
        val pe = runCatching { json?.asJsonObject }.getOrNull() ?: return false
        if (pe.has("repostAsset")) {
            if(!isValidPostEntity(pe.get("repostAsset"))) return false
        }

        if (pe.has("moreStories")) {
            val peMoreStories = kotlin.runCatching { pe.get("moreStories")?.asJsonArray }.getOrNull()?:return false
            for (i in 0 until peMoreStories.size()) {
                if(!isValidPostEntity(peMoreStories.get(i))) return false
            }
        }
        return when {
            !pe.has("format") -> {
                logReject(json, "null format")
                false
            }
            runCatching { Format.valueOf(pe.get("format")?.asString!!) }.isFailure -> {
                logReject(json, "invalid format ${pe.get("format")}")
                false
            }
            runCatching { Format.valueOf(pe.get("format")?.asString!!) == Format.LANGUAGE &&
                CommonUtils.getIsLanguageSelectedOnLanguageCard()}.getOrDefault(false) -> {
                logReject(json, "language card after selection format ${pe.get("format")}")
                false
            }
            !pe.has("subFormat") -> {
                logReject(json, "null subFormat")
                false
            }
            runCatching { SubFormat.valueOf(pe.get("subFormat")?.asString!!) }.isFailure -> {
                logReject(json, "invalid subFormat ${pe.get("subFormat")}")
                false
            }
            !pe.has("uiType") -> {
                logReject(json, "null uiType")
                false
            }
            runCatching { UiType2.valueOf(pe.get("uiType")?.asString!!) }.isFailure -> {
                logReject(json, "invalid uiType ${pe.get("uiType")}")
                false
            }
            else -> true
        }

    }

    private fun logReject(json: JsonElement?, reason: String) {
        val id = kotlin.runCatching { json?.asJsonObject?.get("id")?.asString }.getOrNull()
        Logger.e("CardDeserializer", "rejecting: $id; $reason")
        val logPojo = (id?.let { "id:$it" }) ?: json ?: "null"
        json?.let {  invalidCardsLogger.log(reason, logPojo) }
    }
    private fun logDebug(json: JsonElement?) {
        val id = kotlin.runCatching { json?.asJsonObject?.get("id")?.asString }.getOrNull()
        Logger.d("CardDeserializer", "parsing: $id")
    }


    companion object {
        fun gson(listType: String?, logger : InvalidCardsLogger): Gson {
            return GsonBuilder()
                    .registerTypeAdapter(AnyCard::class.java, CardDeserializer(listType, logger)).create()
        }
    }
}