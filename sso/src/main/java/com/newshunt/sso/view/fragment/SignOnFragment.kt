/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.view.fragment

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Constants.AUTO_LOGIN
import com.newshunt.common.helper.common.Constants.BROWSER_TYPE
import com.newshunt.common.helper.common.Constants.DEEPLINK_URL
import com.newshunt.common.helper.common.Constants.REQ_CODE_GOOGLE
import com.newshunt.common.helper.common.Constants.REQ_CODE_LOGIN_RESULT
import com.newshunt.common.helper.common.Constants.REQ_CODE_TRUECALLER
import com.newshunt.common.helper.common.Constants.RETRY_LOGIN
import com.newshunt.common.helper.common.DeeplinkHelper
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.track.DailyhuntUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ImportContactsEvents
import com.newshunt.dataentity.dhutil.model.entity.BrowserType
import com.newshunt.dataentity.model.entity.AuthType
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.onboarding.RegistrationState
import com.newshunt.dataentity.onboarding.RegistrationUpdate
import com.newshunt.dataentity.onboarding.RetryResigtration
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.helper.browser.NHBrowserUtil
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.news.util.NewsConstants
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.sso.R
import com.newshunt.sso.SSO
import com.newshunt.sso.SignInUIModes
import com.newshunt.sso.analytics.SSOAnalyticsUtility
import com.newshunt.sso.analytics.SSOReferrer
import com.newshunt.sso.helper.CustomHashGenerator
import com.newshunt.sso.helper.social.FacebookHelper
import com.newshunt.sso.helper.social.GoogleSignInHelper
import com.newshunt.sso.model.entity.SSOResult
import com.newshunt.sso.model.entity.UserExplicit
import com.newshunt.sso.model.internal.rest.FetchUserProfilesResponse
import com.newshunt.sso.presenter.FetchUserProfilesPresenter
import com.newshunt.sso.presenter.FetchUserProfilesView
import com.newshunt.sso.presenter.SignOnPresenter
import com.newshunt.sso.view.adapters.DHProfilesAdapter
import com.newshunt.sso.view.adapters.ProfileListItemDecorator
import com.newshunt.sso.view.view.SignOnView
import com.squareup.otto.Subscribe

/**
 * @author anshul.jain
 */



class SignOnFragment : BaseFragment(), View.OnClickListener, FetchUserProfilesView, SignOnView,
        GoogleSignInHelper.LoginCallback, FacebookHelper.Callback {

    private lateinit var phoneNumberLogin: NHTextView
    private lateinit var facebookLogin: NHTextView
    private lateinit var googleLogin: NHTextView
    private var hostId: Int = uniqueScreenId
    private val TAG = "SignOnFragment"
    private lateinit var userProfilesList: RecyclerView
    private val presenter = FetchUserProfilesPresenter(this)
    private val handler = Handler()
    private var userImageList: List<String>? = null
    private lateinit var userProfilesTotalCount: NHTextView
    private lateinit var skipButton: TextView
    private lateinit var signUpHeader: TextView
    private lateinit var signUpSubHeader: TextView
    private lateinit var signUpHeaderIcon: ImageView
    private lateinit var skipButtonGroup: ConstraintLayout
    private lateinit var skipButtonTop : NHTextView
    private lateinit var signInCrossButton: ImageView
    private var customSignInHeader: String? = null
    private var autoLogin: Boolean = false
    private var loginType: LoginType? = LoginType.NONE
    private var retryLogin: Boolean = false
    private val DURATION_FOR_PROFILE_LIST_SCROLL = 1000L
    private var deepLinkUrl: String? = null
    private var browserType: BrowserType? = null
    private var useWideViewPort: Boolean = false
    private var clearHistoryOnPageLoad: Boolean = false
    private var facebookHelper: FacebookHelper? = null
    private var googleSignInHelper: GoogleSignInHelper? = null
    private lateinit var signOnPresenter: SignOnPresenter
    private var uiMode: String? = null
    private lateinit var container: ConstraintLayout
    private lateinit var rlProgressBg: RelativeLayout
    private lateinit var rlContainerParent: ConstraintLayout
    private lateinit var tvProgressText: TextView
    private  lateinit var errorParent: LinearLayout
    private var errorMessageBuilder: ErrorMessageBuilder? = null
    private lateinit var termsView: NHTextView
    private var successPendingIntent: PendingIntent? = null //PendingIntent to call after login success
    private var skipPendingIntent: PendingIntent? = null //PendingIntent to call on skip
    private lateinit var userProfilesGroup: Group
    private var referrer: PageReferrer? = null
    private var referrerViewFPV: Boolean? = true
    private var tpvName: String? = null
    private var delaySignInPageViewEvent = false //Don't hit page view onCreate but delay it until uservisiblehint
    private var isSignInPageViewFired = false
    private var isAccountLinkingNeeded = false
    private var selectedLoginType: LoginType? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        val loginTypeStr = activity?.intent?.getSerializableExtra(Constants.LOGIN_TYPE) as?
                String
        loginTypeStr?.let {
            loginType = LoginType.fromValue(it)
        }
        arguments?.let {
            retryLogin = it.getBoolean(RETRY_LOGIN, false)
            hostId = arguments?.getInt(Constants.BUNDLE_UI_COMPONENT_ID, uniqueScreenId) ?: uniqueScreenId
            autoLogin = it.getBoolean(AUTO_LOGIN, false)
            deepLinkUrl = it.getString(DEEPLINK_URL)
            browserType = it.getSerializable(BROWSER_TYPE) as? BrowserType
            useWideViewPort = it.getBoolean(DailyhuntConstants.USE_WIDE_VIEW_PORT, false)
            clearHistoryOnPageLoad = it.getBoolean(DailyhuntConstants
                    .CLEAR_HISTORY_ON_PAGE_LOAD, false)
            customSignInHeader = it.getString(Constants.BUNDLE_SIGN_IN_CUSTOM_HEADER)
            uiMode = it.getString(Constants.BUNDLE_SIGN_ON_UI_MODE)
            tpvName = it.getString(Constants.BUNDLE_SIGN_IN_TPV_NAME)
            successPendingIntent = it.getParcelable(Constants.BUNDLE_SIGNIN_SUCCESS_PENDING_INTENT)
            skipPendingIntent = it.getParcelable(Constants.BUNDLE_SIGNIN_SKIP_PENDING_INTENT)
            referrer = it.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
            referrerViewFPV = it.getBoolean(Constants.BUNDLE_REFERRER_VIEW_IS_FVP, true)
            delaySignInPageViewEvent = it.getBoolean(Constants.BUNDLE_SIGN_IN_DELAY_PAGE_VIEW, false)
            isAccountLinkingNeeded = it.getBoolean(Constants.BUNDLE_LINK_ACCOUNTS_POST_LOGIN, false)
        }

        if (!delaySignInPageViewEvent) {
            fireSignInPageView()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_sign_on, container, false)
        if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(),
                        NewsConstants.URDU_LANGUAGE_CODE)) {
            ViewCompat.setLayoutDirection(rootView, ViewCompat
                    .LAYOUT_DIRECTION_RTL)
        }
        this.container = rootView as ConstraintLayout
        phoneNumberLogin = rootView.findViewById(R.id.phone_number_login)
        facebookLogin = rootView.findViewById(R.id.facebook_login)
        googleLogin = rootView.findViewById(R.id.google_login)
        phoneNumberLogin.setOnClickListener(this)
        facebookLogin.setOnClickListener(this)
        googleLogin.setOnClickListener(this)
        (phoneNumberLogin.background as? GradientDrawable)
                ?.setColor(CommonUtils.getColor(R.color.follow_color))
        (facebookLogin.background as? GradientDrawable)
                ?.setColor(CommonUtils.getColor(R.color.button_fb_login_fgcolor))
        (googleLogin.background as? GradientDrawable)
                ?.setColor(CommonUtils.getColor(R.color.google_login_bg_color))

        userProfilesList = rootView.findViewById<RecyclerView>(R.id.user_profiles_list)
        userProfilesTotalCount = rootView.findViewById(R.id.user_profiles_total_count)

        rlProgressBg = rootView.findViewById(R.id.rl_signon_progress_bar)
        rlContainerParent = rootView.findViewById<View>(R.id.rl_container_parent) as ConstraintLayout
        tvProgressText = rootView.findViewById(R.id.tv_progress_bar)
        skipButton = rootView.findViewById(R.id.skip_button)
        signUpHeader = rootView.findViewById(R.id.sign_up_header)
        signUpSubHeader = rootView.findViewById(R.id.sign_up_subheader)
        signUpHeaderIcon = rootView.findViewById(R.id.sign_up_header_icon)
        skipButtonGroup = rootView.findViewById(R.id.skip_button_group)
        skipButtonTop = rootView.findViewById(R.id.skipSignIn)
        skipButtonTop.setOnClickListener(this)
        signInCrossButton = rootView.findViewById(R.id.sign_in_cross_button)
        signInCrossButton.setOnClickListener(this)
        skipButtonGroup.setOnClickListener(this)
        userProfilesGroup = rootView.findViewById(R.id.user_profiles_group)
        termsView = rootView.findViewById(R.id.termsView)
        errorParent = rootView.findViewById(R.id.error_parent)
        val originalHtmlString = CommonUtils.getString(R.string.login_terms_condition)
        val htmlString = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(originalHtmlString), HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
        termsView.setSpannableText(htmlString, originalHtmlString)
        termsView.setMovementMethod(LinkMovementMethod.getInstance())


        val disableTrueCallerLogin = PreferenceManager.getPreference(GenericAppStatePreference
                .DISABLE_TRUE_CALLER_LOGIN, false)
        if (disableTrueCallerLogin) {
            phoneNumberLogin.visibility = View.GONE
        }

        val drawable = userProfilesTotalCount.background as? GradientDrawable
        drawable?.setStroke(CommonUtils.getDimension(R.dimen.divider_height), ThemeUtils
                .getThemeColorByAttribute(rootView.context, R.attr.followed_entities_background_color))

        setUIAccordingToMode(uiMode)
        handleAutoLogin()

        return rootView
    }

    private fun handleAutoLogin() {
        if (autoLogin) {
            when (loginType) {
                LoginType.FACEBOOK -> {
                    googleLogin.visibility = View.GONE
                    phoneNumberLogin.visibility = View.GONE
                }
                LoginType.GOOGLE -> {
                    facebookLogin.visibility = View.GONE
                    phoneNumberLogin.visibility = View.GONE
                }

                LoginType.MOBILE -> {
                    googleLogin.visibility = View.GONE
                    facebookLogin.visibility = View.GONE
                }
                else -> {
                }

            }
        }
    }


    override fun onStart() {
        super.onStart()

        val manager = LinearLayoutManager(activity)
        manager.orientation = RecyclerView.HORIZONTAL
        userProfilesList.layoutManager = manager
        if (userProfilesList.adapter == null)
            userProfilesList.addItemDecoration(ProfileListItemDecorator())
        fetchUserProfilesList()
        if (::signOnPresenter.isInitialized.not()) {
            signOnPresenter = SignOnPresenter(this, loginType, retryLogin, id, autoLogin)
        }
        signOnPresenter.start()

    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        if (::signOnPresenter.isInitialized) {
            signOnPresenter.destroy()
        }
        super.onDestroy()
    }

    private fun setUIAccordingToMode(mode: String?) {
        val uiMode = if (mode == null) SignInUIModes.SIGN_IN else SignInUIModes.valueOf(mode)
        when (uiMode) {
            SignInUIModes.SIGN_IN -> {
                signUpHeader.text = CommonUtils.getString(R.string.sign_up_header_text_default)
            }
            SignInUIModes.SIGN_IN_WITH_SKIP_BUTTON -> {
                signUpHeader.text = CommonUtils.getString(R.string.sign_up_header_text_default)
                skipButtonGroup.visibility = View.VISIBLE
            }
            SignInUIModes.SIGN_IN_WITH_CROSS_BUTTON -> {
                signInCrossButton.visibility = View.VISIBLE
                signUpHeaderIcon.visibility = View.VISIBLE
                signUpHeader.text = customSignInHeader
                        ?: CommonUtils.getString(R.string.sign_up_header_text_default)
            }
            SignInUIModes.SIGN_IN_FOR_SOCIAL_ACTIVITIES -> {
                signUpHeader.text = CommonUtils.getString(R.string.sign_up_header_text_social_context)
            }
            SignInUIModes.SIGN_IN_FOR_TPV -> {
                signUpHeader.text = CommonUtils.getString(R.string.sign_up_header_text_tpv_view)
                signUpSubHeader.visibility = View.VISIBLE
                signUpHeaderIcon.visibility = View.VISIBLE
                signUpSubHeader.text = CommonUtils.getString(R.string.sign_up_subheader, tpvName)
                (signUpHeader.layoutParams as? ConstraintLayout.LayoutParams?)?.let {
                    it.topMargin = 0
                }
            }
            SignInUIModes.SIGN_IN_FOR_SOCIAL_ONBOARDING -> {
                signUpHeader.text = CommonUtils.getString(R.string.signup_header_new_boarding)
                signUpHeader.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
                val textColorId = CommonUtils.getResourceIdFromAttribute(activity, R.attr
                        .sign_on_header_textColor_onboarding)
                signUpHeader.setTextColor(CommonUtils.getColor(textColorId))
                skipButtonGroup.visibility = View.GONE
                skipButtonTop.visibility = if (arguments?.getBoolean(Constants.BUNDLE_FLAG_SHOW_SKIP, false) == true) View.VISIBLE else View.GONE
            }
        }

    }

    private fun moveListToNextPosition(position: Int) {
        if (position >= userImageList?.size ?: 0 - 5) {
            return
        }
        userProfilesList.smoothScrollBy(CommonUtils.getDimension(R.dimen.user_profile_image_w_move), 0)
        handler.postDelayed({ moveListToNextPosition(position + 1) }, DURATION_FOR_PROFILE_LIST_SCROLL)
    }

    private fun fetchUserProfilesList() {
        presenter.start()
    }

    override fun onClick(v: View) {
        val isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false)
        when (v) {
            facebookLogin -> {
                selectedLoginType = LoginType.FACEBOOK
            }
            googleLogin -> {
                if (!GoogleSignInHelper.arePlayServicesAvailable(activity)) {
                    GoogleSignInHelper.showPlayServiceUpdateDialog(activity)
                    return
                }
                selectedLoginType = LoginType.GOOGLE
            }
            phoneNumberLogin -> {
                selectedLoginType = LoginType.MOBILE
            }
            signInCrossButton -> {
                (activity as? SignOnFlow?)?.onSignOnDismissed()
            }
            skipButtonGroup -> {
                activity?.run {
                    var skipCounter: Int = PreferenceManager.getPreference(AppStatePreference
                            .SIGNIN_SKIP_COUNTER, 0)
                    PreferenceManager.savePreference(AppStatePreference.SIGNIN_SKIP_COUNTER, ++skipCounter)
                    (this as SignOnFlow?)?.onSkipSignOn(skipPendingIntent)
                }
            }
            skipButtonTop-> {
                getFragmentCommunicationViewModel()?.let {
                    val event = FragmentCommunicationEvent(hostId, ImportContactsEvents.SIGN_IN_SKIP)
                    it.fragmentCommunicationLiveData.value = event
                }
            }
        }

        selectedLoginType?.let {
            if (DailyhuntUtils.isRegisterOrFirstHandshakeDoneInThisVersion()) {
                if(isRegistered) {
                    SSOAnalyticsUtility.logSignInClick(it, referrer, referrerViewFPV ?: true)
                    signOnPresenter.login(it)
                }else{
                    Logger.d(TAG, " RETRY REGISTRATION ")
                    //retry register
                    BusProvider.getUIBusInstance().post(RetryResigtration())
                }
            } else {
                // No login should happen before handshake
              //  showToast(CommonUtils.getString(R.string.error_generic))
                //retry register
                BusProvider.getUIBusInstance().post(RetryResigtration())
            }
        }
    }

    @Subscribe
    fun onRegistrationUpdate(registrationUpdate: RegistrationUpdate) {
        if (RegistrationState.REGISTERED
                        .equals(registrationUpdate.getRegistrationState())) {
            Logger.e(TAG, "REGISTRATION SUCCESS")
            if (!SSO.getInstance().isLoggedIn(false) || CommonUtils.isEmpty(SSO.getInstance().userDetails?.userLoginResponse?.userId)) {
                hideError()
                selectedLoginType?.let {
                    SSOAnalyticsUtility.logSignInClick(it, referrer, referrerViewFPV ?: true)
                    signOnPresenter.login(it)
                }
            }
        } else {
            Logger.e(TAG, "REGISTRATION Fail")
            showErrorScreen()
        }
    }


     fun showErrorScreen() {
         Logger.e(TAG, "REGISTRATION FAILED SHOW FULL PAGE ERROR   ")
        errorParent.visibility = View.VISIBLE
        errorMessageBuilder = ErrorMessageBuilder(errorParent,requireContext(), object :
                ErrorMessageBuilder.ErrorMessageClickedListener {
            override fun onRetryClicked(view: View?) {
                //retry register
                BusProvider.getUIBusInstance().post(RetryResigtration())
            }

            override fun onNoContentClicked(view: View?) {
                // Do nothing
            }

        })
         val resId = ThemeUtils.getThemeDrawableByAttribute(this.context, R.attr.connection_error, View.NO_ID)
        errorMessageBuilder!!.showCustomError(BaseError(CommonUtils.getString(com.newshunt.dhutil.R.string
                .error_syncing)), true,resId)

    }

    private fun hideError() {
        errorMessageBuilder?.hideError()
        errorParent.visibility = View.GONE
        errorMessageBuilder = null
    }




    override fun showResponseForFetchProfileList(response: FetchUserProfilesResponse) {
        val userImageList = response.userImageList
        if (userImageList.isNullOrEmpty()) {
            hideProfileListRelatedViews()
            return
        }
        userProfilesGroup.visibility = View.VISIBLE
        userProfilesTotalCount.text = response.totalWeekCount
        this.userImageList = userImageList
        val adapter = DHProfilesAdapter(userImageList, activity!!)
        userProfilesList.adapter = adapter
        handler.postDelayed({ moveListToNextPosition(1) }, DURATION_FOR_PROFILE_LIST_SCROLL)
    }

    override fun fetchProfileListError() {
        hideProfileListRelatedViews()
    }

    private fun hideProfileListRelatedViews() {
        userProfilesGroup.visibility = View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        BusProvider.getUIBusInstance().register(this)
    }

    override fun onDetach() {
        BusProvider.getUIBusInstance().unregister(this)
        super.onDetach()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Logger.d(TAG, "onActivityResult of fragment with requestCode : $requestCode and " +
                "resultCode: $resultCode")

        when (requestCode) {
            REQ_CODE_LOGIN_RESULT -> handleTCLoginResult(data, resultCode)
            REQ_CODE_GOOGLE -> handleGoogleSignInResult(data)
            REQ_CODE_TRUECALLER -> {
            }
            else -> {
                //FB doesn't provide a specific requestCode. So we need to pass the requestCode
                // to the Facebook SDK to check whether it handled the requestCode or not.
                facebookHelper?.callbackFromActivity(requestCode, resultCode,
                        data)
            }
        }
    }

    private fun handleTCLoginResult(data: Intent?, resultCode: Int) {
        val isLoginSuccessful = data?.getBooleanExtra(Constants.BUNDLE_LOGIN_RESULT_SUCCESSFUL,
                false) ?: false
        val loginPayload = data?.getSerializableExtra(Constants.LOGIN_PAYLOAD) as? LoginPayload?

        if (resultCode == Activity.RESULT_CANCELED || !isLoginSuccessful || loginPayload == null) {
            showLoginView()
            showToast(CommonUtils.getString(R.string.sign_in_failed_message))
            signOnPresenter.onHelperLoginError(LoginType.MOBILE, SSOResult.UNEXPECTED_ERROR)
            (activity as? SignOnFlow?)?.onLoginFailed()
            return
        }
        showLoadingProgress(true, CommonUtils.getString(R.string.please_wait))
        signOnPresenter.socialLogin(loginPayload, LoginType.MOBILE)
    }

    override fun getViewContext(): Context {
        return context ?: CommonUtils.getApplication()
    }

    override fun showLoadingProgress(show: Boolean, text: String?) {
        if (activity?.isFinishing() == true) {
            return
        }

        rlProgressBg.setVisibility(if (show) View.VISIBLE else View.GONE)
        if (!CommonUtils.isEmpty(text) && show) {
            tvProgressText.setText(text)
            tvProgressText.setVisibility(View.VISIBLE)
        }
    }

    override fun showSignOnView(show: Boolean) {
        if (activity?.isFinishing == true) {
            return
        }
        rlContainerParent.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showToast(message: String?) {
        if (activity?.isFinishing == true || message == null) {
            return
        }
        activity?.let {
            FontHelper.showCustomFontToast(it, message, Snackbar.LENGTH_LONG)
        }
    }

    override fun showLoginSuccessMessage(name: String?) {
        if (activity?.isFinishing == true) {
            return
        }
        val success = getString(R.string.successfully_logged_in) + Constants.SPACE_STRING
        showToast(success + name)
        launchDeepLinkUrl()
    }

    private fun launchDeepLinkUrl() {
        if (!CommonUtils.isEmpty(deepLinkUrl)) {
            if (DeeplinkHelper.isInternalDeeplinkUrl(deepLinkUrl)) {
                CommonNavigator.launchDeeplink(this.context, deepLinkUrl, null)
            } else {
                NHBrowserUtil.handleBrowserSelection(activity, deepLinkUrl, browserType, null,
                        useWideViewPort, clearHistoryOnPageLoad)
            }
        }
    }

    override fun showUnexpectedError() {
        if (activity?.isFinishing == true) {
            return
        }
        val error = getString(R.string.unexpected_error_message)
        showToast(error)
    }

    private fun showLoginView() {
        rlProgressBg.visibility = View.GONE
        rlContainerParent.visibility = View.VISIBLE
    }

    override fun onLoginFailed(loginType: LoginType, failureResult: SSOResult?) {

        if (loginType == LoginType.GOOGLE) {
            val googleSignInHelper = GoogleSignInHelper(null, this.viewContext)
            googleSignInHelper.logout()
        }

        showLoginView()
        when (loginType) {
            LoginType.FACEBOOK, LoginType.GOOGLE, LoginType.MOBILE ->
                SSOAnalyticsUtility.logSignInFailure(loginType, failureResult, PageReferrer(SSOReferrer
                        .SIGN_IN_CLICK))
            else -> {
            }
        }
    }

    override fun onLoginSuccessful(name: String?, loginType: LoginType, loginResponse: UserLoginResponse?) {
        when (loginType) {
            LoginType.FACEBOOK, LoginType.GOOGLE, LoginType.MOBILE -> {
                SSOAnalyticsUtility.logSignInSuccessful(loginType, PageReferrer(SSOReferrer
                        .SIGN_IN_CLICK))
                if(isAccountLinkingNeeded && loginResponse?.linkedAccounts.isNullOrEmpty().not()) {
                    Logger.d(TAG, "Account linking needed, lets launch linking")
                    //Account linking is needed, pass on the pending intent further
                    CommonNavigator.launchAccountLinkActivity(activity,
                            loginResponse?.linkedAccounts,
                            null,
                            true,
                            successPendingIntent,
                            PageReferrer(NhGenericReferrer.SIGNIN_VIEW))
                    (activity as? SignOnFlow?)?.onLoginSuccess(null)
                } else {
                    (activity as? SignOnFlow?)?.onLoginSuccess(successPendingIntent)
                }
            }
            else -> {
            }
        }
    }


    override fun phoneNumberLogin() {
        val intent = Intent(activity, TrueCallerVerificationDialogActivity::class.java)
        startActivityForResult(intent, REQ_CODE_LOGIN_RESULT)
    }

    //Google Login related callbacks.
    override fun googleLogin() {
        if (activity?.isFinishing == true) {
            return
        }

        try {
            googleSignInHelper = GoogleSignInHelper(this)
            googleSignInHelper?.login()
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    override fun onGoogleLoginSuccess(token: String, userId: String) {
        val loginPayload = LoginPayload(CustomHashGenerator.getHash(ClientInfoHelper.getClientId())!!,
                AuthType.GOOGLE.name, token, UserExplicit.YES.value)
        signOnPresenter.socialLogin(loginPayload, LoginType.GOOGLE)
    }

    override fun onGoogleLoginError(ssoResult: SSOResult?) {
        if (ssoResult == null) {
            return
        }
        signOnPresenter.onClientLoginError(LoginType.GOOGLE, ssoResult)
    }

    override fun onGoogleLoginFailed() {
        signOnPresenter.onHelperLoginError(LoginType.GOOGLE, SSOResult.LOGIN_INVALID)
    }

    override fun onGoogleLoginCancelled() {
        signOnPresenter.onHelperLoginError(LoginType.GOOGLE, SSOResult.CANCELLED)
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        //Check if the response is success.
        if (googleSignInHelper != null) {
            googleSignInHelper?.handleSignInResult(data)
        } else {
            onGoogleLoginError(SSOResult.UNEXPECTED_ERROR)
        }
    }


    //Facebook Login related Callbacks.
    override fun facebookLogin() {
        if (activity?.isFinishing == true) {
            return
        }
        facebookHelper = FacebookHelper(this)
        facebookHelper?.login(this)
    }

    override fun onFacebookLogin(token: String, userId: String) {
        val loginPayload = LoginPayload(CustomHashGenerator.getHash(ClientInfoHelper.getClientId())!!,
                AuthType.FACEBOOK.name, token, UserExplicit.YES.value)
        signOnPresenter.socialLogin(loginPayload, LoginType.FACEBOOK)
    }

    override fun onFacebookLoginError() {
        signOnPresenter.onHelperLoginError(LoginType.FACEBOOK, SSOResult.UNEXPECTED_ERROR)
    }

    override fun onFacebookLoginCancelled() {
        signOnPresenter.onHelperLoginError(LoginType.FACEBOOK, SSOResult.CANCELLED)
    }

    override fun onFacebookLoginFailed(errorMessage: String) {
        if (!CommonUtils.isEmpty(errorMessage)) {
            showToast(errorMessage)
        }
        signOnPresenter.onHelperLoginError(LoginType.FACEBOOK, SSOResult.LOGIN_INVALID)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (delaySignInPageViewEvent && isVisibleToUser) {
            fireSignInPageView()
        }
    }

    override fun isAccountLinkingFlow(): Boolean {
        return isAccountLinkingNeeded
    }

    private fun fireSignInPageView() {
        if (!isSignInPageViewFired) {
            SSOAnalyticsUtility.logSigninPageView(referrer, referrerViewFPV ?: true)
            isSignInPageViewFired = true
        }
    }
    private fun getFragmentCommunicationViewModel(): FragmentCommunicationsViewModel? {
        return activity?.run {
            ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
        }
    }
}

interface SignOnFlow {
    fun onLoginSuccess(pendingIntent: PendingIntent?) {}
    fun onLoginFailed() {}
    fun onSignOnDismissed() {}
    fun onSkipSignOn(pendingIntent: PendingIntent?) {}
}