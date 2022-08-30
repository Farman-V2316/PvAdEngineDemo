package com.newshunt.appview.common.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.newshunt.adengine.view.helper.ExitSplashAdCommunication
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.analytics.entity.NhAnalyticsPVType
import com.newshunt.appview.R
import com.newshunt.appview.common.di.DaggerWebComponent
import com.newshunt.appview.common.di.WebModule
import com.newshunt.appview.common.viewmodel.WebFragmentViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.NHWebViewUtils
import com.newshunt.common.helper.common.NhWebViewClient
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.font.HtmlFontHelper
import com.newshunt.common.helper.share.NHShareView
import com.newshunt.common.helper.share.ShareUi
import com.newshunt.common.helper.share.ShareViewShowListener
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.view.customview.HorizontalSwipeWebView
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.TabClickEvent
import com.newshunt.dataentity.common.pages.EntityType
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.AstroTriggerAction
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.dhutil.view.view.BackgroundChangeListener
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.news.analytics.NhWebItemType
import com.newshunt.news.helper.AstroHelper
import com.newshunt.news.helper.NHJsInterfaceWithMenuClickHandling
import com.newshunt.news.helper.NHWebViewJSInterface
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.helper.RepostWebItemJsInterfaceClickHandler
import com.newshunt.news.helper.StoryShareUtil
import com.newshunt.news.helper.handler.NhCommandCallback
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.customview.SlidingTabLayout
import com.newshunt.news.view.fragment.ScrollTabHolderFragment
import com.newshunt.news.view.listener.AstroDateSelectedListener
import com.newshunt.news.view.listener.AstroSubscriptionView
import java.util.Calendar
import java.util.LinkedList
import java.util.Locale
import javax.inject.Inject

class WebFragment: ScrollTabHolderFragment() , SwipeRefreshLayout.OnRefreshListener,
	ErrorMessageBuilder.ErrorMessageClickedListener, BackgroundChangeListener, ShareViewShowListener,
	AstroSubscriptionView, AstroDateSelectedListener, NhCommandCallback {

	private lateinit var categoryWebItemContainer :  SwipeRefreshLayout
	private lateinit var errorParent: LinearLayout
	private lateinit var errorMessageBuilder: ErrorMessageBuilder
	private var webItemContent: HorizontalSwipeWebView? = null
	private lateinit var progressBar : ProgressBar
	private var refreshOffset: Int = 0

	private lateinit var vm : WebFragmentViewModel
	private val webHistoryItems = LinkedList<WebHistoryItem>()
	private var currentPageReferrer: PageReferrer? = null
	private var nhShareView: NHShareView? = null
	private var postEntity: PostEntity? = null

	private var pageEntity: PageEntity? = null
	private var position = 0
	private var isEventLogged = false
	private var startTime: Long = -1
	private var isNewsHome = false
	private var section: String = PageSection.NEWS.section
	private var slidingTabId: Int? = null
	private var createdAt: Long = Long.MAX_VALUE

	private lateinit var rl : RelativeLayout

	@Inject
	lateinit var webModelF : WebFragmentViewModel.Factory

	@Inject
	lateinit var nhJsInterfaceWithMenuClickHandling: NHJsInterfaceWithMenuClickHandling

	@Inject
	lateinit var repostWebItemJsInterfaceClickHandler: RepostWebItemJsInterfaceClickHandler

	private var contentUrl: String = Constants.EMPTY_STRING

	override fun onCreate(savedState: Bundle?) {
		super.onCreate(savedState)
		pageEntity = arguments?.getSerializable(NewsConstants.NEWS_PAGE_ENTITY) as? PageEntity
		position = arguments?.getInt(NewsConstants.BUNDLE_ADAPTER_POSITION)?:0
		isNewsHome = arguments?.getBoolean(NewsConstants.BUNDLE_IS_NEWS_HOME) ?: false
		currentPageReferrer = arguments?.getSerializable(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
		section = arguments?.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section
		slidingTabId = arguments?.getInt(NewsConstants.BUNDLE_SLIDING_TAB_ID)
		createdAt = System.currentTimeMillis()

		pageEntity?.let {
			DaggerWebComponent.builder().webModule(WebModule(
					it.id, it.contentRequestMethod ?: "GET", section
			)).build().inject(this)
			contentUrl = it.contentUrl?: Constants.EMPTY_STRING
		}
		vm = ViewModelProviders.of(this, webModelF).get(WebFragmentViewModel::class.java)
		nhJsInterfaceWithMenuClickHandling.pageReferrer = currentPageReferrer
		repostWebItemJsInterfaceClickHandler.pageReferrer = currentPageReferrer

		ExitSplashAdCommunication.requestExitSplash("web_${pageEntity?.id}")
	}

	override fun onStart() {
		super.onStart()

		if (super.getUserVisibleHint() && postEntity == null) {
			showProgress()
			vm.fetchData(contentUrl)
		}
	}

	override fun onStop() {
		super.onStop()
		webItemContent?.webviewPaused()
	}

	override fun setUserVisibleHint(isVisibleToUser: Boolean) {
		super.setUserVisibleHint(isVisibleToUser)
		if (view == null || isVisibleToUser && fragmentManager == null) {
			return
		}
		if (isVisibleToUser) {
			startTime = SystemClock.elapsedRealtime()
			showProgress()
			vm.fetchData(contentUrl)
			webItemContent?.webviewResume()
		} else {
			webItemContent?.webviewPaused()
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_news_category_web_item,
			container, false)
		categoryWebItemContainer = view.findViewById(R.id.category_webitem_container)
		categoryWebItemContainer.setOnRefreshListener(this)

		errorParent = view.findViewById(R.id.error_parent) as LinearLayout
		errorMessageBuilder = ErrorMessageBuilder(errorParent, activity!!, this, this)

		refreshOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f,
			resources.displayMetrics).toInt()

		webItemContent = view.findViewById(R.id.web_item_content) as HorizontalSwipeWebView
		NHWebViewUtils.initializeWebView(webItemContent!!)

		webItemContent?.webViewClient = NewsItemWebViewClient()
		val jsInterface = NHWebViewJSInterface(webItemContent, activity!!, this, getPageReferrerForJs())
		jsInterface.menuJsInterface = nhJsInterfaceWithMenuClickHandling
		jsInterface.observeBus()
		jsInterface.repostWebItemJsInterface = repostWebItemJsInterfaceClickHandler
		jsInterface.pageEntity = pageEntity
		webItemContent?.addJavascriptInterface(jsInterface, NHWebViewJSInterface.INTERFACE_NAME)
		webItemContent?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
			if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
				if (pageEntity?.enableWebHistory == true && (v as WebView).canGoBack()) {
					v.goBack()
					return@OnKeyListener true
				} else if (pageEntity?.enableWebHistory == false && !CommonUtils.isEmpty(webHistoryItems)) {
					webHistoryItems.removeLast()
					if (!CommonUtils.isEmpty(webHistoryItems)) {
						val webHistoryItem = webHistoryItems.last
						loadDataWithBaseUrl(webHistoryItem.apiUrl, webHistoryItem.data)
						return@OnKeyListener true
					}
				}
			}
			false
		})
		//SwipeRefreshLayout needs to know if the custom view has space to scroll vertically up,
		// else it will trigger pullRefresh on every upward swipe.
		categoryWebItemContainer.setOnChildScrollUpCallback { _, _ ->
			webItemContent?.canScrollVertically(-1) ?: true
		}
		webItemContent?.setBackgroundColor(Color.argb(1, 0, 0, 0))

		SlidingTabLayout.tabClickEventLiveData.postValue(null)
		SlidingTabLayout.tabClickEventLiveData.observe(viewLifecycleOwner) {
			handleTabEvent(it)
		}
		vm.webLiveData.observe(viewLifecycleOwner) {
			if (it.isSuccess && it.getOrNull()?.getOrNull(0) != null) {
				val list = it.getOrNull()
				list?.getOrNull(0)?.let { postEntity ->
					this.postEntity = postEntity
					showWebItem(postEntity)
				}
			} else {
				if (it.exceptionOrNull() != null) {
					showError(ApiResponseOperator.getError(it.exceptionOrNull()))
				} else {
					showError(BaseError(CommonUtils.getString(R.string.error_generic)))
				}
			}
		}
		return view
	}

	private fun handleTabEvent(tabClickEvent: TabClickEvent?) {
		tabClickEvent ?: return
		if (!isAdded || position != tabClickEvent.newTabPosition
			|| slidingTabId != null && slidingTabId != tabClickEvent.slidingTabId
			|| createdAt > tabClickEvent.createdAt) {
			return
		}
		if (webItemContent?.scrollY ?: 0 > 0) {
			webItemContent?.scrollTo(0, 0)
		} else {
			categoryWebItemContainer.post {
				categoryWebItemContainer.isRefreshing = true
				refreshData(false)
			}
			AnalyticsHelper2.logExploreButtonClickEvent(
				currentPageReferrer,
				NewsExploreButtonType.TAB_REFRESH, section)
		}
	}

	private fun getProgressbar() {
		progressBar = ProgressBar(context);
		val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup
				.LayoutParams.WRAP_CONTENT)
		params.addRule(RelativeLayout.CENTER_IN_PARENT)
		progressBar.layoutParams = params
		rl = view!!.findViewById(R.id.rl_container)
		rl?.addView(progressBar)
	}

	private fun getPageReferrerForJs() : PageReferrer? {
		return PageReferrer(NewsReferrer.TOPIC_WEB_ITEM, pageEntity?.id)
	}

	private fun loadDataWithBaseUrl(baseUrl: String?, content: String?) {
		if (CommonUtils.isEmpty(content)) {
			baseUrl?.let {
				webItemContent?.loadUrl(it)
			}
			return
		}
		webItemContent?.loadDataWithBaseURL(baseUrl, HtmlFontHelper.wrapDataWithHTML(content),
			NewsConstants.HTML_MIME_TYPE, NewsConstants.HTML_UTF_ENCODING, null)
	}

	override fun onRefresh() {
		refreshData(true)
	}

	override fun onRetryClicked(view: View?) {
		refreshData()
	}

	override fun onNoContentClicked(view: View?) {
		NewsNavigator.navigateToHeadlines(activity)
	}

	private fun initWebFragment() {
		setGenderToWebView()
		setDobToWebView()
		setSubscribeButtonStateToWebView()
	}

	private inner class NewsItemWebViewClient : NhWebViewClient() {
		override fun onPageLoaded(view: WebView, url: String) {
			initWebFragment()
		}
	}

	/**
	 * A call from client to Javascript to set the gender of the user.
	 */
	private fun setGenderToWebView() {
		val gender = AstroHelper.gender
		if (gender != null) {
			val script = NHWebViewUtils.formatScript(DailyhuntConstants.JS_CALLBACK_SET_GENDER, gender
				.gender)
			NHWebViewUtils.callJavaScriptFunctionWithReturnValue(webItemContent, script)
		}
	}

	/**
	 * A call from client to Javascript to set the date of Birth
	 */
	private fun setDobToWebView() {
		val calendar = AstroHelper.calendarFromSavedDate ?: return
		//Set the date
		val date = calendar.get(Calendar.DAY_OF_MONTH)
		//Set the year
		val year = calendar.get(Calendar.YEAR)
		//Set the month
		val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
		val script = NHWebViewUtils.formatScript(DailyhuntConstants.JS_CALLBACK_SET_DOB, Integer
			.toString(year), month, Integer.toString(date))
		NHWebViewUtils.callJavaScriptFunctionWithReturnValue(webItemContent, script)
	}

	/**
	 * A function from client to Javascript to tell if the subscription button can be enabled or not.
	 */
	private fun setSubscribeButtonStateToWebView() {
		if (!AstroHelper.canEnableSubscribeButton()) {
			return
		}
		val script = NHWebViewUtils.formatScript(DailyhuntConstants
			.JS_CALLBACK_SET_SUBSCRIBE_BUTTON_ENABLED, true)
		NHWebViewUtils.callJavaScriptFunctionWithReturnValue(webItemContent, script)
	}

	private fun refreshData(isPullDown: Boolean) {

		if (webItemContent?.visibility == View.VISIBLE
			&& isJsRefreshEnabled && isPullDown) {
			webItemContent?.jsRefresh()
		} else {
			refreshData()
		}
	}

	private fun refreshData() {
		hideError()
		if (!categoryWebItemContainer.isRefreshing) {
			showProgress()
		}
		isEventLogged = false
		vm.fetchData(contentUrl)
	}

	private fun hideError() {
		errorParent.visibility = View.GONE
		if (errorMessageBuilder.isErrorShown) {
			errorMessageBuilder.hideError()
		}
	}

	private fun showProgress() {
		if(!::progressBar.isInitialized)
			getProgressbar()
		progressBar.visibility = View.VISIBLE
	}

	private fun hideProgress() {
		progressBar.visibility = View.GONE
		categoryWebItemContainer.isRefreshing = false
	}

	private fun showError(error: BaseError) {
		hideProgress()
		showBaseError(error)
	}

	private fun showBaseError(error: BaseError) {
		errorParent.visibility = View.VISIBLE
		if (!errorMessageBuilder.isErrorShown) {
			errorMessageBuilder.showError(error)
		}
		webItemContent?.visibility = View.GONE
		// after the error is received start the refresh
		refreshData()
	}

	private fun showWebItem(postEntity: PostEntity) {
		hideProgress()
		if (webItemContent?.visibility != View.VISIBLE) {
			webItemContent?.visibility = View.VISIBLE
		}
		loadDataWithBaseUrl(postEntity.contentBaseUrl, postEntity.content)
		if (!isEventLogged) {
			logEvent()
		}
		if (userVisibleHint && nhShareView != null) {
			showShareView(nhShareView!!)
		}
	}

	override fun openWebItemResource(webItemResourceId: String) {
		// as this comes from webitem need to be run on Ui thread
		Handler(Looper.getMainLooper()).post {
			val url = pageEntity?.moreContentLoadUrl?.replace(NewsConstants.RESOURCE_ID_PLACEHOLDER, webItemResourceId)
			url?.let {
				contentUrl = url
				vm.fetchData(contentUrl)
			}
		}
	}

	private fun logEvent() {
		if (parentFragment is ReferrerProviderlistener) {
			val referrer = (parentFragment as ReferrerProviderlistener).providedReferrer
			val paramMap = getAnalyticsParamMap()
			isEventLogged = true

			if (activity?.intent != null) {
				val referrerRaw = activity?.intent?.getStringExtra(Constants.REFERRER_RAW)
				if (referrerRaw != null) {
					AnalyticsHelper2.appendReferrerRaw(paramMap, referrerRaw)
					paramMap[AnalyticsParam.REFERRER_RAW] = referrerRaw
				}
			}
			AnalyticsClient.log(getAnalyticsEvent(), NhAnalyticsEventSection.NEWS, paramMap, referrer)
		}
	}

	private fun getAnalyticsParamMap(): HashMap<NhAnalyticsEventParam, Any?> {
		val map = HashMap<NhAnalyticsEventParam, Any?>()
		map[NhAnalyticsNewsEventParam.TABTYPE] = pageEntity?.entityType ?: Constants.EMPTY_STRING
		map[NhAnalyticsNewsEventParam.TABITEM_ID] = pageEntity?.id ?: Constants.EMPTY_STRING
		map[NhAnalyticsNewsEventParam.TABINDEX] = position
		map[NhAnalyticsNewsEventParam.TABNAME] = pageEntity?.displayName?:Constants.EMPTY_STRING
		return map
	}

	private fun getAnalyticsEvent(): NhAnalyticsNewsEvent? {
		return if (PageType.SOURCE.pageType == pageEntity?.entityType) {
			NhAnalyticsNewsEvent.CATEGORY_WEB_ITEM
		} else NhAnalyticsNewsEvent.TOPIC_WEB_ITEM
	}

	override fun adjustScroll(scrollHeight: Int, headerTranslationY: Int) {
		categoryWebItemContainer.setPadding(categoryWebItemContainer.paddingLeft, scrollHeight,
			categoryWebItemContainer.paddingRight, categoryWebItemContainer.paddingBottom)
		categoryWebItemContainer.setProgressViewOffset(false, scrollHeight,
			scrollHeight + refreshOffset)
	}

	override fun logTimeSpentEvent(exitAction: NhAnalyticsUserAction) {
		val startTimespent = startTime
		if (startTimespent != -1L) {
			val referrerProviderlistener: ReferrerProviderlistener? =
				when {
					activity is ReferrerProviderlistener -> activity as ReferrerProviderlistener?
					parentFragment is ReferrerProviderlistener -> parentFragment as ReferrerProviderlistener?
					else -> null
				}
			val providedReferrer = referrerProviderlistener?.providedReferrer

			val pvActivity = getPvType()
			val listSection = NewsAnalyticsHelper.getReferrerEventSectionFrom(referrerProviderlistener)
			AnalyticsHelper2.logStoryListTimeSpentEvent(
				pageEntity = pageEntity,
				currentPageReferrer = currentPageReferrer,
				providedReferrer = providedReferrer,
				tabIndex = position,
				startTime = startTimespent,
				pvActivity =  pvActivity,
				exitAction = exitAction,
				section = listSection,
				sectionId = section
			)
			startTime = -1
		}
	}

	private fun getPvType(): NhAnalyticsPVType {
		return when (pageEntity?.entityType) {
			PageType.SOURCE.pageType -> NhAnalyticsPVType.CATEGORY_WEB_ITEM
			PageType.HASHTAG.pageType -> {
				if (isNewsHome) {
					NhAnalyticsPVType.WEB_ITEM
				} else NhAnalyticsPVType.TOPIC_WEB_ITEM
			}
			else -> NhAnalyticsPVType.WEB_ITEM
		}

	}

	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		logTimeSpentEvent(NhAnalyticsUserAction.CLICK)
	}

	override fun onPause() {
		super.onPause()
		logTimeSpentEvent(NhAnalyticsUserAction.IDLE)
	}

	override fun onResume() {
		super.onResume()
		startTime = SystemClock.elapsedRealtime()
		webItemContent?.webviewResume()
	}

	override fun onBackgroundColorChanged(colorCode: Int) {
		categoryWebItemContainer.setBackgroundColor(colorCode)
	}

	override fun showShareView(nhShareView: NHShareView) {
		if (postEntity?.shareParams?.shareTitle != null && pageEntity?.shareUrl != null) {
			nhShareView.visibility = View.VISIBLE
		} else {
			nhShareView.visibility = View.GONE
		}
		this.nhShareView = nhShareView
		this.nhShareView?.setShareListener(this)
	}

	override fun onShareViewClick(packageName: String?, shareUi: ShareUi?) {
		if (postEntity?.shareParams?.shareTitle == null || pageEntity?.shareUrl == null) {
			return
		}

		StoryShareUtil.shareNewsEntity(activity, pageEntity?.shareUrl,
			postEntity?.shareParams?.shareTitle,
			postEntity?.shareParams?.shareDescription,
			packageName, shareUi, getNhWebItemType().getType(), pageEntity?.id,
			postEntity?.experiment, currentPageReferrer)
	}

	override fun getIntentOnShareClicked(shareUi: ShareUi?): Intent {
		StoryShareUtil.logAnalytics(null, shareUi, getNhWebItemType().type,
			pageEntity?.id, postEntity?.experiment, currentPageReferrer)
		return StoryShareUtil.getWebShareIntent(pageEntity?.shareUrl, postEntity)
	}


	private fun getNhWebItemType(): NhWebItemType {
		return if (EntityType.SOURCE.name === pageEntity?.entityType)
			NhWebItemType.WEBITEM_CATEGORY
		else
			NhWebItemType.WEBITEM_TOPIC
	}

	override fun hideProgressBar() {
	}

	override fun showProgressBar() {
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		NHJsInterfaceWithMenuClickHandling.JS_MENU_ACTION.observe(viewLifecycleOwner, Observer {
			val command = NHJsInterfaceWithMenuClickHandling.bundleToCommand(it)
			if(command != null) NHWebViewUtils.callJavaScriptFunction(webItemContent, command)
		})
	}

	override fun onAstroCrossButtonClicked() {
		DialogAnalyticsHelper.deployAstroActionEvent(NhAnalyticsEventSection.NEWS, AstroTriggerAction
			.CROSS_DISMISS.triggerAction, DialogBoxType.ASTRO_ONBOARDING_FORM)
	}

	override fun onAstroEditButtonClicked() {
		DialogAnalyticsHelper.deployAstroViewedEvent(NhAnalyticsEventSection.NEWS,
			DialogBoxType.ASTRO_ONBOARDING_FORM)
	}

	override fun onAstroSubscriptionFailed(failureReason: String?) {
		activity?.let {
			FontHelper.showCustomFontToast(it, failureReason, Toast.LENGTH_LONG)
		}
		val script = NHWebViewUtils.formatScript(DailyhuntConstants
			.JS_CALLBACK_SET_USER_SUBSCRIBED_FAILED)
		NHWebViewUtils.callJavaScriptFunctionWithReturnValue(webItemContent, script)
	}

	override fun onAstroSubscriptionSuccess() {
		AstroHelper.handleAstroSuccessfulSubscription()
		AstroHelper.fireAstroSubscriptionEvent(DailyhuntConstants.ASTRO_FORM)
		refreshData()
	}

	override fun onDatePickerClicked() {
		AstroHelper.launchAndroidDatePicker(childFragmentManager, this)
	}

	override fun onGenderSelected(gender: String) {
		AstroHelper.saveGender(gender)
		setSubscribeButtonStateToWebView()
	}

	override fun onSubscriptionButtonClicked(gender: String, dob: String) {
		DialogAnalyticsHelper.deployAstroActionEvent(NhAnalyticsEventSection.NEWS, AstroTriggerAction
			.SUBSCRIBE.triggerAction, DialogBoxType.ASTRO_ONBOARDING_FORM)
		AstroHelper.doAstroPostRequest(this, gender, dob,
			pageEntity?.id?:Constants.EMPTY_STRING)
	}

	override fun onDateSet(calendar: Calendar) {
		AstroHelper.saveUserDateOfBirth(calendar)
		setDobToWebView()
		setSubscribeButtonStateToWebView()
	}
}

data class WebHistoryItem(val apiUrl: String, val baseUrl: String, val data: String)

