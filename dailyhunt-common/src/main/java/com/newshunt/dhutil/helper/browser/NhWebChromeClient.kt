/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.helper.browser

import android.app.Activity
import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dataentity.dhutil.model.entity.BrowserType

/**
 * WebChromeClient to handle target=_blank attribute in webviews.
 * Landing Browser is configurable.
 *
 * @author raunak.yadav
 */
open class NhWebChromeClient : WebChromeClient() {

    override fun onCreateWindow(view: WebView, dialog: Boolean, userGesture: Boolean,
                                msg: Message): Boolean {
        val hrefMsg = view.handler.obtainMessage()
        view.requestFocusNodeHref(hrefMsg)

        val url = hrefMsg.data.getString("url")
        if (!CommonUtils.isEmpty(url)) {
            val browserType = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.defaultBrowser
            NHBrowserUtil.handleBrowserSelection(view.context as Activity, url,
                    BrowserType.fromName(browserType), null, true, true)
        } else {
            val newWebView = WebView(view.context)
            val transport = msg.obj as WebView.WebViewTransport
            transport.webView = newWebView
            msg.sendToTarget()
        }
        return true
    }
}