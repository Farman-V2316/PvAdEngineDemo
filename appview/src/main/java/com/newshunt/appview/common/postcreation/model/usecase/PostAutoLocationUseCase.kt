package com.newshunt.appview.common.postcreation.model.usecase

import android.os.Bundle
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.POST_QUERY
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.news.model.usecase.BundleUsecase
import io.reactivex.Observable
import javax.inject.Inject


private const val TAG = "PostAutoLocationUseCase"

class PostAutoLocationUseCase @Inject constructor(private val placesClient: PlacesClient) :
    BundleUsecase<List<PostCurrentPlace>> {

    override fun invoke(p1: Bundle): Observable<List<PostCurrentPlace>> {
        val query = p1.getString(POST_QUERY)
        return Observable.create {
            val requestBuilder: FindAutocompletePredictionsRequest.Builder =
                FindAutocompletePredictionsRequest.builder()
                    .setCountry("in")
            val request = requestBuilder.setQuery(query).build()

            placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                Logger.d(TAG, "Found auto complete locations")
                it.onNext(
                    response.autocompletePredictions.map { location ->
                        PostCurrentPlace(
                            id = location.placeId,
                            name = location.getFullText(null).toString(),
                            address = "",
                            latitude = 0.0,
                            longitude = 0.0,
                            isAutoLocation = true
                        )
                    })
            }.addOnFailureListener { exception ->
                Logger.e(TAG, "Exception auto complete result: ")
                if (exception is ApiException) {
                    Logger.e(TAG, "Place not found: ${exception.message} ${exception.statusCode}")
                }
                it.onError(exception)
            }
        }
    }

}