/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/

package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.dataentity.social.entity.ImmersiveAdRuleEntity

@Dao
interface ImmersiveRuleDao : BaseDao<ImmersiveAdRuleEntity> {
    @Query("select * from immersive_ad_rule_stats")
    fun getRecentEntry(): List<ImmersiveAdRuleEntity>?

    @Query("select * from immersive_ad_rule_stats where ad_id =:adId")
    fun getImmersiveAd(adId:String): ImmersiveAdRuleEntity?

    @Transaction
    fun isEligible(entityId: String, adId: String, currentAdPos: Int): Boolean {
        val previousEntity = getRecentEntry() ?: return true
        val recentAdPlayedInImmersive = previousEntity.findLast { it.playedInImmersive } ?: return true

        return (previousEntity.size - previousEntity.indexOf(recentAdPlayedInImmersive)) > recentAdPlayedInImmersive.adDistance
    }
}