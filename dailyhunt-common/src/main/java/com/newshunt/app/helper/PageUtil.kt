package com.newshunt.app.helper

import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.follow.entity.FollowSnackBarInfo
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.deeplink.navigator.CommonNavigator

object PageUtil {

  @JvmStatic
  fun isFollowingTabEntity(pageEntity: PageEntity?): Boolean {
    if (pageEntity == null) {
      return false
    }

    val tabInfo = FollowSnackBarInfo.
        getInstance(CommonNavigator.readFollowSnackBarEntityFromPreferences()).
        getFollowTabInfo() ?:return false

    return (CommonUtils.equals(pageEntity.id, tabInfo.first)) &&
        CommonUtils.equals(pageEntity.entityType, tabInfo.second)
  }
}