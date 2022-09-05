/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.handshake.usecase

import com.newshunt.adengine.handshake.network.AdsConfigApi
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.EntityContextMeta
import com.newshunt.dataentity.social.entity.AdSpecEntity
import com.newshunt.dhutil.helper.preference.AdsPreference
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable

/**
 * Handshake for getting fallback ContentContext for few entities.
 *
 * @author raunak.yadav
 */
class AdsContentContextHandshakeUsecase(private val adsApi: AdsConfigApi) : Usecase<String, EntityContextMeta?> {

    override fun invoke(version: String): Observable<EntityContextMeta?> {
        return adsApi.adsContentContextHandshake(version)
                .map {
                    ApiResponseUtils.throwErrorIfDataNull(it)
                    saveResponse(it.data)
                }
    }

    private fun saveResponse(response: EntityContextMeta): EntityContextMeta {
        val fetchDao = SocialDB.instance().fetchDao()
        val adSpecs = mutableListOf<AdSpecEntity>()
        response.entityContextMeta?.forEach {
            it.adSpec?.let { adSpec ->
                it.entityIds?.forEach { entityId ->
                    //Handshake values are applicable to any section.
                    adSpecs.add(AdSpecEntity(entityId = entityId, adSpec = adSpec, inHandshake = true))
                }
            }
        }
        if (adSpecs.isNotEmpty()) {
            fetchDao.insReplaceAdSpec(adSpecs)
            PreferenceManager.savePreference(AdsPreference.ADS_CONTEXT_HANDSHAKE_VERSION, response.version)
        }
        return response
    }
}
