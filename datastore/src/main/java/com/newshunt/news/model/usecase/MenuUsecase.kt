/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.PostPrivacy
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.MemberRole
import com.newshunt.dataentity.news.model.entity.MenuL1Filter
import com.newshunt.dataentity.news.model.entity.MenuL1PostClkAction
import com.newshunt.dataentity.social.entity.MenuL1Id
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.MenuMeta
import com.newshunt.dataentity.social.entity.MenuOption
import com.newshunt.dataentity.social.entity.MenuOptionListData
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.zipWith
import com.newshunt.news.model.daos.MenuDao
import javax.inject.Inject

/**
 * @author amit.chaudhary
 */
class MenuListUsecase @Inject constructor(private val menuDao: MenuDao) : MediatorUsecase<Bundle, MenuOptionListData> {

    private val _data = MediatorLiveData<Result0<MenuOptionListData>>()
    private val _sta = MutableLiveData<Boolean>()

    override fun execute(t: Bundle): Boolean {
        val menuLocation: MenuLocation = t.getSerializable(Constants.BUNDLE_MENU_CLICK_LOCATION) as?
                MenuLocation
                ?: run {
                    Logger.e(LOG_TAG, "Menu location can not be null")
                    return false
                }

        val primaryMenuLocation = menuLocation.primaryType ?: menuLocation

        val card = t.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset ?: kotlin.run {
            Logger.e(LOG_TAG, "Post can not be null")
            return false
        }

        val loggedInUserId = t.getString(Constants.BUNDLE_USER_ID)

        val groupInfo = t.getSerializable(Constants.BUNDLE_GROUP_INFO) as? GroupInfo

        val canAutoPlayVideo = t.getBoolean(Constants.BUNDLE_CAN_AUTOPLAY_VIDEO, false)

        val userIsOwnerOfGroup: Boolean = if (groupInfo != null) {
            loggedInUserId != null &&
                    (groupInfo.userRole == MemberRole.ADMIN ||
                            groupInfo.userRole == MemberRole.OWNER)
        } else {
            false
        }

        val format = card.i_format()
        val subFormat = card.i_subFormat()
        val uiType = card.i_uiType()
        val entityId = card.i_source()?.id ?: Constants.EMPTY_STRING
        val entityType = card.i_source()?.entityType ?: Constants.EMPTY_STRING
        val entitySubType: String? = card.i_source()?.type ?: Constants.EMPTY_STRING
        val isCreator = card.i_source()?.id == loggedInUserId && loggedInUserId != null
        val sourceType = card.i_source()?.type
        val sourceTypeFilter = listOf(IS_UGC, IS_NOT_UGC).filter {
            it != getSourceFilter(sourceType)
        }
        val canShare = card.i_postPrivacy() != PostPrivacy.PRIVATE

        val canDownloadVideo = CommonUtils.isDownloadableUrl(card.i_videoAsset())

        val notAllowedFilterLiveData = Transformations.map(menuDao.getEntityFollowAction(entityId = entityId,
                entitySubType = entitySubType,
                entityType = entityType)) {
            MenuDao.notAllowedFilters(it)
        }
        val disabledFilters = sourceTypeFilter + noRegDisabledFilters() + saveUnsaveFilters(menuLocation)
        val commentIds = allowedFilter(menuLocation)
        Logger.i(LOG_TAG,"Menu List Fetch : format : ${format} " +
                "\nsubFormat : ${subFormat} " +
                "\nuiType : ${uiType} " +
                "\nentityId : ${entityId} " +
                "\nentityType : ${entityType} " +
                "\nentitySubType : ${entitySubType} " +
                "\nisCreator : ${isCreator} " +
                "\nsourceType : ${sourceType} " +
                "\ndisabledFilters : ${disabledFilters}")

        if (format != null &&
                subFormat != null &&
                uiType != null) {
            val result : LiveData<List<MenuOption>>
                    if(menuLocation == MenuLocation.COMMENTS){
                        val listFromDbObs =
                                Transformations.map(menuDao.optionsMatching1(format, subFormat,
                                    uiType,
                                    primaryMenuLocation.name, commentIds)) {
                                it.map { menuItemView ->
                                    menuItemView.toMenuOption() }
                            }
                         result = listFromDbObs

                    }else {
                        val listFromDbObs = Transformations.switchMap(notAllowedFilterLiveData) { filterNotAllowed ->
                            val allDisabledFilters = disabledFilters + filterNotAllowed
                            Transformations.map(menuDao.optionsMatching1(format, subFormat,
                                    uiType,
                                    primaryMenuLocation.name,
                                    card.i_id(), allDisabledFilters)) {
                                it.map { menuItemView -> menuItemView.toMenuOption() }
                            }
                        }
                        val extraOptions = extraMenuOptionId(isCreator, userIsOwnerOfGroup, menuLocation)

                        val listFromHardCoded2 = Transformations.switchMap(notAllowedFilterLiveData) { filterNotAllowed ->
                            menuDao.optionMatchingKeys(extraOptions.map { it.name }, filterNotAllowed)
                        }.zipWith(menuDao.l2OptionMatchingKeys(extraOptions.mapNotNull { it.l2?.name })) { l1Items, l2Items ->
                            l1Items.map { l1 ->
                                MenuOption(l1, l2Items.find { l2 -> l2.id == MenuL1Id.valueOf(l1.id).l2?.name })
                            }
                        }

                        result = listFromDbObs.zipWith(listFromHardCoded2) { x, y ->
                            x.union(y).toList()
                        }
                    }

            _data.addSource(Transformations.map(result) { menuList ->
                MenuOptionListData(menuList = menuList.filter { menuOptionItem ->
                    filterMenuOption(
                            canDownloadVideo = canDownloadVideo,
                            userIsCreatorOfPost = isCreator,
                            userIsOwnerOfGroup = userIsOwnerOfGroup,
                            canAutoPlayVideo = canAutoPlayVideo,
                            menuLocation = menuLocation,
                            menuOption = menuOptionItem,
                            canShare = canShare
                    )
                }, card = card)
            }) {
                if (it == null || it.menuList.isNullOrEmpty()) {
                    _data.value = Result0.failure(Throwable("null or empty menu list coming for " +
                            "$format - $subFormat - $uiType - ${card.i_id()}"))
                } else {
                    _data.value = Result0.success(it)
                }
                _sta.value = false
            }
            return true
        }
        return false
    }

    private fun saveUnsaveFilters(menuLocation: MenuLocation): List<String> {
        return if(menuLocation == MenuLocation.DETAIL_UNIFIED_BAR)
            listOf(MenuL1Filter.CAN_SAVE.name, MenuL1Filter.CAN_UNSAVE.name)
        else emptyList()
    }

    private fun allowedFilter(menuLocation: MenuLocation): List<String> {
        return if(menuLocation == MenuLocation.COMMENTS)
            listOf(MenuL1Id.L1_SHARE.name, MenuL1Id.L1_REPORT.name)
        else emptyList()
    }




    /**
     * returns the list of filters that need registration, so that they can be filtered out of the
     * query, if registration is not done
     */
    private fun noRegDisabledFilters(): List<String> {
        if(isRegistered()) return emptyList()
        return listOf(MenuL1Filter.CAN_BLOCK.name)
    }

    override fun data(): LiveData<Result0<MenuOptionListData>> {
        return _data
    }

    override fun status(): LiveData<Boolean> {
        return _sta
    }

    private fun filterMenuOption(canDownloadVideo: Boolean = false,
                                 userIsCreatorOfPost: Boolean = false,
                                 userIsOwnerOfGroup: Boolean = false,
                                 canAutoPlayVideo: Boolean = false,
                                 menuLocation: MenuLocation,
                                 menuOption: MenuOption,
                                 canShare: Boolean): Boolean {
        if (userIsCreatorOfPost && menuOption.menuL1.hideForCreator) {
            return false
        }
        if(!isRegistered()) { // if not-registered, disable all L1's that 'block'
            val action =   menuOption.menuL1.postAction?.let { postAction ->
                kotlin.runCatching { MenuL1PostClkAction.valueOf(postAction) }.getOrNull()
            }?: MenuL1PostClkAction.NA
            if (action.isBlock()) {
                Logger.d(LOG_TAG, "filterMenuOption: removed ${menuOption.menuL1.id}, not-registered")
                return false
            }
        }
        return when (menuOption.menuL1.postAction) {
            MenuL1PostClkAction.DOWNLOAD_VIDEO.name -> canDownloadVideo
            MenuL1PostClkAction.DELETE_POST.name -> userIsCreatorOfPost ||
                    (userIsOwnerOfGroup && menuLocation == MenuLocation.GROUP_LIST)
            MenuL1PostClkAction.BROWSE_BY_SOURCE.name -> menuLocation != MenuLocation.PROFILE_POST_LIST
            MenuL1PostClkAction.ENABLE_AUTOPLAY.name -> !canAutoPlayVideo //To pass enable
            // autoplay current state of autoplay needs to be false
            MenuL1PostClkAction.DISABLE_AUTOPLAY.name -> canAutoPlayVideo //To pass disable
            // autoplay current state of autoplay needs to be true
            MenuL1PostClkAction.SHARE.name -> canShare
            else -> true
        }
    }

    private fun extraMenuOptionId(userIsCreatorOfPost: Boolean = false,
                                  userIsOwnerOfGroup: Boolean = false,
                                  menuLocation: MenuLocation): List<MenuL1Id> {
        val idSet = mutableSetOf<MenuL1Id>()
        if (userIsCreatorOfPost || userIsOwnerOfGroup && menuLocation == MenuLocation.GROUP_LIST) {
            idSet.add(MenuL1Id.L1_DELETE_POST)
        }
        return idSet.toList()
    }
}

/**
 * @author amit.chaudhary
 */
class MenuMetaUsecase @Inject constructor(private val menuDao: MenuDao) : MediatorUsecase<Bundle, MenuMeta> {
    private val _data = MediatorLiveData<Result0<MenuMeta>>()
    private val _sta = MutableLiveData<Boolean>()

    override fun execute(t: Bundle): Boolean {
        _data.addSource(menuDao.fetchMenuMetaLive()) {
            if (it.isNullOrEmpty()) {
                _data.value = Result0.failure(Throwable("could not load menu dictionary meta"))
            } else {
                _data.value = Result0.success(it[0])
            }
            _sta.value = false
        }
        return true
    }

    override fun data(): LiveData<Result0<MenuMeta>> {
        return _data
    }

    override fun status(): LiveData<Boolean> {
        return _sta
    }
}

class MenuLocationMappedUsecase @Inject constructor(private val menuDao: MenuDao) :
        MediatorUsecase<Bundle, MenuOptionListData> {
    private val _data = MediatorLiveData<Result0<MenuOptionListData>>()
    private val _sta = MutableLiveData<Boolean>()

    override fun execute(t: Bundle): Boolean {
        val location = t.getSerializable(Constants.BUNDLE_MENU_CLICK_LOCATION) as?
                MenuLocation ?: run {
            Logger.e(LOG_TAG, "location can not be null")
            return false
        }
        val pageEntity = t.getSerializable(Constants.BUNDLE_MENU_ENTITY_INFO) as? PageEntity

        val optionToShow: List<MenuL1Id> = when (location) {
            //IN UI-spec there is no three dot.
            MenuLocation.HASHTAG ->
                listOf(
                        MenuL1Id.L1_SHARE,
                        MenuL1Id.L1_REPORT
                )
            MenuLocation.COMMENTS ->
                listOf(
                        MenuL1Id.L1_SHARE,
                        MenuL1Id.L1_REPORT
                )
            MenuLocation.NP_LANDING ->
                if (isRegistered()) listOf(MenuL1Id.L1_BLOCK_SOURCE, MenuL1Id.L1_UNBLOCK_SOURCE, MenuL1Id.L1_REPORT)
                else {
                    Logger.d(LOG_TAG, "MenuLocationMappedUsecase.execute: " +
                            "MenuLocation.NP_LANDING; empty due to noReg ")
                    emptyList()
                }
            else -> emptyList()
        }

        val listFromHardCoded3 = if (pageEntity != null) {
            Transformations.switchMap(
                    menuDao.getEntityFollowAction(entityId = pageEntity.id,
                            entitySubType = pageEntity.subType,
                            entityType = pageEntity.entityType)) {
                menuDao.optionMatchingKeys(optionToShow.map { it.name }, MenuDao.notAllowedFilters(it))
            }
        } else {
            menuDao.optionMatchingKeys(optionToShow.map { it.name }, emptyList())
        }

        val listFromHardCoded2 = listFromHardCoded3.zipWith(menuDao
                .l2OptionMatchingKeys
                (optionToShow
                        .mapNotNull {
                            it.l2?.name
                        })) { l1Items, l2Items ->
            l1Items.map { l1 ->
                MenuOption(l1, l2Items.find { l2 -> l2.id == MenuL1Id.valueOf(l1.id).l2?.name })
            }
        }

        val result = Transformations.map(listFromHardCoded2) {
            MenuOptionListData(menuList = it, card = null, pageEntity = pageEntity)
        }
        _data.addSource(result) {
            if (it == null || it.menuList.isNullOrEmpty()) {
                _data.value = Result0.failure(Throwable("null or empty menu list coming for " +
                        "location = $location"))
            } else {
                _data.value = Result0.success(it)
            }
            _sta.value = false
        }
        return true
    }

    override fun data(): LiveData<Result0<MenuOptionListData>> {
        return _data
    }

    override fun status(): LiveData<Boolean> {
        return _sta
    }
}

private fun getSourceFilter(type: String?): String {
    return when (type) {
        UGC_TYPE -> IS_UGC
        PGC_TYPE, ICC_TYPE, OGC_TYPE -> IS_NOT_UGC
        else -> NA
    }
}

private fun isRegistered() = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false)

private const val LOG_TAG = "MenuUsecase"
private const val IS_UGC = "IS_UGC"
private const val IS_NOT_UGC = "ISNOT_UGC"
private const val NA = "NA"

private const val UGC_TYPE = "UGC"
private const val PGC_TYPE = "PGC"
private const val OGC_TYPE = "OGC"
private const val ICC_TYPE = "ICC"