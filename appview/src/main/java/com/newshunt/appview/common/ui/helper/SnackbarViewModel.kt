/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.helper

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.SnackbarViewModel.Companion.onFollowChangeEvent
import com.newshunt.common.follow.constructFollowSnackBarMetaData
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.CustomSnackBar
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.usecase.GetLatestUploadedPostUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import javax.inject.Inject
import javax.inject.Named

/**
 * For showing snackbar when something is followed/unfollowed, or after when post upload
 * Util method [onFollowChangeEvent] is provided which can be used to show follow snackbar
 *
 * @author satosh.dhanyamraju
 */
class SnackbarViewModel(private val getLatestFollowUsecase: MediatorUsecase<Bundle, List<FollowSyncEntity>>,
                        private val getLatestUploadedPostUsecase: MediatorUsecase<Bundle, CreatePostEntity?>) : ViewModel() {

    /**
     * view should observe this and call [onFollowChangeEvent]
     */
    val followChanges = getLatestFollowUsecase.data()
    val newPostChanges = getLatestUploadedPostUsecase.data()
    private val creationTime = System.currentTimeMillis()
    private var executed = false

    /**
     * to be called in single thread; UI thread is fine.
     */
    fun start() {
        if(executed) return
        val bundle = bundleOf(Constants.BUNDLE_CREATION_TIME to creationTime)
        getLatestFollowUsecase.execute(bundle)
        getLatestUploadedPostUsecase.execute(bundle)
        executed = true
    }

    companion object {
        private const val TAG = "SnackbarViewModel"

        fun onFollowChangeEvent(it : Result0<List<FollowSyncEntity>>, rootView: View) {
            if (it.isSuccess && it.getOrNull()?.isNotEmpty() == true) {
                val name = it.getOrNull()?.first()?.actionableEntity?.displayName
                val isFollow = it.getOrNull()?.first()?.action == FollowActionType.FOLLOW
                if (name == null) {
                    Logger.e(TAG, "name is null")
                    return
                }
                if (isFollow) {
                    CustomSnackBar.showSnackBar(rootView, rootView.context,
                            constructFollowSnackBarMetaData(name))?.show()
                } else {
                    CustomSnackBar.showUnfollowSnackBar(rootView, rootView.context,
                            constructFollowSnackBarMetaData(name))?.show()
                }
            }
        }



        var lastCreatedId : Int? = null // singleton. If shown in activity, no need to show again.
        fun onPostUploaded(
                result : Result0<CreatePostEntity?>,
                view: View?,
                showSnackbar: Boolean = true,// false only if current tab is myposts. do all
                snackbarStringId: Int? = null,
                actionBarStringId: Int? = null
                // processing except showing snackbar.
        ) {
            val errorMsg = when  {
                view == null -> "view is null"
                result.isFailure -> "error ${result.exceptionOrNull()}"
                else -> null
            }
            if(errorMsg != null) {
                Logger.e(TAG, "onPostUploaded: no snackbar; $errorMsg")
                return
            }
            val actionText = if (actionBarStringId == null) {
                Constants.EMPTY_STRING
            } else {
                CommonUtils.getString(actionBarStringId)
            }
            var snackbarText =
                    if (snackbarStringId == null) {
                        CommonUtils.getString(R.string.cp_success_message)
                    } else {
                        CommonUtils.getString(snackbarStringId)
                    }
            // TODO(satosh.dhanyamraju): i18n, referrers.
            if (result.isSuccess && result.getOrNull() != null) {
                val latestCreatedPost = result.getOrNull()
                if(latestCreatedPost?.cpId != lastCreatedId) {
                    if(showSnackbar) {
                        latestCreatedPost?.message?.let {
                            if (it.isNotEmpty()) {
                                snackbarText = it
                            }
                        }
                        GenericCustomSnackBar.showSnackBar(
                                view!!,
                                view!!.context,
                                snackbarText,
                                Snackbar.LENGTH_LONG,
                                action = actionText,
                                customActionClickListener = View.OnClickListener {
                                    NavigationHelper.navigationLiveData.value =
                                            NavigationEvent(CommonNavigator.getProfileMyPostsIntent(PageReferrer()))
                                },
                                increaseTouchAreaBy = CommonUtils.getPixelFromDP(16, CommonUtils.getApplication())
                        ).show()
                    }
                    lastCreatedId = latestCreatedPost?.cpId
                }
            } else {
                Logger.e(TAG, "onPostUploaded: no snackbar; got null item")
            }
        }
    }

    /**
     * To be injected using dagger
     */
    class Factory @Inject constructor(
            @Named("getFollowsUc") val getLatestFollowUsecase: MediatorUsecase<Bundle, List<FollowSyncEntity>>,
            val getLatestUploadedPostUsecase : GetLatestUploadedPostUsecase
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SnackbarViewModel(getLatestFollowUsecase, getLatestUploadedPostUsecase) as T
        }

    }
}