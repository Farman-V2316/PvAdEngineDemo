/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics

import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams
import com.newshunt.adengine.analytics.NhAnalyticsAdEventParam
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.client.AttributeFilter
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerType
import com.newshunt.helper.SearchAnalyticsHelper
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam

/**
 * Created on 30/10/2019.
 */
object VideoAnalyticsHelper {

    fun addCardParams(map: MutableMap<NhAnalyticsEventParam, Any?>, item: CommonAsset?,
                      addBaseParams: Boolean, addAllParams: Boolean, isAdEvent: Boolean = false): MutableMap<String, Any>? {
        if (item == null ) {
            return mutableMapOf()
        }

        return AttributeFilter.filterForNH(getCardParams(map, item, addBaseParams, addAllParams, isAdEvent))
    }

    fun getCardParams(map: MutableMap<NhAnalyticsEventParam, Any?>, item: CommonAsset?,
                      addBaseParams: Boolean, addAllParams: Boolean = true, isAdEvent: Boolean = false)
            : MutableMap<NhAnalyticsEventParam, Any?> {
        if(item == null) {
            return map
        }
        map[AnalyticsParam.ITEM_ID] = item.i_id()
        if (!CommonUtils.isEmpty(item.i_type())) {
            map[AnalyticsParam.TYPE] = item.i_type()
        }
        if (!CommonUtils.isEmpty(item.i_videoAsset()?.playerType)) {
            map[NhAnalyticsNewsEventParam.PLAYER_TYPE] = item.i_videoAsset()?.playerType
        }
        if (!CommonUtils.isEmpty(item.i_format()?.name)) {
            map[AnalyticsParam.FORMAT] = item.i_format()?.name
        }
        if (!CommonUtils.isEmpty(item.i_subFormat()?.name)) {
            map[AnalyticsParam.SUB_FORMAT] = item.i_subFormat()?.name
        }
        if (!CommonUtils.isEmpty(item.i_source()?.id)) {
            map[AnalyticsParam.ITEM_PUBLISHER_ID] = item.i_source()?.id
        }
        if (!CommonUtils.isEmpty(item.i_source()?.playerKey)) {
            map[AnalyticsParam.PLAYER_KEY] = item.i_source()?.playerKey
        }
        if (!CommonUtils.isEmpty(item.i_uiType()?.name)) {
            map[AnalyticsParam.UI_TYPE] = item.i_uiType()?.name
        }
        if (!CommonUtils.isEmpty(item.i_langCode())) {
            map[AnalyticsParam.ITEM_LANGUAGE] = item.i_langCode()
        }
        if(addAllParams) {
            map[PlayerAnalyticsEventParams.VIDEO_LENGTH] = ((item.i_videoAsset()?.videoDurationInSecs)?.times(1000)).toString()
            if (!CommonUtils.isEmpty(item.i_groupId())) {
                map[AnalyticsParam.GROUP_ID] = item.i_groupId()
            }
            if (!CommonUtils.isEmpty(item.i_cardLabel()?.type?.name)) {
                map[AnalyticsParam.CARD_LABEL] = item.i_cardLabel()?.type?.name
            }
            if (!CommonUtils.isEmpty(item.i_hashtags())) {
                var tagList = ""
                for ((index, value) in item.i_hashtags()!!.withIndex()) {
                    val tag = item.i_hashtags()!!.get(index)
                    tagList += tag?.name
                    if(index < item.i_hashtags()!!.size - 1) {
                       tagList += ","
                    }
                }
                if (!CommonUtils.isEmpty(tagList)) {
                    map[AnalyticsParam.ITEM_TAG_IDS] = tagList
                }
            }
            if (!CommonUtils.isEmpty(item.i_videoAsset()?.type)) {
                map[AnalyticsParam.ASSET_TYPE] = item.i_videoAsset()?.type
            }
            if (!CommonUtils.isEmpty(item.i_contentType())) {
                map[AnalyticsParam.CONTENT_TYPE] = item.i_contentType()
            }

            map[AnalyticsParam.IS_GIF] = item.i_videoAsset()?.isGif
            map[AnalyticsParam.LOOP_COUNT] = item.i_videoAsset()?.loopCount

        }

        if(isAdEvent) {
            if(PlayerType.DH_EMBED_WEBPLAYER.equals(item?.i_videoAsset()?.playerType)) {
                map[NhAnalyticsAdEventParam.AD_POSITION] = AdPosition.INLINE_VIDEO
            }
        }

        if(addBaseParams) {
            AnalyticsClient.addStateParamsAndPermanentParams(map)
        }
        return map
    }

    fun addExperimentParams(card: CommonAsset?, map: MutableMap<String, Any>?) {
        if(card?.i_experiments() != null) {
            map?.putAll(card?.i_experiments()!!)
        }
    }

    fun addReferrerParams(map: HashMap<NhAnalyticsEventParam, Any?>, referrerFlow: PageReferrer?,
                          referrerLead: PageReferrer?, pageReferrer: PageReferrer?) {
        map[NhAnalyticsAppEventParam.REFERRER_FLOW] = referrerFlow?.referrer?.referrerName
        map[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = referrerFlow?.id
        map[PlayerAnalyticsEventParams.REFERRER_LEAD] = referrerLead?.referrer?.referrerName
        map[PlayerAnalyticsEventParams.REFERRER_LEAD_ID] = referrerLead?.id
        map[NhAnalyticsAppEventParam.REFERRER] = pageReferrer?.referrer?.referrerName
        map[NhAnalyticsAppEventParam.REFERRER_ID] = pageReferrer?.id
    }

}