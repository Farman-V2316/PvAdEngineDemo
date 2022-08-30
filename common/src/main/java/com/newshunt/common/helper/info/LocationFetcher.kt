/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.common.helper.info

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.newshunt.common.helper.common.ApplicationStatus
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Fetches location (lastKnown and current)
 *
 * @author raunak.yadav
 */
class LocationFetcher(val context: Context) {

    private var inProgress = false
    private var callbacks = ArrayList<LocationCallback>()

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * Get last known location from the fused provider.
     */
    fun getLastLocation(locationUpdated: (Location?) -> Unit) {
        if (ContextCompat.checkSelfPermission(CommonUtils.getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                locationUpdated(it.result)
            }
        }
    }

    /**
     * - Register to listen to current location.
     * - Do not register frequently and unregister once location is received or after 5 secs,
     * whichever is earlier
     * - Using PRIORITY_HIGH_ACCURACY, else it still gives last location unless some other app has
     * queried with high accuracy.
     */
    fun refreshLocation(locationFetched: (Location) -> Unit) {
        if (ContextCompat.checkSelfPermission(CommonUtils.getApplication(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (inProgress || ApplicationStatus.getVisibleActiviesCount() <= 0) {
            return
        }
        inProgress = true

        val locRequest = LocationRequest.create()
        locRequest.interval = LOCATION_WAIT_TIME_MAX
        locRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                var bestLocation: Location? = null
                locationResult?.locations?.forEach { location ->
                    if (location.accuracy < bestLocation?.accuracy ?: Float.MAX_VALUE) {
                        bestLocation = location
                    }
                    Logger.d(TAG, "CURRENT Location - $location, time - ${location.time}")
                }
                bestLocation?.let {
                    PreferenceManager.saveLong(Constants.USER_LOCATION_FETCH_TIME, System.currentTimeMillis())
                    locationFetched(it)
                    reset()
                }
            }
        }

        callbacks.add(locationCallback)
        Logger.d(TAG, "Registered for location update")
        fusedLocationClient.requestLocationUpdates(locRequest, locationCallback, Looper.getMainLooper())

        HANDLER.postDelayed({ reset() }, LOCATION_WAIT_TIME_MAX)
    }

    fun reset() {
        Logger.d(TAG, "Unregister from location updates. Callbacks : ${callbacks.size}")
        callbacks.forEach {
            try {
                fusedLocationClient.removeLocationUpdates(it)
            } catch (ex: Exception) {
                Logger.e(TAG, "Failed to unregister location updates. Error: ${ex.message}")
            }
        }
        callbacks.clear()
        inProgress = false
    }

    companion object {
        private const val LOCATION_WAIT_TIME_MAX = 5 * 1000L
        private const val TAG = "LocationFetcher"
        private val HANDLER = Handler(Looper.getMainLooper())
    }
}