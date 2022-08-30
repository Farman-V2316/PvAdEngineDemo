package com.newshunt.news.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import com.google.gson.reflect.TypeToken
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsFragment
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.FollowFilter
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.FollowReferrer
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.viewmodel.UserFollowViewModel
import com.newshunt.dataentity.notification.FollowModel
import com.newshunt.dataentity.notification.FollowNavModel
import com.newshunt.deeplink.navigator.FollowSectionNavigator
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.getFormattedCountForLikesAndComments
import com.newshunt.sso.SSO

class UserFollowEntityFragment : BaseFragment(), FollowingFilterCallback {

  private var selectedFilter = ALL_FILTER
  private var followModel  = FollowModel.FOLLOWING
  private lateinit var userId: String
  private lateinit var filterTextView : NHImageView
  private val followFilters : List<FollowFilter>?
  private lateinit var userFollowViewModel: UserFollowViewModel
  private lateinit var followingCount: NHTextView
  private lateinit var section: String
  private var isFPV: Boolean = false
  private var model = FollowModel.FOLLOWING.name
  private var referrer: PageReferrer? = null
  private var referrerRaw: String? = null
  private var userName: String? = null

  init {
    val json = PreferenceManager.getPreference(AppStatePreference.FOLLOW_FILTERS, Constants.EMPTY_STRING)
    val type = object : TypeToken<List<FollowFilter>>(){}.type
    followFilters = JsonUtils.fromJson(json, type)
  }

  companion object {

    @JvmStatic
    fun newInstance(intent: Intent) : UserFollowEntityFragment {
      val fragment = UserFollowEntityFragment()
      fragment.arguments = intent.extras
      return fragment
    }

    const val ALL_FILTER = "all"
    const val ALL_DISPLAY = "All"
  }

  override fun onCreate(savedState: Bundle?) {
    super.onCreate(savedState)
    userName = arguments?.getString(Constants.BUNDLE_USER_NAME) ?: USER
    section = arguments?.getString(NewsConstants.DH_SECTION)?: PageSection.NEWS.section
    model = arguments?.getString(Constants.BUNDLE_FOLLOW_MODEL) ?: "FOLLOWING"
    referrer = arguments?.getSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
    referrerRaw = arguments?.getString(Constants.REFERRER_RAW)
    val followNavModel = arguments?.getSerializable(NewsConstants.BUNDLE_OPEN_FOLLOWED_ENTITY) as? FollowNavModel
    userId = followNavModel?.userId?:arguments?.getString(Constants.BUNDLE_USER_ID) ?: SSO.getInstance().userDetails?.userID ?: Constants.EMPTY_STRING
    isFPV = followNavModel?.isTPV?.not()?:arguments?.getBoolean(Constants.BUNDLE_IS_FPV) ?: false
    followModel = followNavModel?.model?:FollowModel.valueOf(model)
    selectedFilter = when(followModel) {
      FollowModel.FOLLOWERS -> "FOLLOWERS"
      FollowModel.BLOCKED -> "BLOCKED"
      else -> followNavModel?.subTabType?:ALL_FILTER
    }
    userFollowViewModel = ViewModelProviders.of(this).get(UserFollowViewModel::class.java)
    userFollowViewModel.insertPage(userId, selectedFilter, followModel, section)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.activity_followed_entities, container, false)
    view?.findViewById<ConstraintLayout>(R.id.layout_waiting)?.visibility = View.GONE
    filterTextView = view.findViewById(R.id.follow_filter_action)
    followingCount = view.findViewById(R.id.following_count)

    filterTextView.visibility = if (CommonUtils.isEmpty(followFilters) || followModel != FollowModel.FOLLOWING) View.GONE else View.VISIBLE
    filterTextView.setOnClickListener {
      val bottomSheetFragment = FollowedEntitiesFilterFragment(this, followFilters!!,
          selectedFilter)
      bottomSheetFragment.show(childFragmentManager, "followFilter")
    }

    view.findViewById<FrameLayout>(R.id.toolbar_back_button).setOnClickListener {
      if (NewsNavigator.shouldNavigateToHome(activity, referrer, false,referrerRaw)) {
        val backReferrer = PageReferrer(FollowReferrer.FOLLOWING_ALL)
        backReferrer.referrerAction = NhAnalyticsUserAction.BACK
        FollowSectionNavigator.navigateToFollowHome(activity, backReferrer)
      } else {
        activity?.onBackPressed()
      }
    }

    view.findViewById<NHTextView>(R.id.title).text = when (followModel) {
      FollowModel.FOLLOWERS -> CommonUtils.getString(R.string.followers)
      FollowModel.BLOCKED -> CommonUtils.getString(R.string.blocked)
      else -> CommonUtils.getString(R.string.following)
    }

    addCardsFragments()
    return view
  }

  override fun handleBackPress(): Boolean {
    if (NewsNavigator.shouldNavigateToHome(activity, referrer, true,referrerRaw)) {
      val backReferrer = PageReferrer(FollowReferrer.FOLLOWING_ALL)
      backReferrer.referrerAction = NhAnalyticsUserAction.BACK
      FollowSectionNavigator.navigateToFollowHome(activity, backReferrer)
    }
    return false
  }

  override fun onFollowingFilterSelected(filter: FollowFilter) {
    selectedFilter = filter.value
    updateFilter()
  }

  private fun updateFilter() {
    userFollowViewModel.insertPage(userId, selectedFilter, followModel,section)
    addCardsFragments()
  }

  private fun addCardsFragments() {
      childFragmentManager.beginTransaction().replace(R.id.follow_fragment_view, CardsFragment.create(createBundle(),
          bindErrorFunction = this::bindErrorData)).commit()
  }

  private fun createBundle() : Bundle {
    val bundle = bundleOf(
        Constants.PAGE_ID to (userId+"_"+selectedFilter), Constants.BUNDLE_USER_ID to userId,
        Constants.BUNDLE_FILTER to selectedFilter,
        Constants.BUNDLE_IS_FPV to isFPV,
        Constants.LIST_TYPE to Format.ENTITY.name,
        Constants.BUNDLE_ACTIVITY_REFERRER_FLOW to referrer,
        NewsConstants.DH_SECTION to section,
        Constants.BUNDLE_FOLLOW_MODEL to followModel.name,
        Constants.BUNDLE_ERROR_LAYOUT_ID to getErrorLayoutId(),
        Constants.BUNDLE_DELAY_SHOWING_FPE to true,
        Constants.BUNDLE_ADDITIONAL_LOGTAG to "UserFollow"
    )

    if (followModel == FollowModel.FOLLOWERS) {
      bundle.putBoolean(Constants.BUNDLE_SHOW_GUEST_FOOTER, true)
    }
    return bundle
  }

  private fun getDisplayText(filter : String) : String {
    followFilters?.forEach {
      if (CommonUtils.equalsIgnoreCase(filter, it.value)) {
        return it.displayText
      }
    }
    return ALL_DISPLAY
  }

  private fun getErrorLayoutId() : Int {
    return R.layout.no_following_error
  }

  override fun logEntityListViewEvent() {
    AnalyticsHelper2.logEntityListView(selectedFilter, model, isFPV, referrer, PageSection.FOLLOW.section)
  }

  fun bindErrorData(binding: ViewDataBinding) {
    binding.setVariable(BR.isFPV, isFPV)
    binding.setVariable(BR.followModel, followModel)
    binding.setVariable(BR.userName, userName)
    binding.setVariable(BR.filter, selectedFilter)
    binding.executePendingBindings()
  }

  fun updateCount(count: Long) {
    if(count == 0L) {
      followingCount.visibility = View.GONE
    } else {
      followingCount.visibility = View.VISIBLE
      followingCount.text = getFormattedCountForLikesAndComments(count)
    }
  }
}

interface FollowingFilterCallback {
  fun onFollowingFilterSelected(filter: FollowFilter)

  fun logEntityListViewEvent()
}

const val TOPICS = "topics"
const val LOCATIONS = "locations"
const val PROFILES = "profiles"
const val USER = "User"

@BindingAdapter(value = ["bind:entityImage"], requireAll = true)
fun bindEntityImage(view: ImageView, followModel: FollowModel) {

    when(followModel) {
      FollowModel.FOLLOWERS -> view.setImageResource(CommonUtils.getResourceIdFromAttribute(view.context, R.attr.no_followers))
      FollowModel.FOLLOWING -> view.setImageResource(CommonUtils.getResourceIdFromAttribute(view.context, R.attr.not_following))
      FollowModel.BLOCKED -> view.setImageResource(CommonUtils.getResourceIdFromAttribute(view.context, R.attr.no_blocked_items))
    }
}

@BindingAdapter(value = ["bind:entityTitle", "bind:isFPV","bind:userName","bind:filter"], requireAll = true)
fun bindEntityTitle(view: NHTextView, followModel: FollowModel, isFPV: Boolean, userName: String, filter: String) {

    when(followModel) {
      FollowModel.FOLLOWERS -> {
        val text = if (isFPV) CommonUtils.getString(R.string.followers_error_title) else CommonUtils.getString(R.string.followers_error_title_tpv, userName)
        view.text = text
      }
      FollowModel.FOLLOWING -> {
        val text = getString(isFPV, filter, userName)
        view.text = text
      }
      FollowModel.BLOCKED -> view.text = CommonUtils.getString(R.string.blocked_error_title)
    }
}

@BindingAdapter(value = ["bind:entityDescription", "bind:isFPV"], requireAll = true)
fun bindEntityDescription(view: NHTextView, followModel: FollowModel, isFPV: Boolean) {

    when(followModel) {
      FollowModel.FOLLOWERS -> {
        if (isFPV) view.text = CommonUtils.getString(R.string.followers_error_description) else view.visibility = View.GONE
      }
      FollowModel.FOLLOWING -> {
        if (isFPV) view.text = CommonUtils.getString(R.string.following_error_description) else view.visibility = View.GONE
      }
      FollowModel.BLOCKED -> {
        view.text = CommonUtils.getString(R.string.blocked_error_description)
      }
    }
}

fun getString(isFPV: Boolean, filter: String, userName: String) : String {
	return if (isFPV) {
		when (filter) {
			TOPICS -> CommonUtils.getString(R.string.fpv_error_following_hashtag)
			PROFILES -> CommonUtils.getString(R.string.fpv_error_following_profile)
			LOCATIONS -> CommonUtils.getString(R.string.fpv_error_following_location)
			else -> CommonUtils.getString(R.string.fpv_error_following_source)
		}
	} else {
		when(filter) {
			TOPICS -> CommonUtils.getString(R.string.tpv_error_following_hashtag, userName)
			PROFILES -> CommonUtils.getString(R.string.tpv_error_following_profile, userName)
			LOCATIONS -> CommonUtils.getString(R.string.tpv_error_following_location, userName)
			else -> CommonUtils.getString(R.string.tpv_error_following_source, userName)
		}
	}
}