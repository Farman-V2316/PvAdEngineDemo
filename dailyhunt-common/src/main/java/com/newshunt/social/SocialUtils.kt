package com.newshunt.social

import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.usecase.GetAllUserLikesUsecase
import com.newshunt.common.model.usecase.ResetLikesUsecase
import com.newshunt.common.model.usecase.SyncLikeUsecase
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.usecase.toMediator2

class SocialUtils {

  companion object {

    @JvmStatic
    fun resetLikes() {
      ResetLikesUsecase().toMediator2().execute(Any())
      PreferenceManager.savePreference(AppStatePreference.SOCIAL_LIKE_SYNCED, false)
    }

    @JvmStatic
    fun resetOnLoginChange() {
        resetLikes()
    }

    @JvmStatic
    fun syncLikes() {
      SyncLikeUsecase().toMediator2().execute(Any())
    }

    @JvmStatic
    fun getLikes() {
      if (PreferenceManager.getPreference(AppStatePreference.SOCIAL_LIKE_SYNCED, false)) {
        return
      }
      GetAllUserLikesUsecase().toMediator2().execute(Any())
    }
  }
}