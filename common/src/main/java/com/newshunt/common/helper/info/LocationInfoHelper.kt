/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.info

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.PasswordEncryption
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.status.LocationInfo
import com.newshunt.dhutil.helper.preference.AppStatePreference

/**
 * Provides location service for the caller.
 * Fetches current location only if 'x' time has passed since last fetch to save power.
 *
 *  @author raunak.yadav
 */
object LocationInfoHelper {
    private const val TAG = "LocationInfoHelper"

    private var refreshInterval = -1L
    private var lastLocation: Location? = null
    private var lastFetchedTime: Long = PreferenceManager.getLong(Constants.USER_LOCATION_FETCH_TIME)
    private val locationFetcher by lazy {
        LocationFetcher(CommonUtils.getApplication())
    }

    fun initConfig() {
        refreshInterval = PreferenceManager.getPreference(AppStatePreference.LOCATION_FETCH_INTERVAL, -1L)
    }

    @JvmStatic
    fun getLocationInfo(isAds: Boolean = false): LocationInfo {
        try {
            if (ContextCompat.checkSelfPermission(CommonUtils.getApplication(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return LocationInfo()
            }
            val context: Context = CommonUtils.getApplication()
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!isLocationEnabled(locationManager)) {
                Logger.v(TAG, "User location setting is disabled")
                lastLocation = null
                return LocationInfo()
            }
            locationFetcher.getLastLocation {
                lastLocation = it
            }
            refreshLocationIfStale()
            lastLocation?.let { return createLocationInfo(it, isAds) }
        } catch (e: Exception) {
            // Do nothing.
        }
        return LocationInfo()
    }

    private fun createLocationInfo(location: Location, isAds: Boolean): LocationInfo {
        val locationInfo = LocationInfo()
        try {
            if (isAds) {
                locationInfo.lat = PasswordEncryption.encryptForAds(location.latitude.toString())
                locationInfo.lon = PasswordEncryption.encryptForAds(location.longitude.toString())
            } else {
                locationInfo.lat = PasswordEncryption.encrypt(location.latitude.toString())
                locationInfo.lon = PasswordEncryption.encrypt(location.longitude.toString())
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return locationInfo
    }

    private fun refreshLocationIfStale() {
        if (refreshInterval <= 0) {
            // Only positive values should trigger location query.
            return
        }
        if (System.currentTimeMillis() > (lastFetchedTime + refreshInterval)) {
            Logger.v(TAG, "Try querying current location")
            locationFetcher.refreshLocation {
                lastLocation = it
                lastFetchedTime = System.currentTimeMillis()
            }
        }
    }

    private fun isLocationEnabled(locationManager: LocationManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        }
    }

    /**
     * Stop listening to location updates (if any) in cases of App removed from foreground.
     */
    fun stopLocationUpdates() {
        Logger.d(TAG, "Stop location updates if in progress")
        locationFetcher.reset()
    }
}