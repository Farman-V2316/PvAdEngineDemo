/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.appupgrade

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.appview.common.profile.model.usecase.CountFilteredHistoryUsecase
import com.newshunt.common.AppStateChangeEvent
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.appupgrade.InAppUpdateAvailability
import com.newshunt.common.helper.appupgrade.InAppUpdateHelper
import com.newshunt.common.helper.appupgrade.USER_ACTION_TYPE_EXIT
import com.newshunt.common.helper.appupgrade.USER_ACTION_TYPE_SKIP
import com.newshunt.common.helper.appupgrade.USER_ACTION_TYPE_UPGRADE
import com.newshunt.common.helper.appupgrade.UpdateType
import com.newshunt.common.helper.common.ApplicationStatus
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.analytics.SessionInfo
import com.newshunt.dataentity.dhutil.model.entity.upgrade.HandshakeConfigEntity
import com.newshunt.dataentity.model.entity.InAppUpdatesEntity
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.news.model.usecase.BUNDLE_AVAILABLE_APP_VERSION
import com.newshunt.news.model.usecase.BUNDLE_IN_APP_CONFIG_ENTITIES
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.QueryCurrentSessionUsecase
import com.newshunt.news.model.usecase.QueryInAppUpdatePromptsUsecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.onboarding.helper.HandshakeCompleteEvent
import com.squareup.otto.Subscribe
import java.util.concurrent.TimeUnit

/**
 * A helper class to communicate with Google in app updates library.
 * Update availability is communicated via live data to the UI
 *
 * Created by srikanth.ramaswamy on 03/01/2021.
 */
private const val LOG_TAG = "InAppUpdateHelper"
private const val CATEGORY_VIEW = "view"
private const val CATEGORY_CLICK = "click"
private const val UPGRADE_TYPE_MANDATORY = "mandatory"
private const val UPGRADE_TYPE_FLEXIBLE = "optional"

class InAppUpdateHelperImpl(queryCurrentSessionUsecase: QueryCurrentSessionUsecase,
                            queryInAppUpdatePromptsUsecase: QueryInAppUpdatePromptsUsecase,
                            private val countSPVUsecase: CountFilteredHistoryUsecase,
                            incrementUpdatePromptUsecase: BundleUsecase<Unit>): InstallStateUpdatedListener, InAppUpdateHelper {

    private var appUpdateInfo: AppUpdateInfo? = null
    @InstallStatus
    private var installStatus: Int = InstallStatus.UNKNOWN
    @AppUpdateType
    private var updateTypeInProgress: Int = AppUpdateType.FLEXIBLE
    private var updateActionEventFired = false
    private var sessionInfo: SessionInfo? = null
    private var sessionSPVCount: Int = 0
    private var maxVersionFlexibleUpdate: Int = 0
    private var maxVersionForMandatoryUpdate: Int = 0
    private var inAppUpdateEntities: List<InAppUpdatesEntity>? = null //Prompt shown meta data from Room DB is read into this
    private val incrementUpdatePromptMediatorUC = incrementUpdatePromptUsecase.toMediator2() //Prompt shown meta data is saved in Room DB

    //Livedata to communicate with the activities regarding the availability of updates.
    private val inAppUpdateAvailabilityLD by lazy {
        MutableLiveData<InAppUpdateAvailability>()
    }

    init {
        maxVersionFlexibleUpdate = PreferenceManager.getPreference(GenericAppStatePreference.MAX_VERSION_FLEXIBLE_UPDATE, 0)
        maxVersionForMandatoryUpdate = PreferenceManager.getPreference(GenericAppStatePreference.MAX_VERSION_MANDATORY_UPDATE, 0)
        BusProvider.getUIBusInstance().register(this)
        //Keep track of session changes
        queryCurrentSessionUsecase.execute(Unit)
        queryCurrentSessionUsecase.data().distinctUntilChanged()
                .observe(ProcessLifecycleOwner.get(), Observer {
                    if(it.isFailure) {
                        return@Observer
                    }
                    sessionInfo = it.getOrNull() ?: return@Observer
                    observeHistoryCount()
                })

        //Every entry to history table is counted as an SPV and used to determine whether update prompt needs to be shown
        countSPVUsecase.data().distinctUntilChanged().observe(ProcessLifecycleOwner.get(), Observer {
            if(it.isSuccess) {
                sessionSPVCount = it.getOrNull() ?: 0
                Logger.d(LOG_TAG, "Session SPV count: $sessionSPVCount")
                checkInAppUpdate()
            }
        })

        //Query the session prompts from DB
        queryInAppUpdatePromptsUsecase.data().observe(ProcessLifecycleOwner.get(), Observer {
            if (it.isSuccess) {
                inAppUpdateEntities = it.getOrNull()
                Logger.d(LOG_TAG, "update prompt DB update, ${inAppUpdateEntities?.firstOrNull()}")
            }
        })
        queryInAppUpdatePromptsUsecase.execute(Unit)
    }

    @Subscribe
    fun onHandshake(handshakeEvent: HandshakeCompleteEvent) {
        maxVersionForMandatoryUpdate = handshakeEvent.upgradeInfo.maxVersionForMandatoryUpdateV2
        maxVersionFlexibleUpdate = handshakeEvent.upgradeInfo.maxVersionForFlexibleUpdateV2
        checkInAppUpdate()
        Logger.d(LOG_TAG, "onHandshake: maxVersionForMandatoryUpdate=${maxVersionForMandatoryUpdate}, maxVersionForFlexibleUpdate=${maxVersionFlexibleUpdate}")
    }

    @Subscribe
    fun onStaticConfigUpdate(handshakeConfigEntity: HandshakeConfigEntity) {
        Logger.d(LOG_TAG, "Static config API done")
        checkInAppUpdate()
    }

    /**
     * Listen to the App status and perform appropriate actions
     */
    @Subscribe
    fun onAppStateChangeEvent(appStateChangeEvent: AppStateChangeEvent) {
        //Start fetching the current status of updates on first activity created in the process
        if (appStateChangeEvent.isFirstActivityCreated) {
            fetchInAppUpdates()
            return
        }
        completeDownloadIfPossible()
    }

    override fun onStateUpdate(state: InstallState) {
        installStatus = state.installStatus()
        if (installStatus == InstallStatus.DOWNLOADING) {
            logInAppUpdateActionEvent(UserAction.ACTION_UPGRADE)
        }
        Logger.d(LOG_TAG, "onStateUpdate: ${mapInstallState()}")
        completeDownloadIfPossible()
    }

    /**
     * All the logic to decide what kind of update is available, resides in this function. Call
     * this function to set the updated state of the livedata
     */
    override fun checkInAppUpdate() {
        if (UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS == appUpdateInfo?.updateAvailability()) {
            Logger.d(LOG_TAG, "Marking update in progress...")
            setLiveData(InAppUpdateAvailability.UPDATE_IN_PROGRESS)
        } else if (!isUpdateAllowed()) {
            Logger.d(LOG_TAG, "Install state: ${mapInstallState()}, ignoring update.")
        } else if (UpdateAvailability.UPDATE_AVAILABLE == appUpdateInfo?.updateAvailability()) {
            val versionCode = AppConfig.getInstance().appVersionCode
            if (versionCode <= maxVersionForMandatoryUpdate && appUpdateInfo?.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) == true) {
                Logger.d(LOG_TAG, "Marking MANDATORY update available, ver: $versionCode, configVersion: $maxVersionForMandatoryUpdate")
                setLiveData(InAppUpdateAvailability.MANDATORY_UPDATE_AVAILABLE)
                return
            }
            val updatePromptFreqSecs = PreferenceManager.getPreference(GenericAppStatePreference.UPDATE_PROMPT_FREQ, 0L)
            val sessionSPVCountThreshold = PreferenceManager.getPreference(GenericAppStatePreference.SPV_COUNT_UPDATE_PROMPT, 0)
            val updatePromptShownTS = inAppUpdateEntities?.firstOrNull()?.lastPromptTs ?: 0
            //We need to show the update prompt only X number of times until a new version is available
            val promptCount: Int = inAppUpdateEntities?.firstOrNull()?.let {
                if(it.availableVersion == appUpdateInfo?.availableVersionCode()) {
                    it.promptShownCount
                } else {
                    0
                }
            }?: 0
            val maxPromptsPerUpdate = PreferenceManager.getPreference(GenericAppStatePreference.MAX_PROMPTS_PER_UPDATE, 0)
            val timeElapsed = if (updatePromptFreqSecs <= 0 || updatePromptShownTS <= 0) {
                true
            } else {
                CommonUtils.isTimeExpired(updatePromptShownTS, TimeUnit.SECONDS.toMillis(updatePromptFreqSecs), true)
            }
            if (appUpdateInfo?.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) == true &&
                    versionCode <= maxVersionFlexibleUpdate &&
                    timeElapsed &&
                    sessionSPVCount >= sessionSPVCountThreshold &&
                    promptCount < maxPromptsPerUpdate) {
                Logger.d(LOG_TAG, "Marking Flexible update available, version: $versionCode, sessionSPV: $sessionSPVCount, promptCount: $promptCount")
                setLiveData(InAppUpdateAvailability.FLEXIBLE_UPDATE_AVAILABLE)
            } else {
                Logger.d(LOG_TAG, "Marking No update available, version: $versionCode, sessionSPV: $sessionSPVCount, promptCount: $promptCount")
                setLiveData(InAppUpdateAvailability.NO_UPDATE_AVAILABLE)
            }
        } else {
            Logger.d(LOG_TAG, "No update available!!!")
        }
    }

    override fun startUpdate(activity: Activity, requestCode: Int, updateType: UpdateType) {
        _startUpdate(activity, requestCode, if(updateType == UpdateType.FLEXIBLE_UPDATE) AppUpdateType.FLEXIBLE else AppUpdateType.IMMEDIATE)
    }

    override fun continueUpdate(activity: Activity, requestCode: Int) {
        _startUpdate(activity, requestCode, AppUpdateType.IMMEDIATE, true)
    }

    override fun userCancelledUpdate() {
        val userAction = when (updateTypeInProgress) {
            AppUpdateType.FLEXIBLE -> UserAction.ACTION_SKIP
            AppUpdateType.IMMEDIATE -> UserAction.ACTION_EXIT
            else -> UserAction.ACTION_SKIP
        }
        logInAppUpdateActionEvent(userAction)
    }

    override fun getInAppUpdateAvailability(): LiveData<InAppUpdateAvailability> =  inAppUpdateAvailabilityLD

    private fun completeDownloadIfPossible() {
        if (ApplicationStatus.getVisibleActiviesCount() <= 0 && installStatus == InstallStatus.DOWNLOADED) {
            //App is in background, lets finish the downloaded update!
            Logger.d(LOG_TAG, "Completing downloaded update..")
            AppUpdateManagerFactory.create(CommonUtils.getApplication()).completeUpdate()
            setLiveData(InAppUpdateAvailability.UPDATE_IN_PROGRESS)
        }
    }

    /**
     * Helper function for logging and debugging
     */
    private fun mapAvailability(@UpdateAvailability availability: Int?): String {
        return when (availability) {
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS"
            UpdateAvailability.UPDATE_AVAILABLE -> "UPDATE_AVAILABLE"
            UpdateAvailability.UPDATE_NOT_AVAILABLE -> "UPDATE_NOT_AVAILABLE"
            else -> "UNKNOWN"
        }
    }

    /**
     * Helper function for logging and debugging
     */
    private fun mapInstallState(): String {
        return when (installStatus) {
            InstallStatus.CANCELED -> "CANCELED"
            InstallStatus.DOWNLOADED -> "DOWNLOADED"
            InstallStatus.DOWNLOADING -> "DOWNLOADING"
            InstallStatus.FAILED -> "FAILED"
            InstallStatus.INSTALLED -> "INSTALLED"
            InstallStatus.INSTALLING -> "INSTALLING"
            InstallStatus.PENDING -> "PENDING"
            else -> "UNKNOWN"
        }
    }

    /**
     * Helper function to decide whether we can currently show the update prompt
     */
    private fun isUpdateAllowed(): Boolean {
        return when (installStatus) {
            InstallStatus.DOWNLOADED,
            InstallStatus.DOWNLOADING,
            InstallStatus.INSTALLING,
            InstallStatus.PENDING,
            InstallStatus.INSTALLED -> false
            else -> true
        }
    }

    /**
     * Fetch the current status of the update
     */
    private fun fetchInAppUpdates() {
        val updateManager = AppUpdateManagerFactory.create(CommonUtils.getApplication())
        updateManager.appUpdateInfo.addOnSuccessListener { info ->
            appUpdateInfo = info
            Logger.d(LOG_TAG, "onUpdateAvailable, availability=${mapAvailability(info.updateAvailability())}, version: ${info.availableVersionCode()}, current version: ${AppConfig.getInstance().appVersionCode}")
            checkInAppUpdate()
        }
        updateManager.appUpdateInfo.addOnFailureListener {
            Logger.e(LOG_TAG, "Failed, ${it.message}")
        }
        updateManager.registerListener(this)
    }

    private fun updatePromptShown() {
        appUpdateInfo?.let {
            (inAppUpdateEntities ?: emptyList()).let { entities ->
                //Update the DB that a flexible update prompt has been shown
                incrementUpdatePromptMediatorUC.execute(bundleOf(BUNDLE_AVAILABLE_APP_VERSION to it.availableVersionCode(),
                        BUNDLE_IN_APP_CONFIG_ENTITIES to entities))
            }
        }
    }

    private fun _startUpdate(activity: Activity, requestCode: Int, @AppUpdateType updateType: Int, isCompleteInstallation: Boolean = false) {
        //To start an update, we need to fetch the latest state and ensure the update is still available.
        val manager = AppUpdateManagerFactory.create(CommonUtils.getApplication())
        manager.appUpdateInfo.addOnSuccessListener {
            try {
                Logger.d(LOG_TAG, "_startUpdate: appUpdateInfo available, activity: ${activity.javaClass.simpleName}, availability: ${mapAvailability(it.updateAvailability())}, isAllowed: ${it.isUpdateTypeAllowed(updateType)}")
                appUpdateInfo = it
                if (it.isUpdateTypeAllowed(updateType)) {
                    this.updateTypeInProgress = updateType
                    manager.startUpdateFlowForResult(it, updateType, activity, requestCode)
                    if (AppUpdateType.IMMEDIATE == updateType) {
                        activity.finish()
                    }
                    logInAppUpdateViewedEvent()
                    if (!isCompleteInstallation && updateType == AppUpdateType.FLEXIBLE) {
                        updatePromptShown()
                    }
                } else {
                    Logger.d(LOG_TAG, "_startUpdate: appUpdateInfo else case, update not allowed: : ${activity.javaClass.simpleName}")
                }
            } catch (e: Exception) { //IntentSender$SendIntentException
                //https://console.firebase.google.com/project/josh-16d9c/crashlytics/app/android:com.eterno.shortvideos/issues/1aff549b251bf518be9ff949b972c876
                Logger.caughtException(e)
            }
        }
    }

    private fun logInAppUpdateViewedEvent() {
        val eventsMap: Map<NhAnalyticsAppEventParam, Any> = mutableMapOf(
                NhAnalyticsAppEventParam.TYPE to if (updateTypeInProgress == AppUpdateType.FLEXIBLE) UPGRADE_TYPE_FLEXIBLE else UPGRADE_TYPE_MANDATORY,
                NhAnalyticsAppEventParam.CATEGORY to CATEGORY_VIEW,
                NhAnalyticsAppEventParam.CURRENT_APP_VER_CODE to AppConfig.getInstance().appVersionCode,
                NhAnalyticsAppEventParam.EXPECTED_APP_VER_CODE to (appUpdateInfo?.availableVersionCode() ?: 0))

        AnalyticsClient.log(NhAnalyticsAppEvent.IN_APP_UPGRADE, NhAnalyticsEventSection.APP, eventsMap.toMutableMap())
    }

    private fun logInAppUpdateActionEvent(userAction: UserAction) {
        if (updateActionEventFired) {
            return
        }
        val eventsMap: Map<NhAnalyticsAppEventParam, Any> = hashMapOf(
                NhAnalyticsAppEventParam.TYPE to if (updateTypeInProgress == AppUpdateType.FLEXIBLE) UPGRADE_TYPE_FLEXIBLE else UPGRADE_TYPE_MANDATORY,
                NhAnalyticsAppEventParam.CATEGORY to CATEGORY_CLICK,
                NhAnalyticsAppEventParam.USER_ACTION to userAction.actionStr,
                NhAnalyticsAppEventParam.CURRENT_APP_VER_CODE to AppConfig.getInstance().appVersionCode,
                NhAnalyticsAppEventParam.EXPECTED_APP_VER_CODE to (appUpdateInfo?.availableVersionCode() ?: 0))

        AnalyticsClient.log(NhAnalyticsAppEvent.IN_APP_UPGRADE, NhAnalyticsEventSection.APP, eventsMap.toMutableMap())
        updateActionEventFired = true
    }

    private fun observeHistoryCount() {
        sessionInfo?.let { sInfo ->
            sessionSPVCount = 0
            countSPVUsecase.execute(sInfo.startTime * 1000)
            Logger.d(LOG_TAG, "Session $sInfo started, start counting spv count, sessionSPVCount: $sessionSPVCount")
        }
    }

    private fun setLiveData(availability: InAppUpdateAvailability) {
        inAppUpdateAvailabilityLD.value = availability
    }
}

enum class UserAction(val actionStr: String) {
    ACTION_UPGRADE(USER_ACTION_TYPE_UPGRADE),
    ACTION_SKIP(USER_ACTION_TYPE_SKIP),
    ACTION_EXIT(USER_ACTION_TYPE_EXIT);
}

