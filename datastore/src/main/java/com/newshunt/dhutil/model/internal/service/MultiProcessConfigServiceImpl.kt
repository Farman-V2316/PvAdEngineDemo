package com.newshunt.dhutil.model.internal.service

import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.DummyDisposable
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.multiprocess.MultiProcessConfig
import com.newshunt.dataentity.dhutil.model.entity.multiprocess.MultiProcessConfigurationResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dhutil.model.internal.rest.MultiProcessAPI
import com.newshunt.dhutil.model.service.MultiProcessConfigService
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by karthik.r on 11/12/18.
 */
object MultiProcessConfigServiceImpl : MultiProcessConfigService {

    private var isMultiProcessModeEnabled = false
    private var killProcessBGDuration: Int = -1
    private var killProcessFGDuration: Int = -1
    private var scheduleP2BeforeExit = false
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<MultiProcessConfigurationResponse>>()

    override fun isMultiProcessModeEnabled(): Boolean {
        return isMultiProcessModeEnabled && AppConfig.getInstance().isMultiProcessEnabled
    }

    override fun getKillProcessBGDuration(): Int {
        return killProcessBGDuration
    }

    override fun getKillProcessFGDuration(): Int {
        return killProcessFGDuration
    }

    init {
        loadLastKnownValues()
    }

    fun refreshData(): Observable<Unit> {
        return Observable.fromCallable {
            val version = VersionedApiHelper.getLocalVersion(entityType = VersionEntity.MULTI_PROCESS_CONFIG.name)
            return@fromCallable if (version == null) Constants.EMPTY_STRING else version
        }.flatMap { version ->
            val multiProcessAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(MultiProcessAPI::class.java)
            multiProcessAPI.getMultiProcessConfiguration(version = version)
                    .map { handleResponse(it) }
        }
    }

    private fun handleResponse(apiResponse: ApiResponse<MultiProcessConfigurationResponse>?) {
        if (apiResponse != null && apiResponse.data != null && !CommonUtils.isEmpty(apiResponse.data.version)) {
            // Validate and populate data that are relevant
            var configurationFound = false
            // Search for suitable configuration
            if (apiResponse.data?.rows != null) {
                val manufacturer = DeviceInfoHelper.getDeviceInfo().manufacturer.toLowerCase()
                if (!DataUtil.isEmpty(manufacturer)) {
                    for (config in apiResponse.data?.rows!!) {
                        if ((manufacturer.equals(config.manufacturer.toLowerCase()) || DataUtil.isEmpty(config.manufacturer)) &&
                                (config.apiVersionStart == -1 || config.apiVersionStart <= Build.VERSION.SDK_INT) &&
                                (config.apiVersionEnd == -1 || config.apiVersionEnd >= Build
                                        .VERSION.SDK_INT)) {
                            configurationFound = true
                            val multiProcessConfigForDevice = Gson().toJson(config)
                            PreferenceManager.savePreference(GenericAppStatePreference
                                    .MULTI_PROCESS_CONFIGURATION_FOR_DEVICE, multiProcessConfigForDevice)
                            loadValues(config)
                            break;
                        }
                    }
                }
            }

            if (!configurationFound) {
                // If not suitable configuration found, disable entire feature
                isMultiProcessModeEnabled = false
                PreferenceManager.remove(GenericAppStatePreference.MULTI_PROCESS_CONFIGURATION_FOR_DEVICE)
            }

            if (!isMultiProcessModeEnabled) {
                // if entire feature is turned off, reset and ignore all configurations
                killProcessBGDuration = -1
                killProcessFGDuration = -1
                scheduleP2BeforeExit = false
            }
        }
    }

    private fun loadValues(config: MultiProcessConfig) {
        isMultiProcessModeEnabled = config.isEnabled
        killProcessBGDuration = config.killProcessBGDuration
        killProcessFGDuration = config.killProcessFGDuration
    }

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) {
            return Constants.EMPTY_STRING
        }
        try {
            val type = object : TypeToken<ApiResponse<MultiProcessConfigurationResponse>>() {}.type
            val response = CommonUtils.GSON.fromJson<ApiResponse<MultiProcessConfigurationResponse>>(json, type)
            if (response == null || response.data == null) {
                return Constants.EMPTY_STRING
            }
            val versionDbEntity = VersionDbEntity(entityType = VersionEntity.MULTI_PROCESS_CONFIG.name, data = json.toByteArray(),
                    langCode = UserPreferenceUtil.getUserLanguages(), version = response.data.version)
            versionedApiHelper.insertVersionDbEntity(versionDbEntity)
            return response.data.version
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
        return Constants.EMPTY_STRING
    }

    private fun loadLastKnownValues() {
        try {
            val multiProcessConfigForDevice = PreferenceManager.getPreference(GenericAppStatePreference.MULTI_PROCESS_CONFIGURATION_FOR_DEVICE, Constants
                    .EMPTY_STRING)
            val config = Gson().fromJson(multiProcessConfigForDevice, MultiProcessConfig::class.java)
            loadValues(config)
        } catch (e: Exception) {
            Logger.caughtException(e)

        }
    }
}