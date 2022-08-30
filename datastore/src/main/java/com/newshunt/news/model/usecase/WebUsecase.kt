package com.newshunt.news.model.usecase

import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.social.entity.AdSpecEntity
import com.newshunt.news.model.daos.FetchDao
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

class FetchWebDataUsecase @Inject constructor(@Named("entityId") val entityId: String,
                                              @Named("section") val section: String,
                                              @Named("contentRequestMethod") val contentRequestMethod: String,
                                              @Named("fetchUsecase") val fetchUsecase: BundleUsecase<NLResponseWrapper>,
                                              private val fetchDao: FetchDao) : Usecase<String, List<PostEntity>> {

    override fun invoke(p1: String): Observable<List<PostEntity>> {
        return fetchUsecase.invoke(FetchCardListFromUrlUsecase.bundle(p1, contentRequestMethod, entityId))
                .map {
                    it.nlResp.adSpec?.let { adSpec ->
                        fetchDao.insReplaceAdSpec(listOf(AdSpecEntity(entityId = entityId, section = section, adSpec = adSpec, inHandshake = false)))
                    }
                    it.nlResp.rows.filterIsInstance(PostEntity::class.java)
                }
    }
}