/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.util

import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.SplashAdMeta
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.util.AdsUtil.Companion.getAdBaseDirectory
import com.newshunt.adengine.util.AdsUtil.Companion.getIntValue
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.ads.AdFCType
import com.newshunt.dhutil.helper.preference.AdsPreference
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.TimeUnit

/**
 * Helper to read/persist the BaseDisplayAdEntity Object for Splash.
 *
 * @author raunak.yadav
 */
object SplashAdPersistenceHelper {
    private const val TAG = "SplashAdPersistenceHelper"
    private const val SPLASH_AD_ZIP_FOLDER_NAME = "/.DailyHunt/SPLASHAD/"
    private const val SPLASH_AD_PERSISTENCE_FILE_NAME = "splashadpersistencefile"
    private const val SPLASH_SCREEN_WAIT_TIME = "splashScreenWaitTime"

    private var adMeta: SplashAdMeta?

    init {
        adMeta = PreferenceManager.getPreference(AdsPreference.AD_REGULAR_SPLASH_CONFIG,
            Constants.EMPTY_STRING)?.let {
            if (it.isBlank()) {
                null
            } else {
                JsonUtils.fromJson(it, SplashAdMeta::class.java)
            }
        }
    }

    fun writeObjectToPersistentStore(baseDisplayAdEntity: BaseDisplayAdEntity?) {
        baseDisplayAdEntity ?: return

        val filePath = getAdBaseDirectory(SPLASH_AD_ZIP_FOLDER_NAME)
        val file = File("$filePath/$SPLASH_AD_PERSISTENCE_FILE_NAME")

        try {
            if (file.exists()) {
                file.delete()
                file.createNewFile()
            }
            val fos = FileOutputStream(file)
            val os = ObjectOutputStream(fos)
            os.writeObject(baseDisplayAdEntity)
            os.close()
            fos.close()
            AdLogger.d(TAG, "data serialized at :$filePath")
            updateConfigFromAd(baseDisplayAdEntity)
        } catch (e: IOException) {
            Logger.caughtException(e)
        }
    }

    fun readObjectFromPersistentStore(): BaseDisplayAdEntity? {
        var baseDisplayAdEntity: BaseDisplayAdEntity? = null
        val filePath = getAdBaseDirectory(SPLASH_AD_ZIP_FOLDER_NAME)
        val file = File("$filePath/$SPLASH_AD_PERSISTENCE_FILE_NAME")
        if (!file.exists()) {
            return null
        }
        try {
            val fis = FileInputStream(file)
            val `is` = ObjectInputStream(fis)
            baseDisplayAdEntity = `is`.readObject() as BaseDisplayAdEntity
            `is`.close()
            fis.close()
        } catch (e: Exception) {
            Logger.caughtException(e)
            deleteFileFromPersistentStore()
        }
        return baseDisplayAdEntity
    }

    /**
     * delete file from persistent storage and update adMeta and SplashWaitTimeOut
     * values to 0.
     */
    private fun deleteFileFromPersistentStore() {
        val filePath = getAdBaseDirectory(SPLASH_AD_ZIP_FOLDER_NAME)
        val file = File("$filePath/$SPLASH_AD_PERSISTENCE_FILE_NAME")
        if (file.exists()) {
            file.delete()
        }
        adMeta = null
        PreferenceManager.remove(AdsPreference.AD_REGULAR_SPLASH_CONFIG)
        updateSplashWaitTimeOut(0)
    }

    /**
     * This method checks if ad is valid to show based on expiry time and showCount, if not valid
     * delete the file from local storage and returns false else returns true.
     *3
     * @param baseDisplayAdEntity
     * @return
     */
    fun isValidSplashAdToServe(baseDisplayAdEntity: BaseDisplayAdEntity?): Boolean {
        if (baseDisplayAdEntity == null) {
            deleteFileFromPersistentStore()
            return false
        }
        if (AdsUtil.isFCLimitReachedForAd(baseDisplayAdEntity)) {
            AdLogger.e(TAG, "FC exhausted for Splash ad. Drop")
            return false
        }

        if (getShowCount() <= 0) {
            deleteFileFromPersistentStore()
            return false
        }
        val startEpoch = baseDisplayAdEntity.startepoch
        val endEpoch = baseDisplayAdEntity.endepoch
        val currentEpoch = System.currentTimeMillis() / 1000
        if ((startEpoch == null || startEpoch <= currentEpoch)
            && (endEpoch == null || endEpoch >= currentEpoch)) {
            return true
        }
        deleteFileFromPersistentStore()
        return false
    }

    /**
     * Check whether splash meta is valid for rendering.
     */
    fun isSplashMetaValidForRendering(meta: SplashAdMeta?): Boolean {
        meta ?: return false

        if (meta.isEmpty) return false

        //Check if ad's time window is valid.
        if (!isAdTimeValid(meta.startEpoch, meta.endEpoch)) return false

        //Check if FC is valid.
        if (AdsUtil.isFCLimitReached(AdFCType.CAMPAIGN, meta.campaignId)) {
            AdLogger.v(TAG, "Splash meta invalid for FC campaign : ${meta.adId}")
            return false
        }
        if (AdsUtil.isFCLimitReached(AdFCType.BANNER, meta.bannerId)) {
            AdLogger.v(TAG, "Splash meta invalid for FC banner : ${meta.adId}")
            return false
        }
        if (meta.showCount <= 0) {
            AdLogger.v(TAG, "Splash meta invalid for showcount : ${meta.adId}")
            return false
        }
        //All checks passed, we have a valid splash ad
        AdLogger.d(TAG, "Splash meta valid for : ${meta.adId}")
        return true
    }

    // Does current time lies in ad's time window.
    private fun isAdTimeValid(startEpoch: Long?, endEpoch: Long?): Boolean {
        val current = System.currentTimeMillis() / 1000
        return (startEpoch == null || startEpoch <= current) &&
                (endEpoch == null || endEpoch >= current)
    }

    private fun updateConfigFromAd(adEntity: BaseDisplayAdEntity?) {
        adEntity ?: return

        //Save splash config in pref to check at next launch.
        adMeta = SplashAdMeta(adId = adEntity.uniqueAdIdentifier,
            campaignId = adEntity.campaignId ?: Constants.EMPTY_STRING,
            bannerId = adEntity.bannerId ?: Constants.EMPTY_STRING,
            showCount = adEntity.showCount ?: AdConstants.SPLASH_DEFAULT_SHOW_COUNT,
            startEpoch = adEntity.startepoch,
            endEpoch = adEntity.endepoch,
            span = adEntity.span ?: AdConstants.SPLASH_DEFAULT_SPAN,
            isEmpty = adEntity.type == AdContentType.EMPTY_AD)
        PreferenceManager.savePreference(AdsPreference.AD_REGULAR_SPLASH_CONFIG, JsonUtils.toJson(adMeta))
        AdLogger.d(TAG, "Saved Splash ad meta $adMeta")

        var splashAdWaitTimeOut = getIntValue(adEntity.span, AdConstants.SPLASH_DEFAULT_SPAN)
        //We don't want to wait more than 5 seconds on splash screen, So if the value is coming
        // more than 5 seconds we'll stick to 2 seconds
        if (splashAdWaitTimeOut > 5) {
            splashAdWaitTimeOut = AdConstants.SPLASH_DEFAULT_SPAN
        }

        // convert splashAdWaitTimeOut from second to millisecond.
        updateSplashWaitTimeOut(splashAdWaitTimeOut * 1000)
    }

    @JvmStatic
    fun getShowCount(): Int {
        return adMeta?.showCount ?: 0
    }

    @JvmStatic
    fun updateSplashAdShowCount(baseAdEntity: BaseAdEntity, showCount: Int) {
        adMeta?.let {
            if (it.adId == baseAdEntity.i_adId()) {
                it.showCount = showCount
                PreferenceManager.savePreference(AdsPreference.AD_REGULAR_SPLASH_CONFIG, JsonUtils.toJson(it))
            }
        }
    }

    @JvmStatic
    fun getRegularSplashWaitTimeOut(): Int {
        return PreferenceManager.getInt(SPLASH_SCREEN_WAIT_TIME, 0)
    }

    /**
     * Get estimated splash timeout (max of all available timeouts)
     * (Since splash ad is not yet chosen)
     */
    @JvmStatic
    fun getSplashWaitTimeOut(): Int {
        return maxOf(getRegularSplashWaitTimeOut(), EvergreenSplashUtil.getEvergreenSpan())
    }

    @JvmStatic
    fun getDefaultSplashTimeout(): Long {
        return PreferenceManager.getPreference(GenericAppStatePreference.DEFAULT_SPLASH_TIMEOUT,
            Constants.SPLASH_MIN_WAIT_TIME)
    }

    @JvmStatic
    fun updateDefaultSplashTimeOut(timeInSecs: Int?) {
        val timeSecs = timeInSecs ?: AdConstants.AD_NEGATIVE_DEFAULT
        val timeout =
            if (timeSecs > 0) TimeUnit.SECONDS.toMillis(timeSecs.toLong()) else Constants.SPLASH_MIN_WAIT_TIME
        PreferenceManager.savePreference(GenericAppStatePreference.DEFAULT_SPLASH_TIMEOUT, timeout)
    }

    private fun updateSplashWaitTimeOut(splashWaitTimeOut: Int) {
        PreferenceManager.saveInt(SPLASH_SCREEN_WAIT_TIME, splashWaitTimeOut)
    }

    /**
     * Helper function to quickly check if a splash ad is available. Does not mean, it will be rendered.
     * This is just an indication that the Splash ad might show.
     * @return true if a splash ad is cached, false otherwise.
     */
    @JvmStatic
    fun isCachedSplashAdAvailable(): Boolean {
        val filePath = getAdBaseDirectory(SPLASH_AD_ZIP_FOLDER_NAME)
        val regularSplash = File(filePath + File.separator + SPLASH_AD_PERSISTENCE_FILE_NAME)
        return (isSplashMetaValidForRendering(adMeta) && regularSplash.exists())
                || EvergreenSplashUtil.isSplashAdAvailable().also {
            if (it) {
                updateSplashWaitTimeOut(EvergreenSplashUtil.getEvergreenSpan())
            }
        }
    }
}