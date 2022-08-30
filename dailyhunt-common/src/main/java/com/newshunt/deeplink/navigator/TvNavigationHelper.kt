package com.newshunt.deeplink.navigator

import android.content.Intent
import android.os.Bundle
import com.dailyhunt.tv.helper.TVConstants
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.notification.NavigationType
import com.newshunt.dataentity.notification.NewsNavModel
import com.newshunt.dataentity.notification.TVNavModel
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider.getAnyUserAppSectionOfType
import com.newshunt.news.util.NewsConstants


object TvNavigationHelper {

  @JvmStatic
  fun getTargetIntent(tvNavModel: TVNavModel, pageReferrer: PageReferrer?) : Intent {
    val navReferrer = pageReferrer
    if (null == tvNavModel || !CommonUtils.isValidInteger(tvNavModel.getsType())) {
      if (isTVSectionUnAvailable(pageReferrer)) {
        return getIntentOnNoTV(pageReferrer)
      }
    }

    val navigationType = NavigationType.fromIndex(tvNavModel.getsType().toInt())
    return if (navigationType == null) {
      getIntentForTVHome(tvNavModel)
    } else {
      when (navigationType) {
        NavigationType.TYPE_DH_TV_OPEN_TO_DETAIL,
        NavigationType.TYPE_TV_OPEN_TO_DETAIL -> {
          val intent = Intent(Constants.NEWS_DETAIL_ACTION)
          intent.setPackage(CommonUtils.getApplication().packageName)
          intent.putExtra(Constants.STORY_ID, tvNavModel.unitId)

          if (!CommonUtils.isEmpty(tvNavModel.baseInfo.urlParamsMap)) {
            navReferrer?.referrer = NhGenericReferrer.ORGANIC_SOCIAL
            intent.putExtra(Constants.REFERRER_RAW, JsonUtils.toJson(tvNavModel.baseInfo.urlParamsMap))
          }

          // Add swipe urls here
          setNotificationBackAndSwipeUrls(intent, navReferrer, tvNavModel)

          intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
          return intent
        }
        NavigationType.TYPE_DH_TV_OPEN_TO_CHANNEL,
        NavigationType.TYPE_TV_OPEN_TO_GROUP_TAB,
        NavigationType.TYPE_TV_OPEN_TO_CHANNEL -> {
          getTvChannelOrGroupIntent(tvNavModel,pageReferrer)
        }
        else -> getIntentForTVHome(tvNavModel)
      }
    }
  }


  private fun setNotificationBackAndSwipeUrls(intent: Intent?, pageReferrer: PageReferrer?,
                                              tvNavModel: TVNavModel?) {
    if (intent == null || tvNavModel == null ||
            !CommonNavigator.isFromNotificationTray(pageReferrer)) {
      return
    }

    // For notification back and swipe action
    if (!CommonUtils.isEmpty(tvNavModel.baseInfo.v4BackUrl)) {
      intent.putExtra(Constants.V4BACKURL, tvNavModel.baseInfo.v4BackUrl)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    if (!CommonUtils.isEmpty(tvNavModel.baseInfo.v4SwipeUrl)) {
      intent.putExtra(Constants.V4SWIPEURL, tvNavModel.baseInfo.v4SwipeUrl)
    }
  }

  @JvmStatic
  fun isHomeRoutable(tvNavModel: TVNavModel) : Boolean{
    val navigationType = NavigationType.fromIndex(tvNavModel.getsType().toInt())
    return if (navigationType == null) {
       false
    } else {
      when (navigationType) {
        NavigationType.TYPE_DH_TV_OPEN_TO_CHANNEL,
        NavigationType.TYPE_TV_OPEN_TO_GROUP_TAB,
        NavigationType.TYPE_TV_OPEN_TO_CHANNEL -> true
        else -> false
      }
    }

  }

  private fun isTVSectionUnAvailable(referrer: PageReferrer?): Boolean {
    return !AppSectionsProvider.isSectionAvailable(AppSection.TV) ||
        CommonNavigator.isFromNotificationTray(referrer)
  }

  private fun getIntentOnNoTV(pageReferrer: PageReferrer?): Intent {
    return if (CommonNavigator.isFromNotificationTray(pageReferrer)) {
      CommonNavigator.getSectionHomeRouterLaunchIntent(AppSection.TV, pageReferrer)
    } else CommonNavigator.getLastSectionHomeLaunchIntent(CommonUtils.getApplication(), pageReferrer)
  }

  @JvmStatic
  fun getIntentForTVHome(navigationModel: TVNavModel, pageEntity: PageEntity? = null): Intent {
    val  targetIntent = Intent(Constants.NEWS_HOME_ACTION)
    targetIntent.putExtra(TVConstants.KEY_NOTIFICATION_DATA, navigationModel)
    val prevTVAppSection = getAnyUserAppSectionOfType(AppSection.TV)
    if (prevTVAppSection != null) {
      targetIntent.putExtra(Constants.APP_SECTION_ID, prevTVAppSection.id)
    }
    if (pageEntity != null) {
      val bundle = Bundle()
      bundle.putSerializable(NewsConstants.BUNDLE_NEWSPAGE, pageEntity)
      targetIntent.putExtra(NewsConstants.EXTRA_PAGE_ADDED, bundle)
    }
    targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //Setting true to handle onboading completion if not done

    return targetIntent
  }

  @JvmStatic
  fun getTvChannelOrGroupIntent(tvNavModel: TVNavModel, pageReferrer: PageReferrer?) : Intent{
      val intent = Intent(Constants.ENTITY_OPEN_ACTION)
	  val id = if (CommonUtils.isEmpty(tvNavModel.groupId)) tvNavModel.unitId else tvNavModel.groupId
      intent.putExtra(NewsConstants.ENTITY_KEY, id)
      intent.putExtra(NewsConstants.ENTITY_TYPE, tvNavModel.modelType)
      intent.putExtra(NewsConstants.DH_SECTION, PageSection.TV.section)
      if (pageReferrer != null) {
        intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
      }
      return intent
  }
}