package com.newshunt.dhutil.model.internal.service

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUpgradeInfo
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.internal.rest.PlayerUpgradeAPI
import com.newshunt.dhutil.model.service.PlayerUpgradeService
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Single

/**
 * @author jayanth
 */
class PlayerUpgradeServiceImpl : PlayerUpgradeService {

    private val apiEntity = VersionedApiEntity(VersionEntity.PLAYERS_CONFIG)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<PlayerUpgradeInfo>>()

    private fun validate(json: String): String {
        if (TextUtils.isEmpty(json)) Constants.EMPTY_STRING
        try {
            val type = object : TypeToken<ApiResponse<PlayerUpgradeInfo>>() {}.type
            val response = Gson().fromJson<ApiResponse<PlayerUpgradeInfo>>(json, type)
            return if (response == null || response.data == null) {
                Constants.EMPTY_STRING
            } else {
                val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType, data = json.toByteArray(),
                        version = response.data.version, langCode = UserPreferenceUtil.getUserLanguages())
                versionedApiHelper.insertVersionDbEntity(versionDbEntity)
                response.data.version
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    override fun getPlayerUpgradeResponse(): Single<PlayerUpgradeInfo> {
        val priority = Priority.PRIORITY_HIGH
        return Single.fromCallable {
            val version = VersionedApiHelper.getLocalVersion(apiEntity.entityType)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap { version ->
            val eventsAPI = RestAdapterProvider.getRestAdapter(priority, null,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(PlayerUpgradeAPI::class.java)
            eventsAPI.playerInfo.map { if (it == null) throw Exception("data is null") else transform(it)}
        }
    }

    override fun getPlayerInfoLocal(): Single<PlayerUpgradeInfo> {
        val type = object : TypeToken<ApiResponse<PlayerUpgradeInfo>>() {}.type
        return Single.fromObservable(
                versionedApiHelper.fromCacheKt(entityType = apiEntity.entityType, classOfT = type)
                        .map { apiResponseVersionData -> apiResponseVersionData.data })
    }

    private fun transform(playerApiResponse: ApiResponse<PlayerUpgradeInfo>): PlayerUpgradeInfo {
        return playerApiResponse.data
    }
}