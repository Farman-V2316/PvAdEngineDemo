/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.fragment.CardsViewModelProvider
import com.newshunt.common.helper.analytics.NhAnalyticsUtility
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.NHWebViewUtils
import com.newshunt.common.helper.common.NhWebViewClient
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NhWebView
import com.newshunt.common.view.dbgCode
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.TabEntity
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.helper.NHWebViewJSInterface
import com.newshunt.news.util.NewsConstants

/**
 * Created by karthik.r on 2019-11-30.
 */
class PostDetailLastPageFragment : BaseSupportFragment(), ErrorMessageBuilder.ErrorMessageClickedListener  {

    private val TAG = "PostDetailLastPageFragment"
    private lateinit var progressBar: View
    private lateinit var webView: NhWebView
    private var errorMessageBuilder: ErrorMessageBuilder? = null
    private lateinit var errorParent: LinearLayout
    private var storyDetailErrorPageUrl: String? = null
    private var tabEntity: TabEntity? = null
    private var landingAdapterPos: Int = 0
    private var pageReferrer: PageReferrer? = null
    private var baseError: BaseError? = null
    private var showingErrorScreen = false

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        val bundle = arguments
        if (bundle != null) {
            pageReferrer = bundle.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
            tabEntity = bundle.getSerializable(NewsConstants.TAB_ENTITY) as TabEntity?
            landingAdapterPos = bundle.getInt(NewsConstants.LANDING_ADAPTER_POSITION)
        }

        storyDetailErrorPageUrl = PreferenceManager.getPreference(
                GenericAppStatePreference.STORY_DETAIL_ERROR_PAGE_URL, Constants.EMPTY_STRING)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        // TODO : Add data binding support. Low priority.
        val view = inflater.inflate(R.layout.fragment_story_detail_error, container, false)
        progressBar = view.findViewById(R.id.progress_bar)
        errorParent = view.findViewById(R.id.error_parent)
        errorMessageBuilder = ErrorMessageBuilder(errorParent, activity!!, this, this)
        webView = view.findViewById<View>(R.id.story_detail_error_webview) as NhWebView
        NHWebViewUtils.initializeWebView(webView)
        webView.webViewClient = StoryDetailErrorWebClient(webView, errorParent, progressBar)
        val jsInterface = NHWebViewJSInterface(webView, activity, this, pageReferrer)
                .also { it.observeBus() }
        webView.addJavascriptInterface(jsInterface,
                NHWebViewJSInterface.INTERFACE_NAME)
        val defaultBackground = ThemeUtils.getThemeColorByAttribute(activity!!,
                R.attr.story_error_page_background)
        webView.setBackgroundColor(defaultBackground)
        view.findViewById<View>(R.id.actionbar_back_button_layout).setOnClickListener { this.backButtonClick() }
        return view
    }

    override fun onStart() {
        super.onStart()
        val parentFragment = parentFragment
        if (parentFragment is CardsViewModelProvider) {
            val cvmProvider = parentFragment as CardsViewModelProvider?
            cvmProvider?.getCardsViewModel()?.npUsecase?.data()?.observe(viewLifecycleOwner, Observer {
                nlRespResult ->
                        if (nlRespResult.isFailure) {
                            val originalError = nlRespResult.exceptionOrNull()
                            if (originalError is BaseError) {
                                showError(originalError)
                            }
                            else {
                                showError(BaseErrorBuilder.getBaseError(originalError, null, null, null))
                            }
                        }
                        else {
                            loadUrl()
                        }
                    })

            cvmProvider?.getCardsViewModel()?.npStatus?.observe(viewLifecycleOwner, Observer { isFetching ->
                if (isFetching == false && !showingErrorScreen) {
                    loadUrl()
                }
            })
        }
    }

    override fun onRetryClicked(view: View?) {
        errorParent.visibility = View.GONE
        progressBar.visibility = View.VISIBLE


        if (parentFragment is CardsViewModelProvider) {
            val cvmProvider = parentFragment as CardsViewModelProvider
            cvmProvider.getCardsViewModel().npUsecase.execute(Bundle())
        }
    }

    override fun onNoContentClicked(view: View?) {
        val errorListener = errorListener() ?: return
        errorListener.onNoContentClicked(view)
    }

    private fun loadUrl() {
        Logger.d(TAG, "loadUrl")
        progressBar.visibility = View.VISIBLE
        errorParent.visibility = View.GONE
        webView.visibility = View.GONE
        storyDetailErrorPageUrl?.let {
            webView.loadUrl(it)
        }
    }

    private fun showError(baseError: BaseError?) {
        if (baseError == null) {
            return
        }

        Logger.d(TAG, "showError:$baseError")
        this.baseError = baseError
        errorParent.visibility = View.VISIBLE
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        showingErrorScreen = true
        errorMessageBuilder?.showError(baseError)

        NewsAnalyticsHelper.logErrorScreenViewEvent(NhAnalyticsUtility.ErrorResponseCode.NETWORK_ERROR,
                NhAnalyticsUtility.ErrorViewType.FULLSCREEN, NhAnalyticsUtility.ErrorPageType.STORY_LIST,
                baseError.status.toString(), baseError.message, baseError.url,
                tabEntity, pageReferrer, landingAdapterPos, baseError.dbgCode())
    }


    private fun backButtonClick() {
        activity?.onBackPressed()
    }

    private fun errorListener(): ErrorMessageBuilder.ErrorMessageClickedListener? {
        if (activity == null) {
            return null
        }

        val activity = activity
        return if (activity is ErrorMessageBuilder.ErrorMessageClickedListener) {
            activity
        } else null
    }

}

class StoryDetailErrorWebClient(val webView: NhWebView,
                                val errorParent: View,
                                val progressBar: View) : NhWebViewClient() {

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        errorParent.visibility = View.VISIBLE
    }

    override fun onPageLoaded(view: WebView?, url: String?) {
        webView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        errorParent.visibility = View.GONE
    }
}
