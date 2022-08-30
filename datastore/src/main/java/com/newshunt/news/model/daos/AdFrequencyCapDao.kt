/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.ads.AdFCType
import com.newshunt.dataentity.ads.AdFrequencyCapEntity
import com.newshunt.dataentity.model.entity.AdCampaignsInfo

/**
 * @author raunak.yadav
 */
@Dao
interface AdFrequencyCapDao : BaseDao<AdFrequencyCapEntity> {

    @Query("SELECT * FROM ads_frequency_cap_data WHERE campaignId = :campaignId")
    fun fetch(campaignId: String): List<AdFrequencyCapEntity>

    @Query("SELECT * FROM ads_frequency_cap_data")
    fun all(): LiveData<List<AdFrequencyCapEntity>>

    @Query("SELECT * FROM ads_frequency_cap_data")
    fun fetchAll(): List<AdFrequencyCapEntity>

    @Query("DELETE FROM ads_frequency_cap_data where campaignId IN (:campaignIds)")
    fun delete(campaignIds: List<String>): Int

    @Delete
    fun delete(adFrequencyCapEntity: AdFrequencyCapEntity)

    @Query("DELETE FROM persisted_ads where campaignId NOT IN (:campaignIds)")
    fun deleteExpiredCachedAds(campaignIds: List<String>): Int

    /**
     * 1. If adCampaigns is empty => No live campaigns. Delete all data.
     * 2. We only refresh/remove the existing data.
     * 3. New FCdata from the adCampaigns is not added because the api response contains all live
     * campaigns' and banners' data and all might not have been served to a user. So, the table has
     * entries only for received ads which are refreshed periodically by the Fetch API.
     * If the received ads had no cap, we still make an entry with -1 cap to allow refresh from
     * api response later.
     */
    @Transaction
    fun updateCampaignInfo(adCampaigns: AdCampaignsInfo) {
        if (adCampaigns.campaigns.isNullOrEmpty()) return

        //Remove persisted ads of expired campaigns.
        adCampaigns.campaigns?.let { campaigns ->
           val count = deleteExpiredCachedAds(campaigns.map { it.key })
            Logger.d("AdFrequencyCapDao", "Deleted $count expired campaigns.")
        }

        val dbCampaigns = fetchAll()
        if (dbCampaigns.isNullOrEmpty()) {
            Logger.d("AdFrequencyCapDao", "No cached data in DB.")
            return
        }

        val modifiedEntries = ArrayList<AdFrequencyCapEntity>()
        val expiredCampaigns = ArrayList<String>()
        dbCampaigns.forEach {
            if (adCampaigns.campaigns!!.containsKey(it.campaignId)) {
                //Update data from the API response
                adCampaigns.campaigns?.get(it.campaignId)?.also { campaignData ->
                    when (it.type) {
                        AdFCType.CAMPAIGN -> {
                            if (it.cap != campaignData.fcData?.cap || it.resetTime != campaignData.fcData?.resetTime) {
                                it.cap = campaignData.fcData?.cap ?: -1
                                it.resetTime = campaignData.fcData?.resetTime ?: -1L
                                modifiedEntries.add(it)
                            }
                        }
                        AdFCType.BANNER -> {
                            campaignData.banners?.get(it.capId)?.let { bannerData ->
                                if (it.cap != bannerData.fcData?.cap || it.resetTime != bannerData.fcData?.resetTime) {
                                    it.cap = bannerData.fcData?.cap ?: -1
                                    it.resetTime = bannerData.fcData?.resetTime ?: -1L
                                    modifiedEntries.add(it)
                                }
                            }
                        }
                    }
                }
            } else {
                // campaign expired. Remove it.
                expiredCampaigns.add(it.campaignId)
            }
        }
        delete(expiredCampaigns)
        insReplace(modifiedEntries)
    }
}