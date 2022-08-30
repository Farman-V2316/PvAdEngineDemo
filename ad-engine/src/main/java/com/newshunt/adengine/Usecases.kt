/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.newshunt.adengine.model.ReportAdsMenuAPI
import com.newshunt.adengine.model.entity.AdsFallbackEntity
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.processor.AdProcessorFactory
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdConstants.B_AD_ENTITY
import com.newshunt.adengine.util.AdConstants.B_AD_POS
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.dataentity.ads.PersistedAdEntity
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.ReportAdsMenuPostBodyEntity
import com.newshunt.dataentity.social.entity.AdInsertFailReason
import com.newshunt.dataentity.social.entity.AdInsertResult
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dataentity.social.entity.AdSpecEntity
import com.newshunt.dataentity.social.entity.ImmersiveAdRuleEntity
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.news.model.daos.AdsDao
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.ImmersiveRuleDao
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.Usecase
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * Returns id of failed ad if DB insertion fails,
 * null otherwise.
 *
 * @author raunak.yadav
 */
class InsertAdInfoUsecase @Inject constructor(private val fetchDao: FetchDao,
                                              private val immersiveRuleDao: ImmersiveRuleDao,
                                              @Named("entityId") private val entityId: String,
                                              @Named("location") private val location: String,
                                              @Named("section") private val section: String) :
        BundleUsecase<AdInsertResult> {

    override fun invoke(bundle: Bundle): Observable<AdInsertResult> {
        check(bundle.getSerializable(B_AD_ENTITY) != null)
        var ad = bundle.getSerializable(B_AD_ENTITY) as BaseAdEntity
        val prevPostId = bundle.getString(B_PREV_POST_ID)
        val adPosCheckTs = bundle.getLong(B_AD_POS_CHECK_TS, System.currentTimeMillis())
        val baseADPos = bundle.getInt(B_AD_POS, -1)

        return Observable.fromCallable {
            var externalSdkAd:ExternalSdkAd? = null
            if(AdsUtil.isIMAVideoAd(ad)) externalSdkAd = ad as ExternalSdkAd

            // apply immersive ad rule before insert
            if(externalSdkAd != null && !externalSdkAd.forceImmersiveView && baseADPos > -1) {
                if(!externalSdkAd.enableImmersiveView) {
                    immersiveRuleDao.insReplace(ImmersiveAdRuleEntity(
                            adDistance = externalSdkAd.immersiveViewDistance,
                            adId = ad.i_id(),
                            adPosition =  baseADPos,
                            playedInImmersive = false))
                } else {
                    val reason = immersiveRuleDao.isEligible(entityId, ad.uniqueAdIdentifier, baseADPos)
                    externalSdkAd.enableImmersiveView = reason
                }
                ad = externalSdkAd
            }

            val fetchId = fetchDao.fetchInfo(entityId, location, section)?.fetchInfoId?.toString()
                    ?: return@fromCallable AdInsertResult(ad.uniqueAdIdentifier, false, prevPostId, AdInsertFailReason.UNKNOWN_FETCH_ID)

            // 3rd party ads with content meta to insert only in cards and not in fetch_data.
            if (ad.type != AdContentType.CONTENT_AD && ad.contentAsset is PostEntity) {
                fetchDao.insIgnore(listOf(
                    (ad.contentAsset as PostEntity).toCard2(AdConstants.AD_PROXY_FETCH_ID, newLevel = PostEntityLevel.AD_PROXY)
                    )
                )
            }
            val adPost = if (ad.type == AdContentType.CONTENT_AD && ad.contentAsset is PostEntity)
                (ad.contentAsset as PostEntity) else AdsUtil.toPostEntity(ad)
            val uniqueId = if (adPost.format == Format.AD) null
            else adPost.getUniqueId(fetchId, prefix = ad.i_id().plus(Constants.UNDERSCORE_CHARACTER))

            val adCard = adPost.toCard2(fetchId, useUniqueId = uniqueId)
            adCard.adId = ad.i_id()
            val insertFailStatus = fetchDao.insertAdInDB(adCard, prevPostId, adPosCheckTs, entityId, location, section)
            AdInsertResult(ad.uniqueAdIdentifier, insertFailStatus == AdInsertFailReason.NONE, prevPostId, insertFailStatus)
        }
    }

    companion object {
        const val B_PREV_POST_ID = "prevPostId"
        const val B_AD_POS_CHECK_TS = "adPosCheckTs"

        fun bundle(baseAdEntity: BaseAdEntity, prevPostId: String?, adPosCheckTs: Long, adPos: Int = -1): Bundle {
            return bundleOf(B_AD_ENTITY to baseAdEntity, B_PREV_POST_ID to prevPostId,
                    B_AD_POS_CHECK_TS to adPosCheckTs, B_AD_POS to adPos)
        }
    }
}

class InsertProxyAdUsecase @Inject constructor(private val fetchDao: FetchDao) :
    BundleUsecase<Boolean> {

    override fun invoke(bundle: Bundle): Observable<Boolean> {
        check(bundle.getSerializable(B_AD_ENTITY) != null)
        val ad = bundle.getSerializable(B_AD_ENTITY) as BaseAdEntity

        return Observable.fromCallable {
            // 3rd party ads with content meta to insert only in cards and not in fetch_data.
            if (ad.type != AdContentType.CONTENT_AD && ad.contentAsset is PostEntity) {
                fetchDao.insIgnore(listOf((ad.contentAsset as PostEntity).toCard2(AdConstants.AD_PROXY_FETCH_ID, newLevel = PostEntityLevel.AD_PROXY)))
                true
            } else false
        }
    }

    companion object {
        fun bundle(baseAdEntity: BaseAdEntity): Bundle {
            return bundleOf(B_AD_ENTITY to baseAdEntity)
        }
    }
}


/**
 * For replacing failed IMA ads with backup ads.
 */
class ReplaceAdInfoUsecase @Inject constructor(private val fetchDao: FetchDao,
                                               @Named("entityId") private val entityId: String,
                                               @Named("location") private val location: String,
                                               @Named("section") private val section: String) :
        BundleUsecase<Long> {
    override fun invoke(bundle: Bundle): Observable<Long> {
        check(bundle.getSerializable(B_AD_ENTITY) != null)
        val ad = bundle.getSerializable(B_AD_ENTITY) as BaseAdEntity
        val oldAdId = bundle.getString(B_OLD_AD_ID)

        return Observable.fromCallable {
            fetchDao.replaceAd(toPostEntity(ad), oldAdId, entityId, location, section)
        }
    }

    companion object {
        const val B_OLD_AD_ID = "prevPostId"

        fun bundle(baseAdEntity: BaseAdEntity, oldAdId: String?): Bundle {
            return bundleOf(B_AD_ENTITY to baseAdEntity, B_OLD_AD_ID to oldAdId)
        }
    }

    private fun toPostEntity(ad: BaseAdEntity): PostEntity {
        return PostEntity(id = ad.uniqueAdIdentifier, format = ad.i_format(), subFormat = ad.i_subFormat())
    }
}

/**
 * If adId available, removes particular ad, else
 * Reset case : Removes all ads from db for this list.
 *
 * @author raunak.yadav
 */
class ClearAdsDataUsecase @Inject constructor(private val fetchDao: FetchDao,
                                              @Named("entityId") private val entityId: String,
                                              @Named("location") private val location: String,
                                              @Named("section") private val section: String) :
        BundleUsecase<Unit> {

    override fun invoke(bundle: Bundle): Observable<Unit> {
        val adId = bundle.getString(B_AD_ID)
        val reported = bundle.getBoolean(B_AD_REPORTED)
        val isDetailPage = bundle.getBoolean(B_IS_DETAIL_PAGE)

        return Observable.fromCallable {
            when {
                isDetailPage -> fetchDao.clearDetailPageAds(entityId, location, section)
                adId.isNullOrBlank() -> fetchDao.clearAds(entityId, location, section)
                reported -> fetchDao.removeAdOnDislike(adId)
                else -> fetchDao.removeAd(adId, entityId, location, section)
            }
        }
    }

    companion object {
        const val B_AD_ID = "adId"
        const val B_AD_REPORTED = "reported"
        const val B_IS_DETAIL_PAGE = "isDetailPage"

        fun bundle(adId: String?, reported: Boolean = false): Bundle {
            return bundleOf(B_AD_ID to adId, B_AD_REPORTED to reported)
        }
    }
}

/**
 * Fetch adSpecs for entityIds.
 */
class FetchAdSpecUsecase @Inject constructor(private val fetchDao: FetchDao,
                                             @Named("section") private val section: String?) :
        MediatorUsecase<List<String?>, Map<String, AdSpec>> {

    private val liveData = MediatorLiveData<Result0<Map<String, AdSpec>>>()
    private lateinit var currentSource: LiveData<List<AdSpecEntity>>

    override fun execute(t: List<String?>): Boolean {
        val result = HashMap<String, AdSpec>()
        val validIds = t.filterNotNull()
        if (validIds.isEmpty()) {
            liveData.postValue(Result0.success(result))
            return false
        }
        if (::currentSource.isInitialized) {
            liveData.removeSource(currentSource)
        }
        val newSource = if (section == null) fetchDao.adSpecOf(validIds) else fetchDao.adSpecOf(validIds, section)
        liveData.addSource(newSource) { adSpecs ->
            adSpecs.forEach {
                if (result.containsKey(it.entityId) && it.section == AdSpecEntity.SECTION_ANY) {
                    // Do not overwrite section-specific data with default handshake value.
                    return@forEach
                }
                result[it.entityId] = it.adSpec
            }
            liveData.postValue(Result0.success(result))
        }
        currentSource = newSource
        return true
    }

    override fun data(): LiveData<Result0<Map<String, AdSpec>>> = liveData

}

class ReportAdsMenuUsecase : BundleUsecase<Boolean> {

    private val reportAdsMenuApi = RestAdapterProvider
            .getRestAdapter(Priority.PRIORITY_HIGH, null, false)
            .create(ReportAdsMenuAPI::class.java)

    companion object {
        const val DATA_TO_SEND = "data_to_send"
        const val REQUEST_URL = "request_url"
    }

    override fun invoke(p1: Bundle): Observable<Boolean> {
        val dataToSend = p1.getString(DATA_TO_SEND, null)
        val url = p1.getString(REQUEST_URL, null)
        if(CommonUtils.isEmpty(dataToSend) || CommonUtils.isEmpty(url)){
            return  Observable.just(false)
        }
        return reportAdsMenuApi.postReportAdsMenu(url, ReportAdsMenuPostBodyEntity
        (ClientInfoHelper.getClientId(), dataToSend)).map {
            it.isSuccessful
        }
    }
}



class ImmersiveRuleUpdate constructor(private val immersiveRuleDao: ImmersiveRuleDao): BundleUsecase<Boolean> {

    override fun invoke(p1: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            val adEntity = p1.getSerializable(B_AD_ENTITY) as? BaseAdEntity
            val adPlayedImmersive = p1.getBoolean(B_AD_PLAYED_IMMERSIVE)
            check(adEntity != null)
            val adPos = p1.getInt(B_AD_POS)

            val playedImmersive = immersiveRuleDao.getImmersiveAd(adEntity.i_id())?.playedInImmersive ?: false
            if(!playedImmersive) {
                immersiveRuleDao.insReplace(ImmersiveAdRuleEntity(
                        adDistance = if(AdsUtil.isIMAVideoAd(adEntity))(adEntity as ExternalSdkAd).immersiveViewDistance else -1,
                        adId = adEntity.i_id(),
                        adPosition = adPos,
                        playedInImmersive = adPlayedImmersive))
            }
            true
        }
    }

    companion object {
        val B_AD_PLAYED_IMMERSIVE = "adPlayedInImmersive"
    }
}

/**
 * Persist ads that have cacheType = persist for across session usage.
 *
 * @author raunak.yadav
 */
class PersistAdUsecase constructor(private val adsDao: AdsDao) : Usecase<List<AdsFallbackEntity>, Boolean> {

    override fun invoke(p1: List<AdsFallbackEntity>): Observable<Boolean> {
        return Observable.fromCallable {
            var count = 0
            val entities = mutableListOf<PersistedAdEntity>()
            p1.forEach { afe ->
                val adPosition = afe.baseAdEntities.firstOrNull()?.adPosition
                    ?: return@forEach
                val adGroupId = afe.adGroupId ?: return@forEach

                afe.baseAdEntities.forEach { ad ->
                    val adJson = JsonUtils.toJson(ad)
                    if (!adJson.isNullOrBlank()) {
                        entities.add(PersistedAdEntity(adId = ad.uniqueAdIdentifier,
                            adGroupId = adGroupId,
                            campaignId = ad.campaignId ?: "",
                            adPosition = adPosition.value,
                            adContentType = ad.i_type(),
                            adJson = adJson))
                    }
                }
                count++
            }
            AdLogger.d(PERSIST_ADS_TAG, "Persisting ads : ${entities.map { it.adId }}")
            adsDao.insReplace(entities)
            return@fromCallable count > 0
        }
    }
}

/**
 * Fetch previously persisted ads for use in current session.
 *
 * @author raunak.yadav
 */
class ReadPersistedAdUsecase constructor(private val adsDao: AdsDao) : Usecase<AdPosition, List<AdsFallbackEntity>> {

    override fun invoke(adPosition: AdPosition): Observable<List<AdsFallbackEntity>> {
        return Observable.fromCallable {
            val adGroups = HashMap<String, AdsFallbackEntity>()
            adsDao.fetch(adPosition.value).forEach {
                val adContentType = AdContentType.fromName(it.adContentType)
                val clazz = AdProcessorFactory.fromAdContentType(adContentType)
                val adEntity = JsonUtils.fromJson<BaseDisplayAdEntity>(it.adJson, clazz)
                adEntity?.let { ad ->
                    if (!adGroups.containsKey(it.adGroupId)) {
                        adGroups[it.adGroupId] = AdsFallbackEntity().also { afe ->
                            afe.clubType = ad.clubType
                        }
                    }
                    adGroups[it.adGroupId]?.addBaseAdEntity(ad)
                }
            }

            AdLogger.d(PERSIST_ADS_TAG, "Reading ads $adPosition: ${adGroups.size}")
            adGroups.values.toList()
        }
    }
}

/**
 * Remove persisted ads that have been seen or expired.
 *
 * @author raunak.yadav
 */
class RemovePersistedAdUsecase constructor(private val adsDao: AdsDao) : Usecase<Bundle, Boolean> {

    override fun invoke(bundle: Bundle): Observable<Boolean> {
        val adGroupId = bundle.getString(B_AD_GROUP_ID)
        val adPosition = bundle.getString(B_AD_POSITION)

        return Observable.fromCallable {
            if (adGroupId != null) {
                AdLogger.d(PERSIST_ADS_TAG, "Deleting adGroup : $adGroupId")
                adsDao.delete(adGroupId) > 0
            } else if (adPosition != null) {
                AdLogger.d(PERSIST_ADS_TAG, "Deleting adPosition : $adPosition")
                adsDao.deleteAdsForAdPosition(adPosition) > 0
            } else false
        }
    }
    companion object {
        const val B_AD_GROUP_ID = "adGroupId"
        const val B_AD_POSITION = "adPosition"

        fun bundle(adGroupId: String? = null, adPosition: AdPosition? = null): Bundle {
            return bundleOf(B_AD_GROUP_ID to adGroupId, B_AD_POSITION to adPosition?.value)
        }
    }
}
private const val PERSIST_ADS_TAG = "PersistedAds"