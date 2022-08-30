/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

/**
 * @author madhuri.pa
 */

package com.newshunt.appview.common.ui.viewholder

import android.app.Activity
import android.content.Context
import android.webkit.JavascriptInterface
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Guideline
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.appview.R
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NhWebView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.WebCard2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.preference.AdjunctLangPreference
import com.newshunt.news.helper.NHJsInterfaceWithMenuClickHandling
import com.newshunt.news.helper.NHWebViewJSInterface
import com.newshunt.news.util.NewsConstants
import com.newshunt.onboarding.helper.AdjunctLanguageUtils
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder

class WebCardViewHolder(private val viewBinding: ViewDataBinding,
                        val vm: CardsViewModel,
                        val cardType: Int, val context: Context?, val pageReferrer: PageReferrer,
                        private val nhJsInterfaceWithMenuClickHandling:
                         NHJsInterfaceWithMenuClickHandling? = null
) : CardsViewHolder(viewBinding.root), VisibilityAwareViewHolder {

    var webCard2: WebCard2? = null
    val webView: NhWebView = viewBinding.root.findViewById(R.id.webview)
    private var isVisible = false
    lateinit var webItem: CommonAsset
    private val jsInterface = WebCardJSInterface2(webView, itemView.context as Activity,
            pageReferrer)

    init {
        jsInterface.menuJsInterface = nhJsInterfaceWithMenuClickHandling
        webView.addJavascriptInterface(jsInterface, NHWebViewJSInterface.INTERFACE_NAME)
    }


    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        if (item !is CommonAsset)
            return
        webItem = item
        webCard2 = webItem.i_webcard()
        setupView()
        if (CommonUtils.isEmpty(webItem.i_content())) {
            webItem.i_contentBaseUrl()?.let {
                webView.loadUrl(it)
            }
        } else {
            webItem.i_content()?.let {
                webView.loadDataWithBaseURL(
                    webItem.i_contentBaseUrl(), it,
                    NewsConstants.HTML_MIME_TYPE, NewsConstants.HTML_UTF_ENCODING, null
                )
            }
        }
        if(webItem.i_removePadding() != null && webItem.i_removePadding() == true) {
            val guideline = viewBinding.root.findViewById(R.id.guideline1) as Guideline
            guideline.setGuidelineBegin(0)
            val guideline2 = viewBinding.root.findViewById(R.id.guideline2) as Guideline
            guideline2.setGuidelineEnd(0)
        }
        nhJsInterfaceWithMenuClickHandling?.pageReferrer = pageReferrer

        jsInterface.updateData(webCard2, cardPosition)

        webView.webviewResume()

    }

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        val adjunctLang:String? = webItem.i_adjunctLang()
        if(viewVisibilityPercentage == 100 && percentageOfScreen>0 && webItem.i_subFormat() == SubFormat.WEB_ADJUNCT && !adjunctLang.isNullOrEmpty() && !AdjunctLanguageUtils.sameAdjunctLangsFromPreviousWebCard(adjunctLang)){
            AnalyticsHelper2.logAdjunctLangCardViewEvent(adjunctLang, Constants.ADJUNCT_LANGUAGE_HTML_BANNER)
            PreferenceManager.savePreference(AdjunctLangPreference.WEB_CARD_ADJUNCT_LANG,adjunctLang)
        }
    }

    override fun onInVisible() {
        if (isVisible) {
            webView.webviewPaused()
            isVisible = false
        }

    }

    override fun onUserLeftFragment() {
        if (isVisible) {
            webView.webviewPaused()
            isVisible = false
        }

    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {

        if (!isVisible && viewVisibilityPercentage > 0) {
            webView.webviewResume()
            isVisible = true
        }

        if (isVisible && viewVisibilityPercentage == 0) {
            webView.webviewPaused()
            isVisible = false
        }
    }


    private fun setupView() {
        if (viewBinding.root !is ConstraintLayout) return
        val cs = ConstraintSet()
        cs.clone(viewBinding.root as ConstraintLayout)
        cs.setDimensionRatio(webView.id, "H, ${webCard2?.aspectRatio ?: 0}:1")
        cs.applyTo(viewBinding.root as ConstraintLayout)
    }

}

private class WebCardJSInterface2(webView: NhWebView,
                                  activity: Activity,
                                  val referrer: PageReferrer) : NHWebViewJSInterface(webView, activity, referrer) {
    private var item: WebCard2? = null
    private var position: Int = -1
    fun updateData(asset: WebCard2?, position: Int) {
        item = asset
        this.position = position
    }

    @JavascriptInterface
    fun logCardClickEvent(eventParamsJson: String?) {
        val eventParams = if (eventParamsJson == null) {
            mutableMapOf<String, String>()
        } else {
            val type = object : TypeToken<MutableMap<String, String>>() {}.type
            try {
                Gson().fromJson(eventParamsJson, type) as MutableMap<String, String>
            } catch (e: Exception) {
                mutableMapOf<String, String>()
            }
        }
        //todo log web card click event madhuri.pa
        //logWebCardClickEvent(referrer, position, item, referrerProviderlistener, eventParams)
    }
}