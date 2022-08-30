/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.model.entity

import `in`.dailyhunt.money.frequency.FCData
import com.google.gson.annotations.SerializedName
import com.newshunt.adengine.model.entity.version.AdClubType
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.model.entity.version.AdLPBackAction
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.model.entity.version.AdUIType
import com.newshunt.adengine.util.AdConstants
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.AdContextRules
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.social.entity.Position
import java.io.Serializable
import java.util.Observable

/**
 * Base class for all ads. Collection or otherwise.
 *
 * @author shreyas.desai
 */
abstract class BaseAdEntity : Observable(), CommonAsset, Serializable {

    var isShown: Boolean = false
    var isBackUpAd: Boolean = false
    var isEvergreenAd: Boolean = false
    var isRequestUrlHit: Boolean = false
    var isAboveContentAutoplayable: Boolean = false
    var isBelowContentAutoplayable: Boolean = false

    var fallbackReason: EvergreenFallbackReason? = null
    var displayPosition: Position? = null

    override fun i_id(): String {
        return uniqueAdIdentifier
    }

    override fun i_adId(): String? {
        return uniqueAdIdentifier
    }

    override fun i_type(): String {
        return type?.getName() ?: Constants.EMPTY_STRING
    }

    override fun i_format(): Format {
        return Format.AD
    }

    override fun i_subFormat(): SubFormat {
        return SubFormat.AD
    }

    override fun i_uiType(): UiType2? {
        return UiType2.NORMAL
    }

    abstract var adPosition: AdPosition?

    abstract val adGroupId: String?

    open val clubType: AdClubType? = AdClubType.FALLBACK

    abstract val campaignId: String?

    /**
     * Ads BE serves 'bannerId' in this field.
     * e.g. notId=ad-51530, where 51530 is bannerId.
     */
    abstract val aduid: String?

    abstract val beaconUrl: String?

    abstract var cardPosition: Int?

    abstract var positionWithTicker: Int?
    @SerializedName("min-ad-distance")
    open var minAdDistance: Int? = null

    open var largeAdDistance: Int? = null

    open var isLargeAd: Boolean? = null

    abstract val type: AdContentType?

    open var action: String? = null

    abstract fun addLandingUrl(url: String)

    abstract val uniqueAdIdentifier: String

    open var adReportInfo: AdReportInfo? = null

    abstract var adTag: String?

    abstract var adTemplate: AdTemplate?

    abstract var sdkOrder: Int

    abstract var requestUrl: String?

    abstract var fcData: FCData?

    abstract var bannerFCData: FCData?

    abstract val parentIds: MutableSet<Int>

    var receiveTime: Long = 0L

    open var isVideoAd: Boolean = false

    abstract var adContextRules: AdContextRules?
    abstract var dedupId: String?
    abstract var dedupDistance: Int?

    @Transient
    var contentAsset: CommonAsset? = null

    var adDistanceMs: Int = AdConstants.AD_NEGATIVE_DEFAULT
    var ignoreVideoDuration: Int = AdConstants.AD_NEGATIVE_DEFAULT

    open var startepoch: Long? = null
    open var endepoch: Long? = null
    open val supportedZones: Map<String, Map<String, AdSelectionMeta>>? = null

    open var displayType: AdUIType? = null
    open var backFromLpAction: AdLPBackAction? = null
    open var adClosedBeaconUrl: String? = null

    fun copyFrom(fromAd: BaseAdEntity) {
        fromAd.displayType?.let { displayType = it }

        fromAd.backFromLpAction?.let { backFromLpAction = it }
        fromAd.minAdDistance?.let { minAdDistance = it }
    }

    override fun notifyObservers() {
        setChanged()
        super.notifyObservers()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (this === other) {
            return true
        }

        if (other !is BaseAdEntity) {
            return false
        }

        val baseAdEntity = other as BaseAdEntity?
        return uniqueAdIdentifier.equals(baseAdEntity!!.uniqueAdIdentifier, ignoreCase = true)
    }

    override fun hashCode(): Int {
        return uniqueAdIdentifier.hashCode()
    }
}
