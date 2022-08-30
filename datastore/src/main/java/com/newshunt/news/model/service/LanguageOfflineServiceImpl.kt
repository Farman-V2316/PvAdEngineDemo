package com.newshunt.news.model.service

import com.google.gson.reflect.TypeToken
import com.newshunt.dataentity.common.model.entity.language.Language
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.dhutil.model.entity.language.LanguageMultiValueResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper

class LanguageOfflineServiceImpl : LanguageOfflineService {

	override fun getLanguages(edition: String?): LanguageMultiValueResponse {
		val versionedApiHelper = VersionedApiHelper<ApiResponse<MultiValueResponse<Language>>>()
		val apiEntity = VersionedApiEntity(VersionEntity.LANGUAGE)
		val type = object : TypeToken<ApiResponse<MultiValueResponse<Language>>>() {}.type
		return LanguageMultiValueResponse(versionedApiHelper.getLocalEntity(entityType = apiEntity.entityType, classOfT = type)?.data)
	}
}