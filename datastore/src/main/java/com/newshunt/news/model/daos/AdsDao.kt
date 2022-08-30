/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.ads.PersistedAdEntity

/**
 * @author raunak.yadav
 */
@Dao
interface AdsDao : BaseDao<PersistedAdEntity> {

    @Query("SELECT * FROM persisted_ads WHERE adPosition = :adPosition")
    fun fetch(adPosition: String): List<PersistedAdEntity>

    @Query("DELETE FROM persisted_ads where adGroupId = :adGroupId")
    fun delete(adGroupId: String): Int

    @Query("DELETE FROM persisted_ads")
    fun deleteAll()

    @Query("DELETE FROM persisted_ads where adPosition=:adPosition")
    fun deleteAdsForAdPosition(adPosition: String): Int

}