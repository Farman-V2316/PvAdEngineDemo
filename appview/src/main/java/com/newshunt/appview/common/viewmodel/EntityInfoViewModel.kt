/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.viewmodel

import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.R
import com.newshunt.appview.common.model.usecase.EntityConstants
import com.newshunt.appview.common.model.usecase.GetEntityInfoUsecase
import com.newshunt.appview.common.model.usecase.ObservableEntityInfoUsecase
import com.newshunt.appview.common.postcreation.analytics.helper.CreatePostAnalyticsHelper
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.EntityInfoList
import com.newshunt.dataentity.common.pages.EntityInfoView
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.UiUtils
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.view.EntityImageUtils
import com.newshunt.dhutil.view.customview.ExpandableTextView
import com.newshunt.dhutil.view.listener.TextDescriptionSizeChangeListener
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.model.repo.FollowRepo
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.ToggleFollowUseCase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.sdk.network.image.Image

enum class EntityUIEvent {
  BACK_BUTTON
}

class EntityInfoViewModel(val id : String, val entityType: String, val section: String): ViewModel(), ClickHandlingViewModel {

  private val observableInfoUsecase = ObservableEntityInfoUsecase()
  private val getEntityMediatorUsecase = GetEntityInfoUsecase().toMediator2()
  val entityLiveData : LiveData<Result0<EntityInfoList?>>
  val entityUiLiveData = MutableLiveData<EntityUIEvent>()
  val entityErrorLiveData : LiveData<Result0<Any>>
  private var langCode: String? = UserPreferenceUtil.getUserLanguages()

  init {
    entityLiveData = observableInfoUsecase.data()
    entityErrorLiveData = getEntityMediatorUsecase.data()
    observableInfoUsecase.execute(bundleOf(EntityConstants.BUNDLE_ID to id,
        EntityConstants.BUNDLE_SECTION to section, EntityConstants.BUNDLE_LEGACY_KEY to id))
  }

  fun updateLangCode(language: String) {
    if (langCode == null) {
      langCode = language
    }
  }

  fun onViewStarted() {
    if (CommonUtils.isEmpty(langCode)) {
      langCode = Constants.ENGLISH_LANGUAGE_CODE
    }

    getEntityMediatorUsecase.execute(bundleOf(EntityConstants.BUNDLE_ID to id,
        EntityConstants.BUNDLE_ENTITY_TYPE to entityType, EntityConstants.BUNDLE_SECTION to section,
        EntityConstants.BUNDLE_LANG_CODE to langCode))
  }

  override fun onViewClick(view: View, item: Any) {
    when (view.id) {
      R.id.follow_button, R.id.follow_button_profile -> {
        val entity = item as EntityInfoView
        val followEntity = ActionableEntity(entityId = entity.pageEntity.id, entityType = entity.i_type(),
            entitySubType = entity.pageEntity.subType,
            displayName = entity.pageEntity.displayName, entityImageUrl = entity.pageEntity.entityImageUrl,
            iconUrl = entity.pageEntity.header?.logoUrl, deeplinkUrl = entity.pageEntity.deeplinkUrl,
                experiment = entity.i_experiments(), nameEnglish = entity.nameEnglish())
        AnalyticsHelper2.logFollowButtonClickEvent(followEntity, PageReferrer(NewsReferrer.ENTITY_BROWSING), entity.isFollowed.not(), section)
        ToggleFollowUseCase(FollowRepo(SocialDB.instance().followEntityDao())).toMediator2().execute(bundleOf(ToggleFollowUseCase.B_FOLLOW_ENTITY to followEntity))
      }
      R.id.entity_create_post_view -> {
        val entity = item as EntityInfoView
        val pagerReferrer = PageReferrer(NewsReferrer.ENTITY_BROWSING, id)
        CreatePostAnalyticsHelper.logCreatePostClickEvent(pagerReferrer)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(
            CommonNavigator.getPostCreationIntent(null, null,
                SearchSuggestionItem(itemId = entity.parentId, suggestion = entity.pageEntity.createPostText?:"",
                    name = entity.pageEntity.displayName, typeName = entity.pageEntity.createPostType?:""), pagerReferrer),
            callback = null))
      }
    }
  }

  override fun onViewClick(view: View) {
    when (view.id) {
      R.id.actionbar_back_button_layout -> {
        entityUiLiveData.postValue(EntityUIEvent.BACK_BUTTON)

      }
    }
  }

  override fun onThreeDotMenuClick(view: View, item: Any?) {
    super.onThreeDotMenuClick(view, item)
    if (item !is EntityInfoView) {
      return
    }
    val intent = Intent(Constants.MENU_FRAGMENT_OPEN_ACTION)
    val location = if (item.pageEntity.entityType == MenuLocation.HASHTAG.name) {
      MenuLocation.HASHTAG
    } else {
      MenuLocation.NP_LANDING
    }
    intent.putExtra(Constants.BUNDLE_MENU_CLICK_LOCATION, location)
    intent.putExtra(Constants.BUNDLE_MENU_ENTITY_INFO, item.pageEntity)
    NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent, callback = null))
  }
}

class EntityInfoViewModelFactory(val id: String, val entityType: String, val section: String) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
   return EntityInfoViewModel(id, entityType, section) as T
  }

}


