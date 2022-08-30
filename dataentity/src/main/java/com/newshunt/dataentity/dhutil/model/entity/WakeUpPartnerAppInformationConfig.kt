/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity

/**
 * POJO to hold partner app to be woken up and related constraints information
 *
 * @author atul.anand
 */

data class WakeUpPartnerAppInformationConfig(val minimumBatteryRequired: Int?, val partnerPackages: List<PartnerPackagesInformation>?) {

}

data class PartnerPackagesInformation(val packageName: String, val action: String, val foregroundServiceSupport: Boolean, val wakeupsections: List<String>?, val dndStartTime: Long, val dndEndTime: Long, val wakeDelay: Long = 86400000, val disabledManufacturer: List<String>?, var lastWakeUpTime: Long = 0){

}