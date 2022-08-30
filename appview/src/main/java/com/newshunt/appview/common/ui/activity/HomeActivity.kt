/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.facebook.appevents.AppEventsLogger
import com.newshunt.adengine.client.NativeAdInventoryManager
import com.newshunt.adengine.model.entity.version.AdUIType
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.helper.ExitSplashAdCommunication
import com.newshunt.appview.R
import com.newshunt.appview.common.di.DaggerHomeComponent
import com.newshunt.appview.common.ui.adapter.HomeTabsAdapter
import com.newshunt.appview.common.ui.fragment.*
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.helper.NavigationHelper.handleNavigationEvents
import com.newshunt.appview.common.ui.helper.SnackbarViewModel
import com.newshunt.appview.common.viewmodel.AdjunctLanguageViewModel
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.common.view.view.RelaunchableActivity
import com.newshunt.common.view.view.ViewLifecycleFragment
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.AdjunctLangResponse
import com.newshunt.dataentity.common.model.entity.SettingsChangeEvent
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.HomeNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.SharableAppDialogHelper
import com.newshunt.dhutil.helper.appsection.DefaultAppSectionsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.FollowBlockPrefUtil
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.DeeplinkableDetail
import com.newshunt.dhutil.view.SharableAppDialogActivity
import com.newshunt.dhutil.view.ThemeSettingFragment
import com.newshunt.helper.FastBlur
import com.newshunt.news.helper.ExitSplashAdHelper
import com.newshunt.news.helper.RateUsCheckHelperNews
import com.newshunt.news.model.helper.NotificationActionExecutionHelper.executePendingAction
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.activity.NewsBaseActivity
import com.newshunt.news.view.fragment.EntityInfoFragment
import com.newshunt.news.view.fragment.ExitSplashAdFragment
import com.newshunt.news.view.fragment.PhotoSlideDetailFragment
import com.newshunt.news.view.fragment.PhotoSlideFragment
import com.newshunt.onboarding.helper.AdjunctLanguageUtils
import com.newshunt.onboarding.view.activity.OnBoardingActivity
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import javax.inject.Inject

const val FRAGMENT_TAG_HOME = "home"
class HomeActivity : NewsBaseActivity(), RelaunchableActivity,
  FragmentManager.OnBackStackChangedListener {
  @Inject
  lateinit var snackbarViewModelFactory: SnackbarViewModel.Factory


  private val LOG_TAG = "HomeActivity"
  private var acceptableTimeStamp = System.currentTimeMillis()

  private var activityRecreated: Boolean = false//flag to denote that this activity is recreated after system
  // kill and recreation and ignore relaunch call from NHLifecyclecallback - on Activity Resumed,
  // and so this flag will be reset after super.onResume()
  private var forceLoadHomeTab: Boolean = false//flag to denote that activity must land to home tab after
  // system kill and recreate, because viewPager is able to remember the previous position (saved
  // during onSaveInstanceState
  private lateinit var currentSectionId: String
  private val relaunchWhenCurFragmentHome = AtomicBoolean(false)
  val isCurFragmentHome: Boolean
    get() {
      val topMostDHFragment = supportFragmentManager.fragments
              .reversed()
              .find { it is ViewLifecycleFragment }
      Logger.v(LOG_TAG, "isCurFragmentHome: top=${topMostDHFragment?.javaClass?.simpleName}")
      return topMostDHFragment is HomeFragment
    }
  private val containerId = R.id.dh_base_container_fragment
  private var createTime : Long = 0
  private var adjunctLangDisplayType = Constants.NOT_SHOW_ADJUNCT_LANG_DISPLAY
  private var adjunctLangResponse: AdjunctLangResponse?= null
  private lateinit var adjunctLangVm: AdjunctLanguageViewModel
  private val HANDLER = Handler(Looper.getMainLooper())
  private lateinit var adjunctVmFactory: AdjunctLanguageViewModel.AdjunctLanguageViewModelF
  private var appLang = UserPreferenceUtil.getUserNavigationLanguage()
  private var userLangs = UserPreferenceUtil.getUserLanguages()
  private var exitSplashAdHelper: ExitSplashAdHelper? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createTime = System.currentTimeMillis()
    NavigationHelper.navigationLiveData.observe(this, Observer {
      if (it.timeStamp < acceptableTimeStamp) {
        return@Observer
      } else {
        handleNavigationEvents(it, this, containerId)
      }
    })

    HomeNavigator.bottomBarLiveData.observe(this, Observer {
      if (it.timeStamp < acceptableTimeStamp) {
        return@Observer
      } else {
        handleSectionChange(it.intent)
      }
    })

    AppSettingsProvider.settingsChangedLiveData.observe(this, Observer {
      if (createTime > it.timeStamp) {
        return@Observer
      }
      intent.putExtra(Constants.APP_SECTION_ID, currentSectionId)
      when (it.changeType) {
        SettingsChangeEvent.ChangeType.CARD_STYLE -> {
          NativeAdInventoryManager.deleteInventory()
          recreate()
        }
        SettingsChangeEvent.ChangeType.APP_LANGUAGE -> {
          if (appLang != UserPreferenceUtil.getUserNavigationLanguage()) {
            appLang = UserPreferenceUtil.getUserNavigationLanguage()
            recreate()
          }
        }
        SettingsChangeEvent.ChangeType.LANGUAGES -> {
          if (userLangs != UserPreferenceUtil.getUserLanguages()) {
            userLangs = UserPreferenceUtil.getUserLanguages()
            recreate()
          }
        }
        else -> {}
      }
    })

    adjunctVmFactory = AdjunctLanguageViewModel.AdjunctLanguageViewModelF()
    adjunctLangVm = ViewModelProviders.of(this,adjunctVmFactory).get(AdjunctLanguageViewModel::class.java)

    adjunctLangVm.adjunctResponseLiveData.observe(this) {
      val data = it.getOrNull()
      if (it.isSuccess) {
        data?.let {
          adjunctLangResponse = it
        }
      }
    }
    adjunctLangVm.getAdjunctLanguageInfo()

    AdjunctLanguageUtils.adjunctLangLiveData.observe(this, Observer {
      if (PreferenceManager.getPreference(GenericAppStatePreference.SHOW_LANG_SB, false)) {
        HANDLER.postDelayed({
          showAdjunctSnackbar(it.adjunctLang, it.primaryLang,it.langFlow)
          PreferenceManager.savePreference(GenericAppStatePreference.SHOW_LANG_SB, false)
        },1500)
      }
    })

    val referrer = intent?.extras?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
    if (referrer == PageReferrer(NhGenericReferrer.THEME_CHANGE)) {
      overridePendingTransition(R.anim.slow_fade_in, R.anim.slow_fade_out)
    } else if (intent.flags and Intent.FLAG_ACTIVITY_NO_ANIMATION != 0) {
      overridePendingTransition(0, 0)
    }

    currentSectionId = intent.getStringExtra(Constants.APP_SECTION_ID) ?: DefaultAppSectionsProvider.DefaultAppSection.NEWS_SECTION.id

    exitSplashAdHelper = ExitSplashAdHelper(referrer)
    ExitSplashAdCommunication.exitSplashAdRequestLD.observe(this) {
      exitSplashAdHelper?.requestAd()
    }

    if(intent.getBooleanExtra(NewsConstants.IS_ADJUNCT_LANG_NEWS,false)) {
      adjunctLangDisplayType = intent.extras?.getInt(NewsConstants.ADJUNCT_POPUP_DISPLAY_TYPE)
        ?: Constants.NOT_SHOW_ADJUNCT_LANG_DISPLAY
    }

    if (savedInstanceState != null) {
      // activity is recreated - system restores view-state and fragments.
      // We are creating adapter again; it does not have reference to recreated fragments - it
      // creates new fragments;  We have to clear-out the recreated fragments.
      removeFragments()
    }

    //This event log is used by Ads product team to get demographic data(Age, gender, area etc.)
    try {
      AppEventsLogger.activateApp(CommonUtils.getApplication())
    } catch (e: Exception) {
      Logger.caughtException(e)
    }


    if ((!CommonNavigator.isDeeplinkReferrer(intent?.extras?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?) &&
                    !(intent?.action?.equals(Constants.NEWS_DETAIL_ACTION) == true)) &&
            CommonNavigator.launchSplashIfFirstLaunch(this)) {
      finish()
      return
    }

    setContentView(R.layout.layout_dh_base_activity)
    val baseFragment = getBaseFragment(intent)
    supportFragmentManager.beginTransaction().replace(
            containerId, baseFragment.first, baseFragment.second).commitAllowingStateLoss()


    activityRecreated = savedInstanceState != null
    forceLoadHomeTab = activityRecreated
    DaggerHomeComponent.create().inject(this)
    ViewModelProviders.of(this, snackbarViewModelFactory).get(SnackbarViewModel::class.java)
            .also {
              it.followChanges.observe(this, Observer { res->
                if(FollowBlockPrefUtil.isFollowBlockUpdateFromImplicitSignal()) {
                  FollowBlockPrefUtil.updateFollowBlockUpdateFromImplicitSignal(false)
                } else {
                  SnackbarViewModel.onFollowChangeEvent(res, findViewById(containerId))
                }
              })
              it.newPostChanges.observe(this, Observer {res ->
                SnackbarViewModel.onPostUploaded(res, findViewById(containerId), true, null, R.string.view_photo_in_lite_mode_message)
              })
              it.start()
            }
    supportFragmentManager.addOnBackStackChangedListener(this)
    executePendingAction(GenericAppStatePreference.APP_LAUNCH_ACTION)
  }

  override fun onStart() {
    super.onStart()
    acceptableTimeStamp = System.currentTimeMillis()
    if (!PreferenceManager.getPreference(AppStatePreference.HOME_LOADER_SHOWN, false) &&
      NavigationHelper.getFragmentAtTop(supportFragmentManager, containerId).first is HomeFragment) {
      NavigationHelper.navigationLiveData.value = NavigationEvent(Intent(Constants.HOME_LOADER_ACTION))
    }
    exitSplashAdHelper?.start()
  }

  private fun getBaseFragment(intent: Intent) : Pair<BaseFragment, String> {
    val action = intent.action
    return if (action != null) {
      when(action) {
        Constants.NEWS_DETAIL_ACTION -> Pair(NewsDetailFragment2.newInstance(intent, null), "detail")
        Constants.CAROUSEL_DETAIL_ACTION -> Pair(NewsCarouselFragment2.newInstance(intent,null ),"detailcarousel")
        Constants.GALLERY_PHOTO_ACTION -> Pair(PhotoSlideFragment.newInstance(intent), "detailgallery")
        Constants.VIEW_PHOTO_ACTION -> Pair(PhotoSlideDetailFragment.newInstance(intent), "detailgphoto")
        Constants.OP_DETAIL_ACTION -> Pair(NewsOtherPerspectiveFragment2.newInstance(intent,null ),"detailotherperspective")
        Constants.INTENT_ACTIONS_LAUNCH_FOLLOW_HOME -> Pair(FollowHomeFragment.newInstance(intent), FRAGMENT_TAG_HOME)
        Constants.ENTITY_OPEN_ACTION -> Pair(EntityInfoFragment.newInstance(intent), "entityInfo")
        NewsConstants.INTENT_ACTION_LAUNCH_ADD_PAGE -> Pair(AddPageFragment.newInstance(intent), "addPage")
        Constants.REORDER_PAGE_OPEN_ACTION -> Pair(ReorderFragment.newInstance(intent), "reorder")
        Constants.ALL_COMMENTS_ACTION -> Pair(ViewAllCommentsFragment.newInstance(intent), "allcomments")
        Constants.IMPORT_CONTACTS_ACTIONS -> Pair(ImportContactsHomeFragment.newInstance(intent), "import_contacts")
        Constants.EXIT_SPLASH_AD_ACTION -> Pair(ExitSplashAdFragment.newInstance(intent), "exit_splash_ad")
        else -> Pair(HomeFragment.newInstance(intent, VideoRequester(activityId)), FRAGMENT_TAG_HOME)
      }
    } else {
      Pair(HomeFragment.newInstance(intent, VideoRequester(activityId)), FRAGMENT_TAG_HOME)
    }
  }

  private fun handleSectionChange(intent: Intent) {
    currentSectionId = intent.getStringExtra(Constants.APP_SECTION_ID) ?: DefaultAppSectionsProvider.DefaultAppSection.NEWS_SECTION.id
    val action = intent.action
    if (action != null) {
    val fragmentPair : Pair<BaseFragment, String> =
      when (action) {
        Constants.INTENT_ACTIONS_LAUNCH_FOLLOW_HOME -> Pair(FollowHomeFragment.newInstance(intent), FRAGMENT_TAG_HOME)
        else -> Pair(HomeFragment.newInstance(intent, VideoRequester(activityId)), FRAGMENT_TAG_HOME)
      }
      supportFragmentManager.beginTransaction().replace(
          R.id.dh_base_container_fragment, fragmentPair.first, fragmentPair.second).commitAllowingStateLoss()
    }
    setIntent(intent)

  }

  override fun onDeviceThemeChanged() {
    var fragment = this.supportFragmentManager.fragments.lastOrNull()
    if(fragment is MenuFragment) {
      fragment = fragmentBelowMenuFragment()
    }
    if(fragment != null && fragment is DeeplinkableDetail && !fragment.deeplinkUrl().isNullOrEmpty()) {
      val themeSettingFragment = ThemeSettingFragment()
      val bundle = Bundle()
      bundle.putString(Constants.POST_DEEPLINK_FOR_THEME,fragment.deeplinkUrl())
      themeSettingFragment.arguments = bundle
      val transaction = supportFragmentManager.beginTransaction()
      transaction.setCustomAnimations(R.anim.enter_fade_in, R.anim.exit_fade_out, R.anim.enter_fade_in, R.anim.exit_fade_out)
      transaction.replace(R.id.dh_base_container_fragment,themeSettingFragment).addToBackStack(null).commitAllowingStateLoss()
    } else{
      super.onDeviceThemeChanged()
    }
  }

  private fun checkToShowLinkedInSharePrompt() {
    val selectedAppToShare = PreferenceManager.getPreference(AppStatePreference.SELECTED_APP_TO_SHARE,Constants.EMPTY_STRING)
    val showLinkedInShareDialog = PreferenceManager.getPreference(AppStatePreference.LINKEDIN_SHARE_ENABLED,false)
    val title = PreferenceManager.getPreference(AppStatePreference.LINKEDIN_SHARE_DIALOG_TITLE,Constants.EMPTY_STRING)
    val pkgName = PreferenceManager.getPreference(AppStatePreference.LINKEDIN_SHARE_PKG_NAME,Constants.EMPTY_STRING)
    val neverShowPromptForPkgs = PreferenceManager.getPreference(AppStatePreference.NEVER_SHOW_DEFAULT_SHARE_APP_PROMPT_PKG,Constants.EMPTY_STRING)
    val isEnglishPrimaryLanguage = AppUserPreferenceUtils.getUserPrimaryLanguage().equals(Constants.ENGLISH_LANGUAGE_CODE)
    if(showLinkedInShareDialog && isEnglishPrimaryLanguage && pkgName != null && AndroidUtils.isAppInstalled(pkgName) && !selectedAppToShare.equals(pkgName) && !title.isNullOrEmpty() && !neverShowPromptForPkgs.contains(pkgName) ) {
      val defaultShareAppIntent = Intent(this, SharableAppDialogActivity::class.java)
      defaultShareAppIntent.putExtra(Constants.SHARABLE_APP_PKG_NAME, pkgName)
      defaultShareAppIntent.putExtra(Constants.SHARABLE_APP_DIALOG_TITLE, title)
      intent.extras?.let { defaultShareAppIntent.putExtras(it) }
      this.startActivity(defaultShareAppIntent)
      SharableAppDialogHelper.setLinkedInShareShownTime(System.currentTimeMillis())
      SharableAppDialogHelper.setLinkedInShareShowCount(SharableAppDialogHelper.getLinkedInShareShowCount() + 1)
    }
  }

  private fun fragmentBelowMenuFragment(): Fragment? {
    val fragmentList: List<Fragment> = this.supportFragmentManager.fragments
    if(fragmentList.count() > 1) {
      return fragmentList[fragmentList.count() - 2]
    }
    return null
  }

  /**
   * removes fragment types created by NewsHomeTabsAdapter
   */
  private fun removeFragments() {
    try {
      val classes = HomeTabsAdapter.fragmentClasses()
      for (fragment in supportFragmentManager.fragments) {
        if (classes.contains(fragment.javaClass)) {
          supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
      }
    } catch (e: Throwable) {
      Logger.caughtException(e)
    }

  }

  fun showAdjunctSnackbar(adjunctLang: String, primaryLanguage: String,langFlow: String?) {
    var stringInAdjunct = adjunctLangResponse?.snackbarTextMap?.get(adjunctLang+Constants
      .COMMA_CHARACTER+adjunctLang)
    var stringInAppLanguage = adjunctLangResponse?.snackbarTextMap?.get(adjunctLang+Constants
      .COMMA_CHARACTER+primaryLanguage)
    if (stringInAdjunct != null) {
      val snackBarContainer = findViewById<CoordinatorLayout>(R.id.snackbarContainer)
      AdjunctLanguageUtils.showCustomLangSnackBar(snackBarContainer.context, snackBarContainer,
        stringInAdjunct, stringInAppLanguage, 5000, CommonUtils.getDrawable(R
          .drawable.ic_adjunct_lang_settings)) {
        AnalyticsHelper2.logAdjunctLangSnackbarSettingsClick(langFlow)
        val languageIntent = Intent(this, OnBoardingActivity::class.java)
        languageIntent.putExtra(Constants.BUNDLE_LAUNCHED_FROM_SETTINGS, true)
        languageIntent.putExtra(Constants.IS_FROM_ADJUNCT_CROSS, true)
        languageIntent.putExtra(Constants.ADJUNCT_LANG_FLOW,langFlow)
        startActivity(languageIntent)
      }
      AnalyticsHelper2.logAdjunctLangSnackbarViewEvent(adjunctLang,langFlow)
    }
  }

  override fun onBackPressed() {
    Logger.d(LOG_TAG, "Back pressed")
    if(adjunctLangDisplayType != Constants.NOT_SHOW_ADJUNCT_LANG_DISPLAY) {
      val langIntent = Intent(this,AdjunctLanguageDialogBoxActivity::class.java)
      intent.extras?.let { langIntent.putExtras(it) }
      startActivityForResult(langIntent,Constants.ADJUNCT_LANGUAGE_REQUEST_CODE)
    } else {
      NavigationHelper.onBackPressed(this, R.id.dh_base_container_fragment) {
        exitSplashAdHelper?.canRenderExitSplashAd()?.let { ad ->
          // Move to full screen mode and setup a blur bg.
          setupAdBackground(ad.displayType == AdUIType.MINI_SCREEN && !AdsUtil.isExternalPopUpAd(ad))
          true
        } ?: false
      }
    }
  }

  override fun relaunch(relaunchToHomeTab: Boolean) {
    if (isCurFragmentHome) {
      recreateAndRetainCurrentTab()
    }
    else relaunchWhenCurFragmentHome.set(true)
  }

  override fun canRelaunchInCurrentScreen(): Boolean {
    return supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_HOME) != null
  }

  private fun recreateAndRetainCurrentTab() {
    val extras = (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_HOME) as? HomeFragment)?.bundleForSoftRefresh()
    extras?.let {
      intent.putExtras(extras)
    }
    recreate()
  }

  override fun onBackStackChanged() {
    if (isCurFragmentHome && relaunchWhenCurFragmentHome.compareAndSet(true, false))
      recreateAndRetainCurrentTab()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if(requestCode == Constants.SHARE_REQUEST_CODE) {
      if(!RateUsCheckHelperNews.checkToShowRateUsOnShareOrOnNStoryShare() && SharableAppDialogHelper.canShowLinkedInShareDialog()) {
        checkToShowLinkedInSharePrompt()
      }

    } else if(requestCode == Constants.ADJUNCT_LANGUAGE_REQUEST_CODE) {
      adjunctLangDisplayType = Constants.NOT_SHOW_ADJUNCT_LANG_DISPLAY
      onBackPressed()
    }
  }

  override fun onStop() {
    super.onStop()
    exitSplashAdHelper?.stop()
  }

  override fun onDestroy() {
    super.onDestroy()
    HANDLER.removeCallbacksAndMessages(null)
    exitSplashAdHelper?.destroy()
    exitSplashAdHelper = null
  }

  private fun setupAdBackground(isMiniAd: Boolean) {
    if (isMiniAd) {
      ViewUtils.takeScreenShot(findViewById<View>(android.R.id.content).rootView,
        this,
        WeakReference(Consumer {
          val bgView = findViewById<View>(R.id.ad_bg_container)
          val blurBitmap = FastBlur.blur(it, 70, true)
          bgView.background = blurBitmap.toDrawable(resources)
          bgView.visibility = View.VISIBLE
          if (it != blurBitmap) {
            it.recycle()
          }
        }))
    }
    ViewUtils.enableFullScreen(this, isMiniAd)
  }

}