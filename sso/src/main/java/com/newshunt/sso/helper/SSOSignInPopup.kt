package com.newshunt.sso.helper

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.common.Constants
import com.newshunt.sso.SignInUIModes
import com.newshunt.sso.view.fragment.SignOnFragment

/**
 * @author santhosh.kc
 */
interface ShowSignInPopup {
    fun showSignInPopup()
}

const val OVERLAY_SIGN_ON_FRAGMENT_TAG = "over_sign_on_fragment_tag"
const val TPV_SIGNIN_FRAGMENT_TAG = "TPV_SIGNIN_FRAGMENT_TAG"

object SSOSignInPopup {

    fun showSignInPopup(activity: AppCompatActivity?, viewHolderId: Int = View.NO_ID,
                        tag: String = OVERLAY_SIGN_ON_FRAGMENT_TAG,
                        signInUIModes: SignInUIModes = SignInUIModes.SIGN_IN_WITH_CROSS_BUTTON,
                        profileName: String? = Constants.EMPTY_STRING,
                        referrer: PageReferrer?) {
        activity ?: return
        if (viewHolderId == View.NO_ID) {
            return
        }

        val fragmentTransaction = activity.supportFragmentManager.beginTransaction()
        fragmentTransaction.add(viewHolderId, createSignOnFragment(signInUIModes, profileName, referrer), tag)
        fragmentTransaction.commit()
    }

    fun dismissSignInPopup(activity: AppCompatActivity?,
                           tag: String = OVERLAY_SIGN_ON_FRAGMENT_TAG) {
        val supportFragmentManager = activity?.supportFragmentManager ?: return
        supportFragmentManager.findFragmentByTag(tag)?.apply {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.remove(this)
            fragmentTransaction.commitAllowingStateLoss()
        }
    }
}

fun createSignOnFragment(uiMode : SignInUIModes,
                         profileName: String?,
                         referrer: PageReferrer?) : SignOnFragment {
    val fragment = SignOnFragment()
    val args = Bundle()
    args.putString(Constants.BUNDLE_SIGN_ON_UI_MODE, uiMode.name)
    args.putBoolean(Constants.BUNDLE_REFERRER_VIEW_IS_FVP, false)
    args.putSerializable(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
    profileName?.let{
        args.putString(Constants.BUNDLE_SIGN_IN_TPV_NAME, it)
    }
    fragment.arguments = args
    return fragment
}