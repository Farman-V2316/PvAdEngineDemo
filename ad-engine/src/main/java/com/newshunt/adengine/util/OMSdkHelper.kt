/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import android.webkit.WebView
import com.iab.omid.library.versein.Omid
import com.iab.omid.library.versein.ScriptInjector
import com.iab.omid.library.versein.adsession.AdEvents
import com.iab.omid.library.versein.adsession.AdSession
import com.iab.omid.library.versein.adsession.AdSessionConfiguration
import com.iab.omid.library.versein.adsession.AdSessionContext
import com.iab.omid.library.versein.adsession.CreativeType
import com.iab.omid.library.versein.adsession.ImpressionType
import com.iab.omid.library.versein.adsession.Owner
import com.iab.omid.library.versein.adsession.Partner
import com.iab.omid.library.versein.adsession.VerificationScriptResource
import com.newshunt.adengine.client.HttpClientManager
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.omsdk.OMSessionState
import com.newshunt.adengine.model.entity.omsdk.OMTrackType
import com.newshunt.adengine.model.entity.omsdk.OMVendorInfo
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.OmSdkConfig
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dhutil.helper.preference.AdsPreference
import com.newshunt.sdk.network.Priority
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

/**
 * Helper to initialize and manage IAB's Open Measurement Sdk.
 * All methods must be called on the UI thread.
 *
 * @author raunak.yadav
 */
object OMSdkHelper {
    private const val TAG = "OMSdkHelper"
    var isOMSdkEnabled: Boolean = false
        private set
    private var initialized = false
    private var omidServiceJs: String? = Constants.EMPTY_STRING
    private var omidSessionClientJs: String? = Constants.EMPTY_STRING
    private var partner: Partner? = null

    init {
        isOMSdkEnabled = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.omSdkConfig?.isEnabled
                ?: false
    }

    /**
     * Activate the OM SDK.
     */
    fun init() {
        if (!isOMSdkEnabled || initialized) {
            return
        }
        AndroidUtils.getMainThreadHandler().post {
            try {
                Omid.activate(CommonUtils.getApplication())
                initialized = Omid.isActive()
            } catch (e: Exception) {
                AdLogger.e(TAG, "Failed to init OM SDK. ${e.message}")
            }

            if (!initialized) {
                AdLogger.e(TAG, "Failed to activate OM SDK.")
                return@post
            }
            // OM Partner
            if (partner == null) {
                initPartner()
            }
            //OM Service JS
            omidServiceJs = PreferenceManager.getPreference(AdsPreference.OMID_SERVICE_JS, Constants.EMPTY_STRING)
            // If service js is unavailable, we disable the sdk till next ads handshake.
            isOMSdkEnabled = !omidServiceJs.isNullOrBlank()
        }
    }

    private fun initPartner() {
        try {
            partner = Partner.createPartner(AdConstants.OM_PARTNER_NAME, DeviceInfoHelper.getAppVersion())
        } catch (e: Exception) {
            initialized = false
            AdLogger.e(TAG, "Failed to init OM Partner" + e.message)
        }

    }

    /**
     * Called on Ads handshake success if version of script has changed.
     * Should fetch and save in prefs for further use.
     *
     * @param config omSdkConfig
     */
    fun fetchJS(config: OmSdkConfig?) {
        config ?: return
        fetchJSLib(config.serviceJSUrl, AdsPreference.OMID_SERVICE_JS)
        fetchJSLib(config.sessionClientJSUrl, AdsPreference.OMID_SESSION_CLIENT_JS)
    }

    /**
     * Should fetch and save js in prefs for further use.
     */
    private fun fetchJSLib(omidJsUrl: String, preference: AdsPreference) {
        val jsRequest: Call? = try {
            HttpClientManager.newRequestCall(omidJsUrl, Priority.PRIORITY_NORMAL)
        } catch (exception: Exception) {
            AdLogger.e(TAG, "Failed to create request for OM JS script. $exception")
            return
        }
        jsRequest ?: return

        jsRequest.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                AdLogger.d(TAG, "Failed to download the OM JS script. ${e.message}")
                if (preference == AdsPreference.OMID_SERVICE_JS) {
                    isOMSdkEnabled = false
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body() != null) {
                    val jsResponse = response.body()?.string()
                    if (!jsResponse.isNullOrBlank()) {
                        PreferenceManager.savePreference(preference, jsResponse)
                        when (preference) {
                            AdsPreference.OMID_SERVICE_JS -> {
                                omidServiceJs = jsResponse
                            }
                            AdsPreference.OMID_SESSION_CLIENT_JS -> omidSessionClientJs = jsResponse
                        }
                    }
                    isOMSdkEnabled = !CommonUtils.isEmpty(omidServiceJs)
                }
                response.close()
            }
        })
    }

    /**
     * Create and get adSession state associated with a webview.
     * Keeping impression owner always Native.
     *
     * @param adView          webview to be tracked.
     * @param contentUrl Deeplink Url of the screen, ad is added to.
     * @param creativeType Creative type as defined by OM
     * @param impressionType Impression type as defined by OM
     * @param impressionOwner Owner of impression event.
     * @param videoOwner      Owner of video events. Null, if non-video ad.
     * @return adSessionContext
     */
    fun createAdSessionForWebAd(adView: WebView, contentUrl: String?,
                                creativeType: CreativeType, impressionType: ImpressionType,
                                impressionOwner: Owner, videoOwner: Owner?):
            OMSessionState? {
        if (!initialized) {
            init()
        }
        if (!isOMSdkEnabled) {
            return null
        }
        return try {
            val adSessionContext = AdSessionContext.createHtmlAdSessionContext(partner, adView,
                    contentUrl, Constants.EMPTY_STRING)
            val adSession = AdSession.createAdSession(
                    AdSessionConfiguration.createAdSessionConfiguration(creativeType,
                            impressionType, impressionOwner, videoOwner, false), adSessionContext)
            OMSessionState(adSession, impressionOwner)
        } catch (e: Exception) {
            AdLogger.e(TAG, "Failed to create OM adsession for web ad. ${e.message}")
            null
        }

    }

    /**
     * Create and get adSession state associated with a native view.
     *
     * @param vendorsInfo vendorsInfo
     * @param contentUrl Deeplink Url of the screen, ad is added to.
     * @return adSession to fire events
     */
    fun createAdSessionForNativeAd(vendorsInfo: List<OMVendorInfo?>?, contentUrl: String?): OMSessionState? {
        if (!initialized) {
            init()
        }
        if (!isOMSdkEnabled || vendorsInfo.isNullOrEmpty()) {
            return null
        }

        val resources = createVerificationResourcesFrom(vendorsInfo)
        return try {
            val adSessionContext = AdSessionContext.createNativeAdSessionContext(
                    partner, omidServiceJs, resources, contentUrl, Constants.EMPTY_STRING)
            val adSessionConfiguration = AdSessionConfiguration.createAdSessionConfiguration(
                    CreativeType.NATIVE_DISPLAY, ImpressionType.ONE_PIXEL, Owner.NATIVE,
                    Owner.NONE, false)
            OMSessionState(AdSession.createAdSession(adSessionConfiguration, adSessionContext), Owner.NATIVE)
        } catch (e: Exception) {
            AdLogger.e(TAG, "Failed to create OM adsession for native ad. ${e.message}")
            null
        }
    }

    /**
     * Record ad impression associated with an adSession.
     */
    fun recordImpression(adEntity: BaseDisplayAdEntity, uiComponentId: Int) {
        adEntity.omSessionState?.get(uiComponentId)?.let {
            if (!isOMSdkEnabled || adEntity.omImpressionFired ||
                    it.adSession == null || it.impressionOwner == Owner.JAVASCRIPT) {
                return
            }
            return try {
                val adEvents = AdEvents.createAdEvents(it.adSession)
                adEvents.loaded()
                adEvents.impressionOccurred()
                adEntity.omImpressionFired = true
                AdLogger.d(TAG, "OM impression recorded for ${adEntity.uniqueAdIdentifier}")
            } catch (e: Exception) {
                AdLogger.e(TAG, "Failed to record OM Impression. ${e.message}")
            }
        }
    }

    /**
     * Create verificationResource nodes from OMVendorInfo
     *
     * @param vendorsInfos vendors for verification
     */
    private fun createVerificationResourcesFrom(vendorsInfos: List<OMVendorInfo?>?)
            : List<VerificationScriptResource> {
        if (vendorsInfos.isNullOrEmpty()) {
            return ArrayList()
        }

        val verifiers = ArrayList<VerificationScriptResource>()
        for (vendor in vendorsInfos) {
            if (vendor == null) {
                continue
            }
            val verifierJSUrl: URL
            try {
                verifierJSUrl = URL(vendor.javascriptResourceUrl)
            } catch (e: MalformedURLException) {
                Logger.caughtException(e)
                continue
            }

            try {
                verifiers.add(if (vendor.vendorKey.isNullOrBlank() &&
                        vendor.verificationParameters.isNullOrBlank()) {
                    VerificationScriptResource.createVerificationScriptResourceWithoutParameters(verifierJSUrl)
                } else if (vendor.verificationParameters.isNullOrBlank()) {
                    VerificationScriptResource.createVerificationScriptResourceWithoutParameters(vendor.vendorKey, verifierJSUrl)
                } else {
                    VerificationScriptResource.createVerificationScriptResourceWithParameters(vendor.vendorKey, verifierJSUrl, vendor.verificationParameters)
                })
            } catch (e: IllegalArgumentException) {
                Logger.caughtException(e)
            }
        }
        return verifiers
    }

    /**
     * Inject Open Measurement script for tracking the adView.
     *
     * @param adResponseHtml original adResponse (html)
     * @param trackType      OMTracktype. For WEB_VIDEO, client need not handle.
     * @return adResponse with tracking js
     */
    fun injectOMJSInCreative(adResponseHtml: String?, trackType: OMTrackType?): String? {
        var adResponseHtml = adResponseHtml
        if (!isOMSdkEnabled || trackType == null || trackType == OMTrackType.NONE ||
                CommonUtils.isEmpty(adResponseHtml)) {
            return adResponseHtml
        }
        try {
            //OM Session Client JS for web video ads tracking.
            if (trackType == OMTrackType.WEB_VIDEO) {
                if (omidSessionClientJs.isNullOrBlank()) {
                    omidSessionClientJs = PreferenceManager.getPreference(
                            AdsPreference.OMID_SESSION_CLIENT_JS, Constants.EMPTY_STRING)
                }
                adResponseHtml = ScriptInjector.injectScriptContentIntoHtml(omidSessionClientJs, adResponseHtml)
            }
            return ScriptInjector.injectScriptContentIntoHtml(omidServiceJs, adResponseHtml)
        } catch (e: Exception) {
            AdLogger.d(TAG, "Failed to inject OM JS. ${e.message}")
        }

        return adResponseHtml
    }

    fun enableOMSdk(enable: Boolean) {
        if (isOMSdkEnabled != enable) {
            isOMSdkEnabled = enable
            if (enable) {
                init()
            }
        }
    }
}