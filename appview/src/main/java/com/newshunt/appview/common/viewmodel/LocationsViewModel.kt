/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.viewmodel


import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.model.usecase.GetAllLocationUsecase
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.LocationEntityLevel
import com.newshunt.dataentity.common.asset.Locations
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.NLResponseWrapper
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.recolocations.MediatorRecommendedLocationsUsecase
import javax.inject.Inject
import javax.inject.Named

/**
 * @author priya.gupta
 */

class LocationsViewModel(val section: String,
                         @Named("toggleFollowMediatorUC")
                         private val toggleFollowMediatorUC: MediatorUsecase<Bundle, Boolean>,
                         @Named("fetchRecommendedLocationsUsecase")
                         private val fetchRecommendedLocationsUsecase: MediatorUsecase<Bundle,
                                 NLResponseWrapper>,
                         @Named("fetchCardListFromUrlUsecase")
                         private val fetchCardListFromUrlUsecase: MediatorUsecase<Bundle,
                                 NLResponseWrapper>) : ViewModel() {

    private val observableRecommendedTopicUsecase = MediatorRecommendedLocationsUsecase()
    private val getAllLocationsUsecase = GetAllLocationUsecase().toMediator2()
    private val getRecommendedLocationsUsecase = fetchRecommendedLocationsUsecase


    val allLocationLiveData: LiveData<Result0<List<Locations>>>
    val allRecomendationData: LiveData<Result0<NLResponseWrapper>>
    val recomendationLiveData: LiveData<Result0<List<Location>>>


    init {
        allLocationLiveData = getAllLocationsUsecase.data()
        allRecomendationData = getRecommendedLocationsUsecase.data()
        recomendationLiveData = observableRecommendedTopicUsecase.data()
    }


    fun getLocationsNested(): LiveData<List<Locations>> {
        return SocialDB.instance().locationsDao().getLocationsNested()
    }

    fun getAllFollowedLocations(): LiveData<List<FollowSyncEntity>> {
        return SocialDB.instance().followEntityDao().getAllFollowedLocations()
    }

    fun getRcommendedLocations(): LiveData<List<Location>> {
        return SocialDB.instance().locationsDao().getLocationsRecommended(LocationEntityLevel
                .RECOMMENDATION.name)
    }

    fun viewStarted() {
        getAllLocationsUsecase.execute(section)
        fetchRecommendedLocationsUsecase.execute(Bundle())

    }

    fun callRecommendationUsecase() {
        fetchRecommendedLocationsUsecase.execute(Bundle())
        observableRecommendedTopicUsecase.execute(section)
    }


    fun onLocationFollowed(location: Location) {
        val entity = location.toActionableEntity()
        toggleFollowMediatorUC.execute(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to entity,
                ToggleFollowUseCase.B_ACTION to FollowActionType.FOLLOW.name))
    }


}


class LocationsViewModelFactory @Inject constructor(@Named("section")
                                                    val section: String,
                                                    @Named("toggleFollowMediatorUC")
                                                    private val toggleFollowMediatorUC: MediatorUsecase<Bundle, Boolean>,
                                                    @Named("fetchRecommendedLocationsUsecase")
                                                    private val fetchRecommendedLocationsUsecase: MediatorUsecase<Bundle,
                                                            NLResponseWrapper>,
                                                    @Named("fetchCardListFromUrlUsecase")
                                                    private val fetchCardListFromUrlUsecase: MediatorUsecase<Bundle,
                                                            NLResponseWrapper>) : ViewModelProvider.Factory {


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LocationsViewModel(section,
                toggleFollowMediatorUC,
                fetchRecommendedLocationsUsecase,
                fetchCardListFromUrlUsecase) as T
    }
}