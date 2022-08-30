/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.eterno

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.ApplicationStatus
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.BUNDLE_CS_FULL_NEEDED
import com.newshunt.dataentity.model.entity.ContactSyncResetException
import com.newshunt.dataentity.model.entity.ContactsSyncPayload
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.repo.ContactsRepository
import com.newshunt.news.model.usecase.BuildCSFullPayloadUsecase
import com.newshunt.news.model.usecase.SyncContactUsecase
import com.newshunt.news.model.usecase.UpdateContactsDBUsecase
import com.newshunt.permissionhelper.utilities.PermissionUtils
import com.newshunt.sso.SSO
import io.reactivex.Observable
import javax.inject.Inject

private const val LOG_TAG = "BGSyncService"
private const val ID_BG_SYNC = 1245321

/**
 * A common Job intent service which can be used to sync stuff from different B.E services
 * <p>
 * Created by srikanth.ramaswamy on 10/04/2019.
 */
class BGSyncService : JobIntentService() {
    @Inject lateinit var buildContactSyncPayloadUsecase: BuildCSFullPayloadUsecase
    @Inject lateinit var updateContactsDBUsecase: UpdateContactsDBUsecase
    @Inject lateinit var syncContactUsecase: SyncContactUsecase

    override fun onCreate() {
        super.onCreate()
        val bucketSize = PreferenceManager.getPreference(AppStatePreference.CONTACT_SYNC_BUCKET_SIZE, Constants.CONTACT_SYNC_PAYLOAD_BUCKET_SIZE_DEFAULT)
        DaggerBGSyncServiceComponent
                .builder()
                .contactSyncModule(ContactSyncModule(bucketSize))
                .build()
                .inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        syncContacts(intent)
    }

    override fun onDestroy() {
        Logger.d(LOG_TAG, "onDestroy..")
        super.onDestroy()
    }

    /**
     * Sync contact diff with B.E
     */
    private fun syncContacts(intent: Intent) {
        //While a sync in progress, another sync request would queue up and try to execute again. Check that
        if (!isContactSyncEnabled()) {
            Logger.e(LOG_TAG, "A sync might have happened recently, not doing again!")
            return
        }

        Logger.d(LOG_TAG, "Starting contact sync")

        if (intent.getBooleanExtra(BUNDLE_CS_FULL_NEEDED, false)) {
            ContactsRepository.clearContacts()
            Logger.d(LOG_TAG, "Triggering a full sync")
        }

        /**
         * Sync logic is as below:
         * 1. Build the payload depicting # of adds, # of modifications and # of deletions
         * 2. Hit the API if there is any change in the contacts [multiple times, in buckets of
         * size controlled by B.E]
         * 3. On success of the API, update the DB for all contacts syncd with server [Multiple
         * times, after each part success]
         * 4. On API failure, do not update the DB so that next sync will retry the same payload + new delta
         *
         * This entire chain uses blockingSubscribe. If not, the intent service finishes before
         * the chain can complete on other threads. blockingSubscribe ensures the intent service
         * lives through the sync operation.
         */
        buildContactSyncPayloadUsecase.invoke(Unit)
                .flatMap { payload ->
                    //Emit each item in the list as payload: input to API
                    Observable.fromIterable(payload)
                }.concatMap { partialPayload ->
                    //Make the API call for this payload
                    syncContactUsecase.invoke(partialPayload)
                }.map {
                    //Update the partial success to DB
                    updateLocalDB(it)
                }
                .blockingSubscribe({
                    Logger.d(LOG_TAG, "Contact sync partial complete: OnNext")
                }, {
                    //An API/DB write failure will break the chain. New delta needs to be calculated next time
                    Logger.e(LOG_TAG, "Failure syncing contact: ${it.message}")
                    if(it is ContactSyncResetException) {
                        //Clearing the local DB ensures next sync will be a full sync
                        ContactsRepository.clearContacts()
                    }
                    Logger.caughtException(it)
                }, {
                    //All done, contact sync success!!
                    PreferenceManager.savePreference(AppStatePreference.CONTACT_SYNC_LATEST_TIMESTAMP, System.currentTimeMillis())
                    PreferenceManager.savePreference(AppStatePreference.NEED_CS_FULL_SYNC, false)
                    Logger.d(LOG_TAG, "Contact sync is OnComplete")
                })
    }

    /**
     * Update the contact payload to the ROOM table
     */
    private fun updateLocalDB(syncPayload: ContactsSyncPayload?) {
        syncPayload?.let { payload ->
            if (!syncPayload.isEmpty()) {
                updateContactsDBUsecase.invoke(payload)
                        .subscribe({
                            Logger.d(LOG_TAG, "Updated the local DB")
                        },
                        {
                            Logger.caughtException(it)
                        })
            } else {
                Logger.d(LOG_TAG, "payload is empty, nothing to update in DB")
            }
        }
    }

    companion object {
        /**
         * Helper method to start the BGSyncService
         */
        @JvmStatic
        @JvmOverloads
        fun startBGSyncService(context: Context = CommonUtils.getApplication()) {
            when {
                ApplicationStatus.getVisibleActiviesCount() > 0 -> {
                    //After upgrade, make sure to done one sync ASAP. reset the last timestamp
                    val performFullSync = PreferenceManager.getPreference(AppStatePreference.NEED_CS_FULL_SYNC, true)
                    if (performFullSync) {
                        PreferenceManager.savePreference(AppStatePreference.CONTACT_SYNC_LATEST_TIMESTAMP, 0L)
                    }
                    if (isContactSyncEnabled()) {
                        //Start the service only when app is in foreground
                        Intent().apply {
                            putExtra(BUNDLE_CS_FULL_NEEDED, performFullSync)
                            setClass(context, BGSyncService::class.java)
                            setPackage(AppConfig.getInstance().packageName)
                            enqueueWork(context, BGSyncService::class.java, ID_BG_SYNC, this)
                        }
                    }
                }
                else -> {
                    Logger.e(LOG_TAG, "Can not start the service while in background")
                }
            }
        }

        private fun isContactSyncEnabled(): Boolean {
            if (!SSO.getInstance().isLoggedIn(false)) {
                Logger.e(LOG_TAG, "Guest user, can not sync contacts")
                return false
            }
            if (!PermissionUtils.hasPermission(CommonUtils.getApplication(), Manifest.permission.READ_CONTACTS)) {
                Logger.e(LOG_TAG, "No contacts permission, can not sync contacts")
                return false
            }
            if (!PreferenceManager.getPreference(AppStatePreference.CONTACT_SYNC_ENABLED, true)) {
                Logger.e(LOG_TAG, "Contact sync disabled from handshake")
                return false
            }
            val lastSyncTime = PreferenceManager.getPreference(AppStatePreference.CONTACT_SYNC_LATEST_TIMESTAMP, 0L)
            val syncFreqMs = PreferenceManager.getPreference(AppStatePreference.CONTACT_SYNC_FREQUENCY_MS, Constants.CONTACT_SYNC_FREQ_DEFAULT)
            if (!CommonUtils.isTimeExpired(lastSyncTime, syncFreqMs)) {
                Logger.e(LOG_TAG, "sync frequency: $syncFreqMs, last sync time: $lastSyncTime, current time: ${System.currentTimeMillis()} disabled!")
                return false
            }
            return true
        }
    }
}