package com.newshunt.dhutil.model.internal.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.ImageDownloadManager
import com.newshunt.common.helper.common.ImageSaveFailureReason
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.model.ImageDownloadTask
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dhutil.helper.appsection.AppSectionsFilter
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionsResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.internal.rest.AppSectionsAPI
import com.newshunt.dataentity.dhutil.model.service.AppSectionsService
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import java.io.File
import java.util.HashMap

/**
 * An implementation of [AppSectionsService] to get Server configured app sections
 *
 * @author santhosh.kc
 */
class AppSectionsServiceImpl : AppSectionsService {
    val apiEntity = VersionedApiEntity(VersionEntity.APP_SECTIONS)
    private val versionedApiHelper = VersionedApiHelper<ApiResponse<AppSectionsResponse>>()
    private var serverJsonResponse: String? = null
    private var appSectionsResponseFromDB: AppSectionsResponse? = null

    private fun validate(json: String): String {
        if (CommonUtils.isEmpty(json)) Constants.EMPTY_STRING

        try {
            val type = object : TypeToken<ApiResponse<AppSectionsResponse>>() {}.type
            val apiResponse = Gson().fromJson<ApiResponse<AppSectionsResponse>>(json, type)
            if (apiResponse == null || apiResponse.data == null) {
                return Constants.EMPTY_STRING
            }
            val appSectionsResponse = apiResponse.data
            if (!AppSectionsFilter.filterAndValidateResponse(appSectionsResponse)) {
                return Constants.EMPTY_STRING
            }
            serverJsonResponse = json
            val versionDbEntity = VersionDbEntity(entityType = apiEntity.entityType, data = json.toByteArray(),
                    langCode = UserPreferenceUtil.getUserLanguages(), version = apiResponse.data.version)
            versionedApiHelper.insertVersionDbEntity(versionDbEntity)
            return apiResponse.data.version
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Constants.EMPTY_STRING
    }

    override fun updateDBFromServer() : Observable<AppSectionsResponse> {
        return Observable.fromCallable {
            val type = object : TypeToken<ApiResponse<AppSectionsResponse>>() {}.type
            val localEntity = versionedApiHelper.getLocalEntity(entityType = apiEntity.entityType, classOfT = type)
            if (localEntity != null && localEntity.data != null) {
                appSectionsResponseFromDB = localEntity.data
            }
            val version = VersionedApiHelper.getLocalVersion(entityType = apiEntity.entityType)
            if (version == null) Constants.EMPTY_STRING else version
        }.flatMap {
            val appSectionsAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null,
                    VersionedApiInterceptor({ json: String -> this.validate(json) }))
                    .create(AppSectionsAPI::class.java)
            appSectionsAPI.getAppSections(it, UserPreferenceUtil.getUserNavigationLanguage())
                    .map { return@map if (it == null) AppSectionsResponse() else transform(it) }
                    .onErrorResumeNext { t: Throwable ->  handleError(t)}
        }
    }

    private fun transform(response: ApiResponse<AppSectionsResponse>) : AppSectionsResponse {
        val appSectionsResponse = response.data
        if (AppSectionsFilter.filterAndValidateResponse(appSectionsResponse)) {
            scheduleImageDownloadJob(appSectionsResponse)
        }
        return response.data
    }

    private fun handleError(throwable: Throwable) : Observable<AppSectionsResponse> {
        scheduleImageDownloadJob(appSectionsResponseFromDB)
        return Observable.empty()
    }

    override fun reDownloadAppSectionIcons(version: String, missingUrls: Map<String, String?>) {
        val folder = AppSectionsProvider.ICONS_SAVE_FOLDER + File.separator + version
        val task = ImageDownloadTask.Builder().setTag(version)
                .setTask(ImageDownloadManager.Task.DOWNLOAD).setUrls(missingUrls).setAllMandatory(true)
                .setFolderPath(folder).createImageDownloadTask()
        ImageDownloadManager.getInstance().addTask(task)
    }

    private fun scheduleImageDownloadJob(appSectionsResponse: AppSectionsResponse?) {
        if (appSectionsResponse == null || CommonUtils.isEmpty(appSectionsResponse.sections)) {
            return
        }

        val iconUrls = HashMap<String, String?>()
        for (appSectionInfo in appSectionsResponse.sections) {
            if (!CommonUtils.isEmpty(appSectionInfo.activeIconUrl) && !CommonUtils.isEmpty(appSectionInfo
                            .inactiveIconUrl) && !CommonUtils.isEmpty(appSectionInfo.activeIconUrlNight)
                    && !CommonUtils.isEmpty(appSectionInfo.inactiveIconUrlNight)) {
                iconUrls[appSectionInfo.activeIconUrl] = null
                iconUrls[appSectionInfo.activeIconUrlNight] = null
                iconUrls[appSectionInfo.inactiveIconUrl] = null
                iconUrls[appSectionInfo.inactiveIconUrlNight] = null
                if (!CommonUtils.isEmpty(appSectionInfo.refreshIconUrl) && !CommonUtils.isEmpty(appSectionInfo.refreshIconUrlNight)) {
                    iconUrls[appSectionInfo.refreshIconUrl] = null
                    iconUrls[appSectionInfo.refreshIconUrlNight] = null
                }
            }
        }
        if (CommonUtils.isEmpty(iconUrls)) {
            return
        }
        val folder = AppSectionsProvider.ICONS_SAVE_FOLDER + File.separator + appSectionsResponse
                .version
        val tag = AppSectionsProvider.IMAGE_TASK_TAG + appSectionsResponse.version
        val task = ImageDownloadTask.Builder().setTag(tag)
                .setTask(ImageDownloadManager.Task.DOWNLOAD).setUrls(iconUrls)
                .setCallback(
                        DBVersionImageDownloadTaskCallback(appSectionsResponse, serverJsonResponse))
                .setAllMandatory(true).setFolderPath(folder).createImageDownloadTask()
        ImageDownloadManager.getInstance().addTask(task)
    }

    private class DBVersionImageDownloadTaskCallback(private val appSectionsResponseFromDB: AppSectionsResponse,
                                                     private val serverJsonResponse: String?) : ImageDownloadManager.Callback {

        override fun onSuccess(task: ImageDownloadTask?) {
            if (task == null || CommonUtils.isEmpty(task.urls)) {
                return
            }
            Logger.d("AppSectionServiceImpl", "Download of images success for version: " + appSectionsResponseFromDB.version)
            AppSectionsProvider
                    .saveAppSectionsResponseToDB(appSectionsResponseFromDB.version,
                            serverJsonResponse, task.urls)
        }

        override fun onFailure(task: ImageDownloadTask, reason: ImageSaveFailureReason?) {
            // DO NOTHING and do not update shared preferences
            if (reason != null) {
                Logger.d("AppSectionService", "Image download failed: " + reason.name)
            }
        }
    }

}