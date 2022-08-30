package com.newshunt.appview.common.ui.helper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import com.google.android.material.transition.MaterialContainerTransform
import com.newshunt.adengine.client.NativeAdInventoryManager
import com.newshunt.adengine.view.helper.PgiAdHandler
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsFragment
import com.newshunt.appview.common.helper.UserActionHelper
import com.newshunt.appview.common.ui.activity.ViewAllCommentsFragment
import com.newshunt.appview.common.ui.fragment.AddPageFragment
import com.newshunt.appview.common.ui.fragment.HomeFragment
import com.newshunt.appview.common.ui.fragment.HomeLoaderFragment
import com.newshunt.appview.common.ui.fragment.ImportContactsHomeFragment
import com.newshunt.appview.common.ui.fragment.MenuFragment
import com.newshunt.appview.common.ui.fragment.NewsCarouselFragment2
import com.newshunt.appview.common.ui.fragment.NewsDetailFragment2
import com.newshunt.appview.common.ui.fragment.NewsOtherPerspectiveFragment2
import com.newshunt.appview.common.ui.fragment.ReorderFragment
import com.newshunt.appview.common.ui.fragment.WalkThroughFragment
import com.newshunt.appview.common.video.ui.helper.VideoHelper
import com.newshunt.appview.common.viewmodel.handleShare
import com.newshunt.common.helper.common.ApplicationStatus
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.DoubleBackExitEvent
import com.newshunt.dataentity.news.model.internal.cache.NewsTabVisitInfoCache
import com.newshunt.dhutil.CacheProvider
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.VideoPlayerProvider
import com.newshunt.dhutil.helper.RateUsDialogHelper
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.coachmark.CoachMarksHelper
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.helper.KillProcessAlarmManager
import com.newshunt.helper.player.PlayerControlHelper
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.util.NewsApp
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.ExitSplashAdFragment
import com.newshunt.news.view.fragment.PhotoSlideDetailFragment
import com.newshunt.news.view.fragment.PhotoSlideFragment
import java.lang.ref.WeakReference

object NavigationHelper {

    const val LOG_TAG = "NavigationHelper"
    const val FRAGMENT_TRANSITION_NEEDED = "fragment_transition_needed"
    const val FRAGMENT_TRANSITION_TAG = "DHFragmentTransition"
    @JvmField
    val navigationLiveData = MutableLiveData<NavigationEvent>()

    val autoplayInDetail = MutableLiveData<Boolean>()


    @JvmStatic
    fun handleNavigationEvents(navigationEvent: NavigationEvent, activity: AppCompatActivity, fragmentHostId: Int) {
      Logger.d(LOG_TAG, "Handling the navigationEvent")
      navigationEvent.intent?.let { intent ->
        if (fragmentHostId == -1) {
          activity.startActivity(navigationEvent.intent)
          return
        }
        val action = intent.action
        var handled = true
        if (action != null) {
          Logger.d(LOG_TAG, "Handling the navigation event with action $action")
          var callback: VideoPlayerProvider? = null
          if (navigationEvent.callback != null && navigationEvent.callback.get() is VideoPlayerProvider) {
            callback = navigationEvent.callback.get() as VideoPlayerProvider
          }

          when (action) {
            Constants.NEWS_DETAIL_ACTION -> {
              var immersiveMode = callback?.isAutoImmersiveMode ?: false
              addFragment(NewsDetailFragment2.newInstance(intent, callback), "detail", activity.supportFragmentManager, fragmentHostId, true, isAutoClick = immersiveMode)
            }
            Constants.CAROUSEL_DETAIL_ACTION -> {
              addFragment(NewsCarouselFragment2.newInstance(intent,callback), "detailcarousel", activity.supportFragmentManager,fragmentHostId)
            }

            Constants.OP_DETAIL_ACTION -> {
              addFragment( NewsOtherPerspectiveFragment2.newInstance(intent,callback), "detailotherperspective", activity.supportFragmentManager, fragmentHostId)
            }

            Constants.VIEW_PHOTO_ACTION -> {
              addFragment(PhotoSlideDetailFragment.newInstance(intent), "photo_browser", activity.supportFragmentManager, fragmentHostId)
            }

            Constants.GALLERY_PHOTO_ACTION -> {
              val bundle = intent.extras
              val postId = bundle?.getString(Constants.BUNDLE_POST_ID)
              if (postId != null) {
                addFragment(PhotoSlideFragment.newInstance(bundle), "gallery_browser", activity.supportFragmentManager, fragmentHostId)
              }
            }

            NewsConstants.INTENT_ACTION_LAUNCH_ADD_PAGE -> {
              addFragment(AddPageFragment.newInstance(intent), "addPage", activity.supportFragmentManager, fragmentHostId)
            }
            Constants.REORDER_PAGE_OPEN_ACTION -> {
              addFragment(ReorderFragment.newInstance(intent), "reorder", activity.supportFragmentManager,fragmentHostId)
            }
            Constants.ALL_COMMENTS_ACTION -> {
              addFragment(ViewAllCommentsFragment.newInstance(intent), "reorder", activity.supportFragmentManager,fragmentHostId)
            }
            Constants.ENTITY_OPEN_ACTION -> {
              activity.startActivity(navigationEvent.intent)
            }
            DailyhuntConstants.NH_BROWSER_ACTION -> {
              activity.startActivity(navigationEvent.intent)
            }

            DHConstants.PROFILE_OPEN_ACTION -> {
              activity.startActivity(navigationEvent.intent)
            }

            Constants.INTENT_ACTION_LAUNCH_SEARCH -> {
              activity.startActivity(navigationEvent.intent)
            }

            Constants.INTENT_ACTIONS_LAUNCH_CREATE_POST -> {
              activity.startActivity(navigationEvent.intent)
            }

            DHConstants.GROUP_INVITATION_ACTION -> {
              activity.startActivity(navigationEvent.intent)
            }

            Constants.SHARE_POST_ACTION -> {
              handleSharePostAction(intent.extras, activity)
            }

            Constants.MENU_FRAGMENT_OPEN_ACTION -> {
              val args = Bundle(intent.extras).apply {
                putLong(Constants.BUNDLE_TARGET_NAVIGATION_ID, navigationEvent.targetId)
              }
              val fragment: MenuFragment = MenuFragment.createInstance(args)
                fragment.showBottomSheetFragment(activity.supportFragmentManager, "MenuFragment")
            }
            Constants.ONBOARDING_ACTIVITY_OPEN_ACTION -> {
              activity.startActivity(navigationEvent.intent)
            }
	        Constants.WALKTHROUGH_ACTION -> {
                val fragment = WalkThroughFragment()
                fragment.arguments = bundleOf(NewsConstants.EXTRA_SHOW_IMPORT_CONTACTS to intent.getBooleanExtra(NewsConstants.EXTRA_SHOW_IMPORT_CONTACTS, false))
		        addFragment(fragment, "walkThrough", activity.supportFragmentManager, fragmentHostId)
	        }
            Constants.IMPORT_CONTACTS_ACTIONS -> {
                val fragment = ImportContactsHomeFragment()
                fragment.arguments = intent.extras
                addFragment(fragment, "import_contacts", activity.supportFragmentManager, fragmentHostId)

            }
            Constants.HOME_LOADER_ACTION -> {
              val fragment = HomeLoaderFragment.newInstance(intent)
              addFragment(fragment, "home_loader", activity.supportFragmentManager, fragmentHostId)
            }
            Constants.EXIT_SPLASH_AD_ACTION -> {
              val fragment = ExitSplashAdFragment()
              fragment.arguments = intent.extras
                val transaction = activity.supportFragmentManager.beginTransaction()
                transaction.replace(fragmentHostId, fragment, "exit_splash_ad")
                    .commitAllowingStateLoss()
            }
            Constants.EXIT_SPLASH_AD_CLOSE_ACTION -> {
              finalAppCleanup(activity)
            }
            else ->  {
              handled = false
              Logger.d(LOG_TAG, "Unhandled navigation event")
            }
          }
        } else {
          handled = false
        }
        if (!handled) {
          activity.startActivity(navigationEvent.intent)
        }
      }
    }

    private fun addFragment(fragment: Fragment,
                            tag: String,
                            fragmentManager: FragmentManager,
                            fragmentHostId: Int,
                            animate: Boolean = false,
                            isAutoClick: Boolean = false) {
        val transaction = fragmentManager.beginTransaction()
        val topFragment = getFragmentAtTop(fragmentManager,fragmentHostId).first

      if (topFragment != null) {
        transaction.hide(topFragment)
      }
      VideoHelper.topFragmentId.value = (fragment as? BaseFragment)?.hashCode()
      var fragmentTransitionAdded = false
      if (topFragment is FragmentTransitionViewProviderHost) {
        topFragment.getFragmentTransitionViewProvider()?.let {
            val parentStoryId = fragment.arguments?.getString(Constants.PARENT_STORY_ID, Constants.EMPTY_STRING) ?: Constants.EMPTY_STRING
            val clickedStoryId = fragment.arguments?.getString(Constants.STORY_ID, Constants.EMPTY_STRING) ?: Constants.EMPTY_STRING
            fragmentTransitionAdded = if (parentStoryId.isNotBlank()) {
              Logger.d(FRAGMENT_TRANSITION_TAG, "Click on carousel item")
              initFragmentTransition(it, parentStoryId, fragment, transaction, clickedStoryId)
            } else {
              initFragmentTransition(it, clickedStoryId, fragment, transaction, clickedStoryId)
            }
        }
      }
      if (animate && !fragmentTransitionAdded) {
        if (isAutoClick) {
          transaction.setCustomAnimations(R.anim.enter_fade_in, R.anim.exit_fade_out, R.anim.enter_fade_in, R.anim.exit_fade_out)
        } else {
          transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
        }
      }
      transaction.add(fragmentHostId, fragment,tag).addToBackStack(tag).commit()
    }

    fun getFragmentAtTop(fragmentManager: FragmentManager, fragmentHostId:Int): Pair<Fragment?, String?> {
      val backStackCount = fragmentManager.backStackEntryCount
      return if (backStackCount > 0) {
        val entry = fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 1)
        val tag = entry.name
        Pair(fragmentManager.findFragmentByTag(tag),tag)
      } else {
        val fragment = fragmentManager.findFragmentById(fragmentHostId)
        Pair(fragment, null)
      }
    }

    private fun handleSharePostAction(args: Bundle? = null, activity: Activity) {
      args ?: return
      val post = args.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset ?: return
      val packageName = args.getString(Constants.BUNDLE_SHARE_PACKAGE_NAME)
      /*MENU CLICK DELEGATE METHOD SHOULD BE USED AS COMMON SHARE HANDLER FOR POST*/
      handleShare(commonAsset = post,
              packageName = packageName,
              activity = activity)
    }

    fun onBackPressed(activity: AppCompatActivity,
                      fragmentHostId: Int,
                      canRenderExitAd: (() -> Boolean)? = null) {
      if (activity.isFinishing) {
        return
      }
      val pair = getFragmentAtTop(activity.supportFragmentManager,fragmentHostId)
      if (pair.first is BaseFragment && (pair.first as? BaseFragment)!!.handleBackPress()) {
        return
      }

      val backStackCount = activity.supportFragmentManager.backStackEntryCount
      if (backStackCount > 0) {
        try {
          activity.supportFragmentManager.popBackStackImmediate()
        } catch (ex: Exception) {
          Logger.caughtException(ex)
        }
        VideoHelper.handleBackPressState.value = (pair.first as? BaseFragment)?.hashCode()
        //resumeFragmentAtTop(pair.second,activity.supportFragmentManager)
        return
      }

      // in case the base fragment is detail  then exit the activity
        if (activity.supportFragmentManager.findFragmentByTag("home") != null) {
            (pair.first as? HomeNavigation)?.let {
                //If HomeNavigation was not able to handle section change, follow the exit route
                if (!it.handleHomeNavigation()) {
                    if (canRenderExitAd?.invoke() == true) {
                        Logger.d(LOG_TAG, "Will render Exit-Splash ad.")
                        startSoftCleanupForApp()
                    } else {
                        startSoftCleanupForApp()
                        finalAppCleanup(activity)
                    }
                }
            } ?: run {
                startSoftCleanupForApp()
                finalAppCleanup(activity)
            }
        } else {
            // TODO handle the back URL changes in this case
            activity.finish()
        }
    }

    private fun startSoftCleanupForApp() {
        Logger.d(LOG_TAG, "startSoftCleanupForApp")
        UserActionHelper.userActionLiveData.value = NhAnalyticsUserAction.NORMAL_EXIT
        PgiAdHandler.destroy()
        NativeAdInventoryManager.deleteInventory()
        CacheProvider.closeAllCachedApiCache()

        //AppSectionsProvider
        AppSectionsProvider.reset()

        // For analytics
        PreferenceManager.savePreference(GenericAppStatePreference.APP_EXIT_STATUS,
            NhAnalyticsUserAction.NORMAL_EXIT.name)
        NhAnalyticsAppState.getInstance().action = NhAnalyticsUserAction.BACK
        PlayerControlHelper.isListMuteMode = true
        RateUsDialogHelper.resetStoryViewedCountPerSessionOnSessionClose()
        NewsApp.getNewsAppComponent().dislikeService().onExit()
        CoachMarksHelper.cleanUpValues();
        BusProvider.getUIBusInstance().post(DoubleBackExitEvent("news home"))
        NewsTabVisitInfoCache.getInstance().terminate()
        CommonUtils.setIsLanguageSelectedOnLanguageCard(false)
    }

    fun finalAppCleanup(activity: Activity) {
        Logger.d(LOG_TAG, "finalAppCleanup")
        // For analytics
        NewsAnalyticsHelper.logSessionEnd()
        NewsAnalyticsHelper.logOrganicAppExit()
        activity.finish()
        ApplicationStatus.setCanCleanRAM(true)
        KillProcessAlarmManager.onDoubleBackToExit()
    }

  private fun initFragmentTransition(fragmentTransitionViewProvider: FragmentTransitionViewProvider,
                                     parentStoryId: String,
                                     fragment: Fragment,
                                     transaction: FragmentTransaction,
                                     clickedStoryId: String): Boolean {
    var fragmentTransitionAdded = false
    fragmentTransitionViewProvider.getViewForAnimationByItemId(parentStoryId, clickedStoryId)?.let { itemView ->
        //Important to set the custom animations. We set this so the pop animation while coming back from the detail fragments after swiping some items.
        transaction.setCustomAnimations(R.anim.enter_fade_in, R.anim.exit_fade_out, R.anim.enter_fade_in, R.anim.exit_fade_out)
        fragment.arguments?.putBoolean(FRAGMENT_TRANSITION_NEEDED, true)
        fragmentTransitionAdded = true
        transaction.setReorderingAllowed(true)
        itemView.transitionName = clickedStoryId
        Logger.d(FRAGMENT_TRANSITION_TAG, "Found the clicked viewholder finally!!, itemId: $parentStoryId, transitionName: $clickedStoryId")
        transaction.addSharedElement(itemView, clickedStoryId)
        fragment.sharedElementEnterTransition = MaterialContainerTransform()
    } ?: kotlin.run {
      Logger.e(FRAGMENT_TRANSITION_TAG, "Could not find view holder for $parentStoryId")
    }
    return fragmentTransitionAdded
  }
}

data class NavigationEvent(val intent: Intent? = null, // intent to launch
                           val id: Int = 0, // unique id for the consumer
                           val timeStamp: Long = System.currentTimeMillis(),
                           val callback: WeakReference<Any>? = null,
                           val targetId: Long = 0L/*uniquely identifies the intented target for this event.Optional.*/)

/**
 * To be implemented by classes that build and post [NavigationEvent]s to [NavigationHelper.navigationLiveData]
 * @author satosh.dhanyamraju
 */
interface NavigationEventPublisher {
  fun setTargetId(uniqueId: Long)
}

interface FragmentTransitionViewProvider {
  fun getViewForAnimationByItemId(parentStoryId: String, childStoryId: String): View?
}

interface FragmentTransitionViewProviderHost {
    fun getFragmentTransitionViewProvider(): FragmentTransitionViewProvider?
}

interface HomeNavigation {
    fun isHomeSection() = false
    fun handleHomeNavigation(): Boolean = false
}