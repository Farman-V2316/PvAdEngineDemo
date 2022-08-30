package com.newshunt.appview.common.postcreation.model.usecase

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.usecase.BundleUsecase
import io.reactivex.Observable
import java.util.Locale
import javax.inject.Inject


private const val TAG = "PostCurrentPlacesUseCase"

//Get nearby places using Google findCurrentPlace API
class PostCurrentPlacesUseCase @Inject constructor(private val placesClient: PlacesClient) :
    BundleUsecase<List<PostCurrentPlace>> {

    private var placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

    override fun invoke(p1: Bundle): Observable<List<PostCurrentPlace>> {
        val isNearbyPlacesEnable = PreferenceManager.getPreference(
            AppStatePreference.POST_CREATE_LOCATION_NEAR_BY_ENABLE, true
        )
        if (isNearbyPlacesEnable) {
            return Observable.create {
                val request = FindCurrentPlaceRequest.builder(placeFields).build()
                placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
                    Logger.d(TAG, "Place found : ${response.placeLikelihoods} ")
                    it.onNext(transform(response.placeLikelihoods))
                }.addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        Logger.e(
                            TAG,
                            "Place not found: ${exception.statusMessage} ${exception.statusCode}"
                        )
                    }
                    it.onError(exception)
                }
            }
        } else {
            return Observable.just(emptyList())
        }
    }

    private fun transform(placeLikelihoodsList: List<PlaceLikelihood>): List<PostCurrentPlace> {
        val postCurrentPlaceList = mutableListOf<PostCurrentPlace>()
        for (place in placeLikelihoodsList) {
            postCurrentPlaceList.add(
                PostCurrentPlace(
                    id = place.place.id,
                    name = place.place.name, address = place.place.address,
                    latitude = place.place.latLng?.latitude ?: 0.0,
                    longitude = place.place.latLng?.longitude ?: 0.0
                )
            )
        }
        return postCurrentPlaceList
    }
}

//Get current city
class PostCurrentCityUseCase @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient) :
    BundleUsecase<PostCurrentPlace> {

    override fun invoke(p1: Bundle): Observable<PostCurrentPlace> {
        return Observable.create {
            if (ActivityCompat.checkSelfPermission(
                    CommonUtils.getApplication(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val geoCoder = Geocoder(CommonUtils.getApplication(), Locale.getDefault())
                        try {
                            val address : List<Address>? = geoCoder.getFromLocation(
                                location.latitude, location.longitude, 1
                            )
                            if(address.isNullOrEmpty().not()) {
                                val adr: Address = address!![0]
                                val currentPlace = PostCurrentPlace(
                                    name = "${adr.locality} , ${adr.adminArea}",
                                    address = "",
                                    latitude = if (adr.hasLatitude()) adr.latitude else 0.0,
                                    longitude = if (adr.hasLongitude()) adr.longitude else 0.0
                                )
                                it.onNext(currentPlace)
                            } else {
                                it.onError(Throwable("Address not found"))
                            }
                        } catch (e: Exception) {
                            it.onError(e)
                        }
                    } else {
                        it.onError(Throwable("Your location settings is turned off"))
                    }
                }
            } else {
                it.onError(Throwable("You haven't given the permissions"))
            }
        }
    }
}