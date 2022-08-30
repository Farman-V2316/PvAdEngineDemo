/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.news.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.*
import javax.inject.Inject
/**
 * @author aman.roy
 * ViewModel for follow and block suggestion for implicit and explicit signals.
 */
class FollowUpdateViewModel(context:Application,
                            val followBlockUpdateUsecase: MediatorUsecase<SourceFollowBlockEntity?,Boolean>,
                            val implicitFollowUsecase:MediatorUsecase<Bundle,SourceFollowBlockEntity?>,
                            val implicitBlockUsecase:MediatorUsecase<Bundle,SourceFollowBlockEntity?>,
                            val explicitFollowBlockTriggerUsecase:MediatorUsecase<Bundle,ExplicitWrapperObject?>,
                            val getFollowBlockUpdateUsecase:MediatorUsecase<String,SourceFollowBlockEntity?>,
                            val coldSignalUseCase:MediatorUsecase<Bundle,Boolean>,
                            val MinCardPositionUseCase:MediatorUsecase<String,Int?>,
                            val updateFollowBlockImplictDialogCountUsecase: MediatorUsecase<Bundle,Unit>,
                            val cardPositionUseCase: MediatorUsecase<String,Int?>,
                            val bottomBarDurationUseCase: MediatorUsecase<String,Int?>): AndroidViewModel(context) {

    val implicitFollowLiveData: LiveData<SourceFollowBlockEntity> = Transformations.map(implicitFollowUsecase.data()){ result ->
        if (result.isSuccess) {
            result.getOrNull()
        } else {
            null
        }
    }

    val implicitBlockLiveData: LiveData<SourceFollowBlockEntity> = Transformations.map(implicitBlockUsecase.data()){ result ->
        if (result.isSuccess) {
            result.getOrNull()
        } else {
            null
        }
    }
    val explicitFollowBlockLiveData: LiveData<ExplicitWrapperObject> = Transformations.map(explicitFollowBlockTriggerUsecase.data()){ result ->
        if (result.isSuccess) {
            result.getOrNull()
        } else {
            null
        }
    }

    val coldSignalLiveData:LiveData<Boolean> =  Transformations.map(coldSignalUseCase.data()) { result ->
        if (result.isSuccess) {
            result.getOrNull()
        } else {
            false
        }
    }
    val followBlockLiveData:LiveData<SourceFollowBlockEntity?>  =  SocialDB.instance().followBlockRecoDao().getSourceFollowBlockEntityDescLiveData()


    fun updateFollowBlockEntity(entity: SourceFollowBlockEntity?) {
        followBlockUpdateUsecase.execute(entity)
    }

    fun triggerImplicitFollowUsecase(sourceFollowBlockEntity: SourceFollowBlockEntity?) {
        sourceFollowBlockEntity?.let {
            implicitFollowUsecase.execute(
                bundleOf(ImplicitFollowTriggerUsecase.FOLLOW_ITEM to it))
        }
    }

    fun triggerImplicitBlockUsecase(sourceFollowBlockEntity: SourceFollowBlockEntity?) {
        sourceFollowBlockEntity?.let {
            implicitBlockUsecase.execute(
                bundleOf(ImplicitBlockTriggerUsecase.BLOCK_ITEM to it))
        }
    }

    fun triggerColdStartSignal(sourceLang: String) {

        coldSignalUseCase.execute(
            bundleOf(ColdSignalUseCase.APP_LANG to sourceLang)
        )

    }

    fun incrementFollowBlockImplicitDialogCountUsecase(bundle:Bundle) {
        updateFollowBlockImplictDialogCountUsecase.execute(bundle)
    }

    var  getMinCardPostionLiveData: LiveData<Int> = Transformations.map(MinCardPositionUseCase.data()) { result ->
        if (result.isSuccess) {
            result.getOrNull()
        } else {
            null
        }
    }

    var  getCardPostionLiveData: LiveData<Int> = Transformations.map(cardPositionUseCase.data()) { result ->
        if (result.isSuccess) {
            result.getOrNull()
        } else {
            null
        }
    }

    var  getBottomDurationLiveData: LiveData<Int> = Transformations.map(bottomBarDurationUseCase.data()) { result ->
        if (result.isSuccess) {
            result.getOrNull()
        } else {
            null
        }
    }

    fun triggerMinCardPosition() {
        MinCardPositionUseCase.execute(UserPreferenceUtil.getUserPrimaryLanguage() ?: Constants.DEFAULT_LANGUAGE)
    }

    fun triggerCardPosition() {
        cardPositionUseCase.execute(UserPreferenceUtil.getUserPrimaryLanguage() ?: Constants.DEFAULT_LANGUAGE)
    }

    fun triggerBottomBarDuration() {
        bottomBarDurationUseCase.execute(UserPreferenceUtil.getUserPrimaryLanguage() ?: Constants.DEFAULT_LANGUAGE)
    }


    fun triggerExplicitBlockFollowSignal(sourceId:String?,sourceLang:String,commonAsset: CommonAsset?) {

        explicitFollowBlockTriggerUsecase.execute(
            bundleOf(ExplicitFollowBlockTriggerUsecase.ITEM_ID to sourceId,
                ExplicitFollowBlockTriggerUsecase.LANG to sourceLang,
                ExplicitFollowBlockTriggerUsecase.ITEM to commonAsset))


    }

    override fun onCleared() {
        super.onCleared()
        followBlockUpdateUsecase.dispose()
        implicitBlockUsecase.dispose()
        implicitFollowUsecase.dispose()
        explicitFollowBlockTriggerUsecase.dispose()
        getFollowBlockUpdateUsecase.dispose()
        coldSignalUseCase.dispose()
    }


    class Factory @Inject constructor(private val app: Application,
                                      private val followBlockUpdateUsecase: FollowBlockUpdateUsecase,
                                      private val implicitFollowTriggerUsecase: ImplicitFollowTriggerUsecase,
                                      private val implicitBlockTriggerUsecase: ImplicitBlockTriggerUsecase,
                                      private val explicitFollowTriggerUsecase: ExplicitFollowBlockTriggerUsecase,
                                      private val getFollowBlockUpdateUsecase: GetFollowBlockUpdateUsecase,
                                      private val coldSignalUseCase: ColdSignalUseCase,
                                      private val minCardPositionUseCase: MinCardPositionUseCase,
                                      private val updateFollowBlockImplictDialogCountUsecase:UpdateFollowBlockImplictDialogCountUsecase,
                                      private val cardPositionUseCase:CardPositionUseCase,
                                      private val bottomBarDurationUseCase:BottomBarDurationUseCase) : ViewModelProvider.AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FollowUpdateViewModel(app,
                followBlockUpdateUsecase.toMediator2(),
                implicitFollowTriggerUsecase.toMediator2(),
                implicitBlockTriggerUsecase.toMediator2(),
                explicitFollowTriggerUsecase.toMediator2(),
                getFollowBlockUpdateUsecase.toMediator2(),
                coldSignalUseCase.toMediator2(),
                minCardPositionUseCase.toMediator2(),
                updateFollowBlockImplictDialogCountUsecase.toMediator2(),
                cardPositionUseCase.toMediator2(),
                bottomBarDurationUseCase.toMediator2()) as T
        }
      }
}