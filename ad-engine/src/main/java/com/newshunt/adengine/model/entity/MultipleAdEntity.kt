/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity

import `in`.dailyhunt.money.frequency.FCData
import com.newshunt.adengine.model.entity.version.AdClubType
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.model.entity.version.AdLPBackAction
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.model.entity.version.AdUIType
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsUtil.Companion.getIntValue
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.AdContextRules
import java.io.Serializable

/**
 * For Carousel, multiple ads needs to be displayed in the same row.
 * This is a wrapper class that provides ability to send multiple ads in single story.
 * (e.g. AppDownload ads, Content Carousel ads)
 *
 * @author raunak.yadav
 */
class MultipleAdEntity : BaseAdEntity(), Serializable {
    override var cardPosition: Int? = null
        set(value) {
            field = getIntValue(value, DEFAULT_VAL)
        }
        get() = getIntValue(baseDisplayAdEntities.firstOrNull()?.cardPosition, AdConstants.AD_NEGATIVE_DEFAULT)

    override var minAdDistance: Int? = null
        set(value) {
            field = getIntValue(value, DEFAULT_VAL)
        }

    override var largeAdDistance: Int?
        get() = getIntValue(baseDisplayAdEntities.firstOrNull()?.largeAdDistance, AdConstants.AD_NEGATIVE_DEFAULT)
        set(value) {
            baseDisplayAdEntities.forEach {
                it.largeAdDistance = value
            }
        }

    override var isLargeAd: Boolean?
        get() = baseDisplayAdEntities.firstOrNull()?.isLargeAd ?: false
        set(value) {
            baseDisplayAdEntities.forEach {
                it.isLargeAd = value
            }
        }

    override var type: AdContentType? = null
        private set

    override var fcData: FCData?
        get() = baseDisplayAdEntities.firstOrNull()?.fcData
        set(value) {
            baseDisplayAdEntities.forEach {
                it.fcData = value
            }
        }

    override var bannerFCData: FCData?
        get() = baseDisplayAdEntities.firstOrNull()?.bannerFCData
        set(value) {
            baseDisplayAdEntities.forEach {
                it.bannerFCData = value
            }
        }

    override var adContextRules: AdContextRules? = null

    override var dedupId: String?
        get() = baseDisplayAdEntities.firstOrNull()?.dedupId
        set(value) {
            baseDisplayAdEntities.forEach {
                it.dedupId = value
            }
        }

    override var dedupDistance: Int?
        get() = baseDisplayAdEntities.firstOrNull()?.dedupDistance ?: 0
        set(value) {
            baseDisplayAdEntities.forEach {
                it.dedupDistance = value
            }
        }

    override var startepoch: Long?
        get() = baseDisplayAdEntities.firstOrNull()?.startepoch ?: 0L
        set(value) {
            baseDisplayAdEntities.forEach {
                it.startepoch = value
            }
        }

    override var endepoch: Long?
        get() = baseDisplayAdEntities.firstOrNull()?.endepoch ?: 0L
        set(value) {
            baseDisplayAdEntities.forEach {
                it.endepoch = value
            }
        }

    override var displayType: AdUIType?
        get() = baseDisplayAdEntities.firstOrNull()?.displayType
        set(value) {
            baseDisplayAdEntities.forEach {
                it.displayType = value
            }
        }

    override var backFromLpAction: AdLPBackAction?
        get() = baseDisplayAdEntities.firstOrNull()?.backFromLpAction
        set(value) {
            baseDisplayAdEntities.forEach {
                it.backFromLpAction = value
            }
        }

    override var adClosedBeaconUrl: String?
        get() = baseDisplayAdEntities.firstOrNull()?.adClosedBeaconUrl
        set(value) {
            baseDisplayAdEntities.forEach {
                it.adClosedBeaconUrl = value
            }
        }

    override val supportedZones: Map<String, Map<String, AdSelectionMeta>>?
        get() = baseDisplayAdEntities.firstOrNull()?.supportedZones

    var baseDisplayAdEntities: MutableList<BaseDisplayAdEntity> = ArrayList()
        set(value) {
            if (value.isNotEmpty()) {
                adContextRules = baseDisplayAdEntities[0].adContextRules
            }
            field = value
        }

    fun addBaseDisplayAdEntity(baseDisplayAdEntity: BaseDisplayAdEntity) {
        val position = getIntValue(baseDisplayAdEntity.cardPosition, DEFAULT_VAL)
        if (position < cardPosition ?: 0) {
            cardPosition = position
        }
        val minDistance = getIntValue(baseDisplayAdEntity.minAdDistance, DEFAULT_VAL)
        if (minDistance > minAdDistance ?: 0) {
            minAdDistance = minDistance
        }
        if (baseDisplayAdEntities.isEmpty()) {
            adContextRules = baseDisplayAdEntity.adContextRules
        }
        baseDisplayAdEntities.add(baseDisplayAdEntity)
    }

    fun setAdContentType(adContentType: AdContentType?) {
        type = adContentType
    }

    override var action: String?
        get() = baseDisplayAdEntities.firstOrNull()?.action
        set(action) {
            super.action = action
        }

    override fun addLandingUrl(url: String) {}

    override var adPosition: AdPosition?
        get() = baseDisplayAdEntities.firstOrNull()?.adPosition
        set(value) {
            baseDisplayAdEntities.forEach {
                it.adPosition = value
            }
        }

    override val adGroupId: String?
        get() = baseDisplayAdEntities.firstOrNull()?.adGroupId

    override val clubType: AdClubType?
        get() = baseDisplayAdEntities.firstOrNull()?.clubType

    override val campaignId: String?
        get() = baseDisplayAdEntities.firstOrNull()?.campaignId

    override val aduid: String?
        get() = baseDisplayAdEntities.firstOrNull()?.aduid

    override val uniqueAdIdentifier: String
        get() = baseDisplayAdEntities.firstOrNull()?.uniqueAdIdentifier ?: Constants.EMPTY_STRING

    override var isVideoAd: Boolean
        get() = baseDisplayAdEntities.firstOrNull()?.isVideoAd ?: false
        set(isVideoAd) {
            super.isVideoAd = isVideoAd
        }

    override var adReportInfo: AdReportInfo?
        get() = baseDisplayAdEntities.firstOrNull()?.adReportInfo
        set(adReportInfo) {
            super.adReportInfo = adReportInfo
        }

    override var adTag: String?
        get() = baseDisplayAdEntities.firstOrNull()?.adTag
        set(value) {
            baseDisplayAdEntities.forEach {
                it.adTag = value
            }
        }

    override var adTemplate: AdTemplate?
        get() = baseDisplayAdEntities.firstOrNull()?.adTemplate
        set(value) {
            baseDisplayAdEntities.forEach {
                it.adTemplate = value
            }
        }

    override val beaconUrl: String?
        get() = baseDisplayAdEntities.firstOrNull()?.beaconUrl

    override var positionWithTicker: Int?
        get() = getIntValue(baseDisplayAdEntities.firstOrNull()?.positionWithTicker, AdConstants.AD_NEGATIVE_DEFAULT)
        set(value) {
            baseDisplayAdEntities.forEach {
                it.positionWithTicker = value
            }
        }

    override var sdkOrder: Int
        get() = baseDisplayAdEntities.firstOrNull()?.sdkOrder ?: Int.MAX_VALUE
        set(value) {
            baseDisplayAdEntities.forEach {
                it.sdkOrder = value
            }
        }

    override var requestUrl: String? = baseDisplayAdEntities.firstOrNull()?.requestUrl

    override val parentIds: MutableSet<Int>
        get() = baseDisplayAdEntities.firstOrNull()?.parentIds ?: HashSet()

    companion object {
        private const val serialVersionUID = 342476455386711707L
        private const val DEFAULT_VAL = 7
    }
}