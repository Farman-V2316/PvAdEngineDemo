/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.sso.view.view

import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NavigatorCallback
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.sdk.network.image.Image
import com.newshunt.sso.R
import com.newshunt.sso.SSO

/**
 * A Helper class to update profile pic and also handle the click to land to Profile Activity in
 * FPV mode
 * <p>
 * Created by srikanth.ramaswamy on 07/11/2019.
 */
class ProfilePicHelper(lifecycleOwner: LifecycleOwner,
                       private val profilePicImgView: ImageView,
                       private val pageReferrer: PageReferrer?,
                       private val referrerProviderlistener: ReferrerProviderlistener? = null,
                       private val navigatorCallback: NavigatorCallback) : Observer<SSO.UserDetails>, View.OnClickListener {

    init {
        SSO.getInstance().userDetailsLiveData.observe(lifecycleOwner, this)
        profilePicImgView.setOnClickListener(this)
    }

    override fun onChanged(userDetails: SSO.UserDetails?) {
        userDetails?.userLoginResponse?.profileImage ?: return
        profilePicImgView.context?.let {
            val size = CommonUtils.getDimension(R.dimen.profile_icon_height_width)
            Image.load(ImageUrlReplacer.getQualifiedImageUrl(userDetails.userLoginResponse?.profileImage, size, size))
                    .placeHolder(ContextCompat.getDrawable(it, R.drawable.vector_user_avatar))
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePicImgView)
        }
    }

    override fun onClick(view: View?) {
        view?.context?.let {
            val referrer = referrerProviderlistener?.latestPageReferrer ?: pageReferrer
            ?: PageReferrer(NhGenericReferrer.ORGANIC)
            CommonNavigator.launchMyProfileActivity(it, SSO.getInstance().isLoggedIn(false),
                    referrer, navigatorCallback)
        }
    }
}