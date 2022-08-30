/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.appsection

import androidx.lifecycle.LiveData
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.FileUtil
import com.newshunt.common.helper.common.ImageDownloadManager
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.NHJsonTypeAdapter
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.ImageDownloadTask
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.model.entity.UserAppSection
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionInfo
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionsResponse
import com.newshunt.dataentity.dhutil.model.entity.appsection.RecentLaunchList
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.processor.PreferencedResponseProcessor
import com.newshunt.dhutil.model.internal.service.AppSectionsServiceImpl
import com.newshunt.dhutil.model.sqlite.BottomBarDbInstance
import com.newshunt.dhutil.model.sqlite.BottomBarEntity
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.dhutil.zipWith
import java.io.File
import java.util.Arrays
import java.util.HashMap
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * A Provider class to provide all server configured app sections
 *
 * @author santhosh.kc
 */
object AppSectionsProvider : PreferencedResponseProcessor.APIResponseRequester<AppSectionsResponse> {

    private val uniqueRequestId = UniqueIdHelper.getInstance().generateUniqueId()

    private const val TAG = "AppSectionsProvider"

    const val IMAGE_TASK_TAG = "BottomBarIcons_"

    @JvmField
    val ICONS_SAVE_FOLDER = CommonUtils.getApplication().filesDir.absolutePath + File.separator + "bottombaricons"

    private val executor = Executors.newSingleThreadExecutor()
    private val processor: PreferencedResponseProcessor<AppSectionsResponse>

    private val appSectionsService = AppSectionsServiceImpl()

    init {
        val bottomIconsFolder = File(ICONS_SAVE_FOLDER)
        if (!bottomIconsFolder.exists()) {
            Logger.d(TAG,"Bottom bar folders directory does not exist, so creating one")
            bottomIconsFolder.mkdir()
        }

        val apiResponseType = object : TypeToken<ApiResponse<AppSectionsResponse>>() {

        }.type
        val appSectionInfoListType = object : TypeToken<List<AppSectionInfo>>() {

        }.type
        processor = PreferencedResponseProcessor(this, AppSectionsResponse::class.java, apiResponseType,
                NHJsonTypeAdapter<List<AppSectionInfo>>(appSectionInfoListType))

        migrateToDBIfRequired()
    }

    private fun migrateToDBIfRequired() {

        Logger.d(TAG, "MigrateToDBIfRequired entry..")

        if (!PreferenceManager.containsPreference(GenericAppStatePreference.APP_SECTIONS_RESPONSE)) {
            Logger.d(TAG, "No prev shared preference found, so no need to migrate to DB")
            return
        }

        val appSectionsResponseFromPref = PreferenceManager
                .getPreference(GenericAppStatePreference.APP_SECTIONS_RESPONSE, Constants.EMPTY_STRING)

        if (CommonUtils.isEmpty(appSectionsResponseFromPref)) {
            return
        }

        CommonUtils.runInBackground {
            Logger.d(TAG, "Json string from shared pref: $appSectionsResponseFromPref")

            val appSectionsResponse = processor.parseSharedPrefResponse(appSectionsResponseFromPref)

            val bottomBarEntity = BottomBarEntity(timeStamp = System.currentTimeMillis(),
                    version = appSectionsResponse.version, json = appSectionsResponseFromPref)
            bottomBarEntity.version = "0"
            BottomBarDbInstance.bottomBarDao().writeResponse(bottomBarEntity)

            PreferenceManager.remove(GenericAppStatePreference.APP_SECTIONS_RESPONSE)

            Logger.d(TAG, "Migrated the shared preference to DB and deleted shared pref..")
            Logger.d(TAG, "MigrateToDBIfRequired exit..")
        }
    }

    private val visitedAppSectionsInSession: HashMap<String, UserAppSection> =
            getLastVisitedSectionsInfo()

    private val appSectionsFromDB = BottomBarDbInstance.bottomBarDao().readResponse()

    private val bottomBarEntityFromDB: BottomBarEntity?
        get() {
            return appSectionsFromDB.value?.firstOrNull()
                    ?: try {
                        Logger.d(TAG, "Since live data is not filled, reading from DB")
                        val entities = AndroidUtils.IO_THREAD_POOL
                                .submit(Callable<List<BottomBarEntity>> {
                                    BottomBarDbInstance.bottomBarDao().getResponseData() })
                                .get(3000, TimeUnit.MILLISECONDS)
                        entities?.firstOrNull()
                    } catch (e: Exception) {
                        Logger.d(TAG, "Not present in live data, some exception occured while " +
                                "reading from Bottom bar db, so returning Empty version..")
                        Logger.caughtException(e)
                        null
                    }
        }

    val appSectionsObserver: LiveData<AppSectionsResponse> =
            appSectionsFromDB.zipWith(AppSettingsProvider.userLanguagesData) { r,s -> transform(r,s) }

    // Please don't make this variable as public or use getter, always use appSectionsObserver
    // LiveData
    private val appSectionsResponse: AppSectionsResponse
        get() {
            Logger.d(TAG, "AppSectionsResponse: appSectionsObserver.value is null? " +
                    (appSectionsObserver.value == null))

            return appSectionsObserver.value
                    ?: try {
                        Logger.d(TAG, "Since live data is not filled, reading from DB")
                        AndroidUtils.IO_THREAD_POOL.submit(Callable<AppSectionsResponse> {
                            transform(BottomBarDbInstance.bottomBarDao().getResponseData(),
                                    UserPreferenceUtil.getUserLanguages())
                        }).get(3000, TimeUnit.MILLISECONDS)
                    } catch (ex: Exception) {
                        Logger.d(TAG, "Not present in live data, some exception occured while " +
                                "reading from Bottom bar db, so returning default " +
                                "AppSectionResponse")
                        return getDefaultAppSectionsReponse(UserPreferenceUtil.getUserLanguages())
                    }
        }

    private fun transform(bottomBarEntities: List<BottomBarEntity>?, userLanguages: String?):
            AppSectionsResponse {
        Logger.d(TAG, " Transformation function - entry on thread: " + Thread.currentThread().name)

        val response = if (!CommonUtils.isEmpty(bottomBarEntities))
            bottomBarEntities!![0].json
        else
            Constants.EMPTY_STRING

        var appSectionsResponseFromDB = processor.parseSharedPrefResponse(response)

        val isValidResponse = AppSectionsFilter.filterAndValidateResponse(appSectionsResponseFromDB)
        val isValidForLanguage = AppSectionsFilter
                .filterResponseOnLanguage(appSectionsResponseFromDB, userLanguages
                        ?: UserPreferenceUtil.getUserLanguages())

        if (!isValidResponse || !isValidForLanguage) {
            if (!isValidResponse) {
                Logger.d(TAG, " Found the response from DB as not valid, and resetting Versioned " +
                        "DB" + " entry..")
                resetVersionedEntityInDB()
            } else {
                Logger.d(TAG, " On applying Lang filter, none of sections are valid for user langs")
            }

            Logger.d(TAG, " So reading default app sections provider")
            appSectionsResponseFromDB = getDefaultAppSectionsReponse(userLanguages
                    ?: UserPreferenceUtil.getUserLanguages())
        }

        // Rearrange by global priority
        val rearrangedSections = rearrangeSections(appSectionsResponseFromDB.sections)
        appSectionsResponseFromDB.sections.clear()
        appSectionsResponseFromDB.sections.addAll(rearrangedSections)

        updateVisitedInfoForServerConfiguredSections(appSectionsResponseFromDB)
        Logger.d(TAG, " Transformation function - exit")

        return scheduleDownloadJobForMissingIcons(appSectionsResponseFromDB)
    }

    /**
     * 1. Client will always sort on globalpriority  order, and server to mandatory send globalpriority order
     * 2. Server agreed to send index of item as global priority order to avoid corner cases
     *
     * Add all items from list in following conditions:
     * -> Identify replaceable items and figure out positions which are replaceable
     * -> Loop through by global priority and pick it if the position need not be replacebale
     * -> Group positions that need replacement and pick eligible candiates, sort by global
     * priority and assign them to available positions.
     */
    private fun rearrangeSections(sections: List<AppSectionInfo>): Collection<AppSectionInfo> {
        val reorderPositions = mutableListOf<Int>()
        val originalList = mutableListOf<AppSectionInfo>()
        val newSections = mutableListOf<AppSectionInfo>()
        val hardFixedPositions = mutableMapOf<Int, AppSectionInfo>()
        val replaceableIds = mutableListOf<String>()


        originalList.addAll(sections)
        originalList.sortWith(Comparator { o1, o2 ->
            o1.globalPriority - o2.globalPriority
        })

        originalList.forEachIndexed { index, appSectionInfo ->
            if (!appSectionInfo.isReplaceable) {
                hardFixedPositions[index] = appSectionInfo
            }
            else {
                replaceableIds.add(appSectionInfo.id)
            }
        }


        originalList.forEachIndexed { index, appSectionInfo ->
            if (appSectionInfo.isReplaceable) {
                reorderPositions.add(index)
            }
        }

        // Read & Update last access time in shared preference
        val sectionLaunchList = PreferenceManager.getPreference(AppStatePreference.RECENT_SECTION_LAUNCH_LIST,
                Constants.EMPTY_STRING)
        val recentLaunchListUpdated = ArrayList<RecentLaunchList>()
        val recentLaunchList = JsonUtils.fromJson<List<RecentLaunchList>>(sectionLaunchList, object :
                TypeToken<List<RecentLaunchList>>() {}.type)
        recentLaunchList?.forEach {
            // Remove entries older than 7 days
            if (it.time > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)) {
                recentLaunchListUpdated.add(it)
            }
        }

        PreferenceManager.savePreference(AppStatePreference.RECENT_SECTION_LAUNCH_LIST,
                JsonUtils.toJson(recentLaunchListUpdated))

        // Find remaining item ids
        val sectionIds = ArrayList<String>()
        originalList.forEach {
            sectionIds.add(it.id)
        }

        // Initialize all counter values
        val counter : HashMap<String, Int> = HashMap()
        recentLaunchListUpdated.forEach {
            if (sectionIds.contains(it.id)) {
                var count = counter[it.id] ?: -1
                counter[it.id] = ++count
            }
        }

        // Add items that are not having zero counter
        originalList.forEach {
            if (counter[it.id] == null) {
                counter[it.id] = 0
            }
        }

        // Hard fixed position dont get to move
        hardFixedPositions.values.forEach {
            counter.remove(it.id)
        }

        var currentItemIndex = 0
        while (originalList.isNotEmpty()) { // Run as long as we have pending items
            if (!reorderPositions.contains(currentItemIndex)) {
                val hardFixItemToAdd = hardFixedPositions[currentItemIndex]
                if (hardFixItemToAdd != null) {
                    // Add hard fixed item and remove from pending list
                    newSections.add(hardFixItemToAdd)
                    sectionIds.remove(hardFixItemToAdd.id)
                    counter.remove(hardFixItemToAdd.id)
                    originalList.removeAll(newSections)
                    hardFixedPositions.remove(currentItemIndex)
                    currentItemIndex++
                }
                else {
                    // Just add it and remove from pending list
                    newSections.add(originalList[0])
                    sectionIds.remove(originalList[0].id)
                    counter.remove(originalList[0].id)
                    originalList.removeAll(newSections)
                    currentItemIndex++
                }
            }
            else {
                // Need to find sequence of replace-able items and put frequent items
                // Add chunk of replaceable(s)
                var numberOfItemsToAdd = 0
                while (reorderPositions.contains(currentItemIndex) &&
                        currentItemIndex < sections.size) {
                    currentItemIndex++
                    numberOfItemsToAdd++
                }

                val chunkOfReplaceables = mutableListOf<AppSectionInfo>()
                for (k in 0 until numberOfItemsToAdd) {
                    var topCount = -1
                    var topElementId: String? = null
                    counter.entries.forEach {
                        if (topCount < it.value) {
                            topCount = it.value
                            topElementId = it.key
                        }
                        else if (topCount == it.value) {
                            val newItemKey = it.key
                            // Both same count, so pick using global priority
                            val existingTopGlobalPriority = originalList.filter {currentItem ->
                                currentItem.id == topElementId
                            }[0].globalPriority

                            val newItemGlobalPriority = originalList.filter {currentItem ->
                                currentItem.id == newItemKey
                            }[0].globalPriority

                            if (existingTopGlobalPriority > newItemGlobalPriority) {
                                topElementId = newItemKey
                            }
                        }
                    }

                    if (topElementId != null) {
                        counter.remove(topElementId!!)
                        originalList.forEach {
                            if (it.id == topElementId) {
                                chunkOfReplaceables.add(it)
                            }
                        }

                    }
                }

                // Second and third items should ordered by global priority to prevent frequent fluctuation
                chunkOfReplaceables.sortWith(Comparator { o1, o2 ->
                    o1.globalPriority - o2.globalPriority
                })

                newSections.addAll(chunkOfReplaceables)
                originalList.removeAll(chunkOfReplaceables)
            }
        }

        return newSections
    }

    fun saveAppSectionsResponseToDB(version: String, serverJsonResponse: String?,
                                    delta: Map<String, String>) {
        serverJsonResponse ?: return
        executor.execute {
            Logger.d(TAG, "SavingAppSectionResponse to DB for version: $version")
            val jsonData = processor.prepareDataToPersist(version, serverJsonResponse, JsonUtils.toJson(delta))
            val bottomBarEntity = BottomBarEntity(timeStamp = System.currentTimeMillis(), version =
            version, json = jsonData)
            BottomBarDbInstance.bottomBarDao().writeResponse(bottomBarEntity)
            deleteOldVersionedIcons(version)
            Logger.d(TAG, "SavingAppSectionResponse to DB - complete")
        }
    }

    fun reset() {
        saveLastVisitedSectionsInfo()
    }

    private fun getDefaultAppSectionsReponse(userLanguages: String?): AppSectionsResponse {
        val defaultAppSectionsResponse = AppSectionsResponse()
        defaultAppSectionsResponse.version = Constants.EMPTY_STRING
        defaultAppSectionsResponse.sections = DefaultAppSectionsProvider.getInstance().defaultAppSections
        AppSectionsFilter.filterResponseOnLanguage(defaultAppSectionsResponse, userLanguages)
        return defaultAppSectionsResponse
    }

    private fun resetVersionedEntityInDB() {
        CommonUtils.runInBackground {
            Logger.d(TAG, "Reset versioned Entity in DB - Entry")
            VersionedApiHelper.resetVersion(VersionEntity.APP_SECTIONS.name)
            Logger.d(TAG, "Reset versioned Entity in DB - Exit")
        }
    }

    fun resetLocalVersion() {
        Logger.d(TAG, "Reset Local Version - entry")
        val zeroBottomBarEntity = bottomBarEntityFromDB
        if (zeroBottomBarEntity == null) {
            Logger.d(TAG, " no local version found and exiting")
            return
        }

        try {
            AndroidUtils.IO_THREAD_POOL.submit(Callable<Void> {
                zeroBottomBarEntity.version = Constants.ZERO_STRING
                BottomBarDbInstance.bottomBarDao().update(zeroBottomBarEntity)
                Logger.d(TAG, "Resetted the version as zero in DB")
                null
            }).get(3000, TimeUnit.MILLISECONDS)
        } catch (e : Exception) {
            Logger.d(TAG, "Caught exeption while resetting DB version to zero")
            Logger.caughtException(e)
        }
        Logger.d(TAG, "Reset Local Version - exit")
    }

    override fun updateResponseWithDelta(response: AppSectionsResponse?, delta: String?) {
        if (response == null || CommonUtils.isEmpty(response.sections) || CommonUtils.isEmpty(delta)) {
            return
        }
        // Parsing logic when delta is a map of Url->filePath. Must add other parsing logic checks as
        // and when delta type is updated.
        val deltaType = object : TypeToken<Map<String, String>>() {

        }.type
        val imageMap = JsonUtils.fromJson<Map<String, String>>(delta, deltaType)
        if (CommonUtils.isEmpty(imageMap)) {
            return
        }

        Logger.d(TAG, "updateResponsewithdelta -> delta: $imageMap")

        for (appSectionInfo in response.sections) {
            if (!CommonUtils.isEmpty(appSectionInfo.activeIconUrl) && !CommonUtils.isEmpty(appSectionInfo
                            .inactiveIconUrl) && !CommonUtils.isEmpty(appSectionInfo.activeIconUrlNight)
                    && !CommonUtils.isEmpty(appSectionInfo.inactiveIconUrlNight)) {
                appSectionInfo.activeIconFilePath = imageMap!![appSectionInfo.activeIconUrl]
                appSectionInfo.activeIconNightFilePath = imageMap[appSectionInfo.activeIconUrlNight]
                appSectionInfo.inActiveIconFilepath = imageMap[appSectionInfo.inactiveIconUrl]
                appSectionInfo.inActiveIconNightFilePath = imageMap[appSectionInfo.inactiveIconUrlNight]

                if (!CommonUtils.isEmpty(appSectionInfo.refreshIconUrl) && !CommonUtils.isEmpty(appSectionInfo.refreshIconUrlNight)) {
                    appSectionInfo.refreshIconFilePath = imageMap[appSectionInfo.refreshIconUrl]
                    appSectionInfo.refreshIconNightFilePath = imageMap[appSectionInfo.refreshIconUrlNight]
                }
            }
        }
    }

    fun getLocalVersion(): String {
        Logger.d(TAG, "Reading Local Version - Entry")
        return bottomBarEntityFromDB?.version ?: Constants.EMPTY_STRING
    }

    /*-------------------------------------------------------------------------------------
     *   AppSectionInfo during session related function, such info regarding individual entities
     *   stored across the section in that session.
     *
     -------------------------------------------------------------------------------------*/

    private fun getSectionsIdsOfType(type: AppSection?): List<AppSectionInfo>? {
        if (type == null) {
            return null
        }
        val sectionsOfType = ArrayList<AppSectionInfo>()
        val appSectionInfos = appSectionsResponse.sections

        for (appSectionInfo in appSectionInfos) {
            if (appSectionInfo.type == type) {
                sectionsOfType.add(appSectionInfo)
            }
        }
        return sectionsOfType
    }

    fun getAnySectionOfType(type: AppSection?): AppSectionInfo? {
        val matchingSectionsOfType = getSectionsIdsOfType(type)
        return if (CommonUtils.isEmpty(matchingSectionsOfType)) {
            null
        } else matchingSectionsOfType!![0]
    }

    fun getAnyUserAppSectionOfType(type: AppSection?): UserAppSection? {
        val matchinSectionOfType = getAnySectionOfType(type) ?: return null
        return if (visitedAppSectionsInSession.containsKey(matchinSectionOfType.id)) {
            visitedAppSectionsInSession[matchinSectionOfType.id]
        } else UserAppSection.Builder().section(matchinSectionOfType.type)
                .sectionId(matchinSectionOfType.id).build()
    }

    fun getUserAppSection(sectionId: String?): UserAppSection? {
        if (CommonUtils.isEmpty(sectionId) || CommonUtils.isEmpty(appSectionsResponse.sections)) {
            return null
        }

        if (visitedAppSectionsInSession.containsKey(sectionId)) {
            return visitedAppSectionsInSession[sectionId]
        }

        val matchingAppSectionInfo = sectionId?.let { getMatchingAppSectionInfo(it) }

        return if (matchingAppSectionInfo == null)
            null
        else
            UserAppSection.Builder().section(matchingAppSectionInfo.type)
                    .sectionId(matchingAppSectionInfo.id).build()
    }

    fun getAnySection(): UserAppSection? {
        if (CommonUtils.isEmpty(appSectionsResponse.sections)) {
            return null
        }
        val appSectionInfo = appSectionsResponse.sections[0]
        return if (visitedAppSectionsInSession.containsKey(appSectionInfo.id)) {
            visitedAppSectionsInSession[appSectionInfo.id]
        } else UserAppSection.Builder().section(appSectionInfo.type)
                .sectionId(appSectionInfo.id).build()
    }

    fun updateAppSectionInfo(userAppSection: UserAppSection?) {
        if (userAppSection == null || CommonUtils.isEmpty(userAppSection.id)) {
            return
        }
        visitedAppSectionsInSession[userAppSection.id] = userAppSection
        AppUserPreferenceUtils.setAppSectionSelected(
                visitedAppSectionsInSession[userAppSection.id])
    }

    fun getLastVisitedInfo(appSectionId: String?): UserAppSection? {
        return if (CommonUtils.isEmpty(appSectionId)) {
            null
        } else visitedAppSectionsInSession[appSectionId]
    }

    private fun getMatchingAppSectionInfo(sectionId: String?): AppSectionInfo? {
        if (CommonUtils.isEmpty(sectionId) || CommonUtils.isEmpty(appSectionsResponse.sections)) {
            return null
        }
        val appSectionInfos = appSectionsResponse.sections

        var matchingAppSectionInfo: AppSectionInfo? = null
        for (appSectionInfo in appSectionInfos) {
            if (CommonUtils.equals(appSectionInfo.id, sectionId)) {
                matchingAppSectionInfo = appSectionInfo
                break
            }
        }
        return matchingAppSectionInfo
    }

    private fun updateVisitedInfoForServerConfiguredSections(appSectionsResponse: AppSectionsResponse) {

        Logger.d(TAG," UpdateVisitedInfoForServerConfig - entry")

        if (CommonUtils.isEmpty(visitedAppSectionsInSession) || CommonUtils.isEmpty(appSectionsResponse.sections)) {
            Logger.d(TAG,"already visited info is empty or appSections are empty, so return")
            return
        }

        val appSectionInfos = appSectionsResponse.sections
        val serverSectionIds = appSectionInfos.map { it.id }.toSet()

        val keys = visitedAppSectionsInSession.keys.iterator()
        while (keys.hasNext()) {
            val visitedSectionId = keys.next()
            if (!serverSectionIds.contains(visitedSectionId)) {
                keys.remove()
            }
        }

        Logger.d(TAG," UpdateVisitedInfoForServerConfig - exit")
    }

    private fun saveLastVisitedSectionsInfo() {
        val lastVisitedInfoString = JsonUtils.toJson<Map<String, UserAppSection>>(visitedAppSectionsInSession)
        PreferenceManager.savePreference(GenericAppStatePreference.APP_SECTIONS_LAST_INFO,
                lastVisitedInfoString)
    }

    fun isSectionAvailable(appSectionId: String?): Boolean {
        return if (CommonUtils.isEmpty(appSectionId) || CommonUtils.isEmpty(appSectionsResponse.sections)) {
            false
        } else appSectionId?.let { getMatchingAppSectionInfo(it) } != null
    }

    fun isSectionAvailable(appSection: AppSection?): Boolean {
        return appSection != null && getAnyUserAppSectionOfType(appSection) != null
    }

    private fun getLastVisitedSectionsInfo(): HashMap<String, UserAppSection> {
        Logger.d(TAG," read last visited sections info from pref - entry")
        val lastVisitedInfoString = PreferenceManager.getPreference(GenericAppStatePreference
                .APP_SECTIONS_LAST_INFO, Constants.EMPTY_STRING)
        if (CommonUtils.isEmpty(lastVisitedInfoString)) {
            Logger.d(TAG,"no info about prev last visited sections found..")
            return HashMap()
        }

        val type = object : TypeToken<HashMap<String, UserAppSection>>() {

        }.type
        Logger.d(TAG," read last visited sections info from pref - exit")
        return JsonUtils.fromJson<HashMap<String, UserAppSection>>(lastVisitedInfoString, type)
                ?: HashMap()
    }

    private fun scheduleDownloadJobForMissingIcons(
            appSectionsResponse: AppSectionsResponse): AppSectionsResponse {
        Logger.d(TAG,"Check for missing icons - entry")

        val appSectionInfos = appSectionsResponse.sections
        val missingUrls = HashMap<String, String?>()

        for (appSectionInfo in appSectionInfos) {
            val active = FileUtil.checkIfFileExists(appSectionInfo.activeIconFilePath)
            val activeNight = FileUtil.checkIfFileExists(appSectionInfo.activeIconNightFilePath)
            val inactive = FileUtil
                    .checkIfFileExists(appSectionInfo.inActiveIconFilepath)
            val inactiveNight = FileUtil.checkIfFileExists(appSectionInfo
                    .inActiveIconNightFilePath)
            val refresh = FileUtil
                    .checkIfFileExists(appSectionInfo.refreshIconFilePath)
            val refreshNight = FileUtil.checkIfFileExists(appSectionInfo
                    .refreshIconNightFilePath)
            if (active && inactive && activeNight && inactiveNight) {
                // refresh icon is only for news section
                if (appSectionInfo.type != AppSection.NEWS || refresh && refreshNight) {
                    continue
                }
            }

            if (CommonUtils.isEmpty(appSectionInfo.activeIconUrl) || CommonUtils.isEmpty(appSectionInfo
                            .inactiveIconUrl) || CommonUtils.isEmpty(appSectionInfo.activeIconUrlNight) ||
                    CommonUtils.isEmpty(appSectionInfo.inactiveIconUrlNight)) {
                continue
            }
            if (!active) {
                Logger.d(TAG,"File :" + appSectionInfo.activeIconFilePath + " for " +
                        "url: " + appSectionInfo.activeIconUrl + " does not exists")
                appSectionInfo.activeIconFilePath = null
                missingUrls[appSectionInfo.activeIconUrl] = null
            }
            if (!activeNight) {
                Logger.d(TAG,"File :" + appSectionInfo.activeIconNightFilePath + " for " +
                        "url: " + appSectionInfo.activeIconUrlNight + " does not exists")
                appSectionInfo.activeIconNightFilePath = null
                missingUrls[appSectionInfo.activeIconUrlNight]
            }
            if (!inactive) {
                Logger.d(TAG,"File :" + appSectionInfo.inActiveIconFilepath + " for " +
                        "url: " + appSectionInfo.inactiveIconUrl + " does not exists")
                appSectionInfo.inActiveIconFilepath = null
                missingUrls[appSectionInfo.inactiveIconUrl] = null
            }
            if (!inactiveNight) {
                Logger.d(TAG,"File :" + appSectionInfo.inActiveIconNightFilePath + " for " +
                        "url: " + appSectionInfo.inactiveIconUrlNight + " does not exists")
                appSectionInfo.inActiveIconNightFilePath = null
                missingUrls[appSectionInfo.inactiveIconUrlNight] = null
            }
            if (!refresh) {
                Logger.d(TAG,"File :" + appSectionInfo.refreshIconFilePath + " for " +
                        "url: " + appSectionInfo.refreshIconUrl + " does not exists")
                appSectionInfo.refreshIconFilePath = null
                appSectionInfo.refreshIconUrl?.let { missingUrls[appSectionInfo.refreshIconUrl] = null }
            }
            if (!refreshNight) {
                Logger.d(TAG,"File :" + appSectionInfo.refreshIconNightFilePath + " for " +
                        "url: " + appSectionInfo.refreshIconUrlNight + " does not exists")
                appSectionInfo.refreshIconNightFilePath = null
                appSectionInfo.refreshIconUrlNight?.let { missingUrls[appSectionInfo.refreshIconUrlNight] = null }
            }
        }

        if (!CommonUtils.isEmpty(missingUrls)) {
            Logger.d(TAG, "Scheduled for Missing urls: $missingUrls")
            appSectionsService.reDownloadAppSectionIcons(appSectionsResponse.version, missingUrls)
        } else {
            Logger.d(TAG,"None of icon urls are missing")
        }

        Logger.d(TAG,"Check for missing icons - exit")
        return appSectionsResponse
    }

    private fun deleteOldVersionedIcons(bottomBarDBVersion : String) {
        Logger.d(TAG,"Delete Old versioned icons - entry")
        val dbVersionFolder = VersionedApiHelper.getLocalVersion(entityType = VersionEntity.APP_SECTIONS.name)

        val bottombarIconsDir = File(ICONS_SAVE_FOLDER)
        if (!bottombarIconsDir.exists() || !bottombarIconsDir.isDirectory) {
            Logger.d(TAG,"Directory does not exists, or not a directory, so return")
            return
        }

        Logger.d(TAG, "bottomBarDBVersion is:$bottomBarDBVersion")
        Logger.d(TAG, "dbVersion: $dbVersionFolder ")

        val versionFolders = bottombarIconsDir.list()
        if (!CommonUtils.isEmpty(versionFolders)) {
            Logger.d(TAG, "Folders in the bottom bar icons directory is : "
                    + Arrays.toString(versionFolders))
        }

        for (versionFolder in versionFolders) {
            if (CommonUtils.equals(versionFolder, bottomBarDBVersion)
                    || CommonUtils.equals(versionFolder, dbVersionFolder)) {
                continue
            }

            Logger.d(TAG, "deleting folder with verison: $versionFolder")
            val tag = IMAGE_TASK_TAG + versionFolder + Constants.UNDERSCORE_CHARACTER +
                    ImageDownloadManager.Task.DELETE
            val folder = ICONS_SAVE_FOLDER + File.separator + versionFolder
            ImageDownloadManager.getInstance().addTask(ImageDownloadTask.Builder().setTag(tag).setTask(ImageDownloadManager.Task.DELETE).setFolderPath(folder)
                    .createImageDownloadTask())
        }
        Logger.d(TAG,"Delete Old versioned icons - exit")
    }
}