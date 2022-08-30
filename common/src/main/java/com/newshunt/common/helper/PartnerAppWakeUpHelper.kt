/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper

import android.content.Context
import android.content.Intent
import android.os.Build
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.dhutil.model.entity.WakeUpPartnerAppInformationConfig
import com.newshunt.dataentity.dhutil.model.entity.PartnerPackagesInformation
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.preference.AppStatePreference
import java.util.*

/**
 * @author atul.anand
 */

object PartnerAppWakeUpHelper {

    const val TAG = "PartnerAppWakeUpHelper"

    /**
     * called to initiate wake-up of all the partner apps, a list of which was obtained during handshake in wakeUpPartnerInformation
     */
    @JvmStatic
    fun checkAndLaunchPartnerAppWakeUpServices(context: Context, appSection: String) {
        Logger.d(TAG, "check and launch partner app service called")
        try{
            CommonUtils.runInBackGroundSerially(getWakeUpRunnable(appSection))
        }catch(ex: Exception){
            Logger.caughtException(ex)
        }

    }

    /**
     * checks if manufacturer is blacklisted, the list of blackListed manufacturer is obtained during handshake in wakeUpPartnerInformation
     */
    private fun isBlackListedManufacturer(blockedManufacturers: List<String>?): Boolean {
        if (blockedManufacturers == null) {
            return false
        }
        return blockedManufacturers.contains(DeviceInfoHelper.getDeviceInfo().manufacturer.toLowerCase())

    }

    /**
     * checks if the partner-app dnd hours are not going on currently, the dnd start and end time is inside each partnerPackages of wakeUpPartnerInformation
     */
    private fun outsideDNDHours(dndStartTime: Long, dndEndTime: Long): Boolean {

        if (dndStartTime == dndEndTime) {
            return true
        }

        val calendar = Calendar.getInstance()
        resetCalendar(calendar)

        val dndStartDate : Date
        val dndEndDate : Date

        if (dndStartTime > dndEndTime) {
            calendar.set(Calendar.SECOND, dndStartTime.toInt())
            dndStartDate = calendar.time
            resetCalendar(calendar)
            val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
            calendar.set(Calendar.DAY_OF_YEAR, currentDay + 1)
            calendar.set(Calendar.SECOND, dndEndTime.toInt())
            dndEndDate = calendar.time
        } else {
            calendar.set(Calendar.SECOND, dndStartTime.toInt())
            dndStartDate = calendar.time
            resetCalendar(calendar)
            calendar.set(Calendar.SECOND, dndEndTime.toInt())
            dndEndDate = calendar.time
        }

        val currentDate = Date()
        if (CommonUtils.isJobInDND(dndStartDate, dndEndDate, currentDate)) {
            return false
        }

        return true
    }

    private fun resetCalendar(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }

    /**
     * checks if the device charging constraints mentioned in wakeUpPartnerInformation are met
     */
    private fun isDeviceChargeSufficient(minChargeRequired: Int?): Boolean{
        if(minChargeRequired == null){
            return true
        }else if(CommonUtils.getBatteryPercent() > minChargeRequired){
            return true
        }

        return false
    }

    /**
     * checks if the section from which wake up call was initiated eligible for waking up
     */
    private fun isAppSectionCalledFromEligible(wakeUpSections: List<String>?, appSection: String): Boolean{
        if(wakeUpSections != null && wakeUpSections.size > 0 && !wakeUpSections.contains(appSection)){
            return false
        }else{
            return true
        }
    }

    /**
     *  launches partner app services
     */
    private fun performWakeUps(action: String?, packageName: String?, isForegroundSupported: Boolean?, section : String){
        if(!CommonUtils.isEmpty(action) && !CommonUtils.isEmpty(packageName)){
            val intent = Intent(action)
            intent.`package` = packageName
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            intent.putExtra(Constants.SOURCE, Constants.DAILYHUNT_PACKAGE)
            //Adding this redundant extra, since PV expects this extra
            intent.putExtra(Constants.SOURCE_APP, Constants.DAILYHUNT_PACKAGE)
            intent.putExtra(Constants.SECTION, section)

            try {
                if (isForegroundSupported != null && isForegroundSupported) {
                    CommonUtils.getApplication().startForegroundService(intent)
                } else {
                    CommonUtils.getApplication().startService(intent)
                }
            } catch (ex: Exception) {
                Logger.e(TAG, "Exception starting partner", ex)
            }
        }
    }

    /**
     * returns runnable to be posted on handler thread
     */
    private fun getWakeUpRunnable(appSection: String): Runnable{
        Logger.d(TAG, "getting runnable")
        val wakeUpRunnable = object: Runnable{
            val section = appSection
            override fun run() {
                try{
                    Logger.d(TAG, "Running runnable now")
                    val partnerAppWakeUpInformation = JsonUtils.fromJson(PreferenceManager.getPreference(AppStatePreference.PARTNER_APP_WAKE_UP_INFORMATION, ""), WakeUpPartnerAppInformationConfig::class.java)
                    if(partnerAppWakeUpInformation != null){
                        if(partnerAppWakeUpInformation.partnerPackages != null && isDeviceChargeSufficient(partnerAppWakeUpInformation.minimumBatteryRequired)){
                            for(partnerPackage in ((partnerAppWakeUpInformation.partnerPackages) as List<PartnerPackagesInformation>)){
                                if(partnerPackage.lastWakeUpTime == 0L || ((partnerPackage.lastWakeUpTime + partnerPackage.wakeDelay) <= System.currentTimeMillis())){
                                    if(outsideDNDHours(partnerPackage.dndStartTime, partnerPackage.dndEndTime) && !isBlackListedManufacturer(partnerPackage.disabledManufacturer) && isAppSectionCalledFromEligible(partnerPackage.wakeupsections, section)){
                                        performWakeUps(partnerPackage.action, partnerPackage.packageName, partnerPackage.foregroundServiceSupport, section)
                                        partnerPackage.lastWakeUpTime = System.currentTimeMillis()
                                    } else{
                                        Logger.d(TAG, "Conditions for wake up not met either the manufacturer is blacklisted or dndHours are going on")
                                    }
                                }else{
                                    Logger.d(TAG, "Minimum wake delay not reached yet")
                                }
                            }
                            PreferenceManager.savePreference(AppStatePreference.PARTNER_APP_WAKE_UP_INFORMATION, JsonUtils.toJson(partnerAppWakeUpInformation))
                        }
                        else{
                            Logger.d(TAG, "could not wake up partner package information is ${partnerAppWakeUpInformation.partnerPackages} and string stored is ${PreferenceManager.getPreference(AppStatePreference.PARTNER_APP_WAKE_UP_INFORMATION, "")}")
                        }
                    }else{
                        Logger.d(TAG, "partner information is null")
                    }
                }catch(ex: Exception){
                    Logger.caughtException(ex)
                }
            }
        }

        return wakeUpRunnable
    }


}