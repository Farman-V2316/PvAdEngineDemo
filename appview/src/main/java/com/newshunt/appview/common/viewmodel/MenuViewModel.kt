/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.viewmodel

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.news.model.entity.MenuL1PostClkAction
import com.newshunt.dataentity.social.entity.MenuL1Id
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.MenuMeta
import com.newshunt.dataentity.social.entity.MenuOption
import com.newshunt.dataentity.social.entity.MenuOptionListData
import com.newshunt.news.model.usecase.*
import com.newshunt.news.view.activity.NewsBaseActivity
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel
import javax.inject.Inject
import javax.inject.Named

/**
 * @author amit.chaudhary
 */
class MenuViewModel(context: Application,
                    private val location: MenuLocation,
                    private val menuMetaUsecase: MediatorUsecase<Bundle, MenuMeta>,
                    menuListUsecaseForPost: MediatorUsecase<Bundle, MenuOptionListData>,
                    menuLocationMappedUsecase: MediatorUsecase<Bundle, MenuOptionListData>,
                    private val menuClickDelegate: MenuClickDelegate,
                    val canAutoPlayVideo: Boolean) :
        AndroidViewModel(context),
        MenuClickHandlingViewModel by menuClickDelegate {
    lateinit var fragmentCommunicationsViewModel:FragmentCommunicationsViewModel


    var l1Clicked = false
        private set

    var clickedL1Id: String? = null
        private set

    var l1HideCard: Boolean? = false
        private set

    private val menuListUsecase: MediatorUsecase<Bundle, MenuOptionListData> =
            if (location == MenuLocation.HASHTAG ||
                    location == MenuLocation.NP_LANDING) {
                menuLocationMappedUsecase
            } else {
                menuListUsecaseForPost
            }

    val menuOption = menuListUsecase.data()

    val menuMetaDetail = menuMetaUsecase.data()

    val menuVisibility: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    val menuDeletePostData = menuClickDelegate.deletePostData

    fun start(args: Bundle = Bundle()) {
        args.putBoolean(Constants.BUNDLE_CAN_AUTOPLAY_VIDEO,canAutoPlayVideo)
        menuMetaUsecase.execute(args)
        menuListUsecase.execute(args)
    }

    override fun onCleared() {
        super.onCleared()
        menuClickDelegate.clear()
    }

    class Factory @Inject constructor(private val app: Application,
                                      private val menuListUsecase: MenuListUsecase,
                                      private val location: MenuLocation,
                                      private val menuMetaUsecase: MenuMetaUsecase,
                                      private val menuLocationMappedUsecase: MenuLocationMappedUsecase,
                                      private val menuClickDelegate: MenuClickDelegate,
                                      @Named("canAutoPlayVideo")
                                      private val canAutoPlayVideo: Boolean) :
            ViewModelProvider.AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MenuViewModel(
                    context = app,
                    location = location,
                    menuListUsecaseForPost = menuListUsecase,
                    menuMetaUsecase = menuMetaUsecase,
                    menuLocationMappedUsecase = menuLocationMappedUsecase,
                    menuClickDelegate = menuClickDelegate,
                    canAutoPlayVideo = canAutoPlayVideo) as T
        }

    }

    override fun onMenuL1OptionClick(
            view: View,
            menuOption: MenuOption,
            asset: CommonAsset?,
            pageEntity: PageEntity?,
            activity: Activity?
    ) {
        l1Clicked = true
        clickedL1Id = menuOption.menuL1.id
        l1HideCard = menuOption.menuL1.hideCard
        if (menuOption.menuL1.id != MenuL1Id.L1_DELETE_POST.name) {
            menuVisibility.postValue(false)
        }
        val actionValue = menuOption.menuL1.postAction ?: MenuL1PostClkAction.NA.name
        val action = MenuL1PostClkAction.valueOf(actionValue)
        fragmentCommunicationsViewModel=  ViewModelProviders.of(activity as FragmentActivity)
            .get(FragmentCommunicationsViewModel::class.java)

        if (action.isBlock()) {
            val bundle = Bundle();
            bundle.putSerializable(Constants.SOURCE_ENTITY, asset)
            bundle.putLong(Constants.EVENT_CREATED_AT,System.currentTimeMillis())
            fragmentCommunicationsViewModel.fragmentCommunicationLiveData.postValue(
                FragmentCommunicationEvent(
                    (activity as? NewsBaseActivity)?.activityId ?: -1,
                    useCase = Constants.CAROUSEL_LOAD_EXPLICIT_SIGNAL,
                    anyEnum = FollowActionType.BLOCK.name,
                    arguments = bundle
                )
            )
       }
        if (action == MenuL1PostClkAction.FOLLOW) {
            val bundle = Bundle();
            bundle.putSerializable(Constants.SOURCE_ENTITY, asset)
            bundle.putLong(Constants.EVENT_CREATED_AT,System.currentTimeMillis())
            fragmentCommunicationsViewModel.fragmentCommunicationLiveData.postValue(
                FragmentCommunicationEvent(
                    (activity as? NewsBaseActivity)?.activityId ?: -1,
                    useCase = Constants.CAROUSEL_LOAD_EXPLICIT_SIGNAL,
                    anyEnum = FollowActionType.FOLLOW.name,
                    arguments = bundle
                )
            )
        }

        menuClickDelegate.onMenuL1OptionClick(view, menuOption, asset, pageEntity, activity)

    }
}

private const val LOG_TAG = "MenuViewModel"